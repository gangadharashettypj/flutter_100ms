package com.example.flutter_100ms.hms_controller.video_view

import android.content.Context
import android.view.View
import com.example.flutter_100ms.R
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.platform.PlatformView

class HMSVideoRenderView internal constructor(context: Context?) : PlatformView,
    MethodCallHandler {
    private val _videoView = View.inflate(
        context!!,
        R.layout.grid_item_video,
        null,
    )

    override fun dispose() = Unit

    override fun getView(): View = _videoView

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) =
        result.notImplemented()
}
