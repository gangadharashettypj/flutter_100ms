import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_100ms/flutter_100ms.dart';
import 'package:flutter_100ms_example/event_listener.dart';
import 'package:flutter_100ms_example/models.dart';
import 'package:flutter_easy_permission/easy_permissions.dart';
import 'package:fluttertoast/fluttertoast.dart';

void main() {
  runApp(MyApp());
}

final REGEX_MEETING_URL_CODE = RegExp(
    '(https)?://(.*.100ms.live)/meeting/([a-zA-Z0-9]+-[a-zA-Z0-9]+-[a-zA-Z0-9]+).*/?');
final REGEX_MEETING_URL_ROOM_ID =
    RegExp('https?://(.*.100ms.live)/meeting/([a-zA-Z0-9]+)/([a-zA-Z0-9]+)/?');
final REGEX_TOKEN_ENDPOINT =
    RegExp('https?://.*.100ms.live/hmsapi/([a-zA-Z0-9-.]+.100ms.live)/?');

extension on String {
  String get tokenEndpointEnvironment {
    return this.contains("prod2.100ms.live")
        ? "prod-in"
        : (this.contains(".app.100ms.live") ? "prod-in" : "qa-in");
  }

  String get subdomain {
    if (this.contains('prod2.100ms.live')) {
      return 'internal.app.100ms.live';
    } else if (this.contains('qa2.100ms.live')) {
      return 'internal.qa-app.100ms.live';
    }

    RegExp regex;
    if (REGEX_MEETING_URL_ROOM_ID.hasMatch(this))
      regex = REGEX_MEETING_URL_ROOM_ID;
    if (REGEX_MEETING_URL_CODE.hasMatch(this)) regex = REGEX_MEETING_URL_CODE;
    if (REGEX_TOKEN_ENDPOINT.hasMatch(this)) regex = REGEX_TOKEN_ENDPOINT;

    return regex.firstMatch(this).group(2) ?? '';
  }
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  var cameraEnabled = true;
  var audioEnabled = true;
  FlutterEasyPermission _easyPermission;

  @override
  void dispose() {
    _easyPermission.dispose();
    super.dispose();
  }

  @override
  void initState() {
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      listenEvents(
        refresh: () {
          setState(() {});
        },
      );
    });

    _easyPermission = FlutterEasyPermission()
      ..addPermissionCallback(
        onGranted: (requestCode, perms, perm) {},
        onDenied: (requestCode, perms, perm, isPermanent) {
          if (isPermanent) {
            FlutterEasyPermission.showAppSettingsDialog(title: "Camera");
          } else {
            debugPrint("Android Deny authorization:$perms");
            debugPrint("iOS Deny authorization:$perm");
          }
        },
      );
    super.initState();
  }

  String getTokenEndpointForRoomId(String environment, String subdomain) {
    return 'https://$environment.100ms.live/hmsapi/get-token';
  }

  var status = 'Press join to join the meeting';
  static const permissions = [
    Permissions.CAMERA,
    Permissions.RECORD_AUDIO,
  ];

  static const permissionGroup = [
    PermissionGroup.Camera,
    PermissionGroup.Microphone,
  ];
  Widget _buildButtonBar() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        if (joinData == null)
          ElevatedButton(
            onPressed: () async {
              if (!(await FlutterEasyPermission.has(
                perms: permissions,
                permsGroup: permissionGroup,
              ))) {
                FlutterEasyPermission.request(
                  perms: permissions,
                  permsGroup: permissionGroup,
                  rationale: "Give permissions",
                );
                return;
              }
              if (controller.text.isEmpty) {
                status = "Enter valid username";
                Fluttertoast.showToast(
                  msg: "Enter valid username",
                  toastLength: Toast.LENGTH_SHORT,
                  gravity: ToastGravity.TOP,
                  timeInSecForIosWeb: 1,
                  backgroundColor: Colors.red,
                  textColor: Colors.white,
                  fontSize: 16.0,
                );
                setState(() {});
                return;
              }

              status = "Joining...";
              setState(() {});
              String url =
                  'https://frontrow.app.100ms.live/meeting/tasty-auburn-gorilla';
              String endpoint = url.tokenEndpointEnvironment;
              String subDomain = url.subdomain;
              String formattedUrl =
                  getTokenEndpointForRoomId(endpoint, subDomain);

              if (REGEX_MEETING_URL_CODE.hasMatch(url)) {
                final match = REGEX_MEETING_URL_CODE.firstMatch(url);
                final code = match.group(3);
                final response = await Dio().post(
                  formattedUrl,
                  data: {
                    'code': code,
                    'user_id': DateTime.now().millisecondsSinceEpoch.toString(),
                  },
                  options: Options(headers: {
                    'Accept-Type': 'application/json',
                    'subdomain': subDomain,
                  }),
                );
                joinData = JoinDataModel(
                  peerList: [],
                );
                Flutter100ms.init(false);
                Flutter100ms.join(controller.text, '', response.data['token']);
              } else {
                status = "Error joining meeting";
                setState(() {});
              }
            },
            child: Text('Join'),
          ),
        if (joinData != null)
          ElevatedButton(
            onPressed: () {
              Flutter100ms.leave();
              status = 'Press join to join the meeting';
              audioEnabled = true;
              cameraEnabled = true;
              joinData = null;
              videoViews = {};
              viewIds = {};
              setState(() {});
            },
            child: Text('Leave'),
          ),
        if (joinData != null)
          FloatingActionButton(
            onPressed: () async {
              await Flutter100ms.toggleCamera();
              setState(() {
                cameraEnabled = !cameraEnabled;
              });
            },
            child: Icon(
              Icons.camera_alt,
              color: Colors.white,
            ),
            backgroundColor: cameraEnabled ? Colors.grey : Colors.red,
            mini: true,
          ),
        if (joinData != null)
          FloatingActionButton(
            onPressed: () async {
              await Flutter100ms.toggleAudio();
              setState(() {
                audioEnabled = !audioEnabled;
              });
            },
            child: Icon(
              audioEnabled ? Icons.mic : Icons.mic_off,
              color: Colors.white,
            ),
            mini: true,
            backgroundColor: audioEnabled ? Colors.grey : Colors.red,
          ),
      ],
    );
  }

  Widget _buildUserListView() {
    if ((joinData?.peerList?.length ?? 0) == 0) {
      return Center(
        child: Text(
          status,
        ),
      );
    }

    return GridView.count(
      crossAxisCount: 2,
      crossAxisSpacing: 8,
      mainAxisSpacing: 8,
      children: List.generate(
        joinData?.peerList?.length ?? 0,
        (int index) {
          return ListTile(
            title: Container(
              height: 150,
              child: videoViews[joinData?.peerList[index]?.peerId],
            ),
          );
        },
      ),
    );
  }

  final controller = TextEditingController();
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: Padding(
          padding: EdgeInsets.all(8),
          child: SafeArea(
            child: Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.start,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Expanded(
                    child: _buildUserListView(),
                  ),
                  TextField(
                    controller: controller,
                    decoration: InputDecoration(hintText: 'UserName'),
                  ),
                  SizedBox(
                    height: 16,
                  ),
                  _buildButtonBar(),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
