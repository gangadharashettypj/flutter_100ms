#import "Flutter100msPlugin.h"
#if __has_include(<flutter_100ms/flutter_100ms-Swift.h>)
#import <flutter_100ms/flutter_100ms-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_100ms-Swift.h"
#endif

@implementation Flutter100msPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutter100msPlugin registerWithRegistrar:registrar];
}
@end
