/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.microsoft.azure.management.trafficmanager.CheckProfileDnsNameAvailabilityResult;
import com.microsoft.azure.management.trafficmanager.DnsConfig;
import com.microsoft.azure.management.trafficmanager.GeographicHierarchies;
import com.microsoft.azure.management.trafficmanager.GeographicLocation;
import com.microsoft.azure.management.trafficmanager.MonitorConfig;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * Implementation for TrafficManagerProfiles.
 */
@LangDefinition
class TrafficManagerProfilesImpl
    extends TopLevelModifiableResourcesImpl<
        TrafficManagerProfile,
        TrafficManagerProfileImpl,
        ProfileInner,
        ProfilesInner,
        TrafficManager>
    implements TrafficManagerProfiles {
    private GeographicHierarchies geographicHierarchies;

    TrafficManagerProfilesImpl(final TrafficManager trafficManager) {
        super(trafficManager.inner().profiles(), trafficManager);
        this.geographicHierarchies = new GeographicHierarchiesImpl(trafficManager, trafficManager.inner().geographicHierarchies());
    }

    @Override
    public CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel) {
        return this.checkDnsNameAvailabilityAsync(dnsNameLabel).toBlocking().last();
    }

    @Override
    public Observable<CheckProfileDnsNameAvailabilityResult> checkDnsNameAvailabilityAsync(String dnsNameLabel) {
        CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner parameter =
                new CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner()
                        .withName(dnsNameLabel)
                        .withType("Microsoft.Network/trafficManagerProfiles");
        return this.inner()
                .checkTrafficManagerRelativeDnsNameAvailabilityAsync(parameter).map(new Func1<TrafficManagerNameAvailabilityInner, CheckProfileDnsNameAvailabilityResult>() {
                    @Override
                    public CheckProfileDnsNameAvailabilityResult call(TrafficManagerNameAvailabilityInner trafficManagerNameAvailabilityInner) {
                        return new CheckProfileDnsNameAvailabilityResult(trafficManagerNameAvailabilityInner);
                    }
                });
    }

    @Override
    public ServiceFuture<CheckProfileDnsNameAvailabilityResult> checkDnsNameAvailabilityAsync(String dnsNameLabel, ServiceCallback<CheckProfileDnsNameAvailabilityResult> callback) {
        return ServiceFuture.fromBody(this.checkDnsNameAvailabilityAsync(dnsNameLabel), callback);
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
        return new TrafficManagerProfileImpl(inner.name(), inner, this.manager());
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
