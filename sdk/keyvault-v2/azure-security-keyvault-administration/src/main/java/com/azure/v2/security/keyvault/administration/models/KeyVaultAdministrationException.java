// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration.models;

import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * Exception thrown for an invalid response with {@link KeyVaultAdministrationError} information.
 */
@Metadata(properties = { MetadataProperties.IMMUTABLE })
public final class KeyVaultAdministrationException extends HttpResponseException {
    /**
     * Creates a new instance of {@link KeyVaultAdministrationException}.
     *
     * @param message The exception message or the response content if a message is not available.
     * @param response The HTTP response.
     * @param value The deserialized response value.
     */
    public KeyVaultAdministrationException(String message, Response<BinaryData> response,
        KeyVaultAdministrationError value) {
        super(message, response, value);
    }

    @Override
    public KeyVaultAdministrationError getValue() {
        return (KeyVaultAdministrationError) super.getValue();
    }
}
