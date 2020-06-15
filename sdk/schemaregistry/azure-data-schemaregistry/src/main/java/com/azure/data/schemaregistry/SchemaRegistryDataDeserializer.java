// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import reactor.core.publisher.Mono;

/**
 * deserializer implementation
 */
public interface SchemaRegistryDataDeserializer {
    /**
     * deserialize SR client produced bytes
     * @param data to be deserialized
     * @return deserialized object
     */
    Mono<Object> deserialize(byte[] data);
}
