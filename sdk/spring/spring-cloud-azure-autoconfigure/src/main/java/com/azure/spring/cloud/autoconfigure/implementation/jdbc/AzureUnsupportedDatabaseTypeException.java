// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jdbc;

import com.azure.core.exception.AzureException;

/**
 * Exception indicates that the DatabaseType is not supported to enhance authentication
 * with Azure AD by Spring Cloud Azure.
 */
public class AzureUnsupportedDatabaseTypeException extends AzureException {

    public AzureUnsupportedDatabaseTypeException(String message) {
        super(message);
    }
}
