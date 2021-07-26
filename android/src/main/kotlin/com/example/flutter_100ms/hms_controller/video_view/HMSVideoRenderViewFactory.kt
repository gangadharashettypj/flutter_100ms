package com.example.flutter_100ms.hms_controller.video_view

import android.content.Context
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import java.util.*

class HMSVideoRenderViewFactory : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val view = HMSVideoRenderView(context)
        _viewIdToViewMap[viewId] = view
        return view
    }

    companion object {
        private val _viewIdToViewMap: MutableMap<Int, HMSVideoRenderView> = HashMap()

        fun getViewById(id: Int): HMSVideoRenderView? = _viewIdToViewMap[id]

        fun clearViewIds() {
            _viewIdToViewMap.clear()
        }
    }
}
