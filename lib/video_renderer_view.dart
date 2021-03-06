import 'dart:io';

import 'package:flutter/material.dart';

/// A view where local or remote video gets rendered
class HMSVideoRenderView extends StatefulWidget {
  /// Event to be called when the view gets created
  final ValueChanged<int> onPlatformViewCreated;

  /// Creates a [HMSVideoRenderView].
  /// Optional: [onPlatformViewCreated] event to be called when the view gets created.
  HMSVideoRenderView({
    required this.onPlatformViewCreated,
  });

  @override
  _HMSVideoRenderViewState createState() => _HMSVideoRenderViewState();
}

class _HMSVideoRenderViewState extends State<HMSVideoRenderView> {
  @override
  Widget build(BuildContext context) {
    if (Platform.isAndroid) {
      return AndroidView(
        viewType: 'HMSVideoRenderView',
        onPlatformViewCreated: (int viewId) =>
            widget.onPlatformViewCreated.call(viewId),
      );
    } else if (Platform.isIOS) {
      return UiKitView(
        viewType: 'HMSVideoRenderView',
        onPlatformViewCreated: (int viewId) =>
            widget.onPlatformViewCreated.call(viewId),
      );
    } else
      throw Exception('Not implemented');
  }
}
