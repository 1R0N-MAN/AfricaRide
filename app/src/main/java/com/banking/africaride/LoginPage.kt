package com.banking.africaride

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginPage : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var signupTextView: TextView
    private lateinit var forgotPasswordTextView: TextView
    private lateinit var navController: NavController
    private lateinit var dialog: AlertDialog
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var keepMeLoggedInCheckBox: CheckBox
    private var parent: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login_page, container, false)

        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        loginButton.setOnClickListener { initiateLogin() }
        signupTextView = view.findViewById(R.id.signUpTextView)
        signupTextView.setOnClickListener { openSignupPage() }
        forgotPasswordTextView = view.findViewById(R.id.forgotPasswordTextView)
        forgotPasswordTextView.setOnClickListener { openForgotPasswordPage() }
        sharedPreferences = requireContext().getSharedPreferences(AFRICA_RIDE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        keepMeLoggedInCheckBox = view.findViewById(R.id.rememberMeCheckBox)
        navController = findNavController()

        auth = Firebase.auth
        parent = container

        checkIfUserIsLoggedIn()
        return view
    }

    private fun initiateLogin(){
        hideKeyboard()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        when {
            email.isEmpty() -> {
                Toast.makeText(context, "Please enter your email address", Toast.LENGTH_SHORT).show()
            }
            password.isEmpty() -> {
                Toast.makeText(context, "Please enter your password", Toast.LENGTH_SHORT).show()
            }
            password.length < 8 -> {
                Toast.makeText(context, "Password must contain at least 8 characters", Toast.LENGTH_SHORT).show()
            }
            !containsAlphabetAndDigit(password) -> {
                Toast.makeText(context, "Password must contain at least one alphabet and one digit", Toast.LENGTH_SHORT).show()
            }
            else -> {
                loginUser(email, password)
            }
        }
    }

    private fun containsAlphabetAndDigit(input: String): Boolean {
        val regex = "^(?=.*[A-Za-z])(?=.*\\d).+\$".toRegex()
        return regex.matches(input)
    }

    private fun loginUser(email: String, password: String) {
        showLoadingPopup()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val user = auth.currentUser
                val editor = sharedPreferences.edit()
                if (user != null){
                    // check if email is verified
                    if (user.isEmailVerified){
                        dialog.dismiss()
                        // Store the Keep Me Logged in state so users don't have to login later
                        val keepMeLoggedIn = keepMeLoggedInCheckBox.isChecked
                        editor.putBoolean(REMEMBER_ME_BOOLEAN_VALUE_SHPR, keepMeLoggedIn)
                        editor.apply()

                        openHomePage()
                    } else {
                        dialog.dismiss()
                        // send verification email if email is not verified
                        initiateVerification(email)
                    }
                }
            }
        }.addOnFailureListener { exception ->
            dialog.dismiss()
            Toast.makeText(requireContext(), exception.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoadingPopup() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog_layout, null)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setCancelable(false)
        dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun openHomePage() {
        Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
        // Navigate to the Main Page
        navController.navigate(R.id.action_loginPage_to_homePage)
    }

    private fun initiateVerification(email: String){
        // Send verification message to mail
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val verificationMsg = getString(R.string.verification_info, email)
                    openVerificationSnackBar(verificationMsg, parent)
                }
                else {
                    val verificationMsg = getString(R.string.verification_error)
                    openVerificationSnackBar(verificationMsg, parent)
                }
            }
    }

    private fun openVerificationSnackBar(verificationMsg: String, parent: ViewGroup?){
        val snackbar = Snackbar.make(requireView(), "", Snackbar.LENGTH_INDEFINITE)

        val verificationSnackBar = layoutInflater.inflate(R.layout.verification_snackbar_layout, parent, false)
        val verificationInfo = verificationSnackBar.findViewById<TextView>(R.id.verificationInfo)
        verificationInfo.text = verificationMsg

        snackbar.view.setBackgroundColor(Color.TRANSPARENT)

        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(0, 0, 0, 0)

        val resendVerificationMailButton = verificationSnackBar.findViewById<Button>(R.id.resendVerificationMailButton)
        resendVerificationMailButton.setOnClickListener {
            Toast.makeText(requireContext(), "Resending verification mail...", Toast.LENGTH_SHORT).show()
            resendVerificationEmail()
            snackbar.dismiss()
        }

        val closeVerificationSnackbarButton = verificationSnackBar.findViewById<ImageButton>(R.id.closeVerificationSnackbarButton)
        closeVerificationSnackbarButton.setOnClickListener { snackbar.dismiss() }

        snackbarLayout.addView(verificationSnackBar, 0)
        snackbar.show()
    }

    private fun resendVerificationEmail() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    Toast.makeText(
                        context,
                        "Verification Email Sent Successfully!",
                        Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(
                        context,
                        "Verification Email Not Sent! Please try again later.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfUserIsLoggedIn() {
        if (auth.currentUser != null){
            val isLoggedIn = sharedPreferences.getBoolean(REMEMBER_ME_BOOLEAN_VALUE_SHPR, false)

            if (isLoggedIn){
                // Open NigMart Store
                val action = R.id.action_loginPage_to_homePage
                navController.navigate(action)
            }
            else {
                Log.d(tag, "User not logged in! Opening Login Page...")
            }
        }
    }

    private fun openSignupPage() {
        navController.navigate(R.id.action_loginPage_to_signupPage)
    }

    private fun openForgotPasswordPage(){
        navController.navigate(R.id.action_loginPage_to_forgotPasswordPage)
    }
}