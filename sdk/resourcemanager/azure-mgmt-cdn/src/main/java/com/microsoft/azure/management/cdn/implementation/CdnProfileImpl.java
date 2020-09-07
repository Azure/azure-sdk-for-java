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
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private CdnEndpointsImpl endpoints;

    CdnProfileImpl(String name, final ProfileInner innerModel, final CdnManager cdnManager) {
        super(name, innerModel, cdnManager);
        this.endpoints = new CdnEndpointsImpl(this);
    }

    @Override
    public Map<String, CdnEndpoint> endpoints() {
        // This is not right, The endpoints are not inline children of profile (hence they are
        // not cached) but CdnEndpointsImpl today is deriving from ExternalChildResourceCachedImpl
        // instead it should derive from ExternalChildResourceNonCachedImpl
        //
        return this.endpoints.endpointsAsMap();
    }

    @Override
    public String generateSsoUri() {
        return this.generateSsoUriAsync().toBlocking().last();
    }

    @Override
    public Observable<String> generateSsoUriAsync() {
        return this.manager().inner().profiles().generateSsoUriAsync(
                this.resourceGroupName(),
                this.name()).map(new Func1<SsoUriInner, String>() {
            @Override
            public String call(SsoUriInner ssoUriInner) {
                if (ssoUriInner != null) {
                    return ssoUriInner.ssoUriValue();
                }
                return null;
            }
        });
    }

    @Override
    public ServiceFuture<String> generateSsoUriAsync(ServiceCallback<String> callback) {
        return ServiceFuture.fromBody(this.generateSsoUriAsync(), callback);
    }

    @Override
    public void startEndpoint(String endpointName) {
        this.startEndpointAsync(endpointName).await();
    }

    @Override
    public Completable startEndpointAsync(String endpointName) {
        return this.manager().inner().endpoints().startAsync(this.resourceGroupName(), this.name(), endpointName).toCompletable();
    }

    @Override
    public ServiceFuture<Void> startEndpointAsync(String endpointName, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.startEndpointAsync(endpointName), callback);
    }

    @Override
    public void stopEndpoint(String endpointName) {
        this.stopEndpointAsync(endpointName).await();
    }

    @Override
    public Completable stopEndpointAsync(String endpointName) {
        return this.manager().inner().endpoints().stopAsync(this.resourceGroupName(), this.name(), endpointName).toCompletable();
    }

    @Override
    public ServiceFuture<Void> stopEndpointAsync(String endpointName, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.stopEndpointAsync(endpointName), callback);
    }

    @Override
    public PagedList<ResourceUsage> listResourceUsage() {
        return (new PagedListConverter<ResourceUsageInner, ResourceUsage>() {
            @Override
            public Observable<ResourceUsage> typeConvertAsync(ResourceUsageInner inner) {
                return Observable.just((ResourceUsage) new ResourceUsage(inner));
            }
        }).convert(this.manager().inner().profiles().listResourceUsage(
                this.resourceGroupName(),
                this.name()));
    }

    @Override
    public void purgeEndpointContent(String endpointName, Set<String> contentPaths) {
        this.purgeEndpointContentAsync(endpointName, contentPaths).await();
    }

    @Override
    public Completable purgeEndpointContentAsync(String endpointName, Set<String> contentPaths) {
        if (contentPaths != null) {
            return this.manager().inner().endpoints().purgeContentAsync(this.resourceGroupName(), this.name(), endpointName, new ArrayList<>(contentPaths)).toCompletable();
        } else {
            return Observable.empty().toCompletable();
        }
    }

    @Override
    public ServiceFuture<Void> purgeEndpointContentAsync(String endpointName, Set<String> contentPaths, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.purgeEndpointContentAsync(endpointName, contentPaths), callback);
    }

    @Override
    public void loadEndpointContent(String endpointName, Set<String> contentPaths) {
        this.loadEndpointContentAsync(endpointName, contentPaths).await();
    }

    @Override
    public Completable loadEndpointContentAsync(String endpointName, Set<String> contentPaths) {
        if (contentPaths != null) {
            return this.manager().inner().endpoints().loadContentAsync(this.resourceGroupName(), this.name(), endpointName, new ArrayList<>(contentPaths)).toCompletable();
        } else {
            return Observable.empty().toCompletable();
        }
    }

    @Override
    public ServiceFuture<Void> loadEndpointContentAsync(String endpointName, Set<String> contentPaths, ServiceCallback<Void> callback) {
        return ServiceFuture.fromBody(this.loadEndpointContentAsync(endpointName, contentPaths), callback);
    }

    @Override
    public CustomDomainValidationResult validateEndpointCustomDomain(String endpointName, String hostName) {
        return this.validateEndpointCustomDomainAsync(endpointName, hostName).toBlocking().last();
    }

    @Override
    public Observable<CustomDomainValidationResult> validateEndpointCustomDomainAsync(String endpointName, String hostName) {
        return this.manager().inner().endpoints().validateCustomDomainAsync(
                this.resourceGroupName(),
                this.name(),
                endpointName,
                hostName).map(new Func1<ValidateCustomDomainOutputInner, CustomDomainValidationResult>() {
            @Override
            public CustomDomainValidationResult call(ValidateCustomDomainOutputInner validateCustomDomainOutputInner) {
                return new CustomDomainValidationResult(validateCustomDomainOutputInner);
            }
        });
    }

    @Override
    public ServiceFuture<CustomDomainValidationResult> validateEndpointCustomDomainAsync(String endpointName, String hostName, ServiceCallback<CustomDomainValidationResult> callback) {
        return ServiceFuture.fromBody(this.validateEndpointCustomDomainAsync(endpointName, hostName), callback);
    }

    @Override
    public CheckNameAvailabilityResult checkEndpointNameAvailability(String name) {
        return this.checkEndpointNameAvailabilityAsync(name).toBlocking().last();
    }

    @Override
    public Observable<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name) {
        return this.manager().profiles().checkEndpointNameAvailabilityAsync(name);
    }

    @Override
    public ServiceFuture<CheckNameAvailabilityResult> checkEndpointNameAvailabilityAsync(String name, ServiceCallback<CheckNameAvailabilityResult> callback) {
        return ServiceFuture.fromBody(this.checkEndpointNameAvailabilityAsync(name), callback);
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
    public CdnProfileImpl update() {
        this.endpoints.enableCommitMode();
        return super.update();
    }


    @Override
    public Observable<CdnProfile> createResourceAsync() {
        return this.manager().inner().profiles().createAsync(resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Observable<CdnProfile> updateResourceAsync() {
        final CdnProfileImpl self = this;
        final ProfilesInner innerCollection = this.manager().inner().profiles();
        return self.endpoints.commitAndGetAllAsync()
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
    public Completable afterPostRunAsync(final boolean isGroupFaulted) {
        if (isGroupFaulted) {
            endpoints.clear();
            return Completable.complete();
        } else {
            return this.refreshAsync().toCompletable();
        }
    }

    @Override
    public Observable<CdnProfile> refreshAsync() {
        return super.refreshAsync().map(new Func1<CdnProfile, CdnProfile>() {
            @Override
            public CdnProfile call(CdnProfile profile) {
                endpoints.refresh();
                return profile;
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
