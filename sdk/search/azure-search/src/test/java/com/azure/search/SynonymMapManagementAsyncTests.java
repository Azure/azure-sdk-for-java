// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;

import com.azure.core.http.rest.PagedFlux;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

public class SynonymMapManagementAsyncTests extends SynonymMapManagementTestBase {
    private SearchServiceAsyncClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
    public void createSynonymMapReturnsCorrectDefinition() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.createSynonymMap(expectedSynonymMap))
            .assertNext(actualSynonymMap -> {
                assertSynonymMapsEqual(expectedSynonymMap, actualSynonymMap);
            })
            .verifyComplete();
    }

    @Override
    public void createSynonymMapFailsWithUsefulMessageOnUserError() {
        SynonymMap expectedSynonymMap = createTestSynonymMap();
        // Set invalid Synonym
        expectedSynonymMap.setSynonyms("a => b => c");

        StepVerifier
            .create(client.createSynonymMap(expectedSynonymMap))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
                    .getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains("Syntax error in line 1: 'a => b => c'. "
                     + "Only one explicit mapping (=>) can be specified in a synonym rule."));
            });
    }

    @Override
    public void getSynonymMapReturnsCorrectDefinition() {
        SynonymMap expected = createTestSynonymMap();
        client.createSynonymMap(expected).block();

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.getSynonymMap(expected.getName()))
            .assertNext(actual -> assertSynonymMapsEqual(expected, actual))
            .verifyComplete();

        StepVerifier
            .create(client.getSynonymMap(expected.getName(), requestOptions))
            .assertNext(actual -> assertSynonymMapsEqual(expected, actual))
            .verifyComplete();

        StepVerifier
            .create(client.getSynonymMapWithResponse(expected.getName(), requestOptions))
            .assertNext(result -> assertSynonymMapsEqual(expected, result.getValue()))
            .verifyComplete();
    }

    @Override
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.getSynonymMap(synonymMapName))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });

        StepVerifier
            .create(client.getSynonymMap(synonymMapName, requestOptions))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });

        StepVerifier
            .create(client.getSynonymMapWithResponse(synonymMapName, requestOptions))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });
    }

    @Override
    public void canUpdateSynonymMap() {

    }

    @Override
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        RequestOptions requestOptions = new RequestOptions()
                .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.createOrUpdateSynonymMap(expected))
            .assertNext(res -> assertSynonymMapsEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(expected.setName("test-synonym1"),
                new AccessCondition(), requestOptions))
            .assertNext(res -> assertSynonymMapsEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(expected.setName("test-synonym2"),
                new AccessCondition(), requestOptions))
            .assertNext(res -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode());
                assertSynonymMapsEqual(expected, res.getValue());
            })
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap, generateEmptyAccessCondition()).block();
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);

        StepVerifier
            .create(client.createOrUpdateSynonymMap(mutatedResource, generateIfNotExistsAccessCondition()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        SynonymMap resource = createTestSynonymMap();
        Mono<SynonymMap> updatedResource = client.createOrUpdateSynonymMap(resource, generateIfNotExistsAccessCondition());

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> Assert.assertFalse(res.getETag().isEmpty()))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {

    }

    @Override
    public void createOrUpdateSynonymMapIfExistsFailsOnNoResource() {

    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {

    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {

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

        client.createSynonymMap(synonymMap1).block();
        client.createSynonymMap(synonymMap2).block();

        PagedFlux<SynonymMap> listResponse = client.listSynonymMaps();

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
                Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());
            })
            .verifyComplete();

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        listResponse = client.listSynonymMaps("name", requestOptions);

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
                Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());

            })
            .verifyComplete();
    }

    @Override
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.createSynonymMap(synonymMap1).block();
        client.createSynonymMap(synonymMap2).block();

        PagedFlux<SynonymMap> selectedFieldListResponse = client.listSynonymMaps("name");

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
                result.forEach(res -> {
                    Assert.assertNotNull(res.getName());
                    Assert.assertNull(res.getSynonyms());
                    Assert.assertNull(res.getETag());
                });
            })
            .verifyComplete();

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
                Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());
            })
            .verifyComplete();
    }

    @Override
    public void existsReturnsTrueForExistingSynonymMap() {
        SynonymMap synonymMap = createTestSynonymMap();

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        client.createSynonymMap(synonymMap).block();

        StepVerifier
            .create(client.synonymMapExists(synonymMap.getName()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExists(synonymMap.getName(), requestOptions))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExistsWithResponse(synonymMap.getName(), requestOptions))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingSynonymMap() {
        String synonymMapName = "thisSynonymMapDoesNotExist";

        RequestOptions requestOptions = new RequestOptions()
            .setClientRequestId(UUID.randomUUID());

        StepVerifier
            .create(client.synonymMapExists(synonymMapName))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExists(synonymMapName, requestOptions))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExistsWithResponse(synonymMapName, requestOptions))
            .assertNext(res -> Assert.assertFalse(res.getValue()))
            .verifyComplete();
    }
}

