// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.search.common.SuggestOptionsHandler;
import com.azure.search.models.SuggestOptions;
import org.junit.Assert;
import org.junit.Test;
import java.util.Collections;
import java.util.List;

public class SuggestOptionsHandlerTests {

    private static final List<String> SELECT_STAR = Collections.singletonList("*");

    @Test
    public void ensureSelectConvertsEmptyToSelectStar() {

        List<String> emptySelect =  Collections.emptyList();

        SuggestOptions suggestOptions = createTestOptions();
        Assert.assertEquals(suggestOptions.getSelect(), emptySelect);

        SuggestOptions ensuredSuggestOptions = SuggestOptionsHandler.ensureSuggestOptions(suggestOptions);
        Assert.assertEquals(ensuredSuggestOptions.getSelect(), SELECT_STAR);
    }

    @Test
    public void ensureSelectLeavesOtherPropertiesUnchanged() {

        SuggestOptions suggestOptions = createTestOptions();
        SuggestOptions ensuredSuggestOptions = SuggestOptionsHandler.ensureSuggestOptions(suggestOptions);

        Assert.assertEquals(suggestOptions.getFilter(), ensuredSuggestOptions.getFilter());
        Assert.assertEquals(suggestOptions.getHighlightPostTag(), ensuredSuggestOptions.getHighlightPostTag());
        Assert.assertEquals(suggestOptions.getHighlightPreTag(), ensuredSuggestOptions.getHighlightPreTag());
        Assert.assertEquals(suggestOptions.getMinimumCoverage(), ensuredSuggestOptions.getMinimumCoverage());
        Assert.assertEquals(suggestOptions.getOrderBy(), ensuredSuggestOptions.getOrderBy());
        Assert.assertEquals(suggestOptions.getSearchFields(), ensuredSuggestOptions.getSearchFields());
        Assert.assertEquals(suggestOptions.getTop(), ensuredSuggestOptions.getTop());
        Assert.assertEquals(suggestOptions.isUseFuzzyMatching(), ensuredSuggestOptions.isUseFuzzyMatching());

    }

    @Test
    public void ensureSelectReturnsSelfWhenSelectIsPopulated() {
        SuggestOptions suggestOptions = createTestOptions();
        SuggestOptions ensuredSuggestOptions = SuggestOptionsHandler.ensureSuggestOptions(suggestOptions);

        Assert.assertSame(suggestOptions, ensuredSuggestOptions);
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
