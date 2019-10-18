// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.search.models.DataType;
import com.azure.search.models.Field;
import com.azure.search.models.Index;
import com.azure.search.models.ScoringProfile;
import com.azure.search.models.MagnitudeScoringParameters;
import com.azure.search.models.MagnitudeScoringFunction;
import com.azure.search.models.ScoringFunctionAggregation;
import com.azure.search.models.ScoringFunctionInterpolation;
import com.azure.search.models.CorsOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.util.Collections;

public class IndexManagementSyncTests extends IndexManagementTestBase {
    private SearchServiceClient client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

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

        thrown.expect(HttpResponseException.class);
        thrown.expectMessage(expectedMessage);
        client.createIndex(index);
    }

    @Override
    public void getIndexReturnsCorrectDefinition() {
        client = getSearchServiceClientBuilder().buildClient();

        Index index = createTestIndex();
        client.createIndex(index);
        Index createdIndex = client.getIndex(index.getName());

        assertIndexesEqual(createdIndex, index);
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

    }

    @Override
    public void deleteIndexIfExistsWorksOnlyWhenResourceExists() {

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
}
