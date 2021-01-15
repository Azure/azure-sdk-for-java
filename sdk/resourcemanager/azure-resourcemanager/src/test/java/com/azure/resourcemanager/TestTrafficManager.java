// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.trafficmanager.models.EndpointType;
import com.azure.resourcemanager.trafficmanager.models.TargetAzureResourceType;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerAzureEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerNestedProfileEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfiles;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

/** Test of traffic manager management. */
public class TestTrafficManager extends TestTemplate<TrafficManagerProfile, TrafficManagerProfiles> {

    private final PublicIpAddresses publicIpAddresses;

    private final String externalEndpointName21 = "external-ep-1";
    private final String externalEndpointName22 = "external-ep-2";
    private final String externalEndpointName23 = "external-ep-3";

    private final String externalFqdn21 = "www.azure.com";
    private final String externalFqdn22 = "www.bing.com";
    private final String externalFqdn23 = "www.github.com";

    private final String azureEndpointName = "azure-ep-1";
    private final String nestedProfileEndpointName = "nested-profile-ep-1";

    public TestTrafficManager(PublicIpAddresses publicIpAddresses) {
        this.publicIpAddresses = publicIpAddresses;
    }

    @Override
    public TrafficManagerProfile createResource(TrafficManagerProfiles profiles) throws Exception {
        final Region region = Region.US_EAST;
        final String groupName = profiles.manager().resourceManager().internalContext().randomResourceName("rg", 10);

        final String pipName = profiles.manager().resourceManager().internalContext().randomResourceName("pip", 10);
        final String pipDnsLabel = profiles.manager().resourceManager().internalContext().randomResourceName("contoso", 15);

        final String tmProfileName = profiles.manager().resourceManager().internalContext().randomResourceName("tm", 10);
        final String nestedTmProfileName = "nested" + tmProfileName;

        final String tmProfileDnsLabel = profiles.manager().resourceManager().internalContext().randomResourceName("tmdns", 10);
        final String nestedTmProfileDnsLabel = "nested" + tmProfileDnsLabel;

        ResourceGroup.DefinitionStages.WithCreate rgCreatable =
            profiles.manager().resourceManager().resourceGroups().define(groupName).withRegion(region);

        // Creates a TM profile that will be used as a nested profile endpoint in parent TM profile
        //
        TrafficManagerProfile nestedProfile =
            profiles
                .define(nestedTmProfileName)
                .withNewResourceGroup(rgCreatable)
                .withLeafDomainLabel(nestedTmProfileDnsLabel)
                .withPriorityBasedRouting()
                .defineExternalTargetEndpoint(externalEndpointName21)
                .toFqdn("www.gitbook.com")
                .fromRegion(Region.INDIA_CENTRAL)
                .attach()
                .withHttpsMonitoring()
                .withTimeToLive(500)
                .create();

        Assertions.assertTrue(nestedProfile.isEnabled());
        Assertions.assertNotNull(nestedProfile.monitorStatus());
        Assertions.assertEquals(nestedProfile.monitoringPort(), 443);
        Assertions.assertEquals(nestedProfile.monitoringPath(), "/");
        Assertions.assertEquals(nestedProfile.azureEndpoints().size(), 0);
        Assertions.assertEquals(nestedProfile.nestedProfileEndpoints().size(), 0);
        Assertions.assertEquals(nestedProfile.externalEndpoints().size(), 1);
        Assertions.assertEquals(nestedProfile.fqdn(), nestedTmProfileDnsLabel + ".trafficmanager.net");
        Assertions.assertEquals(nestedProfile.timeToLive(), 500);

        // Creates a public ip to be used as an Azure endpoint
        //
        PublicIpAddress publicIPAddress =
            this
                .publicIpAddresses
                .define(pipName)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withLeafDomainLabel(pipDnsLabel)
                .create();

        Assertions.assertNotNull(publicIPAddress.fqdn());
        // Creates a TM profile
        //

        // bugfix
        TrafficManagerProfile updatedProfile =
            nestedProfile
                .update()
                .defineAzureTargetEndpoint(azureEndpointName)
                .toResourceId(publicIPAddress.id())
                .withTrafficDisabled()
                .withRoutingPriority(11)
                .attach()
                .apply();

        Assertions.assertEquals(1, updatedProfile.azureEndpoints().size());
        Assertions.assertTrue(updatedProfile.azureEndpoints().containsKey(azureEndpointName));
        Assertions.assertEquals(1, updatedProfile.externalEndpoints().size());
        Assertions.assertTrue(updatedProfile.externalEndpoints().containsKey(externalEndpointName21));

        TrafficManagerProfile updatedProfileFromGet = profiles.getById(updatedProfile.id());

        Assertions.assertEquals(1, updatedProfileFromGet.azureEndpoints().size());
        Assertions.assertTrue(updatedProfileFromGet.azureEndpoints().containsKey(azureEndpointName));
        Assertions.assertEquals(1, updatedProfileFromGet.externalEndpoints().size());
        Assertions.assertTrue(updatedProfileFromGet.externalEndpoints().containsKey(externalEndpointName21));

        nestedProfile.update().withoutEndpoint(azureEndpointName).apply();

        Assertions.assertEquals(0, nestedProfile.azureEndpoints().size());
        Assertions.assertEquals(1, nestedProfile.externalEndpoints().size());
        Assertions.assertTrue(nestedProfile.externalEndpoints().containsKey(externalEndpointName21));

        updatedProfileFromGet = profiles.getById(updatedProfile.id());
        Assertions.assertEquals(0, updatedProfileFromGet.azureEndpoints().size());
        Assertions.assertEquals(nestedProfile.azureEndpoints().size(), updatedProfileFromGet.azureEndpoints().size());
        Assertions.assertEquals(1, updatedProfileFromGet.externalEndpoints().size());
        Assertions.assertTrue(updatedProfileFromGet.externalEndpoints().containsKey(externalEndpointName21));
        // end of bugfix

        TrafficManagerProfile profile =
            profiles
                .define(tmProfileName)
                .withNewResourceGroup(rgCreatable)
                .withLeafDomainLabel(tmProfileDnsLabel)
                .withWeightBasedRouting()
                .defineExternalTargetEndpoint(externalEndpointName21)
                .toFqdn(externalFqdn21)
                .fromRegion(Region.US_EAST)
                .withRoutingPriority(1)
                .withRoutingWeight(1)
                .attach()
                .defineExternalTargetEndpoint(externalEndpointName22)
                .toFqdn(externalFqdn22)
                .fromRegion(Region.US_EAST2)
                .withRoutingPriority(2)
                .withRoutingWeight(1)
                .withTrafficDisabled()
                .attach()
                .defineAzureTargetEndpoint(azureEndpointName)
                .toResourceId(publicIPAddress.id())
                .withRoutingPriority(3)
                .attach()
                .defineNestedTargetEndpoint(nestedProfileEndpointName)
                .toProfile(nestedProfile)
                .fromRegion(Region.INDIA_CENTRAL)
                .withMinimumEndpointsToEnableTraffic(1)
                .withRoutingPriority(4)
                .attach()
                .withHttpMonitoring()
                .create();

        Assertions.assertTrue(profile.isEnabled());
        Assertions.assertNotNull(profile.monitorStatus());
        Assertions.assertEquals(profile.monitoringPort(), 80);
        Assertions.assertEquals(profile.monitoringPath(), "/");
        Assertions.assertEquals(profile.azureEndpoints().size(), 1);
        Assertions.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assertions.assertEquals(profile.externalEndpoints().size(), 2);
        Assertions.assertEquals(profile.fqdn(), tmProfileDnsLabel + ".trafficmanager.net");
        Assertions.assertEquals(profile.timeToLive(), 300); // Default

        profile = profile.refresh();
        Assertions.assertEquals(profile.azureEndpoints().size(), 1);
        Assertions.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assertions.assertEquals(profile.externalEndpoints().size(), 2);

        int c = 0;
        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
            Assertions.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
            if (endpoint.name().equalsIgnoreCase(externalEndpointName21)) {
                Assertions.assertEquals(endpoint.routingPriority(), 1);
                Assertions.assertEquals(endpoint.fqdn(), externalFqdn21);
                Assertions.assertNotNull(endpoint.monitorStatus());
                Assertions.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST);
                c++;
            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
                Assertions.assertEquals(endpoint.routingPriority(), 2);
                Assertions.assertEquals(endpoint.fqdn(), externalFqdn22);
                Assertions.assertNotNull(endpoint.monitorStatus());
                Assertions.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
                c++;
            }
        }
        Assertions.assertEquals(c, 2);

        c = 0;
        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
            Assertions.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
                Assertions.assertEquals(endpoint.routingPriority(), 3);
                Assertions.assertNotNull(endpoint.monitorStatus());
                Assertions.assertEquals(endpoint.targetAzureResourceId(), publicIPAddress.id());
                Assertions.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
                c++;
            }
        }
        Assertions.assertEquals(c, 1);

        c = 0;
        for (TrafficManagerNestedProfileEndpoint endpoint : profile.nestedProfileEndpoints().values()) {
            Assertions.assertEquals(endpoint.endpointType(), EndpointType.NESTED_PROFILE);
            if (endpoint.name().equalsIgnoreCase(nestedProfileEndpointName)) {
                Assertions.assertEquals(endpoint.routingPriority(), 4);
                Assertions.assertNotNull(endpoint.monitorStatus());
                Assertions.assertEquals(endpoint.minimumChildEndpointCount(), 1);
                Assertions.assertEquals(endpoint.nestedProfileId(), nestedProfile.id());
                Assertions.assertEquals(endpoint.sourceTrafficLocation(), Region.INDIA_CENTRAL);
                c++;
            }
        }
        Assertions.assertEquals(c, 1);
        return profile;
    }

    @Override
    public TrafficManagerProfile updateResource(TrafficManagerProfile profile) throws Exception {
        // Remove an endpoint, update two endpoints and add new one
        //
        profile
            .update()
            .withTimeToLive(600)
            .withHttpMonitoring(8080, "/")
            .withPerformanceBasedRouting()
            .withoutEndpoint(externalEndpointName21)
            .updateAzureTargetEndpoint(azureEndpointName)
            .withRoutingPriority(5)
            .withRoutingWeight(2)
            .parent()
            .updateNestedProfileTargetEndpoint(nestedProfileEndpointName)
            .withTrafficDisabled()
            .parent()
            .defineExternalTargetEndpoint(externalEndpointName23)
            .toFqdn(externalFqdn23)
            .fromRegion(Region.US_CENTRAL)
            .withRoutingPriority(6)
            .attach()
            .apply();

        Assertions.assertEquals(profile.monitoringPort(), 8080);
        Assertions.assertEquals(profile.monitoringPath(), "/");
        Assertions.assertEquals(profile.azureEndpoints().size(), 1);
        Assertions.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assertions.assertEquals(profile.externalEndpoints().size(), 2);
        Assertions.assertEquals(profile.timeToLive(), 600);

        int c = 0;
        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
            Assertions.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
            if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
                Assertions.assertEquals(endpoint.routingPriority(), 2);
                Assertions.assertEquals(endpoint.fqdn(), externalFqdn22);
                Assertions.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
                Assertions.assertNotNull(endpoint.monitorStatus());
                c++;
            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName23)) {
                Assertions.assertEquals(endpoint.routingPriority(), 6);
                Assertions.assertEquals(endpoint.fqdn(), externalFqdn23);
                Assertions.assertNotNull(endpoint.monitorStatus());
                Assertions.assertEquals(endpoint.sourceTrafficLocation(), Region.US_CENTRAL);
                c++;
            } else {
                c++;
            }
        }
        Assertions.assertEquals(c, 2);

        c = 0;
        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
            Assertions.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
                Assertions.assertEquals(endpoint.routingPriority(), 5);
                Assertions.assertEquals(endpoint.routingWeight(), 2);
                Assertions.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
                c++;
            }
        }
        Assertions.assertEquals(c, 1);
        return profile;
    }

    @Override
    public void print(TrafficManagerProfile profile) {
        StringBuilder info = new StringBuilder();
        info
            .append("Traffic Manager Profile: ")
            .append(profile.id())
            .append("\n\tName: ")
            .append(profile.name())
            .append("\n\tResource group: ")
            .append(profile.resourceGroupName())
            .append("\n\tRegion: ")
            .append(profile.regionName())
            .append("\n\tTags: ")
            .append(profile.tags())
            .append("\n\tDNSLabel: ")
            .append(profile.dnsLabel())
            .append("\n\tFQDN: ")
            .append(profile.fqdn())
            .append("\n\tTTL: ")
            .append(profile.timeToLive())
            .append("\n\tEnabled: ")
            .append(profile.isEnabled())
            .append("\n\tRoutingMethod: ")
            .append(profile.trafficRoutingMethod())
            .append("\n\tMonitor status: ")
            .append(profile.monitorStatus())
            .append("\n\tMonitoring port: ")
            .append(profile.monitoringPort())
            .append("\n\tMonitoring path: ")
            .append(profile.monitoringPath());

        Map<String, TrafficManagerAzureEndpoint> azureEndpoints = profile.azureEndpoints();
        if (!azureEndpoints.isEmpty()) {
            info.append("\n\tAzure endpoints:");
            int idx = 1;
            for (TrafficManagerAzureEndpoint endpoint : azureEndpoints.values()) {
                info
                    .append("\n\t\tAzure endpoint: #")
                    .append(idx++)
                    .append("\n\t\t\tId: ")
                    .append(endpoint.id())
                    .append("\n\t\t\tType: ")
                    .append(endpoint.endpointType())
                    .append("\n\t\t\tTarget resourceId: ")
                    .append(endpoint.targetAzureResourceId())
                    .append("\n\t\t\tTarget resourceType: ")
                    .append(endpoint.targetResourceType())
                    .append("\n\t\t\tMonitor status: ")
                    .append(endpoint.monitorStatus())
                    .append("\n\t\t\tEnabled: ")
                    .append(endpoint.isEnabled())
                    .append("\n\t\t\tRouting priority: ")
                    .append(endpoint.routingPriority())
                    .append("\n\t\t\tRouting weight: ")
                    .append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerExternalEndpoint> externalEndpoints = profile.externalEndpoints();
        if (!externalEndpoints.isEmpty()) {
            info.append("\n\tExternal endpoints:");
            int idx = 1;
            for (TrafficManagerExternalEndpoint endpoint : externalEndpoints.values()) {
                info
                    .append("\n\t\tExternal endpoint: #")
                    .append(idx++)
                    .append("\n\t\t\tId: ")
                    .append(endpoint.id())
                    .append("\n\t\t\tType: ")
                    .append(endpoint.endpointType())
                    .append("\n\t\t\tFQDN: ")
                    .append(endpoint.fqdn())
                    .append("\n\t\t\tSource Traffic Location: ")
                    .append(endpoint.sourceTrafficLocation())
                    .append("\n\t\t\tMonitor status: ")
                    .append(endpoint.monitorStatus())
                    .append("\n\t\t\tEnabled: ")
                    .append(endpoint.isEnabled())
                    .append("\n\t\t\tRouting priority: ")
                    .append(endpoint.routingPriority())
                    .append("\n\t\t\tRouting weight: ")
                    .append(endpoint.routingWeight());
            }
        }

        Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints = profile.nestedProfileEndpoints();
        if (!nestedProfileEndpoints.isEmpty()) {
            info.append("\n\tNested profile endpoints:");
            int idx = 1;
            for (TrafficManagerNestedProfileEndpoint endpoint : nestedProfileEndpoints.values()) {
                info
                    .append("\n\t\tNested profile endpoint: #")
                    .append(idx++)
                    .append("\n\t\t\tId: ")
                    .append(endpoint.id())
                    .append("\n\t\t\tType: ")
                    .append(endpoint.endpointType())
                    .append("\n\t\t\tNested profileId: ")
                    .append(endpoint.nestedProfileId())
                    .append("\n\t\t\tMinimum child threshold: ")
                    .append(endpoint.minimumChildEndpointCount())
                    .append("\n\t\t\tSource Traffic Location: ")
                    .append(endpoint.sourceTrafficLocation())
                    .append("\n\t\t\tMonitor status: ")
                    .append(endpoint.monitorStatus())
                    .append("\n\t\t\tEnabled: ")
                    .append(endpoint.isEnabled())
                    .append("\n\t\t\tRouting priority: ")
                    .append(endpoint.routingPriority())
                    .append("\n\t\t\tRouting weight: ")
                    .append(endpoint.routingWeight());
            }
        }
        System.out.println(info.toString());
    }
}
