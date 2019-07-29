package com.azure.security.keyvault.keys.cryptography;


public class EncryptResult {

    private byte[] cipherText;

    private byte[] authenticationTag;

    private EncryptionAlgorithm algorithm;

    public byte[] cipherText() {
        return cipherText;
    }

    public EncryptResult cipherText(byte[] cipherText) {
        this.cipherText = cipherText;
        return this;
    }

    public byte[] authenticationTag() {
        return authenticationTag;
    }

    public EncryptResult authenticationTag(byte[] authenticationTag) {
        this.authenticationTag = authenticationTag;
        return this;
    }

    public EncryptionAlgorithm algorithm() {
        return algorithm;
    }

    public EncryptResult algorithm(EncryptionAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
