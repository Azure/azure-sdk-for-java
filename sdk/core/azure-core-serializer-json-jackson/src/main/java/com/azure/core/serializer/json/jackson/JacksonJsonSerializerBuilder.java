// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonJsonSerializer}.
 */
public final class JacksonJsonSerializerBuilder {
    private ObjectMapper objectMapper;
    private boolean serializeNulls;

    /**
     * Constructs a new instance of {@link JacksonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link JacksonJsonSerializer}.
     */
    public JacksonJsonSerializer build() {
        ObjectMapper mapper = objectMapper == null ? new ObjectMapper() : objectMapper;
        if (serializeNulls) {
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        }
        return new JacksonJsonSerializer(mapper);
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

    /**
     * Sets the {@link JsonOptions} that will be used during serialization.
     *
     * JsonOptions currently support to config whether to serialize null during serialization.
     *
     * @param options {@link JsonOptions} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public JacksonJsonSerializerBuilder options(JsonOptions options) {
        this.serializeNulls = options != null && options.isNullIncluded();
        return this;
    }
}
