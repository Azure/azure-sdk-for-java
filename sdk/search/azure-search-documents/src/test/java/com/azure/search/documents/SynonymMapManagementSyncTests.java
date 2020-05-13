// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SynonymMapClient;
import com.azure.search.documents.indexes.models.RequestOptions;
import com.azure.search.documents.indexes.models.SynonymMap;
import com.azure.search.documents.test.AccessConditionTests;
import com.azure.search.documents.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SynonymMapManagementSyncTests extends SearchServiceTestBase {
    private SynonymMapClient client;

    // commonly used lambda definitions
    private BiFunction<SynonymMap, AccessOptions, SynonymMap> createOrUpdateSynonymMapFunc =
        (SynonymMap synonymMap, AccessOptions accessOptions) ->
            createOrUpdateSynonymMap(synonymMap, accessOptions.getOnlyIfUnchanged(), accessOptions.getRequestOptions());

    private Supplier<SynonymMap> newSynonymMapFunc = this::createTestSynonymMap;

    private Function<SynonymMap, SynonymMap> mutateSynonymMapFunc = this::mutateSynonymsInSynonymMap;

    private BiConsumer<SynonymMap, AccessOptions> deleteSynonymMapFunc = (SynonymMap synonymMap, AccessOptions ac) ->
            client.deleteWithResponse(synonymMap, ac.getOnlyIfUnchanged(),
                ac.getRequestOptions(), Context.NONE);

    private SynonymMap createOrUpdateSynonymMap(
        SynonymMap sm, Boolean onlyIfUnchanged, RequestOptions ro) {
        return client.createOrUpdateWithResponse(sm, onlyIfUnchanged, ro, Context.NONE).getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient().getSynonymMapClient();
    }

    @Test
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.create(expectedSynonymMap);

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);

    }

    @Test
    public void createSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createWithResponse(expectedSynonymMap,
            generateRequestOptions(), Context.NONE).getValue();

        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Test
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap()
            .setSynonyms("a => b => c");

        assertHttpResponseException(
            () -> client.create(expectedSynonymMap),
            HttpResponseStatus.BAD_REQUEST,
            "Syntax error in line 1: 'a => b => c'. Only one explicit mapping (=>) can be specified in a synonym rule."
        );
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();

        client.create(expected);

        SynonymMap actual = client.getSynonymMap(expected.getName());
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapReturnsCorrectDefinitionWithResponse() {
        SynonymMap expected = createTestSynonymMap();
        client.create(expected);

        SynonymMap actual = client.getSynonymMapWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> client.getSynonymMapWithResponse(synonymMapName, generateRequestOptions(), Context.NONE),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void getSynonymMapThrowsOnNotFoundWithResponse() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> client.getSynonymMapWithResponse(synonymMapName, generateRequestOptions(), Context.NONE),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage);
    }

    @Test
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();

        client.create(initial);

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdate(updatedExpected);
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = client.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void canUpdateSynonymMapWithResponse() {
        SynonymMap initial = createTestSynonymMap();

        client.create(initial);

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateWithResponse(updatedExpected, false,
            generateRequestOptions(), Context.NONE).getValue();
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = client.listSynonymMaps();
        assertEquals(1, synonymMaps.stream().count());
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        SynonymMap actual = client.createOrUpdate(expected);
        assertSynonymMapsEqual(expected, actual);
    }

    @Test
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponse() {
        SynonymMap expected = createTestSynonymMap();

        Response<SynonymMap> createOrUpdateResponse = client.createOrUpdateWithResponse(
            expected, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Test
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests.createOrUpdateIfNotExistsSucceedsOnNoResource(createOrUpdateSynonymMapFunc,
            newSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        AccessConditionTests.updateIfExistsSucceedsOnExistingResource(newSynonymMapFunc, createOrUpdateSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests.updateIfNotChangedSucceedsWhenResourceUnchanged(newSynonymMapFunc,
            createOrUpdateSynonymMapFunc, mutateSynonymMapFunc);
    }

    @Test
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests.updateIfNotChangedFailsWhenResourceChanged(newSynonymMapFunc, createOrUpdateSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Test
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();
        Response<Void> deleteResponse = client.deleteWithResponse(synonymMap, false, generateRequestOptions(),
            Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        Response<SynonymMap> createResponse = client.createWithResponse(synonymMap,
            generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());

        deleteResponse = client.deleteWithResponse(synonymMap, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteWithResponse(synonymMap, false, generateRequestOptions(), Context.NONE);
        assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Test
    public void canCreateAndDeleteSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();
        client.create(synonymMap);
        client.delete(synonymMap.getName());
        assertThrows(HttpResponseException.class, () -> client.getSynonymMap(synonymMap.getName()));
    }

    @Test
    public void canCreateAndListSynonymMaps() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.create(synonymMap1);
        client.create(synonymMap2);

        PagedIterable<SynonymMap> actual = client.listSynonymMaps();
        List<SynonymMap> result = actual.stream().collect(Collectors.toList());

        assertEquals(2, result.size());
        assertEquals(synonymMap1.getName(), result.get(0).getName());
        assertEquals(synonymMap2.getName(), result.get(1).getName());
    }

    @Test
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.create(synonymMap1);
        client.create(synonymMap2);

        PagedIterable<SynonymMap> listResponse = client.listSynonymMapNames(generateRequestOptions(), Context.NONE);
        List<SynonymMap> result = listResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            assertNotNull(res.getName());
            assertNull(res.getSynonyms());
            assertNull(res.getETag());
        });

        assertEquals(2, result.size());
        assertEquals(synonymMap1.getName(), result.get(0).getName());
        assertEquals(synonymMap2.getName(), result.get(1).getName());
    }

    @Test
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests.deleteIfNotChangedWorksOnlyOnCurrentResource(deleteSynonymMapFunc, newSynonymMapFunc,
            createOrUpdateSynonymMapFunc, "test-synonym");
    }

    @Test
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests.deleteIfExistsWorksOnlyWhenResourceExists(deleteSynonymMapFunc,
            createOrUpdateSynonymMapFunc, newSynonymMapFunc);
    }

    void assertSynonymMapsEqual(SynonymMap actual, SynonymMap expected) {
        assertEquals(actual.getName(), expected.getName());
        assertEquals(actual.getSynonyms(), expected.getSynonyms());
    }

    SynonymMap createTestSynonymMap() {
        return new SynonymMap()
            .setName("test-synonym")
            .setSynonyms("word1,word2");
    }

    SynonymMap mutateSynonymsInSynonymMap(SynonymMap synonymMap) {
        synonymMap.setSynonyms("mutated1, mutated2");
        return synonymMap;
    }
}
