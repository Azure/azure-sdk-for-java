// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;

/**
 * A class that represents an error occurred in a Key Vault operation.
 */
@Immutable
public final class KeyVaultAdministrationError {
    private final String code;
    private final String message;
    private final KeyVaultAdministrationError innerError;

    /**
     * Creates an object that represents an error occurred in a Key Vault operation.
     *
     * @param code The error code.
     * @param message The error message.
     * @param innerError An Key Vault server-side error.
     */
    public KeyVaultAdministrationError(String code, String message, KeyVaultAdministrationError innerError) {
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
    public KeyVaultAdministrationError getInnerError() {
        return this.innerError;
    }
}
