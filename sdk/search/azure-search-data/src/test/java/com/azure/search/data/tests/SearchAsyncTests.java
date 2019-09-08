// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.tests;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.RangeFacetResult;
import com.azure.search.data.customization.ValueFacetResult;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.QueryType;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.data.models.Bucket;
import com.azure.search.data.models.Hotel;
import com.azure.search.data.models.NonNullableModel;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    public void canContinueSearch() throws InterruptedException {
        documents = uploadHotels(100);

        SearchParameters searchParameters = new SearchParameters().select(Arrays.asList("HotelId")).orderBy(Arrays.asList("HotelId asc"));
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        List<String> expectedId = documents.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        // Default page size is 50 if the value of top is less than 1000, or not specified
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(50, firstPage.value().size());
                assertEqual(expectedId.subList(0, 50), firstPage.value());
                Assert.assertNotEquals(null, firstPage.nextLink());
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(50, nextPage.value().size());
                assertEqual(expectedId.subList(50, 100), nextPage.value());
                Assert.assertEquals(null, nextPage.nextLink());
            }).verifyComplete();
    }

    @Test
    public void canContinueSearchWithTop() throws InterruptedException {
        // upload large documents batch
        documents = uploadHotels(2000);

        SearchParameters searchParameters = new SearchParameters().top(2000).select(Arrays.asList("HotelId")).orderBy(Arrays.asList("HotelId asc"));
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        List<String> expectedId = documents.stream().map(hotel -> (String) hotel.get("HotelId")).sorted().collect(Collectors.toList());

        // Maximum page size is 1000 if the value of top is grater than 1000.
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(1000, firstPage.value().size());
                assertEqual(expectedId.subList(0, 1000), firstPage.value());
                Assert.assertNotEquals(null, firstPage.nextLink());
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(1000, nextPage.value().size());
                assertEqual(expectedId.subList(1000, 2000), nextPage.value());
                Assert.assertEquals(null, nextPage.nextLink());
            }).verifyComplete();
    }

    protected void assertEqual(List<String> expected, List<SearchResult> actual) {
        Assert.assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.additionalProperties().containsKey("HotelId")).map(item -> (String) item.additionalProperties().get("HotelId")).collect(Collectors.toList());
        Assert.assertEquals(expected, actualKeys);
    }

    @Override
    public void canSearchWithSelectedFields() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

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

        // Ask JUST for the following two fields
        SearchParameters sp = new SearchParameters();
        sp.searchFields(new LinkedList<>(Arrays.asList("HotelName", "Category")));
        sp.select(new LinkedList<>(Arrays.asList("HotelName", "Rating", "Address/City", "Rooms/Type")));

        PagedFlux<SearchResult> results = client.search("fancy luxury secret", sp, new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(res -> {
            // expecting to get 2 documents back
            Assert.assertEquals(2, res.items().size());
            Assert.assertEquals(expectedHotel1, dropUnnecessaryFields(res.items().get(0).additionalProperties()));
            Assert.assertEquals(expectedHotel2, dropUnnecessaryFields(res.items().get(1).additionalProperties()));
        }).verifyComplete();
    }

    @Override
    public void canUseTopAndSkipForClientSidePaging() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        List<String> orderBy = Stream.of("HotelId").collect(Collectors.toList());
        SearchParameters parameters = new SearchParameters().top(3).skip(0).orderBy(orderBy);

        PagedFlux<SearchResult> results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "100"));

        parameters = parameters.skip(3);
        results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("11", "12", "13"));
    }

    @Override
    public void canFilterNonNullableType() throws IOException, InterruptedException {
        List<Map<String, Object>> expectedDocsList = prepareDataForNonNullableTest();
        SearchParameters searchParameters = new SearchParameters()
            .filter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(2, res.items().size());
            List<Map<String, Object>> actualResults = new ArrayList<>();
            res.items().forEach(searchResult -> actualResults.add(dropUnnecessaryFields(searchResult.additionalProperties())));
            Assert.assertEquals(expectedDocsList, actualResults);
        }).verifyComplete();
    }

    @Override
    public void searchWithoutOrderBySortsByScore() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters().filter("Rating lt 4"), new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertTrue(res.items().size() >= 2);
            SearchResult firstResult = res.items().get(0);
            SearchResult secondResult = res.items().get(1);
            Assert.assertTrue(firstResult.score() <= secondResult.score());
        }).verifyComplete();
    }

    @Override
    public void orderByProgressivelyBreaksTies() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        List<String> orderByValues = new ArrayList<>();
        orderByValues.add("Rating desc");
        orderByValues.add("LastRenovationDate asc");

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters().orderBy(orderByValues), new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            List<String> actualResults = res.items().stream().map(doc -> getSearchResultId(doc, "HotelId")).collect(Collectors.toList());
            Assert.assertArrayEquals(actualResults.toArray(), expectedResults);
        }).verifyComplete();
    }

    @Override
    public void canFilter() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(2, res.items().size());
            List<Object> hotels = res.items().stream().map(doc -> doc.additionalProperties().get("HotelId")).collect(Collectors.toList());
            Assert.assertTrue(Arrays.asList("1", "5").containsAll(hotels));
        }).verifyComplete();
    }

    @Override
    public void canSearchWithRangeFacets() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", getSearchParametersForRangeFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainKeys(documents, res.items());
                Map<String, List<FacetResult>> facets = ((SearchPagedResponse) res).facets();
                Assert.assertNotNull(facets);
                List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(facets, "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
            }).verifyComplete();
    }

    @Override
    public void canSearchWithValueFacets() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", getSearchParametersForValueFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainKeys(documents, res.items());
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
    public void canSearchWithLuceneSyntax() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

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
    public void canSearchDynamicDocuments() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> actualResults = new ArrayList<>();
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                SearchPagedResponse response = (SearchPagedResponse) res;
                Assert.assertNull(response.count());
                Assert.assertNull(response.coverage());
                Assert.assertNull(response.facets());
                Assert.assertNotNull(response.items());

                response.items().forEach(item -> {
                    Assert.assertEquals(1, item.score(), 0);
                    Assert.assertNull(item.highlights());
                    actualResults.add(dropUnnecessaryFields(item.additionalProperties()));
                });
            }).verifyComplete();

        Assert.assertEquals(documents.size(), actualResults.size());
        Assert.assertTrue(compareResults(actualResults.stream().map(SearchTestBase::dropUnnecessaryFields).collect(Collectors.toList()), documents));
    }

    @Override
    public void canSearchStaticallyTypedDocuments() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Hotel> actualResults = new ArrayList<>();
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                SearchPagedResponse response = (SearchPagedResponse) res;
                Assert.assertNull(response.count());
                Assert.assertNull(response.coverage());
                Assert.assertNull(response.facets());
                Assert.assertNotNull(response.items());

                response.items().forEach(item -> {
                    Assert.assertEquals(1, item.score(), 0);
                    Assert.assertNull(item.highlights());
                    actualResults.add(item.additionalProperties().as(Hotel.class));
                });
            }).verifyComplete();

        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();
        List<Hotel> hotels = documents.stream().map(hotel -> jsonApi.convertObjectToType(hotel, Hotel.class)).collect(Collectors.toList());

        Assert.assertEquals(hotels.size(), actualResults.size());
        Assert.assertEquals(hotels, actualResults);
    }

    @Override
    public void canRoundTripNonNullableValueTypes() throws InterruptedException, ParseException {
        NonNullableModel doc1 = new NonNullableModel()
            .key("123")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.14)
            .startDate(DATE_FORMAT.parse("2010-06-01T00:00:00Z"))
            .endDate(DATE_FORMAT.parse("2010-06-15T00:00:00Z"))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[] {new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7)});

        NonNullableModel doc2 = new NonNullableModel().key("456").buckets(new Bucket[]{});

        client.setIndexName("non-nullable-index");
        uploadDocuments(Arrays.asList(doc1, doc2));

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(2, res.items().size());
            Assert.assertEquals(doc1, res.items().get(0).additionalProperties().as(NonNullableModel.class));
            Assert.assertEquals(doc2, res.items().get(1).additionalProperties().as(NonNullableModel.class));
        }).verifyComplete();
    }

    @Override
    public void canSearchWithDateInStaticModel() throws IOException, InterruptedException, ParseException {
        // check if deserialization of Date type object is successful
        documents = uploadDocuments(HOTELS_DATA_JSON);
        Date expected = DATE_FORMAT.parse("2010-06-27T00:00:00Z");

        PagedFlux<SearchResult> results = client.search("Fancy", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(1, res.items().size());
            Date actual = res.items().get(0).additionalProperties().as(Hotel.class).lastRenovationDate();
            Assert.assertEquals(expected, actual);
        }).verifyComplete();
    }

    @Override
    public void canSearchWithSearchModeAll() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        Flux<SearchResult> response = client.search("Cheapest hotel", new SearchParameters().queryType(SIMPLE).searchMode(ALL), new SearchRequestOptions()).log();
        StepVerifier.create(response).assertNext(res -> Assert.assertEquals("2", getSearchResultId(res, "HotelId"))).verifyComplete();
    }

    @Override
    public void defaultSearchModeIsAny() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

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
    public void canGetResultCountInSearch() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        Flux<PagedResponse<SearchResult>> results = client.search("*", new SearchParameters().includeTotalResultCount(true), new SearchRequestOptions()).byPage();
        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(documents.size(), ((SearchPagedResponse) res).count().intValue()))
            .verifyComplete();
    }

    @Override
    public void canSearchWithRegex() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        SearchParameters searchParameters = new SearchParameters()
            .queryType(QueryType.FULL)
            .select(Arrays.asList("HotelName", "Rating"));

        PagedFlux<SearchResult> results = client
            .search("HotelName:/.*oach.*\\/?/", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(1, res.items().size());
            Assert.assertEquals(dropUnnecessaryFields(res.items().get(0).additionalProperties()), expectedHotel);
        }).verifyComplete();
    }

    @Override
    public void canSearchWithEscapedSpecialCharsInRegex() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters().queryType(QueryType.FULL);

        PagedFlux<SearchResult> results = client
            .search(
                "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchParameters,
                new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(0, res.items().size());
        }).verifyComplete();
    }

    @Override
    public void searchWithScoringProfileBoostsScore() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .scoringProfile("nearest")
            .scoringParameters(Arrays.asList("myloc-'-122','49'"))
            .filter("Rating eq 5 or Rating eq 1");

        Flux<SearchResult> response = client.search("hotel", searchParameters, new SearchRequestOptions()).log();

        StepVerifier.create(response)
            .assertNext(res -> Assert.assertEquals("2", getSearchResultId(res, "HotelId")))
            .assertNext(res -> Assert.assertEquals("1", getSearchResultId(res, "HotelId")))
            .verifyComplete();
    }

    @Override
    public void canSearchWithMinimumCoverage() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        Flux<PagedResponse<SearchResult>> results = client.search("*", new SearchParameters().minimumCoverage(50.0), new SearchRequestOptions()).byPage();
        Assert.assertNotNull(results);

        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(100.0, ((SearchPagedResponse) res).coverage(), 0))
            .verifyComplete();
    }

    @Override
    public void canUseHitHighlighting() throws IOException, InterruptedException {
        documents = uploadDocuments(HOTELS_DATA_JSON);

        //arrange
        String description = "Description";
        String category = "Category";

        SearchParameters sp = new SearchParameters();
        sp.filter("Rating eq 5");
        sp.highlightPreTag("<b>");
        sp.highlightPostTag("</b>");
        sp.highlightFields(Arrays.asList(category, description));

        //act
        PagedFlux<SearchResult> results = client.search("luxury hotel", sp, new SearchRequestOptions());

        //sanity
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            // sanity
            Assert.assertEquals(1, res.items().size());
            Map<String, List<String>> highlights = res.items().get(0).highlights();
            Assert.assertNotNull(highlights);
            Assert.assertEquals(2, highlights.keySet().size());
            Assert.assertTrue(highlights.containsKey(description));
            Assert.assertTrue(highlights.containsKey(category));
            String categoryHighlight = highlights.get(category).get(0);

            //asserts
            Assert.assertEquals("<b>Luxury</b>", categoryHighlight);

            // Typed as IEnumerable so we get the right overload of Assert.Equals below.
            List<String> expectedDescriptionHighlights =
                Arrays.asList(
                    "Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                    "We highly recommend this <b>hotel</b>."
                );
            Assert.assertEquals(expectedDescriptionHighlights, highlights.get(description));
        }).verifyComplete();
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
