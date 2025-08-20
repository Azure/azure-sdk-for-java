// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.AutocompleteItem;
import com.azure.search.documents.models.AutocompleteMode;
import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.documents.util.AutocompletePagedFlux;
import com.azure.search.documents.util.AutocompletePagedIterable;
import com.azure.search.documents.util.AutocompletePagedResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class AutocompleteTests extends SearchTestBase {
    private static final String INDEX_NAME = "azsearch-autocomplete-shared-instance";

    private static SearchIndexClient searchIndexClient;
    private SearchClient client;
    private SearchAsyncClient asyncClient;

    @BeforeAll
    public static void setupClass() {
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, HOTELS_DATA_JSON);
    }

    @Override
    protected void beforeTest() {
        client = getSearchClientBuilder(INDEX_NAME, true).buildClient();
        asyncClient = getSearchClientBuilder(INDEX_NAME, false).buildAsyncClient();
    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(INDEX_NAME);
        }
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterNameSync() {
        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results
            = client.autocomplete("very po", "Invalid suggester", params, Context.NONE);

        HttpResponseException ex
            = assertThrows(HttpResponseException.class, () -> results.iterableByPage().iterator().next());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterNameAsync() {
        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        StepVerifier.create(asyncClient.autocomplete("very po", "Invalid suggester", params, Context.NONE).byPage())
            .thenRequest(1)
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void canAutocompleteDefaultsToOneTermModeSync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        autocompleteAndValidateSync(client.autocomplete("po", "sg"), expected, expected);
    }

    @Test
    public void canAutocompleteDefaultsToOneTermModeAsync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg"), expected, expected);
    }

    @Test
    public void canAutocompleteOneTermWithContextSync() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        autocompleteAndValidateSync(client.autocomplete("looking for very po", "sg", params, Context.NONE),
            expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteOneTermWithContextAsync() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        autocompleteAndValidateAsync(asyncClient.autocomplete("looking for very po", "sg", params), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggesterSync() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setSearchFields("HotelName");

        Iterator<AutocompletePagedResponse> results
            = client.autocomplete("luxu", "sg", params, Context.NONE).iterableByPage().iterator();

        // One page, with 0 items
        assertEquals(0, results.next().getValue().size());
        assertFalse(results.hasNext());
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggesterAsync() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);
        params.setSearchFields("HotelName");

        StepVerifier.create(asyncClient.autocomplete("luxu", "sg", params).byPage())
            .assertNext(page -> assertEquals(0, page.getValue().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefaultSync() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        Iterator<AutocompletePagedResponse> results
            = client.autocomplete("pi", "sg", params, Context.NONE).iterableByPage().iterator();

        // One page, with 0 items
        assertEquals(0, results.next().getValue().size());
        assertFalse(results.hasNext());
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefaultAsync() {
        AutocompleteOptions params = new AutocompleteOptions();
        params.setAutocompleteMode(AutocompleteMode.ONE_TERM);

        StepVerifier.create(asyncClient.autocomplete("pi", "sg", params).byPage())
            .assertNext(page -> assertEquals(0, page.getValue().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteOneTermSync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        autocompleteAndValidateSync(client.autocomplete("po", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void canAutocompleteOneTermAsync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg", params), expected, expected);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocumentsSync() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setUseFuzzyMatching(false);

        autocompleteAndValidateSync(client.autocomplete("very po", "sg", params, Context.NONE), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocumentsAsync() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setUseFuzzyMatching(false);

        autocompleteAndValidateAsync(asyncClient.autocomplete("very po", "sg", params), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformedSync() {
        PagedIterableBase<AutocompleteItem, AutocompletePagedResponse> results = client.autocomplete("very po", "");

        HttpResponseException ex
            = assertThrows(HttpResponseException.class, () -> results.iterableByPage().iterator().next());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformedAsync() {
        StepVerifier.create(asyncClient.autocomplete("very po", "")).thenRequest(1).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void canAutocompleteTwoTermsSync() {
        List<String> expected
            = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        autocompleteAndValidateSync(client.autocomplete("po", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void canAutocompleteTwoTermsAsync() {
        List<String> expected
            = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteCanUseHitHighlightingSync() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        autocompleteAndValidateSync(client.autocomplete("po", "sg", params, Context.NONE), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteCanUseHitHighlightingAsync() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
            .setHighlightPreTag("<b>")
            .setHighlightPostTag("</b>");

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg", params), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFieldsSync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName", "Description");

        autocompleteAndValidateSync(client.autocomplete("mod", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFieldsAsync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName", "Description");

        autocompleteAndValidateAsync(asyncClient.autocomplete("mod", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteWithSelectedFieldsSync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName")
            .setFilter("HotelId eq '7'");

        autocompleteAndValidateSync(client.autocomplete("mod", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteWithSelectedFieldsAsync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName")
            .setFilter("HotelId eq '7'");

        autocompleteAndValidateAsync(asyncClient.autocomplete("mod", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteTopTrimsResultsSync() {
        List<String> expected = Arrays.asList("point", "police");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setTop(2);

        autocompleteAndValidateSync(client.autocomplete("po", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteTopTrimsResultsAsync() {
        List<String> expected = Arrays.asList("point", "police");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setTop(2);

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterSync() {
        List<String> expected = Collections.singletonList("polite");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        autocompleteAndValidateSync(client.autocomplete("po", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAsync() {
        List<String> expected = Collections.singletonList("polite");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        autocompleteAndValidateAsync(asyncClient.autocomplete("po", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzySync() {
        List<String> expected = Collections.singletonList("very polite");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateSync(client.autocomplete("very polit", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzyAsync() {
        List<String> expected = Collections.singletonList("very polite");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocomplete("very polit", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzySync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setUseFuzzyMatching(true);

        autocompleteAndValidateSync(client.autocomplete("mod", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzyAsync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM).setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocomplete("mod", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzySync() {
        List<String> expected = Arrays.asList("model suites", "modern architecture", "modern stay");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.TWO_TERMS).setUseFuzzyMatching(true);

        autocompleteAndValidateSync(client.autocomplete("mod", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzyAsync() {
        List<String> expected = Arrays.asList("model suites", "modern architecture", "modern stay");

        AutocompleteOptions params
            = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.TWO_TERMS).setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocomplete("mod", "sg", params), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzySync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        autocompleteAndValidateSync(client.autocomplete("mod", "sg", params, Context.NONE), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzyAsync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        autocompleteAndValidateAsync(asyncClient.autocomplete("mod", "sg", params), expected, expected);
    }

    private void autocompleteAndValidateSync(AutocompletePagedIterable autocomplete, List<String> expectedTexts,
        List<String> expectedQueryPlusText) {
        validateResults(autocomplete.stream().collect(Collectors.toList()), expectedTexts, expectedQueryPlusText);
    }

    private void autocompleteAndValidateAsync(AutocompletePagedFlux autocomplete, List<String> expectedTexts,
        List<String> expectedQueryPlusText) {
        StepVerifier.create(autocomplete.collectList())
            .assertNext(results -> validateResults(results, expectedTexts, expectedQueryPlusText))
            .verifyComplete();
    }

    /**
     * Compare the autocomplete results with the expected strings
     *
     * @param items results of autocompletion
     * @param expectedText expected text
     * @param expectedQueryPlusText expected query plus text
     */
    private static void validateResults(List<AutocompleteItem> items, List<String> expectedText,
        List<String> expectedQueryPlusText) {
        assertEquals(expectedText.size(), expectedQueryPlusText.size()); // Validate test is set up properly
        assertEquals(expectedText.size(), items.size());

        // Convert passed Lists into ArrayLists so they're mutable.
        ArrayList<String> mutableText = new ArrayList<>(expectedText);
        ArrayList<String> mutableQueryPlusText = new ArrayList<>(expectedQueryPlusText);

        for (AutocompleteItem item : items) {
            // Since these are Strings we can use List.remove and validate it removed a value.
            assertTrue(mutableText.remove(item.getText()));
            assertTrue(mutableQueryPlusText.remove(item.getQueryPlusText()));
        }
    }
}
