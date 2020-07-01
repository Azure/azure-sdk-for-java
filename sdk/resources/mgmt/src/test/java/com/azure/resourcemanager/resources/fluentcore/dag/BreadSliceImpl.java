// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.ExecutableImpl;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation for IBreadSlice.
 */
public class BreadSliceImpl extends ExecutableImpl<IBreadSlice> implements IBreadSlice {
    private final String name;

    public BreadSliceImpl(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public Mono<IBreadSlice> executeWorkAsync() {
        System.out.println("Bread(" + this.name + ")::executeWorkAsync() [Getting slice from store]");
        return Mono.just(this)
                .delayElement(Duration.ofMillis(250))
                .map(breadSlice -> breadSlice);
    }

    @Override
    public IBreadSlice withAnotherSliceFromStore(Executable<IBreadSlice> breadFetcher) {
        this.addDependency(breadFetcher);
        return this;
    }

    @Override
    public IBreadSlice withNewOrder(Creatable<IOrder> order) {
        this.addDependency(order);
        return this;
    }
}
