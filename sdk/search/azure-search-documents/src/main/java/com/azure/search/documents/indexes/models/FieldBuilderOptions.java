// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.core.util.serializer.MemberNameConverterProviders;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;

import java.util.Objects;

/**
 * Additional parameters to build {@link SearchField}.
 */
@Fluent
public final class FieldBuilderOptions {
    private JsonSerializer jsonSerializer;

    /**
     * Gets the serializer used to aid the construction of {@link SearchField SearchFields} in {@link
     * SearchIndexClient#buildSearchFields(Class, FieldBuilderOptions)} buildSearchFields} or {@link
     * SearchIndexAsyncClient#buildSearchFields(Class, FieldBuilderOptions) buildSearchFields}.
     * <p>
     * If {@link JsonSerializer} is {@code null} or doesn't implement the {@link MemberNameConverter} interface then
     * {@link MemberNameConverterProviders#createInstance()} will be used to provide a converter from the classpath.
     *
     * @return The custom {@link JsonSerializer}.
     */
    public JsonSerializer getJsonSerializer() {
        return jsonSerializer;
    }

    /**
     * Sets the serializer.
     * <p>
     * For building {@link SearchField SearchFields} it is expected that the {@link JsonSerializer} passed also
     * implements the {@link MemberNameConverter} interface. If it doesn't {@link
     * MemberNameConverterProviders#createInstance()} will be used to provide a converter from the classpath.
     *
     * @param jsonSerializer The custom serializer.
     * @return The updated FieldBuilderOptions object.
     */
    public FieldBuilderOptions setJsonSerializer(JsonSerializer jsonSerializer) {
        this.jsonSerializer = Objects.requireNonNull(jsonSerializer, "'jsonSerializer' cannot be null");
        return this;
    }


}
