import 'dart:convert';

import 'package:flutter_100ms/flutter_100ms.dart';

void listenEvents() {
  Flutter100ms.eventChannel.receiveBroadcastStream().listen((data) async {
    dynamic event = JsonDecoder().convert(data);
    String eventName = event['Name'];
    dynamic eventArguments = event['Arguments'];
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
