// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.experimental.serializer.JsonSerializer;
import com.azure.core.serializer.json.gson.GsonJsonSerializerBuilder;
import com.azure.search.documents.serializer.gson.implementation.SerializationUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;

/**
 * Fluent builder class that configures and instantiates instances of {@link SearchGsonSerializerBuilder}.
 */
public class SearchGsonSerializerBuilder {
    private JsonOptions options;
    /**
     * Constructs a new instance of {@link JsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link JsonSerializer}.
     */
    public JsonSerializer build() {
        return new GsonJsonSerializerBuilder().options(options)
            .serializer(SerializationUtil.registerAdapter(new GsonBuilder())).build();
    }

    /**
     * Sets the {@link ObjectMapper} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link ObjectMapper} will be used.
     *
     * @param jsonOptions {@link JsonOptions} that will be used during serialization.
     * @return The updated JacksonJsonSerializerBuilder class.
     */
    public SearchGsonSerializerBuilder options(JsonOptions jsonOptions) {
        this.options = jsonOptions;
        return this;
    }
}
