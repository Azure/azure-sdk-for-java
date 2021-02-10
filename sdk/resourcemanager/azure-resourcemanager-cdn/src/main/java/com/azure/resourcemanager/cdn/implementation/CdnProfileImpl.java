// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.cdn.CdnManager;
import com.azure.resourcemanager.cdn.fluent.models.ProfileInner;
import com.azure.resourcemanager.cdn.fluent.models.SsoUriInner;
import com.azure.resourcemanager.cdn.models.ResourceUsage;
import com.azure.resourcemanager.cdn.models.Sku;
import com.azure.resourcemanager.cdn.models.SkuName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.cdn.models.CdnEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.CheckNameAvailabilityResult;
import com.azure.resourcemanager.cdn.models.CustomDomainValidationResult;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for CdnProfile.
 */
class CdnProfileImpl
    extends GroupableResourceImpl<
        CdnProfile,
        ProfileInner,
        CdnProfileImpl,
        CdnManager>
    implements
        CdnProfile,
        CdnProfile.Definition,
        CdnProfile.Update {

    private CdnEndpointsImpl endpoints;

    CdnProfileImpl(String name, final ProfileInner innerModel, final CdnManager cdnManager) {
        super(name, innerModel, cdnManager);
        this.endpoints = new CdnEndpointsImpl(this);
    }

    @Override
    public Map<String, CdnEndpoint> endpoints() {
        return this.endpoints.endpointsAsMap();
    }

    @Override
    public String generateSsoUri() {
        return this.generateSsoUriAsync().block();
    }

    @Override
    public Mono<String> generateSsoUriAsync() {
        return this.manager().serviceClient().getProfiles().generateSsoUriAsync(this.resourceGroupName(), this.name())
            .map(SsoUriInner::ssoUriValue);
    }

    @Override
    public void startEndpoint(String endpointName) {
        this.startEndpointAsync(endpointName).block();
    }

    @Override
    public Mono<Void> startEndpointAsync(String endpointName) {
        return this.manager().serviceClient().getEndpoints()
            .startAsync(this.resourceGroupName(), this.name(), endpointName)
            .then();
    }

    @Override
    public void stopEndpoint(String endpointName) {
        this.stopEndpointAsync(endpointName).block();
    }

    @Override
    public Mono<Void> stopEndpointAsync(String endpointName) {
        return this.manager().serviceClient().getEndpoints()
            .stopAsync(this.resourceGroupName(), this.name(), endpointName)
            .then();
    }

    @Override
    public PagedIterable<ResourceUsage> listResourceUsage() {
        return PagedConverter.mapPage(this.manager().serviceClient().getProfiles().listResourceUsage(this.resourceGroupName(), this.name()),
            ResourceUsage::new);
    }

    @Override
    public void purgeEndpointContent(String endpointName, Set<String> contentPaths) {
        this.purgeEndpointContentAsync(endpointName, contentPaths).block();
    }

    @Override
    public Mono<Void> purgeEndpointContentAsync(String endpointName, Set<String> contentPaths) {
        if (contentPaths != null) {
            return this.manager().serviceClient().getEndpoints()
                .purgeContentAsync(this.resourceGroupName(), this.name(), endpointName, new ArrayList<>(contentPaths));
        }
        return Mono.empty();
    }

    @Override
    public void loadEndpointContent(String endpointName, Set<String> contentPaths) {
        this.loadEndpointContentAsync(endpointName, contentPaths).block();
    }

    @Override
    public Mono<Void> loadEndpointContentAsync(String endpointName, Set<String> contentPaths) {
        if (contentPaths != null) {
            return this.manager().serviceClient().getEndpoints()
                .loadContentAsync(this.resourceGroupName(), this.name(), endpointName, new ArrayList<>(contentPaths));
        }
        return Mono.empty();
    }

    @Override
    public CustomDomainValidationResult validateEndpointCustomDomain(String endpointName, String hostName) {
        return this.validateEndpointCustomDomainAsync(endpointName, hostName).block();
    }

    @Override
    public Mono<CustomDomainValidationResult> validateEndpointCustomDomainAsync(String endpointName, String hostName) {
        return this.manager().serviceClient().getEndpoints().validateCustomDomainAsync(
            this.resourceGroupName(), this.name(), endpointName, hostName)
            .map(CustomDomainValidationResult::new);
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return this.checkEndpointNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name) {
        return this.manager().profiles().checkEndpointNameAvailabilityAsync(name);
    }

    @Override
    public boolean isPremiumVerizon() {
        return this.sku() != null
            && this.sku().name() != null
            && this.sku().name().equals(SkuName.PREMIUM_VERIZON);
    }

    @Override
    public String regionName() {
        return this.innerModel().location();
    }

    @Override
    public Sku sku() {
        return this.innerModel().sku();
    }

    @Override
    public String resourceState() {
        return this.innerModel().resourceState().toString();
    }

    @Override
    protected Mono<ProfileInner> getInnerAsync() {
        return this.manager().serviceClient().getProfiles()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<CdnProfile> createResourceAsync() {
        return this.manager().serviceClient().getProfiles().createAsync(resourceGroupName(), name(), innerModel())
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<CdnProfile> updateResourceAsync() {
        final CdnProfileImpl self = this;
        return this.manager().serviceClient().getProfiles()
            .updateAsync(this.resourceGroupName(), this.name(), innerModel().tags())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> afterPostRunAsync(final boolean isGroupFaulted) {
        if (isGroupFaulted) {
            endpoints.clear();
            return Mono.empty();
        } else {
            return this.refreshAsync().then();
        }
    }

    @Override
    public Mono<CdnProfile> refreshAsync() {
        return super.refreshAsync()
            .map(cdnProfile -> {
                endpoints.clear();
                return cdnProfile;
            });
    }

    @Override
    public CdnProfileImpl withStandardAkamaiSku() {
        this.innerModel()
                .withSku(new Sku()
                            .withName(SkuName.STANDARD_AKAMAI));
        return this;
    }

    @Override
    public CdnProfileImpl withStandardVerizonSku() {
        this.innerModel()
                .withSku(new Sku()
                        .withName(SkuName.STANDARD_VERIZON));
        return this;
    }

    @Override
    public CdnProfileImpl withPremiumVerizonSku() {
        this.innerModel()
                .withSku(new Sku()
                        .withName(SkuName.PREMIUM_VERIZON));
        return this;
    }

    @Override
    public CdnProfileImpl withSku(SkuName skuName) {
        this.innerModel().withSku(new Sku().withName(skuName));
        return this;
    }

    @Override
    public CdnProfileImpl withNewEndpoint(String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.endpoints.defineNewEndpointWithOriginHostname(endpointOriginHostname);
        this.endpoints.addEndpoint(endpoint);
        return this;
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint() {
        return this.endpoints.defineNewEndpoint();
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String name) {
        return this.endpoints.defineNewEndpoint(name);
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String name, String endpointOriginHostname) {
        return this.endpoints.defineNewEndpoint(name, endpointOriginHostname);
    }

    @Override
    public CdnProfileImpl withNewPremiumEndpoint(String endpointOriginHostname) {
        return this.withNewEndpoint(endpointOriginHostname);
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint() {
        return this.endpoints.defineNewEndpoint();
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint(String name) {
        return this.defineNewEndpoint(name);
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint(String name, String endpointOriginHostname) {
        return this.defineNewEndpoint(name, endpointOriginHostname);
    }

    @Override
    public CdnEndpointImpl updateEndpoint(String name) {
        return this.endpoints.updateEndpoint(name);
    }

    @Override
    public CdnEndpointImpl updatePremiumEndpoint(String name) {
        return this.endpoints.updateEndpoint(name);
    }

    @Override
    public Update withoutEndpoint(String name) {
        this.endpoints.remove(name);
        return this;
    }

    CdnProfileImpl withEndpoint(CdnEndpointImpl endpoint) {
        this.endpoints.addEndpoint(endpoint);
        return this;
    }
}
