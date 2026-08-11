// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.implementation.SearchUtils;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.SearchOptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class FacetAggregationTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "facet-aggregation-test-index";
    private static SearchIndexClient searchIndexClient;
    private static SearchClient searchClient;

    @BeforeAll
    public static void setupClass() {
        // Set up any necessary configurations or resources before all tests.
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupIndex();
        searchClient = new SearchClientBuilder().endpoint(SEARCH_ENDPOINT)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(TestHelpers.getTestTokenCredential())
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .indexName(HOTEL_INDEX_NAME)
            .buildClient();

        uploadTestDocuments();

    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK && searchIndexClient != null) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);
        }
    }

    @Test
    public void facetRequestSerializationWithAllMetrics() throws IOException {
        SearchOptions searchOptions = new SearchOptions().setFacets("Rating, metric: min", "Rating, metric: max",
            "Rating, metric: avg", "Rating, metric: sum", "Category, metric: cardinality");

        String serialized = SearchUtils.fromSearchOptions(searchOptions).toJsonString();
        assertTrue(serialized.contains("Rating, metric: min"), "Should serialize min metric");
        assertTrue(serialized.contains("Rating, metric: max"), "Should serialize max metric");
        assertTrue(serialized.contains("Rating, metric: avg"), "Should serialize avg metric");
        assertTrue(serialized.contains("Rating, metric: sum"), "Should serialize sum metric");
        assertTrue(serialized.contains("Category, metric: cardinality"), "Should serialize cardinality metric");
    }

    @Test
    public void facetRequestSerializationWithMultipleMetricsOnSameField() {
        SearchOptions searchOptions
            = new SearchOptions().setFacets("Rating, metric: min", "Rating, metric: max", "Rating, metric: avg");

        List<String> serializedFacets = searchOptions.getFacets();
        assertNotNull(serializedFacets, "Facets should not be null");
        assertEquals(3, serializedFacets.size(), "Facet size should be 3");

        assertTrue(serializedFacets.contains("Rating, metric: min"), "Should include min metric");
        assertTrue(serializedFacets.contains("Rating, metric: max"), "Should include max metric");
        assertTrue(serializedFacets.contains("Rating, metric: avg"), "Should include avg metric");
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithMinAggregation() {
        // Disabled: FacetResult.getMin() was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithMaxAggregation() {
        // Disabled: FacetResult.getMax() was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithAvgAggregation() {
        // Disabled: FacetResult.getAvg() was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithCardinalityAggregation() {
        // Disabled: FacetResult.getCardinality() was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithMultipleMetricsOnSameFieldResponseShape() {
        // Disabled: FacetResult.getMin/getMax/getAvg() were removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetQueryWithCardinalityPrecisionThreshold() {
        // Disabled: FacetResult.getCardinality() was removed in the 2026-04-01 API version.
    }

    @Test
    @Disabled("FacetResult.getMin/getMax/getAvg/getCardinality removed in 2026-04-01 API version")
    public void facetMetricsWithSemanticQuery() {
        // Disabled: FacetResult.getMin/getMax/getCardinality() were removed in the 2026-04-01 API version.
    }

    //    @Test
    //    @Disabled("Issues with responses based on record or playback mode")
    //    public void facetMetricsApiVersionCompatibility() {
    //        SearchClient prevVersionClient
    //            = getSearchClientBuilder(HOTEL_INDEX_NAME, true).serviceVersion(SearchServiceVersion.V2025_09_01)
    //                .buildClient();
    //
    //        SearchOptions searchOptions = new SearchOptions().setFacets("Rating, metric: min");
    //
    //        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> prevVersionClient.search("*", searchOptions, null).iterator().hasNext());
    //
    //        int statusCode = exception.getResponse().getStatusCode();
    //        assertTrue(statusCode == 400 || statusCode == 401, "Should return 400 Bad Request or 401 Unauthorized");
    //        assertTrue(
    //            exception.getMessage().contains("'metric' faceting")
    //                || exception.getMessage().contains("not supported")
    //                || exception.getMessage().contains("401"),
    //            "Should fail due to unsupported facet metrics in previous API version");
    //    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            SemanticConfiguration semanticConfigurations = new SemanticConfiguration("semantic-config",
                new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                    .setContentFields(new SemanticField("Description"))
                    .setKeywordsFields(new SemanticField("Category")));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfigurations);
            searchIndexClient.createIndex(
                TestHelpers.createTestIndex(HOTEL_INDEX_NAME, baseIndex).setSemanticSearch(semanticSearch));

            return searchIndexClient;
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private static void uploadTestDocuments() {
        try {
            // Upload diverse test data to properly test aggregations
            List<Map<String, Object>> hotels = Arrays.asList(createHotel("1", 4, "Luxury", "Grand Hotel"),
                createHotel("2", 3, "Budget", "Economy Inn"), createHotel("3", 4, "Business", "Business Center Hotel"),
                createHotel("4", 5, "Resort", "Paradise Resort"), createHotel("5", 2, "Budget", "Basic Hotel"),
                createHotel("6", null, "Boutique", "Missing Rating Hotel"), // Missing rating for default value testing
                createHotel("7", 4, null, "Missing Category Hotel") // Missing category for default value testing
            );

            searchClient.index(new IndexDocumentsBatch(hotels.stream()
                .map(hotel -> new IndexAction().setActionType(IndexActionType.UPLOAD).setAdditionalProperties(hotel))
                .collect(Collectors.toList())));

            // Wait for indexing to complete
            Thread.sleep(3000);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload test documents", e);
        }
    }

    private static Map<String, Object> createHotel(String id, Integer rating, String category, String name) {
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("HotelId", id);
        hotel.put("HotelName", name);
        hotel.put("Description", "Test hotel for facet aggregation testing");
        if (rating != null) {
            hotel.put("Rating", rating);
        }
        if (category != null) {
            hotel.put("Category", category);
        }
        return hotel;
    }

}
