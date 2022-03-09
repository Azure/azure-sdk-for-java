// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.BatchDeletionImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class BatchDeletionImplTests {

    @Test
    public void testBatchDeletion() {
        BiFunction<String, String, Mono<Void>> mockDeleteByGroupAndNameAsync =
            (rgName, name) -> name.startsWith("invalid") ? Mono.error(new ManagementException("fail on " + name, null)) : Mono.empty();

        // 1 error
        List<String> names = Arrays.asList("valid1", "invalid2", "valid3", "valid4", "valid5");
        List<String> ids = names.stream()
            .map(name -> "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg1/providers/Microsoft.Compute/disks/" + name)
            .collect(Collectors.toList());

        Map<String, String> resultIds = new ConcurrentHashMap<>();
        Flux<String> fluxIds = BatchDeletionImpl.deleteByIdsAsync(ids, mockDeleteByGroupAndNameAsync);

        Assertions.assertThrows(ManagementException.class, () -> {
            fluxIds.doOnNext(id -> resultIds.put(id, id))
                .onErrorMap(e -> e)
                .blockLast();
        });
        Assertions.assertEquals(4, resultIds.size());
    }

    @Test
    public void testBatchDeletionMultipleException() {
        BiFunction<String, String, Mono<Void>> mockDeleteByGroupAndNameAsync =
            (rgName, name) -> name.startsWith("invalid") ? Mono.error(new ManagementException("fail on " + name, null)) : Mono.empty();

        // more than 1 errors
        List<String> names = Arrays.asList("valid1", "invalid2", "valid3", "invalid4", "valid5");
        List<String> ids = names.stream()
            .map(name -> "/subscriptions/00000000-0000-0000-0000-000000000000/resourcegroups/rg1/providers/Microsoft.Compute/disks/" + name)
            .collect(Collectors.toList());

        Map<String, String> resultIds = new ConcurrentHashMap<>();
        Flux<String> fluxIds = BatchDeletionImpl.deleteByIdsAsync(ids, mockDeleteByGroupAndNameAsync);

        // reactor.core.Exceptions.CompositeException
        Assertions.assertThrows(ManagementException.class, () -> {
            fluxIds.doOnNext(id -> resultIds.put(id, id))
                .onErrorMap(e -> e)
                .blockLast();
        });
        Assertions.assertEquals(3, resultIds.size());
    }
}
