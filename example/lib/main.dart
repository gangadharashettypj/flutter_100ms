import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:flutter_100ms/flutter_100ms.dart';
import 'package:flutter_100ms_example/event_listener.dart';

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
  @override
  void initState() {
    WidgetsBinding.instance.addPostFrameCallback((timeStamp) {
      listenEvents(
        refresh: () {
          setState(() {});
        },
      );
    });
    super.initState();
  }

  String getTokenEndpointForRoomId(String environment, String subdomain) {
    return 'https://$environment.100ms.live/hmsapi/get-token';
  }

  Widget _buildButtonBar() {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children: [
        ElevatedButton(
          onPressed: () async {
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

              Flutter100ms.init(false);
              Flutter100ms.join('GS', '', response.data['token']);
            }
            // if (REGEX_MEETING_URL_ROOM_ID.hasMatch(url)) {
            //   final match = REGEX_MEETING_URL_ROOM_ID.firstMatch(url)!;
            //   final roomId = match.group(2);
            //   final role = match.group(3);
            //
            //   final response = await Dio().post(
            //     formattedUrl,
            //     data: {
            //       'room_id': roomId,
            //       'role': role,
            //       'user_id':
            //           DateTime.now().millisecondsSinceEpoch.toString(),
            //     },
            //     options: Options(headers: {
            //       'Accept-Type': 'application/json',
            //       'subdomain': subDomain,
            //     }),
            //   );
            //
            //   Flutter100ms.init(false);
            //   Flutter100ms.join('GS', '', response.data['token']);
            // }
          },
          child: Text('Join'),
        ),
        ElevatedButton(
          onPressed: () {
            Flutter100ms.leave();
            joinData = null;
            setState(() {});
          },
          child: Text('Leave'),
        ),
      ],
    );
  }

  Widget _buildUserListView() {
    if ((joinData?.peerList?.length ?? 0) == 0) {
      return Center(
        child: Text(
          'No one joined',
        ),
      );
    }

    return ListView.separated(
      itemBuilder: (BuildContext context, int index) {
        return ListTile(
          title: Container(
            height: 120,
            child: videoViews[joinData?.peerList[index]?.peerId],
          ),
          // Text(
          //   '${index + 1}. ${joinData?.peerList[index]?.name}',
          // ),
        );
      },
      separatorBuilder: (BuildContext context, int index) {
        return SizedBox(
          height: 16,
        );
      },
      itemCount: joinData?.peerList?.length ?? 0,
    );
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: SafeArea(
          child: Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.start,
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                _buildButtonBar(),
                Expanded(
                  child: _buildUserListView(),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
