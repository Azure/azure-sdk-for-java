// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.SynonymMap;
import org.junit.Assert;
import org.junit.Test;

public abstract class SynonymMapTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createSynonymMapReturnsCorrectDefinition();

    @Test
    public abstract void createSynonymMapFailsWithUsefulMessageOnUserError();

    public abstract void getSynonymMapReturnsCorrectDefinition();

    public abstract void getSynonymMapThrowsOnNotFound();

    public abstract void canUpdateSynonymMap();

    public abstract void createOrUpdateCreatesWhenSynonymMapDoesNotExist();

    public abstract void createOrUpdateSynonymMapIfNotExistsFailsOnExistingResource();

    public abstract void createOrUpdateSynonymMapIfNotExistsSucceedsOnNoResource();

    public abstract void updateSynonymMapIfExistsSucceedsOnExistingResource();

    public abstract void updateSynonymMapIfExistsFailsOnNoResource();

    public abstract void updateSynonymMapIfNotChangedSucceedsWhenResourceUnchanged();

    public abstract void updateSynonymMapIfNotChangedFailsWhenResourceChanged();

    public abstract void deleteSynonymMapIfNotChangedWorksOnlyOnCurrentResource();

    public abstract void deleteSynonymMapIfExistsWorksOnlyWhenResourceExists();

    public abstract void deleteSynonymMapIsIdempotent();

    public abstract void canCreateAndListSynonymMaps();

    public abstract void existsReturnsTrueForExistingSynonymMap();

    public abstract void existsReturnsFalseForNonExistingSynonymMap();

    protected void assertSynonymMapsEqual(SynonymMap actual, SynonymMap expected) {
        Assert.assertEquals(actual.getName(), expected.getName());
        Assert.assertEquals(actual.getSynonyms(), expected.getSynonyms());
        Assert.assertEquals(actual.getFormat(), expected.getFormat());
    }

    protected SynonymMap createTestSynonymMap() {
        return new SynonymMap().setName("test-synonym").setSynonyms("word1,word2");
    }
}
