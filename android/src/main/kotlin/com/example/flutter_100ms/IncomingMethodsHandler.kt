package com.example.flutter_100ms

import android.content.Context
import androidx.annotation.NonNull
import com.example.flutter_100ms.hms_controller.HmsController
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import live.hms.video.sdk.models.HMSConfig


fun initMethodChannelHandler(
    @NonNull call: MethodCall,
    @NonNull result: MethodChannel.Result,
    context: Context,
    eventSink: EventChannel.EventSink
) {
    val speaker = call.argument<Boolean>("speaker") ?: false
    HmsController.instance.init(context, speaker = speaker, eventSink = eventSink)
    result.success(true)
}

fun joinMethodChannelHandler(call: MethodCall, result: MethodChannel.Result) {

    val userName = call.argument<String>("userName")
    val description = call.argument<String>("description")

    throwIf(userName.isNullOrBlank()) { IllegalArgumentException(userName) }

    try {
        HmsController.instance.join(userName!!, description ?: "{}")
    } catch (e: Exception) {
        e.printStackTrace()
        result.success(false)
    }

    result.success(true)
}

fun leaveMethodChannelHandler(result: MethodChannel.Result) {
    HmsController.instance.leave()
    result.success(true)
}