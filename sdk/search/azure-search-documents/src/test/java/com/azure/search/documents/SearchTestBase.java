// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.RangeFacetResult;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SynonymMap;
import com.azure.search.documents.models.ValueFacetResult;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for all Search API tests
 */
public abstract class SearchTestBase extends SearchIndexClientTestBase {

    static final String HOTELS_INDEX_NAME = "hotels";
    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";
    static final String HOTELS_DATA_JSON_WITHOUT_FR_DESCRIPTION = "HotelsDataArrayWithoutFr.json";

    protected List<Map<String, Object>> hotels;

    List<Map<String, Object>> createHotelsList(int count) {
        List<Map<String, Object>> documents = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Map<String, Object> doc = new HashMap<>();

            doc.put("HotelId", Integer.toString(i));
            doc.put("HotelName", "Hotel" + i);
            doc.put("Description", "Desc" + i);
            doc.put("Description_fr", "Desc_fr" + i);
            doc.put("Category", "Catg" + i);
            doc.put("Tags", Collections.singletonList("tag" + i));
            doc.put("ParkingIncluded", false);
            doc.put("SmokingAllowed", false);
            doc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00Z"));
            doc.put("Rating", i);

            documents.add(doc);
        }
        return documents;
    }

    boolean compareResults(List<Map<String, Object>> searchResults, List<Map<String, Object>> hotels) {
        Iterator<Map<String, Object>> searchIterator = searchResults.iterator();
        Iterator<Map<String, Object>> hotelsIterator = hotels.iterator();
        while (searchIterator.hasNext() && hotelsIterator.hasNext()) {
            Map<String, Object> result = searchIterator.next();
            Map<String, Object> hotel = hotelsIterator.next();

            hotel.entrySet().forEach(e -> checkEquals(result, e));
        }

        return true;
    }

    private void checkEquals(Map<String, Object> result, Map.Entry<String, Object> hotel) {
        if (hotel.getValue() != null && result.get(hotel.getKey()) != null) {
            assertObjectEquals(result.get(hotel.getKey()), hotel.getValue());
        }
    }

    <T> void assertRangeFacets(List<RangeFacetResult<T>> baseRateFacets, List<RangeFacetResult<T>> lastRenovationDateFacets) {
        assertNull(baseRateFacets.get(0).getFrom());
        assertEquals(5.0, baseRateFacets.get(0).getTo());
        assertEquals(5.0, baseRateFacets.get(1).getFrom());
        assertEquals(8.0, baseRateFacets.get(1).getTo());
        assertEquals(8.0, baseRateFacets.get(2).getFrom());
        assertEquals(10.0, baseRateFacets.get(2).getTo());
        assertEquals(10.0, baseRateFacets.get(3).getFrom());
        assertNull(baseRateFacets.get(3).getTo());

        assertEquals(1, baseRateFacets.get(0).getCount().intValue());
        assertEquals(1, baseRateFacets.get(1).getCount().intValue());
        assertEquals(1, baseRateFacets.get(2).getCount().intValue());
        assertEquals(0, baseRateFacets.get(3).getCount().intValue());

        assertNull(lastRenovationDateFacets.get(0).getFrom());
        assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(0).getTo());
        assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(1).getFrom());
        assertNull(lastRenovationDateFacets.get(1).getTo());

        assertEquals(5, lastRenovationDateFacets.get(0).getCount().intValue());
        assertEquals(2, lastRenovationDateFacets.get(1).getCount().intValue());
    }

    <T> List<RangeFacetResult<T>> getRangeFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(RangeFacetResult<T>::new).collect(Collectors.toList());
    }

    <T> List<ValueFacetResult<T>> getValueFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(ValueFacetResult<T>::new)
            .collect(Collectors.toList());
    }

    private List<FacetResult> getFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        assertTrue(facets.containsKey(expectedField));
        List<FacetResult> results = facets.get(expectedField);
        assertEquals(expectedCount, results.size());
        return results;
    }

    void assertContainHotelIds(List<Map<String, Object>> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.getDocument().containsKey("HotelId"))
            .map(item -> (String) item.getDocument().get("HotelId")).collect(Collectors.toList());
        List<String> expectedKeys = expected.stream().filter(item -> item.containsKey("HotelId"))
            .map(item -> (String) item.get("HotelId")).collect(Collectors.toList());
        assertEquals(expectedKeys, actualKeys);
    }

    <T> void assertValueFacetsEqual(List<ValueFacetResult<T>> actualFacets, ArrayList<ValueFacetResult<T>> expectedFacets) {
        assertEquals(expectedFacets.size(), actualFacets.size());
        for (int i = 0; i < actualFacets.size(); i++) {
            assertEquals(expectedFacets.get(i).getCount(), actualFacets.get(i).getCount());
            assertEquals(expectedFacets.get(i).getValue(), actualFacets.get(i).getValue());
        }
    }

    String getSearchResultId(SearchResult searchResult, String idFieldName) {
        return searchResult.getDocument().get(idFieldName).toString();
    }

    SearchOptions getSearchOptionsForRangeFacets() {
        return new SearchOptions().setFacets("Rooms/BaseRate,values:5|8|10",
            "LastRenovationDate,values:2000-01-01T00:00:00Z");
    }

    SearchOptions getSearchOptionsForValueFacets() {
        return new SearchOptions().setFacets("Rating,count:2,sort:-value",
            "SmokingAllowed,sort:count",
            "Category",
            "LastRenovationDate,interval:year",
            "Rooms/BaseRate,sort:value",
            "Tags,sort:value");
    }

    void prepareHotelsSynonymMap(String name, String synonyms, String fieldName) {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchServiceClient searchServiceClient = getSearchServiceClientBuilder().buildClient();

            // Create a new SynonymMap
            searchServiceClient.createSynonymMap(new SynonymMap()
                .setName(name)
                .setSynonyms(synonyms));

            // Attach index field to SynonymMap
            Index hotelsIndex = searchServiceClient.getIndex(HOTELS_INDEX_NAME);
            hotelsIndex.getFields().stream()
                .filter(f -> fieldName.equals(f.getName()))
                .findFirst().get().setSynonymMaps(Collections.singletonList(name));

            // Update the index with the SynonymMap
            searchServiceClient.createOrUpdateIndex(hotelsIndex);

            // Wait for the index to update with the SynonymMap
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void assertListEqualHotelIds(List<String> expected, List<SearchResult> actual) {
        assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.getDocument().containsKey("HotelId"))
            .map(item -> (String) item.getDocument().get("HotelId")).collect(Collectors.toList());
        assertEquals(expected, actualKeys);
    }

    String createIndexWithNonNullableTypes() {
        Index index = new Index()
            .setName("non-nullable-index")
            .setFields(Arrays.asList(
                new Field()
                    .setName("Key")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
                    .setRetrievable(true),
                new Field()
                    .setName("Rating")
                    .setType(DataType.EDM_INT32)
                    .setRetrievable(true),
                new Field()
                    .setName("Count")
                    .setType(DataType.EDM_INT64)
                    .setRetrievable(true),
                new Field()
                    .setName("IsEnabled")
                    .setType(DataType.EDM_BOOLEAN)
                    .setRetrievable(true),
                new Field()
                    .setName("Ratio")
                    .setType(DataType.EDM_DOUBLE)
                    .setRetrievable(true),
                new Field()
                    .setName("StartDate")
                    .setType(DataType.EDM_DATE_TIME_OFFSET)
                    .setRetrievable(true),
                new Field()
                    .setName("EndDate")
                    .setType(DataType.EDM_DATE_TIME_OFFSET)
                    .setRetrievable(true),
                new Field()
                    .setName("TopLevelBucket")
                    .setType(DataType.EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("BucketName")
                            .setType(DataType.EDM_STRING)
                            .setFilterable(true),
                        new Field()
                            .setName("Count")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(true))),
                new Field()
                    .setName("Buckets")
                    .setType(DataType.collection(DataType.EDM_COMPLEX_TYPE))
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("BucketName")
                            .setType(DataType.EDM_STRING)
                            .setFilterable(true),
                        new Field()
                            .setName("Count")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(true)))));

        setupIndex(index);

        return index.getName();
    }

    String createIndexWithValueTypes() {
        Index index = new Index()
            .setName("testindex")
            .setFields(Arrays.asList(
                new Field()
                    .setName("Key")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
                    .setSearchable(true),
                new Field()
                    .setName("IntValue")
                    .setType(DataType.EDM_INT32)
                    .setFilterable(true),
                new Field()
                    .setName("Bucket")
                    .setType(DataType.EDM_COMPLEX_TYPE)
                    .setFields(Arrays.asList(
                        new Field()
                            .setName("BucketName")
                            .setType(DataType.EDM_STRING)
                            .setFilterable(true),
                        new Field()
                            .setName("Count")
                            .setType(DataType.EDM_INT32)
                            .setFilterable(true)
                    ))
                )
            );

        setupIndex(index);

        return index.getName();
    }

    @SuppressWarnings({"cast"})
    List<Map<String, Object>> createDocsListWithValueTypes() {
        return Arrays.asList(
            Stream.of(new Object[][]{
                {"Key", "123"},
                {"IntValue", 0},
                {"Bucket", (Map<String, Object>) Stream.of(new Object[][]{
                    {"BucketName", "A"},
                    {"Count", 3}
                }).collect(Collectors.toMap(b -> (String) b[0], b -> (Object) b[1]))}
            }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1])),
            Stream.of(new Object[][]{
                {"Key", "456"},
                {"IntValue", 7},
                {"Bucket", (Map<String, Object>) Stream.of(new Object[][]{
                    {"BucketName", "B"},
                    {"Count", 5}
                }).collect(Collectors.toMap(b -> (String) b[0], b -> (Object) b[1]))}
            }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1])),
            Stream.of(new Object[][]{
                {"Key", "789"},
                {"IntValue", 1},
                {"Bucket", (Map<String, Object>) Stream.of(new Object[][]{
                    {"BucketName", "B"},
                    {"Count", 99}
                }).collect(Collectors.toMap(b -> (String) b[0], b -> (Object) b[1]))}
            }).collect(Collectors.toMap(data -> (String) data[0], data -> (Object) data[1]))
        );
    }

    @Test
    public void searchThrowsWhenRequestIsMalformed() {
        SearchOptions invalidSearchOptions = new SearchOptions().setFilter("This is not a valid filter.");

        assertHttpResponseException(
            () -> search("*", invalidSearchOptions, new RequestOptions()),
            HttpResponseStatus.BAD_REQUEST,
            "Invalid expression: Syntax error at position 7 in 'This is not a valid filter.'");
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescaped() {
        SearchOptions invalidSearchOptions = new SearchOptions().setQueryType(QueryType.FULL);

        assertHttpResponseException(
            () -> search("/.*/.*/", invalidSearchOptions, new RequestOptions()),
            HttpResponseStatus.BAD_REQUEST,
            "Failed to parse query string at line 1, column 8.");
    }

    @Test
    public abstract void canSearchDynamicDocuments() throws IOException;

    @Test
    public abstract void canContinueSearch();

    @Test
    public abstract void canContinueSearchWithTop();

    @Test
    public abstract void canSearchWithSelectedFields() throws IOException;

    @Test
    public abstract void canUseTopAndSkipForClientSidePaging() throws IOException;

    @Test
    public abstract void canFilterNonNullableType() throws Exception;

    @Test
    public abstract void searchWithoutOrderBySortsByScore() throws IOException;

    @Test
    public abstract void orderByProgressivelyBreaksTies() throws IOException;

    @Test
    public abstract void canFilter() throws IOException;

    @Test
    public abstract void canSearchWithRangeFacets() throws IOException;

    @Test
    public abstract void canSearchWithLuceneSyntax() throws IOException;

    @Test
    public abstract void canSearchWithValueFacets() throws IOException;

    @Test
    public abstract void canSearchWithSearchModeAll() throws IOException;

    @Test
    public abstract void defaultSearchModeIsAny() throws IOException;

    @Test
    public abstract void canGetResultCountInSearch() throws IOException;

    @Test
    public abstract void canSearchWithRegex() throws IOException;

    @Test
    public abstract void canSearchWithEscapedSpecialCharsInRegex() throws IOException;

    @Test
    public abstract void canSearchWithMinimumCoverage() throws IOException;

    @Test
    public abstract void searchWithScoringProfileBoostsScore() throws IOException;

    @Test
    public abstract void canUseHitHighlighting() throws IOException;

    @Test
    public abstract void canSearchStaticallyTypedDocuments() throws IOException;

    @Test
    public abstract void canRoundTripNonNullableValueTypes() throws Exception;

    @Test
    public abstract void canSearchWithDateInStaticModel() throws ParseException, IOException;

    @Test
    public abstract void canSearchWithSynonyms() throws IOException;

    abstract void search(String searchText, SearchOptions searchOptions, RequestOptions requestOptions);
}
