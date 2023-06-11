package com.banking.africaride

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

const val AFRICA_RIDE_SHARED_PREFERENCES = "AFRICA_RIDE_SHARED_PREFERENCES_LOGIN"
const val REMEMBER_ME_BOOLEAN_VALUE_SHPR = "REMEMBER_ME_SHARED_PREFERENCES"
const val MY_PROFILE_DATA_PATH = "user-data"
const val DRIVERS_DATA_PATH = "drivers"
const val PASSENGER_DATA_PATH = "passenger-data"
const val TAG = "Africa Ride Debugging"
const val DRIVER_DETAILS_LIST = "driverDetailsList"

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

val states = listOf("Abia","Adamawa","Akwa Ibom","Anambra", "Bauchi","Bayelsa","Benue","Borno",
    "Cross River", "Delta","Ebonyi","Edo","Ekiti","Enugu","Gombe", "Imo","Jigawa","Kaduna","Kano",
    "Katsina","Kebbi", "Kogi","Kwara","Lagos","Nasarawa","Niger","Ogun","Ondo","Osun","Oyo",
    "Plateau","Rivers","Sokoto","Taraba","Yobe","Zamfara")

