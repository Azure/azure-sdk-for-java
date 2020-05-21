// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.schemaregistry.client;

/**
 * Runtime exception to be returned from SchemaRegistryClient implementations.
 */
public class SchemaRegistryClientException extends RuntimeException {
    /**
     * @param s error message returned from schema registry client
     */
    SchemaRegistryClientException(String s) {
        super(s);
    }

    /**
     * @param s error message returned from schema registry client
     * @param cause Throwable cause of the exception
     */
    SchemaRegistryClientException(String s, Throwable cause) {
        super(s, cause);
    }
}
