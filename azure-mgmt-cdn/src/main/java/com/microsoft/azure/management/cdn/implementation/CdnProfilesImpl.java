/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CdnProfiles;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.Operation;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ListableGroupableResourcesPageImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;
import rx.Observable;

import java.util.List;

/**
 * Implementation for {@link CdnProfiles}.
 */
@LangDefinition
class CdnProfilesImpl
        extends ListableGroupableResourcesPageImpl<
            CdnProfile,
            CdnProfileImpl,
            ProfileInner,
            ProfilesInner,
            CdnManager>
        implements CdnProfiles {

    CdnProfilesImpl(final CdnManager cdnManager) {
        super(cdnManager.inner().profiles(), cdnManager);
    }

    @Override
    public PagedList<CdnProfile> list() {
        return wrapList(this.inner().list());
    }

    @Override
    public PagedList<CdnProfile> listByGroup(String groupName) {
        return wrapList(this.inner().listByResourceGroup(groupName));
    }

    @Override
    public CdnProfile getByGroup(String groupName, String name) {
        return wrapModel(this.inner().get(groupName, name));
    }

    @Override
    protected CdnProfileImpl wrapModel(String name) {
        return new CdnProfileImpl(name, new ProfileInner(), this.manager());
    }

    @Override
    protected CdnProfileImpl wrapModel(ProfileInner inner) {
        return new CdnProfileImpl(inner.name(), inner, this.manager());
    }

    @Override
    public CdnProfileImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public String generateSsoUri(String resourceGroupName, String profileName) {
        SsoUriInner ssoUri = this.manager().inner().profiles().generateSsoUri(resourceGroupName, profileName);
        if (ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return new CheckNameAvailabilityResult(this.manager().inner().checkNameAvailability(name));
    }

    @Override
    public PagedList<Operation> listOperations() {
        return (new PagedListConverter<OperationInner, Operation>() {
            @Override
            public Operation typeConvert(OperationInner inner) {
                return new Operation(inner);
            }
        }).convert(this.manager().inner().listOperations());
    }

    @Override
    public void startEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.manager().inner().endpoints().start(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void stopEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.manager().inner().endpoints().stop(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void purgeEndpointContent(String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.manager().inner().endpoints().purgeContent(resourceGroupName, profileName, endpointName, contentPaths);
    }

    @Override
    public void loadEndpointContent(String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.manager().inner().endpoints().loadContent(resourceGroupName, profileName, endpointName, contentPaths);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected Observable<Page<ProfileInner>> listAsyncPage() {
        return inner().listAsync();
    }

    @Override
    protected Observable<Page<ProfileInner>> listByGroupAsyncPage(String resourceGroupName) {
        return inner().listByResourceGroupAsync(resourceGroupName);
    }
}
