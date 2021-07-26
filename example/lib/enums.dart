enum InComingMethodType {
  ON_JOIN,
  ON_PEER_UPDATE,
  ON_ROOM_UPDATE,
  ON_TRACK_UPDATE,
  ON_MESSAGE_RECEIVED,
  ON_ERROR,
  ON_RECONNECTING,
  ON_ROLE_CHANGE_REQUEST,
  ON_RECONNECTED,
}

extension InComingMethodTypeExtension on InComingMethodType {
  String get rawValue {
    switch (this) {
      case InComingMethodType.ON_JOIN:
        return 'ON_JOIN';
      case InComingMethodType.ON_PEER_UPDATE:
        return 'ON_PEER_UPDATE';
      case InComingMethodType.ON_ROOM_UPDATE:
        return 'ON_ROOM_UPDATE';
      case InComingMethodType.ON_TRACK_UPDATE:
        return 'ON_TRACK_UPDATE';
      case InComingMethodType.ON_MESSAGE_RECEIVED:
        return 'ON_MESSAGE_RECEIVED';
      case InComingMethodType.ON_ERROR:
        return 'ON_ERROR';
      case InComingMethodType.ON_RECONNECTING:
        return 'ON_RECONNECTING';
      case InComingMethodType.ON_ROLE_CHANGE_REQUEST:
        return 'ON_ROLE_CHANGE_REQUEST';
      case InComingMethodType.ON_RECONNECTED:
        return 'ON_RECONNECTED';
      default:
        return '';
    }
  }
}

enum PeerUpdateType {
  PEER_JOINED,

  PEER_LEFT,

  AUDIO_TOGGLED,

  VIDEO_TOGGLED,

  BECAME_DOMINANT_SPEAKER,

  NO_DOMINANT_SPEAKER,

  RESIGNED_DOMINANT_SPEAKER,

  STARTED_SPEAKING,

  STOPPED_SPEAKING,

  ROLE_CHANGED,
}

extension PeerUpdateTypeExtension on PeerUpdateType {
  String get rawValue {
    switch (this) {
      case PeerUpdateType.PEER_JOINED:
        return 'PEER_JOINED';
      case PeerUpdateType.PEER_LEFT:
        return 'PEER_LEFT';
      case PeerUpdateType.AUDIO_TOGGLED:
        return 'AUDIO_TOGGLED';
      case PeerUpdateType.VIDEO_TOGGLED:
        return 'VIDEO_TOGGLED';
      case PeerUpdateType.BECAME_DOMINANT_SPEAKER:
        return 'BECAME_DOMINANT_SPEAKER';
      case PeerUpdateType.NO_DOMINANT_SPEAKER:
        return 'NO_DOMINANT_SPEAKER';
      case PeerUpdateType.RESIGNED_DOMINANT_SPEAKER:
        return 'RESIGNED_DOMINANT_SPEAKER';
      case PeerUpdateType.STARTED_SPEAKING:
        return 'STARTED_SPEAKING';
      case PeerUpdateType.STOPPED_SPEAKING:
        return 'STOPPED_SPEAKING';
      case PeerUpdateType.ROLE_CHANGED:
        return 'ROLE_CHANGED';
      default:
        return '';
    }
  }

  static PeerUpdateType fromString(String val) {
    if (val == PeerUpdateType.PEER_JOINED.rawValue) {
      return PeerUpdateType.PEER_JOINED;
    } else if (val == PeerUpdateType.PEER_LEFT.rawValue) {
      return PeerUpdateType.PEER_LEFT;
    } else if (val == PeerUpdateType.AUDIO_TOGGLED.rawValue) {
      return PeerUpdateType.AUDIO_TOGGLED;
    } else if (val == PeerUpdateType.VIDEO_TOGGLED.rawValue) {
      return PeerUpdateType.VIDEO_TOGGLED;
    } else if (val == PeerUpdateType.BECAME_DOMINANT_SPEAKER.rawValue) {
      return PeerUpdateType.BECAME_DOMINANT_SPEAKER;
    } else if (val == PeerUpdateType.NO_DOMINANT_SPEAKER.rawValue) {
      return PeerUpdateType.NO_DOMINANT_SPEAKER;
    } else if (val == PeerUpdateType.RESIGNED_DOMINANT_SPEAKER.rawValue) {
      return PeerUpdateType.RESIGNED_DOMINANT_SPEAKER;
    } else if (val == PeerUpdateType.STARTED_SPEAKING.rawValue) {
      return PeerUpdateType.STARTED_SPEAKING;
    } else if (val == PeerUpdateType.STOPPED_SPEAKING.rawValue) {
      return PeerUpdateType.STOPPED_SPEAKING;
    } else if (val == PeerUpdateType.ROLE_CHANGED.rawValue) {
      return PeerUpdateType.ROLE_CHANGED;
    } else {
      return PeerUpdateType.PEER_JOINED;
    }
  }
}
