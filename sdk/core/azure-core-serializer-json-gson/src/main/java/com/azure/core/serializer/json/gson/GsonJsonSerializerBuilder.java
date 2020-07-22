// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.JsonOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Fluent builder class that configures and instantiates instances of {@link GsonJsonSerializer}.
 */
public final class GsonJsonSerializerBuilder {
    private GsonBuilder gsonBuilder;
    private boolean serializeNulls;

    /**
     * Constructs a new instance of {@link GsonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link GsonJsonSerializer}.
     */
    public GsonJsonSerializer build() {
        GsonBuilder gsonBuilder = (this.gsonBuilder == null) ? new GsonBuilder() : this.gsonBuilder;

        if (serializeNulls) {
            gsonBuilder.serializeNulls();
        }
        return new GsonJsonSerializer(gsonBuilder.create());
    }

    /**
     * Sets the {@link Gson} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link Gson} will be used.
     *
     * @param gsonBuilder {@link GsonBuilder} that will be used during serialization.
     * @return The updated GsonJsonSerializerBuilder class.
     */
    public GsonJsonSerializerBuilder serializer(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
        return this;
    }


    /**
     * Sets the {@link Gson} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link Gson} will be used.
     *
     * @param options {@link Gson} that will be used during serialization.
     * @return The updated GsonJsonSerializerBuilder class.
     */
    public GsonJsonSerializerBuilder options(JsonOptions options) {
        this.serializeNulls = options == null ? false : options.isNullIncluded();
        return this;
    }
}
