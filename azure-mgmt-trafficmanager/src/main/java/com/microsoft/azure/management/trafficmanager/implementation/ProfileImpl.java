/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.trafficmanager.AzureEndpoint;
import com.microsoft.azure.management.trafficmanager.ExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.MonitorConfig;
import com.microsoft.azure.management.trafficmanager.NestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.Profile;
import com.microsoft.azure.management.trafficmanager.ProfileMonitorStatus;
import com.microsoft.azure.management.trafficmanager.TrafficRoutingMethod;
import rx.Observable;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;

/**
 * Implementation for {@link Profile}
 */
public class ProfileImpl
        extends GroupableResourceImpl<
        Profile,
        ProfileInner,
        ProfileImpl,
        TrafficManager>
        implements
        Profile,
        Profile.Definition,
        Profile.Update {
    private final ProfilesInner innerCollection;
    private final EndpointsInner endpointsClient;
    private final String profileStatusDisabled = "Disabled";
    private final String profileStatusEnabled = "Enabled";
    private EndpointsImpl endpoints;

    ProfileImpl(String name,
                final ProfileInner innerModel,
                final ProfilesInner innerCollection,
                final EndpointsInner endpointsClient,
                final TrafficManager trafficManager) {
        super(name, innerModel, trafficManager);
        this.innerCollection = innerCollection;
        this.endpointsClient = endpointsClient;
        this.endpoints = new EndpointsImpl(endpointsClient, this);
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
    public int ttl() {
        return this.inner().dnsConfig().ttl().intValue();
    }

    @Override
    public boolean isDisabled() {
        return this.inner().profileStatus().equalsIgnoreCase(this.profileStatusDisabled);
    }

    @Override
    public TrafficRoutingMethod trafficRoutingMethod() {
        return TrafficRoutingMethod.fromValue(this.inner().trafficRoutingMethod());
    }

    @Override
    public ProfileMonitorStatus monitorStatus() {
        return ProfileMonitorStatus.fromValue(this.inner().monitorConfig().profileMonitorStatus());
    }

    @Override
    public int monitoringPort() {
        return this.inner().monitorConfig().port().intValue();
    }

    @Override
    public String monitoringPath() {
        return this.inner().monitorConfig().path();
    }

    // TODO Expose monitoring protocol

    @Override
    public Map<String, ExternalEndpoint> externalEndpoints() {
        return this.endpoints.externalEndpointsAsMap();
    }

    @Override
    public Map<String, AzureEndpoint> azureEndpoints() {
        return this.endpoints.azureEndpointsAsMap();
    }

    @Override
    public Map<String, NestedProfileEndpoint> nestedProfileEndpoints() {
        return this.endpoints.nestedProfileEndpointsAsMap();
    }

    @Override
    public Profile refresh() {
        ProfileInner inner = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(inner);
        this.endpoints.refresh();
        return this;
    }

    @Override
    public ProfileImpl withDnsLabel(String dnsLabel) {
        this.inner().dnsConfig().withRelativeName(dnsLabel);
        return this;
    }

    @Override
    public ProfileImpl withPriorityRouting() {
        this.withTrafficRoutingMethod(TrafficRoutingMethod.PRIORITY);
        return this;
    }

    @Override
    public ProfileImpl withWeightedRouting() {
        this.withTrafficRoutingMethod(TrafficRoutingMethod.WEIGHTED);
        return this;
    }

    @Override
    public ProfileImpl withPerformanceRouting() {
        this.withTrafficRoutingMethod(TrafficRoutingMethod.PERFORMANCE);
        return this;
    }

    @Override
    public ProfileImpl withTrafficRoutingMethod(TrafficRoutingMethod routingMethod) {
        this.inner().withTrafficRoutingMethod(routingMethod.toString());
        return this;
    }

    @Override
    public EndpointImpl defineNewEndpoint(String name) {
        return this.endpoints.define(name);
    }

    @Override
    public EndpointImpl updateAzureEndpoint(String name) {
        return this.endpoints.updateAzureEndpoint(name);
    }

    @Override
    public EndpointImpl updateExternalEndpoint(String name) {
        return this.endpoints.updateExternalEndpoint(name);
    }

    @Override
    public EndpointImpl updateNestedProfileEndpoint(String name) {
        return this.endpoints.updateNestedProfileEndpoint(name);
    }

    @Override
    public ProfileImpl withHttpMonitoring() {
        return this.withHttpMonitoring(80, "/");
    }

    @Override
    public ProfileImpl withHttpsMonitoring() {
        return this.withHttpsMonitoring(443, "/");
    }

    @Override
    public ProfileImpl withHttpMonitoring(int port, String path) {
        this.inner().withMonitorConfig(new MonitorConfig()
        .withPort(new Long(port))
        .withPath(path)
        .withProtocol("http"));
        return this;
    }

    @Override
    public ProfileImpl withHttpsMonitoring(int port, String path) {
        this.inner().withMonitorConfig(new MonitorConfig()
                .withPort(new Long(port))
                .withPath(path)
                .withProtocol("https"));
        return this;
    }

    @Override
    public ProfileImpl withProfileStatusDisabled() {
        this.inner().withProfileStatus(this.profileStatusDisabled);
        return this;
    }

    @Override
    public ProfileImpl withProfileStatusEnabled() {
        this.inner().withProfileStatus(this.profileStatusEnabled);
        return this;
    }

    @Override
    public ProfileImpl withTtl(int ttlInSeconds) {
        this.inner().dnsConfig().withTtl(new Long(ttlInSeconds));
        return this;
    }

    @Override
    public Observable<Profile> createResourceAsync() {
        final ProfileImpl self = this;
        return innerCollection.createOrUpdateAsync(resourceGroupName(), name(), inner())
                .map(new Func1<ProfileInner, Profile>() {
                    @Override
                    public Profile call(ProfileInner profileInner) {
                        self.setInner(profileInner);
                        return self;
                    }
                }).flatMap(new Func1<Profile, Observable<? extends Profile>>() {
                    @Override
                    public Observable<? extends Profile> call(Profile profile) {
                        return self.endpoints.commitAndGetAllAsync()
                                .map(new Func1<List<EndpointImpl>, Profile>() {
                                    @Override
                                    public Profile call(List<EndpointImpl> endpoints) {
                                        return self;
                                    }
                                });
                    }
                });
    }

    ProfileImpl withEndpoint(EndpointImpl endpoint) {
        this.endpoints.addEndpoint(endpoint);
        return this;
    }
}
