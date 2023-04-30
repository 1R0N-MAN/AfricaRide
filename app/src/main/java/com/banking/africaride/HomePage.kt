package com.banking.africaride

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomePage : Fragment() {

    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbarTitle: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var navMenu: ImageButton
    private lateinit var nestedNavController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home_page, container, false)

        val nestedNavHostFragment = childFragmentManager.findFragmentById(R.id.homePageNavHostFragment) as NavHostFragment
        nestedNavController = nestedNavHostFragment.navController

        // Implement the nav drawer
        drawerLayout = view.findViewById(R.id.drawer_layout)
        actionBarToggle = ActionBarDrawerToggle(requireActivity(), drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        navMenu = view.findViewById(R.id.nav_menu)
        navMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val navView = view.findViewById<NavigationView>(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.myProfile -> {
                    findNavController().navigate(R.id.action_homePage_to_myProfilePage)
                }
                R.id.myWallet -> {
                    displayMessage("My Wallet Selected")
                }
                R.id.pendingRides -> {
                    displayMessage("Pending Rides Selected")
                }
                R.id.history -> {
                    displayMessage("History Page Selected")
                }
                R.id.logout -> {
                    logoutUser()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        toolbarTitle = view.findViewById(R.id.toolbarTitle)

        auth = Firebase.auth

        loadUI(navView)

        return view
    }

//    private fun changeNavMenuFunctionality(pageName: String){
//        toolbarTitle.text = pageName
//
//        navMenu.setImageResource(R.drawable.ic_go_back)
//        navMenu.setOnClickListener {
//            resetNavMenuFunctionality()
//            nestedNavController.popBackStack()
//        }
//    }

//    private fun resetNavMenuFunctionality() {
//
//        val username = auth.currentUser?.displayName
//        toolbarTitle.text = getString(R.string.hey_username, username)
//
//        navMenu.setImageResource(R.drawable.ic_menu)
//        navMenu.setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
//
//        Toast.makeText(context, "Nav Menu Functionality Reset!", Toast.LENGTH_SHORT).show()
//    }

    private fun logoutUser() {
        auth.signOut()
        resetRememberMe()
        displayMessage("Logging out user")
        findNavController().navigate(R.id.action_homePage_to_loginPage)
    }

    private fun resetRememberMe() {
        val sharedPreferences = requireContext().getSharedPreferences(
            AFRICA_RIDE_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        sharedPreferences.edit()
            .putBoolean(REMEMBER_ME_BOOLEAN_VALUE_SHPR, false)
            .apply()
    }


    private fun loadUI(navView: NavigationView) {
        val header = navView.getHeaderView(0)

        // Set user email and username to nav drawer profile
        val user = auth.currentUser
        val username = user?.displayName
        val email = user?.email

        val navDrawerUsername = header.findViewById<TextView>(R.id.navDrawerUserName)
        val navDrawerEmail = header.findViewById<TextView>(R.id.navDrawerEmail)

        navDrawerUsername.text = username
        navDrawerEmail.text = email

        toolbarTitle.text = getString(R.string.hey_username, username)

    }

    private fun displayMessage(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}