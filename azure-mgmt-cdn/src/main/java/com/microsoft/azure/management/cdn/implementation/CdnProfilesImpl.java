/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.Operation;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;

import java.util.List;

/**
 * Implementation for {@link CdnProfiles}.
 */
@LangDefinition
class CdnProfilesImpl
        extends GroupableResourcesImpl<
            CdnProfile,
            CdnProfileImpl,
            ProfileInner,
            ProfilesInner,
            CdnManager>
        implements CdnProfiles {
    private final EndpointsInner endpointsClient;
    private final OriginsInner originsClient;
    private final CustomDomainsInner customDomainsClient;
    private final CdnManagementClientImpl cdnManagementClient;

    CdnProfilesImpl(
            final CdnManagementClientImpl cdnManagementClient,
            final CdnManager cdnManager) {
        super(cdnManagementClient.profiles(), cdnManager);
        this.endpointsClient = cdnManagementClient.endpoints();
        this.originsClient = cdnManagementClient.origins();
        this.customDomainsClient = cdnManagementClient.customDomains();
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
                this.originsClient,
                this.customDomainsClient,
                this.myManager);
    }

    @Override
    protected CdnProfileImpl wrapModel(ProfileInner inner) {
        return new CdnProfileImpl(inner.name(),
                inner,
                this.innerCollection,
                this.endpointsClient,
                this.originsClient,
                this.customDomainsClient,
                this.myManager);
    }

    @Override
    public CdnProfileImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public String generateSsoUri(String resourceGroupName, String profileName) {
        SsoUriInner ssoUri = this.cdnManagementClient.profiles().generateSsoUri(resourceGroupName, profileName);
        if (ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return new CheckNameAvailabilityResult(this.cdnManagementClient.checkNameAvailability(name));
    }

    @Override
    public PagedList<Operation> listOperations() {
        return (new PagedListConverter<OperationInner, Operation>() {
            @Override
            public Operation typeConvert(OperationInner inner) {
                return new Operation(inner);
            }
        }).convert(this.cdnManagementClient.listOperations());
    }

    @Override
    public void startEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.cdnManagementClient.endpoints().start(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void stopEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.cdnManagementClient.endpoints().stop(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void purgeEndpointContent(String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.cdnManagementClient.endpoints().purgeContent(resourceGroupName, profileName, endpointName, contentPaths);
    }

    @Override
    public void loadEndpointContent(String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.cdnManagementClient.endpoints().loadContent(resourceGroupName, profileName, endpointName, contentPaths);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }
}
