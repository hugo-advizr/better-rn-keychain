package com.betterrnkeychain

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import javax.crypto.Cipher

/**
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class CryptographyManager {
  private val ANDROID_KEYSTORE = "AndroidKeyStore"
  private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_ECB
  private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1
  private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_RSA

  @RequiresApi(Build.VERSION_CODES.M)
  fun getInitializedCipherForEncryption(alias: String): Cipher {
    getKeyStore().deleteEntry(alias)

    val cipher = getCipher()

    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey(alias).public)

    return cipher
  }

  @RequiresApi(Build.VERSION_CODES.M)
  fun getInitializedCipherForDecryption(alias: String): Cipher {
    val cipher = getCipher()

    cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(alias).private)

    return cipher
  }

  fun hasAlias(alias: String): Boolean {
    return getKeyStore().containsAlias(alias)
  }

  fun encryptData(plainText: String, cipher: Cipher): ByteArray {
    return cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
  }

  fun decryptData(cipherText: ByteArray, cipher: Cipher): String {
    return cipher.doFinal(cipherText).toString(Charsets.UTF_8)
  }

  private fun getCipher(): Cipher {
    return Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
  }

  private fun getKeyStore(): KeyStore {
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)

    keyStore.load(null)

    return keyStore
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun getOrCreateSecretKey(alias: String): KeyPair {

    val keyStore = getKeyStore()

    val keyEntry = keyStore.getEntry(alias, null)

    if (keyEntry != null) {
      keyEntry as KeyStore.PrivateKeyEntry

      return KeyPair(keyEntry.certificate.publicKey, keyEntry.privateKey)
    }

    val paramsBuilder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)

    paramsBuilder.apply {
      setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
      setBlockModes(ENCRYPTION_BLOCK_MODE)
      setEncryptionPaddings(ENCRYPTION_PADDING)
      setKeySize(2048)
      setRandomizedEncryptionRequired(true)
      setUserAuthenticationRequired(true)
    }

    val keyGenParams = paramsBuilder.build()
    val keyGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE)

    keyGenerator.initialize(keyGenParams)

    return keyGenerator.genKeyPair()
  }
}
