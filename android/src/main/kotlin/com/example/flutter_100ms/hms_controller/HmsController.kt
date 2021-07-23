package com.example.flutter_100ms.hms_controller

import android.content.Context
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.flutter_100ms.api.Status
import com.example.flutter_100ms.constants.OutGoingMethodType
import com.example.flutter_100ms.extensions.sendEvent
import com.example.flutter_100ms.model.RoomDetails
import com.example.flutter_100ms.model.TokenResponse
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
import live.hms.video.utils.toJsonObject
import java.util.*


class HmsController {
    private lateinit var hmsSDK: HMSSDK
    private lateinit var eventSink: EventChannel.EventSink
    private var hmsRequestHandler = HMSRequestHandler()
    private lateinit var settings: SettingsStore

    companion object {
        var instance = HmsController()
    }

    fun init(context: Context, speaker: Boolean = false, eventSink: EventChannel.EventSink) {
        this.eventSink = eventSink

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
    }

    fun join(userName: String, description: String) {

        hmsRequestHandler.authTokenResponse.observeForever { response ->
            when (response.status) {
                Status.LOADING -> {

                }
                Status.SUCCESS -> {
                    val data = response.data!!
                    val roomDetails = RoomDetails(
                        env = settings.environment,
                        url = settings.lastUsedMeetingUrl,
                        username = userName,
                        authToken = data.token
                    )
                    Log.i("HMS", "Auth Token: ${roomDetails.authToken}")

                    val config = HMSConfig(
                        userName = userName,
                        authtoken = data.token,
                        metadata = description,
                        initEndpoint = "https://${settings.environment}.100ms.live/init"
                    )

                    hmsSDK.join(config, hmsUpdateListener)
                }
                Status.ERROR -> {
                    Log.e("HMS", "observeLiveData: $response")
                }
            }
        }
    }

    fun leave() {
        hmsSDK.leave()
    }

    fun getLocalPeer(): HMSPeer {
        return hmsSDK.getLocalPeer()!!
    }

    fun getRemotePeers(): List<HMSPeer> {
        return hmsSDK.getRemotePeers().toList()
    }

    fun getPeers(): List<HMSPeer> {
        return hmsSDK.getPeers().toList()
    }

    fun sendMessage(type: String, message: String) {
        hmsSDK.sendMessage(type, message)
    }

    fun addAudioObserver(observer: HMSAudioListener) {
        hmsSDK.addAudioObserver(observer)
    }

    fun removeAudioObserver() {
        hmsSDK.removeAudioObserver()
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
            eventSink.sendEvent(OutGoingMethodType.ON_JOIN, room.toJson())
        }

        override fun onPeerUpdate(type: HMSPeerUpdate, peer: HMSPeer) {
            val arguments = peer.toJsonObject()
            arguments.addProperty("type", type.name)
            eventSink.sendEvent(OutGoingMethodType.ON_PEER_UPDATE, arguments.toJson())
        }

        override fun onRoomUpdate(type: HMSRoomUpdate, hmsRoom: HMSRoom) {
            val arguments = hmsRoom.toJsonObject()
            arguments.addProperty("type", type.name)
            eventSink.sendEvent(OutGoingMethodType.ON_ROOM_UPDATE, arguments.toJson())
        }

        override fun onTrackUpdate(type: HMSTrackUpdate, track: HMSTrack, peer: HMSPeer) {
            val arguments = track.toJsonObject()
            arguments.addProperty("type", type.name)
            arguments.addProperty("peer", peer.toJson())
            eventSink.sendEvent(OutGoingMethodType.ON_TRACK_UPDATE, arguments.toJson())
        }

        override fun onMessageReceived(message: HMSMessage) {
            eventSink.sendEvent(OutGoingMethodType.ON_MESSAGE_RECEIVED, message.toJson())
        }

        override fun onError(error: HMSException) {
            eventSink.sendEvent(OutGoingMethodType.ON_ERROR, error.toJson())
        }

        override fun onReconnecting(error: HMSException) {
            eventSink.sendEvent(OutGoingMethodType.ON_RECONNECTING, error.toJson())
        }

        override fun onRoleChangeRequest(request: HMSRoleChangeRequest) {
            eventSink.sendEvent(OutGoingMethodType.ON_ROLE_CHANGE_REQUEST, request.toJson())
        }

        override fun onReconnected() {
            eventSink.sendEvent(OutGoingMethodType.ON_RECONNECTED)
        }
    }
}