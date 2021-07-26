import 'dart:convert';

import 'package:flutter_100ms/flutter_100ms.dart';
import 'package:flutter_100ms/video_renderer_view.dart';
import 'package:flutter_100ms_example/enums.dart';
import 'package:flutter_100ms_example/models.dart';

JoinDataModel joinData = JoinDataModel(
  peerList: [],
);
Map<String, HMSVideoRenderView> videoViews = {};
Map<String, int> viewIds = {};

void listenEvents({Null Function() refresh}) {
  Flutter100ms.eventChannel.receiveBroadcastStream().listen((data) async {
    dynamic event = jsonDecode(data);
    String eventName = event['eventType'];
    dynamic eventArguments = jsonDecode(event['arguments']);
    print('>>>>>>>>>>>');
    print(eventName);
    print(eventArguments);
    print('????????????');
    if (eventName == InComingMethodType.ON_JOIN.rawValue) {
      final _tempData = JoinDataModel.fromJson(eventArguments);
      if (joinData == null) {
        joinData = _tempData;
      } else {
        joinData.roomId = _tempData.roomId;
        joinData.name = _tempData.name;
        _tempData.peerList.forEach((element) {
          final index = joinData.peerList
              .indexWhere((peer) => element.peerId == peer.peerId);
          if (index == -1) {
            joinData.peerList.add(element);
          } else {
            joinData.peerList[index] = element;
          }
        });
      }

      refresh();
    } else if (eventName == InComingMethodType.ON_TRACK_UPDATE.rawValue) {
      final trackData = TrackUpdateDataModel.fromJson(eventArguments);
      createVideoView(trackData.peerId);
      refresh();
    } else if (eventName == InComingMethodType.ON_PEER_UPDATE.rawValue) {
      final peerData = TrackUpdateDataModel.fromJson(eventArguments);

      joinData.peerList.firstWhere(
        (element) => element.peerId == peerData.peerId,
        orElse: () {
          joinData.peerList.add(PeerInfo.fromJson(peerData.toJson()));

          return null;
        },
      );
      switch (peerData.type) {
        case PeerUpdateType.PEER_LEFT:
          videoViews.remove(peerData.peerId);
          joinData.peerList
              .removeWhere((element) => element.peerId == peerData.peerId);
          break;
        case PeerUpdateType.PEER_JOINED:
        case PeerUpdateType.AUDIO_TOGGLED:
        case PeerUpdateType.VIDEO_TOGGLED:
        case PeerUpdateType.BECAME_DOMINANT_SPEAKER:
        case PeerUpdateType.NO_DOMINANT_SPEAKER:
        case PeerUpdateType.RESIGNED_DOMINANT_SPEAKER:
        case PeerUpdateType.STARTED_SPEAKING:
        case PeerUpdateType.STOPPED_SPEAKING:
        case PeerUpdateType.ROLE_CHANGED:
          final index = joinData.peerList
              .indexWhere((element) => element.peerId == peerData.peerId);
          joinData.peerList[index] = PeerInfo.fromJson(peerData.toJson());
          break;
      }
      refresh();
    } else {
      print(
          'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onData()');
      print('Warning: Unhandled event: $eventName');
      print('Data: $data');
    }
  }, onDone: () {
    print(
        'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onDone()');
  }, onError: (e) {
    print(
        'Flutter100ms.eventChannel.receiveBroadcastStream().listen()/onError()');
  });
}

void createVideoView(String peerId) {
  videoViews[peerId] = HMSVideoRenderView(
    onPlatformViewCreated: (int viewId) async {
      viewIds[peerId] = viewId;
      await Flutter100ms.bindVideoView(viewId, peerId);
    },
  );
}
