// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Executable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation of {@link ISandwich}
 */
public class SandwichImpl
        extends CreatableUpdatableImpl<ISandwich, SandwichInner, SandwichImpl>
        implements ISandwich {
    /**
     * Creates SandwichImpl.
     *
     * @param name the name of the model
     * @param innerObject the inner model object
     */
    protected SandwichImpl(String name, SandwichInner innerObject) {
        super(name, name, innerObject);
    }


    @Override
    public ISandwich withBreadSliceFromStore(Executable<IBreadSlice> breadFetcher) {
        this.addDependency(breadFetcher);
        return this;
    }

    @Override
    public Mono<ISandwich> createResourceAsync() {
        System.out.println("Sandwich(" + this.name() + ")::createResourceAsync() [Creating sandwich]");
        return Mono.just(this)
                .delayElement(Duration.ofMillis(250))
                .map(sandwich -> sandwich);
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Mono<SandwichInner> getInnerAsync() {
        return Mono.just(this.inner());
    }
}
