// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.serializer.json.jackson.implementation.ObjectMapperShim;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonJsonSerializer}.
 */
public final class JacksonJsonSerializerBuilder {
    /*
     * This is identical to JacksonAdapter to keep the serialization behaviour similar to what is used in Azure Core.
     */
    private static final ObjectMapperShim DEFAULT_MAPPER = ObjectMapperShim
        .createJsonMapper(ObjectMapperShim.createSimpleMapper());

    private ObjectMapperShim objectMapper;

    /**
     * Creates an instance of {@link JacksonJsonSerializerBuilder}.
     */
    public JacksonJsonSerializerBuilder() {
    }

    /**
     * Constructs a new instance of {@link JacksonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link JacksonJsonSerializer}.
     */
    public JacksonJsonSerializer build() {
        return (objectMapper == null)
            ? new JacksonJsonSerializer(DEFAULT_MAPPER)
            : new JacksonJsonSerializer(objectMapper);
    }

    /**
     * Sets the {@link ObjectMapper} that will be used during serialization.
     * <p>
     * If this is set to {@code null} an internal implementation with default visibility and non-null inclusion
     * will be used as the default.
     *
     * @param objectMapper {@link ObjectMapper} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public JacksonJsonSerializerBuilder serializer(ObjectMapper objectMapper) {
        this.objectMapper = new ObjectMapperShim(objectMapper);
        return this;
    }
}
