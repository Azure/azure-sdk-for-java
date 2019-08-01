package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of verify operation result.
 */
public class VerifyResult {
    public VerifyResult(Boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * THe verify operation result.
     */
    private Boolean isValid;

    /**
     * Get the verify operation result.
     * @return The verification result.
     */
    public Boolean isValid() {
        return isValid;
    }
}
