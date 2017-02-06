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
 * Implementation for TrafficManagerProfiles.
 */
@LangDefinition
class TrafficManagerProfilesImpl extends GroupableResourcesImpl<
        TrafficManagerProfile,
        TrafficManagerProfileImpl,
        ProfileInner,
        ProfilesInner,
        TrafficManager>
        implements TrafficManagerProfiles {

    TrafficManagerProfilesImpl(final TrafficManager trafficManager) {
        super(trafficManager.inner().profiles(), trafficManager);
    }

    @Override
    public CheckProfileDnsNameAvailabilityResult checkDnsNameAvailability(String dnsNameLabel) {
        CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner parameter =
                new CheckTrafficManagerRelativeDnsNameAvailabilityParametersInner()
                    .withName(dnsNameLabel)
                    .withType("Microsoft.Network/trafficManagerProfiles");
        return new CheckProfileDnsNameAvailabilityResult(this
                .inner()
                .checkTrafficManagerRelativeDnsNameAvailability(parameter));
    }

    @Override
    public PagedList<TrafficManagerProfile> list() {
        return wrapList(this.inner().listAll());
    }

    @Override
    public PagedList<TrafficManagerProfile> listByGroup(String groupName) {
        return wrapList(this.inner().listAllInResourceGroup(groupName));
    }

    @Override
    public TrafficManagerProfile getByGroup(String groupName, String name) {
        return wrapModel(this.inner().get(groupName, name));
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(String name) {
        return new TrafficManagerProfileImpl(name,
                new ProfileInner(),
                this.inner(),
                this.manager().inner().endpoints(),
                this.manager());
    }

    @Override
    protected TrafficManagerProfileImpl wrapModel(ProfileInner inner) {
        return new TrafficManagerProfileImpl(inner.name(),
                inner,
                this.inner(),
                this.manager().inner().endpoints(),
                this.manager());
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
