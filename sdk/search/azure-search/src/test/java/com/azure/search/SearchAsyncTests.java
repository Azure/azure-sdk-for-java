// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.http.rest.PagedFluxBase;
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
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static com.azure.search.models.QueryType.SIMPLE;
import static com.azure.search.models.SearchMode.ALL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchAsyncTests extends SearchTestBase {

    private SearchIndexAsyncClient client;

    @Test
    public void canContinueSearch() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = createHotelsList(100);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions().setSelect("HotelId")
            .setOrderBy("HotelId asc");
        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", searchOptions, generateRequestOptions());

        List<String> expectedId = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        // Default page size is 50 if the value of top is less than 1000, or not specified
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                assertEquals(50, firstPage.getValue().size());
                assertListEqualHotelIds(expectedId.subList(0, 50), firstPage.getValue());
                assertNotNull(firstPage.getContinuationToken());
            })
            .assertNext(nextPage -> {
                assertEquals(50, nextPage.getValue().size());
                assertListEqualHotelIds(expectedId.subList(50, 100), nextPage.getValue());
                assertNull(nextPage.getContinuationToken());
            }).verifyComplete();
    }

    @Test
    public void canContinueSearchWithTop() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        // upload large documents batch
        hotels = createHotelsList(3000);
        uploadDocuments(client, hotels);

        SearchOptions searchOptions = new SearchOptions().setTop(2000).setSelect("HotelId")
            .setOrderBy("HotelId asc");
        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", searchOptions, generateRequestOptions());


        List<String> expectedId = hotels.stream().map(hotel -> (String) hotel.get("HotelId")).sorted()
            .collect(Collectors.toList());

        // Maximum page size is 1000 if the value of top is grater than 1000.
        // https://docs.microsoft.com/en-us/rest/api/searchservice/search-documents#top-optional
        StepVerifier.create(results.byPage())
            .assertNext(firstPage -> {
                assertEquals(1000, firstPage.getValue().size());
                assertListEqualHotelIds(expectedId.subList(0, 1000), firstPage.getValue());
                assertNotNull(firstPage.getContinuationToken());
            })
            .assertNext(nextPage -> {
                assertEquals(1000, nextPage.getValue().size());
                assertListEqualHotelIds(expectedId.subList(1000, 2000), nextPage.getValue());
                assertNull(nextPage.getContinuationToken());
            }).verifyComplete();
    }

    @Test
    public void canSearchWithSelectedFields() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

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
        HashMap<String, Object> address = new HashMap<>();
        address.put("City", "New York");
        expectedHotel2.put("Address", address);
        HashMap<String, Object> rooms = new HashMap<>();
        rooms.put("Type", "Budget Room");
        HashMap<String, Object> rooms2 = new HashMap<>();
        rooms2.put("Type", "Budget Room");
        expectedHotel2.put("Rooms", Arrays.asList(rooms, rooms2));

        // Ask JUST for the following two fields
        SearchOptions sp = new SearchOptions();
        sp.setSearchFields("HotelName", "Category");
        sp.setSelect("HotelName", "Rating", "Address/City", "Rooms/Type");

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("fancy luxury secret", sp, generateRequestOptions());
        assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(res -> {
            // expecting to get 2 documents back
            assertEquals(2, res.getItems().size());
            assertEquals(expectedHotel1, dropUnnecessaryFields(res.getItems().get(0).getDocument()));
            assertEquals(expectedHotel2, dropUnnecessaryFields(res.getItems().get(1).getDocument()));
        }).verifyComplete();
    }

    @Test
    public void canUseTopAndSkipForClientSidePaging() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions parameters = new SearchOptions().setTop(3).setSkip(0).setOrderBy("HotelId");

        Flux<SearchResult> results = client.search("*", parameters, generateRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("1", "10", "2"), results);

        parameters.setSkip(3);
        results = client.search("*", parameters, generateRequestOptions());
        assertHotelIdSequenceEqual(Arrays.asList("3", "4", "5"), results);
    }

    @Test
    public void canFilterNonNullableType() {
        String indexName = createIndexWithValueTypes();
        client = getSearchIndexClientBuilder(indexName).buildAsyncClient();

        List<Map<String, Object>> docsList = createDocsListWithValueTypes();
        uploadDocuments(client, docsList);

        List<Map<String, Object>> expectedDocsList =
            docsList
                .stream()
                .filter(d -> !d.get("Key").equals("789"))
                .collect(Collectors.toList());

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("IntValue eq 0 or (Bucket/BucketName eq 'B' and Bucket/Count lt 10)");

        Flux<SearchResult> results = client.search("*", searchOptions, generateRequestOptions()).log();
        assertNotNull(results);
        StepVerifier.create(results)
            .assertNext(res -> TestHelpers.assertDocumentsEqual(dropUnnecessaryFields(res.getDocument()), expectedDocsList.get(0)))
            .assertNext(res -> TestHelpers.assertDocumentsEqual(dropUnnecessaryFields(res.getDocument()), expectedDocsList.get(1)))
            .verifyComplete();
    }

    @Test
    public void searchWithoutOrderBySortsByScore() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", new SearchOptions().setFilter("Rating lt 4"),
            generateRequestOptions());
        assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            assertTrue(res.getItems().size() >= 2);
            SearchResult firstResult = res.getItems().get(0);
            SearchResult secondResult = res.getItems().get(1);
            assertTrue(firstResult.getScore() <= secondResult.getScore());
        }).verifyComplete();
    }

    @Test
    public void orderByProgressivelyBreaksTies() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        List<String> expected = Arrays.asList("1", "9", "3", "4", "5", "10", "2", "6", "7", "8");

        Flux<SearchResult> results = client.search("*", new SearchOptions().setOrderBy("Rating desc", "LastRenovationDate asc"),
            generateRequestOptions()).log();
        assertHotelIdSequenceEqual(expected, results);
    }

    @Test
    public void canFilter() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions searchOptions = new SearchOptions()
            .setFilter("Rating gt 3 and LastRenovationDate gt 2000-01-01T00:00:00Z");
        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", searchOptions, generateRequestOptions());

        assertNotNull(results);
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertEquals(2, res.getItems().size());
                List<String> actualKeys = res.getItems()
                    .stream()
                    .filter(item -> item.getDocument().containsKey("HotelId"))
                    .map(item -> getSearchResultId(item, "HotelId"))
                    .collect(Collectors.toList());
                assertEquals(Arrays.asList("1", "5"), actualKeys);
            }).verifyComplete();
    }

    @Test
    public void canSearchWithRangeFacets() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client
            .search("*", getSearchOptionsForRangeFacets(), generateRequestOptions());
        assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainHotelIds(hotels, res.getItems());
                Map<String, List<FacetResult>> facets = res.getFacets();
                assertNotNull(facets);
                List<RangeFacetResult> baseRateFacets = getRangeFacetsForField(facets, "Rooms/BaseRate", 4);
                List<RangeFacetResult> lastRenovationDateFacets = getRangeFacetsForField(
                    facets, "LastRenovationDate", 2);
                assertRangeFacets(baseRateFacets, lastRenovationDateFacets);
            }).verifyComplete();
    }

    @Test
    public void canSearchWithValueFacets() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client
            .search("*", getSearchOptionsForValueFacets(), generateRequestOptions());
        assertNotNull(results);

        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertContainHotelIds(hotels, res.getItems());
                Map<String, List<FacetResult>> facets = res.getFacets();
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

    @Test
    public void canSearchWithLuceneSyntax() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        HashMap<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("HotelName", "Roach Motel");
        expectedResult.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");
        PagedFluxBase<SearchResult, SearchPagedResponse> results = client
            .search("HotelName:roch~", searchOptions, generateRequestOptions());

        assertNotNull(results);
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertEquals(1, res.getItems().size());
                assertEquals(expectedResult, dropUnnecessaryFields(res.getItems().get(0).getDocument()));
            }).verifyComplete();
    }

    @Test
    public void canSearchDynamicDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*");
        assertNotNull(results);

        List<Map<String, Object>> actualResults = new ArrayList<>();
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertNull(res.getCount());
                assertNull(res.getCoverage());
                assertNull(res.getFacets());
                assertNotNull(res.getItems());

                res.getItems().forEach(item -> {
                    assertEquals(1, item.getScore(), 0);
                    assertNull(item.getHighlights());
                    actualResults.add(dropUnnecessaryFields(item.getDocument()));
                });
            }).verifyComplete();

        assertEquals(hotels.size(), actualResults.size());
        assertTrue(compareResults(actualResults, hotels));
    }

    @Test
    public void canSearchStaticallyTypedDocuments() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", new SearchOptions(), generateRequestOptions());
        assertNotNull(results);

        List<Hotel> actualResults = new ArrayList<>();
        StepVerifier.create(results.byPage())
            .assertNext(res -> {
                assertNull(res.getCount());
                assertNull(res.getCoverage());
                assertNull(res.getFacets());
                assertNotNull(res.getItems());

                res.getItems().forEach(item -> {
                    assertEquals(1, item.getScore(), 0);
                    assertNull(item.getHighlights());
                    actualResults.add(convertToType(item.getDocument(), Hotel.class));
                });
            }).verifyComplete();

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
        client = getSearchIndexClientBuilder(indexName).buildAsyncClient();

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

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("*", new SearchOptions(), generateRequestOptions());
        assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            assertEquals(2, res.getItems().size());
            TestHelpers.assetNonNullableModelsEqual(doc1, convertToType(res.getItems().get(0).getDocument(), NonNullableModel.class));
            TestHelpers.assetNonNullableModelsEqual(doc2, convertToType(res.getItems().get(1).getDocument(), NonNullableModel.class));
        }).verifyComplete();
    }

    @Test
    public void canSearchWithDateInStaticModel() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        // check if deserialization of Date type object is successful
        uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Date expected = DATE_FORMAT.parse("2010-06-27T00:00:00Z");

        Flux<SearchResult> results = client.search("Fancy", new SearchOptions(), generateRequestOptions()).log();
        assertNotNull(results);
        StepVerifier.create(results).assertNext(res -> {
            Date actual = convertToType(res.getDocument(), Hotel.class).lastRenovationDate();
            assertEquals(expected, actual);
        }).verifyComplete();
    }

    @Test
    public void canSearchWithSynonyms() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        String fieldName = "HotelName";
        prepareHotelsSynonymMap("names", "luxury,fancy", fieldName);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSearchFields(fieldName)
            .setSelect("HotelName", "Rating");

        Flux<SearchResult> results = client.search("luxury", searchOptions, generateRequestOptions()).log();
        assertNotNull(results);

        StepVerifier.create(results)
            .assertNext(res -> {
                Hotel hotel = convertToType(res.getDocument(), Hotel.class);
                assertEquals(5, hotel.rating(), 0);
                assertEquals("Fancy Stay", hotel.hotelName());
            }).verifyComplete();
    }

    @Test
    public void canSearchWithSearchModeAll() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<SearchResult> response = client
            .search("Cheapest hotel", new SearchOptions().setQueryType(SIMPLE).setSearchMode(ALL),
                generateRequestOptions()).log();
        StepVerifier.create(response)
            .assertNext(res -> assertEquals("2", getSearchResultId(res, "HotelId"))).verifyComplete();
    }

    @Test
    public void defaultSearchModeIsAny() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<SearchResult> response = client
            .search("Cheapest hotel", new SearchOptions(), generateRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("2", "10", "3", "4", "5", "1", "9"), response);
    }

    @Test
    public void canGetResultCountInSearch() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        hotels = uploadDocumentsJson(client, HOTELS_DATA_JSON);
        Flux<SearchPagedResponse> results = client
            .search("*", new SearchOptions().setIncludeTotalResultCount(true), generateRequestOptions()).byPage();
        StepVerifier.create(results)
            .assertNext(res -> assertEquals(hotels.size(), res.getCount().intValue()))
            .verifyComplete();
    }

    @Test
    public void canSearchWithRegex() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Map<String, Object> expectedHotel = new HashMap<>();
        expectedHotel.put("HotelName", "Roach Motel");
        expectedHotel.put("Rating", 1);

        SearchOptions searchOptions = new SearchOptions()
            .setQueryType(QueryType.FULL)
            .setSelect("HotelName", "Rating");

        Flux<SearchResult> results = client
            .search("HotelName:/.*oach.*\\/?/", searchOptions, generateRequestOptions()).log();

        assertNotNull(results);
        StepVerifier.create(results)
            .assertNext(res -> assertEquals(dropUnnecessaryFields(res.getDocument()), expectedHotel))
            .verifyComplete();
    }

    @Test
    public void canSearchWithEscapedSpecialCharsInRegex() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client
            .search(
                "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\~\\*\\?\\:", searchOptions,
                generateRequestOptions());
        assertNotNull(results);

        StepVerifier.create(results.byPage()).assertNext(res -> assertEquals(0, res.getItems().size()))
            .verifyComplete();
    }

    @Test
    public void searchWithScoringProfileBoostsScore() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        SearchOptions searchOptions = new SearchOptions()
            .setScoringProfile("nearest")
            .setScoringParameters("myloc-'-122','49'")
            .setFilter("Rating eq 5 or Rating eq 1");

        Flux<SearchResult> response = client.search("hotel", searchOptions, generateRequestOptions()).log();
        assertHotelIdSequenceEqual(Arrays.asList("2", "1"), response);
    }

    @Test
    public void canSearchWithMinimumCoverage() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        uploadDocumentsJson(client, HOTELS_DATA_JSON);

        Flux<SearchPagedResponse> results = client
            .search("*", new SearchOptions().setMinimumCoverage(50.0), generateRequestOptions()).byPage();
        assertNotNull(results);

        StepVerifier.create(results)
            .assertNext(res -> assertEquals(100.0, res.getCoverage(), 0))
            .verifyComplete();
    }

    @Test
    public void canUseHitHighlighting() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

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
        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search("luxury hotel", sp, generateRequestOptions());

        //sanity
        assertNotNull(results);
        StepVerifier.create(results.byPage()).assertNext(res -> {
            // sanity
            assertEquals(1, res.getItems().size());
            Map<String, List<String>> highlights = res.getItems().get(0).getHighlights();
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
        }).verifyComplete();
    }

    @Override
    protected void search(
        String searchText, SearchOptions searchOptions, RequestOptions requestOptions) {
        createHotelIndex();
        client = getSearchIndexClientBuilder(HOTELS_INDEX_NAME).buildAsyncClient();

        PagedFluxBase<SearchResult, SearchPagedResponse> results = client.search(searchText, searchOptions, requestOptions);
        results.log().blockFirst();
    }

    private void assertHotelIdSequenceEqual(List<String> expectedIds, Flux<SearchResult> results) {
        assertNotNull(results);
        StepVerifier.FirstStep<SearchResult> sv = StepVerifier.create(results);
        expectedIds.forEach(k -> sv.assertNext(res -> assertEquals(k, getSearchResultId(res, "HotelId"))));
        sv.verifyComplete();
    }

}
