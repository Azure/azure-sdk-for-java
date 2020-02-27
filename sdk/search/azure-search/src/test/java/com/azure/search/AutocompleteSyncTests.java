// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.util.Context;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class AutocompleteSyncTests extends SearchIndexClientTestBase {
    private static final String HOTELS_DATA_JSON = "HotelsDataArray.json";

    private SearchIndexClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();

        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterName() {
        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("very po", "Invalid suggester", params, generateRequestOptions(), Context.NONE);

        assertHttpResponseException(
            () -> results.iterableByPage().iterator().next(),
            HttpResponseStatus.BAD_REQUEST,
            "The specified suggester name 'Invalid suggester' does not exist in this index definition.\\r\\nParameter name: name");
    }

    @Test
    public void canAutocompleteDefaultsToOneTermMode() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        validateResults(client.autocomplete("po", "sg").iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteOneTermWithContext() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("looking for very po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggester() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setSearchFields("HotelName");

        Iterator<AutocompletePagedResponse> results = client
            .autocomplete("luxu", "sg", params, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        // One page, with 0 items
        assertEquals(0, results.next().getValue().size());
        assertFalse(results.hasNext());
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefault() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        Iterator<AutocompletePagedResponse> results = client
            .autocomplete("pi", "sg", params, generateRequestOptions(), Context.NONE)
            .iterableByPage()
            .iterator();

        // One page, with 0 items
        assertEquals(0, results.next().getValue().size());
        assertFalse(results.hasNext());
    }

    @Test
    public void canAutocompleteOneTerm() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocuments() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(false);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("very po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformed() {
        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client.autocomplete("very po", "");
        assertHttpResponseException(
            () -> results.iterableByPage().iterator().next(),
            HttpResponseStatus.BAD_REQUEST,
            "Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in the query.\\r\\nParameter name: suggestions"
        );
    }

    @Test
    public void canAutocompleteTwoTerms() {
        List<String> expectedText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");
        List<String> expectedQueryPlusText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteCanUseHitHighlighting() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFields() {
        List<String> expectedText = Arrays.asList("model", "modern");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName", "Description");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("mod", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithSelectedFields() {
        List<String> expectedText = Collections.singletonList("modern");
        List<String> expectedQueryPlusText = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName")
            .setFilter("HotelId eq '7'");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("mod", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteTopTrimsResults() {
        List<String> expectedText = Arrays.asList("point", "police");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setTop(2);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithFilter() {
        List<String> expectedText = Collections.singletonList("polite");
        List<String> expectedQueryPlusText = Collections.singletonList("polite");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("po", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzy() {
        List<String> expectedText = Arrays.asList("very polite", "very police");
        List<String> expectedQueryPlusText = Arrays.asList("very polite", "very police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("very polit", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzy() {
        List<String> expectedText = Arrays.asList("model", "modern", "morel", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern", "morel", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("mod", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzy() {
        List<String> expectedText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.TWO_TERMS)
            .setUseFuzzyMatching(true);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("mod", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzy() {
        List<String> expectedText = Arrays.asList("modern", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("modern", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client
            .autocomplete("mod", "sg", params, generateRequestOptions(), Context.NONE);

        validateResults(results.iterator(), expectedText, expectedQueryPlusText);
    }

    /**
     * Compare the autocomplete results with the expected strings
     *
     * @param iterator results iterator
     * @param expectedText expected text
     * @param expectedQueryPlusText expected query plus text
     */
    private void validateResults(Iterator<AutocompleteItem> iterator, List<String> expectedText,
        List<String> expectedQueryPlusText) {
        int index = 0;
        while (iterator.hasNext()) {
            AutocompleteItem item = iterator.next();
            assertEquals(expectedText.get(index), item.getText());
            assertEquals(expectedQueryPlusText.get(index), item.getQueryPlusText());
            index++;
        }
    }
}
