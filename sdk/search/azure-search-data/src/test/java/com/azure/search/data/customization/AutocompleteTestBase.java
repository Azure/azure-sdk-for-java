// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import org.junit.Test;

public abstract class AutocompleteTestBase extends SearchIndexClientTestBase {

    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    protected abstract void initializeClient();

    @Test
    public abstract void canAutocompleteThrowsWhenGivenBadSuggesterName() throws Exception;

    @Test
    public abstract void canAutocompleteDefaultsToOneTermMode() throws Exception;

    @Test
    public abstract void canAutocompleteExcludesFieldsNotInSuggester() throws Exception;

    @Test
    public abstract void canAutocompleteFuzzyIsOffByDefault() throws Exception;

    @Test
    public abstract void canAutocompleteOneTerm() throws Exception;

    @Test
    public abstract void canAutocompleteOneTermWithContext() throws Exception;

    @Test
    public abstract void canAutocompleteOneTermWithFuzzy() throws Exception;

    @Test
    public abstract void canAutocompleteOneTermWithContextWithFuzzy() throws Exception;

    @Test
    public abstract void canAutocompleteStaticallyTypedDocuments() throws Exception;

    @Test
    public abstract void canAutocompleteThrowsWhenRequestIsMalformed() throws Exception;

    @Test
    public abstract void canAutocompleteTwoTerms() throws Exception;

    @Test
    public abstract void canAutocompleteTwoTermsWithFuzzy() throws Exception;

    @Test
    public abstract void testAutocompleteCanUseHitHighlighting() throws Exception;

    @Test
    public abstract void testAutocompleteWithFilter() throws Exception;

    @Test
    public abstract void testAutocompleteOneTermWithContextWithFuzzy() throws Exception;

    @Test
    public abstract void testAutocompleteOneTermWithFuzzy() throws Exception;

    @Test
    public abstract void testAutocompleteTwoTermsWithFuzzy() throws Exception;

    @Test
    public abstract void testAutocompleteWithFilterAndFuzzy() throws Exception;
}
