// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.customization;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.data.common.SearchPagedResponse;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.models.CoordinateSystem;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.QueryType;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import com.azure.search.test.environment.models.Bucket;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.NonNullableModel;
import org.junit.Assert;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.data.generated.models.QueryType.SIMPLE;
import static com.azure.search.data.generated.models.SearchMode.ALL;

public class SearchAsyncTests extends SearchTestBase {

    private SearchIndexAsyncClient client;

    @Override
    public void canContinueSearch() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = createHotelsList(100);
        uploadDocuments(client, hotels);

        SearchParameters searchParameters = new SearchParameters().select(Collections.singletonList("HotelId"))
            .orderBy(Collections.singletonList("HotelId asc"));
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        List<String> expectedId = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        // Default page size is 50 if the value of top is less than 1000, or not specified
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(50, firstPage.value().size());
                assertListEqualHotelIds(expectedId.subList(0, 50), firstPage.value());
                Assert.assertNotNull(firstPage.nextLink());
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(50, nextPage.value().size());
                assertListEqualHotelIds(expectedId.subList(50, 100), nextPage.value());
                Assert.assertNull(nextPage.nextLink());
            }).verifyComplete();
    }

    @Override
    public void canContinueSearchWithTop() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        // upload large documents batch
        hotels = createHotelsList(2000);
        uploadDocuments(client, hotels);

        SearchParameters searchParameters = new SearchParameters().top(2000).select(
            Collections.singletonList("HotelId"))
            .orderBy(Collections.singletonList("HotelId asc"));
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        List<String> expectedId = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        // Maximum page size is 1000 if the value of top is grater than 1000.
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                Assert.assertEquals(1000, firstPage.value().size());
                assertListEqualHotelIds(expectedId.subList(0, 1000), firstPage.value());
                Assert.assertNotNull(firstPage.nextLink());
            })
            .assertNext(nextPage -> {
                Assert.assertEquals(1000, nextPage.value().size());
                assertListEqualHotelIds(expectedId.subList(1000, 2000), nextPage.value());
                Assert.assertNull(nextPage.nextLink());
            }).verifyComplete();
    }

    @Override
    public void canSearchWithSelectedFields() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        HashMap<String, Object> expectedHotel1 = new HashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Collections.emptyList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        HashMap<String, Object> expectedHotel2 = new HashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 4);
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
    public void canUseTopAndSkipForClientSidePaging() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters parameters = new SearchParameters().top(3).skip(0).orderBy(
            Collections.singletonList("HotelId"));

        Flux<SearchResult> results = client.search("*", parameters, new SearchRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("1", "10", "2"), results);

        parameters.skip(3);
        results = client.search("*", parameters, new SearchRequestOptions());
        assertHotelIdSequenceEqual(Arrays.asList("3", "4", "5"), results);
    }

    @Override
    public void canFilterNonNullableType() throws Exception {
        setupIndexFromJsonFile(MODEL_WITH_VALUE_TYPES_INDEX_JSON);
        client = getClientBuilder(MODEL_WITH_INDEX_TYPES_INDEX_NAME).buildAsyncClient();

        List<Map<String, Object>> expectedDocsList =
            uploadDocumentsJson(client, MODEL_WITH_VALUE_TYPES_DOCS_JSON)
                .stream().filter(d -> !d.get("Key").equals("789")).collect(
                Collectors.toList());

        SearchParameters searchParameters = new SearchParameters()
            .filter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        Flux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions()).log();
        Assert.assertNotNull(results);
        StepVerifier.create(results)
            .assertNext(
                res -> Assert.assertEquals(dropUnnecessaryFields(res.additionalProperties()), expectedDocsList.get(0)))
            .assertNext(
                res -> Assert.assertEquals(dropUnnecessaryFields(res.additionalProperties()), expectedDocsList.get(1)))
            .verifyComplete();
    }

    @Override
    public void searchWithoutOrderBySortsByScore() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters().filter("Rating lt 4"),
            new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertTrue(res.items().size() >= 2);
            SearchResult firstResult = res.items().get(0);
            SearchResult secondResult = res.items().get(1);
            Assert.assertTrue(firstResult.score() <= secondResult.score());
        }).verifyComplete();
    }

    @Override
    public void orderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<String> orderByValues = Arrays.asList("Rating desc", "LastRenovationDate asc");
        List<String> expected = Arrays.asList("1", "9", "3", "4", "5", "10", "2", "6", "7", "8");

        Flux<SearchResult> results = client.search("*", new SearchParameters().orderBy(orderByValues),
            new SearchRequestOptions()).log();
        assertHotelIdSequenceEqual(expected, results);
    }

    @Override
    public void canFilter() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .filter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedFlux<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                Assert.assertEquals(2, res.items().size());
                List<String> actualKeys = res.items().stream().filter(item -> item.additionalProperties().containsKey("HotelId"))
                    .map(item -> getSearchResultId(item, "HotelId")).collect(Collectors.toList());
                Assert.assertEquals(Arrays.asList("1", "5"), actualKeys);
            }).verifyComplete();
    }

    @Override
    public void canSearchWithRangeFacets() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client
            .search("*", getSearchParametersForRangeFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainHotelIds(hotels, res.items());
                Map<String, List<FacetResult>> facets = ((SearchPagedResponse) res).facets();
                Assert.assertNotNull(facets);
                List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(
                    facets, "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
            }).verifyComplete();
    }

    @Override
    public void canSearchWithValueFacets() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFlux<SearchResult> results = client
            .search("*", getSearchParametersForValueFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainHotelIds(hotels, res.items());
                Map<String, List<FacetResult>> facets = ((SearchPagedResponse) res).facets();
                Assert.assertNotNull(facets);

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "Rating", 2),
                    new ArrayList<>(Arrays.asList(
                        new ValueFacetResult(1L, 5),
                        new ValueFacetResult(4L, 4))));

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "SmokingAllowed", 2),
                    new ArrayList<>(Arrays.asList(
                        new ValueFacetResult(4L, false),
                        new ValueFacetResult(2L, true))));

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "Category", 3),
                    new ArrayList<>(Arrays.asList(
                        new ValueFacetResult(5L, "Budget"),
                        new ValueFacetResult(1L, "Boutique"),
                        new ValueFacetResult(1L, "Luxury"))));

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "LastRenovationDate", 6),
                    new ArrayList<>(Arrays.asList(new ValueFacetResult(1L, OffsetDateTime.parse("1970-01-01T00:00:00Z")),
                        new ValueFacetResult(1L, OffsetDateTime.parse("1982-01-01T00:00:00Z")),
                        new ValueFacetResult(2L, OffsetDateTime.parse("1995-01-01T00:00:00Z")),
                        new ValueFacetResult(1L, OffsetDateTime.parse("1999-01-01T00:00:00Z")),
                        new ValueFacetResult(1L, OffsetDateTime.parse("2010-01-01T00:00:00Z")),
                        new ValueFacetResult(1L, OffsetDateTime.parse("2012-01-01T00:00:00Z")))));

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "Rooms/BaseRate", 4),
                    new ArrayList<>(Arrays.asList(
                        new ValueFacetResult(1L, 2.44),
                        new ValueFacetResult(1L, 7.69),
                        new ValueFacetResult(1L, 8.09),
                        new ValueFacetResult(1L, 9.69))));

                assertValueFacetsEqual(
                    getValueFacetsForField(facets, "Tags", 10),
                    new ArrayList<>(Arrays.asList(
                        new ValueFacetResult(1L, "24-hour front desk service"),
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
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        HashMap<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchParameters searchParameters = new SearchParameters().queryType(QueryType.FULL)
            .select(Arrays.asList("HotelName", "Rating"));
        PagedFlux<SearchResult> results = client
            .search("HotelName:roch~", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                Assert.assertEquals(1, res.items().size());
                Assert.assertEquals(expectedResult, dropUnnecessaryFields(res.items().get(0).additionalProperties()));
            }).verifyComplete();
    }

    @Override
    public void canSearchDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

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

        Assert.assertEquals(hotels.size(), actualResults.size());
        Assert.assertTrue(compareResults(actualResults, hotels));
    }

    @Override
    public void canSearchStaticallyTypedDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

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
        List<Hotel> hotelsList = hotels.stream().map(hotel -> {
            Hotel h = jsonApi.convertObjectToType(hotel, Hotel.class);
            if (h.location() != null) {
                h.location().coordinateSystem(CoordinateSystem.create());
            }
            return h;
        }).collect(Collectors.toList());

        Assert.assertEquals(hotelsList.size(), actualResults.size());
        Assert.assertEquals(hotelsList, actualResults);
    }

    @Override
    public void canRoundTripNonNullableValueTypes() throws Exception {
        setupIndexFromJsonFile(NON_NULLABLE_INDEX_JSON);
        client = getClientBuilder(NON_NULLABLE_INDEX_NAME).buildAsyncClient();

        NonNullableModel doc1 = new NonNullableModel()
            .key("123")
            .count(3)
            .isEnabled(true)
            .rating(5)
            .ratio(3.14)
            .startDate(DATE_FORMAT.parse("2010-06-01T00:00:00Z"))
            .endDate(DATE_FORMAT.parse("2010-06-15T00:00:00Z"))
            .topLevelBucket(new Bucket().bucketName("A").count(12))
            .buckets(new Bucket[]{new Bucket().bucketName("B").count(20), new Bucket().bucketName("C").count(7)});

        NonNullableModel doc2 = new NonNullableModel().key("456").buckets(new Bucket[]{});

        uploadDocuments(client, Arrays.asList(doc1, doc2));

        PagedFlux<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            Assert.assertEquals(2, res.items().size());
            Assert.assertEquals(doc1, res.items().get(0).additionalProperties().as(NonNullableModel.class));
            Assert.assertEquals(doc2, res.items().get(1).additionalProperties().as(NonNullableModel.class));
        }).verifyComplete();
    }

    @Override
    public void canSearchWithDateInStaticModel() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        // check if deserialization of Date type object is successful
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Date expected = DATE_FORMAT.parse("2010-06-27T00:00:00Z");

        Flux<SearchResult> results = client.search("Fancy", new SearchParameters(), new SearchRequestOptions()).log();
        Assert.assertNotNull(results);
        StepVerifier.create(results).assertNext(res -> {
            Date actual = res.additionalProperties().as(Hotel.class).lastRenovationDate();
            Assert.assertEquals(expected, actual);
        }).verifyComplete();
    }

    @Override
    public void canSearchWithSynonyms() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String fieldName = "HotelName";
        prepareHotelsSynonymMap("names", "luxury,fancy", fieldName);

        SearchParameters searchParameters = new SearchParameters()
            .queryType(QueryType.FULL)
            .searchFields(Collections.singletonList(fieldName))
            .select(Arrays.asList("HotelName", "Rating"));

        Flux<SearchResult> results = client.search("luxury", searchParameters, new SearchRequestOptions()).log();
        Assert.assertNotNull(results);

        StepVerifier.create(results)
            .assertNext(res -> {
                Hotel hotel = res.additionalProperties().as(Hotel.class);
                Assert.assertEquals(5, hotel.rating(), 0);
                Assert.assertEquals("Fancy Stay", hotel.hotelName());
            }).verifyComplete();
    }

    @Override
    public void canSearchWithSearchModeAll() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<SearchResult> response = client
            .search("Cheapest hotel", new SearchParameters().queryType(SIMPLE).searchMode(ALL),
                new SearchRequestOptions()).log();
        StepVerifier.create(response)
            .assertNext(res -> Assert.assertEquals("2", getSearchResultId(res, "HotelId"))).verifyComplete();
    }

    @Override
    public void defaultSearchModeIsAny() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<SearchResult> response = client
            .search("Cheapest hotel", new SearchParameters(), new SearchRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("2", "10", "3", "4", "5", "1", "9"), response);
    }

    @Override
    public void canGetResultCountInSearch() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Flux<PagedResponse<SearchResult>> results = client
            .search("*", new SearchParameters().includeTotalResultCount(true), new SearchRequestOptions()).byPage();
        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(hotels.size(), ((SearchPagedResponse) res).count().intValue()))
            .verifyComplete();
    }

    @Override
    public void canSearchWithRegex() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        SearchParameters searchParameters = new SearchParameters()
            .queryType(QueryType.FULL)
            .select(Arrays.asList("HotelName", "Rating"));

        Flux<SearchResult> results = client
            .search("HotelName:/.*oach.*\\/?/", searchParameters, new SearchRequestOptions()).log();

        Assert.assertNotNull(results);
        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(dropUnnecessaryFields(res.additionalProperties()), expectedHotel))
            .verifyComplete();
    }

    @Override
    public void canSearchWithEscapedSpecialCharsInRegex() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters().queryType(QueryType.FULL);

        PagedFlux<SearchResult> results = client
            .search(
                "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchParameters,
                new SearchRequestOptions());
        Assert.assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(res -> Assert.assertEquals(0, res.items().size()))
            .verifyComplete();
    }

    @Override
    public void searchWithScoringProfileBoostsScore() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .scoringProfile("nearest")
            .scoringParameters(Collections.singletonList("myloc-'-122','49'"))
            .filter("Rating eq 5 or Rating eq 1");

        Flux<SearchResult> response = client.search("hotel", searchParameters, new SearchRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("2", "1"), response);
    }

    @Override
    public void canSearchWithMinimumCoverage() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<PagedResponse<SearchResult>> results = client
            .search("*", new SearchParameters().minimumCoverage(50.0), new SearchRequestOptions()).byPage();
        Assert.assertNotNull(results);

        StepVerifier.create(results)
            .assertNext(res -> Assert.assertEquals(100.0, ((SearchPagedResponse) res).coverage(), 0))
            .verifyComplete();
    }

    @Override
    public void canUseHitHighlighting() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

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
    protected void search(
        String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        PagedFlux<SearchResult> results = client.search(searchText, searchParameters, searchRequestOptions);
        results.log().blockFirst();
    }

    private void assertHotelIdsEqual(List<String> expected, List<SearchResult> results) {
        Assert.assertNotNull(results);
        List<String> actualKeys = results.stream().filter(item -> item.additionalProperties().containsKey("HotelId"))
            .map(item -> (String) item.additionalProperties().get("HotelId")).collect(Collectors.toList());
        Assert.assertEquals(expected, actualKeys);
    }

    private void assertHotelIdSequenceEqual(List<String> expectedIds, Flux<SearchResult> results) {
        Assert.assertNotNull(results);
        StepVerifier.FirstStep<SearchResult> sv = StepVerifier.create(results);
        expectedIds.forEach(k -> sv.assertNext(res -> Assert.assertEquals(k, getSearchResultId(res, "HotelId"))));
        sv.verifyComplete();
    }

}
