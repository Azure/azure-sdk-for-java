// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.models.Index;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IndexManagementAsyncTests extends IndexManagementTestBase {
    private SearchServiceAsyncClient client;

    @Override
    public void createIndexReturnsCorrectDefinition() {

    }

    @Override
    public void createIndexReturnsCorrectDefaultValues() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        index.setCorsOptions(new CorsOptions().setAllowedOrigins(Collections.singletonList("*")));
        index.setScoringProfiles(Collections.singletonList(new ScoringProfile()
            .setName("MyProfile")
            .setFunctions(Collections.singletonList(new MagnitudeScoringFunction()
                .setParameters(new MagnitudeScoringParameters()
                    .setBoostingRangeStart(1)
                    .setBoostingRangeEnd(4))
                .setFieldName("Rating")
                .setBoost(2.0))
            )
        ));

        StepVerifier
            .create(client.createIndex(index))
            .assertNext(indexResponse -> {
                ScoringProfile scoringProfile = indexResponse.getScoringProfiles().get(0);
                Assert.assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
                Assert.assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
                Assert.assertNotNull(scoringProfile.getFunctions().get(0));
                Assert.assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
            })
            .verifyComplete();
    }

    @Override
    public void createIndexFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        String indexName = "hotels";
        Index index = new Index()
            .setName(indexName)
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(false)
            ));

        String expectedMessage = String.format("The request is invalid. Details: index : Found 0 key fields in index '%s'. "
            + "Each index must have exactly one key field.", indexName);

        StepVerifier
            .create(client.createIndex(index))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains(expectedMessage));
            });
    }

    @Override
    public void getIndexReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.getIndex(index.getName()))
            .assertNext(res -> {
                assertIndexesEqual(index, res);
            })
            .verifyComplete();
    }

    @Override
    public void getIndexThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier
            .create(client.getIndex("thisindexdoesnotexist"))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) error).getResponse().getStatusCode());
                Assert.assertTrue(error.getMessage().contains("No index with the name 'thisindexdoesnotexist' was found in the service"));
            });
    }

    @Override
    public void existsReturnsTrueForExistingIndex() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.indexExists(index.getName()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingIndex() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier
            .create(client.indexExists("invalidindex"))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();
    }

    @Override
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        Index staleResource = client.upsertIndex(index).block();
        Index currentResource = client.upsertIndex(mutateCorsOptionsInIndex(staleResource)).block();

        StepVerifier
            .create(client.deleteIndex(index.getName(), null, generateIfMatchAccessCondition(staleResource.getETag())))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
        client.deleteIndex(index.getName(), null, generateIfMatchAccessCondition(currentResource.getETag())).block();
    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, generateIfExistsAccessCondition()))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), res.getStatusCode()))
            .verifyComplete();
        StepVerifier
            .create(client.deleteIndex(index.getName(), null, generateIfExistsAccessCondition()))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void deleteIndexIsIdempotent() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = new Index()
            .setName("hotels")
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
            ));
        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(404, indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.createIndexWithResponse(index, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(201, indexResponse.getStatusCode());
            })
            .verifyComplete();

        // Delete the same index twice
        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(204, indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(404, indexResponse.getStatusCode());
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndDeleteIndex() {

    }

    @Override
    public void canAddSynonymFieldProperty() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap).block();

        Index index = new Index()
            .setName("hotels")
            .setFields(Arrays.asList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true),
                new Field()
                    .setName("HotelName")
                    .setType(DataType.EDM_STRING)
                    .setSynonymMaps(Arrays.asList(synonymMapName))

            ));

        StepVerifier
            .create(client.createIndex(index))
            .assertNext(createdIndex -> {
                List<String> actualSynonym = index.getFields().get(1).getSynonymMaps();
                List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMaps();
                Assert.assertEquals(actualSynonym, expectedSynonym);
            })
            .verifyComplete();
    }

    public void canUpdateIndexDefinition() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index fullFeaturedIndex = createTestIndex();
        Index initialIndex = createTestIndex();

        // Start out with no scoring profiles and different CORS options.
        initialIndex.setName(fullFeaturedIndex.getName());
        initialIndex.setScoringProfiles(new ArrayList<>());
        initialIndex.setDefaultScoringProfile(null);
        initialIndex.setCorsOptions(initialIndex.getCorsOptions().setAllowedOrigins(Arrays.asList("*")));

        Index index = client.createIndex(initialIndex).block();

        // Now update the index.
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles());
        index.setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile());
        index.setCorsOptions(index.getCorsOptions().setAllowedOrigins(fullFeaturedIndex.getCorsOptions().getAllowedOrigins()));

        StepVerifier
            .create(client.upsertIndex(index))
            .assertNext(res -> {
                assertIndexesEqual(fullFeaturedIndex, res);
            })
            .verifyComplete();
    }

    @Override
    public void upsertIndexCreatesWhenIndexDoesNotExist() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();

        StepVerifier
            .create(client.upsertIndexWithResponse(index))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode()))
            .verifyComplete();
    }

    @Override
    public void upsertIndexIfNotExistsFailsOnExistingResource() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);

        StepVerifier
            .create(client.upsertIndex(mutatedResource, generateIfNotExistsAccessCondition(), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void upsertIndexIfNotExistsSucceedsOnNoResource() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index resource = createTestIndex();
        Mono<Index> updatedResource = client.upsertIndex(resource, generateIfNotExistsAccessCondition(), null);

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> Assert.assertFalse(res.getETag().isEmpty()))
            .verifyComplete();
    }

    @Override
    public void upsertIndexIfExistsSucceedsOnExistingResource() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Mono<Index> updatedResource = client.upsertIndex(mutatedResource, generateIfExistsAccessCondition(), null);

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> {
                Assert.assertFalse(res.getETag().isEmpty());
                Assert.assertNotEquals(createdResource.getETag(), res.getETag());
            })
            .verifyComplete();
    }

    @Override
    public void upsertIndexIfExistsFailsOnNoResource() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index resource = createTestIndex();

        StepVerifier
            .create(client.upsertIndex(resource, generateIfExistsAccessCondition(), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        // The resource should never have been created on the server, and thus it should not have an ETag
        Assert.assertNull(resource.getETag());
    }

    @Override
    public void upsertIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Mono<Index> updatedResource = client.upsertIndex(mutatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null);

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
    public void upsertIndexIfNotChangedFailsWhenResourceChanged() {
        client = getSearchServiceClientBuilder().buildAsyncClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Index updatedResource = client.upsertIndex(mutatedResource, generateEmptyAccessCondition(), null).block();

        StepVerifier
            .create(client.upsertIndex(updatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }
}
