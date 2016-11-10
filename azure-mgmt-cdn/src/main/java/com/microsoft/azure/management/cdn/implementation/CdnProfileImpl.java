/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.CustomDomainValidationResult;
import com.microsoft.azure.management.cdn.ProfileUpdateParameters;
import com.microsoft.azure.management.cdn.Sku;
import com.microsoft.azure.management.cdn.SkuName;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for Redis Cache and its parent interfaces.
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

    private final EndpointsInner endpointsClient;
    private ProfileUpdateParameters updateParameters;
    private final ProfilesInner innerCollection;
    private CdnEndpointsImpl endpointsImpl;

    CdnProfileImpl(String name,
                   final ProfileInner innerModel,
                   final ProfilesInner innerCollection,
                   final EndpointsInner endpointsClient,
                   final CdnManager cdnManager) {
        super(name, innerModel, cdnManager);
        this.innerCollection = innerCollection;
        this.endpointsClient = endpointsClient;
        this.endpointsImpl = new CdnEndpointsImpl(endpointsClient, this);
    }

    @Override
    public Map<String, CdnEndpoint> endpoints() {
        return this.endpointsImpl.endpointsAsMap();
    }

    @Override
    public String generateSsoUri() {
        SsoUriInner ssoUri = this.innerCollection.generateSsoUri(
                this.resourceGroupName(),
                this.name());
        if(ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public CdnEndpoint endpointStart(String endpointName) {
        return null;
    }

    @Override
    public CdnEndpoint endpointStop(String endpointName) {
        return null;
    }

    @Override
    public void endpointPurgeContent(String endpointName, List<String> contentPaths) {

    }

    @Override
    public void endpointLoadContent(String endpointName, List<String> contentPaths) {

    }

    @Override
    public CustomDomainValidationResult endpointValidateCustomDomain(String endpointName, String hostName) {
        return null;
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return this.myManager.profiles().checkEndpointNameAvailability(name);
    }

    @Override
    public boolean isPremiumVerizon() {
        if( this.sku() != null &&
                this.sku().name() != null &&
                this.sku().name().equals(SkuName.PREMIUM_VERIZON)) {
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
    public CdnProfileImpl refresh() {
        ProfileInner cdnProfileInner =
                this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(cdnProfileInner);
        return this;
    }

    @Override
    public CdnProfileImpl update() {
        return super.update();
    }

    @Override
    public Observable<CdnProfile> updateResourceAsync() {
        return innerCollection.updateAsync(resourceGroupName(), name(), updateParameters.tags())
                .map(innerToFluentMap(this))
                .doOnNext(new Action1<CdnProfile>() {
                    @Override
                    public void call(CdnProfile profile) {
                        /*updatePatchSchedules();*/
                    }
                });
    }

    @Override
    public Observable<CdnProfile> createResourceAsync() {
        final CdnProfileImpl self = this;
        return innerCollection.createAsync(resourceGroupName(), name(), inner())
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
    public CdnProfileImpl withNewEndpoint(String endpointHostname, String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.endpointsImpl.defineNewEndpointWithOrigin(endpointHostname, endpointOriginHostname);
        this.endpointsImpl.addEndpoint(endpoint);
        return this;
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String endpointHostname) {
        return this.endpointsImpl.defineNewEndpoint(endpointHostname);
    }

    @Override
    public CdnEndpointImpl defineNewEndpoint(String name, String endpointHostname) {
        return this.endpointsImpl.defineNewEndpoint(name, endpointHostname);
    }

    @Override
    public CdnProfileImpl withNewPremiumEndpoint(String endpointHostname, String endpointOriginHostname) {
        return this.withNewEndpoint(endpointHostname, endpointOriginHostname);
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint(String endpointHostname) {
        return this.defineNewEndpoint(endpointHostname);
    }

    @Override
    public CdnEndpointImpl defineNewPremiumEndpoint(String name, String endpointHostname) {
        return this.defineNewEndpoint(name, endpointHostname);
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
