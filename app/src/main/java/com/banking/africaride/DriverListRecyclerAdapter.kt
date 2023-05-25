package com.banking.africaride

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DriverListRecyclerAdapter(
    private val context: Context,
    private val activity: Activity,
    private val driverDetailsList: List<DriverDetails>,
): RecyclerView.Adapter<DriverListRecyclerAdapter.MyViewHolder>() {

    private lateinit var dialog: AlertDialog

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val driverTypeTag: TextView = itemView.findViewById(R.id.driverTypeTag)
        val licencePlateTextView: TextView = itemView.findViewById(R.id.licencePlateTextView)
        val arrivalTimeTextView: TextView = itemView.findViewById(R.id.arrivalTimeTextView)
        val passengerCountTextView: TextView = itemView.findViewById(R.id.passengerCountTextView)
        val startingLocationTextView: TextView = itemView.findViewById(R.id.startingLocationTextView)
        val destinationLocationTextView: TextView = itemView.findViewById(R.id.destinationLocationTextView)
        val bookVehicleButton: Button = itemView.findViewById(R.id.bookVehicleButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.driver_list_item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val driverDetails = driverDetailsList[position]
        holder.driverTypeTag.text = driverDetails.driverType

        val licencePlate = driverDetails.driversLicence
        holder.licencePlateTextView.text = context.getString(R.string.licence_plate_placeholder, licencePlate)

        val arrivalTime = driverDetails.arrivalTime
        holder.arrivalTimeTextView.text = context.getString(R.string.arrival_time_placeholder, arrivalTime)

        val passengerCount = driverDetails.passengerCount
        holder.passengerCountTextView.text = context.getString(R.string.passenger_count_placeholder, passengerCount)

        val startingLocation = driverDetails.startingBusStop
        holder.startingLocationTextView.text = context.getString(R.string.starting_location_placeholder, startingLocation)

        val destinationLocation = driverDetails.destinationBusStop
        holder.destinationLocationTextView.text = context.getString(R.string.destination_location_placeholder, destinationLocation)

        holder.bookVehicleButton.setOnClickListener {
            changeDriverIsActiveStatus(driverDetails.driverId)
            if (driverDetails.driverType == "public"){
                openBookPublicVehicleDialog(driverDetails.driverId, driverDetails.passengerCount)
            }
            else {
                openBookPrivateVehicleDialog(driverDetails.driverId, driverDetails.passengerCount)
            }
        }
    }

    private fun changeDriverIsActiveStatus(driverId: String, isActive: Boolean=false) {
        val db = Firebase.firestore
        val selectedDriverRef = db.collection(DRIVERS_DATA_PATH).document(driverId)
         selectedDriverRef.update("isActive", isActive)

        Toast.makeText(context, "Driver Is Active Status Changed", Toast.LENGTH_SHORT).show()
    }

    private fun openBookPublicVehicleDialog(driverId: String, fixedPassengerCount: Int){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.book_public_vehicle_dialog_layout, null)

        val reducePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.reducePassengerCountButton)
        val increasePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.increasePassengerCountButton)
        val passengerCountTextView = dialogView.findViewById<TextView>(R.id.passengerCountTextView)
        val nextButton = dialogView.findViewById<Button>(R.id.nextButton)
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.cancelButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
            changeDriverIsActiveStatus(driverId, true)
        }

        checkPassengerCount(dialogView, fixedPassengerCount)

        reducePassengerCountButton.setOnClickListener { decrementPassengerCount(dialogView, fixedPassengerCount) }
        increasePassengerCountButton.setOnClickListener { incrementPassengerCount(dialogView, fixedPassengerCount) }

        nextButton.setOnClickListener {
            val passengerCount = passengerCountTextView.text.toString().toInt()
            bookVehicle(driverId, "public", passengerCount)
        }

        dialogBuilder.setView(dialogView)
        dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun openBookPrivateVehicleDialog(driverId: String, fixedPassengerCount: Int){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.book_private_vehicle_dialog_layout, null)

        val reducePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.reducePassengerCountButton)
        val increasePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.increasePassengerCountButton)
        val passengerCountTextView = dialogView.findViewById<TextView>(R.id.passengerCountTextView)
        val pickupAddressTextInput = dialogView.findViewById<EditText>(R.id.pickupAddressTextInputEditText)
        val phoneNumberTextInput = dialogView.findViewById<EditText>(R.id.phoneNumberTextInputEditText)
        val nextButton = dialogView.findViewById<Button>(R.id.nextButton)
        val cancelButton = dialogView.findViewById<ImageButton>(R.id.cancelButton)

        cancelButton.setOnClickListener {
            dialog.dismiss()
            changeDriverIsActiveStatus(driverId, true)
        }

        checkPassengerCount(dialogView, fixedPassengerCount)

        reducePassengerCountButton.setOnClickListener { decrementPassengerCount(dialogView, fixedPassengerCount) }
        increasePassengerCountButton.setOnClickListener { incrementPassengerCount(dialogView, fixedPassengerCount) }

        nextButton.setOnClickListener {
            val passengerCount = passengerCountTextView.text.toString().toInt()
            val pickupAddress = pickupAddressTextInput.text.toString()
            val phoneNumber = phoneNumberTextInput.text.toString()

            bookVehicle(driverId, "private", passengerCount, pickupAddress, phoneNumber)
        }

        dialogBuilder.setView(dialogView)
        dialog = dialogBuilder.create()
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun bookVehicle(
        driverId: String,
        driverType: String,
        passengerCount: Int,
        pickupAddress: String? = null,
        phoneNumber: String? = null)
    {
        dialog.dismiss()
        changeDriverIsActiveStatus(driverId, true)
        Toast.makeText(
            context,
            "Driver Id: $driverId, Driver Type: $driverType, " +
                    "Passenger Count: $passengerCount, " +
                    "Pickup Address: $pickupAddress, " +
                    "Phone Number: $phoneNumber",
            Toast.LENGTH_SHORT).show()
    }

    private fun decrementPassengerCount(dialogView: View, fixedPassengerCount: Int) {
        val passengerCountTextView = dialogView.findViewById<TextView>(R.id.passengerCountTextView)
        var passengerCount = passengerCountTextView.text.toString().toInt()
        passengerCount--
        passengerCountTextView.text = "$passengerCount"
        checkPassengerCount(dialogView, fixedPassengerCount)
    }

    private fun incrementPassengerCount(dialogView: View, fixedPassengerCount: Int) {
        val passengerCountTextView = dialogView.findViewById<TextView>(R.id.passengerCountTextView)
        var passengerCount = passengerCountTextView.text.toString().toInt()
        passengerCount++
        passengerCountTextView.text = "$passengerCount"
        checkPassengerCount(dialogView, fixedPassengerCount)
    }

    private fun checkPassengerCount(dialogView: View, fixedPassengerCount: Int) {
        val reducePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.reducePassengerCountButton)
        val increasePassengerCountButton = dialogView.findViewById<ImageButton>(R.id.increasePassengerCountButton)
        val passengerCountTextView = dialogView.findViewById<TextView>(R.id.passengerCountTextView)

        val passengerCount = passengerCountTextView.text.toString().toInt()

        when {
            passengerCount <= 1 -> {
                passengerCountTextView.text = "1"
                reducePassengerCountButton.isClickable=false

                val purpleLight = ContextCompat.getColor(context, R.color.purple_200)
                reducePassengerCountButton.imageTintList = ColorStateList.valueOf(purpleLight)
            }
            passengerCount >= fixedPassengerCount -> {
                passengerCountTextView.text = "$fixedPassengerCount"
                increasePassengerCountButton.isClickable=false

                val purpleLight = ContextCompat.getColor(context, R.color.purple_200)
                increasePassengerCountButton.imageTintList = ColorStateList.valueOf(purpleLight)
            }
            else -> {
                increasePassengerCountButton.isClickable = true
                reducePassengerCountButton.isClickable = true

                val yellow700 = ContextCompat.getColor(context, R.color.yellow_700)
                increasePassengerCountButton.imageTintList = ColorStateList.valueOf(yellow700)
                reducePassengerCountButton.imageTintList = ColorStateList.valueOf(yellow700)
            }
        }
    }

    override fun getItemCount(): Int {
        return driverDetailsList.size
    }
}