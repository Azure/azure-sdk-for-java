// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.implementation.util.Utility;
import com.azure.search.documents.models.SuggestOptions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SuggestOptionsHandlerTests {

    private static final List<String> SELECT_STAR = Collections.singletonList("*");

    @Test
    public void ensureSelectConvertsEmptyToSelectStar() {
        List<String> emptySelect = Collections.emptyList();

        SuggestOptions suggestOptions = createTestOptions();
        assertEquals(suggestOptions.getSelect(), emptySelect);

        SuggestOptions ensuredSuggestOptions = Utility.ensureSuggestOptions(suggestOptions);
        assertEquals(ensuredSuggestOptions.getSelect(), SELECT_STAR);
    }

    @Test
    public void ensureSelectLeavesOtherPropertiesUnchanged() {

        SuggestOptions suggestOptions = createTestOptions();
        SuggestOptions ensuredSuggestOptions = Utility.ensureSuggestOptions(suggestOptions);

        assertEquals(suggestOptions.getFilter(), ensuredSuggestOptions.getFilter());
        assertEquals(suggestOptions.getHighlightPostTag(), ensuredSuggestOptions.getHighlightPostTag());
        assertEquals(suggestOptions.getHighlightPreTag(), ensuredSuggestOptions.getHighlightPreTag());
        assertEquals(suggestOptions.getMinimumCoverage(), ensuredSuggestOptions.getMinimumCoverage());
        assertEquals(suggestOptions.getOrderBy(), ensuredSuggestOptions.getOrderBy());
        assertEquals(suggestOptions.getSearchFields(), ensuredSuggestOptions.getSearchFields());
        assertEquals(suggestOptions.getTop(), ensuredSuggestOptions.getTop());
        assertEquals(suggestOptions.useFuzzyMatching(), ensuredSuggestOptions.useFuzzyMatching());

    }

    @Test
    public void ensureSelectReturnsSelfWhenSelectIsPopulated() {
        SuggestOptions suggestOptions = createTestOptions();
        SuggestOptions ensuredSuggestOptions = Utility.ensureSuggestOptions(suggestOptions);

        assertEquals(suggestOptions, ensuredSuggestOptions);
    }

    private static SuggestOptions createTestOptions() {
        return new SuggestOptions()
            .setFilter("x eq y")
            .setHighlightPreTag("<em>")
            .setHighlightPostTag("</em>")
            .setMinimumCoverage(33.3)
            .setOrderBy("a", "b desc")
            .setSearchFields("a", "b", "c")
            .setSelect()
            .setTop(5)
            .setUseFuzzyMatching(true);
    }
}
