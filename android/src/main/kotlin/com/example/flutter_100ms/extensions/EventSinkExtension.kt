package com.example.flutter_100ms.extensions

import com.example.flutter_100ms.Flutter100msPlugin
import com.example.flutter_100ms.constants.OutGoingMethodType
import io.flutter.plugin.common.EventChannel
import live.hms.video.utils.toJson
import org.json.JSONObject


fun EventChannel.EventSink.sendEvent(eventType: OutGoingMethodType, arguments: String = "") {
    val json = JSONObject()
    json.put("eventType", eventType.name)
    if (arguments.isNotBlank()) {
        json.put("arguments", arguments)
    }

    Flutter100msPlugin.handler?.post {
        this.success(json.toJson())
    }
}