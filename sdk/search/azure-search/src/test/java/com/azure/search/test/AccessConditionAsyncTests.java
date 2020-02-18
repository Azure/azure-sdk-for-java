// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.CoreUtils;
import com.azure.search.TestHelpers;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Contains async version of the access condition tests
 */
public class AccessConditionAsyncTests extends AccessConditionBase {

    /**
     * Checks that delete if exists for a resource works only when the resource exists
     *
     * @param deleteFunc a function that deletes a resource in the service
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param resourceName the name of the resource
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void deleteIfExistsWorksOnlyWhenResourceExistsAsync(
        BiFunction<String, AccessOptions, Mono<Void>> deleteFunc,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition,
        String resourceName
    ) {
        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        AccessOptions accessOptions = new AccessOptions(null);
        AccessOptions deleteAccessOptions = new AccessOptions(generateIfExistsAccessCondition());

        // Create it on the search service
        Mono<Void> deletionResult = createOrUpdateDefinition.apply(newResource, accessOptions)
            .flatMap(r -> deleteFunc.apply(resourceName, deleteAccessOptions));

        // Try to delete again and expect to fail
        StepVerifier
            .create(Flux.concat(deletionResult, deleteFunc.apply(resourceName, deleteAccessOptions)))
            .expectNext()
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(),
                    ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    /**
     * Checks that delete if not changed only works on the last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param deleteFunc a function that deletes the resource
     * @param mutateResourceDefinition a function that changes the resource
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void deleteIfNotChangedWorksOnlyOnCurrentResourceAsync(
        BiFunction<String, AccessOptions, Mono<Void>> deleteFunc, Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition, Function<T, T> mutateResourceDefinition,
        String resourceName) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T staleResource = newResourceDefinition.get();
        AccessOptions accessOptions = new AccessOptions(null);

        // Create the resource in the search service
        staleResource =
            createOrUpdateDefinition.apply(staleResource, accessOptions).block();

        // Get the eTag for the newly created resource
        String eTagStale = getEtag(staleResource);

        // Update the resource, the etag will be changed
        T currentResource = mutateResourceDefinition.apply(staleResource);
        currentResource = createOrUpdateDefinition.apply(currentResource, accessOptions).block();

        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(eTagStale));
        StepVerifier
            .create(deleteFunc.apply(resourceName, accessOptions))
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        // Get the new eTag
        String eTagCurrent = getEtag(currentResource);
        accessOptions = new AccessOptions(generateIfNotChangedAccessCondition(eTagCurrent));

        // Delete should succeed
        StepVerifier
            .create(deleteFunc.apply(resourceName, accessOptions))
            .verifyComplete();
    }

    /**
     * Checks that create or update fails when a resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void createOrUpdateIfNotExistsFailsOnExistingResourceAsync(
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource in the search service
        AccessOptions accessOptions =
            new AccessOptions(generateIfNotExistsAccessCondition());
        T createdResource = createOrUpdateDefinition.apply(newResource, accessOptions).block();

        // Change the resource object (locally, not on the service)
        T mutatedResource = mutateResourceDefinition.apply(createdResource);

        // Update the resource, expect to fail as it already exists
        StepVerifier
            .create(createOrUpdateDefinition.apply(mutatedResource, accessOptions))
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    /**
     * Checks that create or update only work if item which does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void createOrUpdateIfNotExistsSucceedsOnNoResourceAsync(
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Supplier<T> newResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        AccessOptions accessOptions =
            new AccessOptions(generateIfNotExistsAccessCondition());

        StepVerifier
            .create(createOrUpdateDefinition.apply(newResource, accessOptions))
            .assertNext(r -> assertFalse(TestHelpers.isBlank(getEtag(newResource))))
            .verifyComplete();
    }

    /**
     * Checks that update if exists fails when the resource does not exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfExistsFailsOnNoResourceAsync(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition) {
        T newResource = newResourceDefinition.get();

        AccessOptions accessOptions =
            new AccessOptions(generateIfExistsAccessCondition());

        StepVerifier
            .create(createOrUpdateDefinition.apply(newResource, accessOptions))
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });

        // The resource should never have been created on the server, and thus it should not have an ETag
        assertNull(getEtag(newResource));
    }

    /**
     * Checks that update if exists succeed when the resource exists
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfExistsSucceedsOnExistingResourceAsync(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions = new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions).block();

        // get the original eTag
        String originalETag = getEtag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfExistsAccessCondition());
        Mono<T> updatedResource = createOrUpdateDefinition.apply(mutateResource, accessOptions);

        StepVerifier
            .create(updatedResource)
            .assertNext(res -> {
                // Get the updated ETag
                String updatedETag = getEtag(res);

                // Verify the eTag is not empty and was changed
                assertFalse(updatedETag.isEmpty());
                assertNotEquals(originalETag, updatedETag);
            })
            .verifyComplete();
    }

    /**
     * Checks that update if not changed only works on last/current resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfNotChangedFailsWhenResourceChangedAsync(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions = new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions).block();
        String originalETag = getEtag(newResource);

        // Change the resource object
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalETag));
        mutateResource = createOrUpdateDefinition.apply(mutateResource, accessOptions).block();

        // Get the updated eTag
        String updatedETag = getEtag(mutateResource);

        // Update and check the eTags were changed
        StepVerifier
            .create(createOrUpdateDefinition.apply(mutateResource, accessOptions))
            .then(() -> {
                assertFalse(CoreUtils.isNullOrEmpty(originalETag));
                assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
                assertNotEquals(originalETag, updatedETag);
            })
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.PRECONDITION_FAILED.code(), ((HttpResponseException) error).getResponse().getStatusCode());
            });
    }

    /**
     * Checks that update if not changed fails on unchanged resource
     *
     * @param createOrUpdateDefinition a function that creates or updates a resource in the service
     * @param newResourceDefinition a function to generate a new resource object
     * @param <T> one of the entity types (Index / Indexer / SynonymMap / Datasource / etc)
     */
    public <T> void updateIfNotChangedSucceedsWhenResourceUnchangedAsync(
        Supplier<T> newResourceDefinition,
        BiFunction<T, AccessOptions, Mono<T>> createOrUpdateDefinition,
        Function<T, T> mutateResourceDefinition) {

        // Create a new resource (Indexer, SynonymMap, etc...)
        T newResource = newResourceDefinition.get();

        // Create the resource on the search service
        AccessOptions accessOptions =
            new AccessOptions(null);
        newResource = createOrUpdateDefinition.apply(newResource, accessOptions).block();
        String originalEtag = getEtag(newResource);

        // Change the resource
        T mutateResource = mutateResourceDefinition.apply(newResource);

        // Update the resource on the service
        accessOptions.setAccessCondition(generateIfNotChangedAccessCondition(originalEtag));

        StepVerifier
            .create(createOrUpdateDefinition.apply(mutateResource, accessOptions))
            .assertNext((res) -> {
                String updatedETag = getEtag(res);

                assertFalse(CoreUtils.isNullOrEmpty(originalEtag));
                assertFalse(CoreUtils.isNullOrEmpty(updatedETag));
                assertNotEquals(originalEtag, updatedETag);
            })
            .verifyComplete();
    }
}
