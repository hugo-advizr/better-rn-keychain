import * as React from 'react';
import { Button, SafeAreaView, Text, TextInput } from 'react-native';
import { useCallback, useEffect, useState } from 'react';
import {
  canUseSecureStorage,
  getSecureValue,
  hasSecureValue,
  setSecureValue,
} from 'better-rn-keychain';

export default function App() {
  const [clearText, setClearText] = useState<string>('some secret');
  const [decryptedValue, setDecryptedValue] = useState<string>();
  const [hasSecureStorage, setHasSecureStorage] = useState<boolean>(false);

  useEffect(() => {
    (async () => {
      setHasSecureStorage(await canUseSecureStorage());
    })();
  }, []);

  const encryptValue = useCallback(async () => {
    await setSecureValue('key', clearText);
  }, [clearText]);

  const decryptValue = useCallback(async () => {
    if (await hasSecureValue('key')) {
      setDecryptedValue(await getSecureValue('key'));
    }
  }, [setDecryptedValue]);

  return (
    <SafeAreaView>
      <Text>Can use secure storage: {hasSecureStorage.toString()}</Text>

      <Text>Clear text</Text>
      <TextInput value={clearText} onChangeText={setClearText} />

      <Text>Decrypted value: {decryptedValue}</Text>

      <Button title="encrypt" onPress={encryptValue} />

      <Button title="decrypt" onPress={decryptValue} />
    </SafeAreaView>
  );
}
