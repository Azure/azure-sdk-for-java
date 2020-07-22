// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.jackson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.serializer.jackson.implementation.SerializationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fluent builder class that configures and instantiates instances of {@link SearchJacksonSerializerBuilder}.
 */
public class SearchJacksonSerializerBuilder {
    private JsonOptions options;

    /**
     * Constructs a new instance of {@link JacksonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link JacksonJsonSerializer}.
     */
    public JacksonJsonSerializer build() {
        ObjectMapper mapper = new JacksonAdapter().serializer();
        SerializationUtil.configureMapper(mapper);
        return new JacksonJsonSerializerBuilder().serializer(mapper).options(options).build();
    }

    /**
     * Sets the {@link ObjectMapper} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link ObjectMapper} will be used.
     *
     * @param jsonOptions {@link JsonOptions} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public SearchJacksonSerializerBuilder options(JsonOptions jsonOptions) {
        this.options = jsonOptions;
        return this;
    }
}
