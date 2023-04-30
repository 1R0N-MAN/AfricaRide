package com.banking.africaride

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class DriverListsPage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_driver_lists_page, container, false)

        val driverDetailsList = arguments?.getParcelableArrayList<DriverDetails>(DRIVER_DETAILS_LIST)

        if (driverDetailsList != null) {
            val driverListRecyclerView = view.findViewById<RecyclerView>(R.id.driverListRecyclerView)
            val driverListRecyclerAdapter= DriverListRecyclerAdapter(requireContext(), requireActivity(), driverDetailsList)
            driverListRecyclerView.adapter = driverListRecyclerAdapter

            Log.d(TAG, "Driver List: $driverDetailsList")
            if (driverDetailsList.isEmpty()){
                Toast.makeText(context, "Driver list is empty, please try another search", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Driver list not found", Toast.LENGTH_SHORT).show()
        }
        return view
    }
}