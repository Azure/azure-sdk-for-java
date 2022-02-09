// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.ElisionTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter} and
 * {@link ElisionTokenFilter}.
 */
public final class ElisionTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter} to
     * {@link ElisionTokenFilter}.
     */
    public static ElisionTokenFilter map(com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new ElisionTokenFilter(obj.getName()).setArticles(obj.getArticles());
    }

    /**
     * Maps from {@link ElisionTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter map(ElisionTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.ElisionTokenFilter(obj.getName())
            .setArticles(obj.getArticles());
    }

    private ElisionTokenFilterConverter() {
    }
}
