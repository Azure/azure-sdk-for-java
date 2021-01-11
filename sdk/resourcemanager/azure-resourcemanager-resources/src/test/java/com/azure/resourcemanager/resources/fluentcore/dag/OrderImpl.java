// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Implementation of {@link IOrder}
 */
public class OrderImpl
        extends CreatableUpdatableImpl<IOrder, OrderInner, OrderImpl>
        implements IOrder {
    /**
     * Creates SandwichImpl.
     *
     * @param name the name of the model
     * @param innerObject the inner model object
     */
    protected OrderImpl(String name, OrderInner innerObject) {
        super(name, name, innerObject);
    }

    @Override
    public Mono<IOrder> createResourceAsync() {
        System.out.println("Order(" + this.name() + ")::createResourceAsync() [Creating order]");
        return Mono.just(this)
                .delayElement(Duration.ofMillis(250))
                .map(sandwich -> sandwich);
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Mono<OrderInner> getInnerAsync() {
        return Mono.just(this.innerModel());
    }
}
