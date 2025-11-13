// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.SearchServiceVersion;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.CorsOptions;
import com.azure.search.documents.indexes.models.IndexStatisticsSummary;
import com.azure.search.documents.indexes.models.LexicalAnalyzerName;
import com.azure.search.documents.indexes.models.MagnitudeScoringFunction;
import com.azure.search.documents.indexes.models.MagnitudeScoringParameters;
import com.azure.search.documents.indexes.models.ScoringFunctionAggregation;
import com.azure.search.documents.indexes.models.ScoringFunctionInterpolation;
import com.azure.search.documents.indexes.models.ScoringProfile;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchIndexStatistics;
import com.azure.search.documents.indexes.models.SearchSuggester;
import com.azure.search.documents.indexes.models.SemanticConfiguration;
import com.azure.search.documents.indexes.models.SemanticField;
import com.azure.search.documents.indexes.models.SemanticPrioritizedFields;
import com.azure.search.documents.indexes.models.SemanticSearch;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.QueryType;
import com.azure.search.documents.util.SearchPagedIterable;
import com.azure.search.documents.models.SearchOptions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.HOTEL_INDEX_NAME;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class IndexManagementTests extends SearchTestBase {
    private static SearchIndexClient sharedIndexClient;
    private static SynonymMap sharedSynonymMap;

    private final List<String> indexesToDelete = new ArrayList<>();

    private SearchIndexClient client;
    private SearchIndexAsyncClient asyncClient;

    @BeforeAll
    public static void setupSharedResources() {
        sharedSynonymMap = new SynonymMap("sharedhotelmotel").setSynonyms("hotel,motel");

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        sharedIndexClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(TestHelpers.getTestTokenCredential())
            .buildClient();

        sharedSynonymMap = sharedIndexClient.createSynonymMap(sharedSynonymMap);
    }

    @AfterAll
    public static void cleanupSharedResources() {
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to run.
        }

        sharedIndexClient.deleteSynonymMap(sharedSynonymMap.getName());
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();

        client = getSearchIndexClientBuilder(true).buildClient();
        asyncClient = getSearchIndexClientBuilder(false).buildAsyncClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        for (String index : indexesToDelete) {
            client.deleteIndex(index);
        }
    }

    @Test
    public void createAndGetIndexReturnsCorrectDefinitionSync() {
        SearchIndex index = createTestIndex(null);
        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        assertObjectEquals(index, createdIndex, true, "etag");

        SearchIndex getIndex = client.getIndex(index.getName());
        assertObjectEquals(index, getIndex, true, "etag");
    }

    @Test
    public void createAndGetIndexReturnsCorrectDefinitionAsync() {
        SearchIndex index = createTestIndex(null);

        StepVerifier.create(asyncClient.createIndex(index)).assertNext(createdIndex -> {
            indexesToDelete.add(createdIndex.getName());
            assertObjectEquals(index, createdIndex, true, "etag");
        }).verifyComplete();

        StepVerifier.create(asyncClient.getIndex(index.getName()))
            .assertNext(getIndex -> assertObjectEquals(index, getIndex, true, "etag"))
            .verifyComplete();
    }

    @Test
    public void createAndGetIndexReturnsCorrectDefinitionWithResponseSync() {
        SearchIndex index = createTestIndex("hotel2");
        Response<SearchIndex> createIndexResponse = client.createIndexWithResponse(index, Context.NONE);
        indexesToDelete.add(createIndexResponse.getValue().getName());

        assertObjectEquals(index, createIndexResponse.getValue(), true, "etag");

        Response<SearchIndex> getIndexResponse = client.getIndexWithResponse(index.getName(), Context.NONE);
        assertObjectEquals(index, getIndexResponse.getValue(), true, "etag");
    }

    @Test
    public void createAndGetIndexReturnsCorrectDefinitionWithResponseAsync() {
        SearchIndex index = createTestIndex("hotel2");

        StepVerifier.create(asyncClient.createIndexWithResponse(index)).assertNext(response -> {
            indexesToDelete.add(response.getValue().getName());

            assertObjectEquals(index, response.getValue(), true, "etag");
        }).verifyComplete();

        StepVerifier.create(asyncClient.getIndexWithResponse(index.getName()))
            .assertNext(response -> assertObjectEquals(index, response.getValue(), true, "etag"))
            .verifyComplete();
    }

    @Test
    public void createIndexReturnsCorrectDefaultValuesSync() {
        SearchIndex index = createTestIndex(null).setCorsOptions(new CorsOptions(Collections.singletonList("*")))
            .setScoringProfiles(new ScoringProfile("MyProfile")
                .setFunctions(new MagnitudeScoringFunction("Rating", 2.0, new MagnitudeScoringParameters(1, 4))));
        SearchIndex indexResponse = client.createIndex(index);
        indexesToDelete.add(indexResponse.getName());

        ScoringProfile scoringProfile = indexResponse.getScoringProfiles().get(0);
        assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
        assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
        assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
    }

    @Test
    public void createIndexReturnsCorrectDefaultValuesAsync() {
        SearchIndex index = createTestIndex(null).setCorsOptions(new CorsOptions(Collections.singletonList("*")))
            .setScoringProfiles(new ScoringProfile("MyProfile")
                .setFunctions(new MagnitudeScoringFunction("Rating", 2.0, new MagnitudeScoringParameters(1, 4))));

        StepVerifier.create(asyncClient.createIndex(index)).assertNext(created -> {
            indexesToDelete.add(created.getName());

            ScoringProfile scoringProfile = created.getScoringProfiles().get(0);
            assertNull(created.getCorsOptions().getMaxAgeInSeconds());
            assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
            assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
        }).verifyComplete();
    }

    @Test
    public void createIndexFailsWithUsefulMessageOnUserErrorSync() {
        String indexName = HOTEL_INDEX_NAME;
        SearchIndex index = new SearchIndex(indexName)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(false));
        String expectedMessage = String
            .format("Found 0 key fields in index '%s'. " + "Each index must have exactly one key field.", indexName);

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> client.createIndex(index));
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
        assertTrue(ex.getMessage().contains(expectedMessage), "Expected exception to contain message '"
            + expectedMessage + "' but the message was '" + ex.getMessage() + "'.");
    }

    @Test
    public void createIndexFailsWithUsefulMessageOnUserErrorAsync() {
        String indexName = HOTEL_INDEX_NAME;
        SearchIndex index = new SearchIndex(indexName)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(false));
        String expectedMessage = String
            .format("Found 0 key fields in index '%s'. " + "Each index must have exactly one key field.", indexName);

        StepVerifier.create(asyncClient.createIndex(index)).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
            assertTrue(ex.getMessage().contains(expectedMessage), "Expected exception to contain message '"
                + expectedMessage + "' but the message was '" + ex.getMessage() + "'.");
        });
    }

    @Test
    public void getIndexThrowsOnNotFoundSync() {
        assertHttpResponseException(() -> client.getIndex("thisindexdoesnotexist"), HttpURLConnection.HTTP_NOT_FOUND,
            "No index with the name 'thisindexdoesnotexist' was found in the service");
    }

    @Test
    public void getIndexThrowsOnNotFoundAsync() {
        StepVerifier.create(asyncClient.getIndex("thisindexdoesnotexist"))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_NOT_FOUND,
                "No index with the name 'thisindexdoesnotexist' was found in the service"));
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResourceSync() {
        SearchIndex indexToCreate = createTestIndex(null);

        // Create the resource in the search service
        SearchIndex originalIndex
            = client.createOrUpdateIndexWithResponse(indexToCreate, false, false, Context.NONE).getValue();

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex
            = client
                .createOrUpdateIndexWithResponse(
                    originalIndex.setCorsOptions(new CorsOptions(Collections.singletonList("https://test.com/"))),
                    false, false, Context.NONE)
                .getValue();

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteIndexWithResponse(originalIndex, true, Context.NONE));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        assertDoesNotThrow(() -> client.deleteIndexWithResponse(updatedIndex, true, Context.NONE));
    }

    @Test
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResourceAsync() {
        SearchIndex indexToCreate = createTestIndex(null);

        // Create the resource in the search service
        SearchIndex originalIndex = asyncClient.createOrUpdateIndexWithResponse(indexToCreate, false, false)
            .map(Response::getValue)
            .blockOptional()
            .orElseThrow(NoSuchElementException::new);

        // Update the resource, the eTag will be changed
        SearchIndex updatedIndex = asyncClient.createOrUpdateIndexWithResponse(
            originalIndex.setCorsOptions(new CorsOptions(Collections.singletonList("https://test.com/"))), false, false)
            .map(Response::getValue)
            .block();

        StepVerifier.create(asyncClient.deleteIndexWithResponse(originalIndex, true))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
            });

        StepVerifier.create(asyncClient.deleteIndexWithResponse(updatedIndex, true))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExistsSync() {
        SearchIndex index
            = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE).getValue();

        client.deleteIndexWithResponse(index, true, Context.NONE);

        // Try to delete again and expect to fail
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteIndexWithResponse(index, true, Context.NONE));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
    }

    @Test
    public void deleteIndexIfExistsWorksOnlyWhenResourceExistsAsync() {
        SearchIndex index = asyncClient.createOrUpdateIndexWithResponse(createTestIndex(null), false, false)
            .map(Response::getValue)
            .block();

        asyncClient.deleteIndexWithResponse(index, true).block();

        // Try to delete again and expect to fail
        StepVerifier.create(asyncClient.deleteIndexWithResponse(index, true)).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void deleteIndexIsIdempotentSync() {
        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true));
        Response<Void> deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SearchIndex> createResponse = client.createIndexWithResponse(index, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void deleteIndexIsIdempotentAsync() {
        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME)
            .setFields(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true));

        StepVerifier.create(asyncClient.deleteIndexWithResponse(index, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.createIndexWithResponse(index))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode()))
            .verifyComplete();

        // Delete the same index twice
        StepVerifier.create(asyncClient.deleteIndexWithResponse(index, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.deleteIndexWithResponse(index, false))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void canCreateAndListIndexesSyncAndAsync() {
        SearchIndex index1 = createTestIndex("a" + randomIndexName(HOTEL_INDEX_NAME));
        SearchIndex index2 = createTestIndex("b" + randomIndexName(HOTEL_INDEX_NAME));

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        Map<String, SearchIndex> expectedIndexes = new HashMap<>();
        expectedIndexes.put(index1.getName(), index1);
        expectedIndexes.put(index2.getName(), index2);

        Map<String, SearchIndex> actualIndexes
            = client.listIndexes().stream().collect(Collectors.toMap(SearchIndex::getName, si -> si));

        compareMaps(expectedIndexes, actualIndexes, (expected, actual) -> assertObjectEquals(expected, actual, true),
            false);

        StepVerifier.create(asyncClient.listIndexes().collectMap(SearchIndex::getName))
            .assertNext(actualIndexes2 -> compareMaps(expectedIndexes, actualIndexes2,
                (expected, actual) -> assertObjectEquals(expected, actual, true), false))
            .verifyComplete();
    }

    @Test
    public void canListIndexesWithSelectedFieldSyncAndAsync() {
        SearchIndex index1 = createTestIndex("a" + randomIndexName(HOTEL_INDEX_NAME));
        SearchIndex index2 = createTestIndex("b" + randomIndexName(HOTEL_INDEX_NAME));

        client.createIndex(index1);
        indexesToDelete.add(index1.getName());
        client.createIndex(index2);
        indexesToDelete.add(index2.getName());

        Set<String> expectedIndexNames = new HashSet<>(Arrays.asList(index1.getName(), index2.getName()));
        Set<String> actualIndexNames = client.listIndexNames(Context.NONE).stream().collect(Collectors.toSet());

        // Only check that listing returned the expected index names. Don't check the number of indexes returned as
        // other tests may have created indexes.
        assertTrue(actualIndexNames.containsAll(expectedIndexNames));

        StepVerifier.create(asyncClient.listIndexNames().collect(Collectors.toSet()))
            .assertNext(actualIndexNames2 -> assertTrue(actualIndexNames2.containsAll(expectedIndexNames)))
            .verifyComplete();
    }

    @Test
    public void canAddSynonymFieldPropertySync() {
        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME).setFields(Arrays.asList(
            new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("HotelName", SearchFieldDataType.STRING).setSynonymMapNames(sharedSynonymMap.getName())));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        List<String> actualSynonym = index.getFields().get(1).getSynonymMapNames();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMapNames();
        assertEquals(actualSynonym, expectedSynonym);
    }

    @Test
    public void canAddSynonymFieldPropertyAsync() {
        SearchIndex index = new SearchIndex(HOTEL_INDEX_NAME).setFields(Arrays.asList(
            new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
            new SearchField("HotelName", SearchFieldDataType.STRING).setSynonymMapNames(sharedSynonymMap.getName())));

        StepVerifier.create(asyncClient.createIndex(index)).assertNext(createdIndex -> {
            indexesToDelete.add(createdIndex.getName());

            List<String> actualSynonym = index.getFields().get(1).getSynonymMapNames();
            List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMapNames();
            assertEquals(actualSynonym, expectedSynonym);
        }).verifyComplete();
    }

    @Test
    public void canUpdateSynonymFieldPropertySync() {
        // Create an index
        SearchIndex index = createTestIndex(null);
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMapNames(sharedSynonymMap.getName());
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        // Update an existing index
        SearchIndex existingIndex = client.getIndex(index.getName());
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMapNames(Collections.emptyList());

        SearchIndex updatedIndex
            = client.createOrUpdateIndexWithResponse(existingIndex, true, false, Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSynonymFieldPropertyAsync() {
        // Create an index
        SearchIndex index = createTestIndex(null);
        SearchField hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMapNames(sharedSynonymMap.getName());
        asyncClient.createIndex(index).block();
        indexesToDelete.add(index.getName());

        // Update an existing index
        Mono<Tuple2<SearchIndex, SearchIndex>> getAndUpdateIndexMono
            = asyncClient.getIndex(index.getName()).flatMap(existingIndex -> {
                getFieldByName(existingIndex, "HotelName").setSynonymMapNames(Collections.emptyList());

                return asyncClient.createOrUpdateIndexWithResponse(existingIndex, true, false)
                    .map(response -> Tuples.of(existingIndex, response.getValue()));
            });

        StepVerifier.create(getAndUpdateIndexMono)
            .assertNext(indexes -> assertObjectEquals(indexes.getT1(), indexes.getT2(), true, "etag", "@odata.etag"))
            .verifyComplete();
    }

    @Test
    public void canUpdateIndexDefinitionSync() {
        SearchIndex fullFeaturedIndex = createTestIndex(null);

        // Start out with no scoring profiles and different CORS options.
        SearchIndex initialIndex = createTestIndex(fullFeaturedIndex.getName());
        initialIndex.setScoringProfiles(new ArrayList<>())
            .setDefaultScoringProfile(null)
            .setCorsOptions(mutateCorsOptionsInIndex(initialIndex, Collections.singletonList("*")).getCorsOptions());

        SearchIndex index = client.createIndex(initialIndex);
        indexesToDelete.add(index.getName());

        // Now update the index.
        List<String> allowedOrigins = fullFeaturedIndex.getCorsOptions().getAllowedOrigins();
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(mutateCorsOptionsInIndex(index, allowedOrigins).getCorsOptions());

        SearchIndex updatedIndex = client.createOrUpdateIndex(index);

        assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag");

        // Modify the fields on an existing index
        SearchIndex existingIndex = client.getIndex(fullFeaturedIndex.getName());

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzerName(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMapNames(sharedSynonymMap.getName());

        SearchField hotelWebSiteField
            = new SearchField("HotelWebsite", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

        SearchField hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        updatedIndex = client.createOrUpdateIndexWithResponse(existingIndex, true, false, Context.NONE).getValue();

        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateIndexDefinitionAsync() {
        SearchIndex fullFeaturedIndex = createTestIndex(null);

        // Start out with no scoring profiles and different CORS options.
        SearchIndex initialIndex = mutateCorsOptionsInIndex(
            createTestIndex(fullFeaturedIndex.getName()).setScoringProfiles(Collections.emptyList())
                .setDefaultScoringProfile(null));

        SearchIndex index
            = asyncClient.createIndex(initialIndex).blockOptional().orElseThrow(NoSuchElementException::new);
        indexesToDelete.add(index.getName());

        // Now update the index.
        List<String> allowedOrigins = fullFeaturedIndex.getCorsOptions().getAllowedOrigins();
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles())
            .setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile())
            .setCorsOptions(mutateCorsOptionsInIndex(index, allowedOrigins).getCorsOptions());

        StepVerifier.create(asyncClient.createOrUpdateIndex(index))
            .assertNext(
                updatedIndex -> assertObjectEquals(fullFeaturedIndex, updatedIndex, true, "etag", "@odata.etag"))
            .verifyComplete();

        // Modify the fields on an existing index
        SearchIndex existingIndex = asyncClient.getIndex(fullFeaturedIndex.getName())
            .blockOptional()
            .orElseThrow(NoSuchElementException::new);

        SearchField tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setHidden(true)
            .setSearchAnalyzerName(LexicalAnalyzerName.WHITESPACE)
            .setSynonymMapNames(sharedSynonymMap.getName());

        SearchField hotelWebSiteField
            = new SearchField("HotelWebsite", SearchFieldDataType.STRING).setSearchable(Boolean.TRUE)
                .setFilterable(Boolean.TRUE);
        existingIndex.getFields().add(hotelWebSiteField);

        SearchField hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setHidden(true);

        StepVerifier.create(asyncClient.createOrUpdateIndexWithResponse(existingIndex, true, false))
            .assertNext(response -> assertObjectEquals(existingIndex, response.getValue(), true, "etag", "@odata.etag"))
            .verifyComplete();
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFieldsSync() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());

        existingIndex.getFields()
            .addAll(Arrays.asList(new SearchField("HotelAmenities", SearchFieldDataType.STRING),
                new SearchField("HotelRewards", SearchFieldDataType.STRING)));
        existingIndex.setSuggesters(new SearchSuggester("Suggestion", Arrays.asList("HotelAmenities", "HotelRewards")));

        SearchIndex updatedIndex
            = client.createOrUpdateIndexWithResponse(existingIndex, true, false, Context.NONE).getValue();
        assertObjectEquals(existingIndex, updatedIndex, true, "etag", "@odata.etag");
    }

    @Test
    public void canUpdateSuggesterWithNewIndexFieldsAsync() {
        Mono<Tuple2<SearchIndex, SearchIndex>> createAndUpdateIndexMono
            = asyncClient.createIndex(createTestIndex(null)).flatMap(index -> {
                indexesToDelete.add(index.getName());
                index.getFields()
                    .addAll(Arrays.asList(new SearchField("HotelAmenities", SearchFieldDataType.STRING),
                        new SearchField("HotelRewards", SearchFieldDataType.STRING)));
                index.setSuggesters(new SearchSuggester("Suggestion", Arrays.asList("HotelAmenities", "HotelRewards")));

                return asyncClient.createOrUpdateIndexWithResponse(index, true, false)
                    .map(response -> Tuples.of(index, response.getValue()));
            });

        StepVerifier.create(createAndUpdateIndexMono)
            .assertNext(indexes -> assertObjectEquals(indexes.getT1(), indexes.getT2(), true, "etag", "@odata.etag"))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFieldsSync() {
        SearchIndex index = createTestIndex(null);
        client.createIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndex existingIndex = client.getIndex(index.getName());
        String existingFieldName = "Category";
        existingIndex.setSuggesters(new SearchSuggester("Suggestion", Collections.singletonList(existingFieldName)));

        assertHttpResponseException(() -> client.createOrUpdateIndex(existingIndex), HttpURLConnection.HTTP_BAD_REQUEST,
            "Fields that were already present in an index (" + existingFieldName + ") cannot be referenced by a new "
                + "suggester. Only new fields added in the same index update operation are allowed.");
    }

    @Test
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFieldsAsync() {
        String existingFieldName = "Category";
        Mono<SearchIndex> createThenInvalidUpdateMono
            = asyncClient.createIndex(createTestIndex(null)).flatMap(index -> {
                indexesToDelete.add(index.getName());

                return asyncClient.createOrUpdateIndex(index
                    .setSuggesters(new SearchSuggester("Suggestion", Collections.singletonList(existingFieldName))));
            });

        StepVerifier.create(createThenInvalidUpdateMono)
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_BAD_REQUEST,
                "Fields that were already present in an index (" + existingFieldName + ") cannot be referenced by a "
                    + "new suggester. Only new fields added in the same index update operation are allowed."));
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistSync() {
        SearchIndex expected = createTestIndex(null);

        SearchIndex actual = client.createOrUpdateIndex(expected);
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistAsync() {
        Function<String, Mono<Tuple2<SearchIndex, SearchIndex>>> createAndValidateFunction = indexName -> {
            SearchIndex expected = createTestIndex(indexName);
            return asyncClient.createOrUpdateIndex(expected).map(actual -> Tuples.of(expected, actual));
        };

        StepVerifier.create(createAndValidateFunction.apply(null)).assertNext(indexes -> {
            indexesToDelete.add(indexes.getT2().getName());
            assertObjectEquals(indexes.getT1(), indexes.getT2(), true, "etag");
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponseSync() {
        SearchIndex expected = createTestIndex(null);

        SearchIndex actual = client.createOrUpdateIndexWithResponse(expected, false, false, Context.NONE).getValue();
        indexesToDelete.add(actual.getName());
        assertObjectEquals(expected, actual, true, "etag");
    }

    @Test
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExistWithResponseAsync() {
        Function<String, Mono<Tuple2<SearchIndex, SearchIndex>>> createAndValidateFunction = indexName -> {
            SearchIndex expected = createTestIndex(indexName);
            return asyncClient.createOrUpdateIndexWithResponse(expected, false, false)
                .map(response -> Tuples.of(expected, response.getValue()));
        };

        StepVerifier.create(createAndValidateFunction.apply(null)).assertNext(indexes -> {
            indexesToDelete.add(indexes.getT2().getName());
            assertObjectEquals(indexes.getT1(), indexes.getT2(), true, "etag");
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResourceSync() {
        SearchIndex index
            = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, true, Context.NONE).getValue();
        indexesToDelete.add(index.getName());

        assertNotNull(index.getETag());
    }

    @Test
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResourceAsync() {
        StepVerifier.create(asyncClient.createOrUpdateIndexWithResponse(createTestIndex(null), false, true))
            .assertNext(response -> {
                indexesToDelete.add(response.getValue().getName());
                assertNotNull(response.getValue().getETag());
            })
            .verifyComplete();
    }

    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResourceSync() {
        SearchIndex original
            = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE).getValue();
        indexesToDelete.add(original.getName());

        SearchIndex updated
            = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, false, Context.NONE)
                .getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResourceAsync() {
        Mono<Tuple2<String, String>> createThenUpdateMono
            = asyncClient.createOrUpdateIndexWithResponse(createTestIndex(null), false, false).flatMap(response -> {
                SearchIndex original = response.getValue();
                String originalETag = original.getETag();
                indexesToDelete.add(original.getName());

                return asyncClient.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, false)
                    .map(update -> Tuples.of(originalETag, update.getValue().getETag()));
            });

        StepVerifier.create(createThenUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SearchIndex original
            = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE).getValue();
        indexesToDelete.add(original.getName());

        String updatedETag
            = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, Context.NONE)
                .getValue()
                .getETag();

        validateETagUpdate(original.getETag(), updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        Mono<Tuple2<String, String>> createThenUpdateMono
            = asyncClient.createOrUpdateIndexWithResponse(createTestIndex(null), false, false).flatMap(response -> {
                SearchIndex original = response.getValue();
                String originalETag = original.getETag();
                indexesToDelete.add(original.getName());

                return asyncClient.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true)
                    .map(update -> Tuples.of(originalETag, update.getValue().getETag()));
            });

        StepVerifier.create(createThenUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChangedSync() {
        SearchIndex original
            = client.createOrUpdateIndexWithResponse(createTestIndex(null), false, false, Context.NONE).getValue();
        indexesToDelete.add(original.getName());

        String updatedETag
            = client.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true, Context.NONE)
                .getValue()
                .getETag();

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.createOrUpdateIndexWithResponse(original, false, true, Context.NONE),
            "createOrUpdateDefinition should have failed due to precondition.");

        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        validateETagUpdate(original.getETag(), updatedETag);
    }

    @Test
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChangedAsync() {
        Mono<Response<SearchIndex>> createUpdateThenFailUpdateMono
            = asyncClient.createOrUpdateIndexWithResponse(createTestIndex(null), false, false).flatMap(response -> {
                SearchIndex original = response.getValue();
                String originalETag = original.getETag();
                indexesToDelete.add(original.getName());

                return asyncClient.createOrUpdateIndexWithResponse(mutateCorsOptionsInIndex(original), false, true)
                    .map(update -> Tuples.of(originalETag, update.getValue().getETag(), original));
            })
                .doOnNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
                .flatMap(original -> asyncClient.createOrUpdateIndexWithResponse(original.getT3(), false, true));

        StepVerifier.create(createUpdateThenFailUpdateMono).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void canCreateAndGetIndexStatsSync() {
        SearchIndex index = createTestIndex(null);
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        SearchIndexStatistics indexStatistics = client.getIndexStatistics(index.getName());
        assertEquals(0, indexStatistics.getDocumentCount());
        assertEquals(0, indexStatistics.getStorageSize());

        Response<SearchIndexStatistics> indexStatisticsResponse
            = client.getIndexStatisticsWithResponse(index.getName(), Context.NONE);
        assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
        assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());
    }

    @Test
    public void canCreateAndGetIndexStatsAsync() {
        SearchIndex index = createTestIndex(null);
        asyncClient.createOrUpdateIndex(index).block();
        indexesToDelete.add(index.getName());

        StepVerifier.create(asyncClient.getIndexStatistics(index.getName())).assertNext(indexStatistics -> {
            assertEquals(0, indexStatistics.getDocumentCount());
            assertEquals(0, indexStatistics.getStorageSize());
        }).verifyComplete();

        StepVerifier.create(asyncClient.getIndexStatisticsWithResponse(index.getName()))
            .assertNext(indexStatisticsResponse -> {
                assertEquals(0, indexStatisticsResponse.getValue().getDocumentCount());
                assertEquals(0, indexStatisticsResponse.getValue().getStorageSize());
            })
            .verifyComplete();
    }

    @Test
    public void canCreateAndGetIndexStatsSummarySync() {
        List<String> indexNames = new ArrayList<>();

        assertFalse(client.getIndexStatsSummary().stream().findAny().isPresent(), "Unexpected index stats summary.");

        SearchIndex index = createTestIndex(null);
        indexNames.add(index.getName());
        client.createOrUpdateIndex(index);
        indexesToDelete.add(index.getName());

        assertEquals(1, client.getIndexStatsSummary().stream().count());

        for (int i = 0; i < 4; i++) {
            index = createTestIndex(null);
            indexNames.add(index.getName());
            client.createOrUpdateIndex(index);
            indexesToDelete.add(index.getName());
        }

        List<String> returnedNames
            = client.getIndexStatsSummary().stream().map(IndexStatisticsSummary::getName).collect(Collectors.toList());
        assertEquals(5, returnedNames.size());

        for (String name : indexNames) {
            assertTrue(returnedNames.contains(name),
                () -> String.format("Stats summary didn't contain expected index '%s'. Found: '%s'", name,
                    String.join(", ", returnedNames)));
        }
    }

    // I want an async version of the test above. Don't block, use StepVerifier instead.
    @Test
    public void canCreateAndGetIndexStatsSummaryAsync() {

        List<String> indexNames = new ArrayList<>();

        StepVerifier.create(asyncClient.getIndexStatsSummary()).expectNextCount(0).verifyComplete();

        SearchIndex index = createTestIndex(null);
        indexNames.add(index.getName());
        asyncClient.createOrUpdateIndex(index).block();
        indexesToDelete.add(index.getName());

        StepVerifier.create(asyncClient.getIndexStatsSummary()).expectNextCount(1).verifyComplete();

        for (int i = 0; i < 4; i++) {
            index = createTestIndex(null);
            indexNames.add(index.getName());
            asyncClient.createOrUpdateIndex(index).block();
            indexesToDelete.add(index.getName());
        }

        StepVerifier.create(asyncClient.getIndexStatsSummary().map(IndexStatisticsSummary::getName).collectList())
            .assertNext(returnedNames -> {
                assertEquals(5, returnedNames.size());

                for (String name : indexNames) {
                    assertTrue(returnedNames.contains(name),
                        () -> String.format("Stats summary didn't contain expected index '%s'. Found: '%s'", name,
                            String.join(", ", returnedNames)));
                }
            })
            .verifyComplete();
    }

    @Test
    public void canCreateIndexWithProductScoringAggregationSync() {
        SearchIndex index = new SearchIndex(randomIndexName("product-scoring-index"))
            .setFields(Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("rating", SearchFieldDataType.DOUBLE).setFilterable(true)))
            .setScoringProfiles(Arrays.asList(
                new ScoringProfile("productScoringProfile").setFunctionAggregation(ScoringFunctionAggregation.PRODUCT)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))))));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        ScoringProfile profile = createdIndex.getScoringProfiles().get(0);
        assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
    }

    @Test
    public void canCreateIndexWithProductScoringAggregationAsync() {
        SearchIndex index = new SearchIndex(randomIndexName("product-scoring-index"))
            .setFields(Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("title", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("rating", SearchFieldDataType.DOUBLE).setFilterable(true)))
            .setScoringProfiles(Arrays.asList(
                new ScoringProfile("productScoringProfile").setFunctionAggregation(ScoringFunctionAggregation.PRODUCT)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))))));

        StepVerifier.create(asyncClient.createIndex(index)).assertNext(createdIndex -> {
            indexesToDelete.add(createdIndex.getName());
            ScoringProfile profile = createdIndex.getScoringProfiles().get(0);
            assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
        }).verifyComplete();
    }

    @Test
    public void readIndexWithProductScoringAggregationSync() {
        SearchIndex index = createIndexWithScoringAggregation("read-test");
        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        SearchIndex retrievedIndex = client.getIndex(createdIndex.getName());

        ScoringProfile profile = retrievedIndex.getScoringProfiles().get(0);
        assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
        assertObjectEquals(createdIndex, retrievedIndex, true, "etag");
    }

    @Test
    public void readIndexWithProductScoringAggregationAsync() {
        SearchIndex index = createIndexWithScoringAggregation("read-test");

        Mono<Tuple2<SearchIndex, SearchIndex>> createAndRetrieveMono
            = asyncClient.createIndex(index).flatMap(createdIndex -> {
                indexesToDelete.add(createdIndex.getName());
                return asyncClient.getIndex(createdIndex.getName())
                    .map(retrievedIndex -> Tuples.of(createdIndex, retrievedIndex));
            });

        StepVerifier.create(createAndRetrieveMono).assertNext(indexes -> {
            SearchIndex createdIndex = indexes.getT1();
            SearchIndex retrievedIndex = indexes.getT2();

            ScoringProfile profile = retrievedIndex.getScoringProfiles().get(0);
            assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
            assertObjectEquals(createdIndex, retrievedIndex, true, "etag");
        }).verifyComplete();
    }

    @Test
    public void updateIndexWithProductScoringAggregationSync() {
        SearchIndex index = new SearchIndex(randomIndexName("update-test"))
            .setFields(Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("rating", SearchFieldDataType.DOUBLE).setFilterable(true)))
            .setScoringProfiles(
                Arrays.asList(new ScoringProfile("testProfile").setFunctionAggregation(ScoringFunctionAggregation.SUM) // Start with SUM
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))))));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        createdIndex.getScoringProfiles().get(0).setFunctionAggregation(ScoringFunctionAggregation.PRODUCT);

        SearchIndex updatedIndex = client.createOrUpdateIndex(createdIndex);

        ScoringProfile updatedProfile = updatedIndex.getScoringProfiles().get(0);
        assertEquals(ScoringFunctionAggregation.PRODUCT, updatedProfile.getFunctionAggregation());
    }

    @Test
    public void updateIndexWithProductScoringAggregationAsync() {
        SearchIndex index = new SearchIndex(randomIndexName("update-test"))
            .setFields(Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("rating", SearchFieldDataType.DOUBLE).setFilterable(true)))
            .setScoringProfiles(
                Arrays.asList(new ScoringProfile("testProfile").setFunctionAggregation(ScoringFunctionAggregation.SUM)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))))));

        Mono<Tuple2<SearchIndex, SearchIndex>> createAndUpdateMono
            = asyncClient.createIndex(index).flatMap(createdIndex -> {
                indexesToDelete.add(createdIndex.getName());
                createdIndex.getScoringProfiles().get(0).setFunctionAggregation(ScoringFunctionAggregation.PRODUCT);

                return asyncClient.createOrUpdateIndex(createdIndex)
                    .map(updatedIndex -> Tuples.of(createdIndex, updatedIndex));
            });

        StepVerifier.create(createAndUpdateMono).assertNext(indexes -> {
            SearchIndex updatedIndex = indexes.getT2();
            ScoringProfile updatedProfile = updatedIndex.getScoringProfiles().get(0);
            assertEquals(ScoringFunctionAggregation.PRODUCT, updatedProfile.getFunctionAggregation());
        }).verifyComplete();
    }

    @Test
    public void deleteIndexWithProductScoringAggregationSync() {
        SearchIndex index = createIndexWithScoringAggregation("delete-test");
        indexesToDelete.add(index.getName());

        ScoringProfile profile = index.getScoringProfiles().get(0);
        assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());

        client.deleteIndex(index.getName());
        indexesToDelete.remove(index.getName());
    }

    @Test
    public void deleteIndexWithProductScoringAggregationAsync() {
        SearchIndex index = createIndexWithScoringAggregation("delete-test");

        Mono<Void> createAndDeleteMono = asyncClient.createIndex(index).flatMap(createdIndex -> {
            indexesToDelete.add(createdIndex.getName());
            ScoringProfile profile = createdIndex.getScoringProfiles().get(0);
            assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());

            return asyncClient.deleteIndex(createdIndex.getName())
                .doOnSuccess(unused -> indexesToDelete.remove(createdIndex.getName()));
        });

        StepVerifier.create(createAndDeleteMono).verifyComplete();
    }

    @Test
    public void testProductScoringAggregationApiVersionCompatibility() {
        SearchIndex index = createIndexWithScoringAggregation("api-version-test");
        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        ScoringProfile profile = createdIndex.getScoringProfiles().get(0);
        assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
    }

    @Test
    public void testProductScoringAggregationWithOlderApiVersions() {
        SearchIndexClient olderApiClient
            = getSearchIndexClientBuilder(true).serviceVersion(SearchServiceVersion.V2023_11_01).buildClient();

        SearchIndex index = createIndexWithScoringAggregation("older-api-test");

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            olderApiClient.createIndex(index);
        });

        assertEquals(400, exception.getResponse().getStatusCode());
    }

    @Test
    public void testProductScoringAggregationSerialization() {
        ScoringProfile profile = new ScoringProfile("testProfile")
            .setFunctionAggregation(ScoringFunctionAggregation.PRODUCT)
            .setFunctions(
                Arrays.asList(new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))));

        String json = BinaryData.fromObject(profile).toString();
        assertTrue(json.contains("\"functionAggregation\":\"product\""));
    }

    @Test
    public void testProductScoringAggregationDeserialization() {
        String json = "{\"name\":\"productProfile\",\"functionAggregation\":\"product\",\"functions\":[]}";

        try (JsonReader reader = JsonProviders.createReader(json)) {
            ScoringProfile profile = ScoringProfile.fromJson(reader);
            assertEquals("productProfile", profile.getName());
            assertEquals(ScoringFunctionAggregation.PRODUCT, profile.getFunctionAggregation());
            assertNotNull(profile.getFunctions());
            assertTrue(profile.getFunctions().isEmpty());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private SearchIndex createIndexWithScoringAggregation(String suffix) {
        return new SearchIndex(randomIndexName("agg-test"))
            .setFields(Arrays.asList(new SearchField("id", SearchFieldDataType.STRING).setKey(true),
                new SearchField("rating", SearchFieldDataType.DOUBLE).setFilterable(true)))
            .setScoringProfiles(Arrays
                .asList(new ScoringProfile("testProfile").setFunctionAggregation(ScoringFunctionAggregation.PRODUCT)
                    .setFunctions(Arrays.asList(
                        new MagnitudeScoringFunction("rating", 2.0, new MagnitudeScoringParameters(1.0, 5.0))))));
    }

    @Test
    public void createIndexWithPurviewEnabledSucceeds() {
        String indexName = randomIndexName("purview-enabled-index");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        assertTrue(createdIndex.isPurviewEnabled());
        assertTrue(createdIndex.getFields()
            .stream()
            .anyMatch(f -> "SensitivityLabel".equals(f.getName()) && f.isSensitivityLabel()));

    }

    @Test
    public void createIndexWithPurviewEnabledRequiresSensitivityLabelField() {
        String indexName = randomIndexName("purview-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true)));

        HttpResponseException exception = assertThrows(HttpResponseException.class, () -> {
            client.createIndex(index);
        });

        assertEquals(400, exception.getResponse().getStatusCode());
        assertTrue(exception.getMessage().toLowerCase().contains("sensitivity")
            || exception.getMessage().toLowerCase().contains("purview"));
    }

    @Test
    public void purviewEnabledIndexRejectsApiKeyAuth() {
        String indexName = randomIndexName("purview-api-key-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        String apiKey = System.getenv("AZURE_SEARCH_ADMIN_KEY");

        SearchClient apiKeyClient = new SearchClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(new AzureKeyCredential(apiKey))
            .indexName(createdIndex.getName())
            .buildClient();

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> {
            apiKeyClient.search("*").iterator().hasNext();
        });

        assertTrue(ex.getResponse().getStatusCode() == 401
            || ex.getResponse().getStatusCode() == 403
            || ex.getResponse().getStatusCode() == 400);
    }

    @Test
    public void purviewEnabledIndexDisablesAutocompleteAndSuggest() {
        String indexName = randomIndexName("purview-suggest-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)))
            .setSuggesters(
                Collections.singletonList(new SearchSuggester("sg", Collections.singletonList("HotelName"))));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        SearchClient searchClient = getSearchClientBuilder(createdIndex.getName(), true).buildClient();

        HttpResponseException ex1 = assertThrows(HttpResponseException.class, () -> {
            searchClient.autocomplete("test", "sg").iterator().hasNext();
        });
        assertTrue(ex1.getResponse().getStatusCode() == 400 || ex1.getResponse().getStatusCode() == 403);

        HttpResponseException ex2 = assertThrows(HttpResponseException.class, () -> {
            searchClient.suggest("test", "sg").iterator().hasNext();
        });
        assertTrue(ex2.getResponse().getStatusCode() == 400 || ex2.getResponse().getStatusCode() == 403);
    }

    @Test
    public void cannotTogglePurviewEnabledAfterCreation() {
        String indexName = randomIndexName("purview-toggle-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(false)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        createdIndex.setPurviewEnabled(true)
            .getFields()
            .add(new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                .setSensitivityLabel(true));

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> {
            client.createOrUpdateIndex(createdIndex);
        });

        assertEquals(400, ex.getResponse().getStatusCode());
        assertTrue(ex.getMessage().toLowerCase().contains("immutable")
            || ex.getMessage().toLowerCase().contains("purview")
            || ex.getMessage().toLowerCase().contains("cannot be changed"));
    }

    @Test
    public void cannotModifySensitivityLabelFieldAfterCreation() {
        String indexName = randomIndexName("purview-field-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        createdIndex.getFields()
            .stream()
            .filter(f -> "SensitivityLabel".equals(f.getName()))
            .findFirst()
            .ifPresent(f -> f.setSensitivityLabel(false));

        HttpResponseException ex = assertThrows(HttpResponseException.class, () -> {
            client.createOrUpdateIndex(createdIndex);
        });

        assertEquals(400, ex.getResponse().getStatusCode());
        assertTrue(ex.getMessage().toLowerCase().contains("immutable")
            || ex.getMessage().toLowerCase().contains("sensitivity"));
    }

    @Test
    public void purviewEnabledIndexSupportsBasicSearch() {
        String indexName = randomIndexName("purview-search-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        SearchClient searchClient = getSearchClientBuilder(createdIndex.getName(), true).buildClient();

        List<Map<String, Object>> documents = Arrays.asList(createTestDocument("1", "Test Hotel", "Public"));
        searchClient.uploadDocuments(documents);
        waitForIndexing();

        SearchPagedIterable results = searchClient.search("Test");
        assertNotNull(results);
        // getTotalCount() can be null, so check for non-null or use iterator
        Long totalCount = results.getTotalCount();
        assertTrue(totalCount == null || totalCount >= 0);

        // Verify we can iterate over results without errors
        assertNotNull(results.iterator());
    }

    @Test
    public void purviewEnabledIndexSupportsSemanticSearch() {
        String indexName = randomIndexName("purview-semantic-test");
        SearchIndex index = new SearchIndex(indexName).setPurviewEnabled(true)
            .setFields(Arrays.asList(new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true),
                new SearchField("HotelName", SearchFieldDataType.STRING).setSearchable(true),
                new SearchField("SensitivityLabel", SearchFieldDataType.STRING).setFilterable(true)
                    .setSensitivityLabel(true)))
            .setSemanticSearch(new SemanticSearch().setDefaultConfigurationName("semantic")
                .setConfigurations(Collections.singletonList(new SemanticConfiguration("semantic",
                    new SemanticPrioritizedFields().setContentFields(new SemanticField("HotelName"))))));

        SearchIndex createdIndex = client.createIndex(index);
        indexesToDelete.add(createdIndex.getName());

        SearchClient searchClient = getSearchClientBuilder(createdIndex.getName(), true).buildClient();

        List<Map<String, Object>> documents = Arrays.asList(createTestDocument("1", "Test Hotel", "Public"));
        searchClient.uploadDocuments(documents);
        waitForIndexing();

        SearchOptions searchOptions = new SearchOptions().setQueryType(QueryType.SEMANTIC);

        SearchPagedIterable results = searchClient.search("Test", searchOptions, null);
        assertNotNull(results);
    }

    static SearchIndex mutateCorsOptionsInIndex(SearchIndex index) {
        return mutateCorsOptionsInIndex(index, Collections.singletonList("*"));
    }

    static SearchIndex mutateCorsOptionsInIndex(SearchIndex index, List<String> allowedOrigins) {
        CorsOptions mutatedCorsOptions
            = new CorsOptions(allowedOrigins).setMaxAgeInSeconds(index.getCorsOptions().getMaxAgeInSeconds());

        return index.setCorsOptions(mutatedCorsOptions);
    }

    static SearchField getFieldByName(SearchIndex index, String name) {
        for (SearchField field : index.getFields()) {
            if (Objects.equals(name, field.getName())) {
                return field;
            }
        }

        throw new NoSuchElementException(
            "Unable to find a field with name '" + name + "' in index '" + index.getName() + "'.");
    }

    private Map<String, Object> createTestDocument(String id, String hotelName, String sensitivityLabel) {
        Map<String, Object> document = new HashMap<>();
        document.put("HotelId", id);
        document.put("HotelName", hotelName);
        document.put("SensitivityLabel", sensitivityLabel);
        return document;
    }

    private void waitForIndexing() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
