/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
import rx.Observable;

/**
 * Implementation for {@link TrafficManagerProfiles}
 */
class TrafficManagerProfilesImpl extends GroupableResourcesImpl<
        TrafficManagerProfile,
        TrafficManagerProfileImpl,
        ProfileInner,
        ProfilesInner,
        TrafficManager>
        implements TrafficManagerProfiles {
    private final EndpointsInner endpointsClient;

    TrafficManagerProfilesImpl(
            final TrafficManagerManagementClientImpl trafficManagementClient,
            final TrafficManager trafficManager) {
        super(trafficManagementClient.profiles(), trafficManager);
        this.endpointsClient = trafficManagementClient.endpoints();
    }

    @Override
    public PagedList<TrafficManagerProfile> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<TrafficManagerProfile> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listAllInResourceGroup(groupName));
    }

    @Override
    public TrafficManagerProfile getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(String name) {
        return new TrafficManagerProfileImpl(name,
                new ProfileInner(),
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(ProfileInner inner) {
        return new TrafficManagerProfileImpl(inner.name(),
                inner,
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    public TrafficManagerProfileImpl define(String name) {
        return wrapModel(name);
    }
}
