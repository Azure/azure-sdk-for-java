// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.dag;

import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreateUpdateTask;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link IPasta}
 */
class PastaImpl
        extends CreatableUpdatableImpl<IPasta, PastaInner, PastaImpl>
        implements IPasta {
    final List<Creatable<IPasta>> delayedPastas;
    final long eventDelayInMilliseconds;
    final Throwable errorToThrow;
    boolean prepareCalled = false;

    PastaImpl(String name, long eventDelayInMilliseconds) {
        this(name, eventDelayInMilliseconds, false);
    }

    PastaImpl(String name, long eventDelayInMilliseconds, boolean fault) {
        super(name, name, new PastaInner());
        this.eventDelayInMilliseconds = eventDelayInMilliseconds;
        if (fault) {
            this.errorToThrow = new RuntimeException(name);
        } else {
            this.errorToThrow = null;
        }
        delayedPastas = new ArrayList<>();
    }

    /**
     * a pasta specified via this wither will be added immediately as dependency.
     *
     * @param pasta the pasta
     * @return the next stage of pasta
     */
    @Override
    public PastaImpl withInstantPasta(Creatable<IPasta> pasta) {
        this.addDependency(pasta);
        return this;
    }

    /**
     * a pancake specified via this wither will not be added immediately as a dependency, will be added only
     * inside beforeGroupCreateOrUpdate {@link CreateUpdateTask.ResourceCreatorUpdater#beforeGroupCreateOrUpdate()}
     *
     * @param pasta the pasta
     * @return the next stage of pasta
     */
    @Override
    public PastaImpl withDelayedPasta(Creatable<IPasta> pasta) {
        this.delayedPastas.add(pasta);
        return this;
    }


    @Override
    public void beforeGroupCreateOrUpdate() {
        Assertions.assertFalse(this.prepareCalled, "PastaImpl::beforeGroupCreateOrUpdate() should not be called multiple times");
        prepareCalled = true;
        int oldCount = this.taskGroup().getNode(this.key()).dependencyKeys().size();
        for (Creatable<IPasta> pancake : this.delayedPastas) {
            this.addDependency(pancake);
        }
        int newCount = this.taskGroup().getNode(this.key()).dependencyKeys().size();
        System.out.println("Pasta(" + this.name() + ")::beforeGroupCreateOrUpdate() 'delayedSize':" + this.delayedPastas.size()
                + " 'dependency count [old, new]': [" + oldCount + "," + newCount + "]");
    }

    @Override
    public Mono<IPasta> createResourceAsync() {
        if (this.errorToThrow == null) {
            System.out.println("Pasta(" + this.name() + ")::createResourceAsync() 'onNext()'");
            return Mono.just(this)
                    .delayElement(Duration.ofMillis(this.eventDelayInMilliseconds))
                    .map(pasta -> pasta);
        } else {
            System.out.println("Pasta(" + this.name() + ")::createResourceAsync() 'onError()'");
            return Mono.just(this)
                    .delayElement(Duration.ofMillis(this.eventDelayInMilliseconds))
                    .flatMap(pasta -> toErrorObservable(errorToThrow));
        }
    }

    @Override
    public boolean isInCreateMode() {
        return true;
    }

    @Override
    protected Mono<PastaInner> getInnerAsync() {
        return Mono.just(this.inner());
    }

    private Mono<IPasta> toErrorObservable(Throwable throwable) {
        return Mono.error(throwable);
    }
}
