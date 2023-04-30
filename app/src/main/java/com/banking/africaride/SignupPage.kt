package com.banking.africaride

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignupPage : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var loginTextView: TextView
    private lateinit var signupButton: Button
    private lateinit var dialog: AlertDialog
    private var parent: ViewGroup? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_signup_page, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        confirmPasswordEditText = view.findViewById(R.id.confirmPasswordEditText)
        signupButton = view.findViewById(R.id.signupButton)
        signupButton.setOnClickListener { initiateSignup() }
        loginTextView = view.findViewById(R.id.loginTextView)
        loginTextView.setOnClickListener { openLoginPage() }

        auth = Firebase.auth
        parent = container

        return view
    }

    private fun initiateSignup() {
        hideKeyboard()
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        when {
            username.isEmpty() -> {
                Toast.makeText(context, "Please enter a valid username", Toast.LENGTH_SHORT).show()
            }
            username.length < 2 -> {
                Toast.makeText(context, "Username must be at least 2 characters long", Toast.LENGTH_SHORT).show()
            }
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
            confirmPassword != password -> {
                Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else -> {
                signupUser(email, password)
            }
        }
    }

    private fun containsAlphabetAndDigit(input: String): Boolean {
        val regex = "^(?=.*[A-Za-z])(?=.*\\d).+\$".toRegex()
        return regex.matches(input)
    }

    private fun signupUser(email: String, password: String) {
        showLoadingPopup()
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity()) { task ->
            if(task.isSuccessful){
                val user = auth.currentUser
                if (user != null){
                    // set user displayName to username
                    updateUsername(user)
                    dialog.dismiss()
                    // send verification email
                    initiateVerification(email)
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

    private fun updateUsername(user: FirebaseUser){
        val username = usernameEditText.text.toString().trim()
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Log.d(tag, "Username successfully updated")
                }
                else {
                    Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun initiateVerification(email: String){
        // Send verification message to mail
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val verificationMsg = getString(R.string.verification_info, email)
                    openVerificationSnackBar(verificationMsg)
                }
                else {
                    val verificationMsg = getString(R.string.verification_error)
                    openVerificationSnackBar(verificationMsg)
                }
            }
    }

    private fun openVerificationSnackBar(verificationMsg: String){
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

    private fun openLoginPage() {
        val navController = findNavController()
        navController.popBackStack()
    }
}