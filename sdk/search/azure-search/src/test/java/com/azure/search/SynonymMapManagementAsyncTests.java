// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.search.models.AccessCondition;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

        StepVerifier
            .create(client.getSynonymMap(expected.getName()))
            .assertNext(actual -> assertSynonymMapsEqual(expected, actual))
            .verifyComplete();

        StepVerifier
            .create(client.getSynonymMap(expected.getName(), generateRequestOptions()))
            .assertNext(actual -> assertSynonymMapsEqual(expected, actual))
            .verifyComplete();

        StepVerifier
            .create(client.getSynonymMapWithResponse(expected.getName(), generateRequestOptions()))
            .assertNext(result -> assertSynonymMapsEqual(expected, result.getValue()))
            .verifyComplete();
    }

    @Override
    public void getSynonymMapThrowsOnNotFound() {
        final String synonymMapName = "thisSynonymMapDoesNotExist";
        final String exceptionMessage = String.format("No synonym map with the name '%s' was found", synonymMapName);

        StepVerifier
            .create(client.getSynonymMap(synonymMapName))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });

        StepVerifier
            .create(client.getSynonymMap(synonymMapName, generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });

        StepVerifier
            .create(client.getSynonymMapWithResponse(synonymMapName, generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(exceptionMessage));
            });
    }

    @Override
    public void canUpdateSynonymMap() {
        SynonymMap initial = createTestSynonymMap();

        client.createSynonymMap(initial).block();

        SynonymMap updatedExpected = createTestSynonymMap()
            .setName(initial.getName())
            .setSynonyms("newword1,newword2");

        StepVerifier
            .create(client.createOrUpdateSynonymMap(updatedExpected))
            .assertNext(updatedActual -> assertSynonymMapsEqual(updatedExpected, updatedActual));

        StepVerifier
            .create(client.listSynonymMaps().collectList())
            .assertNext(e -> Assert.assertEquals(1, e.size()));
    }

    @Override
    public void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist() {
        SynonymMap expected = createTestSynonymMap();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(expected))
            .assertNext(res -> assertSynonymMapsEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(expected.setName("test-synonym1"),
                new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> assertSynonymMapsEqual(expected, res))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(expected.setName("test-synonym2"),
                new AccessCondition(), generateRequestOptions()))
            .assertNext(res -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode());
                assertSynonymMapsEqual(expected, res.getValue());
            })
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap).block();
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);

        StepVerifier
            .create(client.createOrUpdateSynonymMap(mutatedResource,
                generateIfNotExistsAccessCondition(), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(mutatedResource,
                generateIfNotExistsAccessCondition(), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {
        SynonymMap resource = createTestSynonymMap();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(resource,
                generateIfNotExistsAccessCondition(), generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res.getETag().isEmpty()))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(resource.setName("test-synonym1"),
                generateIfNotExistsAccessCondition(), generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res.getETag().isEmpty()))
            .verifyComplete();

        StepVerifier
            .create(client.createOrUpdateSynonymMapWithResponse(resource.setName("test-synonym2"),
                generateIfNotExistsAccessCondition(), generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res.getValue().getETag().isEmpty()))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap).block();
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        Mono<SynonymMap> updatedResource = client.createOrUpdateSynonymMap(mutatedResource,
            generateIfExistsAccessCondition(), generateRequestOptions());

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> {
                Assert.assertFalse(res.getETag().isEmpty());
                Assert.assertNotEquals(createdResource.getETag(), res.getETag());
            })
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfExistsFailsOnNoResource() {
        SynonymMap resource = createTestSynonymMap();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(resource,
                generateIfExistsAccessCondition(), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        // The resource should never have been created on the server, and thus it should not have an ETag
        Assert.assertNull(resource.getETag());
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap).block();
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        Mono<SynonymMap> updatedResource = client.createOrUpdateSynonymMap(mutatedResource,
            generateIfMatchAccessCondition(createdResource.getETag()), generateRequestOptions());

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> {
                Assert.assertFalse(createdResource.getETag().isEmpty());
                Assert.assertFalse(res.getETag().isEmpty());
                Assert.assertNotEquals(createdResource.getETag(), res.getETag());
            })
            .verifyComplete();
    }

    @Override
    public void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap createdResource = client.createOrUpdateSynonymMap(synonymMap).block();
        SynonymMap mutatedResource = mutateSynonymsInSynonymMap(createdResource);
        SynonymMap updatedResource = client.createOrUpdateSynonymMap(mutatedResource).block();

        StepVerifier
            .create(client.createOrUpdateSynonymMap(updatedResource,
                generateIfMatchAccessCondition(createdResource.getETag()), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource() {
        SynonymMap synonymMap = createTestSynonymMap();
        SynonymMap staleResource = client.createOrUpdateSynonymMap(synonymMap).block();
        SynonymMap currentResource = client.createOrUpdateSynonymMap(staleResource.setSynonyms("updatedword1,updatedword2")).block();

        StepVerifier
            .create(client.deleteSynonymMap(synonymMap.getName(),
                generateIfMatchAccessCondition(staleResource.getETag()), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        Response<Void> response = client.deleteSynonymMapWithResponse(synonymMap.getName(),
            generateIfMatchAccessCondition(currentResource.getETag()),
            null,
            null
        ).block();
        Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), response.getStatusCode());
    }

    @Override
    public void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists() {
        SynonymMap synonymMap = createTestSynonymMap();
        client.createSynonymMap(synonymMap).block();

        client.deleteSynonymMap(synonymMap.getName(), generateIfExistsAccessCondition(), generateRequestOptions()).block();
        StepVerifier
            .create(client.deleteSynonymMap(synonymMap.getName(), generateIfExistsAccessCondition(), generateRequestOptions()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void deleteSynonymMapIsIdempotent() {
        SynonymMap synonymMap = createTestSynonymMap();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(), null, null, null))
            .assertNext(synonymMapResponse -> {
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), synonymMapResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.createSynonymMapWithResponse(synonymMap, null, null))
            .assertNext(synonymMapResponse -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), synonymMapResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(), null, null, null))
            .assertNext(synonymMapResponse -> {
                Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), synonymMapResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.deleteSynonymMapWithResponse(synonymMap.getName(), null, null, null))
            .assertNext(synonymMapResponse -> {
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), synonymMapResponse.getStatusCode());
            })
            .verifyComplete();
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

        listResponse = client.listSynonymMaps("name", generateRequestOptions());

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(synonymMap1.getName(), result.get(0).getName());
                Assert.assertEquals(synonymMap2.getName(), result.get(1).getName());

            })
            .verifyComplete();

        StepVerifier
            .create(client.listSynonymMapsWithResponse("name", generateRequestOptions()))
            .assertNext(result -> {
                Assert.assertEquals(2, result.getItems().size());
                Assert.assertEquals(synonymMap1.getName(), result.getValue().get(0).getName());
                Assert.assertEquals(synonymMap2.getName(), result.getValue().get(1).getName());
            })
            .verifyComplete();
    }

    @Override
    public void canListSynonymMapsWithSelectedField() {
        SynonymMap synonymMap1 = createTestSynonymMap();
        SynonymMap synonymMap2 = createTestSynonymMap().setName("test-synonym1");

        client.createSynonymMap(synonymMap1).block();
        client.createSynonymMap(synonymMap2).block();

        PagedFlux<SynonymMap> selectedFieldListResponse = client.listSynonymMaps("name", generateRequestOptions());

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

        client.createSynonymMap(synonymMap).block();

        StepVerifier
            .create(client.synonymMapExists(synonymMap.getName()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExists(synonymMap.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExistsWithResponse(synonymMap.getName(), generateRequestOptions()))
            .assertNext(res -> Assert.assertTrue(res.getValue()))
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingSynonymMap() {
        String synonymMapName = "thisSynonymMapDoesNotExist";

        StepVerifier
            .create(client.synonymMapExists(synonymMapName))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExists(synonymMapName, generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();

        StepVerifier
            .create(client.synonymMapExistsWithResponse(synonymMapName, generateRequestOptions()))
            .assertNext(res -> Assert.assertFalse(res.getValue()))
            .verifyComplete();
    }
}

