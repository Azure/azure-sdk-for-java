/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.Executable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link ISandwich}
 */
public class SandwichImpl
        extends CreatableUpdatableImpl<ISandwich, SandwichInner, SandwichImpl>
        implements ISandwich {
    /**
     * Creates SandwichImpl.
     *
     * @param name        the name of the model
     * @param innerObject the inner model object
     */
    protected SandwichImpl(String name, SandwichInner innerObject) {
        super(name, innerObject);
    }


    @Override
    public ISandwich withBreadSliceFromStore(Executable<IBreadSlice> breadFetcher) {
        this.addExecutableDependency(breadFetcher);
        return this;
    }

    @Override
    public Observable<ISandwich> createResourceAsync() {
        System.out.println("Sandwich(" + this.name() + ")::createResourceAsync() [Creating sandwich]");
        return Observable.just(this)
                .delay(250, TimeUnit.MILLISECONDS)
                .map(new Func1<SandwichImpl, ISandwich>() {
                    @Override
                    public ISandwich call(SandwichImpl sandwich) {
                        return sandwich;
                    }
                });
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Observable<SandwichInner> getInnerAsync() {
        return Observable.just(this.inner());
    }
}