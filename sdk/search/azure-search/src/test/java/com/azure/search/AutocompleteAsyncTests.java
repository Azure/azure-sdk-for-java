// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteParameters;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AutocompleteAsyncTests extends AutocompleteTestBase {

    private SearchIndexAsyncClient client;

    @Override
    protected void initializeClient() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
    }

    @Override
    public void canAutocompleteThrowsWhenGivenBadSuggesterName() {
        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "Invalid suggester", params, null);
        StepVerifier
                .create(results)
                .verifyErrorSatisfies(error -> {
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                    assertTrue(error.getMessage().contains("The specified suggester name 'Invalid suggester' does not exist in this index definition.\\r\\nParameter name: name"));
                });
    }

    @Override
    public void canAutocompleteDefaultsToOneTermMode() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg");
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void canAutocompleteExcludesFieldsNotInSuggester() {
        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setSearchFields(Arrays.asList("HotelName"));

        PagedFlux<AutocompleteItem> results = client.autocomplete("luxu", "sg", params, null);
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(0, pageResult.getItems().size());
        }).verifyComplete();
    }

    @Override
    public void canAutocompleteFuzzyIsOffByDefault() {
        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("pi", "sg", params, null);
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            Assert.assertEquals(0, pageResult.getItems().size());
        }).verifyComplete();
    }

    @Override
    public void canAutocompleteOneTerm() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void canAutocompleteOneTermWithContext() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        PagedFlux<AutocompleteItem> results = client.autocomplete("looking for very po", "sg", params, null);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void canAutocompleteStaticallyTypedDocuments() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setUseFuzzyMatching(false);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "sg", params, null);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void canAutocompleteThrowsWhenRequestIsMalformed() {
        PagedFlux<AutocompleteItem> results = client.autocomplete("very po", "");
        StepVerifier
                .create(results)
                .verifyErrorSatisfies(error -> {
                    assertEquals(HttpResponseException.class, error.getClass());
                    assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                    assertTrue(error.getMessage().contains("Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in the query.\\r\\nParameter name: suggestions"));
                });
    }

    @Override
    public void canAutocompleteTwoTerms() {
        List<String> expectedText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");
        List<String> expectedQueryPlusText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteParameters params = new AutocompleteParameters();
        params.setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteCanUseHitHighlighting() throws Exception {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithMultipleSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern");
        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields(Arrays.asList("HotelName", "Description"));

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithSelectedFields() throws Exception {
        List<String> expectedText = Arrays.asList("modern");
        List<String> expectedQueryPlusText = Arrays.asList("modern");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields(Arrays.asList("HotelName"))
            .setFilter("HotelId eq '7'");

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteTopTrimsResults() throws Exception {
        List<String> expectedText = Arrays.asList("point", "police");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setTop(2);

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithFilter() throws Exception {
        List<String> expectedText = Arrays.asList("polite");
        List<String> expectedQueryPlusText = Arrays.asList("polite");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        PagedFlux<AutocompleteItem> results = client.autocomplete("po", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteOneTermWithContextWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("very polite", "very police");
        List<String> expectedQueryPlusText = Arrays.asList("very polite", "very police");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("very polit", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteOneTermWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model", "modern", "morel", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern", "morel", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteTwoTermsWithFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.TWO_TERMS)
            .setUseFuzzyMatching(true);

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    @Override
    public void testAutocompleteWithFilterAndFuzzy() throws Exception {
        List<String> expectedText = Arrays.asList("modern", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("modern", "motel");

        AutocompleteParameters params = new AutocompleteParameters()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        PagedFlux<AutocompleteItem> results = client.autocomplete("mod", "sg", params, null);

        Assert.assertNotNull(results);
        validateResults(results, expectedText, expectedQueryPlusText);
    }

    /**
     * Validate the text and query plus text results
     * @param results
     * @param expectedText
     * @param expectedQueryPlusText
     */
    private void validateResults(PagedFlux<AutocompleteItem> results, List<String> expectedText, List<String> expectedQueryPlusText) {
        StepVerifier.create(results.byPage()).assertNext(pageResult -> {
            List<String> textResults = new ArrayList<>();
            List<String> queryPlusTextResults = new ArrayList<>();
            pageResult.getItems()
                    .forEach(res -> {
                        textResults.add(res.getText());
                        queryPlusTextResults.add(res.getQueryPlusText());
                    });
            Assert.assertEquals(expectedText, textResults);
            Assert.assertEquals(expectedQueryPlusText, queryPlusTextResults);
        }).verifyComplete();
    }
}
