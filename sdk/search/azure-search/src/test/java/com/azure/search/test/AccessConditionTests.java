// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.core.exception.HttpResponseException;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class AccessConditionTests extends AccessConditionBase {

    /**
     * Checks that create or update fails when a resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void createOrUpdateIfNotExistsFailsOnExistingResource(
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource in the search service
        AccessOptions accessOptions =
            new AccessOptions(generateIfNotExistsAccessCondition());
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        try {
            // Change the resource object (locally, not on the service)
            T mutatedResource = mutateResourceDefinition.apply(createdResource);

            // Update the resource, expect to fail as it already exists
            createOrUpdateDefinition.apply(mutatedResource, accessOptions);
            Assert.fail("createOrUpdateDefinition should have failed due to "
                + "selected AccessCondition");
        } catch (Exception exc) {
            Assert.assertEquals(HttpResponseException.class, exc.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }
    }

    /**
     * Checks that create or update only work if item which does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> T createOrUpdateIfNotExistsSucceedsOnNoResource(
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        AccessOptions accessOptions =
            new AccessOptions(generateIfNotExistsAccessCondition());

        // Create the resource on the service
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        String eTag = getEtag(createdResource);
        Assert.assertTrue(StringUtils.isNotBlank(eTag));

        return createdResource;
    }

    /**
     * Checks that delete if exists for a resource works only when the resource exists
     *
     * @param deleteFunc a function that deletes a resource in the service
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param resourceName the name of the resource
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void deleteIfExistsWorksOnlyWhenResourceExists(
        BiConsumer<String, AccessOptions> deleteFunc,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition,
        String resourceName
    ) {
        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(null);

        // Create it on the search service
        createOrUpdateDefinition.apply(newResource, accessOptions);

        // Try to delete and expect to succeed
        accessOptions =
            new AccessOptions(generateIfExistsAccessCondition());
        deleteFunc.accept(resourceName, accessOptions);

        // Try to delete again and expect to fail
        try {
            deleteFunc.accept(resourceName, accessOptions);
            Assert.fail("deleteFunc should have failed due to non existent resource");
        } catch (Exception exc) {
            Assert.assertEquals(HttpResponseException.class, exc.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }
    }

    /**
     * Checks that delete if not changed only works on the last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void deleteIfNotChangedWorksOnlyOnCurrentResource(
        BiConsumer<String, AccessOptions> deleteFunc,
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        String resourceName
    ) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T staleResource = newResourceDefinition.get();
        AccessOptions accessOptions =
            new AccessOptions(null);

        // Create the resource in the search service
        staleResource =
            createOrUpdateDefinition.apply(staleResource, accessOptions);

        // Get the eTag for the newly created resource
        String eTagStale = getEtag(staleResource);

        // Update the resource, the etag will be changed
        T currentResource = createOrUpdateDefinition.apply(staleResource, accessOptions);

        try {
            accessOptions =
                new AccessOptions(generateIfNotChangedAccessCondition(eTagStale));
            deleteFunc.accept(resourceName, accessOptions);
            Assert.fail("deleteFunc should have failed due to selected AccessCondition");
        } catch (Exception exc) {
            Assert.assertEquals(HttpResponseException.class, exc.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // Get the new eTag
        String eTagCurrent = getEtag(currentResource);
        accessOptions =
            new AccessOptions(generateIfNotChangedAccessCondition(eTagCurrent));

        // Delete should succeed
        deleteFunc.accept(resourceName, accessOptions);
    }

    /**
     * Checks that update if exists fails when the resource does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfExistsFailsOnNoResource(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition) {
        T newResource = newResourceDefinition.get();
        try {
            AccessOptions accessOptions =
                new AccessOptions(generateIfExistsAccessCondition());
            createOrUpdateDefinition.apply(newResource, accessOptions);
            Assert.fail("createOrUpdateDefinition should have failed due to "
                + "selected AccessCondition");
        } catch (Exception exc) {
            Assert.assertEquals(HttpResponseException.class, exc.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // The resource should never have been created on the server, and thus it should not have an ETag
        String eTag = getEtag(newResource);
        Assert.assertNull(eTag);
    }

    /**
     * Checks that update if exists succeed when the resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfExistsSucceedsOnExistingResource(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions =
            new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);

        // get the original eTag
        String originalETag = getEtag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfExistsAccessCondition());
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated ETag
        String updatedETag = getEtag(mutateResource);

        // Verify the eTag is not empty and was changed
        Assert.assertTrue(StringUtils.isNotEmpty(updatedETag));
        Assert.assertNotEquals(originalETag, updatedETag);
    }

    /**
     * Checks that update if not changed fails on unchanged resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfNotChangedFailsWhenResourceChanged(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions =
            new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalETag = getEtag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalETag));
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        // Get the updated eTag
        String updatedETag = getEtag(mutateResource);

        // Update and check the eTags were changed
        try {
            createOrUpdateDefinition.apply(mutateResource, accessOptions);
            Assert.fail("createOrUpdateDefinition should have failed due to "
                + "selected AccessCondition");
        } catch (Exception exc) {
            Assert.assertEquals(HttpResponseException.class, exc.getClass());
            Assert.assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) exc).getResponse().getStatusCode());
        }

        // Check eTags
        Assert.assertTrue(StringUtils.isNotEmpty(originalETag));
        Assert.assertTrue(StringUtils.isNotEmpty(updatedETag));
        Assert.assertNotEquals(originalETag, updatedETag);
    }

    /**
     * Checks that update if not changed only works on last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfNotChangedSucceedsWhenResourceUnchanged(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, T> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions =
            new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions);
        String originalEtag = getEtag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalEtag));
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        String updatedETag = getEtag(mutateResource);

        // Check eTags as expected
        Assert.assertTrue(StringUtils.isNotEmpty(originalEtag));
        Assert.assertTrue(StringUtils.isNotEmpty(updatedETag));
        Assert.assertNotEquals(originalEtag, updatedETag);
    }
}
