// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.models.Location;
import com.azure.resourcemanager.resources.models.Subscription;
import com.azure.resourcemanager.resources.models.SubscriptionPolicies;
import com.azure.resourcemanager.resources.models.SubscriptionState;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.azure.resourcemanager.resources.fluent.models.SubscriptionInner;
import com.azure.resourcemanager.resources.fluent.SubscriptionsClient;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * The implementation of {@link Subscription}.
 */
final class SubscriptionImpl extends
        IndexableWrapperImpl<SubscriptionInner>
        implements
        Subscription  {

    private final SubscriptionsClient client;

    SubscriptionImpl(SubscriptionInner innerModel, final SubscriptionsClient client) {
        super(innerModel);
        this.client = client;
    }

    @Override
    public String subscriptionId() {
        return this.innerModel().subscriptionId();
    }

    @Override
    public String displayName() {
        return this.innerModel().displayName();
    }

    @Override
    public SubscriptionState state() {
        return this.innerModel().state();
    }

    @Override
    public SubscriptionPolicies subscriptionPolicies() {
        return this.innerModel().subscriptionPolicies();
    }

    @Override
    public PagedIterable<Location> listLocations() {
        return PagedConverter.mapPage(client.listLocations(this.subscriptionId()), LocationImpl::new);
    }

    @Override
    public Location getLocationByRegion(Region region) {
        if (region != null) {
            PagedIterable<Location> locations = listLocations();
            for (Location location : locations) {
                if (region.equals(location.region())) {
                    return location;
                }
            }
        }
        return null;
    }
}
