// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.search.common.SearchPagedResponse;
import com.azure.search.common.jsonwrapper.JsonWrapper;
import com.azure.search.common.jsonwrapper.api.Config;
import com.azure.search.common.jsonwrapper.api.JsonApi;
import com.azure.search.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.models.CoordinateSystem;
import com.azure.search.models.FacetResult;
import com.azure.search.models.QueryType;
import com.azure.search.models.SearchParameters;
import com.azure.search.models.SearchRequestOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.test.environment.models.Bucket;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.NonNullableModel;
import org.junit.Assert;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.models.QueryType.SIMPLE;
import static com.azure.search.models.SearchMode.ALL;

public class SearchSyncTests extends SearchTestBase {

    private SearchIndexClient client;

    @Override
    protected void search(
        String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions) {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        PagedIterable<SearchResult> results = client.search(searchText, searchParameters, searchRequestOptions);
        results.iterableByPage().iterator().next();
    }

    @Override
    public void canSearchDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> iterator = pagesIterable.iterator();

        List<Map<String, Object>> actualResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            Assert.assertNull(result.count());
            Assert.assertNull(result.coverage());
            Assert.assertNull(result.facets());
            Assert.assertNotNull(result.getItems());

            result.getItems().forEach(item -> {
                Assert.assertEquals(1, item.getScore(), 0);
                Assert.assertNull(item.getHighlights());
                actualResults.add(item.getAdditionalProperties());
            });
        }
        Assert.assertEquals(hotels.size(), actualResults.size());
        Assert.assertTrue(compareResults(actualResults, hotels));
    }

    @Override
    public void canContinueSearch() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // upload large documents batch
        hotels = createHotelsList(100);
        uploadDocuments(client, hotels);

        SearchParameters searchParameters = new SearchParameters().setSelect(Collections.singletonList("HotelId"))
            .setOrderBy(Collections.singletonList("HotelId asc"));

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        PagedIterable<SearchResult> results =
            client.search("*", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);

        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();

        PagedResponse<SearchResult> firstPage = iterator.next();
        Assert.assertEquals(50, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 50), firstPage.getValue());
        Assert.assertNotNull(firstPage.getContinuationToken());

        PagedResponse<SearchResult> secondPage = iterator.next();
        Assert.assertEquals(50, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(50, 100), secondPage.getValue());
        Assert.assertNull(secondPage.getContinuationToken());
    }

    @Override
    public void canContinueSearchWithTop() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // upload large documents batch
        hotels = createHotelsList(2000);
        uploadDocuments(client, hotels);

        SearchParameters searchParameters = new SearchParameters()
            .setTop(2000)
            .setSelect(Collections.singletonList("HotelId"))
            .setOrderBy(Collections.singletonList("HotelId asc"));

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        PagedIterable<SearchResult> results =
            client.search("*", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);

        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();

        PagedResponse<SearchResult> firstPage = iterator.next();
        Assert.assertEquals(1000, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), firstPage.getValue());
        Assert.assertNotNull(firstPage.getContinuationToken());

        PagedResponse<SearchResult> secondPage = iterator.next();
        Assert.assertEquals(1000, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), secondPage.getValue());
        Assert.assertNull(secondPage.getContinuationToken());
    }

    @Override
    public void canSearchStaticallyTypedDocuments() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> iterator = pagesIterable.iterator();

        List<Hotel> actualResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            Assert.assertNull(result.count());
            Assert.assertNull(result.coverage());
            Assert.assertNull(result.facets());
            Assert.assertNotNull(result.getItems());

            result.getItems().forEach(item -> {
                Assert.assertEquals(1, item.getScore(), 0);
                Assert.assertNull(item.getHighlights());
                actualResults.add(item.getAdditionalProperties().as(Hotel.class));
            });
        }

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
        client = getClientBuilder(NON_NULLABLE_INDEX_NAME).buildClient();

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

        PagedIterable<SearchResult> results = client.search("*", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);
        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        Assert.assertTrue(iterator.hasNext());

        PagedResponse<SearchResult> result = iterator.next();
        Assert.assertEquals(2, result.getItems().size());
        Assert.assertEquals(doc1, result.getItems().get(0).getAdditionalProperties().as(NonNullableModel.class));
        Assert.assertEquals(doc2, result.getItems().get(1).getAdditionalProperties().as(NonNullableModel.class));
    }

    @Override
    public void canSearchWithDateInStaticModel() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // check if deserialization of Date type object is successful
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Date expected = DATE_FORMAT.parse("2010-06-27T00:00:00Z");

        PagedIterable<SearchResult> results = client
            .search("Fancy", new SearchParameters(), new SearchRequestOptions());
        Assert.assertNotNull(results);
        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        Assert.assertTrue(iterator.hasNext());

        PagedResponse<SearchResult> result = iterator.next();
        Assert.assertEquals(1, result.getItems().size());
        Date actual = result.getItems().get(0).getAdditionalProperties().as(Hotel.class).lastRenovationDate();
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canSearchWithSelectedFields() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        // Ask JUST for the following two fields
        SearchParameters sp = new SearchParameters();
        sp.setSearchFields(new LinkedList<>(Arrays.asList("HotelName", "Category")));
        sp.setSelect(new LinkedList<>(Arrays.asList("HotelName", "Rating", "Address/City", "Rooms/Type")));

        PagedIterable<SearchResult> results = client.search("fancy luxury secret", sp, new SearchRequestOptions());

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

        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        PagedResponse<SearchResult> result = iterator.next();
        Assert.assertEquals(2, result.getItems().size());

        // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
        // as a preparation to check equality
        Map<String, Object> hotel1 = extractAndTransformSingleResult(result.getItems().get(0));
        Map<String, Object> hotel2 = extractAndTransformSingleResult(result.getItems().get(1));

        Assert.assertEquals(expectedHotel1, hotel1);
        Assert.assertEquals(expectedHotel2, hotel2);
    }

    @Override
    public void canUseTopAndSkipForClientSidePaging() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<String> orderBy = Stream.of("HotelId").collect(Collectors.toList());
        SearchParameters parameters = new SearchParameters().setTop(3).setSkip(0).setOrderBy(orderBy);

        PagedIterable<SearchResult> results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "2"));

        parameters.setSkip(3);
        results = client.search("*", parameters, new SearchRequestOptions());
        assertKeySequenceEqual(results, Arrays.asList("3", "4", "5"));
    }

    @Override
    public void searchWithoutOrderBySortsByScore() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Iterator<SearchResult> results = client
            .search("*", new SearchParameters().setFilter("Rating lt 4"), new SearchRequestOptions()).iterator();
        SearchResult firstResult = results.next();
        SearchResult secondResult = results.next();
        Assert.assertTrue(firstResult.getScore() <= secondResult.getScore());
    }

    @Override
    public void orderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<String> orderByValues = new ArrayList<>();
        orderByValues.add("Rating desc");
        orderByValues.add("LastRenovationDate asc");

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        Stream<String> results = client
            .search("*", new SearchParameters().setOrderBy(orderByValues), new SearchRequestOptions()).stream()
            .map(res -> getSearchResultId(res, "HotelId"));
        Assert.assertArrayEquals(results.toArray(), expectedResults);
    }

    @Override
    public void canFilter() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedIterable<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        Assert.assertEquals(2, searchResultsList.size());

        List hotelIds = searchResultsList.stream().map(r -> r.get("HotelId")).collect(Collectors.toList());
        Assert.assertTrue(Arrays.asList("1", "5").containsAll(hotelIds));
    }

    @Override
    public void canSearchWithRangeFacets() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SearchResult> results = client.search("*", getSearchParametersForRangeFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();

        for (SearchPagedResponse result : pagesIterable) {
            assertContainHotelIds(hotels, result.getItems());
            Assert.assertNotNull(result.facets());
            List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(result.facets(), "Rooms/BaseRate", 4);
            List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(
                result.facets(), "LastRenovationDate", 2);
            assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
        }
    }

    @Override
    public void canSearchWithValueFacets() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SearchResult> results = client.search("*", getSearchParametersForValueFacets(), new SearchRequestOptions());
        Assert.assertNotNull(results);

        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();

        for (SearchPagedResponse result : pagesIterable) {
            assertContainHotelIds(hotels, result.getItems());
            Map<String, List<FacetResult>> facets = result.facets();
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

            assertValueFacetsEqual(getValueFacetsForField(facets, "LastRenovationDate", 6),
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
        }
    }

    @Override
    public void canSearchWithLuceneSyntax() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchParameters searchParameters = new SearchParameters().setQueryType(QueryType.FULL).setSelect(Arrays.asList("HotelName", "Rating"));
        PagedIterable<SearchResult> results = client.search("HotelName:roch~", searchParameters, new SearchRequestOptions());

        Assert.assertNotNull(results);
        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        Assert.assertEquals(1, searchResultsList.size());
        Assert.assertEquals(expectedResult, searchResultsList.get(0));
    }

    @Override
    public void canFilterNonNullableType() throws Exception {
        setupIndexFromJsonFile(MODEL_WITH_VALUE_TYPES_INDEX_JSON);
        client = getClientBuilder(MODEL_WITH_INDEX_TYPES_INDEX_NAME).buildClient();

        List<Map<String, Object>> expectedDocsList =
            uploadDocumentsJson(client, MODEL_WITH_VALUE_TYPES_DOCS_JSON)
                .stream()
                .filter(d -> !d.get("Key").equals("789"))
                .collect(Collectors.toList());

        SearchParameters searchParameters = new SearchParameters()
            .setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        PagedIterable<SearchResult> results = client.search("*", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        Assert.assertEquals(2, searchResultsList.size());

        Assert.assertEquals(expectedDocsList, searchResultsList);
    }

    @Override
    public void canSearchWithSearchModeAll() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<Map<String, Object>> response = getSearchResults(client
            .search("Cheapest hotel", new SearchParameters().setQueryType(SIMPLE).setSearchMode(ALL),
                new SearchRequestOptions()));
        Assert.assertEquals(1, response.size());
        Assert.assertEquals("2", response.get(0).get("HotelId"));
    }

    @Override
    public void defaultSearchModeIsAny() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<Map<String, Object>> response = getSearchResults(client.search("Cheapest hotel", new SearchParameters(), new SearchRequestOptions()));
        Assert.assertEquals(7, response.size());
        Assert.assertEquals(
            Arrays.asList("2", "10", "3", "4", "5", "1", "9"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Override
    public void canGetResultCountInSearch() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterable<SearchResult> results = client.search("*", new SearchParameters().setIncludeTotalResultCount(true), new SearchRequestOptions());
        Assert.assertNotNull(results);
        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> iterator = pagesIterable.iterator();

        Assert.assertEquals(hotels.size(), iterator.next().count().intValue());
        Assert.assertFalse(iterator.hasNext());
    }

    @Override
    public void canSearchWithRegex() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchParameters searchParameters = new SearchParameters()
            .setQueryType(QueryType.FULL)
            .setSelect(Arrays.asList("HotelName", "Rating"));

        PagedIterable<SearchResult> results = client
            .search("HotelName:/.*oach.*\\/?/", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        Assert.assertEquals(1, resultsList.size());
        Assert.assertEquals(dropUnnecessaryFields(resultsList.get(0)), expectedHotel);
    }

    @Override
    public void canSearchWithEscapedSpecialCharsInRegex() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SearchParameters searchParameters = new SearchParameters().setQueryType(QueryType.FULL);

        PagedIterable<SearchResult> results = client
            .search(
                "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchParameters,
                new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);
        Assert.assertEquals(0, resultsList.size());
    }

    @Override
    public void searchWithScoringProfileBoostsScore() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SearchParameters searchParameters = new SearchParameters()
            .setScoringProfile("nearest")
            .setScoringParameters(Collections.singletonList("myloc-'-122','49'"))
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResults(
            client.search("hotel", searchParameters, new SearchRequestOptions()));
        Assert.assertEquals(2, response.size());
        Assert.assertEquals(
            Arrays.asList("2", "1"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Override
    public void canSearchWithMinimumCoverage() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        PagedIterable<SearchResult> results = client
            .search("*", new SearchParameters().setMinimumCoverage(50.0), new SearchRequestOptions());
        Assert.assertNotNull(results);

        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> resultsIterator = pagesIterable.iterator();

        Assert.assertEquals(100.0, resultsIterator.next().coverage(), 0);
    }

    @Override
    public void canUseHitHighlighting() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        String description = "Description";
        String category = "Category";

        SearchParameters sp = new SearchParameters();
        sp.setFilter("Rating eq 5");
        sp.setHighlightPreTag("<b>");
        sp.setHighlightPostTag("</b>");
        sp.setHighlightFields(Arrays.asList(category, description));

        //act
        PagedIterable<SearchResult> results = client.search("luxury hotel", sp, new SearchRequestOptions());

        //sanity
        Assert.assertNotNull(results);
        Iterator<PagedResponse<SearchResult>> iterator = results.iterableByPage().iterator();
        PagedResponse<SearchResult> result = iterator.next();
        List<SearchResult> documents = result.getItems();

        // sanity
        Assert.assertEquals(1, documents.size());
        Map<String, List<String>> highlights = documents.get(0).getHighlights();
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
    }

    @Override
    public void canSearchWithSynonyms() {
        createHotelIndex();
        client = getClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String fieldName = "HotelName";
        prepareHotelsSynonymMap("names", "luxury,fancy", fieldName);

        SearchParameters searchParameters = new SearchParameters()
            .setQueryType(QueryType.FULL)
            .setSearchFields(Collections.singletonList(fieldName))
            .setSelect(Arrays.asList("HotelName", "Rating"));

        PagedIterable<SearchResult> results =
            client.search("luxury", searchParameters, new SearchRequestOptions());
        Assert.assertNotNull(results);

        List<Map<String, Object>> response = getSearchResults(results);
        Assert.assertEquals(1, response.size());
        Assert.assertEquals("Fancy Stay", response.get(0).get("HotelName"));
        Assert.assertEquals(5, response.get(0).get("Rating"));
    }

    private List<Map<String, Object>> getSearchResults(PagedIterable<SearchResult> results) {
        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> resultsIterator = pagesIterable.iterator();

        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (resultsIterator.hasNext()) {
            SearchPagedResponse result = resultsIterator.next();
            Assert.assertNotNull(result.getItems());
            result.getItems().forEach(item -> searchResults.add(dropUnnecessaryFields(item.getAdditionalProperties())));
        }

        return searchResults;
    }

    private Map<String, Object> extractAndTransformSingleResult(SearchResult result) {
        return dropUnnecessaryFields(convertLinkedHashMapToMap(
                (result.getAdditionalProperties())));
    }

    /**
     * Convert a Linked HashMap object to Map object
     *
     * @param linkedMapObject object to convert
     * @return {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}
     */
    private static Map<String, Object> convertLinkedHashMapToMap(Object linkedMapObject) {
        /** This SuppressWarnings is for the checkstyle
         it is used because api return type can be anything and therefore is an Object
         in our case we know and we use it only when the return type is LinkedHashMap
         **/
        @SuppressWarnings(value = "unchecked")
        LinkedHashMap<String, Object> linkedMap = (LinkedHashMap<String, Object>) linkedMapObject;

        Set<Map.Entry<String, Object>> entries = linkedMap.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();

            if (value instanceof LinkedHashMap) {
                value = convertLinkedHashMapToMap(entry.getValue());
            }
            if (value instanceof ArrayList) {
                value = convertArray((ArrayList) value);

            }

            convertedMap.put(entry.getKey(), value);
        }

        return convertedMap;
    }

    /**
     * Convert Array Object elements
     *
     * @param array which elements will be converted
     * @return {@link ArrayList}{@code <}{@link Object}{@code >}
     */
    private static ArrayList<Object> convertArray(ArrayList array) {
        ArrayList<Object> convertedArray = new ArrayList<>();
        for (Object arrayValue : array) {
            if (arrayValue instanceof LinkedHashMap) {
                convertedArray.add(convertLinkedHashMapToMap(arrayValue));
            } else {
                convertedArray.add(arrayValue);
            }
        }
        return convertedArray;
    }

    private void assertKeySequenceEqual(PagedIterable<SearchResult> results, List<String> expectedKeys) {
        Assert.assertNotNull(results);

        List<String> actualKeys = results.stream().filter(doc -> doc.getAdditionalProperties().containsKey("HotelId"))
            .map(doc -> (String) doc.getAdditionalProperties().get("HotelId")).collect(Collectors.toList());

        Assert.assertEquals(expectedKeys, actualKeys);
    }
}
