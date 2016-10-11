/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.trafficmanager.Profile;
import com.microsoft.azure.management.trafficmanager.Profiles;
import rx.Observable;

/**
 * Implementation for {@link Profiles}
 */
public class ProfilesImpl extends GroupableResourcesImpl<
        Profile,
        ProfileImpl,
        ProfileInner,
        ProfilesInner,
        TrafficManager>
        implements Profiles {
    private final EndpointsInner endpointsClient;

    ProfilesImpl(
            final TrafficManagerManagementClientImpl trafficManagementClient,
            final TrafficManager trafficManager) {
        super(trafficManagementClient.profiles(), trafficManager);
        this.endpointsClient = trafficManagementClient.endpoints();
    }

    @Override
    public PagedList<Profile> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<Profile> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listAllInResourceGroup(groupName));
    }

    @Override
    public Profile getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    protected ProfileImpl wrapModel(String name) {
        return new ProfileImpl(name,
                new ProfileInner(),
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    protected ProfileImpl wrapModel(ProfileInner inner) {
        return new ProfileImpl(inner.name(),
                inner,
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    public ProfileImpl define(String name) {
        return wrapModel(name);
    }
}
