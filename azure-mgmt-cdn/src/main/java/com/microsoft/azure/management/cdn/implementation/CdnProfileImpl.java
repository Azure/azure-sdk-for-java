/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
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
    /*private final PatchSchedulesInner patchSchedulesInner;
    private RedisAccessKeys cachedAccessKeys;
    private RedisCreateParametersInner createParameters;
    private RedisUpdateParametersInner updateParameters;
    private Map<DayOfWeek, ScheduleEntry> scheduleEntries;*/
    private final EndpointsInner endpointsClient;
    private ProfileUpdateParameters updateParameters;
    private final ProfilesInner innerCollection;
    private CdnEndpointsImpl endpoints;

    CdnProfileImpl(String name,
                   final ProfileInner innerModel,
                   final ProfilesInner innerCollection,
                   final EndpointsInner endpointsClient,
                   final CdnManager cdnManager) {
        super(name, innerModel, cdnManager);
        this.innerCollection = innerCollection;
        this.endpointsClient = endpointsClient;
        this.endpoints = new CdnEndpointsImpl(endpointsClient, this);
    }

    @Override
    public Map<String, CdnEndpoint> endpoints() {
        return this.endpoints.endpointsAsMap();
    }

    @Override
    public String generateSsoUri(String resourceGroupName, String profileName) {
        SsoUriInner ssoUri = this.innerCollection.generateSsoUri(resourceGroupName, profileName);
        if(ssoUri != null) {
            return ssoUri.ssoUriValue();
        }
        return null;
    }

    @Override
    public String regionName() {
        return this.inner().location();
    }

    @Override
    public String generateSsoUri() {
        return generateSsoUri(resourceGroupName(), name());
    }

    @Override
    public Sku sku() {
        return this.inner().sku();
    }

    @Override
    public String resourceState() {
        return null;
    }

    @Override
    public CdnProfileImpl refresh() {
        ProfileInner cdnProfileInner =
                this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(cdnProfileInner);
        return this;
    }

    @Override
    public CdnProfileImpl withSku(SkuName skuName) {
        this.inner().withSku(new Sku().withName(skuName));
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
                        return self.endpoints.commitAndGetAllAsync()
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
    public CdnEndpointImpl defineNewEndpoint(String name) {
        return null;
    }

    CdnProfileImpl withEndpoint(CdnEndpointImpl endpoint) {
        //this.endpoints.addEndpoint(endpoint);
        return this;
    }
}
