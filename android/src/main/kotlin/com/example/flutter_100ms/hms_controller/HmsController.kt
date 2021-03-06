package com.example.flutter_100ms.hms_controller

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.example.flutter_100ms.Flutter100msPlugin
import com.example.flutter_100ms.constants.OutGoingMethodType
import com.example.flutter_100ms.extensions.sendEvent
import com.example.flutter_100ms.hms_controller.video_view.HMSVideoRenderView
import com.example.flutter_100ms.hms_controller.video_view.HMSVideoRenderViewFactory
import com.example.flutter_100ms.util.*
import com.example.flutter_100ms.util.EmailUtils.TAG
import io.flutter.plugin.common.EventChannel
import kotlinx.android.synthetic.main.grid_item_video.view.*
import kotlinx.android.synthetic.main.video_card.view.*
import live.hms.video.error.HMSException
import live.hms.video.media.settings.HMSTrackSettings
import live.hms.video.media.settings.HMSVideoResolution
import live.hms.video.media.settings.HMSVideoTrackSettings
import live.hms.video.media.tracks.*
import live.hms.video.sdk.HMSAudioListener
import live.hms.video.sdk.HMSPreviewListener
import live.hms.video.sdk.HMSSDK
import live.hms.video.sdk.HMSUpdateListener
import live.hms.video.sdk.models.*
import live.hms.video.sdk.models.enums.HMSAnalyticsEventLevel
import live.hms.video.sdk.models.enums.HMSPeerUpdate
import live.hms.video.sdk.models.enums.HMSRoomUpdate
import live.hms.video.sdk.models.enums.HMSTrackUpdate
import live.hms.video.utils.HMSLogger
import org.webrtc.RendererCommon
import java.util.*
import kotlin.collections.ArrayList


class HmsController {
    private var hmsSDK: HMSSDK? = null
    private var eventSink: EventChannel.EventSink? = null
    private var settings: SettingsStore? = null

    val tracks = Collections.synchronizedList(ArrayList<MeetingTrack>())

    val state = MutableLiveData<MeetingState>(MeetingState.Disconnected())

    // TODO: Listen to changes in publishVideo & publishAudio
    //  when it is possible to switch from Audio/Video only to Audio+Video/Audio/Video/etc
    // Live data for user media controls
    val isLocalAudioEnabled = MutableLiveData(settings?.publishAudio)
    val isLocalVideoEnabled = MutableLiveData(settings?.publishVideo)

    private var localAudioTrack: HMSLocalAudioTrack? = null
    private var localVideoTrack: HMSLocalVideoTrack? = null

    val speakers = MutableLiveData<Array<HMSSpeaker>>()

    // Dominant speaker
    val dominantSpeaker = MutableLiveData<MeetingTrack?>(null)

    val broadcastsReceived = MutableLiveData<ChatMessage>()

    private val failures = ArrayList<HMSException>()

    private var config: HMSConfig? = null

    private val bindedVideoTrackIds = mutableSetOf<String>()

    private val renderedViews = ArrayList<RenderedViewPair>()

    var viewIds = mutableMapOf<String, Int>()

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

    fun update() {
        val videos = Array(tracks.size) { idx ->
            findTrack { it.peer.peerID == tracks[idx].peer.peerID && it.isScreen.not() }
        }
        updateVideos3(videos)
    }

    private fun saveTokenEndpointUrlIfValid(url: String): Boolean {
        if (url.isValidMeetingUrl()) {
            settings?.lastUsedMeetingUrl = url
            settings?.environment = url.getInitEndpointEnvironment()
            return true
        }

        return false
    }

    val peers: Array<HMSPeer>
        get() = hmsSDK!!.getPeers()

    fun <R : Any> mapTracks(transform: (track: MeetingTrack) -> R?): List<R> =
        synchronized(tracks) {
            return tracks.mapNotNull(transform)
        }

    fun getTrackByPeerId(peerId: String): MeetingTrack? = synchronized(tracks) {
        return tracks.find { it.peer.peerID == peerId }
    }

    fun findTrack(predicate: (track: MeetingTrack) -> Boolean): MeetingTrack? =
        synchronized(tracks) {
            return tracks.find(predicate)
        }

    fun startPreview(listener: HMSPreviewListener) {
        // call Preview api
        hmsSDK!!.preview(config!!, listener)
    }

    private fun setLocalVideoEnabled(enabled: Boolean) {

        localVideoTrack?.apply {

            setMute(!enabled)

            isLocalVideoEnabled.postValue(enabled)
//            crashlyticsLog(TAG, "toggleUserVideo: enabled=$enabled")
        }
    }

    fun isLocalVideoEnabled(): Boolean? = localVideoTrack?.isMute?.not()

    fun toggleLocalVideo() {
        localVideoTrack?.let { setLocalVideoEnabled(it.isMute) }
    }

    private fun setLocalAudioEnabled(enabled: Boolean) {

        localAudioTrack?.apply {
            setMute(!enabled)

            isLocalAudioEnabled.postValue(enabled)
//            crashlyticsLog(TAG, "toggleUserMic: enabled=$enabled")
        }

    }

    fun isLocalAudioEnabled(): Boolean? {
        return localAudioTrack?.isMute?.not()
    }

    fun toggleLocalAudio() {
        // If mute then enable audio, if not mute, disable it
        localAudioTrack?.let { setLocalAudioEnabled(it.isMute) }
    }

    private var isAudioMuted: Boolean = false
        set(value) {
            synchronized(tracks) {
                field = value

                val volume = if (isAudioMuted) 0.0 else 1.0
                tracks.forEach { track ->
                    track.audio?.let {
                        if (it is HMSRemoteAudioTrack) {
                            it.setVolume(volume)
                        }
                    }
                }
            }
        }

    fun isPeerAudioEnabled(): Boolean = !isAudioMuted

    /**
     * Helper function to toggle others audio tracks
     */
    fun toggleAudio() {
        setPeerAudioEnabled(isAudioMuted)
    }

    private fun setPeerAudioEnabled(enabled: Boolean) {
        isAudioMuted = !enabled
    }

    fun sendChatMessage(message: String) {
        hmsSDK!!.sendMessage("chat", message)
    }

    private fun cleanup() {
        failures.clear()
        tracks.clear()
        dominantSpeaker.postValue(null)

        localVideoTrack = null
        localAudioTrack = null
    }

    fun join(userName: String, description: String, token: String) {

        if (settings?.lastUsedMeetingUrl?.isBlank() != false) {
            return
        }

        config = HMSConfig(
            userName = userName,
            authtoken = token,
            metadata = description,
        )

        hmsSDK?.join(config!!, hmsUpdateListener)
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

    private fun addAudioTrack(track: HMSAudioTrack, peer: HMSPeer) {
        synchronized(tracks) {
            // Check if this track already exists
            if (track is HMSRemoteAudioTrack) {
                track.setVolume(if (isAudioMuted) 0.0 else 1.0)
            }

            val _track = tracks.find {
                it.audio == null &&
                        it.peer.peerID == peer.peerID &&
                        it.isScreen.not()
            }

            if (_track == null) {
                if (peer.isLocal) {
                    tracks.add(0, MeetingTrack(peer, null, track))
                } else {
                    tracks.add(MeetingTrack(peer, null, track))
                }
            } else {
                _track.audio = track
            }
        }
    }


    private fun addVideoTrack(track: HMSVideoTrack, peer: HMSPeer) {
        synchronized(tracks) {
            // Check if this track already exists
            val _track = tracks.find { it.video == null && it.peer.peerID == peer.peerID }
            if (_track == null) {
                if (peer.isLocal) {
                    tracks.add(0, MeetingTrack(peer, track, null))
                } else {
                    tracks.add(MeetingTrack(peer, track, null))
                }
            } else {
                _track.video = track
            }
        }
    }

    private fun addTrack(track: HMSTrack, peer: HMSPeer) {
        if (track is HMSAudioTrack) addAudioTrack(track, peer)
        else if (track is HMSVideoTrack) addVideoTrack(track, peer)

        Log.v(TAG, "addTrack: count=${tracks.size} track=$track, peer=$peer")
    }

    fun updateTrack(track: HMSTrack, peer: HMSPeer) {
        synchronized(tracks) {
            val trackToUpdate = tracks.find {
                it.peer.peerID == peer.peerID &&
                        it.video?.trackId == track.trackId
            }
            if(track.type == HMSTrackType.AUDIO){
                trackToUpdate!!.audio = track as HMSAudioTrack
            }

            if(track.type == HMSTrackType.VIDEO){
                trackToUpdate!!.video = track as HMSVideoTrack
            }
        }
    }

    private fun removeTrack(track: HMSVideoTrack, peer: HMSPeer) {
        synchronized(tracks) {
            val trackToRemove = tracks.find {
                it.peer.peerID == peer.peerID &&
                        it.video?.trackId == track.trackId
            }
            tracks.remove(trackToRemove)
        }
    }

    fun unbindSurfaceView(
        binding: View,
        item: MeetingTrack,
        metadata: String = ""
    ) {
        if (!bindedVideoTrackIds.contains(item.video?.trackId ?: "")) return

        SurfaceViewRendererUtil.unbind(binding.surface_view, item, metadata).let {
            if (it) {
                binding.surface_view.visibility = View.INVISIBLE
                bindedVideoTrackIds.remove(item.video!!.trackId)
            }
        }
    }

    private fun bindSurfaceView(
        binding: View,
        item: MeetingTrack,
        scalingType: RendererCommon.ScalingType = RendererCommon.ScalingType.SCALE_ASPECT_BALANCED
    ) {
        if (item.video == null || item.video?.isMute == true) return

        binding.surface_view.let { view ->
            Flutter100msPlugin.handler!!.post {
                view.setScalingType(scalingType)
                view.setEnableHardwareScaler(true)
                try{
                    SurfaceViewRendererUtil.bind(view, item).let {
                        if (it) {
                            binding.surface_view.visibility = View.VISIBLE
                            bindedVideoTrackIds.add(item.video!!.trackId)
                        }
                    }
                }
                catch(e:Exception){
                    e.printStackTrace()
                }
            }


        }
    }

    private fun createVideoView(viewId: Int): HMSVideoRenderView {
        return HMSVideoRenderViewFactory.getViewById(viewId)!!
    }

    private fun bindVideo(binding: View, item: MeetingTrack) {
        // FIXME: Add a shared VM with activity scope to subscribe to events
        // binding.container.setOnClickListener { viewModel.onVideoItemClick?.invoke(item) }

        Flutter100msPlugin.handler?.post {
            binding.apply {
                name.text = item.peer.name
                name_initials.text = NameUtils.getInitials(item.peer.name)
                icon_screen_share.visibility = if (item.isScreen) View.VISIBLE else View.GONE
                icon_audio_off.visibility = visibility(
                    item.isScreen.not() &&
                            (item.audio == null || item.audio!!.isMute)
                )

                /** [View.setVisibility] */
                val surfaceViewVisibility = if (item.video == null || item.video?.isMute == true) {
                    View.INVISIBLE
                } else {
                    View.VISIBLE
                }

                if (surface_view.visibility != surfaceViewVisibility) {
                    surface_view.visibility = surfaceViewVisibility
                }
            }
        }
    }


    fun updateVideos() {
        val newVideos: List<MeetingTrack> = tracks.toList()

        val newRenderedViews = ArrayList<RenderedViewPair>()

        // Remove all the views which are not required now
        for (currentRenderedView in renderedViews) {
            val newVideo = newVideos.find { it == currentRenderedView.video }
            if (newVideo == null) {
                unbindSurfaceView(
                    currentRenderedView.binding.video_card,
                    currentRenderedView.video
                )
            }
        }

        for (_newVideo in newVideos) {
            _newVideo.also { newVideo ->

                // Check if video already rendered
                val renderedViewPair = renderedViews.find { it.video == newVideo }
                if (renderedViewPair != null) {
                    newRenderedViews.add(renderedViewPair)

                    if (!bindedVideoTrackIds.contains(newVideo.video?.trackId ?: "")) {
                        bindSurfaceView(renderedViewPair.binding.video_card, newVideo)
                    }

                } else if (viewIds.containsKey(newVideo.peer.peerID)) {
                    // Create a new view
                    try {
                        val videoBinding = createVideoView(viewIds[newVideo.peer.peerID]!!)

                        bindSurfaceView(videoBinding.view, newVideo)

                        newRenderedViews.add(RenderedViewPair(videoBinding.view, newVideo))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        renderedViews.clear()
        renderedViews.addAll(newRenderedViews)

        // Re-bind all the videos, this handles any changes made in isMute
        for (view in renderedViews) {
            bindVideo(view.binding.video_card, view.video)
        }
    }

    fun updateVideos2() {
        val newVideos: List<MeetingTrack> = tracks
        val newRenderedViews = ArrayList<RenderedViewPair>()

        // Remove all the views which are not required now
        for (currentRenderedView in renderedViews) {
            val newVideo = newVideos.find { it == currentRenderedView.video }
            if (newVideo == null) {
                unbindSurfaceView(
                    currentRenderedView.binding.video_card,
                    currentRenderedView.video
                )

            }
        }

        for (_newVideo in newVideos) {
            _newVideo?.also { newVideo ->

                // Check if video already rendered
                val renderedViewPair = renderedViews.find { it.video == newVideo }
                if (renderedViewPair != null) {
                    newRenderedViews.add(renderedViewPair)

                    if (!bindedVideoTrackIds.contains(newVideo.video?.trackId ?: "")) {
                        // This view is not yet initialized (possibly because when AudioTrack was added --
                        // VideoTrack was not present, hence had to create an empty tile)
                        bindSurfaceView(renderedViewPair.binding.video_card, newVideo)
                    }

                } else if (viewIds.containsKey(newVideo.peer.peerID)) {
                    // Create a new view

                    val videoBinding = createVideoView(viewIds[_newVideo.peer.peerID]!!)

                    // Bind surfaceView when view is visible to user
                    bindSurfaceView(videoBinding.view.video_card, newVideo)

                    newRenderedViews.add(RenderedViewPair(videoBinding.view, newVideo))
                }
            }
        }


        renderedViews.clear()
        renderedViews.addAll(newRenderedViews)

        // Re-bind all the videos, this handles any changes made in isMute
        for (view in renderedViews) {
            bindVideo(view.binding.video_card, view.video)
        }
    }

    private fun updateVideos3(newVideos: Array<MeetingTrack?> ) {

        val newRenderedViews = ArrayList<RenderedViewPair>()

        // Remove all the views which are not required now
        for (currentRenderedView in renderedViews) {
            val newVideo = newVideos.find { it == currentRenderedView.video }
            if (newVideo == null) {
                unbindSurfaceView(
                    currentRenderedView.binding.video_card,
                    currentRenderedView.video
                )

            }
        }

        for (_newVideo in newVideos) {
            _newVideo?.also { newVideo ->

                // Check if video already rendered
                val renderedViewPair = renderedViews.find { it.video == newVideo }
                if (renderedViewPair != null) {
                    newRenderedViews.add(renderedViewPair)

                    if (!bindedVideoTrackIds.contains(newVideo.video?.trackId ?: "")) {

                        if(newVideo.peer.isLocal && isLocalAudioEnabled() != true){
                            return
                        }

                        // This view is not yet initialized (possibly because when AudioTrack was added --
                        // VideoTrack was not present, hence had to create an empty tile)
                        bindSurfaceView(renderedViewPair.binding.video_card, newVideo)
                    }

                } else if (viewIds.containsKey(newVideo.peer.peerID)) {
                    // Create a new view

                    val videoBinding = createVideoView(viewIds[_newVideo.peer.peerID]!!)

                    // Bind surfaceView when view is visible to user
                    bindSurfaceView(videoBinding.view.video_card, newVideo)

                    newRenderedViews.add(RenderedViewPair(videoBinding.view, newVideo))
                }
            }
        }


        renderedViews.clear()
        renderedViews.addAll(newRenderedViews)

        // Re-bind all the videos, this handles any changes made in isMute
        for (view in renderedViews) {
            bindVideo(view.binding.video_card, view.video)
        }
    }

    private val hmsUpdateListener = object : HMSUpdateListener {

        override fun onJoin(room: HMSRoom) {
            failures.clear()
            val peer = hmsSDK!!.getLocalPeer()
            peer?.audioTrack?.apply {
                localAudioTrack = this
                isLocalAudioEnabled.postValue(!isMute)
                addTrack(this, peer)
            }
            peer?.videoTrack?.apply {
                localVideoTrack = this
                isLocalVideoEnabled.postValue(!isMute)
                addTrack(this, peer)
            }

            state.postValue(MeetingState.Ongoing())
            eventSink?.sendEvent(
                OutGoingMethodType.ON_JOIN,
                JSONConvertor.hmsRoomToJSON(room).toString()
            )
        }

        override fun onPeerUpdate(type: HMSPeerUpdate, peer: HMSPeer) {
            Log.d(TAG, "join:onPeerUpdate type=$type, peer=$peer")
            when (type) {
                HMSPeerUpdate.PEER_LEFT -> {
                    synchronized(tracks) {
                        for (track in tracks) {
                            if (track.peer.peerID == peer.peerID) {
                                tracks.remove(track)
                            }
                        }
                    }
                }

                HMSPeerUpdate.BECAME_DOMINANT_SPEAKER -> {
                    synchronized(tracks) {
                        val track = tracks.find {
                            it.peer.peerID == peer.peerID &&
                                    it.video?.trackId == peer.videoTrack?.trackId
                        }
                        if (track != null) dominantSpeaker.postValue(track)
                    }
                }

                HMSPeerUpdate.NO_DOMINANT_SPEAKER -> {
                    dominantSpeaker.postValue(null)
                }

                HMSPeerUpdate.AUDIO_TOGGLED -> {
                    synchronized(tracks) {
                        for (track in tracks) {
                            if (track.peer.peerID == peer.peerID) {
                               track.peer = peer
                            }
                        }
                        update()
                    }
                }

                HMSPeerUpdate.VIDEO_TOGGLED -> {
                    synchronized(tracks) {
                        for (track in tracks) {
                            if (track.peer.peerID == peer.peerID) {
                                track.peer = peer
                            }
                        }
                        update()
                    }
                }

                else -> Unit
            }
            val arguments = JSONConvertor.hmsPeerToJSON(peer)
            arguments.put("type", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_PEER_UPDATE, arguments.toString())
        }

        override fun onRoomUpdate(type: HMSRoomUpdate, hmsRoom: HMSRoom) {
            val arguments = JSONConvertor.hmsRoomToJSON(hmsRoom)
            arguments.put("type", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_ROOM_UPDATE, arguments.toString())
        }

        override fun onTrackUpdate(type: HMSTrackUpdate, track: HMSTrack, peer: HMSPeer) {
            Log.d(TAG, "join:onTrackUpdate type=$type, track=$track, peer=$peer")
            when (type) {
                HMSTrackUpdate.TRACK_ADDED -> addTrack(track, peer)
                HMSTrackUpdate.TRACK_REMOVED -> {
                    if (track.type == HMSTrackType.VIDEO) removeTrack(
                        track as HMSVideoTrack,
                        peer
                    )
                }
                HMSTrackUpdate.TRACK_MUTED -> {
                    updateTrack(track,peer)
                }
                HMSTrackUpdate.TRACK_UNMUTED -> {
                    updateTrack(track,peer)
                }
                HMSTrackUpdate.TRACK_DESCRIPTION_CHANGED -> {
                }
            }

            update()
            val arguments = JSONConvertor.hmsPeerToJSON(peer)
            arguments.put("trackStatus", type.name)
            eventSink?.sendEvent(OutGoingMethodType.ON_TRACK_UPDATE, arguments.toString())
        }

        override fun onMessageReceived(message: HMSMessage) {
            eventSink?.sendEvent(
                OutGoingMethodType.ON_MESSAGE_RECEIVED,
                JSONConvertor.hmsMessageToJSON(message).toString()
            )
        }

        override fun onError(error: HMSException) {
            eventSink?.sendEvent(
                OutGoingMethodType.ON_ERROR,
                JSONConvertor.hmsExceptionToJSON(error).toString()
            )
        }

        override fun onReconnecting(error: HMSException) {
            state.postValue(MeetingState.Reconnecting("Reconnecting", error.toString()))
            eventSink?.sendEvent(
                OutGoingMethodType.ON_RECONNECTING,
                JSONConvertor.hmsExceptionToJSON(error).toString()
            )
        }

        override fun onRoleChangeRequest(request: HMSRoleChangeRequest) {
            eventSink?.sendEvent(
                OutGoingMethodType.ON_ROLE_CHANGE_REQUEST,
                JSONConvertor.hmsRoleChangeRequest(request).toString()
            )
        }

        override fun onReconnected() {
            failures.clear()
            eventSink?.sendEvent(OutGoingMethodType.ON_RECONNECTED)
        }
    }
}

class RenderedViewPair(
    val binding: View,
    val video: MeetingTrack
)