// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.databasewatcher.implementation;

import com.azure.core.management.SystemData;
import com.azure.core.util.Context;
import com.azure.resourcemanager.databasewatcher.fluent.models.SharedPrivateLinkResourceInner;
import com.azure.resourcemanager.databasewatcher.models.SharedPrivateLinkResource;
import com.azure.resourcemanager.databasewatcher.models.SharedPrivateLinkResourceProperties;

public final class SharedPrivateLinkResourceImpl
    implements SharedPrivateLinkResource, SharedPrivateLinkResource.Definition {
    private SharedPrivateLinkResourceInner innerObject;

    private final com.azure.resourcemanager.databasewatcher.DatabaseWatcherManager serviceManager;

    SharedPrivateLinkResourceImpl(SharedPrivateLinkResourceInner innerObject,
        com.azure.resourcemanager.databasewatcher.DatabaseWatcherManager serviceManager) {
        this.innerObject = innerObject;
        this.serviceManager = serviceManager;
    }

    public String id() {
        return this.innerModel().id();
    }

    public String name() {
        return this.innerModel().name();
    }

    public String type() {
        return this.innerModel().type();
    }

    public SharedPrivateLinkResourceProperties properties() {
        return this.innerModel().properties();
    }

    public SystemData systemData() {
        return this.innerModel().systemData();
    }

    public SharedPrivateLinkResourceInner innerModel() {
        return this.innerObject;
    }

    private com.azure.resourcemanager.databasewatcher.DatabaseWatcherManager manager() {
        return this.serviceManager;
    }

    private String resourceGroupName;

    private String watcherName;

    private String sharedPrivateLinkResourceName;

    public SharedPrivateLinkResourceImpl withExistingWatcher(String resourceGroupName, String watcherName) {
        this.resourceGroupName = resourceGroupName;
        this.watcherName = watcherName;
        return this;
    }

    public SharedPrivateLinkResource create() {
        this.innerObject = serviceManager.serviceClient()
            .getSharedPrivateLinkResources()
            .create(resourceGroupName, watcherName, sharedPrivateLinkResourceName, this.innerModel(), Context.NONE);
        return this;
    }

    public SharedPrivateLinkResource create(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getSharedPrivateLinkResources()
            .create(resourceGroupName, watcherName, sharedPrivateLinkResourceName, this.innerModel(), context);
        return this;
    }

    SharedPrivateLinkResourceImpl(String name,
        com.azure.resourcemanager.databasewatcher.DatabaseWatcherManager serviceManager) {
        this.innerObject = new SharedPrivateLinkResourceInner();
        this.serviceManager = serviceManager;
        this.sharedPrivateLinkResourceName = name;
    }

    public SharedPrivateLinkResource refresh() {
        this.innerObject = serviceManager.serviceClient()
            .getSharedPrivateLinkResources()
            .getWithResponse(resourceGroupName, watcherName, sharedPrivateLinkResourceName, Context.NONE)
            .getValue();
        return this;
    }

    public SharedPrivateLinkResource refresh(Context context) {
        this.innerObject = serviceManager.serviceClient()
            .getSharedPrivateLinkResources()
            .getWithResponse(resourceGroupName, watcherName, sharedPrivateLinkResourceName, context)
            .getValue();
        return this;
    }

    public SharedPrivateLinkResourceImpl withProperties(SharedPrivateLinkResourceProperties properties) {
        this.innerModel().withProperties(properties);
        return this;
    }
}
