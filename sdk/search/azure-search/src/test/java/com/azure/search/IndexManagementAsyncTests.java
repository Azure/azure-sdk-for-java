// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.models.AnalyzerName;
import com.azure.core.http.rest.PagedFlux;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.Suggester;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

public class IndexManagementAsyncTests extends IndexManagementTestBase {
    private SearchServiceAsyncClient client;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        client = getSearchServiceClientBuilder().buildAsyncClient();
    }

    @Override
    public void createIndexReturnsCorrectDefinition() {
        Index index = createTestIndex();
        StepVerifier
            .create(client.createIndex(index))
            .assertNext(createdIndex -> {
                assertIndexesEqual(index, createdIndex);
            })
            .verifyComplete();
    }

    @Override
    public void createIndexReturnsCorrectDefaultValues() {
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
        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.indexExists(index.getName()))
            .assertNext(res -> Assert.assertTrue(res))
            .verifyComplete();
    }

    @Override
    public void existsReturnsFalseForNonExistingIndex() {
        StepVerifier
            .create(client.indexExists("invalidindex"))
            .assertNext(res -> Assert.assertFalse(res))
            .verifyComplete();
    }

    @Override
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        Index index = createTestIndex();
        Index staleResource = client.createOrUpdateIndex(index).block();
        Index currentResource = client.createOrUpdateIndex(mutateCorsOptionsInIndex(staleResource)).block();

        StepVerifier
            .create(client.deleteIndex(index.getName(), generateIfMatchAccessCondition(staleResource.getETag()), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
        client.deleteIndex(index.getName(), generateIfMatchAccessCondition(currentResource.getETag()), null).block();
    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), generateIfExistsAccessCondition(), null))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), res.getStatusCode()))
            .verifyComplete();
        StepVerifier
            .create(client.deleteIndex(index.getName(), generateIfExistsAccessCondition(), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void deleteIndexIsIdempotent() {
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
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.createIndexWithResponse(index, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.CREATED.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        // Delete the same index twice
        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.NO_CONTENT.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();

        StepVerifier
            .create(client.deleteIndexWithResponse(index.getName(), null, null, null))
            .assertNext(indexResponse -> {
                Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), indexResponse.getStatusCode());
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndDeleteIndex() {
        Index index = createTestIndex();
        client.createIndex(index).block();
        client.deleteIndex(index.getName()).block();

        StepVerifier
            .create(client.indexExists(index.getName()))
            .assertNext(response -> {
                Assert.assertFalse(response);
            })
            .verifyComplete();
    }

    @Override
    public void canCreateAndListIndexes() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1).block();
        client.createIndex(index2).block();

        PagedFlux<Index> listResponse = client.listIndexes();

        StepVerifier
            .create(listResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(result.get(0).getName(), index1.getName());
                Assert.assertEquals(result.get(1).getName(), index2.getName());
            })
            .verifyComplete();
    }

    @Override
    public void canListIndexesWithSelectedField() {
        Index index1 = createTestIndex();
        Index index2 = createTestIndex().setName("hotels2");

        client.createIndex(index1).block();
        client.createIndex(index2).block();

        PagedFlux<Index> selectedFieldListResponse = client.listIndexes("name");

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
                result.forEach(res -> {
                    Assert.assertNotNull(res.getName());
                    Assert.assertNull(res.getFields());
                    Assert.assertNull(res.getDefaultScoringProfile());
                    Assert.assertNull(res.getCorsOptions());
                    Assert.assertNull(res.getScoringProfiles());
                    Assert.assertNull(res.getSuggesters());
                    Assert.assertNull(res.getAnalyzers());
                    Assert.assertNull(res.getTokenizers());
                    Assert.assertNull(res.getTokenFilters());
                    Assert.assertNull(res.getCharFilters());
                });
            })
            .verifyComplete();

        StepVerifier
            .create(selectedFieldListResponse.collectList())
            .assertNext(result -> {
                Assert.assertEquals(2, result.size());
                Assert.assertEquals(result.get(0).getName(), index1.getName());
                Assert.assertEquals(result.get(1).getName(), index2.getName());
            })
            .verifyComplete();
    }

    @Override
    public void canAddSynonymFieldProperty() {
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

    @Override
    public void canUpdateSynonymFieldProperty() {
        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap()
            .setName(synonymMapName)
            .setSynonyms("hotel,motel");

        client.createSynonymMap(synonymMap).block();

        // Create an index
        Index index = createTestIndex();
        Field hotelNameField = getFieldByName(index, "HotelName");
        hotelNameField.setSynonymMaps(Collections.singletonList(synonymMapName));
        client.createIndex(index).block();

        // Update an existing index
        Index existingIndex = client.getIndex(index.getName()).block();
        hotelNameField = getFieldByName(existingIndex, "HotelName");
        hotelNameField.setSynonymMaps(Collections.<String>emptyList());

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex, true))
            .assertNext(res  -> {
                assertIndexesEqual(existingIndex, res);
            })
            .verifyComplete();
    }

    public void canUpdateIndexDefinition() {
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
            .create(client.createOrUpdateIndex(index))
            .assertNext(res -> {
                assertIndexesEqual(fullFeaturedIndex, res);
            })
            .verifyComplete();

        // Modify the fields on an existing index
        Index existingIndex = client.getIndex(fullFeaturedIndex.getName()).block();

        SynonymMap synonymMap = client.createSynonymMap(new SynonymMap()
            .setName("names")
            .setSynonyms("hotel,motel")
        ).block();

        Field tagsField = getFieldByName(existingIndex, "Description_Custom");
        tagsField.setRetrievable(false)
            .setSearchAnalyzer(AnalyzerName.WHITESPACE)
            .setSynonymMaps(Collections.singletonList(synonymMap.getName()));

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex, true))
            .assertNext(res -> {
                assertIndexesEqual(existingIndex, res);
            })
            .verifyComplete();

    }

    @Override
    public void canUpdateSuggesterWithNewIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        Index existingIndex = client.getIndex(index.getName()).block();

        existingIndex.getFields().addAll(Arrays.asList(
            new Field()
                .setName("HotelAmenities")
                .setType(DataType.EDM_STRING),
            new Field()
                .setName("HotelRewards")
                .setType(DataType.EDM_STRING)));
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Arrays.asList("HotelAmenities", "HotelRewards"))
        ));

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex, true))
            .assertNext(res -> assertIndexesEqual(existingIndex, res))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields() {
        Index index = createTestIndex();
        client.createIndex(index).block();

        Index existingIndex = client.getIndex(index.getName()).block();
        String existingFieldName = "Category";
        existingIndex.setSuggesters(Collections.singletonList(new Suggester()
            .setName("Suggestion")
            .setSourceFields(Collections.singletonList(existingFieldName))
        ));

        StepVerifier
            .create(client.createOrUpdateIndex(existingIndex, true))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error)
                    .getResponse().getStatusCode());
                String expectedMessage = String.format("Fields that were already present in an index (%s) cannot be "
                    + "referenced by a new suggester. Only new fields added in the same index update operation are allowed.", existingFieldName);
                Assert.assertTrue(error.getMessage().contains(expectedMessage));
            });
    }

    @Override
    public void createOrUpdateIndexCreatesWhenIndexDoesNotExist() {
        Index index = createTestIndex();

        StepVerifier
            .create(client.createOrUpdateIndexWithResponse(index))
            .assertNext(res -> Assert.assertEquals(HttpResponseStatus.CREATED.code(), res.getStatusCode()))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexIfNotExistsFailsOnExistingResource() {
        Index index = createTestIndex();
        Index createdResource = client.createOrUpdateIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);

        StepVerifier
            .create(client.createOrUpdateIndex(mutatedResource, generateIfNotExistsAccessCondition(), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    @Override
    public void createOrUpdateIndexIfNotExistsSucceedsOnNoResource() {
        Index resource = createTestIndex();
        Mono<Index> updatedResource = client.createOrUpdateIndex(resource, generateIfNotExistsAccessCondition(), null);

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> Assert.assertFalse(res.getETag().isEmpty()))
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexIfExistsSucceedsOnExistingResource() {
        Index index = createTestIndex();
        Index createdResource = client.createOrUpdateIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Mono<Index> updatedResource = client.createOrUpdateIndex(mutatedResource, generateIfExistsAccessCondition(), null);

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> {
                Assert.assertFalse(res.getETag().isEmpty());
                Assert.assertNotEquals(createdResource.getETag(), res.getETag());
            })
            .verifyComplete();
    }

    @Override
    public void createOrUpdateIndexIfExistsFailsOnNoResource() {
        Index resource = createTestIndex();

        StepVerifier
            .create(client.createOrUpdateIndex(resource, generateIfExistsAccessCondition(), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        // The resource should never have been created on the server, and thus it should not have an ETag
        Assert.assertNull(resource.getETag());
    }

    @Override
    public void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        Index index = createTestIndex();
        Index createdResource = client.createOrUpdateIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Mono<Index> updatedResource = client.createOrUpdateIndex(mutatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null);

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
    public void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged() {
        Index index = createTestIndex();
        Index createdResource = client.createOrUpdateIndex(index, generateEmptyAccessCondition(), null).block();
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Index updatedResource = client.createOrUpdateIndex(mutatedResource, generateEmptyAccessCondition(), null).block();

        StepVerifier
            .create(client.createOrUpdateIndex(updatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null))
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }
}
