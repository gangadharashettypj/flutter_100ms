package com.example.flutter_100ms

import android.content.Context
import androidx.annotation.NonNull
import com.example.flutter_100ms.hms_controller.HmsController
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import live.hms.video.media.tracks.HMSTrack
import live.hms.video.media.tracks.HMSVideoTrack


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
    HmsController.instance.viewIds = mutableMapOf()
    HmsController.instance = HmsController()
    result.success(true)
}

fun bindVideoViewChannelHandler(call: MethodCall, result: MethodChannel.Result) {
    val viewId = call.argument<Int>("ViewId")!!
    val peerId = call.argument<String>("PeerId")!!
    HmsController.instance.viewIds[peerId] = viewId
    HmsController.instance.update()
    result.success("OK")
}

fun toggleVideoChannelHandler(result: MethodChannel.Result) {
    HmsController.instance.toggleLocalVideo()
    HmsController.instance.update()
    result.success("OK")
}

fun toggleAudioChannelHandler(result: MethodChannel.Result) {
    HmsController.instance.toggleLocalAudio()
    result.success("OK")
}

fun isVideoEnabledChannelHandler(result: MethodChannel.Result) {
    result.success(HmsController.instance.isLocalVideoEnabled)
}

fun isAudioEnabledChannelHandler(result: MethodChannel.Result) {
    result.success(HmsController.instance.isLocalAudioEnabled)
}