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
    private MemberNameConverter serializer;

    /**
     * Gets the serializer use to build search fields in
     * {@link SearchIndexClient#buildSearchField(Class, FieldBuilderOptions)} buildSearchField} or
     * {@link SearchIndexAsyncClient#buildSearchField(Class, FieldBuilderOptions) buildSearchField}
     *
     * @return the custom serializer.
     */
    public MemberNameConverter getSerializer() {
        return serializer;
    }

    /**
     * Sets the custom serializer.
     *
     * @param serializer The custom serializer to set
     * @return The {@link FieldBuilderOptions} object itself.
     */
    public FieldBuilderOptions setSerializer(MemberNameConverter serializer) {
        this.serializer = Objects.requireNonNull(serializer, "The serializer cannot be null");
        return this;
    }


}
