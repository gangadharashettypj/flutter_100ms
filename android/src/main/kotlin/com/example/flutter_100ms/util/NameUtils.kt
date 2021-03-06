package com.example.flutter_100ms.util

import java.util.*

object NameUtils {
    fun getInitials(name: String): String {
        val words = name.trim().uppercase(Locale.ROOT).split(' ')
        return when {
            words.isEmpty() -> {
                "--"
            }
            words.size == 1 -> {
                words[0].substring(0, words[0].length.coerceAtMost(2))
            }
            else -> {
                "${words[0][0]}${words[1][0]}"
            }
        }
    }
}