/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.childresource;

import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import java.util.ArrayList;
import java.util.List;

class PulletsImpl extends ExternalChildResourcesCachedImpl<PulletImpl, Pullet, Object, ChickenImpl, Object> {
    PulletsImpl(ChickenImpl parent) {
        super(parent, "Pullet");
        cacheCollection();
    }

    public PulletImpl define(String name) {
        return super.prepareDefine(name);
    }

    public PulletImpl update(String name) {
        return super.prepareUpdate(name);
    }

    public void remove(String name) {
        super.prepareRemove(name);
    }

    public void addPullet(PulletImpl pullet) {
        this.addChildResource(pullet);
    }

    @Override
    protected List<PulletImpl> listChildResources() {
        List<PulletImpl> resources = new ArrayList<>();
        resources.add(new PulletImpl("Tilly", this.parent()));
        resources.add(new PulletImpl("Clover", this.parent()));
        resources.add(new PulletImpl("Savvy", this.parent()));
        resources.add(new PulletImpl("Pinky", this.parent()));
        resources.add(new PulletImpl("Goldilocks", this.parent()));
        return resources;
    }

    @Override
    protected PulletImpl newChildResource(String name) {
        return new PulletImpl(name, this.parent());
    }
}
