package com.azure.security.keyvault.keys.cryptography;

/**
 * Represents the details of verify operation result.
 */
public final class VerifyResult {
    /**
     * THe verify operation result.
     */
    private Boolean isValid;

    public VerifyResult(Boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * Get the verify operation result.
     * @return The verification result.
     */
    public Boolean isValid() {
        return isValid;
    }
}
