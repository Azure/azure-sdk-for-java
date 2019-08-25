// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.customization.RangeFacetResult;
import com.azure.search.data.customization.ValueFacetResult;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.QueryType;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.data.generated.models.QueryType.SIMPLE;
import static com.azure.search.data.generated.models.SearchMode.ALL;

public class SearchAsyncTests extends SearchTestBase {

    private SearchIndexAsyncClient client;
    protected static final String LARGE_HOTELS_DATA_JSON = "LargeHotelDataArray.json";

    @Override
    protected void indexDocuments(List<IndexAction> indexActions) {
        client.index(new IndexBatch().actions(indexActions)).block();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }

    @Override
    protected void setIndexName(String indexName) {
        client.setIndexName(indexName);
    }

    @Test
    public void canContinueSearch() throws Exception {
        // upload large documents batch
        uploadDocuments(LARGE_HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(firstPage.value().size(), 50);
                Assert.assertNotEquals(firstPage.nextLink(), null);
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(nextPage.value().size(), 50);
                Assert.assertEquals(nextPage.nextLink(), null);
            }).verifyComplete();
    }

    @Override
    public void canSearchWithSelectedFields() {
        // Ask JUST for the following two fields
        SearchParameters sp = new SearchParameters();
        sp.searchFields(new LinkedList<>(Arrays.asList("HotelName", "Category")));
        sp.select(new LinkedList<>(Arrays.asList("HotelName", "Rating", "Address/City", "Rooms/Type")));

        PagedFlux<SearchResult> results = client.search("fancy luxury secret", sp, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<SearchResult> documents = results.log().collectList().block();

        // expecting to get 2 documents back
        Assert.assertEquals(2, documents.size());

        HashMap<String, Object> expectedHotel1 = new HashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Arrays.asList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        HashMap<String, Object> expectedHotel2 = new HashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 3);
        HashMap<String, Object> address = new LinkedHashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        HashMap<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("Type", "Budget Room");
        HashMap<String, Object> rooms2 = new LinkedHashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        Assert.assertEquals(expectedHotel1, dropUnnecessaryFields(documents.get(0).additionalProperties()));
        Assert.assertEquals(expectedHotel2, dropUnnecessaryFields(documents.get(1).additionalProperties()));
    }

    @Override
    public void canUseTopAndSkipForClientSidePaging() {
        List<String> orderBy = Stream.of("HotelId").collect(Collectors.toList());
        SearchParameters parameters = new SearchParameters().top(3).skip(0).orderBy(orderBy);

        PagedFlux<SearchResult> results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "100"));

        parameters = parameters.skip(3);
        results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("11", "12", "13"));
    }

    @Override
    public void canFilterNonNullableType() throws Exception {
        List<Map<String, Object>> expectedDocsList = prepareDataForNonNullableTest();
        SearchParameters searchParameters = new SearchParameters()
            .filter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<SearchResult> searchResultsList = results.log().collectList().block();
        Assert.assertEquals(2, searchResultsList.size());

        List<Map<String, Object>> actualResults = new ArrayList<>();
        searchResultsList.forEach(searchResult -> actualResults.add(dropUnnecessaryFields(searchResult.additionalProperties())));

        Assert.assertEquals(expectedDocsList, actualResults);
    }

    @Override
    public void searchWithoutOrderBySortsByScore() {
        PagedFlux<SearchResult> results = client.search("*", new SearchParameters().filter("Rating lt 4"), new SearchRequestOptions());
        List<SearchResult> searchResultsList = results.log().collectList().block();

        Assert.assertTrue(searchResultsList.size() >= 2);
        SearchResult firstResult = searchResultsList.get(0);
        SearchResult secondResult = searchResultsList.get(1);
        Assert.assertTrue(firstResult.score() <= secondResult.score());
    }

    @Override
    public void orderByProgressivelyBreaksTies() {
        List<String> orderByValues = new ArrayList<>();
        orderByValues.add("Rating desc");
        orderByValues.add("LastRenovationDate asc");

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters().orderBy(orderByValues), new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<String> actualResults = results.log().map(res -> getSearchResultId(res, "HotelId")).collectList().block();

        Assert.assertArrayEquals(actualResults.toArray(), expectedResults);
    }

    @Override
    public void canFilter() {
        SearchParameters searchParameters = new SearchParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> searchResultsList = results.log().map(res -> res.additionalProperties()).collectList().block();
        Assert.assertNotNull(searchResultsList);
        Assert.assertEquals(2, searchResultsList.size());

        List hotelIds = searchResultsList.stream().map(r -> r.get("HotelId")).collect(Collectors.toList());
        Assert.assertTrue(Arrays.asList("1", "5").containsAll(hotelIds));
    }

    @Override
    public void canSearchWithRangeFacets() {
        PagedFlux<SearchResult> results = client.search("*", getSearchParametersForRangeFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainKeys(res.items());
                Map<String, List<FacetResult>> facets = ((SearchPagedResponse) res).facets();
                Assert.assertNotNull(facets);
                List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(facets, "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
            }).verifyComplete();
    }

    @Override
    public void canSearchWithValueFacets() {
        PagedFlux<SearchResult> results = client.search("*", getSearchParametersForValueFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainKeys(res.items());
                Map<String, List<FacetResult>> facets = ((SearchPagedResponse) res).facets();
                Assert.assertNotNull(facets);

                assertValueFacetsEqual(getValueFacetsForField(facets, "Rating", 2),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(1L, 5),
                        new ValueFacetResult(4L, 4))));

                assertValueFacetsEqual(getValueFacetsForField(facets, "SmokingAllowed", 2),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(4L, false),
                        new ValueFacetResult(2L, true))));

                assertValueFacetsEqual(getValueFacetsForField(facets, "Category", 3),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(5L, "Budget"),
                        new ValueFacetResult(1L, "Boutique"),
                        new ValueFacetResult(1L, "Luxury"))));

                assertValueFacetsEqual(getValueFacetsForField(facets, "LastRenovationDate", 6),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(1L, "1970-01-01T00:00:00Z"),
                        new ValueFacetResult(1L, "1982-01-01T00:00:00Z"),
                        new ValueFacetResult(2L, "1995-01-01T00:00:00Z"),
                        new ValueFacetResult(1L, "1999-01-01T00:00:00Z"),
                        new ValueFacetResult(1L, "2010-01-01T00:00:00Z"),
                        new ValueFacetResult(1L, "2012-01-01T00:00:00Z"))));

                assertValueFacetsEqual(getValueFacetsForField(facets, "Rooms/BaseRate", 4),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(1L, 2.44),
                        new ValueFacetResult(1L, 7.69),
                        new ValueFacetResult(1L, 8.09),
                        new ValueFacetResult(1L, 9.69))));

                assertValueFacetsEqual(getValueFacetsForField(facets, "Tags", 10),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(1L, "24-hour front desk service"),
                        new ValueFacetResult(1L, "air conditioning"),
                        new ValueFacetResult(4L, "budget"),
                        new ValueFacetResult(1L, "coffee in lobby"),
                        new ValueFacetResult(2L, "concierge"),
                        new ValueFacetResult(1L, "motel"),
                        new ValueFacetResult(2L, "pool"),
                        new ValueFacetResult(1L, "restaurant"),
                        new ValueFacetResult(1L, "view"),
                        new ValueFacetResult(4L, "wifi"))));

            }).verifyComplete();
    }

    @Override
    public void canSearchWithLuceneSyntax() {
        HashMap<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchParameters searchParameters = new SearchParameters().queryType(QueryType.FULL).select(Arrays.asList("HotelName", "Rating"));
        PagedFlux<SearchResult> results = client.search("HotelName:roch~", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                Assert.assertEquals(1, res.items().size());
                Assert.assertEquals(expectedResult, dropUnnecessaryFields(res.items().get(0).additionalProperties()));
            }).verifyComplete();
    }

    @Override
    public void canSearchDynamicDocuments() {
        List<PagedResponse<SearchResult>> results = client.search("*", new SearchParameters(), new SearchRequestOptions()).byPage().log().collectList().block();
        Assert.assertNotNull(results);

        List<Map<String, Object>> actualResults = new ArrayList<>();
        results.forEach(res -> assertResponse((SearchPagedResponse) res, actualResults));

        Assert.assertEquals(hotels.size(), actualResults.size());
        Assert.assertTrue(compareResults(actualResults.stream().map(SearchTestBase::dropUnnecessaryFields).collect(Collectors.toList()), hotels));
    }

    @Override
    public void canSearchWithSearchModeAll() {
        Flux<SearchResult> response = client.search("Cheapest hotel", new SearchParameters().queryType(SIMPLE).searchMode(ALL), new SearchRequestOptions()).log();
        StepVerifier.create(response).assertNext(res -> Assert.assertEquals("2", getSearchResultId(res, "HotelId"))).verifyComplete();
    }

    @Override
    public void defaultSearchModeIsAny() {
        Flux<SearchResult> response = client.search("Cheapest hotel", new SearchParameters(), new SearchRequestOptions()).log();

        StepVerifier.create(response)
            .assertNext(res -> Assert.assertEquals("2", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("10", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("3", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("4", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("5", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("1", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("9", getSearchResultId(res, "HotelId")))
            .verifyComplete();
    }

    @Override
    public void canGetResultCountInSearch() {
        Flux<PagedResponse<SearchResult>> results = client.search("*", new SearchParameters().includeTotalResultCount(true), new SearchRequestOptions()).byPage();
        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(hotels.size(), ((SearchPagedResponse) res).count().intValue()))
            .verifyComplete();
    }

    @Override
    public void canSearchWithRegex() {
        SearchParameters searchParameters = new SearchParameters()
            .queryType(QueryType.FULL)
            .select(Arrays.asList("HotelName", "Rating"));

        PagedFlux<SearchResult> results = client
            .search("HotelName:/.*oach.*\\/?/", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<SearchResult> searchResultsList = results.log().collectList().block();
        Assert.assertEquals(1, searchResultsList.size());

        Map<String, Object> result = searchResultsList.get(0).additionalProperties();

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        Assert.assertEquals(dropUnnecessaryFields(result), expectedHotel);
    }

    @Override
    public void canSearchWithEscapedSpecialCharsInRegex() {
        SearchParameters searchParameters = new SearchParameters().queryType(QueryType.FULL);

        PagedFlux<SearchResult> results = client
            .search(
                "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchParameters,
                new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<SearchResult> searchResultsList = results.log().collectList().block();
        Assert.assertEquals(0, searchResultsList.size());
    }

    private void assertResponse(SearchPagedResponse response, List<Map<String, Object>> actualResults) {
        Assert.assertNull(response.count());
        Assert.assertNull(response.coverage());
        Assert.assertNull(response.facets());
        Assert.assertNotNull(response.items());

        response.items().forEach(item -> {
            Assert.assertEquals(1, item.score(), 0);
            Assert.assertNull(item.highlights());
            actualResults.add(dropUnnecessaryFields(item.additionalProperties()));
        });
    }

    @Override
    protected void search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        PagedFlux<SearchResult> results = client.search(searchText, searchParameters, searchRequestOptions);
        results.log().blockFirst();
    }

    private void assertKeySequenceEqual(PagedFlux<SearchResult> results, List<String> expectedKeys) {
        Assert.assertNotNull(results);

        List<String> actualKeys = results.log()
            .filter(doc -> doc.additionalProperties().containsKey("HotelId"))
            .map(doc -> getSearchResultId(doc, "HotelId"))
            .collectList()
            .block();

        Assert.assertEquals(expectedKeys, actualKeys);
    }
}
