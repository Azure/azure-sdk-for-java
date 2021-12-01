// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.Location;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluent.models.LocationInner;

/**
 * The implementation of {@link Location}.
 */
final class LocationImpl extends
        IndexableWrapperImpl<LocationInner>
        implements
        Location {
    LocationImpl(LocationInner innerModel) {
        super(innerModel);
    }

    @Override
    public String subscriptionId() {
        return this.innerModel().subscriptionId();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String displayName() {
        return this.innerModel().displayName();
    }

    @Override
    public String latitude() {
        return this.innerModel().latitude();
    }

    @Override
    public String longitude() {
        return this.innerModel().longitude();
    }

    @Override
    public Region region() {
        return Region.fromName(this.name());
    }
}
