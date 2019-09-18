// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.env.SearchIndexClientTestBase;
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
    public abstract void autocompleteThrowsWhenGivenBadSuggesterName() throws Exception;

    @Test
    public abstract void autocompleteDefaultsToOneTermMode() throws Exception;

    @Test
    public abstract void autocompleteExcludesFieldsNotInSuggester() throws Exception;

    @Test
    public abstract void autocompleteFuzzyIsOffByDefault() throws Exception;

    @Test
    public abstract void autocompleteOneTerm() throws Exception;

    @Test
    public abstract void autocompleteStaticallyTypedDocuments() throws Exception;

    @Test
    public abstract void autocompleteThrowsWhenRequestIsMalformed() throws Exception;

}
