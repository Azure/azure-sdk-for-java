// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.Field;
import com.azure.search.models.Index;
import org.junit.Test;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public abstract class IndexManagementTestBase extends SearchServiceTestBase {

    @Test
    public abstract void createIndexReturnsCorrectDefinition();

    @Test
    public abstract void createIndexReturnsCorrectDefaultValues();

    @Test
    public abstract void createIndexFailsWithUsefulMessageOnUserError();

    @Test
    public abstract void getIndexReturnsCorrectDefinition();

    @Test
    public abstract void getIndexThrowsOnNotFound();

    @Test
    public abstract void existsReturnsTrueForExistingIndex();

    @Test
    public abstract void existsReturnsFalseForNonExistingIndex();

    @Test
    public abstract void deleteIndexIfNotChangedWorksOnlyOnCurrentResource();

    @Test
    public abstract void deleteIndexIfExistsWorksOnlyWhenResourceExists();

    @Test
    public abstract void deleteIndexIsIdempotent();

    @Test
    public abstract void canCreateAndDeleteIndex();

    @Test
    public abstract void canCreateAndListIndexes();

    @Test
    public abstract void canListIndexesWithSelectedField();

    @Test
    public abstract void canAddSynonymFieldProperty();

    @Test
    public abstract void canUpdateSynonymFieldProperty();

    @Test
    public abstract void canUpdateIndexDefinition();

    @Test
    public abstract void canUpdateSuggesterWithNewIndexFields();

    @Test
    public abstract void createOrUpdateIndexThrowsWhenUpdatingSuggesterWithExistingIndexFields();

    @Test
    public abstract void createOrUpdateIndexCreatesWhenIndexDoesNotExist();

    @Test
    public abstract void createOrUpdateIndexIfNotExistsFailsOnExistingResource();

    @Test
    public abstract void createOrUpdateIndexIfNotExistsSucceedsOnNoResource();

    @Test
    public abstract void createOrUpdateIndexIfExistsSucceedsOnExistingResource();

    @Test
    public abstract void createOrUpdateIndexIfExistsFailsOnNoResource();

    @Test
    public abstract void createOrUpdateIndexIfNotChangedSucceedsWhenResourceUnchanged();

    @Test
    public abstract void createOrUpdateIndexIfNotChangedFailsWhenResourceChanged();

    protected void assertIndexesEqual(Index expected, Index actual) {
        expected.setETag("none");
        actual.setETag("none");

        // we ignore defaults as when properties are not set they are returned from the service with
        // default values
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    protected Index mutateCorsOptionsInIndex(Index index) {
        index.setCorsOptions(index.getCorsOptions().setAllowedOrigins("*"));
        return index;
    }

    protected Field getFieldByName(Index index, String name) {
        return index.getFields()
            .stream()
            .filter(f -> f.getName().equals(name))
            .findFirst().get();
    }
}
