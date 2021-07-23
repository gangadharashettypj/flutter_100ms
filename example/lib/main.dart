import 'package:flutter/material.dart';
import 'package:flutter_100ms/flutter_100ms.dart';
import 'package:flutter_100ms_example/event_listener.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    listenEvents();
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () {
                  Flutter100ms.init(false);
                  Flutter100ms.join('GS', '');
                },
                child: Text('Join'),
              ),
              SizedBox(
                height: 32,
              ),
              ElevatedButton(
                onPressed: () {
                  Flutter100ms.leave();
                },
                child: Text('Leave'),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
