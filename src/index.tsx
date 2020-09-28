import { NativeModules } from 'react-native';

const { BetterRnKeychain } = NativeModules;

export const hasSecureValue: (alias: string) => Promise<boolean> =
  BetterRnKeychain.hasSecureValue;

export const setSecureValue: (alias: string, secret: string) => Promise<void> =
  BetterRnKeychain.setSecureValue;

export const getSecureValue: (alias: string) => Promise<string> =
  BetterRnKeychain.getSecureValue;

export const canUseSecureStorage: () => Promise<boolean> =
  BetterRnKeychain.canUseSecureStorage;

export const deleteSecureValue: (alias: string) => Promise<void> =
  BetterRnKeychain.deleteSecureValue;
