// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.utils.ReactorMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Utility class for batch deletion.
 */
public class BatchDeletionImpl {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Flux<String> deleteByIdsAsync(Collection<String> ids,
                                                BiFunction<String, String, Mono<Void>> deleteByIdAsync) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        Collection<Mono<String>> observables = new ArrayList<>();
        for (String id : ids) {
            final String resourceGroupName = ResourceUtils.groupFromResourceId(id);
            final String name = ResourceUtils.nameFromResourceId(id);
            Mono<String> o = ReactorMapper.map(deleteByIdAsync.apply(resourceGroupName, name), id);
            observables.add(o);
        }

        return Flux.mergeDelayError(32, observables.toArray(new Mono[0]));
    }
}
