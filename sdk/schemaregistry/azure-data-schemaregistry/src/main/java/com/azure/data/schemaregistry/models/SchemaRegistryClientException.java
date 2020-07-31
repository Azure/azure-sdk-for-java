// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.models;

import com.azure.core.exception.AzureException;

/**
 * Runtime exception to be returned from CachedSchemaRegistryClient implementations.
 */
public class SchemaRegistryClientException extends AzureException {
    /**
     * @param s error message returned from schema registry client
     */
    public SchemaRegistryClientException(String s) {
        super(s);
    }

    /**
     * @param s error message returned from schema registry client
     * @param cause Throwable cause of the exception
     */
    public SchemaRegistryClientException(String s, Throwable cause) {
        super(s, cause);
    }
}
