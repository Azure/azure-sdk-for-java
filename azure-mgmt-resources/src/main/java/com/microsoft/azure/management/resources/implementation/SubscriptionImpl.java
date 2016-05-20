package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.implementation.api.LocationInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionInner;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionPolicies;

import java.io.IOException;

public class SubscriptionImpl extends
        IndexableWrapperImpl<SubscriptionInner>
        implements
        Subscription  {

    private final SubscriptionsInner client;

    public SubscriptionImpl(SubscriptionInner innerModel, final SubscriptionsInner client) {
        super(innerModel.id(), innerModel);
        this.client = client;
    }

    /***********************************************************
     * Getters
     ***********************************************************/

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

    /***********************************************************
     * Other operations
     ***********************************************************/

    @Override
    public PagedList<Location> listLocations() throws IOException, CloudException {
        PagedListConverter<LocationInner, Location> converter = new PagedListConverter<LocationInner, Location>() {
            @Override
            public Location typeConvert(LocationInner locationInner) {
                return new LocationImpl(locationInner, client);
            }
        };
        return converter.convert(client.listLocations(this.subscriptionId()).getBody());
    }
}
