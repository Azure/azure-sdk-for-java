// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.SuggestOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SuggestOptions} and
 * {@link SuggestOptions}.
 */
public final class SuggestOptionsConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SuggestOptions} to {@link SuggestOptions}.
     */
    public static SuggestOptions map(com.azure.search.documents.implementation.models.SuggestOptions obj) {
        if (obj == null) {
            return null;
        }
        SuggestOptions suggestOptions = new SuggestOptions();

        String filter = obj.getFilter();
        suggestOptions.setFilter(filter);

        Boolean useFuzzyMatching = obj.isUseFuzzyMatching();
        suggestOptions.setUseFuzzyMatching(useFuzzyMatching);

        Double minimumCoverage = obj.getMinimumCoverage();
        suggestOptions.setMinimumCoverage(minimumCoverage);

        if (obj.getSelect() != null) {
            List<String> select = new ArrayList<>(obj.getSelect());
            SuggestOptionsHelper.setSelect(suggestOptions, select);
        }

        Integer top = obj.getTop();
        suggestOptions.setTop(top);

        String highlightPostTag = obj.getHighlightPostTag();
        suggestOptions.setHighlightPostTag(highlightPostTag);

        if (obj.getOrderBy() != null) {
            List<String> orderBy = new ArrayList<>(obj.getOrderBy());
            SuggestOptionsHelper.setOrderBy(suggestOptions, orderBy);
        }

        if (obj.getSearchFields() != null) {
            List<String> searchFields = new ArrayList<>(obj.getSearchFields());
            SuggestOptionsHelper.setSearchFields(suggestOptions, searchFields);
        }

        String highlightPreTag = obj.getHighlightPreTag();
        suggestOptions.setHighlightPreTag(highlightPreTag);
        return suggestOptions;
    }

    /**
     * Maps from {@link SuggestOptions} to {@link com.azure.search.documents.implementation.models.SuggestOptions}.
     */
    public static com.azure.search.documents.implementation.models.SuggestOptions map(SuggestOptions obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SuggestOptions suggestOptions =
            new com.azure.search.documents.implementation.models.SuggestOptions();

        String filter = obj.getFilter();
        suggestOptions.setFilter(filter);

        Boolean useFuzzyMatching = obj.useFuzzyMatching();
        suggestOptions.setUseFuzzyMatching(useFuzzyMatching);

        Double minimumCoverage = obj.getMinimumCoverage();
        suggestOptions.setMinimumCoverage(minimumCoverage);

        if (obj.getSelect() != null) {
            List<String> select = new ArrayList<>(obj.getSelect());
            suggestOptions.setSelect(select);
        }

        Integer top = obj.getTop();
        suggestOptions.setTop(top);

        String highlightPostTag = obj.getHighlightPostTag();
        suggestOptions.setHighlightPostTag(highlightPostTag);

        if (obj.getOrderBy() != null) {
            List<String> orderBy = new ArrayList<>(obj.getOrderBy());
            suggestOptions.setOrderBy(orderBy);
        }

        if (obj.getSearchFields() != null) {
            List<String> searchFields = new ArrayList<>(obj.getSearchFields());
            suggestOptions.setSearchFields(searchFields);
        }

        String highlightPreTag = obj.getHighlightPreTag();
        suggestOptions.setHighlightPreTag(highlightPreTag);
        return suggestOptions;
    }

    private SuggestOptionsConverter() {
    }
}
