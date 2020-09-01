/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.trafficmanager.models.MonitorProtocol;
import com.azure.resourcemanager.trafficmanager.models.ProfileStatus;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerAzureEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerNestedProfileEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.ProfileMonitorStatus;
import com.azure.resourcemanager.trafficmanager.models.TrafficRoutingMethod;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for TrafficManagerProfile.
 */
@LangDefinition
class TrafficManagerProfileImpl
        extends GroupableResourceImpl<
            TrafficManagerProfile,
            ProfileInner,
            TrafficManagerProfileImpl,
    TrafficManager>
        implements
            TrafficManagerProfile,
            TrafficManagerProfile.Definition,
            TrafficManagerProfile.Update {
    private TrafficManagerEndpointsImpl endpoints;

    TrafficManagerProfileImpl(String name, final ProfileInner innerModel, final TrafficManager trafficManager) {
        super(name, innerModel, trafficManager);
        this.endpoints = new TrafficManagerEndpointsImpl(trafficManager.inner().endpoints(), this);
        this.endpoints.enablePostRunMode();
    }

    @Override
    public String dnsLabel() {
        return this.inner().dnsConfig().relativeName();
    }

    @Override
    public String fqdn() {
        return this.inner().dnsConfig().fqdn();
    }

    @Override
    public long timeToLive() {
        return Utils.toPrimitiveLong(this.inner().dnsConfig().ttl());
    }

    @Override
    public boolean isEnabled() {
        return this.inner().profileStatus().equals(ProfileStatus.ENABLED);
    }

    @Override
    public TrafficRoutingMethod trafficRoutingMethod() {
        return this.inner().trafficRoutingMethod();
    }

    @Override
    public ProfileMonitorStatus monitorStatus() {
        return this.inner().monitorConfig().profileMonitorStatus();
    }

    @Override
    public long monitoringPort() {
        return Utils.toPrimitiveLong(this.inner().monitorConfig().port());
    }

    @Override
    public String monitoringPath() {
        return this.inner().monitorConfig().path();
    }

    // TODO Expose monitoring protocol

    @Override
    public Map<String, TrafficManagerExternalEndpoint> externalEndpoints() {
        return this.endpoints.externalEndpointsAsMap();
    }

    @Override
    public Map<String, TrafficManagerAzureEndpoint> azureEndpoints() {
        return this.endpoints.azureEndpointsAsMap();
    }

    @Override
    public Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints() {
        return this.endpoints.nestedProfileEndpointsAsMap();
    }

    @Override
    public Observable<TrafficManagerProfile> refreshAsync() {
        return super.refreshAsync().map(new Func1<TrafficManagerProfile, TrafficManagerProfile>() {
            @Override
            public TrafficManagerProfile call(TrafficManagerProfile trafficManagerProfile) {
                TrafficManagerProfileImpl impl = (TrafficManagerProfileImpl) trafficManagerProfile;
                impl.endpoints.refresh();
                return impl;
            }
        });
    }

    @Override
    protected Observable<ProfileInner> getInnerAsync() {
        return this.manager().inner().profiles().getByResourceGroupAsync(
                this.resourceGroupName(), this.name());
    }

    @Override
    public TrafficManagerProfileImpl withLeafDomainLabel(String dnsLabel) {
        this.inner().dnsConfig().withRelativeName(dnsLabel);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withPriorityBasedRouting() {
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY);
    }

    @Override
    public TrafficManagerProfileImpl withWeightBasedRouting() {
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.WEIGHTED);
    }

    @Override
    public TrafficManagerProfileImpl withPerformanceBasedRouting() {
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.PERFORMANCE);
    }

    @Override
    public TrafficManagerProfileImpl withGeographicBasedRouting() {
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.GEOGRAPHIC);
    }

    @Override
    public TrafficManagerProfileImpl withMultiValueBasedRouting(long maxResult) {
        this.inner().withMaxReturn(maxResult);
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.MULTI_VALUE);
    }

    @Override
    public TrafficManagerProfileImpl withSubnetBasedRouting() {
        return this.withTrafficRoutingMethod(TrafficRoutingMethod.SUBNET);
    }

    @Override
    public TrafficManagerProfileImpl withTrafficRoutingMethod(TrafficRoutingMethod routingMethod) {
        this.inner().withTrafficRoutingMethod(routingMethod);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl defineAzureTargetEndpoint(String name) {
        return this.endpoints.defineAzureTargetEndpoint(name);
    }

    @Override
    public TrafficManagerEndpointImpl defineExternalTargetEndpoint(String name) {
        return this.endpoints.defineExteralTargetEndpoint(name);
    }

    @Override
    public TrafficManagerEndpointImpl defineNestedTargetEndpoint(String name) {
        return this.endpoints.defineNestedProfileTargetEndpoint(name);
    }

    @Override
    public TrafficManagerEndpointImpl updateAzureTargetEndpoint(String name) {
        return this.endpoints.updateAzureEndpoint(name);
    }

    @Override
    public TrafficManagerEndpointImpl updateExternalTargetEndpoint(String name) {
        return this.endpoints.updateExternalEndpoint(name);
    }

    @Override
    public TrafficManagerEndpointImpl updateNestedProfileTargetEndpoint(String name) {
        return this.endpoints.updateNestedProfileEndpoint(name);
    }

    @Override
    public TrafficManagerProfileImpl withoutEndpoint(String name) {
        this.endpoints.remove(name);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withHttpMonitoring() {
        return this.withHttpMonitoring(80, "/");
    }

    @Override
    public TrafficManagerProfileImpl withHttpsMonitoring() {
        return this.withHttpsMonitoring(443, "/");
    }

    @Override
    public TrafficManagerProfileImpl withHttpMonitoring(int port, String path) {
        this.inner().monitorConfig()
            .withPort(new Long(port))
            .withPath(path)
            .withProtocol(MonitorProtocol.HTTP);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withHttpsMonitoring(int port, String path) {
        this.inner().monitorConfig()
            .withPort(new Long(port))
            .withPath(path)
            .withProtocol(MonitorProtocol.HTTPS);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withProfileStatusDisabled() {
        this.inner().withProfileStatus(ProfileStatus.DISABLED);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withProfileStatusEnabled() {
        this.inner().withProfileStatus(ProfileStatus.ENABLED);
        return this;
    }

    @Override
    public TrafficManagerProfileImpl withTimeToLive(int ttlInSeconds) {
        this.inner().dnsConfig().withTtl(new Long(ttlInSeconds));
        return this;
    }

    @Override
    public TrafficManagerProfileImpl update() {
        this.endpoints.enableCommitMode();
        return super.update();
    }

    @Override
    public Observable<TrafficManagerProfile> createResourceAsync() {
        return this.manager().inner().profiles().createOrUpdateAsync(
                resourceGroupName(), name(), inner())
                .map(innerToFluentMap(this));
    }

    @Override
    public Observable<TrafficManagerProfile> updateResourceAsync() {
        final TrafficManagerProfileImpl self = this;
        // In update we first commit the endpoints then update profile, the reason is through portal and direct API
        // call one can create endpoints without properties those are not applicable for the profile's current routing
        // method. We cannot update the routing method of the profile until existing endpoints contains the properties
        // required for the new routing method.
        final ProfilesInner innerCollection = this.manager().inner().profiles();
        return self.endpoints.commitAndGetAllAsync()
                .flatMap(new Func1<List<TrafficManagerEndpointImpl>, Observable<? extends TrafficManagerProfile>>() {
                    public Observable<? extends TrafficManagerProfile> call(List<TrafficManagerEndpointImpl> endpoints) {
                        inner().withEndpoints(self.endpoints.allEndpointsInners());
                        return innerCollection.createOrUpdateAsync(resourceGroupName(), name(), inner())
                            .map(new Func1<ProfileInner, TrafficManagerProfile>() {
                                    @Override
                                    public TrafficManagerProfile call(ProfileInner profileInner) {
                                self.setInner(profileInner);
                                return self;
                                }
                                });
                    }
                });
    }

    @Override
    public Completable afterPostRunAsync(final boolean isGroupFaulted) {
        this.endpoints.clear();
        if (isGroupFaulted) {
            return Completable.complete();
        } else {
            return this.refreshAsync().toCompletable();
        }
    }

    TrafficManagerProfileImpl withEndpoint(TrafficManagerEndpointImpl endpoint) {
        this.endpoints.addEndpoint(endpoint);
        return this;
    }
}
