/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.Operation;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

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
    private final CdnManagementClientImpl cdnManagementClient;

    CdnProfilesImpl(
            final CdnManagementClientImpl cdnManagementClient,
            final CdnManager cdnManager) {
        super(cdnManagementClient.profiles(), cdnManager);
        this.endpointsClient = cdnManagementClient.endpoints();
        this.cdnManagementClient = cdnManagementClient;
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
        return wrapModel(name);
    }

    @Override
    public String generateSsoUri(String resourceGroupName, String profileName) {
        return null;
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return new CheckNameAvailabilityResult(this.cdnManagementClient.checkNameAvailability(name));
    }

    @Override
    public List<Operation> listOperations() {
        return null;
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return null;
    }
}
