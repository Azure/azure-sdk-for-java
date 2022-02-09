// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown for an invalid response with {@link KeyVaultAdministrationError} information.
 */
@Immutable
public final class KeyVaultAdministrationException extends HttpResponseException {
    /**
     * Creates a new instance of the {@link KeyVaultAdministrationException} class.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     */
    public KeyVaultAdministrationException(String message, HttpResponse response) {
        super(message, response);
    }

    /**
     * Creates a new instance of {@link KeyVaultAdministrationException}.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     * @param value The deserialized response value.
     */
    public KeyVaultAdministrationException(String message, HttpResponse response, KeyVaultAdministrationError value) {
        super(message, response, value);
    }

    @Override
    public KeyVaultAdministrationError getValue() {
        return (KeyVaultAdministrationError) super.getValue();
    }
}
