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
import com.microsoft.azure.management.cdn.EdgeNode;
import com.microsoft.azure.management.cdn.Operation;
import com.microsoft.azure.management.cdn.ResourceUsage;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;

/**
 * Implementation for {@link CdnProfiles}.
 */
@LangDefinition
class CdnProfilesImpl
        extends TopLevelModifiableResourcesImpl<
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
    protected CdnProfileImpl wrapModel(String name) {
        return new CdnProfileImpl(name, new ProfileInner(), this.manager());
    }

    @Override
    protected CdnProfileImpl wrapModel(ProfileInner inner) {
        if (inner ==  null) {
            return null;
        }
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
        return this.checkEndpointNameAvailabilityAsync(name).toBlocking().last();
    }

    @Override
    public Observable<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name) {
        return this.manager().inner().checkNameAvailabilityAsync(name).map(new Func1<CheckNameAvailabilityOutputInner, CheckNameAvailabilityResult>() {
            @Override
            public CheckNameAvailabilityResult call(CheckNameAvailabilityOutputInner checkNameAvailabilityOutputInner) {
                return new CheckNameAvailabilityResult(checkNameAvailabilityOutputInner);
            }
        });
    }

    @Override
    public ServiceFuture<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback) {
        return ServiceFuture.fromBody(this.checkEndpointNameAvailabilityAsync(name), callback);
    }

    @Override
    public PagedList<Operation> listOperations() {
        return (new PagedListConverter<OperationInner, Operation>() {
            @Override
            public Observable<Operation> typeConvertAsync(OperationInner inner) {
                return Observable.just((Operation) new Operation(inner));
            }
        }).convert(this.manager().inner().operations().list());
    }

    @Override
    public PagedList<ResourceUsage> listResourceUsage() {
        return (new PagedListConverter<ResourceUsageInner, ResourceUsage>() {
            @Override
            public Observable<ResourceUsage> typeConvertAsync(ResourceUsageInner inner) {
                return Observable.just((ResourceUsage) new ResourceUsage(inner));
            }
        }).convert(this.manager().inner().resourceUsages().list());
    }

    @Override
    public PagedList<EdgeNode> listEdgeNodes() {
        return (new PagedListConverter<EdgeNodeInner, EdgeNode>() {
            @Override
            public Observable<EdgeNode> typeConvertAsync(EdgeNodeInner inner) {
                return Observable.just((EdgeNode) new EdgeNode(inner));
            }
        }).convert(this.manager().inner().edgeNodes().list());
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
}
