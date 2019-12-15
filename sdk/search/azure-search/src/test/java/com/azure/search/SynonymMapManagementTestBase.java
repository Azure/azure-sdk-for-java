// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.SynonymMap;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public abstract class SynonymMapManagementTestBase extends SearchServiceTestBase {
    @Test
    public abstract void createSynonymMapReturnsCorrectDefinition();

    @Test
    public abstract void createSynonymMapReturnsCorrectDefinitionWithResponse();

    @Test
    public abstract void createSynonymMapFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void getSynonymMapReturnsCorrectDefinition();

    @Test
    public abstract void getSynonymMapReturnsCorrectDefinitionWithResponse();

    @Test
    public abstract void getSynonymMapThrowsOnNotFound();

    @Test
    public abstract void getSynonymMapThrowsOnNotFoundWithResponse();

    @Test
    public abstract void canUpdateSynonymMap();

    @Test
    public abstract void canUpdateSynonymMapWithResponse();

    @Test
    public abstract void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExist();

    @Test
    public abstract void createOrUpdateSynonymMapCreatesWhenSynonymMapDoesNotExistWithResponse();

    @Test
    public abstract void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void createOrUpdateSynonymMapIfExistsSucceedsOnExistingResource();

    @Test
    public abstract void createOrUpdateSynonymMapIfExistsFailsOnNoResource();

    @Test
    public abstract void createOrUpdateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged();

    @Test
    public abstract void createOrUpdateSynonymMapIfNotChangedFailsWhenResourceChanged();

    @Test
    public abstract void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource();

    @Test
    public abstract void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteSynonymMapIsIdempotent();

    @Test
    public abstract void canCreateAndDeleteSynonymMap();

    @Test
    public abstract void canCreateAndListSynonymMaps();

    @Test
    public abstract void canListSynonymMapsWithSelectedField();

    @Test
    public abstract void existsReturnsTrueForExistingSynonymMap();

    @Test
    public abstract void existsReturnsTrueForExistingSynonymMapWithResponse();

    @Test
    public abstract void existsReturnsFalseForNonExistingSynonymMap();

    @Test
    public abstract void existsReturnsFalseForNonExistingSynonymMapWithResponse();

    void assertSynonymMapsEqual(SynonymMap actual, SynonymMap expected) {
        Assert.assertEquals(actual.getName(), expected.getName());
        Assert.assertEquals(actual.getSynonyms(), expected.getSynonyms());
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
