// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.serializer.JacksonAdapter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link JacksonJsonSerializer}.
 */
public final class JacksonJsonSerializerBuilder {
    /*
     * The ObjectMapper used by JacksonAdapter uses inclusion scopes and null handling that differs from the default
     * Jackson uses. This configuration is reset here by mutating the inclusion scope and null handling to use the
     * default Jackson values so that JacksonJsonSerializer has less friction when this default is used.
     */
    private static final ObjectMapperShim DEFAULT_MAPPER = ObjectMapperShim
        .createJsonMapper(ObjectMapperShim.createSimpleMapper(),
                  (mapper, innerMapper) -> mapper
                    .setSerializationInclusion(JsonInclude.Include.USE_DEFAULTS)
                    .setDefaultVisibility(JsonAutoDetect.Value.defaultVisibility()));

    private ObjectMapperShim objectMapper;

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
     * If this is set to {@code null} {@link JacksonAdapter#serializer()} with default visibility and non-null inclusion
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
