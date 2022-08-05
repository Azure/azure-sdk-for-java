// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.core.exception.AzureException;

public class AzureUnsupportedDatabaseTypeException extends AzureException {

    public AzureUnsupportedDatabaseTypeException(String message) {
        super(message);
    }
}
