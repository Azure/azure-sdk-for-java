// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.ElisionTokenFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.ElisionTokenFilter} and
 * {@link ElisionTokenFilter}.
 */
public final class ElisionTokenFilterConverter {
    private static final ClientLogger LOGGER = new ClientLogger(ElisionTokenFilterConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.ElisionTokenFilter} to
     * {@link ElisionTokenFilter}.
     */
    public static ElisionTokenFilter map(com.azure.search.documents.implementation.models.ElisionTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        ElisionTokenFilter elisionTokenFilter = new ElisionTokenFilter();

        String _name = obj.getName();
        elisionTokenFilter.setName(_name);

        if (obj.getArticles() != null) {
            List<String> _articles = new ArrayList<>(obj.getArticles());
            elisionTokenFilter.setArticles(_articles);
        }
        return elisionTokenFilter;
    }

    /**
     * Maps from {@link ElisionTokenFilter} to
     * {@link com.azure.search.documents.implementation.models.ElisionTokenFilter}.
     */
    public static com.azure.search.documents.implementation.models.ElisionTokenFilter map(ElisionTokenFilter obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.ElisionTokenFilter elisionTokenFilter =
            new com.azure.search.documents.implementation.models.ElisionTokenFilter();

        String _name = obj.getName();
        elisionTokenFilter.setName(_name);

        if (obj.getArticles() != null) {
            List<String> _articles = new ArrayList<>(obj.getArticles());
            elisionTokenFilter.setArticles(_articles);
        }
        return elisionTokenFilter;
    }
}
