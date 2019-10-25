// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.models.SynonymMap;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.test.StepVerifier;

public class SynonymMapAsyncTests extends SynonymMapTestBase {
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

    }

    @Override
    public void getSynonymMapThrowsOnNotFound() {

    }

    @Override
    public void canUpdateSynonymMap() {

    }

    @Override
    public void createOrUpdateCreatesWhenSynonymMapDoesNotExist() {

    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource() {

    }

    @Override
    public void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource() {

    }

    @Override
    public void updateSynonymMapIfExistsSucceedsOnExistingResource() {

    }

    @Override
    public void updateSynonymMapIfExistsFailsOnNoResource() {

    }

    @Override
    public void updateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged() {

    }

    @Override
    public void updateSynonymMapIfNotChangedFailsWhenResourceChanged() {

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

    }

    @Override
    public void existsReturnsTrueForExistingSynonymMap() {

    }

    @Override
    public void existsReturnsFalseForNonExistingSynonymMap() {

    }
}

