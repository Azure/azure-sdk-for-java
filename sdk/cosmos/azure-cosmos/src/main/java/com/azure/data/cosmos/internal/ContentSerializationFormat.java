// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

public enum ContentSerializationFormat {
    /**
     * Standard JSON RFC UTF-8 text
     */
    JsonText,

    /**
     * CUSTOM binary for Cosmos DB that encodes a superset of JSON values.
     */
    CosmosBinary,
}
