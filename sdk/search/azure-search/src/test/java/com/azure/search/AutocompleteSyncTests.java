// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.search.common.AutoCompletePagedResponse;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;

public class AutocompleteSyncTests extends AutocompleteTestBase {

    private SearchIndexClient client;

    @Override
    protected void initializeClient() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterName() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterable<AutocompleteItem> results = client.autocomplete("very po", "Invalid suggester", params, null);
        assertException(
            () -> results.iterableByPage().iterator().next(),
            HttpResponseException.class,
            "The specified suggester name 'Invalid suggester' does not exist in this index definition.\\r\\nParameter name: name");
    }

    @Override
    public void canAutocompleteDefaultsToOneTermMode() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg");

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void canAutocompleteOneTermWithContext() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedIterable<AutocompleteItem> results = client.autocomplete("looking for very po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggester() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setSearchFields("HotelName");

        PagedIterable<AutocompleteItem> results = client.autocomplete("luxu", "sg", params, null);
        Assert.assertNotNull(results);
        List allItems = results.streamByPage().collect(Collectors.toList());
        // One page, with 0 items
        Assert.assertEquals(1, allItems.size());
        Assert.assertEquals(0, ((AutoCompletePagedResponse) allItems.get(0)).getItems().size());
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefault() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterable<AutocompleteItem> results = client.autocomplete("pi", "sg", params, null);
        Assert.assertNotNull(results);
        List allItems = results.streamByPage().collect(Collectors.toList());
        // One page, with 0 items
        Assert.assertEquals(1, allItems.size());
        Assert.assertEquals(0, ((AutoCompletePagedResponse) allItems.get(0)).getItems().size());
    }

    @Test
    public void canAutocompleteOneTerm() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocuments() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setUseFuzzyMatching(false);

        PagedIterable<AutocompleteItem> results = client.autocomplete("very po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformed() {
        PagedIterable<AutocompleteItem> results = client.autocomplete("very po", "");
        assertException(
            () -> results.iterableByPage().iterator().next(),
            HttpResponseException.class,
            "Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in the query.\\r\\nParameter name: suggestions"
        );
    }

    @Override
    public void canAutocompleteTwoTerms() {
        List<String> expectedText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");
        List<String> expectedQueryPlusText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteCanUseHitHighlighting() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithMultipleSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName", "Description");

        PagedIterable<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("modern");
        List<String> expectedQueryPlusText = Arrays.asList("modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName")
            .setFilter("HotelId eq '7'");

        PagedIterable<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteTopTrimsResults() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setTop(2);

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithFilter() throws Exception {
        List<String> expectedText = Arrays.asList("polite");
        List<String> expectedQueryPlusText = Arrays.asList("polite");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        PagedIterable<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteOneTermWithContextWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("very polite", "very police");
        List<String> expectedQueryPlusText = Arrays.asList("very polite", "very police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        PagedIterable<AutocompleteItem> results = client.autocomplete("very polit", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteOneTermWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern", "morel", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern", "morel", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true);

        PagedIterable<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteTwoTermsWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.TWO_TERMS)
            .setUseFuzzyMatching(true);

        PagedIterable<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithFilterAndFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("modern", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("modern", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        PagedIterable<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    /**
     * Compare the autocomplete results with the expected strings
     *
     * @param results
     * @param expectedText
     * @param expectedQueryPlusText
     */
    private void validateResults(PagedIterable<AutocompleteItem> results, List<String> expectedText, List<String> expectedQueryPlusText) {
        int index = 0;
        Iterator<AutocompleteItem> it = results.stream().iterator();
        while (it.hasNext()) {
            AutocompleteItem item = it.next();
            Assert.assertEquals(expectedText.get(index), item.getText());
            Assert.assertEquals(expectedQueryPlusText.get(index), item.getQueryPlusText());
            index++;
        }
    }
}
