package com.example.flutter_100ms.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RoomDetails(
    val env: String,
    val url: String,
    val username: String,
    val authToken: String
) : Parcelable
