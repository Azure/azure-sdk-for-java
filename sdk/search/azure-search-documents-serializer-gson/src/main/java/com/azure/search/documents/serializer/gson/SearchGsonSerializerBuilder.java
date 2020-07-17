// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.serializer.gson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.search.documents.serializer.SearchSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchGsonSerializerBuilder {

    /**
     * Constructs a new instance of {@link SearchSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link SearchSerializer}.
     */
    public  build() {
//        return new SearchGsonSerializer(() -> {
//            Gson gson = new Gson();
//        });
        return
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
