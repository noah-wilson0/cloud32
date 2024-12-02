package com.example.childgps.entity

import com.google.gson.annotations.SerializedName

data class LocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
