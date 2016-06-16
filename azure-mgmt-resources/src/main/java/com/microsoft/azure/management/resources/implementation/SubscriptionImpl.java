/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionPolicies;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.List;

/**
 * The implementation of Subscription and its nested interfaces.
 */
final class SubscriptionImpl extends
        IndexableWrapperImpl<SubscriptionInner>
        implements
        Subscription  {

    private final SubscriptionsInner client;

    SubscriptionImpl(SubscriptionInner innerModel, final SubscriptionsInner client) {
        super(innerModel.id(), innerModel);
        this.client = client;
    }

    @Override
    public String subscriptionId() {
        return this.inner().subscriptionId();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public String state() {
        return this.inner().state();
    }

    @Override
    public SubscriptionPolicies subscriptionPolicies() {
        return this.inner().subscriptionPolicies();
    }

    @Override
    public PagedList<Location> listLocations() throws IOException, CloudException {
        PagedListConverter<LocationInner, Location> converter = new PagedListConverter<LocationInner, Location>() {
            @Override
            public Location typeConvert(LocationInner locationInner) {
                return new LocationImpl(locationInner);
            }
        };
        return converter.convert(toPagedList(client.listLocations(this.subscriptionId()).getBody()));
    }

    private PagedList<LocationInner> toPagedList(List<LocationInner> list) {
        PageImpl<LocationInner> page = new PageImpl<>();
        page.setItems(list);
        page.setNextPageLink(null);
        return new PagedList<LocationInner>(page) {
            @Override
            public Page<LocationInner> nextPage(String nextPageLink) throws RestException, IOException {
                return null;
            }
        };
    }
}
