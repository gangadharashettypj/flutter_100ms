package com.example.flutter_100ms.hms_controller

import live.hms.video.error.HMSException
import live.hms.video.media.tracks.HMSTrack

// TODO: Provide a way to bind a message with each state
//  such that the UI can be updated with proper message as well.

sealed class MeetingState {

  data class Connecting(val heading: String, val message: String) : MeetingState()
  data class Joining(val heading: String, val message: String) : MeetingState()
  data class LoadingMedia(val heading: String, val message: String) : MeetingState()
  data class PublishingMedia(val heading: String, val message: String) : MeetingState()
  data class Ongoing(val message: String = "") : MeetingState()
  data class Disconnecting(val heading: String, val message: String) : MeetingState()
  data class Reconnecting(val heading: String, val message: String) : MeetingState()
  data class Disconnected(val goToHome: Boolean = false) : MeetingState()

  data class Failure(val exceptions: ArrayList<HMSException>) : MeetingState()
}

