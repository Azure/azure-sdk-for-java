// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedIterableBase;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import com.azure.search.models.CoordinateSystem;
import com.azure.search.models.FacetResult;
import com.azure.search.models.QueryType;
import com.azure.search.models.RangeFacetResult;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SearchOptions;
import com.azure.search.models.SearchResult;
import com.azure.search.models.ValueFacetResult;
import com.azure.search.test.environment.models.Bucket;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.NonNullableModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.models.QueryType.SIMPLE;
import static com.azure.search.models.SearchMode.ALL;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchSyncTests extends SearchTestBase {

    private SearchIndexClient client;

    @Override
    protected void search(
        String searchText, SearchOptions searchOptions, RequestOptions requestOptions) {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        client.search(searchText, searchOptions, requestOptions, Context.NONE).iterableByPage().iterator().next();
    }

    @Test
    public void canSearchDynamicDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        PagedIterableBase<SearchResult, SearchPagedResponse> searchResults = client.search("*");
        assertNotNull(searchResults);

        Iterator<SearchPagedResponse> iterator = searchResults.iterableByPage().iterator();

        List<Map<String, Object>> actualResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNull(result.getCount());
            assertNull(result.getCoverage());
            assertNull(result.getFacets());
            assertNotNull(result.getValue());

            result.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());
                actualResults.add(item.getDocument());
            });
        }
        assertEquals(hotels.size(), actualResults.size());
        assertTrue(compareResults(actualResults, hotels));
    }

    @Test
    public void canContinueSearch() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // upload large documents batch
        hotels = createHotelsList(100);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        PagedIterableBase<SearchResult, SearchPagedResponse> results =
            client.search("*", searchOptions, generateRequestOptions(), Context.NONE);

        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        PagedResponse<SearchResult> firstPage = iterator.next();
        assertEquals(50, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 50), firstPage.getValue());
        assertNotNull(firstPage.getContinuationToken());

        PagedResponse<SearchResult> secondPage = iterator.next();
        assertEquals(50, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(50, 100), secondPage.getValue());
        assertNull(secondPage.getContinuationToken());
    }

    @Test
    public void canContinueSearchWithTop() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // upload large documents batch
        hotels = createHotelsList(3000);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions()
            .setTop(2000)
            .setSelect("HotelId")
            .setOrderBy("HotelId asc");

        List<String> expectedHotelIds = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        PagedIterableBase<SearchResult, SearchPagedResponse> results =
            client.search("*", searchOptions, generateRequestOptions(), Context.NONE);

        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        PagedResponse<SearchResult> firstPage = iterator.next();
        assertEquals(1000, firstPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(0, 1000), firstPage.getValue());
        assertNotNull(firstPage.getContinuationToken());

        PagedResponse<SearchResult> secondPage = iterator.next();
        assertEquals(1000, secondPage.getValue().size());
        assertListEqualHotelIds(expectedHotelIds.subList(1000, 2000), secondPage.getValue());
        assertNull(secondPage.getContinuationToken());
    }

    @Test
    public void canSearchStaticallyTypedDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            new SearchOptions(), generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();

        List<Hotel> actualResults = new ArrayList<>();
        while (iterator.hasNext()) {
            SearchPagedResponse result = iterator.next();
            assertNull(result.getCount());
            assertNull(result.getCoverage());
            assertNull(result.getFacets());
            assertNotNull(result.getValue());

            result.getElements().forEach(item -> {
                assertEquals(1, item.getScore(), 0);
                assertNull(item.getHighlights());
                actualResults.add(convertToType(item.getDocument(), Hotel.class));
            });
        }

        ObjectMapper objectMapper = new ObjectMapper();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getDefault());
        objectMapper.setDateFormat(df);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<Hotel> hotelsList = hotels.stream().map(hotel -> {
            Hotel h = objectMapper.convertValue(hotel, Hotel.class);
            if (h.location() != null) {
                h.location().setCoordinateSystem(CoordinateSystem.create());
            }
            return h;
        }).collect(Collectors.toList());

        assertEquals(hotelsList.size(), actualResults.size());
        for (int i = 0; i < hotelsList.size(); i++) {
            TestHelpers.assertHotelsEqual(hotelsList.get(i), actualResults.get(i));
        }
    }

    @Test
    public void canRoundTripNonNullableValueTypes() throws Exception {
        String indexName = createIndexWithNonNullableTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

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

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            new SearchOptions(), generateRequestOptions(), Context.NONE);
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        assertTrue(iterator.hasNext());

        PagedResponse<SearchResult> result = iterator.next();
        assertEquals(2, result.getValue().size());
        TestHelpers.assetNonNullableModelsEqual(doc1, convertToType(result.getValue().get(0).getDocument(), NonNullableModel.class));
        TestHelpers.assetNonNullableModelsEqual(doc2, convertToType(result.getValue().get(1).getDocument(), NonNullableModel.class));
    }

    @Test
    public void canSearchWithDateInStaticModel() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        // check if deserialization of Date type object is successful
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Date expected = DATE_FORMAT.parse("2010-06-27T00:00:00Z");

        PagedIterableBase<SearchResult, SearchPagedResponse>  results = client
            .search("Fancy", new SearchOptions(), generateRequestOptions(), Context.NONE);
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        assertTrue(iterator.hasNext());

        PagedResponse<SearchResult> result = iterator.next();
        assertEquals(1, result.getValue().size());
        Date actual = convertToType(result.getValue().get(0).getDocument(), Hotel.class).lastRenovationDate();
        assertEquals(expected, actual);
    }

    @Test
    public void canSearchWithSelectedFields() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        // Ask JUST for the following two fields
        SearchOptions sp = new SearchOptions();
        sp.setSearchFields("HotelName", "Category");
        sp.setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("fancy luxury secret",
            sp, generateRequestOptions(), Context.NONE);

        HashMap<String, Object> expectedHotel1 = new HashMap<>();
        expectedHotel1.put("HotelName", "Fancy Stay");
        expectedHotel1.put("Rating", 5);
        expectedHotel1.put("Address", null);
        expectedHotel1.put("Rooms", Collections.emptyList());

        // This is the expected document when querying the document later (notice that only two fields are expected)
        HashMap<String, Object> expectedHotel2 = new HashMap<>();
        expectedHotel2.put("HotelName", "Secret Point Motel");
        expectedHotel2.put("Rating", 4);
        HashMap<String, Object> address = new HashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        HashMap<String, Object> rooms = new HashMap<>();
        rooms.put("Type", "Budget Room");
        HashMap<String, Object> rooms2 = new HashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        PagedResponse<SearchResult> result = iterator.next();
        assertEquals(2, result.getValue().size());

        // From the result object, extract the two hotels, clean up (irrelevant fields) and change data structure
        // as a preparation to check equality
        Map<String, Object> hotel1 = extractAndTransformSingleResult(result.getValue().get(0));
        Map<String, Object> hotel2 = extractAndTransformSingleResult(result.getValue().get(1));

        assertEquals(expectedHotel1, hotel1);
        assertEquals(expectedHotel2, hotel2);
    }

    @Test
    public void canUseTopAndSkipForClientSidePaging() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            parameters, generateRequestOptions(), Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("1", "10", "2"));

        parameters.setSkip(3);
        results = client.search("*", parameters, generateRequestOptions(), Context.NONE);
        assertKeySequenceEqual(results, Arrays.asList("3", "4", "5"));
    }

    @Test
    public void searchWithoutOrderBySortsByScore() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Iterator<SearchResult> results = client
            .search("*", new SearchOptions().setFilter("Rating lt 4"), generateRequestOptions(), Context.NONE).iterator();
        SearchResult firstResult = results.next();
        SearchResult secondResult = results.next();
        assertTrue(firstResult.getScore() <= secondResult.getScore());
    }

    @Test
    public void orderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String[] expectedResults = new String[]{"1", "9", "3", "4", "5", "10", "2", "6", "7", "8"};

        Stream<String> results = client
            .search("*",
                new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc"), generateRequestOptions(), Context.NONE).stream()
            .map(res -> getSearchResultId(res, "HotelId"));
        assertArrayEquals(results.toArray(), expectedResults);
    }

    @Test
    public void canFilter() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            searchOptions, generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(2, searchResultsList.size());

        List<Object> hotelIds = searchResultsList.stream().map(r -> r.get("HotelId")).collect(Collectors.toList());
        assertTrue(Arrays.asList("1", "5").containsAll(hotelIds));
    }

    @Test
    public void canSearchWithRangeFacets() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            getSearchOptionsForRangeFacets(), generateRequestOptions(), Context.NONE);

        for (SearchPagedResponse result : results.iterableByPage()) {
            assertContainHotelIds(hotels, result.getValue());
            assertNotNull(result.getFacets());
            List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(result.getFacets(), "Rooms/BaseRate", 4);
            List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(
                result.getFacets(), "LastRenovationDate", 2);
            assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
        }
    }

    @Test
    public void canSearchWithValueFacets() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            getSearchOptionsForValueFacets(), generateRequestOptions(), Context.NONE);

        for (SearchPagedResponse result : results.iterableByPage()) {
            assertContainHotelIds(hotels, result.getValue());
            Map<String, List<FacetResult>> facets = result.getFacets();
            assertNotNull(facets);

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

    @Test
    public void canSearchWithLuceneSyntax() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL).setSelect("HotelName", "Rating");
        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("HotelName:roch~",
            searchOptions, generateRequestOptions(), Context.NONE);

        assertNotNull(results);
        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(1, searchResultsList.size());
        assertEquals(expectedResult, searchResultsList.get(0));
    }

    @Test
    public void canFilterNonNullableType() {
        String indexName = createIndexWithValueTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        List<Map<String, Object>> docsList = createDocsListWithValueTypes();
        uploadDocuments(client, docsList);

        List<Map<String, Object>> expectedDocsList =
            docsList
                .stream()
                .filter(d -> !d.get("Key").equals("789"))
                .collect(Collectors.toList());


        SearchOptions searchOptions = new SearchOptions()
            .setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("*",
            searchOptions, generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> searchResultsList = getSearchResults(results);
        assertEquals(2, searchResultsList.size());
        for (int i = 0; i < searchResultsList.size(); i++) {
            TestHelpers.assertDocumentsEqual(expectedDocsList.get(i), searchResultsList.get(i));
        }
    }

    @Test
    public void canSearchWithSearchModeAll() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<Map<String, Object>> response = getSearchResults(client
            .search("Cheapest hotel", new SearchOptions().setQueryType(SIMPLE).setSearchMode(ALL),
                generateRequestOptions(), Context.NONE));
        assertEquals(1, response.size());
        assertEquals("2", response.get(0).get("HotelId"));
    }

    @Test
    public void defaultSearchModeIsAny() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        List<Map<String, Object>> response = getSearchResults(client.search("Cheapest hotel",
            new SearchOptions(), generateRequestOptions(), Context.NONE));
        assertEquals(7, response.size());
        assertEquals(
            Arrays.asList("2", "10", "3", "4", "5", "1", "9"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void canGetResultCountInSearch() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedIterableBase<SearchResult, SearchPagedResponse>  results = client.search("*",
            new SearchOptions().setIncludeTotalResultCount(true), generateRequestOptions(), Context.NONE);
        assertNotNull(results);
        Iterable<SearchPagedResponse> pagesIterable = results.iterableByPage();
        Iterator<SearchPagedResponse> iterator = pagesIterable.iterator();

        assertEquals(hotels.size(), iterator.next().getCount().intValue());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void canSearchWithRegex() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");

        PagedIterableBase<SearchResult, SearchPagedResponse>  results = client.search("HotelName:/.*oach.*\\/?/",
            searchOptions, generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        assertEquals(1, resultsList.size());
        assertEquals(dropUnnecessaryFields(resultsList.get(0)), expectedHotel);
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegex() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        PagedIterableBase<SearchResult, SearchPagedResponse>  results = client.search("\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:",
            searchOptions, generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> resultsList = getSearchResults(results);
        assertEquals(0, resultsList.size());
    }

    @Test
    public void searchWithScoringProfileBoostsScore() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("nearest")
            .setScoringParameters("myloc-'-122','49'")
            .setFilter("Rating eq 5 or Rating eq 1");

        List<Map<String, Object>> response = getSearchResults(client.search("hotel",
            searchOptions, generateRequestOptions(), Context.NONE));
        assertEquals(2, response.size());
        assertEquals(
            Arrays.asList("2", "1"),
            response.stream().map(res -> res.get("HotelId").toString()).collect(Collectors.toList()));
    }

    @Test
    public void canSearchWithMinimumCoverage() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        PagedIterableBase<SearchResult, SearchPagedResponse>  results = client.search("*",
            new SearchOptions().setMinimumCoverage(50.0), generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        Iterator<SearchPagedResponse> resultsIterator = results.iterableByPage().iterator();

        assertEquals(100.0, resultsIterator.next().getCoverage(), 0);
    }

    @Test
    public void canUseHitHighlighting() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        //arrange
        String description = "Description";
        String category = "Category";

        SearchOptions sp = new SearchOptions();
        sp.setFilter("Rating eq 5");
        sp.setHighlightPreTag("<b>");
        sp.setHighlightPostTag("</b>");
        sp.setHighlightFields(category, description);

        //act
        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("luxury hotel",
            sp, generateRequestOptions(), Context.NONE);

        //sanity
        assertNotNull(results);
        Iterator<SearchPagedResponse> iterator = results.iterableByPage().iterator();
        PagedResponse<SearchResult> result = iterator.next();
        List<SearchResult> documents = result.getValue();

        // sanity
        assertEquals(1, documents.size());
        Map<String, List<String>> highlights = documents.get(0).getHighlights();
        assertNotNull(highlights);
        assertEquals(2, highlights.keySet().size());
        assertTrue(highlights.containsKey(description));
        assertTrue(highlights.containsKey(category));

        String categoryHighlight = highlights.get(category).get(0);

        //asserts
        assertEquals("<b>Luxury</b>", categoryHighlight);

        // Typed as IEnumerable so we get the right overload of Equals below.
        List<String> expectedDescriptionHighlights =
            Arrays.asList(
                "Best <b>hotel</b> in town if you like <b>luxury</b> <b>hotels</b>.",
                "We highly recommend this <b>hotel</b>."
            );

        assertEquals(expectedDescriptionHighlights, highlights.get(description));
    }

    @Test
    public void canSearchWithSynonyms() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String fieldName = "HotelName";
        prepareHotelsSynonymMap("names", "luxury,fancy", fieldName);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSearchFields(fieldName)
            .setSelect("HotelName", "Rating");

        PagedIterableBase<SearchResult, SearchPagedResponse> results = client.search("luxury",
            searchOptions, generateRequestOptions(), Context.NONE);
        assertNotNull(results);

        List<Map<String, Object>> response = getSearchResults(results);
        assertEquals(1, response.size());
        assertEquals("Fancy Stay", response.get(0).get("HotelName"));
        assertEquals(5, response.get(0).get("Rating"));
    }

    private List<Map<String, Object>> getSearchResults(PagedIterableBase<SearchResult, SearchPagedResponse> results) {
        Iterator<SearchPagedResponse> resultsIterator = results.iterableByPage().iterator();

        List<Map<String, Object>> searchResults = new ArrayList<>();
        while (resultsIterator.hasNext()) {
            SearchPagedResponse result = resultsIterator.next();
            assertNotNull(result.getValue());
            result.getElements().forEach(item -> searchResults.add(dropUnnecessaryFields(item.getDocument())));
        }

        return searchResults;
    }

    private Map<String, Object> extractAndTransformSingleResult(SearchResult result) {
        return dropUnnecessaryFields(convertHashMapToMap(
                (result.getDocument())));
    }

    /**
     * Convert a HashMap object to Map object
     *
     * @param mapObject object to convert
     * @return {@link Map}{@code <}{@link String}{@code ,}{@link Object}{@code >}
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> convertHashMapToMap(Object mapObject) {
        // This SuppressWarnings is for the checkstyle it is used because api return type can
        // be anything and therefore is an Object in our case we know and we use it only when
        // the return type is LinkedHashMap. The object is converted into HashMap (which LinkedHashMap
        // extends)
        HashMap<String, Object> map = (HashMap<String, Object>) mapObject;

        Set<Map.Entry<String, Object>> entries = map.entrySet();

        Map<String, Object> convertedMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : entries) {
            Object value = entry.getValue();

            if (value instanceof HashMap) {
                value = convertHashMapToMap(entry.getValue());
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
    private static ArrayList<Object> convertArray(ArrayList<Object> array) {
        ArrayList<Object> convertedArray = new ArrayList<>();
        for (Object arrayValue : array) {
            if (arrayValue instanceof HashMap) {
                convertedArray.add(convertHashMapToMap(arrayValue));
            } else {
                convertedArray.add(arrayValue);
            }
        }
        return convertedArray;
    }

    private void assertKeySequenceEqual(PagedIterableBase<SearchResult, SearchPagedResponse> results, List<String> expectedKeys) {
        assertNotNull(results);

        List<String> actualKeys = results.stream().filter(doc -> doc.getDocument().containsKey("HotelId"))
            .map(doc -> (String) doc.getDocument().get("HotelId")).collect(Collectors.toList());

        assertEquals(expectedKeys, actualKeys);
    }
}
