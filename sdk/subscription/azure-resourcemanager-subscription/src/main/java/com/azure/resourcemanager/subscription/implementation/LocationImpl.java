// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.subscription.implementation;

import com.azure.resourcemanager.subscription.fluent.models.LocationInner;
import com.azure.resourcemanager.subscription.models.Location;

public final class LocationImpl implements Location {
    private LocationInner innerObject;

    private final com.azure.resourcemanager.subscription.SubscriptionManager serviceManager;

    LocationImpl(LocationInner innerObject, com.azure.resourcemanager.subscription.SubscriptionManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String id() {
        return this.innerModel().id();
    }

    public String subscriptionId() {
        return this.innerModel().subscriptionId();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String displayName() {
        return this.innerModel().displayName();
    }

    public String latitude() {
        return this.innerModel().latitude();
    }

    public String longitude() {
        return this.innerModel().longitude();
    }

    public LocationInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.subscription.SubscriptionManager manager() {
        return this.serviceManager;
    }
}
