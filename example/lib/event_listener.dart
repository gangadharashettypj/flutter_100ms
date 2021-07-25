import 'dart:convert';

import 'package:flutter_100ms/flutter_100ms.dart';

void listenEvents() {
  Flutter100ms.eventChannel.receiveBroadcastStream().listen((data) async {
    dynamic event = jsonDecode(data)['nameValuePairs'];
    String eventName = event['eventType'];
    dynamic eventArguments = event['arguments'];
    print('>>>>>>>>>>>');
    print(eventName);
    print(eventArguments);
    print('????????????');
    switch (eventName) {
      default:
        print(
            'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onData()');
        print('Warning: Unhandled event: $eventName');
        print('Data: $data');
        break;
    }
  }, onDone: () {
    print(
        'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onDone()');
  }, onError: (e) {
    print(
        'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onError()');
  });
}
