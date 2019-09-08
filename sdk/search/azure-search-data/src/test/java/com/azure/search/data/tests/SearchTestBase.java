// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.tests;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.customization.RangeFacetResult;
import com.azure.search.data.customization.ValueFacetResult;
import com.azure.search.data.customization.models.CoordinateSystem;
import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.env.SearchIndexService;
import com.azure.search.data.generated.models.FacetResult;
import com.azure.search.data.generated.models.QueryType;
import com.azure.search.data.generated.models.SearchParameters;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.generated.models.SearchResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

/**
 * Abstract base class for all Search API tests
 */
public abstract class SearchTestBase extends SearchIndexClientTestBase {

    static final String HOTELS_INDEX_NAME = "hotels";
    static final String HOTELS_DATA_JSON = "HotelsDataArray.json";
    private static final String SEARCH_SCORE_FIELD = "@search.score";
    private static final String MODEL_WITH_VALUE_TYPES_INDEX_JSON = "ModelWithValueTypesIndexData.json";
    static final String MODEL_WITH_VALUE_TYPES_DOCS_JSON = "ModelWithValueTypesDocsData.json";
    static final String MODEL_WITH_INDEX_TYPES_INDEX_NAME = "testindex";
    private static final String NON_NULLABLE_INDEX_JSON = "NonNullableIndexData.json";
    static final String NON_NULLABLE_INDEX_NAME = "non-nullable-index";

    protected List<Map<String, Object>> hotels;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

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
            doc.put("LastRenovationDate", "2010-06-27T00:00:00Z");
            doc.put("Rating", i);

            documents.add(doc);
        }
        return documents;
    }

    /**
     * Drop fields that shouldn't be in the returned object
     *
     * @param map the map to drop items from
     * @return the new map
     */
    static Map<String, Object> dropUnnecessaryFields(Map<String, Object> map) {
        map.remove(SEARCH_SCORE_FIELD);
        return map;
    }

    boolean compareResults(List<Map<String, Object>> searchResults, List<Map<String, Object>> hotels) {
        Iterator<Map<String, Object>> searchIterator = searchResults.iterator();
        Iterator<Map<String, Object>> hotelsIterator = hotels.iterator();
        while (searchIterator.hasNext() && hotelsIterator.hasNext()) {
            Map<String, Object> result = searchIterator.next();
            Map<String, Object> hotel = hotelsIterator.next();

            Map loc = (Map) hotel.get("Location");
            if (loc != null) {
                loc.put("crs", CoordinateSystem.create().createObjectMap());
            }
            assertTrue(hotel.entrySet().stream().allMatch(e -> checkEquals(e, result)));
        }

        return true;
    }

    private boolean checkEquals(Map.Entry<String, Object> hotel, Map<String, Object> result) {
        if (hotel.getValue() != null) {
            return hotel.getValue().equals(result.get(hotel.getKey()));
        }
        return true;
    }

    void assertRangeFacets(List<RangeFacetResult> baseRateFacets, List<RangeFacetResult> lastRenovationDateFacets) {
        Assert.assertNull(baseRateFacets.get(0).from());
        Assert.assertEquals(5.0, baseRateFacets.get(0).to());
        Assert.assertEquals(5.0, baseRateFacets.get(1).from());
        Assert.assertEquals(8.0, baseRateFacets.get(1).to());
        Assert.assertEquals(8.0, baseRateFacets.get(2).from());
        Assert.assertEquals(10.0, baseRateFacets.get(2).to());
        Assert.assertEquals(10.0, baseRateFacets.get(3).from());
        Assert.assertNull(baseRateFacets.get(3).to());

        Assert.assertEquals(1, baseRateFacets.get(0).count());
        Assert.assertEquals(1, baseRateFacets.get(1).count());
        Assert.assertEquals(1, baseRateFacets.get(2).count());
        Assert.assertEquals(0, baseRateFacets.get(3).count());

        Assert.assertNull(lastRenovationDateFacets.get(0).from());
        Assert.assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(0).to());
        Assert.assertEquals("2000-01-01T00:00:00.000+0000", lastRenovationDateFacets.get(1).from());
        Assert.assertNull(lastRenovationDateFacets.get(1).to());

        Assert.assertEquals(5, lastRenovationDateFacets.get(0).count());
        Assert.assertEquals(2, lastRenovationDateFacets.get(1).count());
    }

    List<RangeFacetResult> getRangeFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(facetResult -> new RangeFacetResult(facetResult)).collect(Collectors.toList());
    }

    List<ValueFacetResult> getValueFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        List<FacetResult> facetCollection = getFacetsForField(facets, expectedField, expectedCount);
        return facetCollection.stream().map(facetResult -> new ValueFacetResult(facetResult)).collect(Collectors.toList());
    }

    List<FacetResult> getFacetsForField(
        Map<String, List<FacetResult>> facets, String expectedField, int expectedCount) {
        Assert.assertTrue(facets.containsKey(expectedField));
        List<FacetResult> results = facets.get(expectedField);
        Assert.assertEquals(expectedCount, results.size());
        return results;
    }

    void assertContainKeys(List<Map<String, Object>> expected, List<SearchResult> actual) {
        Assert.assertNotNull(actual);
        List<String> actualKeys = actual.stream().filter(item -> item.additionalProperties().containsKey("HotelId"))
            .map(item -> (String) item.additionalProperties().get("HotelId")).collect(Collectors.toList());
        List<String> expectedKeys = expected.stream().filter(item -> item.containsKey("HotelId"))
            .map(item -> (String) item.get("HotelId")).collect(Collectors.toList());
        Assert.assertEquals(expectedKeys, actualKeys);
    }

    void assertValueFacetsEqual(List<ValueFacetResult> actualFacets, ArrayList<ValueFacetResult> expectedFacets) {
        Assert.assertEquals(expectedFacets.size(), actualFacets.size());
        for (int i = 0; i < actualFacets.size(); i++) {
            Assert.assertEquals(expectedFacets.get(i).count(), actualFacets.get(i).count());
            Assert.assertEquals(expectedFacets.get(i).value(), actualFacets.get(i).value());
        }
    }

    String getSearchResultId(SearchResult searchResult, String idFieldName) {
        return searchResult.additionalProperties().get(idFieldName).toString();
    }

    SearchParameters getSearchParametersForRangeFacets() {
        return new SearchParameters().facets(Arrays.asList(
            "Rooms/BaseRate,values:5|8|10",
            "LastRenovationDate,values:2000-01-01T00:00:00Z"));
    }

    SearchParameters getSearchParametersForValueFacets() {
        return new SearchParameters().facets(Arrays.asList(
            "Rating,count:2,sort:-value",
            "SmokingAllowed,sort:count",
            "Category",
            "LastRenovationDate,interval:year",
            "Rooms/BaseRate,sort:value",
            "Tags,sort:value"));
    }

    void createIndexForModelWithValueTypesTest() throws Exception {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                MODEL_WITH_VALUE_TYPES_INDEX_JSON, searchServiceName, apiKey);
            searchIndexService.initialize();
        }
    }

    void createIndexForNonNullableTest() throws Exception {
        if (!interceptorManager.isPlaybackMode()) {
            // In RECORDING mode (only), create a new index:
            SearchIndexService searchIndexService = new SearchIndexService(
                NON_NULLABLE_INDEX_JSON, searchServiceName, apiKey);
            searchIndexService.initialize();
        }
    }

    @Test
    public void searchThrowsWhenRequestIsMalformed() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Invalid expression: Syntax error at position 7 in 'This is not a valid filter.'");

        SearchParameters invalidSearchParameters = new SearchParameters()
            .filter("This is not a valid filter.");

        search("*", invalidSearchParameters, new SearchRequestOptions());
    }

    @Test
    public void searchThrowsWhenSpecialCharInRegexIsUnescaped() {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("Failed to parse query string at line 1, column 8.");

        SearchParameters invalidSearchParameters = new SearchParameters()
            .queryType(QueryType.FULL);

        search("/.*/.*/", invalidSearchParameters, new SearchRequestOptions());
    }

    @Test
    public abstract void canSearchDynamicDocuments();

    @Test
    public abstract void canSearchWithSelectedFields();

    @Test
    public abstract void canUseTopAndSkipForClientSidePaging();

    @Test
    public abstract void canFilterNonNullableType() throws Exception;

    @Test
    public abstract void searchWithoutOrderBySortsByScore();

    @Test
    public abstract void orderByProgressivelyBreaksTies();

    @Test
    public abstract void canFilter();

    @Test
    public abstract void canSearchWithRangeFacets();

    @Test
    public abstract void canSearchWithLuceneSyntax();

    @Test
    public abstract void canSearchWithValueFacets();

    @Test
    public abstract void canSearchWithSearchModeAll();

    @Test
    public abstract void defaultSearchModeIsAny();

    @Test
    public abstract void canGetResultCountInSearch();

    @Test
    public abstract void canSearchWithRegex();

    @Test
    public abstract void canSearchWithEscapedSpecialCharsInRegex();

    @Test
    public abstract void canSearchWithMinimumCoverage();

    @Test
    public abstract void searchWithScoringProfileBoostsScore();

    @Test
    public abstract void canUseHitHighlighting();

    @Test
    public abstract void canSearchStaticallyTypedDocuments();

    @Test
    public abstract void canRoundTripNonNullableValueTypes() throws Exception;

    @Test
    public abstract void canSearchWithDateInStaticModel() throws ParseException;

    abstract void search(String searchText, SearchParameters searchParameters, SearchRequestOptions searchRequestOptions);

    abstract void initializeClient();

    protected abstract void setIndexName(String indexName);
}
