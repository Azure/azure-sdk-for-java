// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.Gson;

/**
 * Fluent builder class that configures and instantiates instances of {@link GsonJsonSerializer}.
 */
public final class GsonJsonSerializerBuilder {
    private Gson gson;

    /**
     * Constructs a new instance of {@link GsonJsonSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link GsonJsonSerializer}.
     */
    public GsonJsonSerializer build() {
        return gson == null
            ? new GsonJsonSerializer(new Gson())
            : new GsonJsonSerializer(gson);
    }

    /**
     * Sets the {@link Gson} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link Gson} will be used.
     *
     * @param gson {@link Gson} that will be used during serialization.
     * @return The updated GsonJsonSerializerBuilder class.
     */
    public GsonJsonSerializerBuilder serializer(Gson gson) {
        this.gson = gson;
        return this;
    }
}
