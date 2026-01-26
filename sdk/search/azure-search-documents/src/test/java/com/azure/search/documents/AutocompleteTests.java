// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.search.documents.implementation.models.AutocompleteMode;
import com.azure.search.documents.implementation.models.AutocompletePostOptions;
import com.azure.search.documents.implementation.models.AutocompleteResult;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.AutocompleteItem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        AutocompletePostOptions options = new AutocompletePostOptions("very po", "Invalid suggester")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM);

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> client.autocompletePost(options));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterNameAsync() {
        AutocompletePostOptions options = new AutocompletePostOptions("very po", "Invalid suggester")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM);

        StepVerifier.create(autocompleteWithResponseAsync(options)).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void canAutocompleteDefaultsToOneTermModeSync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        autocompleteAndValidateSync(client.autocompletePost(new AutocompletePostOptions("po", "sg")), expected,
            expected);
    }

    @Test
    public void canAutocompleteDefaultsToOneTermModeAsync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(new AutocompletePostOptions("po", "sg")), expected,
            expected);
    }

    @Test
    public void canAutocompleteOneTermWithContextSync() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompletePostOptions options = new AutocompletePostOptions("looking for very po", "sg")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteOneTermWithContextAsync() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompletePostOptions options = new AutocompletePostOptions("looking for very po", "sg")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggesterSync() {
        AutocompletePostOptions params
            = new AutocompletePostOptions("luxu", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName");

        AutocompleteResult results = autocompleteWithResponseSync(params);

        assertEquals(0, results.getResults().size());
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggesterAsync() {
        AutocompletePostOptions params
            = new AutocompletePostOptions("luxu", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName");

        StepVerifier.create(asyncClient.autocompletePost(params))
            .assertNext(results -> assertEquals(0, results.getResults().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefaultSync() {
        AutocompletePostOptions params
            = new AutocompletePostOptions("pi", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM);

        AutocompleteResult results = autocompleteWithResponseSync(params);

        assertEquals(0, results.getResults().size());
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefaultAsync() {
        AutocompletePostOptions options
            = new AutocompletePostOptions("pi", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM);

        StepVerifier.create(asyncClient.autocompletePost(options))
            .assertNext(results -> assertEquals(0, results.getResults().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteOneTermSync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void canAutocompleteOneTermAsync() {
        List<String> expected = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocumentsSync() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompletePostOptions options
            = new AutocompletePostOptions("very po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(false);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocumentsAsync() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText
            = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompletePostOptions options
            = new AutocompletePostOptions("very po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(false);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformedSync() {
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.autocompletePost(new AutocompletePostOptions("very po", "")));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformedAsync() {
        StepVerifier.create(asyncClient.autocompletePost(new AutocompletePostOptions("very po", "")))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            });
    }

    @Test
    public void canAutocompleteTwoTermsSync() {
        List<String> expected
            = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void canAutocompleteTwoTermsAsync() {
        List<String> expected
            = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteCanUseHitHighlightingSync() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
                .setHighlightPreTag("<b>")
                .setHighlightPostTag("</b>");

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteCanUseHitHighlightingAsync() {
        List<String> expectedText = Arrays.asList("pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("<b>pool</b>", "<b>popular</b>");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setFilter("HotelName eq 'EconoStay' or HotelName eq 'Fancy Stay'")
                .setHighlightPreTag("<b>")
                .setHighlightPostTag("</b>");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expectedText, expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFieldsSync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName", "Description");

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFieldsAsync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName", "Description");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithSelectedFieldsSync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName")
                .setFilter("HotelId eq '7'");

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithSelectedFieldsAsync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setSearchFields("HotelName")
                .setFilter("HotelId eq '7'");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteTopTrimsResultsSync() {
        List<String> expected = Arrays.asList("point", "police");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM).setTop(2);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteTopTrimsResultsAsync() {
        List<String> expected = Arrays.asList("point", "police");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM).setTop(2);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterSync() {
        List<String> expected = Collections.singletonList("polite");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setFilter("search.in(HotelId, '6,7')");

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAsync() {
        List<String> expected = Collections.singletonList("polite");

        AutocompletePostOptions options
            = new AutocompletePostOptions("po", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setFilter("search.in(HotelId, '6,7')");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzySync() {
        List<String> expected = Collections.singletonList("very polite");

        AutocompletePostOptions options = new AutocompletePostOptions("very polit", "sg")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzyAsync() {
        List<String> expected = Collections.singletonList("very polite");

        AutocompletePostOptions options = new AutocompletePostOptions("very polit", "sg")
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzySync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzyAsync() {
        List<String> expected = Arrays.asList("model", "modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzySync() {
        List<String> expected = Arrays.asList("model suites", "modern architecture", "modern stay");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.TWO_TERMS)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzyAsync() {
        List<String> expected = Arrays.asList("model suites", "modern architecture", "modern stay");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.TWO_TERMS)
                .setUseFuzzyMatching(true);

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzySync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(true)
                .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        autocompleteAndValidateSync(autocompleteWithResponseSync(options), expected, expected);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzyAsync() {
        List<String> expected = Collections.singletonList("modern");

        AutocompletePostOptions options
            = new AutocompletePostOptions("mod", "sg").setAutocompleteMode(AutocompleteMode.ONE_TERM)
                .setUseFuzzyMatching(true)
                .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        autocompleteAndValidateAsync(asyncClient.autocompletePost(options), expected, expected);
    }

    private AutocompleteResult autocompleteWithResponseSync(AutocompletePostOptions options) {
        return client
            .autocompletePostWithResponse(BinaryData.fromObject(options), new RequestOptions().setContext(Context.NONE))
            .getValue()
            .toObject(AutocompleteResult.class);
    }

    private Mono<AutocompleteResult> autocompleteWithResponseAsync(AutocompletePostOptions options) {
        return asyncClient
            .autocompletePostWithResponse(BinaryData.fromObject(options), new RequestOptions().setContext(Context.NONE))
            .map(response -> response.getValue().toObject(AutocompleteResult.class));
    }

    private static void autocompleteAndValidateSync(AutocompleteResult autocomplete, List<String> expectedTexts,
        List<String> expectedQueryPlusText) {
        validateResults(autocomplete.getResults(), expectedTexts, expectedQueryPlusText);
    }

    private static void autocompleteAndValidateAsync(Mono<AutocompleteResult> autocomplete, List<String> expectedTexts,
        List<String> expectedQueryPlusText) {
        StepVerifier.create(autocomplete)
            .assertNext(results -> validateResults(results.getResults(), expectedTexts, expectedQueryPlusText))
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
