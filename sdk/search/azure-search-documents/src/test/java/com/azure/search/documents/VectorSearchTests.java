// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.implementation.util.SearchPagedResponseAccessHelper;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.DistanceScoringFunction;
import com.azure.search.documents.indexes.models.DistanceScoringParameters;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import com.azure.search.documents.models.QueryAnswer;
import com.azure.search.documents.models.QueryAnswerType;
import com.azure.search.documents.models.QueryCaption;
import com.azure.search.documents.models.QueryCaptionType;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchResult;
import com.azure.search.documents.models.SemanticSearchOptions;
import com.azure.search.documents.models.VectorSearchOptions;
import com.azure.search.documents.models.VectorizedQuery;
import com.azure.search.documents.test.environment.models.HotelAddress;
import com.azure.search.documents.test.environment.models.HotelRoom;
import com.azure.search.documents.test.environment.models.VectorHotel;
import com.azure.search.documents.util.SearchPagedResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests Vector search functionality.
 */
@Execution(ExecutionMode.CONCURRENT)
public class VectorSearchTests extends SearchTestBase {
    private static void assertKeysEqual(List<SearchResult> results, Function<SearchResult, String> keyAccessor,
                                        String[] expectedKeys) {
        assertArrayEquals(expectedKeys, results.stream().map(keyAccessor).toArray());
    }

    private static final String HOTEL_INDEX_NAME = "azsearch-vector-shared-hotel-instance";
    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .serviceVersion(SearchServiceVersion.V2023_11_01)
            .credential(TestHelpers.getTestTokenCredential())
            .retryPolicy(SERVICE_THROTTLE_SAFE_RETRY_POLICY)
            .buildClient();

        searchIndexClient.createIndex(getVectorIndex());

        searchIndexClient.getSearchClient(HOTEL_INDEX_NAME).uploadDocuments(VECTORIZED_HOTELS);

        waitForIndexing();
    }

    @AfterAll
    protected static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private final List<String> indexesToDelete = new ArrayList<>();

    @AfterEach
    public void deleteIndexes() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            for (String index : indexesToDelete) {
                searchIndexClient.deleteIndex(index);
            }
        }
    }

    @Test
    public void singleVectorSearchAsync() {
        SearchAsyncClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName");

        StepVerifier.create(searchClient.search(null, searchOptions).collectList())
            .assertNext(results -> assertKeysEqual(results,
                r -> (String) r.getDocument(SearchDocument.class).get("HotelId"), new String[]{"3", "5", "1"}))
            .verifyComplete();
    }

    @Test
    public void singleVectorSearchSync() {
        SearchClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName");

        List<SearchResult> results = searchClient.search(null, searchOptions, Context.NONE).stream()
            .collect(Collectors.toList());

        assertKeysEqual(results, r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
            new String[]{"3", "5", "1"});
    }

    @Test
    public void singleVectorSearchWithFilterAsync() {
        SearchAsyncClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName", "Category")
            .setFilter("Category eq 'Budget'");

        StepVerifier.create(searchClient.search(null, searchOptions).collectList())
            .assertNext(results -> assertKeysEqual(results,
                r -> (String) r.getDocument(SearchDocument.class).get("HotelId"), new String[]{"3", "5", "4"}))
            .verifyComplete();
    }

    @Test
    public void singleVectorSearchWithFilterSync() {
        SearchClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName", "Category")
            .setFilter("Category eq 'Budget'");

        List<SearchResult> results = searchClient.search(null, searchOptions, Context.NONE)
            .stream().collect(Collectors.toList());

        assertKeysEqual(results, r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
            new String[]{"3", "5", "4"});
    }

    @Test
    public void simpleHybridSearchAsync() {
        SearchAsyncClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName");

        StepVerifier.create(searchClient.search("Top hotels in town", searchOptions).collectList())
            .assertNext(results -> assertKeysEqual(results,
                r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
                new String[]{"3", "1", "5", "2", "10", "4", "9"}))
            .verifyComplete();
    }

    @Test
    public void simpleHybridSearchSync() {
        SearchClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName");

        List<SearchResult> results = searchClient.search("Top hotels in town", searchOptions, Context.NONE)
            .stream().collect(Collectors.toList());

        assertKeysEqual(results, r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
            new String[]{"3", "1", "5", "2", "10", "4", "9"});
    }

    @Test
    @Disabled("Need to get manual recordings as this doesn't work with all SKUs or regions.")
    public void semanticHybridSearchAsync() {
        SearchAsyncClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, false).buildAsyncClient();

        SearchOptions searchOptions = new SearchOptions().setVectorSearchOptions(new VectorSearchOptions()
            .setQueries(new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                .setKNearestNeighborsCount(3)
                .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName", "Description", "Category")
            .setQueryType(QueryType.SEMANTIC)
            .setSemanticSearchOptions(new SemanticSearchOptions()
                .setSemanticConfigurationName("my-semantic-config")
                .setQueryCaption(new QueryCaption(QueryCaptionType.EXTRACTIVE))
                .setQueryAnswer(new QueryAnswer(QueryAnswerType.EXTRACTIVE)));

        StepVerifier.create(searchClient.search(
                "Is there any hotel located on the main commercial artery of the city in the heart of New York?",
                searchOptions).byPage().collectList())
            .assertNext(pages -> {
                SearchPagedResponse page1 = pages.get(0);
                assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1));
                assertEquals(1, SearchPagedResponseAccessHelper.getQueryAnswers(page1).size());
                assertEquals("9", SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getKey());
                assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getHighlights());
                assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getText());

                List<SearchResult> results = new ArrayList<>();
                for (SearchPagedResponse page : pages) {
                    for (SearchResult result : page.getValue()) {
                        results.add(result);

                        assertNotNull(result.getSemanticSearch().getQueryCaptions());
                        assertNotNull(result.getSemanticSearch().getQueryCaptions().get(0).getHighlights());
                        assertNotNull(result.getSemanticSearch().getQueryCaptions().get(0).getText());
                    }
                }

                assertKeysEqual(results, r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
                    new String[]{"9", "3", "2", "5", "10", "1", "4"});
            })
            .verifyComplete();
    }

    @Test
    @Disabled("Need to get manual recordings as this doesn't work with all SKUs or regions.")
    public void semanticHybridSearchSync() {
        SearchClient searchClient = getSearchClientBuilder(HOTEL_INDEX_NAME, true).buildClient();
        waitForIndexing();
        SearchOptions searchOptions = new SearchOptions()
            .setVectorSearchOptions(new VectorSearchOptions().setQueries(
                new VectorizedQuery(VectorSearchEmbeddings.SEARCH_VECTORIZE_DESCRIPTION)
                    .setKNearestNeighborsCount(3)
                    .setFields("DescriptionVector")))
            .setSelect("HotelId", "HotelName", "Description", "Category")
            .setQueryType(QueryType.SEMANTIC)
            .setSemanticSearchOptions(new SemanticSearchOptions()
                .setSemanticConfigurationName("my-semantic-config")
                .setQueryCaption(new QueryCaption(QueryCaptionType.EXTRACTIVE))
                .setQueryAnswer(new QueryAnswer(QueryAnswerType.EXTRACTIVE)));

        List<SearchPagedResponse> pages = searchClient.search(
            "Is there any hotel located on the main commercial artery of the city in the heart of New York?",
            searchOptions, Context.NONE).streamByPage().collect(Collectors.toList());

        SearchPagedResponse page1 = pages.get(0);
        assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1));
        assertEquals(1, SearchPagedResponseAccessHelper.getQueryAnswers(page1).size());
        assertEquals("9", SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getKey());
        assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getHighlights());
        assertNotNull(SearchPagedResponseAccessHelper.getQueryAnswers(page1).get(0).getText());

        List<SearchResult> results = new ArrayList<>();
        for (SearchPagedResponse page : pages) {
            for (SearchResult result : page.getValue()) {
                results.add(result);

                assertNotNull(result.getSemanticSearch().getQueryCaptions());
                assertNotNull(result.getSemanticSearch().getQueryCaptions().get(0).getHighlights());
                assertNotNull(result.getSemanticSearch().getQueryCaptions().get(0).getText());
            }
        }

        assertKeysEqual(results, r -> (String) r.getDocument(SearchDocument.class).get("HotelId"),
            new String[]{"9", "3", "2", "5", "10", "1", "4"});
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateExistingIndexToAddVectorFieldsAsync() {
        String indexName = randomIndexName("addvectorasync");
        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(
                new SearchField("Id", SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true));

        SearchIndexAsyncClient searchIndexClient = getSearchIndexClientBuilder(false).buildAsyncClient();
        searchIndexClient.createIndex(searchIndex).block();
        indexesToDelete.add(indexName);

        // Upload data
        SearchDocument document = new SearchDocument();
        document.put("Id", "1");
        document.put("Name", "Countryside Hotel");

        SearchAsyncClient searchClient = searchIndexClient.getSearchAsyncClient(indexName);
        searchClient.uploadDocuments(Collections.singletonList(document)).block();

        waitForIndexing();

        // Get the document
        StepVerifier.create(searchClient.getDocument("1", SearchDocument.class))
            .assertNext(response -> {
                assertEquals(document.get("Id"), response.get("Id"));
                assertEquals(document.get("Name"), response.get("Name"));
            })
            .verifyComplete();

        // Update created index to add vector field

        // Get created index
        Mono<SearchIndex> getAndUpdateIndex = searchIndexClient.getIndex(indexName)
            .flatMap(createdIndex -> {
                // Add vector
                SearchField vectorField = new SearchField("DescriptionVector",
                    SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setHidden(false)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile");

                createdIndex.getFields().add(vectorField);

                createdIndex.setVectorSearch(new VectorSearch()
                    .setProfiles(Collections.singletonList(
                        new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                    .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config"))));

                return searchIndexClient.createOrUpdateIndex(createdIndex);
            });

        // Update index
        StepVerifier.create(getAndUpdateIndex).thenAwait(Duration.ofSeconds(10))
            .assertNext(response -> {
                assertEquals(indexName, response.getName());
                assertEquals(3, response.getFields().size());
            })
            .verifyComplete();
        waitForIndexing();

        // Update document to add vector field's data

        // Get the document
        Mono<SearchDocument> getAndUpdateDocument = searchClient.getDocument("1", SearchDocument.class)
            .flatMap(resultDoc -> {
                // Update document to add vector field data
                resultDoc.put("DescriptionVector", VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION);
                return searchClient.mergeDocuments(Collections.singletonList(resultDoc));

            })
            .flatMap(ignored -> {
                // Equivalent of 'waitForIndexing()' where in PLAYBACK getting the document is called right away,
                // but for LIVE and RECORD it waits two seconds for the document to be available.
                if (TEST_MODE == TestMode.PLAYBACK) {
                    return searchClient.getDocument("1", SearchDocument.class);
                } else {
                    waitForIndexing();
                    return searchClient.getDocument("1", SearchDocument.class)
                        .delayElement(Duration.ofSeconds(5));
                }
            });

        // Get the document
        StepVerifier.create(getAndUpdateDocument.delayElement(Duration.ofSeconds(5)))
            .assertNext(response -> {
                assertEquals(document.get("Id"), response.get("Id"));
                assertEquals(document.get("Name"), response.get("Name"));
                compareFloatListToDeserializedFloatList(VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION,
                    (List<Number>) response.get("DescriptionVector"));
            })
            .verifyComplete();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void updateExistingIndexToAddVectorFieldsSync() {
        String indexName = randomIndexName("addvectorsync");
        SearchIndex searchIndex = new SearchIndex(indexName)
            .setFields(
                new SearchField("Id", SearchFieldDataType.STRING)
                    .setKey(true),
                new SearchField("Name", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder(true).buildClient();
        searchIndexClient.createIndex(searchIndex);
        indexesToDelete.add(indexName);
        // Upload data
        SearchDocument document = new SearchDocument();
        document.put("Id", "1");
        document.put("Name", "Countryside Hotel");

        SearchClient searchClient = searchIndexClient.getSearchClient(indexName);
        searchClient.uploadDocuments(Collections.singletonList(document));

        waitForIndexing();

        // Get the document
        SearchDocument responseDocument = searchClient.getDocument("1", SearchDocument.class);

        assertEquals(document.get("Id"), responseDocument.get("Id"));
        assertEquals(document.get("Name"), responseDocument.get("Name"));

        // Update created index to add vector field

        // Get created index
        SearchIndex createdIndex = searchIndexClient.getIndex(indexName);

        waitForIndexing();

        // Add vector
        SearchField vectorField = new SearchField("DescriptionVector",
            SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
            .setSearchable(true)
            .setHidden(false)
            .setVectorSearchDimensions(1536)
            .setVectorSearchProfileName("my-vector-profile");

        createdIndex.getFields().add(vectorField);

        createdIndex.setVectorSearch(new VectorSearch()
                .setProfiles(Collections.singletonList(
                    new VectorSearchProfile("my-vector-profile", "my-vector-config")))
            .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config"))));

        // Update index
        SearchIndex responseIndex = searchIndexClient.createOrUpdateIndex(createdIndex);

        waitForIndexing();

        assertEquals(indexName, responseIndex.getName());
        assertEquals(3, responseIndex.getFields().size());

        // Update document to add vector field's data

        // Get the document
        SearchDocument resultDoc = searchClient.getDocument("1", SearchDocument.class);

        // Update document to add vector field data
        resultDoc.put("DescriptionVector", VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION);

        searchClient.mergeDocuments(Collections.singletonList(resultDoc));
        waitForIndexing();

        // Get the document
        responseDocument = searchClient.getDocument("1", SearchDocument.class);

        waitForIndexing();

        assertEquals(document.get("Id"), responseDocument.get("Id"));
        assertEquals(document.get("Name"), responseDocument.get("Name"));
        compareFloatListToDeserializedFloatList(VectorSearchEmbeddings.DEFAULT_VECTORIZE_DESCRIPTION,
            (List<Number>) responseDocument.get("DescriptionVector"));
    }

    private static void compareFloatListToDeserializedFloatList(List<Float> expected, List<Number> actual) {
        if (actual == null) {
            assertNull(expected);
            return;
        }

        assertEquals(expected.size(), actual.size());

        Object obj = actual.get(0);
        if (obj instanceof Float || obj instanceof Double) {
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i), actual.get(i).floatValue());
            }
        } else {
            throw new IllegalStateException("Deserialization of a float list returned an unexpected type. Type was: "
                + obj.getClass().getName());
        }
    }

    private static SearchIndex getVectorIndex() {
        return new SearchIndex(HOTEL_INDEX_NAME)
            .setFields(
                new SearchField("HotelId", SearchFieldDataType.STRING)
                    .setKey(true)
                    .setFilterable(true)
                    .setSortable(true)
                    .setFacetable(true),
                new SearchField("HotelName", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setSortable(true),
                new SearchField("Description", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                new SearchField("Description_fr", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
                new SearchField("DescriptionVector", SearchFieldDataType.collection(SearchFieldDataType.SINGLE))
                    .setSearchable(true)
                    .setVectorSearchDimensions(1536)
                    .setVectorSearchProfileName("my-vector-profile"),
                new SearchField("Category", SearchFieldDataType.STRING)
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                    .setSearchable(true)
                    .setFilterable(true)
                    .setFacetable(true),
                new SearchField("ParkingIncluded", SearchFieldDataType.BOOLEAN)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("LastRenovationDate", SearchFieldDataType.DATE_TIME_OFFSET)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("Rating", SearchFieldDataType.INT32)
                    .setFilterable(true)
                    .setFacetable(true)
                    .setSortable(true),
                new SearchField("Location", SearchFieldDataType.GEOGRAPHY_POINT)
                    .setFilterable(true)
                    .setSortable(true),
                new SearchField("Address", SearchFieldDataType.COMPLEX)
                    .setFields(
                        new SearchField("StreetAddress", SearchFieldDataType.STRING)
                            .setSearchable(true),
                        new SearchField("City", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true)
                            .setSortable(true),
                        new SearchField("StateProvince", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true)
                            .setSortable(true),
                        new SearchField("Country", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true)
                            .setSortable(true),
                        new SearchField("PostalCode", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true)
                            .setSortable(true)),
                new SearchField("Rooms", SearchFieldDataType.collection(SearchFieldDataType.COMPLEX))
                    .setFields(
                        new SearchField("Description", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setAnalyzerName(LexicalAnalyzerName.EN_LUCENE),
                        new SearchField("Description_fr", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setAnalyzerName(LexicalAnalyzerName.FR_LUCENE),
                        new SearchField("Type", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true),
                        new SearchField("BaseRate", SearchFieldDataType.DOUBLE)
                            .setFilterable(true)
                            .setFacetable(true),
                        new SearchField("BedOptions", SearchFieldDataType.STRING)
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true),
                        new SearchField("SleepsCount", SearchFieldDataType.INT32)
                            .setFilterable(true)
                            .setFacetable(true),
                        new SearchField("SmokingAllowed", SearchFieldDataType.BOOLEAN)
                            .setFilterable(true)
                            .setFacetable(true),
                        new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                            .setSearchable(true)
                            .setFilterable(true)
                            .setFacetable(true)))
            .setVectorSearch(new VectorSearch()
                .setProfiles(Collections.singletonList(
                    new VectorSearchProfile("my-vector-profile", "my-vector-config")))
                .setAlgorithms(Collections.singletonList(new HnswAlgorithmConfiguration("my-vector-config"))))
            .setSemanticSearch(new SemanticSearch()
                .setConfigurations(Collections.singletonList(new SemanticConfiguration("my-semantic-config",
                    new SemanticPrioritizedFields().setTitleField(new SemanticField("HotelName"))
                        .setContentFields(Collections.singletonList(new SemanticField("Description")))
                        .setKeywordsFields(Collections.singletonList(new SemanticField("Category")))))))
            .setSuggesters(new SearchSuggester("sg", Arrays.asList("Description", "HotelName")))
            .setScoringProfiles(new ScoringProfile("nearest")
                .setFunctionAggregation(ScoringFunctionAggregation.SUM)
                .setFunctions(new DistanceScoringFunction("Location", 2, new DistanceScoringParameters("myloc", 100))));
    }

    /*
     * Hotels with vectorized data.
     */
    private static final List<VectorHotel> VECTORIZED_HOTELS = Arrays.asList(
        new VectorHotel()
            .hotelId("1")
            .description("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, and "
                + "a really helpful concierge. The location is perfect -- right downtown, close to all the tourist "
                + "attractions. We highly recommend this hotel.")
            .descriptionFr("Meilleur hôtel en ville si vous aimez les hôtels de luxe. Ils ont une magnifique piscine à "
                + "débordement, un spa et un concierge très utile. L'emplacement est parfait – en plein centre, à "
                + "proximité de toutes les attractions touristiques. Nous recommandons fortement cet hôtel.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL1_VECTORIZE_DESCRIPTION)
            .hotelName("Fancy Stay")
            .category("Luxury")
            .tags(Arrays.asList("pool", "view", "wifi", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(5)
            .location(new GeoPoint(-122.131577, 47.678581)),
        new VectorHotel()
            .hotelId("2")
            .description("Cheapest hotel in town. Infact, a motel.")
            .descriptionFr("Hôtel le moins cher en ville. Infact, un motel.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL2_VECTORIZE_DESCRIPTION)
            .hotelName("Roach Motel")
            .category("Budget")
            .tags(Arrays.asList("motel", "budget"))
            .parkingIncluded(true)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("1982-04-28T00:00:00Z"))
            .rating(1)
            .location(new GeoPoint(-122.131577, 49.678581)),
        new VectorHotel()
            .hotelId("3")
            .description("Very popular hotel in town")
            .descriptionFr("Hôtel le plus populaire en ville")
            .descriptionVector(VectorSearchEmbeddings.HOTEL3_VECTORIZE_DESCRIPTION)
            .hotelName("EconoStay")
            .category("Budget")
            .tags(Arrays.asList("wifi", "budget"))
            .parkingIncluded(true)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("1995-07-01T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-122.131577, 46.678581)),
        new VectorHotel()
            .hotelId("4")
            .description("Pretty good hotel")
            .descriptionFr("Assez bon hôtel")
            .descriptionVector(VectorSearchEmbeddings.HOTEL4_VECTORIZE_DESCRIPTION)
            .hotelName("Express Rooms")
            .category("Budget")
            .tags(Arrays.asList("wifi", "budget"))
            .parkingIncluded(true)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("1995-07-01T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-122.131577, 48.678581)),
        new VectorHotel()
            .hotelId("5")
            .description("Another good hotel")
            .descriptionFr("Un autre bon hôtel")
            .descriptionVector(VectorSearchEmbeddings.HOTEL5_VECTORIZE_DESCRIPTION)
            .hotelName("Comfy Place")
            .category("Budget")
            .tags(Arrays.asList("wifi", "budget"))
            .parkingIncluded(true)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("2012-08-12T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-122.131577, 48.678581))
            .address(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("NEW YORK")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022")),
        new VectorHotel()
            .hotelId("6")
            .description("Surprisingly expensive. Model suites have an ocean-view.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL6_VECTORIZE_DESCRIPTION)
            .lastRenovationDate(null),
        new VectorHotel()
            .hotelId("7")
            .description("Modern architecture, very polite staff and very clean. Also very affordable.")
            .descriptionFr("Architecture moderne, personnel poli et très propre. Aussi très abordable.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL7_VECTORIZE_DESCRIPTION)
            .hotelName("Modern Stay"),
        new VectorHotel()
            .hotelId("8")
            .description("Has some road noise and is next to the very police station. Bathrooms had morel coverings.")
            .descriptionFr("Il y a du bruit de la route et se trouve à côté de la station de police. Les salles de "
                + "bain avaient des revêtements de morilles.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL8_VECTORIZE_DESCRIPTION),
        new VectorHotel()
            .hotelId("9")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of "
                + "New York. A few minutes away is Time's Square and the historic centre of the city, as well as other "
                + "places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein "
                + "cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la "
                + "ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL9_VECTORIZE_DESCRIPTION)
            .category("Boutique")
            .tags(Arrays.asList("pool", "air conditioning", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("1970-01-18T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-73.97332, 40.763843))
            .address(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .rooms(Arrays.asList(
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd"}),
                new HotelRoom()
                    .description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd", "jacuzzi tub"}))),
        new VectorHotel()
            .hotelId("10")
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full "
                + "kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .descriptionFr("Économisez jusqu'à 50% sur les hôtels traditionnels.  WiFi gratuit, très bien situé près "
                + "du centre-ville, cuisine complète, laveuse & sécheuse, support 24/7, bowling, centre de fitness et "
                + "plus encore.")
            .descriptionVector(VectorSearchEmbeddings.HOTEL10_VECTORIZE_DESCRIPTION)
            .category("Budget")
            .tags(Arrays.asList("24-hour front desk service", "coffee in lobby", "restaurant"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("1999-09-06T00:00:00Z"))
            .rating(3)
            .location(new GeoPoint(-78.940483, 35.904160))
            .address(new HotelAddress()
                .streetAddress("6910 Fayetteville Rd")
                .city("Durham")
                .stateProvince("NC")
                .country("USA")
                .postalCode("27713"))
            .rooms(Arrays.asList(
                new HotelRoom()
                    .description("Suite, 1 King Bed (Amenities)")
                    .descriptionFr("Suite, 1 très grand lit (Services)")
                    .type("Suite")
                    .baseRate(2.44)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"coffee maker"}),
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Amenities)")
                    .descriptionFr("Chambre Économique, 1 grand lit (Services)")
                    .type("Budget Room")
                    .baseRate(7.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(false)
                    .tags(new String[]{"coffee maker"}))));
}
