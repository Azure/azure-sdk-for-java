// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.childresource;

import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

class PulletImpl extends ExternalChildResourceImpl<Pullet, Object, ChickenImpl, Object>
        implements Pullet {
    Integer age;
    private FailFlag failFlag = FailFlag.None;

    PulletImpl(String name, ChickenImpl parent) {
        super(name, name, parent, new Object());
    }

    public PulletImpl withAge(Integer age) {
        this.age = age;
        return this;
    }

    public PulletImpl withFailFlag(FailFlag failFlag) {
        this.failFlag = failFlag;
        return this;
    }

    public ChickenImpl attach() {
        return this.parent().withPullet(this);
    }

    @Override
    public Mono<Pullet> createResourceAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnCreate) {
            return Mono.error(new Exception("Creation of " + this.name() + " failed"));
        }

        Pullet self = this;
        return Mono
                .just(self)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Pullet> updateResourceAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnUpdate) {
            return Mono.error(new Exception("Update of " + this.name() + " failed"));
        }

        Pullet self = this;
        return Mono
                .just(self)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnDelete) {
            return Mono.error(new Exception("Deletion of " + this.name() + " failed"));
        }

        return Mono.empty();
    }

    @Override
    protected Mono<Object> getInnerAsync() {
        return Mono.empty();
    }

    @Override
    public String id() {
        return null;
    }

    enum FailFlag {
        None,
        OnCreate,
        OnUpdate,
        OnDelete
    }
}
