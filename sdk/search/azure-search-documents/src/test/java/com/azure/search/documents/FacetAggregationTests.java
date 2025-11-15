package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.models.FacetResult;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SemanticSearchOptions;
import com.azure.search.documents.util.SearchPagedIterable;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.search.documents.TestHelpers.loadResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    public void facetRequestSerializationWithAllMetrics() {
        SearchOptions searchOptions = new SearchOptions().setFacets("Rating, metric: min", "Rating, metric: max",
            "Rating, metric: avg", "Rating, metric: sum", "Category, metric: cardinality");

        String serialized = BinaryData.fromObject(searchOptions).toString();
        assertTrue(serialized.contains("Rating, metric: min"), "Should serialize min metric");
        assertTrue(serialized.contains("Rating, metric: max"), "Should serialize max metric");
        assertTrue(serialized.contains("Rating, metric: avg"), "Should serialize avg metric");
        assertTrue(serialized.contains("Rating, metric: sum"), "Should serialize sum metric");
        assertTrue(serialized.contains("Category, metric: cardinality"), "Should serialize cardinality metric");
    }

    @Test
    public void facetRequestSerializationWithMultipleMetricsOnSameField() {

        List<String> facets = Arrays.asList("Rating, metric: min", "Rating, metric: max", "Rating, metric: avg");

        SearchOptions searchOptions = new SearchOptions().setFacets(facets.toArray(new String[0]));

        List<String> serializedFacets = searchOptions.getFacets();
        assertNotNull(serializedFacets, "Facets should not be null");
        assertEquals(serializedFacets.size(), 3, "Facet size should be 3");

        assertTrue(serializedFacets.contains("Rating, metric: min"), "Should include min metric");
        assertTrue(serializedFacets.contains("Rating, metric: max"), "Should include max metric");
        assertTrue(serializedFacets.contains("Rating, metric: avg"), "Should include avg metric");
    }

    @Test
    public void facetQueryWithMinAggregation() {
        SearchOptions searchOptions = new SearchOptions().setFacets(("Rating, metric : min"));

        SearchPagedIterable results = searchClient.search("*", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets, "Facets should not be null");
        assertTrue(facets.containsKey("Rating"), "Rating facet should be present");

        List<FacetResult> ratingFacets = facets.get("Rating");
        assertNotNull(ratingFacets, "Rating facet results should not be null");

        boolean hasMinMetric = ratingFacets.stream().anyMatch(facet -> facet.getMin() != null);
        assertTrue(hasMinMetric, "Min metric should be present in facets response");
    }

    @Test
    public void facetQueryWithMaxAggregation() {
        SearchOptions searchOptions = new SearchOptions().setFacets(("Rating, metric : max"));

        SearchPagedIterable results = searchClient.search("*", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets, "Facets should not be null");
        assertTrue(facets.containsKey("Rating"), "Rating facet should be present");

        List<FacetResult> ratingFacets = facets.get("Rating");
        assertNotNull(ratingFacets, "Rating facet results should not be null");

        boolean hasMaxMetric = ratingFacets.stream().anyMatch(facet -> facet.getMax() != null);
        assertTrue(hasMaxMetric, "Max metric should be present in facets response");
    }

    @Test
    public void facetQueryWithAvgAggregation() {
        SearchOptions searchOptions = new SearchOptions().setFacets(("Rating, metric : avg"));

        SearchPagedIterable results = searchClient.search("*", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets, "Facets should not be null");
        assertTrue(facets.containsKey("Rating"), "Rating facet should be present");

        List<FacetResult> ratingFacets = facets.get("Rating");
        assertNotNull(ratingFacets, "Rating facet results should not be null");

        boolean hasAvgMetric = ratingFacets.stream().anyMatch(facet -> facet.getAvg() != null);
        assertTrue(hasAvgMetric, "Avg metric should be present in facets response");
    }

    @Test
    public void facetQueryWithCardinalityAggregation() {
        SearchOptions searchOptions = new SearchOptions().setFacets(("Category, metric : cardinality"));

        SearchPagedIterable results = searchClient.search("*", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets, "Facets should not be null");
        assertTrue(facets.containsKey("Category"), "Category facet should be present");

        List<FacetResult> categoryFacets = facets.get("Category");
        assertNotNull(categoryFacets, "Category facet results should not be null");

        boolean hasCardinalityMetric = categoryFacets.stream().anyMatch(facet -> facet.getCardinality() != null);
        assertTrue(hasCardinalityMetric, "Cardinality metric should be present in facets response");
    }

    @Test
    public void facetQueryWithMultipleMetricsOnSameFieldResponseShape() {
        SearchOptions searchOptions
            = new SearchOptions().setFacets("Rating, metric: min", "Rating, metric: max", "Rating, metric: avg");

        SearchPagedIterable results = searchClient.search("*", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets);
        assertTrue(facets.containsKey("Rating"));

        List<FacetResult> ratingFacets = facets.get("Rating");

        boolean hasMin = ratingFacets.stream().anyMatch(f -> f.getMin() != null);
        boolean hasMax = ratingFacets.stream().anyMatch(f -> f.getMax() != null);
        boolean hasAvg = ratingFacets.stream().anyMatch(f -> f.getAvg() != null);

        assertTrue(hasMin, "Min metric should be present");
        assertTrue(hasMax, "Max metric should be present");
        assertTrue(hasAvg, "Avg metric should be present");
    }

    @Test
    public void facetQueryWithCardinalityPrecisionThreshold() {
        SearchOptions defaultThreshold = new SearchOptions().setFacets("Category, metric : cardinality");

        SearchOptions maxThreshold
            = new SearchOptions().setFacets("Category, metric : cardinality, precisionThreshold: 40000");

        SearchPagedIterable defaultResults = searchClient.search("*", defaultThreshold, null);
        SearchPagedIterable maxResults = searchClient.search("*", maxThreshold, null);

        assertNotNull(defaultResults.getFacets().get("Category"));
        assertNotNull(maxResults.getFacets().get("Category"));

        boolean defaultHasCardinality
            = defaultResults.getFacets().get("Category").stream().anyMatch(f -> f.getCardinality() != null);
        boolean maxHasCardinality
            = maxResults.getFacets().get("Category").stream().anyMatch(f -> f.getCardinality() != null);

        assertTrue(defaultHasCardinality, "Default threshold should return cardinality");
        assertTrue(maxHasCardinality, "Max threshold should return cardinality");
    }

    @Test
    public void facetMetricsWithSemanticQuery() {
        SearchOptions searchOptions = new SearchOptions()
            .setFacets("Rating, metric: min", "Rating, metric: max", "Category, metric: cardinality")
            .setQueryType(QueryType.SEMANTIC)
            .setSemanticSearchOptions(new SemanticSearchOptions().setSemanticConfigurationName("semantic-config"));
        SearchPagedIterable results = searchClient.search("luxury hotel", searchOptions, null);
        Map<String, List<FacetResult>> facets = results.getFacets();

        assertNotNull(facets, "Facets should not be null");
        assertTrue(facets.containsKey("Rating"), "Rating facet should be present");
        assertTrue(facets.containsKey("Category"), "Category facet should be present");

        boolean hasRatingMetrics
            = facets.get("Rating").stream().anyMatch(facet -> facet.getMin() != null || facet.getMax() != null);
        boolean hasCategoryMetrics = facets.get("Category").stream().anyMatch(facet -> facet.getCardinality() != null);

        assertTrue(hasRatingMetrics, "Rating metrics should work with semantic query");
        assertTrue(hasCategoryMetrics, "Category metrics should work with semantic query");
    }

    @Test
    public void facetMetricsApiVersionCompatibility() {
        SearchClient prevVersionClient = new SearchClientBuilder().endpoint(SEARCH_ENDPOINT)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(TestHelpers.getTestTokenCredential())
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .indexName(HOTEL_INDEX_NAME)
            .serviceVersion(SearchServiceVersion.V2025_09_01)
            .buildClient();

        SearchOptions searchOptions = new SearchOptions().setFacets("Rating, metric: min");

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            prevVersionClient.search("*", searchOptions, null).iterator().hasNext();
        });

        assertEquals(400, exception.getResponse().getStatusCode(), "Should return 400 Bad Request");
        assertTrue(
            exception.getMessage().contains("'metric' faceting") || exception.getMessage().contains("not supported"),
            "Should fail due to unsupported facet metrics in previous API version");

    }

    private static SearchIndexClient setupIndex() {
        try (JsonReader jsonReader = JsonProviders.createReader(loadResource(HOTELS_TESTS_INDEX_DATA_JSON))) {
            SearchIndex baseIndex = SearchIndex.fromJson(jsonReader);

            SearchIndexClient searchIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
                .credential(TestHelpers.getTestTokenCredential())
                .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
                .buildClient();

            List<SemanticConfiguration> semanticConfigurations
                = Collections.singletonList(new SemanticConfiguration("semantic-config",
                    new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                        .setContentFields(new SemanticField("Description"))
                        .setKeywordsFields(new SemanticField("Category"))));
            SemanticSearch semanticSearch = new SemanticSearch().setDefaultConfigurationName("semantic-config")
                .setConfigurations(semanticConfigurations);
            searchIndexClient.createOrUpdateIndex(
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

            searchClient.uploadDocuments(hotels);

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
        if (rating != null)
            hotel.put("Rating", rating);
        if (category != null)
            hotel.put("Category", category);
        return hotel;
    }

}
