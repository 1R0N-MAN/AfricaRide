package com.banking.africaride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class FindADriverPage : Fragment() {

    private lateinit var selectStateSpinnerStart: Spinner
    private lateinit var selectLGASpinnerStart: Spinner
    private lateinit var selectStateSpinnerDestination: Spinner
    private lateinit var selectLGASpinnerDestination: Spinner
    private lateinit var privateVehiclesCheckbox: CheckBox
    private lateinit var publicVehiclesCheckbox: CheckBox
    private lateinit var findDriverButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_find_a_driver_page, container, false)

        selectStateSpinnerStart = view.findViewById(R.id.selectStateSpinnerStart)
        selectLGASpinnerStart = view.findViewById(R.id.selectLGASpinnerStart)
        selectStateSpinnerDestination = view.findViewById(R.id.selectStateSpinnerDestination)
        selectLGASpinnerDestination = view.findViewById(R.id.selectLGASpinnerDestination)
        privateVehiclesCheckbox = view.findViewById(R.id.privateVehiclesCheckBox)
        publicVehiclesCheckbox = view.findViewById(R.id.publicVehiclesCheckBox)
        findDriverButton = view.findViewById(R.id.findDriverButton)
        findDriverButton.setOnClickListener { findDriver() }

        inflateStateSpinners(0)
        inflateLGASpinner(selectLGASpinnerStart, 0)
        inflateLGASpinner(selectLGASpinnerDestination, 0)

        auth = Firebase.auth
        db = Firebase.firestore

        loadUserStateOfResidence()

        return view
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

    private fun inflateStateSpinners(position: Int) {
        val stateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, states)

        selectStateSpinnerStart.adapter = stateAdapter
        selectStateSpinnerDestination.adapter = stateAdapter

        selectStateSpinnerStart.setSelection(position)
        selectStateSpinnerDestination.setSelection(position)

        selectStateSpinnerStart.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                inflateLGASpinner(selectLGASpinnerStart, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        selectStateSpinnerDestination.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                inflateLGASpinner(selectLGASpinnerDestination, position)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun inflateLGASpinner(spinner: Spinner, position: Int){
        val initPosition = 0
        val selectedState = states[position]
        val localGovtAreas = statesAndLGAs[selectedState]

        if (localGovtAreas != null){
            val lgaAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                localGovtAreas
            )

            spinner.adapter = lgaAdapter
            spinner.setSelection(initPosition)
        }
    }

    private fun loadUserStateOfResidence(){
        val userData = db.collection(MY_PROFILE_DATA_PATH).document("${auth.uid}")
        userData.get()
            .addOnSuccessListener { result ->
                val stateOfResidencePosition = result["stateOfResidencePosition"]
                if (stateOfResidencePosition != null) {
                    inflateStateSpinners(stateOfResidencePosition.toString().toInt())
                } else {
                    inflateStateSpinners(0)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting user data!", exception)
            }
    }

    private fun findDriver() {
        showLoadingPopup()

        val startLGA = selectLGASpinnerStart.selectedItem.toString()
        val destinationLGA = selectLGASpinnerDestination.selectedItem.toString()

        val showPrivateVehicles = privateVehiclesCheckbox.isChecked
        val showPublicVehicles = publicVehiclesCheckbox.isChecked

        val driversDataRef = db.collection(DRIVERS_DATA_PATH).whereEqualTo("isActive", true)
        val availableDrivers = if(!showPrivateVehicles && showPublicVehicles){
            driversDataRef.whereEqualTo("startingLGA", startLGA)
                .whereEqualTo("destinationLGA", destinationLGA)
                .whereEqualTo("driverType", "public")
        } else if (!showPublicVehicles && showPrivateVehicles){
            driversDataRef.whereEqualTo("startingLGA", startLGA)
                .whereEqualTo("destinationLGA", destinationLGA)
                .whereEqualTo("driverType", "private")
        } else {
            driversDataRef.whereEqualTo("startingLGA", startLGA)
                .whereEqualTo("destinationLGA", destinationLGA)
        }

        availableDrivers.get()
            .addOnSuccessListener { documents ->
                val driverDetailsList = mutableListOf<DriverDetails>()

                for (document in documents) {
                    val driverDetails = loadDriverDetails(document)
                    driverDetailsList.add(driverDetails)
                }

                dialog.dismiss()

                val bundle = Bundle()
                bundle.putParcelableArrayList(DRIVER_DETAILS_LIST, ArrayList(driverDetailsList))
                findNavController().navigate(R.id.action_findADriverPage_to_driverListsPage, bundle)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                Toast.makeText(
                    context,
                    "There was a problem finding drivers! Please try again!",
                    Toast.LENGTH_LONG).show()

                dialog.dismiss()
            }
    }

    private fun loadDriverDetails(document: QueryDocumentSnapshot): DriverDetails {
        val data = document.data

        val driverId = document.id
        val arrivalTime = data["arrivalTime"].toString()
        val destinationBusStop = data["destinationBusStop"].toString()
        val destinationLGA = data["destinationLGA"].toString()
        val driverType = data["driverType"].toString()
        val driversLicence = data["driversLicence"].toString()
        val passengerCount = data["passengerCount"].toString().toInt()
        val startingBusStop = data["startingBusStop"].toString()
        val startingLGA = data["startingLGA"].toString()

        return DriverDetails(
            driverId,
            arrivalTime,
            destinationBusStop,
            destinationLGA,
            driverType,
            driversLicence,
            passengerCount,
            startingBusStop,
            startingLGA
        )
    }
}