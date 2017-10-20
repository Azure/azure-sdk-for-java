/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.dag;

import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreateUpdateTask;
import org.junit.Assert;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of {@link IPancake}
 */
class PancakeImpl
        extends CreatableUpdatableImpl<IPancake, PancakeInner, PancakeImpl>
        implements IPancake {
    final List<Creatable<IPancake>> delayedPancakes;
    final long eventDelayInMilliseconds;
    final Throwable errorToThrow;
    boolean prepareCalled = false;

    public PancakeImpl(String name, long eventDelayInMilliseconds) {
        this(name, eventDelayInMilliseconds, false);
    }

    public PancakeImpl(String name, long eventDelayInMilliseconds, boolean fault) {
        super(name, new PancakeInner());
        this.eventDelayInMilliseconds = eventDelayInMilliseconds;
        if (fault) {
            this.errorToThrow = new RuntimeException(name);
        } else {
            this.errorToThrow = null;
        }
        delayedPancakes = new ArrayList<>();
    }

    /**
     * a pancake specified via this wither will be added immediately as dependency.
     *
     * @param pancake the pancake
     * @return the next stage of pancake
     */
    @Override
    public PancakeImpl withInstantPancake(Creatable<IPancake> pancake) {
        this.addCreatableDependency(pancake);
        return this;
    }

    /**
     * a pancake specified via this wither will not be added immediately as a dependency, will be added only
     * inside prepare {@link CreateUpdateTask.ResourceCreatorUpdater#prepare()}
     *
     * @param pancake the pancake
     * @return the next stage of pancake
     */
    @Override
    public PancakeImpl withDelayedPancake(Creatable<IPancake> pancake) {
        this.delayedPancakes.add(pancake);
        return this;
    }

    @Override
    public Observable<IPancake> createResourceAsync() {
        if (this.errorToThrow == null) {
            System.out.println("Pancake(" + this.name() + ")::createResourceAsync() 'onNext()'");
            return Observable.just(this)
                    .delay(this.eventDelayInMilliseconds, TimeUnit.MILLISECONDS)
                    .map(new Func1<PancakeImpl, IPancake>() {
                        @Override
                        public IPancake call(PancakeImpl pancake) {
                            return pancake;
                        }
                    });
        } else {
            System.out.println("Pancake(" + this.name() + ")::createResourceAsync() 'onError()'");
            return Observable.just(this)
                    .delay(this.eventDelayInMilliseconds, TimeUnit.MILLISECONDS)
                    .flatMap(new Func1<PancakeImpl, Observable<IPancake>>() {
                        @Override
                        public Observable<IPancake> call(PancakeImpl pancake) {
                            return toErrorObservable(errorToThrow);
                        }
                    });
        }
    }

    @Override
    public void prepare() {
        Assert.assertFalse("PancakeImpl::prepare() should not be called multiple times", this.prepareCalled);
        prepareCalled = true;
        int oldCount = this.taskGroup().getNode(this.key()).dependencyKeys().size();
        for(Creatable<IPancake> pancake : this.delayedPancakes) {
            this.addCreatableDependency(pancake);
        }
        int newCount = this.taskGroup().getNode(this.key()).dependencyKeys().size();
        System.out.println("Pancake(" + this.name() + ")::prepare() 'delayedSize':" + this.delayedPancakes.size()
                + " 'dependency count [old, new]': [" + oldCount + "," + newCount + "]");
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Observable<PancakeInner> getInnerAsync() {
        return null;
    }

    private Observable<IPancake> toErrorObservable(Throwable throwable) {
        return Observable.error(throwable);
    }
}
