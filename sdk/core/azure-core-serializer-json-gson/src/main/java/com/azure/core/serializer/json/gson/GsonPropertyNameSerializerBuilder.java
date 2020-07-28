// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.GsonBuilder;

/**
 * Fluent builder class that configures and instantiates instances of {@link GsonPropertyNameSerializer}.
 */
public class GsonPropertyNameSerializerBuilder {

    private GsonBuilder gsonBuilder;

    /**
     * Constructs a new instance of {@link GsonPropertyNameSerializer} with the configurations set in this builder.
     *
     * @return A new instance of {@link GsonPropertyNameSerializer}.
     */
    public GsonPropertyNameSerializer build() {
        GsonBuilder gsonBuilder = (this.gsonBuilder == null) ? new GsonBuilder() : this.gsonBuilder;
        return new GsonPropertyNameSerializer(gsonBuilder.create());
    }

    /**
     * Sets the {@link GsonBuilder} that will be used during serialization.
     * <p>
     * If this is set to {@code null} the default {@link GsonBuilder} will be used.
     *
     * @param gsonBuilder {@link GsonBuilder} that will be used during serialization.
     * @return The updated GsonPropertyNameSerializer class.
     */
    public GsonPropertyNameSerializerBuilder serializer(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;
        return this;
    }
}
