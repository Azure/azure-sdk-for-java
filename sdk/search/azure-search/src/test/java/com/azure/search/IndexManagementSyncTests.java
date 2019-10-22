// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.CorsOptions;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class IndexManagementSyncTests extends IndexManagementTestBase {
    private SearchServiceClient client;

    @Override
    public void createIndexReturnsCorrectDefinition() {

    }

    @Override
    public void createIndexReturnsCorrectDefaultValues() {
        client = getSearchServiceClientBuilder().buildClient();

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

        Index indexResponse = client.createIndex(index);
        ScoringProfile scoringProfile = indexResponse.getScoringProfiles().get(0);
        Assert.assertNull(indexResponse.getCorsOptions().getMaxAgeInSeconds());
        Assert.assertEquals(ScoringFunctionAggregation.SUM, scoringProfile.getFunctionAggregation());
        Assert.assertNotNull(scoringProfile.getFunctions().get(0));
        Assert.assertEquals(ScoringFunctionInterpolation.LINEAR, scoringProfile.getFunctions().get(0).getInterpolation());
    }

    @Override
    public void createIndexFailsWithUsefulMessageOnUserError() {
        client = getSearchServiceClientBuilder().buildClient();

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

        try {
            client.createIndex(index);
            Assert.fail("createIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains(expectedMessage));
        }
    }

    @Override
    public void getIndexReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        client.createIndex(index);
        Index createdIndex = client.getIndex(index.getName());

        assertIndexesEqual(index, createdIndex);
    }

    @Override
    public void getIndexThrowsOnNotFound() {
        client = getSearchServiceClientBuilder().buildClient();

        try {
            client.getIndex("thisindexdoesnotexist");
            Assert.fail("getIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
            Assert.assertTrue(ex.getMessage().contains("No index with the name 'thisindexdoesnotexist' was found in the service"));
        }
    }

    @Override
    public void existsReturnsTrueForExistingIndex() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        client.createIndex(index);

        Assert.assertTrue(client.indexExists(index.getName()));
    }

    @Override
    public void existsReturnsFalseForNonExistingIndex() {
        client = getSearchServiceClientBuilder().buildClient();

        Assert.assertFalse(client.indexExists("invalidindex"));
    }

    @Override
    public void deleteIndexIfNotChangedWorksOnlyOnCurrentResource() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        Index staleResource = client.upsertIndex(index);
        Index currentResource = client.upsertIndex(mutateCorsOptionsInIndex(staleResource));

        try {
            client.deleteIndex(index.getName(), null, generateIfMatchAccessCondition(staleResource.getETag()));
            Assert.fail("deleteIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
        client.deleteIndex(index.getName(), null, generateIfMatchAccessCondition(currentResource.getETag()));
    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        client.createIndex(index);

        client.deleteIndex(index.getName(), null, generateIfExistsAccessCondition());
        try {
            client.deleteIndex(index.getName(), null, generateIfExistsAccessCondition());
            Assert.fail("deleteIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
    }

    @Override
    public void deleteIndexIsIdempotent() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = new Index()
            .setName("hotels")
            .setFields(Collections.singletonList(
                new Field()
                    .setName("HotelId")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
            ));
        Response deleteResponse = client.deleteIndexWithResponse(index.getName(), null, null, null);
        Assert.assertEquals(404, deleteResponse.getStatusCode());

        Response createResponse = client.createIndexWithResponse(index, null, null);
        Assert.assertEquals(201, createResponse.getStatusCode());

        // Delete the same index twice
        deleteResponse = client.deleteIndexWithResponse(index.getName(), null, null, null);
        Assert.assertEquals(204, deleteResponse.getStatusCode());

        deleteResponse = client.deleteIndexWithResponse(index.getName(), null, null, null);
        Assert.assertEquals(404, deleteResponse.getStatusCode());
    }

    @Override
    public void canCreateAndDeleteIndex() {

    }

    @Override
    public void canAddSynonymFieldProperty() {
        client = getSearchServiceClientBuilder().buildClient();

        String synonymMapName = "names";
        SynonymMap synonymMap = new SynonymMap().setName(synonymMapName).setSynonyms("hotel,motel");
        client.createSynonymMap(synonymMap);

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

        Index createdIndex = client.createIndex(index);

        List<String> actualSynonym = index.getFields().get(1).getSynonymMaps();
        List<String> expectedSynonym = createdIndex.getFields().get(1).getSynonymMaps();
        Assert.assertEquals(actualSynonym, expectedSynonym);
    }

    public void canUpdateIndexDefinition() {
        client = getSearchServiceClientBuilder().buildClient();

        Index fullFeaturedIndex = createTestIndex();
        Index initialIndex = createTestIndex();

        // Start out with no scoring profiles and different CORS options.
        initialIndex.setName(fullFeaturedIndex.getName());
        initialIndex.setScoringProfiles(new ArrayList<>());
        initialIndex.setDefaultScoringProfile(null);
        initialIndex.setCorsOptions(initialIndex.getCorsOptions().setAllowedOrigins(Arrays.asList("*")));

        Index index = client.createIndex(initialIndex);

        // Now update the index.
        index.setScoringProfiles(fullFeaturedIndex.getScoringProfiles());
        index.setDefaultScoringProfile(fullFeaturedIndex.getDefaultScoringProfile());
        index.setCorsOptions(index.getCorsOptions()
            .setAllowedOrigins(fullFeaturedIndex.getCorsOptions().getAllowedOrigins()));

        Index updatedIndex = client.upsertIndex(index);

        assertIndexesEqual(fullFeaturedIndex, updatedIndex);
    }

    @Override
    public void upsertIndexCreatesWhenIndexDoesNotExist() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();

        Response<Index> createOrUpdateResponse = client.upsertIndexWithResponse(index,
            null,
            null,
            null, Context.NONE);

        Assert.assertEquals(HttpResponseStatus.CREATED.code(), createOrUpdateResponse.getStatusCode());
    }

    @Override
    public void upsertIndexIfNotExistsFailsOnExistingResource() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null);
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);

        try {
            client.upsertIndex(mutatedResource, generateIfNotExistsAccessCondition(), null);
            Assert.fail("upsertIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
    }

    @Override
    public void upsertIndexIfNotExistsSucceedsOnNoResource() {
        client = getSearchServiceClientBuilder().buildClient();

        Index resource = createTestIndex();
        Index updatedResource = client.upsertIndex(resource, generateIfNotExistsAccessCondition(), null);

        Assert.assertFalse(updatedResource.getETag().isEmpty());
    }

    @Override
    public void upsertIndexIfExistsSucceedsOnExistingResource() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null);
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Index updatedResource = client.upsertIndex(mutatedResource, generateIfExistsAccessCondition(), null);

        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void upsertIndexIfExistsFailsOnNoResource() {
        client = getSearchServiceClientBuilder().buildClient();

        Index resource = createTestIndex();

        try {
            client.upsertIndex(resource, generateIfExistsAccessCondition(), null);
            Assert.fail("upsertIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
        // The resource should never have been created on the server, and thus it should not have an ETag
        Assert.assertNull(resource.getETag());
    }

    @Override
    public void upsertIndexIfNotChangedSucceedsWhenResourceUnchanged() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null);
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Index updatedResource = client.upsertIndex(mutatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null);

        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }

    @Override
    public void upsertIndexIfNotChangedFailsWhenResourceChanged() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        Index createdResource = client.upsertIndex(index, generateEmptyAccessCondition(), null);
        Index mutatedResource = mutateCorsOptionsInIndex(createdResource);
        Index updatedResource = client.upsertIndex(mutatedResource, generateEmptyAccessCondition(), null);

        try {
            client.upsertIndex(updatedResource, generateIfMatchAccessCondition(createdResource.getETag()), null);
            Assert.fail("upsertIndex did not throw an expected Exception");
        } catch (Exception ex) {
            Assert.assertEquals(HttpResponseException.class, ex.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) ex).getResponse().getStatusCode());
        }
        Assert.assertFalse(createdResource.getETag().isEmpty());
        Assert.assertFalse(updatedResource.getETag().isEmpty());
        Assert.assertNotEquals(createdResource.getETag(), updatedResource.getETag());
    }
}
