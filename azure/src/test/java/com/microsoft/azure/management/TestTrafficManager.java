package com.microsoft.azure.management;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.trafficmanager.TrafficManagerAzureEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;

import java.util.Map;

/**
 * Test of virtual network management.
 */
public class TestTrafficManager extends TestTemplate<TrafficManagerProfile, TrafficManagerProfiles> {

    @Override
    public TrafficManagerProfile createResource(TrafficManagerProfiles profiles) throws Exception {
        Region region = Region.US_EAST;
        String groupName = "rg" + this.testId;
        final String tmProfileName = "tm" + this.testId;
        final String tmProfileDnsLabel = ResourceNamer.randomResourceName("tmdns", 20);

        TrafficManagerProfile profile = profiles.define(tmProfileName)
                .withNewResourceGroup(groupName, region)
                .withDnsLabel(tmProfileDnsLabel)
                .withPriorityBasedRouting()
                .withHttpMonitoring()
                .defineEndpoint("ep1")
                    .withExternalFqdn("www.google.com")
                    .withSourceTrafficLocation(Region.ASIA_EAST)
                    .withRoutingPriority(1)
                    .withRoutingWeight(1)
                    .attach()
                .create();
        return profile;
    }

    @Override
    public TrafficManagerProfile updateResource(TrafficManagerProfile profile) throws Exception {
        return profile;
    }

    @Override
    public void print(TrafficManagerProfile profile) {
        StringBuilder info = new StringBuilder();
        info.append("Traffic Manager Profile: ").append(profile.id())
                .append("\n\tName: ").append(profile.name())
                .append("\n\tResource group: ").append(profile.resourceGroupName())
                .append("\n\tRegion: ").append(profile.regionName())
                .append("\n\tTags: ").append(profile.tags())
                .append("\n\tDNSLabel: ").append(profile.dnsLabel())
                .append("\n\tFQDN: ").append(profile.fqdn())
                .append("\n\tTTL: ").append(profile.ttl())
                .append("\n\tDisabled: ").append(profile.isDisabled())
                .append("\n\tRoutingMethod: ").append(profile.trafficRoutingMethod())
                .append("\n\tMonitor status: ").append(profile.monitorStatus())
                .append("\n\tMonitoring port: ").append(profile.monitoringPort())
                .append("\n\tMonitoring path: ").append(profile.monitoringPath());

        Map<String, TrafficManagerAzureEndpoint> azureEndpoints = profile.azureEndpoints();
        if (!azureEndpoints.isEmpty()) {
            info.append("\n\tAzure endpoints:");
            int idx = 1;
            for (TrafficManagerAzureEndpoint endpoint : azureEndpoints.values()) {
                info.append("\n\t\tAzure endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tTarget resourceId: ").append(endpoint.targetAzureResourceId())
                        .append("\n\t\t\tTarget resourceType: ").append(endpoint.targetResourceType())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tDisabled: ").append(endpoint.isDisabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerExternalEndpoint> externalEndpoints = profile.externalEndpoints();
        if (!externalEndpoints.isEmpty()) {
            info.append("\n\tExternal endpoints:");
            int idx = 1;
            for (TrafficManagerExternalEndpoint endpoint : externalEndpoints.values()) {
                info.append("\n\t\tExternal endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tFQDN: ").append(endpoint.fqdn())
                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tDisabled: ").append(endpoint.isDisabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints = profile.nestedProfileEndpoints();
        if (!nestedProfileEndpoints.isEmpty()) {
            info.append("\n\tNested profile endpoints:");
            int idx = 1;
            for (TrafficManagerNestedProfileEndpoint endpoint : nestedProfileEndpoints.values()) {
                info.append("\n\t\tNested profile endpoint: #").append(idx++)
                        .append("\n\t\t\tId: ").append(endpoint.id())
                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
                        .append("\n\t\t\tNested profileId: ").append(endpoint.nestedProfileId())
                        .append("\n\t\t\tMinimum child threshold: ").append(endpoint.minChildEndpoints())
                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
                        .append("\n\t\t\tDisabled: ").append(endpoint.isDisabled())
                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
            }
        }
        System.out.println(info.toString());
    }
}
