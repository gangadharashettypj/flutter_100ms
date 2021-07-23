package com.example.flutter_100ms.hms_controller

import com.example.flutter_100ms.api.RetrofitBuilder
import com.example.flutter_100ms.model.TokenResponse
import okhttp3.Request

class HomeRepository {

    fun fetchAuthToken(request: Request): TokenResponse {
        return RetrofitBuilder.fetchAuthToken(request)
    }
}