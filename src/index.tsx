import { NativeModules } from 'react-native';

type BetterRnKeychainType = {
  multiply(a: number, b: number): Promise<number>;
};

const { BetterRnKeychain } = NativeModules;

export default BetterRnKeychain as BetterRnKeychainType;
