package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of key unwrap operation result.
 */
public class KeyUnwrapResult {
    public KeyUnwrapResult(byte[] key) {
        this.key = key;
    }

    /**
     * The unwrapped key content.
     */
    private byte[] key;

    /**
     * Get the unwrapped key content.
     * @return The unwrapped key content.
     */
    public byte[] key() {
        return key;
    }
}
