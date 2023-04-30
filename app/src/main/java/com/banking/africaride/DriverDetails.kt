package com.banking.africaride

import android.os.Parcel
import android.os.Parcelable

data class DriverDetails(
    val driverId: String,
    val arrivalTime: String,
    val destinationBusStop: String,
    val destinationLGA: String,
    val driverType: String,
    val driversLicence: String,
    val passengerCount: Int,
    val startingBusStop: String,
    val startingLGA: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(driverId)
        parcel.writeString(arrivalTime)
        parcel.writeString(destinationBusStop)
        parcel.writeString(destinationLGA)
        parcel.writeString(driverType)
        parcel.writeString(driversLicence)
        parcel.writeInt(passengerCount)
        parcel.writeString(startingBusStop)
        parcel.writeString(startingLGA)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DriverDetails> {
        override fun createFromParcel(parcel: Parcel): DriverDetails {
            return DriverDetails(parcel)
        }

        override fun newArray(size: Int): Array<DriverDetails?> {
            return arrayOfNulls(size)
        }
    }
}