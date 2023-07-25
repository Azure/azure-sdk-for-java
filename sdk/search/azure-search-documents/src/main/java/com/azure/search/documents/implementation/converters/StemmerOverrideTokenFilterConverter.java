// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StemmerOverrideTokenFilter;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter} and
 * {@link StemmerOverrideTokenFilter}.
 */
public final class StemmerOverrideTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter} to {@link
     * StemmerOverrideTokenFilter}.
     */
    public static StemmerOverrideTokenFilter map(
        com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        return new StemmerOverrideTokenFilter(obj.getName(), obj.getRules());
    }

    /**
     * Maps from {@link StemmerOverrideTokenFilter} to {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter map(
        StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }

        return new com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter(obj.getName(),
            obj.getRules());
    }

    private StemmerOverrideTokenFilterConverter() {
    }
}
