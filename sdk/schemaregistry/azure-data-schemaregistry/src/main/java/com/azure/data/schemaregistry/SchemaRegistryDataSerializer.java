// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import reactor.core.publisher.Mono;

/**
 * serializer interface
 */
public interface SchemaRegistryDataSerializer {

    Mono<Void> prefetchSchema(Class objectClass);

    /**
     * serializer method
     * @param object to be serialized
     * @return Mono of serialized byte array
     */
    byte[] serialize(Object object);
}
