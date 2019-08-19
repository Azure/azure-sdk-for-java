// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.models;

/**
 * Represents the details of verify operation result.
 */
public final class VerifyResult {
    /**
     * THe verify operation result.
     */
    private Boolean isValid;

    /**
     * Creates the instance of Verify Result holding the verification response information.
     * @param isValid The verification info.
     */
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
