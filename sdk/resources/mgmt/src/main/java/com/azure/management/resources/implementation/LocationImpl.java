/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.Location;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.management.resources.models.LocationInner;

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
        return this.inner().getSubscriptionId();
    }

    @Override
    public String name() {
        return this.inner().getName();
    }

    @Override
    public String displayName() {
        return this.inner().getDisplayName();
    }

    @Override
    public String latitude() {
        return this.inner().getLatitude();
    }

    @Override
    public String longitude() {
        return this.inner().getLongitude();
    }

    @Override
    public Region region() {
        return Region.fromName(this.name());
    }
}
