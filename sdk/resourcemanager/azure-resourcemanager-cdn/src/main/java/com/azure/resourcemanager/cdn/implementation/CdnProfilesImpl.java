// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.CdnManager;
import com.azure.resourcemanager.cdn.fluent.ProfilesClient;
import com.azure.resourcemanager.cdn.fluent.models.ProfileInner;
import com.azure.resourcemanager.cdn.fluent.models.SsoUriInner;
import com.azure.resourcemanager.cdn.models.ResourceUsage;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CdnProfiles;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.EdgeNode;
import com.azure.resourcemanager.cdn.models.Operation;
import reactor.core.publisher.Mono;

import java.util.List;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for {@link CdnProfiles}.
 */
public final class CdnProfilesImpl
    extends TopLevelModifiableResourcesImpl<
        CdnProfile,
        CdnProfileImpl,
        ProfileInner,
        ProfilesClient,
        CdnManager>
    implements CdnProfiles {

    public CdnProfilesImpl(final CdnManager cdnManager) {
        super(cdnManager.serviceClient().getProfiles(), cdnManager);
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
        SsoUriInner ssoUri = this.manager().serviceClient().getProfiles()
            .generateSsoUri(resourceGroupName, profileName);
        if (ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return this.checkEndpointNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name) {
        return this.manager().serviceClient().checkNameAvailabilityAsync(name)
            .map(CheckNameAvailabilityResult::new);
    }

    @Override
    public PagedIterable<Operation> listOperations() {
        return PagedConverter.mapPage(this.manager().serviceClient().getOperations().list(),
            Operation::new);
    }

    @Override
    public PagedIterable<ResourceUsage> listResourceUsage() {
        return PagedConverter.mapPage(this.manager().serviceClient().getResourceUsages().list(),
            ResourceUsage::new);
    }

    @Override
    public PagedIterable<EdgeNode> listEdgeNodes() {
        return PagedConverter.mapPage(this.manager().serviceClient().getEdgeNodes().list(),
            EdgeNode::new);
    }

    @Override
    public void startEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.manager().serviceClient().getEndpoints().start(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void stopEndpoint(String resourceGroupName, String profileName, String endpointName) {
        this.manager().serviceClient().getEndpoints().stop(resourceGroupName, profileName, endpointName);
    }

    @Override
    public void purgeEndpointContent(
        String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.manager().serviceClient().getEndpoints()
            .purgeContent(resourceGroupName, profileName, endpointName, contentPaths);
    }

    @Override
    public void loadEndpointContent(
        String resourceGroupName, String profileName, String endpointName, List<String> contentPaths) {
        this.manager().serviceClient().getEndpoints()
            .loadContent(resourceGroupName, profileName, endpointName, contentPaths);
    }
}
