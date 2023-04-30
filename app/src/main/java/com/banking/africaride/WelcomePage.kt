package com.banking.africaride

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.Navigation

class WelcomePage : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_welcome_page, container, false)

        val findDriverButton = view.findViewById<Button>(R.id.findDriverButton)
        findDriverButton.setOnClickListener { navigateToFindDriverPage() }
        return view
    }

    private fun navigateToFindDriverPage() {
        val navController = Navigation.findNavController(requireActivity(), R.id.activityMainNavHostFragment)
        navController.navigate(R.id.action_homePage_to_findADriverPage)
    }
}