package com.example.flutter_100ms

inline fun throwIf(condition: Boolean, thr: () -> Throwable) {
    if(condition) {
        throw thr()
    }
}
