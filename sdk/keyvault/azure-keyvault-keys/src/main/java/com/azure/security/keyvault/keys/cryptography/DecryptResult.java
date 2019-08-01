package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of decrypt operation result.
 */
public class DecryptResult {
    public DecryptResult(byte[] plainText) {
        this.plainText = plainText;
    }

    /**
     * The decrypted content.
     */
    private byte[] plainText;

    /**
     * Get the encrypted content.
     * @return The decrypted content.
     */
    public byte[] plainText() {
        return plainText;
    }
}
