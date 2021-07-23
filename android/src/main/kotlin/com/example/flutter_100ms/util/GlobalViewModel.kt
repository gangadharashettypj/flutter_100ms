package com.example.flutter_100ms.util

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.flutter_100ms.api.Resource
import retrofit2.Response

private const val TAG = "GlobalViewModel"

fun <T> ViewModel.handleResponse(response: Response<T>, errorMessage: String? = null): Resource<T> {
    if (response.isSuccessful) {
        response.body()?.let { body ->
            Log.d(TAG, body.toString())
            return Resource.success(body)
        }
    }

    // TODO: Implement an optional callback which parses the error message from the response
    return Resource.error(errorMessage)
}

