package com.example.flutter_100ms.hms_controller

import java.util.*

data class ChatMessage(
  val senderName: String,
  val time: Date,
  val message: String,
  val isSentByMe: Boolean,
)

