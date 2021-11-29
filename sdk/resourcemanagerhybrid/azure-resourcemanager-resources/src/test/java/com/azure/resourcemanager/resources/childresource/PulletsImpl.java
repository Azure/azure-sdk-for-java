// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.childresource;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class PulletsImpl extends ExternalChildResourcesCachedImpl<PulletImpl, Pullet, Object, ChickenImpl, Object> {
    PulletsImpl(ChickenImpl parent) {
        super(parent, null, "Pullet");
        cacheCollection();
    }

    public PulletImpl define(String name) {
        return super.prepareInlineDefine(name);
    }

    public PulletImpl update(String name) {
        return super.prepareInlineUpdate(name);
    }

    public void remove(String name) {
        super.prepareInlineRemove(name);
    }

    public void addPullet(PulletImpl pullet) {
        this.addChildResource(pullet);
    }

    @Override
    protected List<PulletImpl> listChildResources() {
        List<PulletImpl> resources = new ArrayList<>();
        resources.add(new PulletImpl("Tilly", this.getParent()));
        resources.add(new PulletImpl("Clover", this.getParent()));
        resources.add(new PulletImpl("Savvy", this.getParent()));
        resources.add(new PulletImpl("Pinky", this.getParent()));
        resources.add(new PulletImpl("Goldilocks", this.getParent()));
        return Collections.unmodifiableList(resources);
    }

    @Override
    protected Flux<PulletImpl> listChildResourcesAsync() {
        return Flux.fromIterable(listChildResources());
    }

    @Override
    protected PulletImpl newChildResource(String name) {
        return new PulletImpl(name, this.getParent());
    }
}
