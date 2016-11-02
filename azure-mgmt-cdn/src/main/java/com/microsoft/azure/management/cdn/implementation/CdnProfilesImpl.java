/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

import java.util.ArrayList;

/**
 * Implementation for {@link CdnProfiles}.
 */
class CdnProfilesImpl
        extends GroupableResourcesImpl<
            CdnProfile,
            CdnProfileImpl,
            ProfileInner,
            ProfilesInner,
            CdnManager>
        implements CdnProfiles {
    private final EndpointsInner endpointsClient;

    CdnProfilesImpl(
            final CdnManagementClientImpl cdnManagementClient,
            final CdnManager cdnManager) {
        super(cdnManagementClient.profiles(), cdnManager);
        this.endpointsClient = cdnManagementClient.endpoints();
    }

    @Override
    public PagedList<CdnProfile> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    public PagedList<CdnProfile> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listByResourceGroup(groupName));
    }

    @Override
    public CdnProfile getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    protected CdnProfileImpl wrapModel(String name) {
        return new CdnProfileImpl(name,
                new ProfileInner(),
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    protected CdnProfileImpl wrapModel(ProfileInner inner) {
        return new CdnProfileImpl(inner.name(),
                inner,
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    public CdnProfileImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    private CdnProfileImpl setDefaults(CdnProfileImpl profile) {
        // TM location must be 'global' irrespective of region of the resource group it resides.
        profile.inner().withLocation("global");
        return profile;
    }
}
