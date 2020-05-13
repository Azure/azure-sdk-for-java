// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.search.documents.indexes.SynonymMapClient;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.models.SearchErrorException;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.generateRequestOptions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class SynonymMapManagementSyncTests extends SearchTestBase {
    private final List<String> synonymMapsToDelete = new ArrayList<>();

    private SearchServiceClient client;
    private SynonymMapClient synonymMapClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
        synonymMapClient = client.getSynonymMapClient();
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        boolean synonymMapsDeleted = false;
        for (String synonymMap : synonymMapsToDelete) {
            synonymMapClient.delete(synonymMap);
            synonymMapsDeleted = true;
        }

        if (synonymMapsDeleted) {
            sleepIfRunningAgainstService(5000);
        }
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = synonymMapClient.create(expectedSynonymMap);
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = synonymMapClient.createWithResponse(expectedSynonymMap,
            generateRequestOptions(), Context.NONE).getValue();
        synonymMapsToDelete.add(actualSynonymMap.getName());

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap()
            .setSynonyms("a => b => c");

        assertHttpResponseException(
            () -> synonymMapClient.create(expectedSynonymMap),
            HttpURLConnection.HTTP_BAD_REQUEST,
            "Syntax error in line 1: 'a => b => c'. Only one explicit mapping (=>) can be specified in a synonym rule."
        );
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();
        synonymMapClient.create(expected);
        synonymMapsToDelete.add(expected.getName());

        SynonymMap actual = synonymMapClient.getSynonymMap(expected.getName());
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        synonymMapClient.create(expected);
        synonymMapsToDelete.add(expected.getName());

        SynonymMap actual = synonymMapClient.getSynonymMapWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> synonymMapClient.getSynonymMapWithResponse(synonymMapName, generateRequestOptions(), Context.NONE),
            HttpURLConnection.HTTP_NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponse() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> synonymMapClient.getSynonymMapWithResponse(synonymMapName, generateRequestOptions(), Context.NONE),
            HttpURLConnection.HTTP_NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();
        synonymMapClient.create(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        SynonymMap updatedActual = synonymMapClient.createOrUpdate(updatedExpected);
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = synonymMapClient.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void canUpdateSynonymMapWithResponse() {
        SynonymMap initial = createTestSynonymMap();
        synonymMapClient.create(initial);
        synonymMapsToDelete.add(initial.getName());

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        SynonymMap updatedActual = synonymMapClient.createOrUpdateWithResponse(updatedExpected, false,
            generateRequestOptions(), Context.NONE).getValue();
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = synonymMapClient.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();
        SynonymMap actual = synonymMapClient.createOrUpdate(expected);
        synonymMapsToDelete.add(expected.getName());

        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        Response<SynonymMap> createOrUpdateResponse = synonymMapClient.createOrUpdateWithResponse(
            expected, false, generateRequestOptions(), Context.NONE);
        synonymMapsToDelete.add(expected.getName());

        assertEquals(HttpURLConnection.HTTP_CREATED, createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap created = synonymMapClient.createOrUpdateWithResponse(synonymMap, true, null, Context.NONE)
            .getValue();
        synonymMapsToDelete.add(created.getName());

        assertFalse(CoreUtils.isNullOrEmpty(created.getETag()));
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = synonymMapClient.createOrUpdateWithResponse(synonymMap, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = synonymMapClient.createOrUpdateWithResponse(original.setSynonyms("mutated1, mutated2"),
            false, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = synonymMapClient.createOrUpdateWithResponse(synonymMap, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = synonymMapClient.createOrUpdateWithResponse(original.setSynonyms("mutated1, mutated2"),
            true, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        SynonymMap synonymMap = createTestSynonymMap();

        SynonymMap original = synonymMapClient.createOrUpdateWithResponse(synonymMap, false, null, Context.NONE)
            .getValue();
        String originalETag = original.getETag();
        synonymMapsToDelete.add(original.getName());

        SynonymMap updated = synonymMapClient.createOrUpdateWithResponse(original.setSynonyms("mutated1, mutated2"),
            true, null, Context.NONE)
            .getValue();
        String updatedETag = updated.getETag();

        // Update and check the eTags were changed
        try {
            synonymMapClient.createOrUpdateWithResponse(original, true, null, Context.NONE);
            fail("createOrUpdateDefinition should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    @Test
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();
        Response<Void> deleteResponse = synonymMapClient.deleteWithResponse(synonymMap, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());

        Response<SynonymMap> createResponse = synonymMapClient.createWithResponse(synonymMap,
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_CREATED, createResponse.getStatusCode());

        deleteResponse = synonymMapClient.deleteWithResponse(synonymMap, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, deleteResponse.getStatusCode());

        deleteResponse = synonymMapClient.deleteWithResponse(synonymMap, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();
        synonymMapClient.create(synonymMap);
        synonymMapClient.delete(synonymMap.getName());
        assertThrows(HttpResponseException.class, () -> synonymMapClient.getSynonymMap(synonymMap.getName()));
    }

    @Test
    public void canCreateAndListSynonymMaps() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();
        Set<String> expectedNames = new HashSet<>();
        expectedNames.add(synonymMap1.getName());
        expectedNames.add(synonymMap2.getName());

        synonymMapClient.create(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        synonymMapClient.create(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        PagedIterable<SynonymMap> actual = synonymMapClient.listSynonymMaps();
        List<SynonymMap> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        expectedNames.containsAll(result);
    }

    @Test
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap();
        Set<String> expectedNames = new HashSet<>();
        expectedNames.add(synonymMap1.getName());
        expectedNames.add(synonymMap2.getName());

        synonymMapClient.create(synonymMap1);
        synonymMapsToDelete.add(synonymMap1.getName());
        synonymMapClient.create(synonymMap2);
        synonymMapsToDelete.add(synonymMap2.getName());

        PagedIterable<SynonymMap> listResponse = synonymMapClient.listSynonymMapNames(generateRequestOptions(),
            Context.NONE);
        List<SynonymMap> result = listResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            assertNotNull(res.getName());
            assertNull(res.getSynonyms());
            assertNull(res.getETag());
        });

        assertEquals(2, result.size());
        expectedNames.containsAll(result);
    }

    @Test
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        SynonymMap stale = synonymMapClient.createOrUpdateWithResponse(createTestSynonymMap(), true, null, Context.NONE)
            .getValue();

        // Update the resource, the eTag will be changed
        SynonymMap current = synonymMapClient.createOrUpdateWithResponse(stale, true, null, Context.NONE)
            .getValue();

        try {
            synonymMapClient.deleteWithResponse(stale, true, null, Context.NONE);
            fail("deleteFunc should have failed due to precondition.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }

        synonymMapClient.deleteWithResponse(current, true, null, Context.NONE);
    }

    @Test
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        SynonymMap updated = synonymMapClient.createOrUpdateWithResponse(createTestSynonymMap(), false, null,
            Context.NONE).getValue();

        synonymMapClient.deleteWithResponse(updated, true, null, Context.NONE);

        // Try to delete again and expect to fail
        try {
            synonymMapClient.deleteWithResponse(updated, true, null, Context.NONE);
            fail("deleteFunc should have failed due to non existent resource.");
        } catch (SearchErrorException ex) {
            assertEquals(HttpURLConnection.HTTP_PRECON_FAILED, ex.getResponse().getStatusCode());
        }
    }

    void assertSynonymMapsEqual(SynonymMap actual, SynonymMap expected) {
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getSynonyms(), expected.getSynonyms());
    }

    SynonymMap createTestSynonymMap() {
        return new SynonymMap().setName(testResourceNamer.randomName("test-synonym", 32))
            .setSynonyms("word1,word2");
    }
}
