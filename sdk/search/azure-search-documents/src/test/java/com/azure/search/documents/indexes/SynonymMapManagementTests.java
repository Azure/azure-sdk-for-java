// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.ifMatch;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class SynonymMapManagementTests extends SearchTestBase {
    private final List<String> synonymMapsToDelete = new ArrayList<>();

    private SearchIndexClient client;
    private SearchIndexAsyncClient asyncClient;

    @BeforeAll
    public static void beforeAll() {
        // When running against the live service ensure all synonym maps are deleted before running these tests.
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to cleanup.
        }

        SearchIndexClient cleanupClient = new SearchIndexClientBuilder().endpoint(SEARCH_ENDPOINT)
            .credential(TestHelpers.getTestTokenCredential())
            .buildClient();

        boolean synonymMapDeleted = false;
        for (String synonymMap : cleanupClient.listSynonymMapNames()) {
            cleanupClient.deleteSynonymMap(synonymMap);
            synonymMapDeleted = true;
        }

        if (synonymMapDeleted) {
            TestHelpers.sleepIfRunningAgainstService(3000);
        }
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

        boolean synonymMapsDeleted = false;
        for (String synonymMap : synonymMapsToDelete) {
            client.deleteSynonymMap(synonymMap);
            synonymMapsDeleted = true;
        }

        if (synonymMapsDeleted) {
            sleepIfRunningAgainstService(5000);
        }
    }

    @Test
    public void createAndGetSynonymMapReturnsCorrectDefinitionSync() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createSynonymMap(expectedSynonymMap);
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);

        actualSynonymMap = client.getSynonymMap(expectedSynonymMap.getName());
        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionAsync() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();

        StepVerifier.create(asyncClient.createSynonymMap(expectedSynonymMap)).assertNext(actual -> {
            synonymMapsToDelete.add(actual.getName());
            assertSynonymMapsEqual(expectedSynonymMap, actual);
        }).verifyComplete();

        StepVerifier.create(asyncClient.getSynonymMap(expectedSynonymMap.getName()))
            .assertNext(actual -> assertSynonymMapsEqual(expectedSynonymMap, actual))
            .verifyComplete();
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponseSync() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createSynonymMapWithResponse(expectedSynonymMap, null).getValue();
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);

        actualSynonymMap = client.getSynonymMapWithResponse(expectedSynonymMap.getName(), null).getValue();
        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponseAsync() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        StepVerifier.create(asyncClient.createSynonymMapWithResponse(expectedSynonymMap, null))
            .assertNext(response -> {
                SynonymMap synonymMap = response.getValue();
                synonymMapsToDelete.add(synonymMap.getName());
                assertSynonymMapsEqual(expectedSynonymMap, synonymMap);
            })
            .verifyComplete();

        StepVerifier.create(asyncClient.getSynonymMapWithResponse(expectedSynonymMap.getName(), null))
            .assertNext(response -> assertSynonymMapsEqual(expectedSynonymMap, response.getValue()))
            .verifyComplete();
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserErrorSync() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap("a => b => c");

        assertHttpResponseException(() -> client.createSynonymMap(expectedSynonymMap),
            HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserErrorAsync() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap("a => b => c");

        StepVerifier.create(asyncClient.createSynonymMap(expectedSynonymMap)).verifyErrorSatisfies(throwable -> {
            HttpResponseException ex = assertInstanceOf(HttpResponseException.class, throwable);
            assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ex.getResponse().getStatusCode());
        });
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundSync() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(() -> client.getSynonymMap(synonymMapName), HttpURLConnection.HTTP_NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundAsync() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        StepVerifier.create(asyncClient.getSynonymMap(synonymMapName))
            .verifyErrorSatisfies(
                throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_NOT_FOUND, exceptionMessage));
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponseSync() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(() -> client.getSynonymMapWithResponse(synonymMapName, null),
            HttpURLConnection.HTTP_NOT_FOUND, exceptionMessage);
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponseAsync() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        StepVerifier.create(asyncClient.getSynonymMapWithResponse(synonymMapName, null))
            .verifyErrorSatisfies(
                throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_NOT_FOUND, exceptionMessage));
    }

    @Test
    public void canUpdateSynonymMapSync() {
        SynonymMap initial = createTestSynonymMap();
        client.createSynonymMap(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = new SynonymMap(initial.getName(), "newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateSynonymMap(updatedExpected);
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        assertEquals(1, client.listSynonymMaps().getSynonymMaps().size());
    }

    @Test
    public void canUpdateSynonymMapAsync() {
        Mono<Tuple2<SynonymMap, SynonymMap>> createAndUpdateMono
            = asyncClient.createSynonymMap(createTestSynonymMap()).flatMap(original -> {
                synonymMapsToDelete.add(original.getName());

                SynonymMap updatedExpected = new SynonymMap(original.getName(), "newword1,newword2");
                return asyncClient.createOrUpdateSynonymMap(updatedExpected)
                    .map(updateActual -> Tuples.of(updatedExpected, updateActual));
            });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(synonyms -> assertSynonymMapsEqual(synonyms.getT1(), synonyms.getT2()))
            .verifyComplete();

        StepVerifier.create(asyncClient.listSynonymMaps()).expectNextCount(1).verifyComplete();
    }

    @Test
    public void canUpdateSynonymMapWithResponseSync() {
        SynonymMap initial = createTestSynonymMap();
        client.createSynonymMap(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = new SynonymMap(initial.getName(), "newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateSynonymMapWithResponse(updatedExpected, null).getValue();
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        assertEquals(1, client.listSynonymMaps().getSynonymMaps().size());
    }

    @Test
    public void canUpdateSynonymMapWithResponseAsync() {
        Mono<Tuple2<SynonymMap, SynonymMap>> createAndUpdateMono
            = asyncClient.createSynonymMap(createTestSynonymMap()).flatMap(original -> {
                synonymMapsToDelete.add(original.getName());
                SynonymMap updatedExpected = new SynonymMap(original.getName(), "newword1,newword2");

                return asyncClient.createOrUpdateSynonymMapWithResponse(updatedExpected, null)
                    .map(response -> Tuples.of(updatedExpected, response.getValue()));
            });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(synonyms -> assertSynonymMapsEqual(synonyms.getT1(), synonyms.getT2()))
            .verifyComplete();

        StepVerifier.create(asyncClient.listSynonymMaps()).expectNextCount(1).verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistSync() {
        SynonymMap expected = createTestSynonymMap();
        SynonymMap actual = client.createOrUpdateSynonymMap(expected);
        synonymMapsToDelete.add(expected.getName());

        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistAsync() {
        SynonymMap expected = createTestSynonymMap();

        StepVerifier.create(asyncClient.createOrUpdateSynonymMap(expected)).assertNext(actual -> {
            synonymMapsToDelete.add(actual.getName());
            assertSynonymMapsEqual(expected, actual);
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponseSync() {
        SynonymMap expected = createTestSynonymMap();
        Response<SynonymMap> createOrUpdateResponse = client.createOrUpdateSynonymMapWithResponse(expected, null);
        synonymMapsToDelete.add(expected.getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponseAsync() {
        SynonymMap expected = createTestSynonymMap();

        StepVerifier.create(asyncClient.createOrUpdateSynonymMapWithResponse(expected, null)).assertNext(response -> {
            synonymMapsToDelete.add(response.getValue().getName());

            assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode());
            assertSynonymMapsEqual(expected, response.getValue());
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResourceSync() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap created = client.createOrUpdateSynonymMapWithResponse(synonymMap, null).getValue();
        synonymMapsToDelete.add(created.getName());

        assertNotNull(created.getETag());
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResourceAsync() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier.create(asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, null)).assertNext(response -> {
            synonymMapsToDelete.add(response.getValue().getName());
            assertNotNull(response.getValue().getETag());
        }).verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResourceSync() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, null).getValue();
        synonymMapsToDelete.add(original.getName());

        original.getSynonyms().clear();
        original.getSynonyms().add("mutated1, mutated2");
        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(original, null).getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResourceAsync() {
        SynonymMap synonymMap = createTestSynonymMap();

        Mono<Tuple2<String, String>> createAndUpdateMono
            = asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, null).flatMap(response -> {
                SynonymMap original = response.getValue();
                synonymMapsToDelete.add(original.getName());

                original.getSynonyms().clear();
                original.getSynonyms().add("mutated1, mutated2");
                return asyncClient.createOrUpdateSynonymMapWithResponse(original, null)
                    .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
            });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchangedSync() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, null).getValue();
        synonymMapsToDelete.add(original.getName());

        original.getSynonyms().clear();
        original.getSynonyms().add("mutated1, mutated2");
        SynonymMap updated
            = client.createOrUpdateSynonymMapWithResponse(original, ifMatch(original.getETag())).getValue();

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchangedAsync() {
        SynonymMap synonymMap = createTestSynonymMap();

        Mono<Tuple2<String, String>> createAndUpdateMono
            = asyncClient.createOrUpdateSynonymMapWithResponse(synonymMap, null).flatMap(response -> {
                SynonymMap original = response.getValue();
                synonymMapsToDelete.add(original.getName());

                original.getSynonyms().clear();
                original.getSynonyms().add("mutated1, mutated2");
                return asyncClient.createOrUpdateSynonymMapWithResponse(original, ifMatch(original.getETag()))
                    .map(update -> Tuples.of(original.getETag(), update.getValue().getETag()));
            });

        StepVerifier.create(createAndUpdateMono)
            .assertNext(etags -> validateETagUpdate(etags.getT1(), etags.getT2()))
            .verifyComplete();
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChangedSyncAndAsync() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, null).getValue();
        synonymMapsToDelete.add(original.getName());

        original.getSynonyms().clear();
        original.getSynonyms().add("mutated1, mutated2");
        SynonymMap updated
            = client.createOrUpdateSynonymMapWithResponse(original, ifMatch(original.getETag())).getValue();

        // Update and check the eTags were changed
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.createOrUpdateSynonymMapWithResponse(original, ifMatch(original.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.createOrUpdateSynonymMapWithResponse(original, ifMatch(original.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });

        validateETagUpdate(original.getETag(), updated.getETag());
    }

    @Test
    public void deleteSynonymMapIsIdempotentSync() {
        SynonymMap synonymMap = createTestSynonymMap();
        Response<?> deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<?> createResponse = client.createSynonymMapWithResponse(synonymMap, null);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(), null);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void deleteSynonymMapIsIdempotentAsync() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier.create(asyncClient.deleteSynonymMapWithResponse(synonymMap.getName(), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.createSynonymMapWithResponse(synonymMap, null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_CREATED, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.deleteSynonymMapWithResponse(synonymMap.getName(), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getStatusCode()))
            .verifyComplete();

        StepVerifier.create(asyncClient.deleteSynonymMapWithResponse(synonymMap.getName(), null))
            .assertNext(response -> assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void canCreateAndListSynonymMapsSyncAndAsync() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();

        client.createSynonymMap(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        client.createSynonymMap(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        Map<String, SynonymMap> expectedSynonyms = new HashMap<>();
        expectedSynonyms.put(synonymMap1.getName(), synonymMap1);
        expectedSynonyms.put(synonymMap2.getName(), synonymMap2);

        Map<String, SynonymMap> actualSynonyms = client.listSynonymMaps()
            .getSynonymMaps()
            .stream()
            .collect(Collectors.toMap(SynonymMap::getName, sm -> sm));

        compareMaps(expectedSynonyms, actualSynonyms, (expected, actual) -> assertObjectEquals(expected, actual, true));

        StepVerifier.create(asyncClient.listSynonymMaps()
            .map(result -> result.getSynonymMaps().stream().collect(Collectors.toMap(SynonymMap::getName, sm -> sm))))
            .assertNext(actualSynonyms2 -> compareMaps(expectedSynonyms, actualSynonyms2,
                (expected, actual) -> assertObjectEquals(expected, actual, true)))
            .verifyComplete();
    }

    @Test
    public void canListSynonymMapsWithSelectedFieldSyncAndAsync() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();

        client.createSynonymMap(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        client.createSynonymMap(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        Set<String> expectedSynonymNames = new HashSet<>(Arrays.asList(synonymMap1.getName(), synonymMap2.getName()));
        Set<String> actualSynonymNames = new HashSet<>(client.listSynonymMapNames());

        assertEquals(expectedSynonymNames.size(), actualSynonymNames.size());
        assertTrue(actualSynonymNames.containsAll(expectedSynonymNames));

        StepVerifier.create(asyncClient.listSynonymMapNames().map(HashSet::new)).assertNext(actualSynonymNames2 -> {
            assertEquals(expectedSynonymNames.size(), actualSynonymNames2.size());
            assertTrue(actualSynonymNames2.containsAll(expectedSynonymNames));
        }).verifyComplete();
    }

    @Test
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResourceSyncAndAsync() {
        SynonymMap stale = client.createOrUpdateSynonymMapWithResponse(createTestSynonymMap(), null).getValue();

        // Update the resource, the eTag will be changed
        SynonymMap current = client.createOrUpdateSynonymMapWithResponse(stale, ifMatch(stale.getETag())).getValue();

        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSynonymMapWithResponse(stale.getName(), ifMatch(stale.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.deleteSynonymMapWithResponse(stale.getName(), ifMatch(stale.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });

        client.deleteSynonymMapWithResponse(current.getName(), ifMatch(current.getETag()));
    }

    @Test
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExistsSyncAndAsync() {
        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(createTestSynonymMap(), null).getValue();

        client.deleteSynonymMapWithResponse(updated.getName(), ifMatch(updated.getETag()));

        // Try to delete again and expect to fail
        HttpResponseException ex = assertThrows(HttpResponseException.class,
            () -> client.deleteSynonymMapWithResponse(updated.getName(), ifMatch(updated.getETag())));
        assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());

        StepVerifier.create(asyncClient.deleteSynonymMapWithResponse(updated.getName(), ifMatch(updated.getETag())))
            .verifyErrorSatisfies(throwable -> {
                HttpResponseException exAsync = assertInstanceOf(HttpResponseException.class, throwable);
                assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, exAsync.getResponse().getStatusCode());
            });
    }

    static void assertSynonymMapsEqual(SynonymMap expected, SynonymMap actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSynonyms(), actual.getSynonyms());
    }

    SynonymMap createTestSynonymMap() {
        return new SynonymMap(testResourceNamer.randomName("test-synonym", 32), "word1,word2");
    }

    SynonymMap createTestSynonymMap(String... synonyms) {
        return new SynonymMap(testResourceNamer.randomName("test-synonym", 32), synonyms);
    }
}
