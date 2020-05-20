// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.StemmerOverrideTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter} and
 * {@link StemmerOverrideTokenFilter}.
 */
public final class StemmerOverrideTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(StemmerOverrideTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter} to
     * {@link StemmerOverrideTokenFilter}.
     */
    public static StemmerOverrideTokenFilter map(com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        StemmerOverrideTokenFilter stemmerOverrideTokenFilter = new StemmerOverrideTokenFilter();

        String _name = obj.getName();
        stemmerOverrideTokenFilter.setName(_name);

        if (obj.getRules() != null) {
            List<String> _rules = new ArrayList<>(obj.getRules());
            stemmerOverrideTokenFilter.setRules(_rules);
        }
        return stemmerOverrideTokenFilter;
    }

    /**
     * Maps from {@link StemmerOverrideTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter map(StemmerOverrideTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter stemmerOverrideTokenFilter =
            new com.azure.search.documents.implementation.models.StemmerOverrideTokenFilter();

        String _name = obj.getName();
        stemmerOverrideTokenFilter.setName(_name);

        if (obj.getRules() != null) {
            List<String> _rules = new ArrayList<>(obj.getRules());
            stemmerOverrideTokenFilter.setRules(_rules);
        }
        return stemmerOverrideTokenFilter;
    }
}
