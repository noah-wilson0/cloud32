package com.example.childgps.api

import android.util.Log
import com.example.childgps.entity.LocationData
import com.kakao.vectormap.LatLng


class ApiClient {
    suspend fun getAllLocations(): List<LocationData>? {
        return try {
            RetrofitInstance.api.getAllLocations("getLocations")
        } catch (e: Exception) {
            Log.e("ApiClient", "API 호출 실패: ${e.localizedMessage}", e)
            null
        }
    }
    // LocationData 리스트를 LatLng 리스트로 변환
    fun convertToLatLngList(locations: List<LocationData>): List<LatLng> {
        return locations.map { LatLng.from(it.latitude, it.longitude) }
    }

    suspend fun insertCallValue(value: Int): Boolean {
        return try {
            val response = RetrofitInstance.api.insertCallValue(CallRequest("insertCall", value))
            if (response.isSuccessful) {
                Log.i("ApiClient", "Call value inserted successfully.")
                true
            } else {
                Log.e("ApiClient", "Failed to insert call value. Code: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            Log.e("ApiClient", "API call failed: ${e.localizedMessage}", e)
            false
        }
    }
}
