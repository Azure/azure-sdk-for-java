// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes.models;

import com.azure.core.util.serializer.MemberNameConverter;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;

import java.util.Objects;

/**
 * Additional parameters to build {@link SearchField}.
 */
public class FieldBuilderOptions {
    private MemberNameConverter converter;

    /**
     * Gets the serializer use to build search fields in
     * {@link SearchIndexClient#buildSearchFields(Class, FieldBuilderOptions)} buildSearchFields} or
     * {@link SearchIndexAsyncClient#buildSearchFields(Class, FieldBuilderOptions) buildSearchFields}
     *
     * @return the custom serializer.
     */
    public MemberNameConverter getConverter() {
        return converter;
    }

    /**
     * Sets the custom serializer.
     *
     * @param converter The custom serializer to set
     * @return The {@link FieldBuilderOptions} object itself.
     */
    public FieldBuilderOptions setConverter(MemberNameConverter converter) {
        this.converter = Objects.requireNonNull(converter, "The serializer cannot be null");
        return this;
    }


}
