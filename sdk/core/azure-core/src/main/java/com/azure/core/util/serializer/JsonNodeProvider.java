// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

/**
 * Interface to be implemented by an azure-core plugin that wishes to provide a {@link JsonNode} implementation.
 */
@FunctionalInterface
public interface JsonNodeProvider {
    /**
     * Creates a {@link JsonNode} from the passed value.
     *
     * @param value Value to turn into a {@link JsonNode}.
     * @param <T> Generic type of JsonNode.
     * @return A {@link JsonNode} representation of the object.
     */
    <T extends JsonNode> T createInstance(Object value);
}
