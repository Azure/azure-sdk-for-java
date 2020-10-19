// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.azure.resourcemanager.trafficmanager.fluent.ProfilesClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import com.azure.resourcemanager.trafficmanager.fluent.models.ProfileInner;
import com.azure.resourcemanager.trafficmanager.models.CheckProfileDnsNameAvailabilityResult;
import com.azure.resourcemanager.trafficmanager.models.CheckTrafficManagerRelativeDnsNameAvailabilityParameters;
import com.azure.resourcemanager.trafficmanager.models.DnsConfig;
import com.azure.resourcemanager.trafficmanager.models.GeographicHierarchies;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;
import com.azure.resourcemanager.trafficmanager.models.MonitorConfig;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfiles;
import java.util.ArrayList;
import reactor.core.publisher.Mono;

/** Implementation for TrafficManagerProfiles. */
public class TrafficManagerProfilesImpl
    extends TopLevelModifiableResourcesImpl<
        TrafficManagerProfile, TrafficManagerProfileImpl, ProfileInner, ProfilesClient, TrafficManager>
    implements TrafficManagerProfiles {
    private GeographicHierarchies geographicHierarchies;

    public TrafficManagerProfilesImpl(final TrafficManager trafficManager) {
        super(trafficManager.serviceClient().getProfiles(), trafficManager);
        this.geographicHierarchies =
            new GeographicHierarchiesImpl(trafficManager, trafficManager.serviceClient().getGeographicHierarchies());
    }

    @Override
    public CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel) {
        return this.checkDnsNameAvailabilityAsync(dnsNameLabel).block();
    }

    @Override
    public Mono<CheckProfileDnsNameAvailabilityResult> checkDnsNameAvailabilityAsync(String dnsNameLabel) {
        CheckTrafficManagerRelativeDnsNameAvailabilityParameters parameter =
            new CheckTrafficManagerRelativeDnsNameAvailabilityParameters()
                .withName(dnsNameLabel)
                .withType("Microsoft.Network/trafficManagerProfiles");
        return this
            .inner()
            .checkTrafficManagerRelativeDnsNameAvailabilityAsync(parameter)
            .map(CheckProfileDnsNameAvailabilityResult::new);
    }

    @Override
    public GeographicLocation getGeographicHierarchyRoot() {
        return this.geographicHierarchies.getRoot();
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(String name) {
        return new TrafficManagerProfileImpl(name, new ProfileInner(), this.manager());
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(ProfileInner inner) {
        if (inner == null) {
            return null;
        }
        return new TrafficManagerProfileImpl(inner.name(), inner, this.manager());
    }

    @Override
    public TrafficManagerProfileImpl define(String name) {
        return setDefaults(wrapModel(name));
    }

    private TrafficManagerProfileImpl setDefaults(TrafficManagerProfileImpl profile) {
        // MonitorConfig is required
        profile.innerModel().withMonitorConfig(new MonitorConfig());
        profile.withHttpMonitoring(); // Default to Http monitoring
        // DnsConfig is required
        profile.innerModel().withDnsConfig(new DnsConfig());
        profile.withTimeToLive(300);
        // TM location must be 'global' irrespective of region of the resource group it resides.
        profile.innerModel().withLocation("global");
        // Endpoints are external child resource still initializing it avoid null checks in the model impl.
        profile.innerModel().withEndpoints(new ArrayList<EndpointInner>());
        return profile;
    }
}
