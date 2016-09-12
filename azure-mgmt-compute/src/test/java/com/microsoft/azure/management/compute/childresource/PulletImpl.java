package com.microsoft.azure.management.compute.childresource;

import com.microsoft.azure.management.compute.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.schedulers.Schedulers;

class PulletImpl extends ExternalChildResourceImpl<Pullet, Object, ChickenImpl>
        implements Pullet {
    Integer age;
    private FailFlag failFlag = FailFlag.None;

    PulletImpl(String name, ChickenImpl parent) {
        super(name, parent, new Object());
    }

    public PulletImpl withAge(Integer age) {
        this.age = age;
        return this;
    }

    public PulletImpl withFailFlag(FailFlag failFlag) {
        this.failFlag = failFlag;
        return this;
    }

    public ChickenImpl parent() {
        return this.parent;
    }

    public ChickenImpl attach() {
        return this.parent.withPullet(this);
    }

    @Override
    public Observable<Pullet> createAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnCreate) {
            return Observable.error(new Exception("Creation of " + this.name() + " failed"));
        }

        Pullet self = this;
        return Observable
                .just(self)
                .observeOn(Schedulers.computation());
    }

    @Override
    public Observable<Pullet> updateAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnUpdate) {
            return Observable.error(new Exception("Update of " + this.name() + " failed"));
        }

        Pullet self = this;
        return Observable
                .just(self)
                .observeOn(Schedulers.computation());
    }

    @Override
    public Observable<Void> deleteAsync() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }

        if (this.failFlag == FailFlag.OnDelete) {
            return Observable.error(new Exception("Deletion of " + this.name() + " failed"));
        }

        return Observable
                .just(null);
    }

    @Override
    public PulletImpl refresh() throws Exception {
        return null;
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
