// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.exception.AzureException;

// TODO: moderakh we should extend CosmosException. dotnet has problem too
// TODO also we should set proper error message, status code
class KeyVaultAccessException extends AzureException {
    public KeyVaultAccessException() {
        // TODO: remove this
    }

    public KeyVaultAccessException(int statusCode,
                                   String keyVaultErrorCode,
                                   String errorMessage,
                                   Exception innerException) {
        // TODO: CosmosException as super class?
        //super(statusCode, keyVaultErrorCode, errorMessage, innerException)
    }
}
