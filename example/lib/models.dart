import 'package:flutter_100ms_example/enums.dart';

class JoinDataModel {
  String name;
  String roomId;
  List<PeerInfo> peerList;

  JoinDataModel({this.name, this.roomId, this.peerList});

  JoinDataModel.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    roomId = json['roomId'];
    if (json['peerList'] != null) {
      peerList = <PeerInfo>[];
      json['peerList'].forEach((v) {
        peerList.add(new PeerInfo.fromJson(v));
      });
    }
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['name'] = this.name;
    data['roomId'] = this.roomId;
    if (this.peerList != null) {
      data['peerList'] = this.peerList.map((v) => v.toJson()).toList();
    }
    return data;
  }
}

class PeerInfo {
  String name;
  String peerId;
  String customerUserID;
  String customerDescription;
  bool isLocal;
  HmsRole hmsRole;

  PeerInfo(
      {this.name,
      this.peerId,
      this.customerUserID,
      this.customerDescription,
      this.isLocal,
      this.hmsRole});

  PeerInfo.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    peerId = json['peerId'];
    customerUserID = json['customerUserID'];
    customerDescription = json['customerDescription'];
    isLocal = json['isLocal'];
    hmsRole =
        json['hmsRole'] != null ? new HmsRole.fromJson(json['hmsRole']) : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['name'] = this.name;
    data['peerId'] = this.peerId;
    data['customerUserID'] = this.customerUserID;
    data['customerDescription'] = this.customerDescription;
    data['isLocal'] = this.isLocal;
    if (this.hmsRole != null) {
      data['hmsRole'] = this.hmsRole.toJson();
    }
    return data;
  }
}

class HmsRole {
  String name;
  String permission;
  int priority;
  PublishParams publishParams;
  SubscribeParams subscribeParams;

  HmsRole(
      {this.name,
      this.permission,
      this.priority,
      this.publishParams,
      this.subscribeParams});

  HmsRole.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    permission = json['permission'];
    priority = json['priority'];
    publishParams = json['publishParams'] != null
        ? new PublishParams.fromJson(json['publishParams'])
        : null;
    subscribeParams = json['subscribeParams'] != null
        ? new SubscribeParams.fromJson(json['subscribeParams'])
        : null;
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['name'] = this.name;
    data['permission'] = this.permission;
    data['priority'] = this.priority;
    if (this.publishParams != null) {
      data['publishParams'] = this.publishParams.toJson();
    }
    if (this.subscribeParams != null) {
      data['subscribeParams'] = this.subscribeParams.toJson();
    }
    return data;
  }
}

class PublishParams {
  String allowed;

  PublishParams({this.allowed});

  PublishParams.fromJson(Map<String, dynamic> json) {
    allowed = json['allowed'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['allowed'] = this.allowed;
    return data;
  }
}

class SubscribeParams {
  String subscribeTo;
  int maxSubsBitRate;

  SubscribeParams({this.subscribeTo, this.maxSubsBitRate});

  SubscribeParams.fromJson(Map<String, dynamic> json) {
    subscribeTo = json['subscribeTo'];
    maxSubsBitRate = json['maxSubsBitRate'];
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['subscribeTo'] = this.subscribeTo;
    data['maxSubsBitRate'] = this.maxSubsBitRate;
    return data;
  }
}

class TrackUpdateDataModel {
  String name;
  String peerId;
  String customerUserID;
  String customerDescription;
  bool isLocal;
  HmsRole hmsRole;
  PeerUpdateType type;

  TrackUpdateDataModel(
      {this.name,
      this.peerId,
      this.customerUserID,
      this.customerDescription,
      this.isLocal,
      this.hmsRole,
      this.type});

  TrackUpdateDataModel.fromJson(Map<String, dynamic> json) {
    name = json['name'];
    peerId = json['peerId'];
    customerUserID = json['customerUserID'];
    customerDescription = json['customerDescription'];
    isLocal = json['isLocal'];
    hmsRole =
        json['hmsRole'] != null ? new HmsRole.fromJson(json['hmsRole']) : null;
    type = PeerUpdateTypeExtension.fromString(json['type']);
  }

  Map<String, dynamic> toJson() {
    final Map<String, dynamic> data = new Map<String, dynamic>();
    data['name'] = this.name;
    data['peerId'] = this.peerId;
    data['customerUserID'] = this.customerUserID;
    data['customerDescription'] = this.customerDescription;
    data['isLocal'] = this.isLocal;
    if (this.hmsRole != null) {
      data['hmsRole'] = this.hmsRole.toJson();
    }
    data['type'] = this.type.rawValue;
    return data;
  }
}
