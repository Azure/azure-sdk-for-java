// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.Test;

import java.io.IOException;

public abstract class AutocompleteTestBase extends SearchIndexClientTestBase {

    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        try {
            initializeClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected abstract void initializeClient() throws IOException;

    @Test
    public abstract void canAutocompleteThrowsWhenGivenBadSuggesterName();

    @Test
    public abstract void canAutocompleteDefaultsToOneTermMode();

    @Test
    public abstract void canAutocompleteExcludesFieldsNotInSuggester();

    @Test
    public abstract void canAutocompleteFuzzyIsOffByDefault();

    @Test
    public abstract void canAutocompleteOneTerm();

    @Test
    public abstract void canAutocompleteOneTermWithContext();

    @Test
    public abstract void canAutocompleteStaticallyTypedDocuments();

    @Test
    public abstract void canAutocompleteThrowsWhenRequestIsMalformed();

    @Test
    public abstract void canAutocompleteTwoTerms();

    @Test
    public abstract void testAutocompleteCanUseHitHighlighting();

    @Test
    public abstract void testAutocompleteWithMultipleSelectedFields();

    @Test
    public abstract void testAutocompleteWithSelectedFields();

    @Test
    public abstract void testAutocompleteTopTrimsResults();

    @Test
    public abstract void testAutocompleteWithFilter();

    @Test
    public abstract void testAutocompleteOneTermWithContextWithFuzzy();

    @Test
    public abstract void testAutocompleteOneTermWithFuzzy();

    @Test
    public abstract void testAutocompleteTwoTermsWithFuzzy();

    @Test
    public abstract void testAutocompleteWithFilterAndFuzzy();
}
