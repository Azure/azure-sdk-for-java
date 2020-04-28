// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test;

import com.azure.core.util.CoreUtils;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.models.SearchErrorException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class AccessConditionTests {
    /**
     * Checks that create or update only work if item which does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> T createOrUpdateIfNotExistsSucceedsOnNoResource(
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Supplier<T> newResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        AccessOptions accessOptions = new AccessOptions(true);

        // Create the resource on the service
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        assertFalse(TestHelpers.isBlank(TestHelpers.getETag(createdResource)));

        return createdResource;
    }

    /**
     * Checks that delete if exists for a resource works only when the resource exists
     *
     * @param deleteFunc a function that deletes a resource in the service
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void deleteIfExistsWorksOnlyWhenResourceExists(BiConsumer<T, AccessOptions> deleteFunc,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Supplier<T> newResourceDefinition) {
        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(false);

        // Create it on the search service
        T updatedSource = createOrUpdateDefinition.apply(newResource, accessOptions);

        // Try to delete and expect to succeed
        accessOptions = new AccessOptions(true);
        deleteFunc.accept(updatedSource, accessOptions);

        // Try to delete again and expect to fail
        try {
            deleteFunc.accept(updatedSource, accessOptions);
            fail("deleteFunc should have failed due to non existent resource");
        } catch (Exception exc) {
            assertEquals(SearchErrorException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((SearchErrorException) exc).getResponse().getStatusCode());
        }
    }

    /**
     * Checks that delete if not changed only works on the last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void deleteIfNotChangedWorksOnlyOnCurrentResource(BiConsumer<T, AccessOptions> deleteFunc,
        Supplier<T> newResourceDefinition, BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        String resourceName) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T staleResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(true);

        // Create the resource in the search service
        staleResource = createOrUpdateDefinition.apply(staleResource, accessOptions);

        // Update the resource, the eTag will be changed
        T currentResource = createOrUpdateDefinition.apply(staleResource, accessOptions);

        try {
            accessOptions = new AccessOptions(true);
            deleteFunc.accept(staleResource, accessOptions);
            fail("deleteFunc should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(SearchErrorException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((SearchErrorException) exc).getResponse().getStatusCode());
        }

        // Get the new eTag
        accessOptions = new AccessOptions(true);

        // Delete should succeed
        deleteFunc.accept(currentResource, accessOptions);
    }

    /**
     * Checks that update if exists succeed when the resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void updateIfExistsSucceedsOnExistingResource(Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions = new AccessOptions(false);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        // get the original eTag
        String originalETag = TestHelpers.getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(false);
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated ETag
        String updatedETag = TestHelpers.getETag(mutateResource);

        // Verify the eTag is not empty and was changed
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    /**
     * Checks that update if not changed fails on unchanged resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void updateIfNotChangedFailsWhenResourceChanged(Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions = new AccessOptions(false);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalETag = TestHelpers.getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(true);
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated eTag
        String updatedETag = TestHelpers.getETag(mutateResource);

        // Update and check the eTags were changed
        try {
            createOrUpdateDefinition.apply(newResource, accessOptions);
            fail("createOrUpdateDefinition should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(SearchErrorException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((SearchErrorException) exc).getResponse().getStatusCode());
        }

        // Check eTags
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }

    /**
     * Checks that update if not changed only works on last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void updateIfNotChangedSucceedsWhenResourceUnchanged(Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions = new AccessOptions(false);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalETag = TestHelpers.getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(true);
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        String updatedETag = TestHelpers.getETag(mutateResource);

        // Check eTags as expected
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }
}
