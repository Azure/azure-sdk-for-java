// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.http.rest.PagedIterable;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class SynonymMapManagementSyncTests extends SynonymMapManagementTestBase {
    private SearchServiceClient client;

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
    }

    @Override
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        // Set invalid Synonym
        expectedSynonymMap.setSynonyms("a => b => c");

        try {
            client.createSynonymMap(expectedSynonymMap);
            Assert.fail("createSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains("Syntax error in line 1: 'a => b => c'. "
                + "Only one explicit mapping (=>) can be specified in a synonym rule."));
        }
    }

    @Override
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();

        client.createSynonymMap(expected);

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        Context context = new Context("key", "value");

        SynonymMap actual = client.getSynonymMap(expected.getName());
        assertSynonymMapsEqual(expected, actual);

        actual = client.getSynonymMap(expected.getName(), requestOptions);
        assertSynonymMapsEqual(expected, actual);

        actual = client.getSynonymMapWithResponse(expected.getName(), requestOptions, context)
            .getValue();
        assertSynonymMapsEqual(expected, actual);


    }

    @Override
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        Context context = new Context("key", "value");

        validateSynonymMapNotFoundThrowsException(synonymMapName, () -> client.getSynonymMap(synonymMapName));
        validateSynonymMapNotFoundThrowsException(synonymMapName, () -> client.getSynonymMap(synonymMapName, requestOptions));
        validateSynonymMapNotFoundThrowsException(synonymMapName, () -> client.getSynonymMap(synonymMapName,
            requestOptions, context));
        validateSynonymMapNotFoundThrowsException(synonymMapName, () -> client.getSynonymMapWithResponse(synonymMapName,
            requestOptions, context));
    }

    @Override
    public void canUpdateSynonymMap() {

    }

    @Override
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        RequestOptions requestOptions = new RequestOptions()
                .setClientRequestId(UUID.randomUUID());

        Context context = new Context("key", "value");

        SynonymMap actual = client.createOrUpdateSynonymMap(expected);
        assertSynonymMapsEqual(expected, actual);

        actual = client.createOrUpdateSynonymMap(expected.setName("test-synonym1"),
            new AccessCondition(), requestOptions);
        assertSynonymMapsEqual(expected, actual);

        Response<SynonymMap> createOrUpdateResponse = client.createOrUpdateSynonymMapWithResponse(
            expected.setName("test-synonym2"),
            new AccessCondition(), requestOptions, context);
        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
        assertSynonymMapsEqual(expected, createOrUpdateResponse.getValue());
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap);
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());
        Context context = new Context("key", "value");

        try {
            client.createOrUpdateSynonymMap(mutatedResource, generateIfNotExistsAccessCondition());
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }

        try {
            client.createOrUpdateSynonymMap(mutatedResource, generateIfNotExistsAccessCondition(), requestOptions);
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }

        try {
            client.createOrUpdateSynonymMap(mutatedResource, generateIfNotExistsAccessCondition(), requestOptions, context);
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }

        try {
            client.createOrUpdateSynonymMapWithResponse(mutatedResource, generateIfNotExistsAccessCondition(), requestOptions, context);
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        SynonymMap resource = createTestSynonymMap();
        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());
        Context context = new Context("key", "value");

        SynonymMap updatedResource = client.createOrUpdateSynonymMap(resource, generateIfNotExistsAccessCondition());
        Assert.assertFalse(updatedResource.getETag().isEmpty());

        updatedResource = client.createOrUpdateSynonymMap(resource.setName("test-synonym1"),
            generateIfNotExistsAccessCondition(),
            requestOptions);
        Assert.assertFalse(updatedResource.getETag().isEmpty());

        Response<SynonymMap> updatedResponse = client.createOrUpdateSynonymMapWithResponse(resource.setName("test-synonym2"),
            generateIfNotExistsAccessCondition(),
            requestOptions,
            context);
        Assert.assertFalse(updatedResponse.getValue().getETag().isEmpty());
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap);
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        SynonymMap updatedResource = client.createOrUpdateSynonymMap(mutatedResource, generateIfExistsAccessCondition());

        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsFailsOnNoResource() {
        SynonymMap resource = createTestSynonymMap();

        try {
            client.createOrUpdateSynonymMap(resource, generateIfExistsAccessCondition());
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
        // The resource should never have been created on the server, and thus it should not have an ETag
        Assert.assertNull(resource.getETag());
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap);
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        SynonymMap updatedResource = client.createOrUpdateSynonymMap(mutatedResource, generateIfMatchAccessCondition(createdResource.getETag()));

        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap);
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        SynonymMap updatedResource = client.createOrUpdateSynonymMap(mutatedResource);

        try {
            client.createOrUpdateSynonymMap(updatedResource, generateIfMatchAccessCondition(createdResource.getETag()));
            Assert.fail("createOrUpdateSynonymMap did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {

    }

    @Override
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {

    }

    @Override
    public void deleteSynonymMapIsIdempotent() {

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

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        actual = client.listSynonymMaps("name", requestOptions);
        result = actual.stream().collect(Collectors.toList());

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
        Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());

        Context context = new Context("key", "value");
        PagedResponse<SynonymMap> listResponse = client.listSynonymMapsWithResponse("name",
            requestOptions, context);
        result = listResponse.getItems();

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

        PagedIterable<SynonymMap> listResponse = client.listSynonymMaps("name");
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

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());
        Context context = new Context("key", "value");

        client.createSynonymMap(synonymMap);

        Assert.assertTrue(client.synonymMapExists(synonymMap.getName()));
        Assert.assertTrue(client.synonymMapExists(synonymMap.getName(), requestOptions));
        Assert.assertTrue(client.synonymMapExistsWithResponse(synonymMap.getName(),
            requestOptions,
            context)
            .getValue());
    }

    @Override
    public void existsReturnsFalseForNonExistingSynonymMap() {
        String synonymMapName = "thisSynonymMapDoesNotExist";

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());
        Context context = new Context("key", "value");

        Assert.assertFalse(client.synonymMapExists(synonymMapName));
        Assert.assertFalse(client.synonymMapExists(synonymMapName, requestOptions));
        Assert.assertFalse(client.synonymMapExistsWithResponse(synonymMapName,
            requestOptions,
            context)
            .getValue());
    }

    private void validateSynonymMapNotFoundThrowsException(String synonymMapName, Runnable getSynonymMapAction) {
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);
        try {
            getSynonymMapAction.run();
            Assert.fail("the action did not throw an exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains(exceptionMessage));
        }
    }
}
