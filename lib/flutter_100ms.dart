import 'dart:async';

import 'package:flutter/services.dart';

class Flutter100ms {
  static const MethodChannel _channel = const MethodChannel('flutter_100ms');

  static const EventChannel _eventChannel =
      const EventChannel('flutter_100ms_events');

  static EventChannel get eventChannel => _eventChannel;

  static Future<dynamic> get platformVersion async {
    final version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<dynamic> init(bool speaker) async {
    final response = await _channel.invokeMethod(
      'INIT',
      {
        'speaker': speaker,
      },
    );
    return response;
  }

  static Future<dynamic> join(
      String userName, String description, String token) async {
    final response = await _channel.invokeMethod(
      'JOIN',
      {
        'userName': userName,
        'description': description,
        'token': token,
      },
    );
    return response;
  }

  static Future<dynamic> leave() async {
    final response = await _channel.invokeMethod('LEAVE');
    return response;
  }

  static Future<dynamic> bindVideoView(int viewId, String peerId) async {
    var params = {"ViewId": viewId, "PeerId": peerId};
    return _channel.invokeMethod('BIND_VIDEO_VIEW', params);
  }
}
