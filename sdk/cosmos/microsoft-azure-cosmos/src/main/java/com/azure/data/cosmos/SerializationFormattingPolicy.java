// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The formatting policy associated with JSON serialization in the Azure Cosmos DB database service.
 */
public enum SerializationFormattingPolicy {

    /**
     *  No additional formatting required.
     */
    NONE,

    /**
     * Indent the fields appropriately.
     */
    INDENTED
}
