package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of wrap operation result.
 */
public final class KeyWrapResult {

    public KeyWrapResult(byte[] encryptedKey, KeyWrapAlgorithm algorithm) {
        this.encryptedKey = encryptedKey;
        this.algorithm = algorithm;
    }

    /**
     * The encrypted key content
     */
    private byte[] encryptedKey;

    /**
     * The key wrap algorithm used to wrap the key content.
     */
    private KeyWrapAlgorithm algorithm;

    /**
     * Get the encrypted key content.
     * @return The encrypted key.
     */
    public byte[] encryptedKey() {
        return encryptedKey;
    }

    public KeyWrapResult encryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
        return this;
    }

    /**
     * Get the key wrap algorithm used to wrap the key content.
     * @return The key wrap algorithm.
     */
    public KeyWrapAlgorithm algorithm() {
        return algorithm;
    }

    public KeyWrapResult algorithm(KeyWrapAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
