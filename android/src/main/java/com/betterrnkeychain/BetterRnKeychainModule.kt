package com.betterrnkeychain

import android.preference.PreferenceManager
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.*
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import kotlinx.coroutines.*
import java.lang.Exception

class BetterRnKeychainModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private var promptInfo: BiometricPrompt.PromptInfo = BiometricPrompt.PromptInfo.Builder().setConfirmationRequired(false).setTitle("Authenticate to login to server").setNegativeButtonText("Cancel").build()
  private var cryptographyManager: CryptographyManager = CryptographyManager()

  private fun createBiometricPrompt(alias: String, promise: Promise): BiometricPrompt {
    val executor = ContextCompat.getMainExecutor(reactContext)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)

        if (errorCode == 13) { // user canceled
          promise.resolve(null)
        } else {
          promise.reject(errorCode.toString(), errString.toString())
        }
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()

        promise.reject(null, "Authentication failed for an unknown reason")
      }

      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)

        val cipherText = PreferenceManager.getDefaultSharedPreferences(reactContext).getString(alias, null)

        if (cipherText != null) {
          promise.resolve(cryptographyManager.decryptData(Base64.decode(cipherText, Base64.NO_WRAP), result.cryptoObject?.cipher!!))
        } else {
          promise.resolve(null)
        }
      }
    }

    if (reactContext.currentActivity == null) throw Exception("No activity running in the current context")

    return BiometricPrompt(reactContext.currentActivity as FragmentActivity, executor, callback)
  }

  @ReactMethod
  fun hasSecureValue(alias: String, promise: Promise) {
    try {
      promise.resolve(cryptographyManager.hasAlias(alias))
    } catch (e: Exception) {
      promise.reject(null, e)
    }
  }

  @ReactMethod
  fun setSecureValue(alias: String, secret: String, promise: Promise) {

        GlobalScope.launch(Dispatchers.Default) {
          try {
          val cipher = cryptographyManager.getInitializedCipherForEncryption(alias)

          val cipherText = Base64.encodeToString(cryptographyManager.encryptData(secret, cipher), Base64.NO_WRAP)

          PreferenceManager.getDefaultSharedPreferences(reactContext).edit().putString(alias, cipherText).apply()

          promise.resolve(null)
          } catch (e: Exception) {
            promise.reject(null, e)
          }
        }

  }

  @ReactMethod
  fun getSecureValue(alias: String, promise: Promise) {
    runOnUiThread {
      try {
        val cipher = cryptographyManager.getInitializedCipherForDecryption(alias)

        createBiometricPrompt(alias, promise).authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
      } catch (e: Exception) {
        promise.reject(null, e)
      }
    }
  }

  @ReactMethod
  fun deleteSecureValue(alias: String, promise: Promise) {
    try {
      cryptographyManager.deleteAlias(alias)

      promise.resolve(null)
    } catch (e: Exception) {
      promise.reject(null, e)
    }
  }

  @ReactMethod
  fun canUseSecureStorage(promise: Promise) {
    promise.resolve(BiometricManager.from(reactContext).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS)
  }

  override fun getName(): String {
    return "BetterRnKeychain"
  }
}
