// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.serializer;

/**
 * Interface that represents a JSON null.
 */
public interface JsonNull extends JsonNode {
    @Override
    default boolean isNull() {
        return true;
    }
}
