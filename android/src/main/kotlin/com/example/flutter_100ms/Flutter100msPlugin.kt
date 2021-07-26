package com.example.flutter_100ms

import android.content.Context

import android.os.Handler;
import android.os.Looper;
import android.util.Log
import androidx.annotation.NonNull
import com.example.flutter_100ms.constants.IncomingMethodType
import com.example.flutter_100ms.hms_controller.video_view.HMSVideoRenderViewFactory
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

private const val TAG = "HMS"

/** Flutter100msPlugin */
class Flutter100msPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel

    private lateinit var context: Context

    private var eventSink: EventSink? = null

    companion object {
        var handler: Handler? = null
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_100ms")
        channel.setMethodCallHandler(this)

        context = flutterPluginBinding.applicationContext
        val eventChannel =
            EventChannel(flutterPluginBinding.binaryMessenger, "flutter_100ms_events")
        eventChannel.setStreamHandler(object : StreamHandler {
            override fun onListen(arguments: Any?, events: EventSink?) {
                eventSink = events
                handler = Handler(Looper.getMainLooper());
            }

            override fun onCancel(arguments: Any?) {
                Log.d("HMS", "EventChannel.setStreamHandler()/onCancel()")
            }
        })

        flutterPluginBinding.platformViewRegistry.registerViewFactory("HMSVideoRenderView", HMSVideoRenderViewFactory())
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            IncomingMethodType.VERSION.name -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            IncomingMethodType.INIT.name -> {
                initMethodChannelHandler(call, result, context, eventSink!!)
            }
            IncomingMethodType.JOIN.name -> {
                joinMethodChannelHandler(call, result)
            }
            IncomingMethodType.LEAVE.name -> {
                leaveMethodChannelHandler(result)
            }
            IncomingMethodType.BIND_VIDEO_VIEW.name -> {
                bindVideoViewChannelHandler(call, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}
