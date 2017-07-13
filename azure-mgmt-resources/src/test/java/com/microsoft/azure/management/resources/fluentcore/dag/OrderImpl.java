/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link IOrder}
 */
public class OrderImpl
        extends CreatableUpdatableImpl<IOrder, OrderInner, OrderImpl>
        implements IOrder {
    /**
     * Creates SandwichImpl.
     *
     * @param name        the name of the model
     * @param innerObject the inner model object
     */
    protected OrderImpl(String name, OrderInner innerObject) {
        super(name, innerObject);
    }

    @Override
    public Observable<IOrder> createResourceAsync() {
        System.out.println("Order(" + this.name() + ")::createResourceAsync() [Creating order]");
        return Observable.just(this)
                .delay(250, TimeUnit.MILLISECONDS)
                .map(new Func1<OrderImpl, IOrder>() {
                    @Override
                    public IOrder call(OrderImpl sandwich) {
                        return sandwich;
                    }
                });
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Observable<OrderInner> getInnerAsync() {
        return Observable.just(this.inner());
    }
}