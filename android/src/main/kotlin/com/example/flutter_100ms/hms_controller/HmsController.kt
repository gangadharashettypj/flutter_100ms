package com.example.flutter_100ms.hms_controller

import android.content.Context
import com.example.flutter_100ms.constants.OutGoingMethodType
import com.example.flutter_100ms.extensions.sendEvent
import com.example.flutter_100ms.util.JSONUtils
import com.example.flutter_100ms.util.getInitEndpointEnvironment
import com.example.flutter_100ms.util.isValidMeetingUrl
import io.flutter.plugin.common.EventChannel
import live.hms.video.error.HMSException
import live.hms.video.media.settings.HMSTrackSettings
import live.hms.video.media.settings.HMSVideoResolution
import live.hms.video.media.settings.HMSVideoTrackSettings
import live.hms.video.media.tracks.HMSTrack
import live.hms.video.sdk.HMSAudioListener
import live.hms.video.sdk.HMSSDK
import live.hms.video.sdk.HMSUpdateListener
import live.hms.video.sdk.models.*
import live.hms.video.sdk.models.enums.HMSAnalyticsEventLevel
import live.hms.video.sdk.models.enums.HMSPeerUpdate
import live.hms.video.sdk.models.enums.HMSRoomUpdate
import live.hms.video.sdk.models.enums.HMSTrackUpdate
import live.hms.video.utils.HMSLogger
import live.hms.video.utils.toJson


class HmsController {
    private var hmsSDK: HMSSDK? = null
    private var eventSink: EventChannel.EventSink? = null
    private var settings: SettingsStore? = null

    companion object {
        var instance = HmsController()
    }

    fun init(context: Context, speaker: Boolean = false, eventSink: EventChannel.EventSink) {
        this.eventSink = eventSink

        settings = SettingsStore(context)

        val hmsVideoResolution =
            HMSVideoResolution(width = getWidth(speaker), height = getHeight(speaker))

        val hmsVideoTrackSettings = HMSVideoTrackSettings.Builder()
            .resolution(hmsVideoResolution)
            .build()
        val hmsTrackSettings = HMSTrackSettings.Builder()
            .video(hmsVideoTrackSettings)
            .build()

        hmsSDK = HMSSDK
            .Builder(context)
            .setTrackSettings(hmsTrackSettings)
            .setAnalyticEventLevel(HMSAnalyticsEventLevel.ERROR)
            .setLogLevel(HMSLogger.LogLevel.ERROR)
            .build()

        saveTokenEndpointUrlIfValid("https://frontrow.app.100ms.live/meeting/tasty-auburn-gorilla")
    }

    private fun saveTokenEndpointUrlIfValid(url: String): Boolean {
        if (url.isValidMeetingUrl()) {
            settings?.lastUsedMeetingUrl = url
            settings?.environment = url.getInitEndpointEnvironment()
            return true
        }

        return false
    }

    fun join(userName: String, description: String, token: String) {

        if (settings?.lastUsedMeetingUrl?.isBlank() != false) {
            return
        }

        val config = HMSConfig(
            userName = userName,
            authtoken = token,
            metadata = description,
            initEndpoint = "https://${settings!!.environment}.100ms.live/init"
        )

        hmsSDK?.join(config, hmsUpdateListener)
    }

    fun leave() {
        try {
            hmsSDK?.leave()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLocalPeer(): HMSPeer {
        return hmsSDK?.getLocalPeer()!!
    }

    fun getRemotePeers(): List<HMSPeer>? {
        return hmsSDK?.getRemotePeers()?.toList()
    }

    fun getPeers(): List<HMSPeer>? {
        return hmsSDK?.getPeers()?.toList()
    }

    fun sendMessage(type: String, message: String) {
        hmsSDK?.sendMessage(type, message)
    }

    fun addAudioObserver(observer: HMSAudioListener) {
        hmsSDK?.addAudioObserver(observer)
    }

    fun removeAudioObserver() {
        hmsSDK?.removeAudioObserver()
    }

    private fun getWidth(speaker: Boolean): Int {
        if (speaker) {
            return 320
        }
        return 120

    }

    private fun getHeight(speaker: Boolean): Int {
        if (speaker) {
            return 320
        }
        return 120
    }

    private val hmsUpdateListener = object : HMSUpdateListener {

        override fun onJoin(room: HMSRoom) {
            eventSink?.sendEvent(OutGoingMethodType.ON_JOIN, JSONUtils.hmsRoomToJSON(room).toString())
        }

        override fun onPeerUpdate(type: HMSPeerUpdate, peer: HMSPeer) {
            val arguments = JSONUtils.hmsPeerToJSON(peer)
            arguments.put("type", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_PEER_UPDATE, arguments.toString())
        }

        override fun onRoomUpdate(type: HMSRoomUpdate, hmsRoom: HMSRoom) {
            val arguments = JSONUtils.hmsRoomToJSON(hmsRoom)
            arguments.put("type", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_ROOM_UPDATE, arguments.toString())
        }

        override fun onTrackUpdate(type: HMSTrackUpdate, track: HMSTrack, peer: HMSPeer) {
            val arguments = JSONUtils.hmsPeerToJSON(peer)
            arguments.put("trackStatus", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_TRACK_UPDATE, arguments.toString())
        }

        override fun onMessageReceived(message: HMSMessage) {
            eventSink?.sendEvent(OutGoingMethodType.ON_MESSAGE_RECEIVED, JSONUtils.hmsMessageToJSON(message).toString())
        }

        override fun onError(error: HMSException) {
            eventSink?.sendEvent(OutGoingMethodType.ON_ERROR, JSONUtils.hmsExceptionToJSON(error).toString())
        }

        override fun onReconnecting(error: HMSException) {
            eventSink?.sendEvent(OutGoingMethodType.ON_RECONNECTING, JSONUtils.hmsExceptionToJSON(error).toString())
        }

        override fun onRoleChangeRequest(request: HMSRoleChangeRequest) {
            eventSink?.sendEvent(OutGoingMethodType.ON_ROLE_CHANGE_REQUEST, JSONUtils.hmsRoleChangeRequest(request).toString())
        }

        override fun onReconnected() {
            eventSink?.sendEvent(OutGoingMethodType.ON_RECONNECTED)
        }
    }
}