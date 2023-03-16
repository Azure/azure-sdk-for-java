// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.SynonymMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SynonymMapManagementSyncTests extends SearchTestBase {
    private final List<String> synonymMapsToDelete = new ArrayList<>();

    private SearchIndexClient client;

    @BeforeAll
    public static void beforeAll() {
        // When running against the live service ensure all synonym maps are deleted before running these tests.
        if (TEST_MODE == TestMode.PLAYBACK) {
            return; // Running in PLAYBACK, no need to cleanup.
        }

        SearchIndexClient cleanupClient = new SearchIndexClientBuilder()
            .endpoint(ENDPOINT)
            .credential(new AzureKeyCredential(API_KEY))
            .buildClient();

        boolean synonymMapDeleted = false;
        for (String synonymMap : cleanupClient.listSynonymMapNames()) {
            cleanupClient.deleteSynonymMap(synonymMap);
            synonymMapDeleted = true;
        }

        if (synonymMapDeleted) {
            TestHelpers.sleepIfRunningAgainstService(5000);
        }
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchIndexClientBuilder().buildClient();
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
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createSynonymMap(expectedSynonymMap);
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createSynonymMapWithResponse(expectedSynonymMap, Context.NONE).getValue();
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap()
            .setSynonyms("a => b => c");

        assertHttpResponseException(
            () -> client.createSynonymMap(expectedSynonymMap),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Syntax error in line 1: 'a => b => c'. Only one explicit mapping (=>) can be specified in a synonym rule."
        );
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();
        client.createSynonymMap(expected);
        synonymMapsToDelete.add(expected.getName());

        SynonymMap actual = client.getSynonymMap(expected.getName());
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        client.createSynonymMap(expected);
        synonymMapsToDelete.add(expected.getName());

        SynonymMap actual = client.getSynonymMapWithResponse(expected.getName(), Context.NONE)
            .getValue();
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> client.getSynonymMapWithResponse(synonymMapName, Context.NONE),
            HttpURLConnection.HTTP_NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponse() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> client.getSynonymMapWithResponse(synonymMapName, Context.NONE),
            HttpURLConnection.HTTP_NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();
        client.createSynonymMap(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = new SynonymMap(initial.getName(), "newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateSynonymMap(updatedExpected);
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = client.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void canUpdateSynonymMapWithResponse() {
        SynonymMap initial = createTestSynonymMap();
        client.createSynonymMap(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = new SynonymMap(initial.getName(), "newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateSynonymMapWithResponse(updatedExpected, false, Context.NONE)
            .getValue();
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = client.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();
        SynonymMap actual = client.createOrUpdateSynonymMap(expected);
        synonymMapsToDelete.add(expected.getName());

        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        Response<SynonymMap> createOrUpdateResponse = client.createOrUpdateSynonymMapWithResponse(
            expected, false, Context.NONE);
        synonymMapsToDelete.add(expected.getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap created = client.createOrUpdateSynonymMapWithResponse(synonymMap, true, Context.NONE)
            .getValue();
        synonymMapsToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(original.setSynonyms("mutated1, mutated2"),
            false, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(original.setSynonyms("mutated1, mutated2"),
            true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = client.createOrUpdateSynonymMapWithResponse(synonymMap, false, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(original.setSynonyms("mutated1, mutated2"),
            true, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            client.createOrUpdateSynonymMapWithResponse(original, true, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();
        Response<Void> deleteResponse = client.deleteSynonymMapWithResponse(synonymMap, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SynonymMap> createResponse = client.createSynonymMapWithResponse(synonymMap, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap, false, Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();
        client.createSynonymMap(synonymMap);
        client.deleteSynonymMap(synonymMap.getName());
        assertThrows(HttpResponseException.class, () -> client.getSynonymMap(synonymMap.getName()));
    }

    @Test
    public void canCreateAndListSynonymMaps() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();

        client.createSynonymMap(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        client.createSynonymMap(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        Iterator<SynonymMap> actual = client.listSynonymMaps().iterator();

        SynonymMap temp = actual.next();
        if (synonymMap1.getName().equals(temp.getName())) {
            assertObjectEquals(synonymMap1, temp, true);
            assertObjectEquals(synonymMap2, actual.next(), true);
        } else {
            assertObjectEquals(synonymMap2, temp, true);
            assertObjectEquals(synonymMap1, actual.next(), true);
        }

        assertFalse(actual.hasNext());
    }

    @Test
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();
        Set<String> expectedNames = new HashSet<>();
        expectedNames.add(synonymMap1.getName());
        expectedNames.add(synonymMap2.getName());

        client.createSynonymMap(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        client.createSynonymMap(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        PagedIterable<String> listResponse = client.listSynonymMapNames(Context.NONE);
        List<String> result = listResponse.stream().collect(Collectors.toList());

        result.forEach(Assertions::assertNotNull);

        assertEquals(2, result.size());
        expectedNames.containsAll(result);
    }

    @Test
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        SynonymMap stale = client.createOrUpdateSynonymMapWithResponse(createTestSynonymMap(), true, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        SynonymMap current = client.createOrUpdateSynonymMapWithResponse(stale, true, Context.NONE)
            .getValue();

        try {
            client.deleteSynonymMapWithResponse(stale, true, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        client.deleteSynonymMapWithResponse(current, true, Context.NONE);
    }

    @Test
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        SynonymMap updated = client.createOrUpdateSynonymMapWithResponse(createTestSynonymMap(), false, Context.NONE)
            .getValue();

        client.deleteSynonymMapWithResponse(updated, true, Context.NONE);

        // Try to delete again and expect to fail
        try {
            client.deleteSynonymMapWithResponse(updated, true, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (HttpResponseException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    void assertSynonymMapsEqual(SynonymMap actual, SynonymMap expected) {
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getSynonyms(), expected.getSynonyms());
    }

    SynonymMap createTestSynonymMap() {
        return new SynonymMap(testResourceNamer.randomName("test-synonym", 32), "word1,word2");
    }
}
