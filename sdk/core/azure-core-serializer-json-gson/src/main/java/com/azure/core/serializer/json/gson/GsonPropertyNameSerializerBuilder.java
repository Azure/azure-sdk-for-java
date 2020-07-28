// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonPropertyNameSerializerBuilder {

    private GsonBuilder gsonBuilder;

    /**
     * Constructs a new instance of {@link GsonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link GsonJsonSerializer}.
     */
    public GsonPropertyNameSerializer build() {
        GsonBuilder gsonBuilder = (this.gsonBuilder == null) ? new GsonBuilder() : this.gsonBuilder;
        return new GsonPropertyNameSerializer(gsonBuilder.create());
    }

    /**
     * Sets the {@link Gson} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link Gson} will be used.
     *
     * @param gsonBuilder {@link GsonBuilder} that will be used during serialization.
     * @return The updated GsonJsonSerializerBuilder class.
     */
    public GsonPropertyNameSerializerBuilder serializer(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
        return this;
    }
}
