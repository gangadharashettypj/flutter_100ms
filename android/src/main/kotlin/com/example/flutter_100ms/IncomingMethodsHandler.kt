package com.example.flutter_100ms

import android.content.Context
import androidx.annotation.NonNull
import com.example.flutter_100ms.hms_controller.HmsController
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel


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
    val token = call.argument<String>("token")

    throwIf(userName.isNullOrBlank()) { IllegalArgumentException(userName) }
    throwIf(token.isNullOrBlank()) { IllegalArgumentException(token) }

    try {
        HmsController.instance.join(userName!!, description ?: "{}", token!!)
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

fun bindVideoViewChannelHandler(call: MethodCall, result: MethodChannel.Result) {
    val viewId = call.argument<Int>("ViewId")!!
    val peerId = call.argument<String>("PeerId")!!
    HmsController.instance.viewIds[peerId] = viewId
    HmsController.instance.updateVideos(peerId)
    result.success("OK")
}