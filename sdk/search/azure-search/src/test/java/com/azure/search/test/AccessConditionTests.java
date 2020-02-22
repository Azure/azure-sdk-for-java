// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.CoreUtils;
import com.azure.search.TestHelpers;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.azure.search.TestHelpers.generateIfExistsAccessCondition;
import static com.azure.search.TestHelpers.generateIfNotChangedAccessCondition;
import static com.azure.search.TestHelpers.generateIfNotExistsAccessCondition;
import static com.azure.search.TestHelpers.getETag;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class AccessConditionTests {

    /**
     * Checks that create or update fails when a resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void createOrUpdateIfNotExistsFailsOnExistingResource(
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Supplier<T> newResourceDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource in the search service
        AccessOptions accessOptions = new AccessOptions(generateIfNotExistsAccessCondition());
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        try {
            // Change the resource object (locally, not on the service)
            T mutatedResource = mutateResourceDefinition.apply(createdResource);

            // Update the resource, expect to fail as it already exists
            createOrUpdateDefinition.apply(mutatedResource, accessOptions);
            fail("createOrUpdateDefinition should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(),
                ((HttpResponseException) exc).getResponse().getStatusCode());
        }
    }

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

        AccessOptions accessOptions = new AccessOptions(generateIfNotExistsAccessCondition());

        // Create the resource on the service
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        assertFalse(TestHelpers.isBlank(getETag(createdResource)));

        return createdResource;
    }

    /**
     * Checks that delete if exists for a resource works only when the resource exists
     *
     * @param deleteFunc a function that deletes a resource in the service
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param resourceName the name of the resource
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void deleteIfExistsWorksOnlyWhenResourceExists(BiConsumer<String, AccessOptions> deleteFunc,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition, Supplier<T> newResourceDefinition,
        String resourceName) {
        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(null);

        // Create it on the search service
        createOrUpdateDefinition.apply(newResource, accessOptions);

        // Try to delete and expect to succeed
        accessOptions = new AccessOptions(generateIfExistsAccessCondition());
        deleteFunc.accept(resourceName, accessOptions);

        // Try to delete again and expect to fail
        try {
            deleteFunc.accept(resourceName, accessOptions);
            fail("deleteFunc should have failed due to non existent resource");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }
    }

    /**
     * Checks that delete if not changed only works on the last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void deleteIfNotChangedWorksOnlyOnCurrentResource(BiConsumer<String, AccessOptions> deleteFunc,
        Supplier<T> newResourceDefinition, BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        String resourceName) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T staleResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(null);

        // Create the resource in the search service
        staleResource = createOrUpdateDefinition.apply(staleResource, accessOptions);

        // Get the eTag for the newly created resource
        String eTagStale = getETag(staleResource);

        // Update the resource, the eTag will be changed
        T currentResource = createOrUpdateDefinition.apply(staleResource, accessOptions);

        try {
            accessOptions = new AccessOptions(generateIfNotChangedAccessCondition(eTagStale));
            deleteFunc.accept(resourceName, accessOptions);
            fail("deleteFunc should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // Get the new eTag
        String eTagCurrent = getETag(currentResource);
        accessOptions = new AccessOptions(generateIfNotChangedAccessCondition(eTagCurrent));

        // Delete should succeed
        deleteFunc.accept(resourceName, accessOptions);
    }

    /**
     * Checks that update if exists fails when the resource does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / DataSource / etc)
     */
    public static <T> void updateIfExistsFailsOnNoResource(Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition) {
        T newResource = newResourceDefinition.get();
        try {
            AccessOptions accessOptions = new AccessOptions(generateIfExistsAccessCondition());
            createOrUpdateDefinition.apply(newResource, accessOptions);
            fail("createOrUpdateDefinition should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // The resource should never have been created on the server, and thus it should not have an ETag
        assertNull(getETag(newResource));
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
        AccessOptions accessOptions = new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        // get the original eTag
        String originalETag = getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfExistsAccessCondition());
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated ETag
        String updatedETag = getETag(mutateResource);

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
        AccessOptions accessOptions = new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalETag = getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalETag));
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated eTag
        String updatedETag = getETag(mutateResource);

        // Update and check the eTags were changed
        try {
            createOrUpdateDefinition.apply(mutateResource, accessOptions);
            fail("createOrUpdateDefinition should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            assertEquals(HttpResponseException.class, exc.getClass());
            assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
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
        AccessOptions accessOptions = new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalETag = getETag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalETag));
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        String updatedETag = getETag(mutateResource);

        // Check eTags as expected
        assertFalse(CoreUtils.isNullOrEmpty(originalETag));
        assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
        assertNotEquals(originalETag, updatedETag);
    }
}
