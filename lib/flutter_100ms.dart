import 'dart:async';

import 'package:flutter/services.dart';

class Flutter100ms {
  static const MethodChannel _channel = const MethodChannel('flutter_100ms');

  static const EventChannel _eventChannel =
      const EventChannel('flutter_100ms_events');

  static EventChannel get eventChannel => _eventChannel;

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<bool?> init(bool speaker) async {
    final response = await _channel.invokeMethod(
      'INIT',
      {
        'speaker': speaker,
      },
    );
    return response;
  }

  static Future<bool?> join(String userName, String description) async {
    final response = await _channel.invokeMethod(
      'JOIN',
      {
        'userName': userName,
        'description': description,
      },
    );
    return response;
  }

  static Future<bool?> leave() async {
    final response = await _channel.invokeMethod('LEAVE');
    return response;
  }
}
