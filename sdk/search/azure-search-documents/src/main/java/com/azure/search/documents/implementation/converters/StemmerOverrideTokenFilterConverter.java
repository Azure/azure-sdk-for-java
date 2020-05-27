// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.indexes.models.StemmerOverrideTokenFilter;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter} and
 * {@link StemmerOverrideTokenFilter}.
 */
public final class StemmerOverrideTokenFilterConverter {
    /**
     * Maps from {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter} to
     * {@link StemmerOverrideTokenFilter}.
     */
    public static StemmerOverrideTokenFilter map(com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        StemmerOverrideTokenFilter stemmerOverrideTokenFilter = new StemmerOverrideTokenFilter();

        String name = obj.getName();
        stemmerOverrideTokenFilter.setName(name);

        if (obj.getRules() != null) {
            List<String> rules = new ArrayList<>(obj.getRules());
            stemmerOverrideTokenFilter.setRules(rules);
        }
        return stemmerOverrideTokenFilter;
    }

    /**
     * Maps from {@link StemmerOverrideTokenFilter} to
     * {@link com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter}.
     */
    public static com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter map(StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter stemmerOverrideTokenFilter =
            new com.azure.search.documents.indexes.implementation.models.StemmerOverrideTokenFilter();

        String name = obj.getName();
        stemmerOverrideTokenFilter.setName(name);

        if (obj.getRules() != null) {
            List<String> rules = new ArrayList<>(obj.getRules());
            stemmerOverrideTokenFilter.setRules(rules);
        }
        return stemmerOverrideTokenFilter;
    }

    private StemmerOverrideTokenFilterConverter() {
    }
}
