import KeychainAccess
import LocalAuthentication

@objc(BetterRnKeychain)
class BetterRnKeychain: NSObject {
    var authContext = LAContext()
    
    @objc(hasSecureValue:withResolver:withRejecter:)
    func hasSecureValue(alias: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let keychain = Keychain()
        
        DispatchQueue.global().async {
            do {
                resolve(try keychain.contains(alias, withoutAuthenticationUI: true))
            } catch let error as NSError {
                reject(String(error.code), error.localizedDescription, error)
            }
        }
    }
    
    @objc(setSecureValue:withSecret:withResolver:withRejecter:)
    func setSecureValue(alias: String, secret: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let keychain = Keychain()
        
        DispatchQueue.global().async {
            do {
                try keychain.remove(alias)
                
                var authenticationPolicy = AuthenticationPolicy.touchIDAny
                
                if #available(iOS 11.3, *) {
                    authenticationPolicy = AuthenticationPolicy.biometryAny
                }
                
                try keychain
                    .accessibility(.whenUnlocked, authenticationPolicy: authenticationPolicy)
                    .set(secret, key: alias)
                
                resolve(nil)
            } catch let error as NSError {
                reject(String(error.code), error.localizedDescription, error)
            }
        }
    }
    
    @objc(getSecureValue:withResolver:withRejecter:)
    func getSecureValue(alias: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let keychain = Keychain()
        
        DispatchQueue.global().async {
            do {
                let password = try keychain
                    .authenticationPrompt("Authenticate to login to server")
                    .get(alias)
                
                resolve(password)
            } catch let error as NSError {
                reject(String(error.code), error.localizedDescription, error)
            }
        }
    }
    
    @objc(canUseSecureStorage:withRejecter:)
    func canUseSecureStorage(resolve: RCTPromiseResolveBlock, reject: RCTPromiseRejectBlock) -> Void {
        if #available(iOS 11, *) {
            authContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)
            
            switch(authContext.biometryType) {
            case .none:
                resolve(false)
            case .touchID:
                resolve(true)
            case .faceID:
                resolve(true)
            @unknown default:
                resolve(false)
            }
        } else {
            if (authContext.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: nil)) {
                resolve(true)
            } else {
                resolve(false)
            }
        }
    }
    
    @objc(deleteSecureValue:withResolver:withRejecter:)
    func deleteSecureValue(alias: String, resolve: @escaping RCTPromiseResolveBlock, reject: @escaping RCTPromiseRejectBlock) -> Void {
        let keychain = Keychain()
        
        DispatchQueue.global().async {
            do {
                try keychain.remove(alias)
                
                resolve(nil)
            } catch let error as NSError {
                reject(String(error.code), error.localizedDescription, error)
            }
        }
    }
}
