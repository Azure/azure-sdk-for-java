// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedFluxBase;
import com.azure.search.models.AutocompleteItem;
import com.azure.search.models.AutocompleteMode;
import com.azure.search.models.AutocompleteOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.azure.search.SearchTestBase.HOTELS_INDEX_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutocompleteAsyncTests extends AutocompleteTestBase {

    private SearchIndexAsyncClient client;

    @Override
    protected void initializeClient() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
    }

    @Test
    public void canAutocompleteThrowsWhenGivenBadSuggesterName() {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM);

        assertHttpResponseExceptionAsync(
            client.autocomplete("very po", "Invalid suggester", params, generateRequestOptions()),
            HttpResponseStatus.BAD_REQUEST,
            "The specified suggester name 'Invalid suggester' does not exist in this index definition"
                + ".\\r\\nParameter name: name"
        );
    }

    @Test
    public void canAutocompleteDefaultsToOneTermMode() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        validateResults(client.autocomplete("po", "sg"), expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteExcludesFieldsNotInSuggester() {
        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName");

        StepVerifier.create(client.autocomplete("luxu", "sg", params, generateRequestOptions()).byPage())
            .assertNext(pageResult -> assertEquals(0, pageResult.getItems().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteFuzzyIsOffByDefault() {
        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        StepVerifier.create(client.autocomplete("pi", "sg", params, generateRequestOptions()).byPage())
            .assertNext(pageResult -> assertEquals(0, pageResult.getItems().size()))
            .verifyComplete();
    }

    @Test
    public void canAutocompleteOneTerm() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police", "polite", "pool", "popular");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.ONE_TERM);

        validateResults(client.autocomplete("po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteOneTermWithContext() {
        List<String> expectedText = Arrays.asList("very police", "very polite", "very popular");
        List<String> expectedQueryPlusText = Arrays.asList("looking for very police", "looking for very polite", "looking for very popular");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT);

        validateResults(client.autocomplete("looking for very po", "sg", params, generateRequestOptions()),
            expectedText, expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteStaticallyTypedDocuments() {
        List<String> expectedText = Arrays.asList("point", "police", "polite", "pool", "popular");
        List<String> expectedQueryPlusText = Arrays.asList("very point", "very police", "very polite", "very pool", "very popular");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(false);

        validateResults(client.autocomplete("very po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void canAutocompleteThrowsWhenRequestIsMalformed() {
        assertHttpResponseExceptionAsync(
            client.autocomplete("very po", ""),
            HttpResponseStatus.BAD_REQUEST,
            "Cannot find fields enabled for suggestions. Please provide a value for 'suggesterName' in"
                + " the query.\\r\\nParameter name: suggestions"
        );
    }

    @Test
    public void canAutocompleteTwoTerms() {
        List<String> expectedText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");
        List<String> expectedQueryPlusText = Arrays.asList("point motel", "police station", "polite staff", "pool a", "popular hotel");

        AutocompleteOptions params = new AutocompleteOptions().setAutocompleteMode(AutocompleteMode.TWO_TERMS);

        validateResults(client.autocomplete("po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
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

        validateResults(client.autocomplete("po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithMultipleSelectedFields() {
        List<String> expectedText = Arrays.asList("model", "modern");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName", "Description");

        validateResults(client.autocomplete("mod", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithSelectedFields() {
        List<String> expectedText = Collections.singletonList("modern");
        List<String> expectedQueryPlusText = Collections.singletonList("modern");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setSearchFields("HotelName")
            .setFilter("HotelId eq '7'");

        validateResults(client.autocomplete("mod", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteTopTrimsResults() {
        List<String> expectedText = Arrays.asList("point", "police");
        List<String> expectedQueryPlusText = Arrays.asList("point", "police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setTop(2);

        validateResults(client.autocomplete("po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithFilter() {
        List<String> expectedText = Collections.singletonList("polite");
        List<String> expectedQueryPlusText = Collections.singletonList("polite");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setFilter("search.in(HotelId, '6,7')");

        validateResults(client.autocomplete("po", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteOneTermWithContextWithFuzzy() {
        List<String> expectedText = Arrays.asList("very polite", "very police");
        List<String> expectedQueryPlusText = Arrays.asList("very polite", "very police");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM_WITH_CONTEXT)
            .setUseFuzzyMatching(true);

        validateResults(client.autocomplete("very polit", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteOneTermWithFuzzy() {
        List<String> expectedText = Arrays.asList("model", "modern", "morel", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model", "modern", "morel", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true);

        validateResults(client.autocomplete("mod", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteTwoTermsWithFuzzy() {
        List<String> expectedText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("model suites", "modern architecture", "modern stay", "morel coverings", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.TWO_TERMS)
            .setUseFuzzyMatching(true);

        validateResults(client.autocomplete("mod", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    @Test
    public void testAutocompleteWithFilterAndFuzzy() {
        List<String> expectedText = Arrays.asList("modern", "motel");
        List<String> expectedQueryPlusText = Arrays.asList("modern", "motel");

        AutocompleteOptions params = new AutocompleteOptions()
            .setAutocompleteMode(AutocompleteMode.ONE_TERM)
            .setUseFuzzyMatching(true)
            .setFilter("HotelId ne '6' and (HotelName eq 'Modern Stay' or Tags/any(t : t eq 'budget'))");

        validateResults(client.autocomplete("mod", "sg", params, generateRequestOptions()), expectedText,
            expectedQueryPlusText);
    }

    /**
     * Validate the text and query plus text results
     *
     * @param results results
     * @param expectedText expected text
     * @param expectedQueryPlusText expected query plus text
     */
    private void validateResults(PagedFluxBase<AutocompleteItem, AutocompletePagedResponse> results,
        List<String> expectedText, List<String> expectedQueryPlusText) {
        StepVerifier.create(results.byPage())
            .assertNext(pageResult -> {
                for (int i = 0; i < pageResult.getItems().size(); i++) {
                    assertEquals(expectedText.get(i), pageResult.getItems().get(i).getText());
                    assertEquals(expectedQueryPlusText.get(i), pageResult.getItems().get(i).getQueryPlusText());
                }
            })
            .verifyComplete();
    }
}
