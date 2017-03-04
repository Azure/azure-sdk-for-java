/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.CustomDomainValidationResult;
import com.microsoft.azure.management.cdn.ResourceUsage;
import com.microsoft.azure.management.cdn.Sku;
import com.microsoft.azure.management.cdn.SkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for CdnProfile.
 */
@LangDefinition
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

    private CdnEndpointsImpl endpointsImpl;

    CdnProfileImpl(String name, final ProfileInner innerModel, final CdnManager cdnManager) {
        super(name, innerModel, cdnManager);
        this.endpointsImpl = new CdnEndpointsImpl(this);
    }

    @Override
    public Map<String, CdnEndpoint> endpoints() {
        return this.endpointsImpl.endpointsAsMap();
    }

    @Override
    public String generateSsoUri() {
        SsoUriInner ssoUri = this.manager().inner().profiles().generateSsoUri(
                this.resourceGroupName(),
                this.name());
        if (ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public void startEndpoint(String endpointName) {
        this.manager().inner().endpoints().start(this.resourceGroupName(), this.name(), endpointName);
    }

    @Override
    public void stopEndpoint(String endpointName) {
        this.manager().inner().endpoints().stop(this.resourceGroupName(), this.name(), endpointName);
    }

    @Override
    public PagedList<ResourceUsage> listResourceUsage() {
        return (new PagedListConverter<ResourceUsageInner, ResourceUsage>() {
            @Override
            public ResourceUsage typeConvert(ResourceUsageInner inner) {
                return new ResourceUsage(inner);
            }
        }).convert(this.manager().inner().profiles().listResourceUsage(
                this.resourceGroupName(),
                this.name()));
    }

    @Override
    public void purgeEndpointContent(String endpointName, List<String> contentPaths) {
        this.manager().inner().endpoints().purgeContent(this.resourceGroupName(), this.name(), endpointName, contentPaths);
    }

    @Override
    public void loadEndpointContent(String endpointName, List<String> contentPaths) {
        this.manager().inner().endpoints().loadContent(this.resourceGroupName(), this.name(), endpointName, contentPaths);
    }

    @Override
    public CustomDomainValidationResult validateEndpointCustomDomain(String endpointName, String hostName) {
        return new CustomDomainValidationResult(
                this.manager().inner().endpoints().validateCustomDomain(
                        this.resourceGroupName(),
                        this.name(),
                        endpointName,
                        hostName));
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return this.manager().profiles().checkEndpointNameAvailability(name);
    }

    @Override
    public boolean isPremiumVerizon() {
        if (this.sku() != null
                && this.sku().name() != null
                && this.sku().name().equals(SkuName.PREMIUM_VERIZON)) {
            return true;
        }
        return false;
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public String resourceState() {
        return this.inner().resourceState().toString();
    }

    @Override
    protected Observable<ProfileInner> getInnerAsync() {
        return this.manager().inner().profiles().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public Observable<CdnProfile> updateResourceAsync() {
        final CdnProfileImpl self = this;
        final ProfilesInner innerCollection = this.manager().inner().profiles();
        return self.endpointsImpl.commitAndGetAllAsync()
                .flatMap(new Func1<List<CdnEndpointImpl>, Observable<? extends CdnProfile>>() {
                    public Observable<? extends CdnProfile> call(List<CdnEndpointImpl> endpoints) {
                        return innerCollection.updateAsync(resourceGroupName(), name(), inner().getTags())
                                .map(new Func1<ProfileInner, CdnProfile>() {
                                    @Override
                                    public CdnProfile call(ProfileInner profileInner) {
                                        self.setInner(profileInner);
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public Observable<CdnProfile> createResourceAsync() {
        final CdnProfileImpl self = this;
        return this.manager().inner().profiles().createAsync(resourceGroupName(), name(), inner())
                .map(new Func1<ProfileInner, CdnProfile>() {
                    @Override
                    public CdnProfile call(ProfileInner profileInner) {
                        self.setInner(profileInner);
                        return self;
                    }
                }).flatMap(new Func1<CdnProfile, Observable<? extends CdnProfile>>() {
                    @Override
                    public Observable<? extends CdnProfile> call(CdnProfile profile) {
                        return self.endpointsImpl.commitAndGetAllAsync()
                                .map(new Func1<List<CdnEndpointImpl>, CdnProfile>() {
                                    @Override
                                    public CdnProfile call(List<CdnEndpointImpl> endpoints) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    @Override
    public CdnProfileImpl withStandardAkamaiSku() {
        this.inner()
                .withSku(new Sku()
                            .withName(SkuName.STANDARD_AKAMAI));
        return this;
    }

    @Override
    public CdnProfileImpl withStandardVerizonSku() {
        this.inner()
                .withSku(new Sku()
                        .withName(SkuName.STANDARD_VERIZON));
        return this;
    }

    @Override
    public CdnProfileImpl withPremiumVerizonSku() {
        this.inner()
                .withSku(new Sku()
                        .withName(SkuName.PREMIUM_VERIZON));
        return this;
    }

    @Override
    public CdnProfileImpl withNewEndpoint(String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.endpointsImpl.defineNewEndpointWithOriginHostname(endpointOriginHostname);
        this.endpointsImpl.addEndpoint(endpoint);
        return this;
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint() {
        return this.endpointsImpl.defineNewEndpoint();
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String name) {
        return this.endpointsImpl.defineNewEndpoint(name);
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String name, String endpointOriginHostname) {
        return this.endpointsImpl.defineNewEndpoint(name, endpointOriginHostname);
    }

    @Override
    public CdnProfileImpl withNewPremiumEndpoint(String endpointOriginHostname) {
        return this.withNewEndpoint(endpointOriginHostname);
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint() {
        return this.endpointsImpl.defineNewEndpoint();
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
        return this.endpointsImpl.updateEndpoint(name);
    }

    @Override
    public CdnEndpointImpl updatePremiumEndpoint(String name) {
        return this.endpointsImpl.updateEndpoint(name);
    }

    @Override
    public Update withoutEndpoint(String name) {
        this.endpointsImpl.remove(name);
        return this;
    }

    CdnProfileImpl withEndpoint(CdnEndpointImpl endpoint) {
        this.endpointsImpl.addEndpoint(endpoint);
        return this;
    }
}
