#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(BetterRnKeychain, NSObject)

RCT_EXTERN_METHOD(
                  hasSecureValue:(NSString *)alias
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  setSecureValue:(NSString *)alias
                  withSecret:(NSString *)secret
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  getSecureValue:(NSString *)alias
                  withResolver:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject
                  )

RCT_EXTERN_METHOD(
                  canUseSecureStorage:(RCTPromiseResolveBlock)resolve
                  withRejecter:(RCTPromiseRejectBlock)reject
                  )

@end
