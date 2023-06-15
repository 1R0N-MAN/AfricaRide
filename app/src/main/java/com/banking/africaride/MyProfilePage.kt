package com.banking.africaride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyProfilePage : Fragment() {

    private lateinit var usernameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var phoneNumberInput: EditText
    private lateinit var stateInput: Spinner
    private lateinit var saveChangesButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_profile_page, container, false)

        usernameInput = view.findViewById(R.id.usernameEditText)
        emailInput = view.findViewById(R.id.emailEditText)
        emailInput.setOnClickListener{
            Toast.makeText(context, "Email address cannot be modified!", Toast.LENGTH_SHORT).show()
        }

        phoneNumberInput = view.findViewById(R.id.phoneNumberEditText)
        stateInput = view.findViewById(R.id.stateSpinner)
        saveChangesButton = view.findViewById(R.id.saveChangesButton)
        saveChangesButton.setOnClickListener { saveChanges() }
        auth = Firebase.auth
        db = Firebase.firestore

        getUserData()
        return view
    }

    private fun getUserData(){
        val userData = db.collection(MY_PROFILE_DATA_PATH).document("${auth.uid}")
        userData.get()
            .addOnSuccessListener { result ->
                Log.d(TAG, "Result: ${result.data}")
                val phoneNumber = result["phoneNumber"]
                val stateOfResidencePosition = result["stateOfResidencePosition"]
                loadUI(phoneNumber, stateOfResidencePosition)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting user data!", exception)
            }
    }

    private fun loadUI(phoneNumber: Any?, stateOfResidencePosition: Any?) {
        val user = auth.currentUser

        val username = user?.displayName
        usernameInput.setText(username)

        val email = user?.email
        emailInput.setText(email)

        if (phoneNumber != null) {
            phoneNumberInput.setText(phoneNumber.toString())
        }

        if (stateOfResidencePosition != null) {
            inflateSpinner(stateOfResidencePosition.toString().toInt())
        } else {
            inflateSpinner(0)
        }
    }

    private fun inflateSpinner(stateOfResidencePosition: Int) {
        if(context != null){
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                states
            )
            stateInput.adapter = adapter
            stateInput.setSelection(stateOfResidencePosition)
        }
    }

    private fun saveChanges() {
        val newUsername = usernameInput.text.toString()
        val newPhoneNumber = phoneNumberInput.text.toString()
        val newStateOfResidencePosition = stateInput.selectedItemPosition


        val userData = hashMapOf(
            "username" to newUsername,
            "phoneNumber" to newPhoneNumber,
            "stateOfResidencePosition" to newStateOfResidencePosition
        )

        updateUsername(newUsername)

        db.collection(MY_PROFILE_DATA_PATH).document("${auth.uid}").set(userData)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Document Snapshot added with ID: $documentReference")
                Toast.makeText(context, "User Profile Saved Successfully!", Toast.LENGTH_SHORT).show()
                hideKeyboard()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(context, "Error updating user profile!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUsername(username: String){
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(username)
            .build()

        auth.currentUser?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Log.d(TAG, "Username successfully updated")
                }
                else {
                    Toast.makeText(requireContext(), "Failed to update username", Toast.LENGTH_SHORT).show()
                }
            }
    }
}