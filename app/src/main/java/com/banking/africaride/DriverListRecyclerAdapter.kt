package com.banking.africaride

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

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

        holder.bookVehicleButton.setOnClickListener { bookVehicle(driverDetails.driverId, driverDetails.driverType) }
    }

    private fun bookVehicle(driverId: String, driverType: String) {
        if (driverType == "public"){
            openBookPublicVehicleDialog(driverId)
        }
        else {
            openBookPrivateVehicleDialog(driverId)
        }
    }

    private fun openBookPublicVehicleDialog(driverId: String){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.book_public_vehicle_dialog_layout, null)
        dialogBuilder.setView(dialogView)
        dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun openBookPrivateVehicleDialog(driverId: String){
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = activity.layoutInflater
        val dialogView = inflater.inflate(R.layout.book_private_vehicle_dialog_layout, null)
        dialogBuilder.setView(dialogView)
        dialog = dialogBuilder.create()
        dialog.show()
    }

    override fun getItemCount(): Int {
        return driverDetailsList.size
    }
}