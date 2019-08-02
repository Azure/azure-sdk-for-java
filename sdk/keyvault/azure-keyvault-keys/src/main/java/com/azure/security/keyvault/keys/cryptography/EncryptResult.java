package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of encrypt operation result.
 */
public final class EncryptResult {

    public EncryptResult(byte[] cipherText, byte[] authenticationTag, EncryptionAlgorithm algorithm) {
        this.cipherText = cipherText;
        this.authenticationTag = authenticationTag;
        this.algorithm = algorithm;
    }

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


    /**
     * Get the authentication tag.
     * @return The authentication tag.
     */
    public byte[] authenticationTag() {
        return authenticationTag;
    }

    /**
     * Get the encryption algorithm used for encryption.
     * @return The encryption algorithm used.
     */
    public EncryptionAlgorithm algorithm() {
        return algorithm;
    }

}
