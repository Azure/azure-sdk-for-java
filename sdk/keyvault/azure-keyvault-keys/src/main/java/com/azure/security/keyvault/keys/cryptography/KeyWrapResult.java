package com.azure.security.keyvault.keys.cryptography;


public class KeyWrapResult {

    private byte[] encryptedKey;

    private KeyWrapAlgorithm algorithm;

    public byte[] encryptedKey() {
        return encryptedKey;
    }

    public KeyWrapResult encryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
        return this;
    }

    public KeyWrapAlgorithm algorithm() {
        return algorithm;
    }

    public KeyWrapResult algorithm(KeyWrapAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
