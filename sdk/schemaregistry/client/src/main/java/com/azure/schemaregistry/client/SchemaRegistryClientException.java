/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.client;

public class SchemaRegistryClientException extends Exception {
    public SchemaRegistryClientException(String s) {
        super(s);
    }

    public SchemaRegistryClientException(String s, Throwable cause) {
        super(s, cause);
    }
}
