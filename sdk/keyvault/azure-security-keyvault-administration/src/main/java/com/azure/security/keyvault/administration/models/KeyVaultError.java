// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

/**
 * A class that represents an error occurred in a Key Vault operation.
 */
public final class KeyVaultError {
    private final String code;
    private final String message;
    private final KeyVaultError innerError;

    /**
     * Creates an object that represents an error occurred in a Key Vault operation.
     *
     * @param code The error code.
     * @param message The error message.
     * @param innerError An Key Vault server-side error.
     */
    public KeyVaultError(String code, String message, KeyVaultError innerError) {
        this.code = code;
        this.message = message;
        this.innerError = innerError;
    }

    /**
     * Get the error code.
     *
     * @return The error code.
     */
    public String getCode() {
        return this.code;
    }

    /**
     * Get the error message.
     *
     * @return The error message.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * Get the Key Vault server-side error.
     *
     * @return The Key Vault server-side error.
     */
    public KeyVaultError getInnerError() {
        return this.innerError;
    }
}
