// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.serializer.SearchSerializer;
import com.azure.search.documents.serializer.jackson.implementation.SerializationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchJacksonSerializerBuilder {
    private ObjectMapper objectMapper;
    private static final ObjectMapper DEFAULT_MAPPER;
    static {
        DEFAULT_MAPPER = new JacksonAdapter().serializer();
        SerializationUtil.configureMapper(DEFAULT_MAPPER);
    }

    /**
     * Constructs a new instance of {@link SearchSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link SearchSerializer}.
     */
    public SearchSerializer build() {
        return (objectMapper == null)
            ? new SearchJacksonSerializer(DEFAULT_MAPPER)
            : new SearchJacksonSerializer(objectMapper);
    }

    /**
     * Sets the {@link ObjectMapper} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link ObjectMapper} will be used.
     *
     * @param objectMapper {@link ObjectMapper} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public SearchJacksonSerializerBuilder serializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        return this;
    }
}
