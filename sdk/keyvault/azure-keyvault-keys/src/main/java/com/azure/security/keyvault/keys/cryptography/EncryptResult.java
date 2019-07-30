package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of encryption operation result.
 */
public class EncryptResult {

    /**
     * THe encrypted content.
     */
    private byte[] cipherText;

    /**
     * The authentication tag.
     */
    private byte[] authenticationTag;

    /**
     * The encrypyion algorithm used for the encryption operation.
     */
    private EncryptionAlgorithm algorithm;


    /**
     * Get the encrypted content.
     * @return The encrypted content.
     */
    public byte[] cipherText() {
        return cipherText;
    }

    public EncryptResult cipherText(byte[] cipherText) {
        this.cipherText = cipherText;
        return this;
    }

    /**
     * Get the authentication tag.
     * @return The authentication tag.
     */
    public byte[] authenticationTag() {
        return authenticationTag;
    }

    public EncryptResult authenticationTag(byte[] authenticationTag) {
        this.authenticationTag = authenticationTag;
        return this;
    }

    /**
     * Get the encryption algorithm used for encryption.
     * @return The encryption algorithm used.
     */
    public EncryptionAlgorithm algorithm() {
        return algorithm;
    }

    public EncryptResult algorithm(EncryptionAlgorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }
}
