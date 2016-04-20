package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;
import com.microsoft.azure.management.resources.implementation.api.SubscriptionsInner;
import com.microsoft.azure.management.resources.Location;
import com.microsoft.azure.management.resources.implementation.api.LocationInner;

public class LocationImpl extends
        IndexableWrapperImpl<LocationInner>
        implements
        Location {

    private final SubscriptionsInner client;

    public LocationImpl(LocationInner location, SubscriptionsInner client) {
        super(location.id(), location);
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
    public String name() {
        return this.inner().name();
    }

    @Override
    public String displayName() {
        return this.inner().displayName();
    }

    @Override
    public String latitude() {
        return this.inner().latitude();
    }

    @Override
    public String longitude() {
        return this.inner().longitude();
    }
}
