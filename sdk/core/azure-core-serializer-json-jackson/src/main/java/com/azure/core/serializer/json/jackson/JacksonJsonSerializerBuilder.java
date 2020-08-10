// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonJsonSerializer}.
 */
public final class JacksonJsonSerializerBuilder {
    private ObjectMapper objectMapper;

    /**
     * Constructs a new instance of {@link JacksonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link JacksonJsonSerializer}.
     */
    public JacksonJsonSerializer build() {
        if (objectMapper == null) {
            return new JacksonJsonSerializer(new ObjectMapper());
        }
        return new JacksonJsonSerializer(objectMapper);
    }

    /**
     * Sets the {@link ObjectMapper} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link ObjectMapper} will be used.
     *
     * @param objectMapper {@link ObjectMapper} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public JacksonJsonSerializerBuilder serializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }
}
