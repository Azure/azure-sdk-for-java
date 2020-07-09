// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluent.inner.LocationInner;
import com.azure.resourcemanager.resources.models.RegionCategory;
import com.azure.resourcemanager.resources.models.RegionType;

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
        return this.inner().subscriptionId();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public String latitude() {
        return this.inner().metadata() == null ? null : this.inner().metadata().latitude();
    }

    @Override
    public String longitude() {
        return this.inner().metadata() == null ? null : this.inner().metadata().longitude();
    }

    @Override
    public RegionType regionType() {
        return this.inner().metadata() == null ? null : this.inner().metadata().regionType();
    }

    @Override
    public RegionCategory regionCategory() {
        return this.inner().metadata() == null ? null : this.inner().metadata().regionCategory();
    }

    @Override
    public String geographyGroup() {
        return this.inner().metadata() == null ? null : this.inner().metadata().geographyGroup();
    }

    @Override
    public String physicalLocation() {
        return this.inner().metadata() == null ? null : this.inner().metadata().physicalLocation();
    }

    @Override
    public Region region() {
        return Region.fromName(this.name());
    }
}
