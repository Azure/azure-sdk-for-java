/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.trafficmanager.CheckProfileDnsNameAvailabilityResult;
import com.microsoft.azure.management.trafficmanager.DnsConfig;
import com.microsoft.azure.management.trafficmanager.MonitorConfig;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
import rx.Completable;

import java.util.ArrayList;

/**
 * Implementation for {@link TrafficManagerProfiles}.
 */
@LangDefinition
class TrafficManagerProfilesImpl extends GroupableResourcesImpl<
        TrafficManagerProfile,
        TrafficManagerProfileImpl,
        ProfileInner,
        ProfilesInner,
        TrafficManager>
        implements TrafficManagerProfiles {
    private final EndpointsInner endpointsClient;

    TrafficManagerProfilesImpl(
            final TrafficManagerManagementClientImpl trafficManagementClient,
            final TrafficManager trafficManager) {
        super(trafficManagementClient.profiles(), trafficManager);
        this.endpointsClient = trafficManagementClient.endpoints();
    }

    @Override
    public CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel) {
        CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner parameter =
                new CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner()
                    .withName(dnsNameLabel)
                    .withType("Microsoft.Network/trafficManagerProfiles");
        return new CheckProfileDnsNameAvailabilityResult(this
                .innerCollection
                .checkTrafficManagerRelativeDnsNameAvailability(parameter));
    }

    @Override
    public PagedList<TrafficManagerProfile> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<TrafficManagerProfile> listByGroup(String groupName) {
        return wrapList(this.innerCollection.listAllInResourceGroup(groupName));
    }

    @Override
    public TrafficManagerProfile getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(String name) {
        return new TrafficManagerProfileImpl(name,
                new ProfileInner(),
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(ProfileInner inner) {
        return new TrafficManagerProfileImpl(inner.name(),
                inner,
                this.innerCollection,
                this.endpointsClient,
                this.myManager);
    }

    @Override
    public TrafficManagerProfileImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    private TrafficManagerProfileImpl setDefaults(TrafficManagerProfileImpl profile) {
        // MonitorConfig is required
        profile.inner().withMonitorConfig(new MonitorConfig());
        profile.withHttpMonitoring(); // Default to Http monitoring
        // DnsConfig is required
        profile.inner().withDnsConfig(new DnsConfig());
        profile.withTimeToLive(300);
        // TM location must be 'global' irrespective of region of the resource group it resides.
        profile.inner().withLocation("global");
        // Endpoints are external child resource still initializing it avoid null checks in the model impl.
        profile.inner().withEndpoints(new ArrayList<EndpointInner>());
        return profile;
    }
}
