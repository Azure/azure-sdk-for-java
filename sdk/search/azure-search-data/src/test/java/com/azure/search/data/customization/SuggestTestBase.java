// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SuggestPagedResponse;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.generated.models.SuggestResult;
import com.azure.search.test.environment.models.Hotel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class SuggestTestBase extends SearchIndexClientTestBase {
    protected JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
    static final String BOOKS_INDEX_JSON = "BooksIndexData.json";

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    protected abstract void initializeClient();

    protected void verifyFuzzySuggest(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(5, suggestResultPagedResponse.value().size());
    }

    protected void verifyHitHighlightingSuggest(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(1, suggestResultPagedResponse.value().size());
        Assert.assertTrue(suggestResultPagedResponse.value().get(0).text().startsWith("Best <b>hotel</b> in town"));
    }

    protected void verifyFieldsExcludesFieldsSuggest(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(0, suggestResultPagedResponse.value().size());
    }

    protected void verifyDynamicDocumentSuggest(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(2, suggestResultPagedResponse.value().size());
        Hotel hotel = suggestResultPagedResponse.value().get(0).additionalProperties().as(Hotel.class);
        Assert.assertEquals("10", hotel.hotelId());
    }

    protected void verifyCanSuggestStaticallyTypedDocuments(PagedResponse<SuggestResult> suggestResultPagedResponse, List<Map<String, Object>> expectedHotels) {
        //sanity
        Assert.assertNotNull(suggestResultPagedResponse);
        List<Document> docs = suggestResultPagedResponse.value().stream().map(h -> h.additionalProperties()).collect(Collectors.toList());
        List<SuggestResult> hotelsList = suggestResultPagedResponse.value();
        List<Hotel> expectedHotelsList = expectedHotels.stream().map(hotel ->
            jsonApi.convertObjectToType(hotel, Hotel.class))
            .filter(h -> h.hotelId().equals("10") || h.hotelId().equals("8"))
            .sorted(Comparator.comparing(Hotel::hotelId)).collect(Collectors.toList());

        //assert
        //verify fields
        Assert.assertEquals(2, docs.size());
        Assert.assertEquals(hotelsList.stream().map(h -> h.text()).collect(Collectors.toList()),
            expectedHotelsList.stream().map(h -> h.description()).collect(Collectors.toList()));
    }

    protected void verifyFuzzyIsOffByDefault(PagedResponse<SuggestResult> suggestResultPagedResponse) {

        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(0, suggestResultPagedResponse.value().size());
    }

    protected void verifySuggestThrowsWhenGivenBadSuggesterName(Throwable error) {
        assertEquals(HttpResponseException.class, error.getClass());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
        assertTrue(error.getMessage().contains("The specified suggester name 'Suggester does not exist' does not exist in this index definition."));
    }

    protected void verifySuggestThrowsWhenRequestIsMalformed(Throwable error) {
        assertEquals(HttpResponseException.class, error.getClass());
        assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
        assertTrue(error.getMessage().contains("Invalid expression: Syntax error at position 7 in 'This is not a valid orderby.'"));
    }

    protected void verifyMinimumCoverage(PagedResponse<SuggestResult> suggestResultPagedResponse) {

        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(new Double(100), ((SuggestPagedResponse) suggestResultPagedResponse).coverage());
    }

    protected void verifyTopDocumentSuggest(PagedResponse<SuggestResult> suggestResultPagedResponse) {
        Assert.assertNotNull(suggestResultPagedResponse);
        Assert.assertEquals(3, suggestResultPagedResponse.value().size());
        List<String> resultIds = suggestResultPagedResponse
            .value()
            .stream()
            .map(hotel -> hotel.additionalProperties().as(Hotel.class).hotelId())
            .collect(Collectors.toList());

        Assert.assertEquals(Arrays.asList("1", "10", "2"), resultIds);
    }

    @Test
    public abstract void canSuggestDynamicDocuments() throws Exception;

    @Test
    public abstract void searchFieldsExcludesFieldsFromSuggest() throws Exception;

    @Test
    public abstract void canUseSuggestHitHighlighting() throws Exception;

    @Test
    public abstract void canGetFuzzySuggestions() throws Exception;

    @Test
    public abstract void canSuggestStaticallyTypedDocuments();

    @Test
    public abstract void canSuggestWithDateTimeInStaticModel() throws Exception;

    @Test
    public abstract void fuzzyIsOffByDefault();

    @Test
    public abstract void suggestThrowsWhenGivenBadSuggesterName() throws Exception;

    @Test
    public abstract void suggestThrowsWhenRequestIsMalformed() throws Exception;

    @Test
    public abstract void testCanSuggestWithMinimumCoverage() throws Exception;

    @Test
    public abstract void testTopTrimsResults();

    @Test
    public abstract void testCanFilter();

    @Test
    public abstract void testOrderByProgressivelyBreaksTies();
}
