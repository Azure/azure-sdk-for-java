// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SynonymMap;
import com.azure.search.test.AccessConditionTests;
import com.azure.search.test.AccessOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SynonymMapManagementSyncTests extends SynonymMapManagementTestBase {
    private SearchServiceClient client;

    // commonly used lambda definitions
    private BiFunction<SynonymMap,
        AccessOptions,
        SynonymMap> createOrUpdateSynonymMapFunc =
            (SynonymMap synonymMap, AccessOptions accessOptions) ->
                createOrUpdateSynonymMap(
                    synonymMap, accessOptions.getAccessCondition(), accessOptions.getRequestOptions());

    private Supplier<SynonymMap> newSynonymMapFunc = this::createTestSynonymMap;

    private Function<SynonymMap, SynonymMap> mutateSynonymMapFunc = this::mutateSynonymsInSynonymMap;

    private BiConsumer<String, AccessOptions> deleteSynonymMapFunc =
        (String name, AccessOptions ac) ->
            client.deleteSynonymMapWithResponse(name, ac.getAccessCondition(), ac.getRequestOptions(), Context.NONE);

    private SynonymMap createOrUpdateSynonymMap(
        SynonymMap sm, AccessCondition ac, RequestOptions ro) {
        return client.createOrUpdateSynonymMapWithResponse(sm, ac, ro, Context.NONE).getValue();
    }

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildClient();
    }

    @Override
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        SynonymMap actualSynonymMap = client.createSynonymMap(expectedSynonymMap);
        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);

        actualSynonymMap = client.createSynonymMapWithResponse(expectedSynonymMap.setName("test-synonym2"),
            generateRequestOptions(), Context.NONE).getValue();
        assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
    }

    @Override
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        // Create SynonymMap with invalid synonym
        SynonymMap expectedSynonymMap = createTestSynonymMap()
            .setSynonyms("a => b => c");

        assertHttpResponseException(
            () -> client.createSynonymMap(expectedSynonymMap),
            HttpResponseStatus.BAD_REQUEST,
            "Syntax error in line 1: 'a => b => c'. Only one explicit mapping (=>) can be specified in a synonym rule."
        );
    }

    @Override
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();

        client.createSynonymMap(expected);

        SynonymMap actual = client.getSynonymMap(expected.getName());
        assertSynonymMapsEqual(expected, actual);

        actual = client.getSynonymMapWithResponse(expected.getName(), generateRequestOptions(), Context.NONE)
            .getValue();
        assertSynonymMapsEqual(expected, actual);
    }

    @Override
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        assertHttpResponseException(
            () -> client.getSynonymMap(synonymMapName),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage);

        assertHttpResponseException(
            () -> client.getSynonymMapWithResponse(synonymMapName, generateRequestOptions(), Context.NONE),
            HttpResponseStatus.NOT_FOUND,
            exceptionMessage);
    }

    @Override
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();

        client.createSynonymMap(initial);

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        SynonymMap updatedActual = client.createOrUpdateSynonymMap(updatedExpected);
        assertSynonymMapsEqual(updatedExpected, updatedActual);

        PagedIterable<SynonymMap> synonymMaps = client.listSynonymMaps();
        Assert.assertEquals(1, synonymMaps.stream().count());
    }

    @Override
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        SynonymMap actual = client.createOrUpdateSynonymMap(expected);
        assertSynonymMapsEqual(expected, actual);

        Response<SynonymMap> createOrUpdateResponse = client.createOrUpdateSynonymMapWithResponse(
            expected.setName("test-synonym2"),
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.createOrUpdateIfNotExistsFailsOnExistingResource(createOrUpdateSynonymMapFunc,
            newSynonymMapFunc, mutateSynonymMapFunc);
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();

        act.createOrUpdateIfNotExistsSucceedsOnNoResource(
            createOrUpdateSynonymMapFunc,
            newSynonymMapFunc);
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsSucceedsOnExistingResource(
            newSynonymMapFunc,
            createOrUpdateSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsFailsOnNoResource() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfExistsFailsOnNoResource(
            newSynonymMapFunc,
            createOrUpdateSynonymMapFunc);
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedSucceedsWhenResourceUnchanged(
            newSynonymMapFunc,
            createOrUpdateSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        AccessConditionTests act = new AccessConditionTests();
        act.updateIfNotChangedFailsWhenResourceChanged(
            newSynonymMapFunc,
            createOrUpdateSynonymMapFunc,
            mutateSynonymMapFunc);
    }

    @Override
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();
        Response<Void> deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(),
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());

        Response<SynonymMap> createResponse = client.createSynonymMapWithResponse(synonymMap,
            generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(),
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), deleteResponse.getStatusCode());

        deleteResponse = client.deleteSynonymMapWithResponse(synonymMap.getName(),
            new AccessCondition(), generateRequestOptions(), Context.NONE);
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), deleteResponse.getStatusCode());
    }

    @Override
    public void canCreateAndDeleteSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();
        client.createSynonymMap(synonymMap);
        client.deleteSynonymMap(synonymMap.getName());
        Assert.assertFalse(client.synonymMapExists(synonymMap.getName()));
    }

    @Override
    public void canCreateAndListSynonymMaps() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.createSynonymMap(synonymMap1);
        client.createSynonymMap(synonymMap2);

        PagedIterable<SynonymMap> actual = client.listSynonymMaps();
        List<SynonymMap> result = actual.stream().collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
        Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());
    }

    @Override
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.createSynonymMap(synonymMap1);
        client.createSynonymMap(synonymMap2);

        PagedIterable<SynonymMap> listResponse = client.listSynonymMaps("name", generateRequestOptions(), Context.NONE);
        List<SynonymMap> result = listResponse.stream().collect(Collectors.toList());

        result.forEach(res -> {
            Assert.assertNotNull(res.getName());
            Assert.assertNull(res.getSynonyms());
            Assert.assertNull(res.getETag());
        });

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
        Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());
    }

    @Override
    public void existsReturnsTrueForExistingSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();

        client.createSynonymMap(synonymMap);

        Assert.assertTrue(client.synonymMapExists(synonymMap.getName()));
        Assert.assertTrue(client.synonymMapExistsWithResponse(synonymMap.getName(), generateRequestOptions(), Context.NONE)
            .getValue());
    }

    @Override
    public void existsReturnsFalseForNonExistingSynonymMap() {
        String synonymMapName = "thisSynonymMapDoesNotExist";

        Assert.assertFalse(client.synonymMapExists(synonymMapName));
        Assert.assertFalse(client.synonymMapExistsWithResponse(synonymMapName, generateRequestOptions(), Context.NONE)
            .getValue());
    }

    @Override
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        AccessConditionTests act = new AccessConditionTests();

        String synonymName = "test-synonym";
        act.deleteIfNotChangedWorksOnlyOnCurrentResource(
            deleteSynonymMapFunc,
            newSynonymMapFunc,
            createOrUpdateSynonymMapFunc,
            synonymName);
    }

    @Override
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        AccessConditionTests act = new AccessConditionTests();

        act.deleteIfExistsWorksOnlyWhenResourceExists(
            deleteSynonymMapFunc,
            createOrUpdateSynonymMapFunc,
            newSynonymMapFunc,
            "test-synonym");
    }
}
