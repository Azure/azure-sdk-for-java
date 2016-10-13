package com.microsoft.azure.management;

import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.trafficmanager.EndpointType;
import com.microsoft.azure.management.trafficmanager.TargetAzureResourceType;
import com.microsoft.azure.management.trafficmanager.TrafficManagerAzureEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
import org.junit.Assert;

import java.util.Map;

/**
 * Test of virtual network management.
 */
public class TestTrafficManager extends TestTemplate<TrafficManagerProfile, TrafficManagerProfiles> {

    private final PublicIpAddresses publicIpAddresses;
    private final ResourceGroups resourceGroups;

    private final String externalEndpointName21 = "external-ep-1";
    private final String externalEndpointName22 = "external-ep-2";
    private final String externalEndpointName23 = "external-ep-3";

    private final String externalFqdn21 = "www.azure.microsoft.com";
    private final String externalFqdn22 = "www.bing.com";
    private final String externalFqdn23 = "www.github.com";

    private final String azureEndpointName = "azure-ep-1";
    private final String nestedProfileEndpointName = "nested-profile-ep-1";

    public TestTrafficManager(ResourceGroups resourceGroups, PublicIpAddresses publicIpAddresses) {
        this.resourceGroups = resourceGroups;
        this.publicIpAddresses = publicIpAddresses;
    }

    @Override
    public TrafficManagerProfile createResource(TrafficManagerProfiles profiles) throws Exception {
        final Region region = Region.US_EAST;
        final String groupName = "rg" + this.testId;

        final String pipName = "pip" + this.testId;
        final String pipDnsLabel = ResourceNamer.randomResourceName("contoso", 15);

        final String tmProfileName = "tm" + this.testId;
        final String nestedTmProfileName = "nested" + tmProfileName;

        final String tmProfileDnsLabel = ResourceNamer.randomResourceName("tmdns", 15);
        final String nestedTmProfileDnsLabel = "nested" + tmProfileDnsLabel;

        ResourceGroup.DefinitionStages.WithCreate rgCreatable = resourceGroups.define(groupName)
                .withRegion(region);

        // Creates a TM profile that will be used as a nested profile endpoint in parent TM profile
        //
        TrafficManagerProfile nestedProfile = profiles.define(nestedTmProfileName)
                .withNewResourceGroup(rgCreatable)
                .withDnsLabel(nestedTmProfileDnsLabel)
                .withPriorityBasedRouting()
                .withHttpsMonitoring()
                .defineEndpoint("external-ep-1")
                    .withExternalFqdn("www.gitbook.com")
                    .withSourceTrafficLocation(Region.INDIA_CENTRAL)
                    .attach()
                .withTtl(500)
                .create();

        Assert.assertFalse(nestedProfile.isDisabled());
        Assert.assertNotNull(nestedProfile.monitorStatus());
        Assert.assertEquals(nestedProfile.monitoringPort(), 443);
        Assert.assertEquals(nestedProfile.monitoringPath(), "/");
        Assert.assertEquals(nestedProfile.azureEndpoints().size(), 0);
        Assert.assertEquals(nestedProfile.nestedProfileEndpoints().size(), 0);
        Assert.assertEquals(nestedProfile.externalEndpoints().size(), 1);
        Assert.assertEquals(nestedProfile.fqdn(), nestedTmProfileDnsLabel + ".trafficmanager.net");
        Assert.assertEquals(nestedProfile.ttl(), 500);

        // Creates a public ip to be used as an Azure endpoint
        //
        PublicIpAddress publicIpAddress = this.publicIpAddresses.define(pipName)
                .withRegion(region)
                .withNewResourceGroup(rgCreatable)
                .withLeafDomainLabel(pipDnsLabel)
                .create();

        Assert.assertNotNull(publicIpAddress.fqdn());
        // Creates a TM profile
        //
        TrafficManagerProfile profile = profiles.define(tmProfileName)
                .withNewResourceGroup(rgCreatable)
                .withDnsLabel(tmProfileDnsLabel)
                .withWeightBasedRouting()
                .withHttpMonitoring()
                .defineEndpoint(externalEndpointName21)
                    .withExternalFqdn(externalFqdn21)
                    .withSourceTrafficLocation(Region.US_EAST)
                    .withRoutingPriority(1)
                    .withRoutingWeight(1)
                    .attach()
                .defineEndpoint(externalEndpointName22)
                    .withExternalFqdn(externalFqdn22)
                    .withSourceTrafficLocation(Region.US_EAST2)
                    .withRoutingPriority(2)
                    .withRoutingWeight(1)
                    .withTrafficDisabled()
                    .attach()
                .defineEndpoint(azureEndpointName)
                    .withTargetAzureResourceId(publicIpAddress.id())
                    .withRoutingPriority(3)
                .attach()
                .defineEndpoint(nestedProfileEndpointName)
                    .withNestedProfile(nestedProfile)
                    .withMinimumChildEndpoints(1)
                    .withSourceTrafficLocation(Region.INDIA_CENTRAL)
                    .withRoutingPriority(4)
                    .attach()
                .create();

        Assert.assertFalse(profile.isDisabled());
        Assert.assertNotNull(profile.monitorStatus());
        Assert.assertEquals(profile.monitoringPort(), 80);
        Assert.assertEquals(profile.monitoringPath(), "/");
        Assert.assertEquals(profile.azureEndpoints().size(), 1);
        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assert.assertEquals(profile.externalEndpoints().size(), 2);
        Assert.assertEquals(profile.fqdn(), tmProfileDnsLabel + ".trafficmanager.net");
        Assert.assertEquals(profile.ttl(), 300); // Default

        profile = profile.refresh();
        Assert.assertEquals(profile.azureEndpoints().size(), 1);
        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assert.assertEquals(profile.externalEndpoints().size(), 2);

        int c = 0;
        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
            Assert.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
            if (endpoint.name().equalsIgnoreCase(externalEndpointName21)) {
                Assert.assertEquals(endpoint.routingPriority(), 1);
                Assert.assertEquals(endpoint.fqdn(), externalFqdn21);
                Assert.assertNotNull(endpoint.monitorStatus());
                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST);
                c++;
            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
                Assert.assertEquals(endpoint.routingPriority(), 2);
                Assert.assertEquals(endpoint.fqdn(), externalFqdn22);
                Assert.assertNotNull(endpoint.monitorStatus());
                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
                c++;
            }
        }
        Assert.assertEquals(c, 2);

        c = 0;
        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
            Assert.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
                Assert.assertEquals(endpoint.routingPriority(), 3);
                Assert.assertNotNull(endpoint.monitorStatus());
                Assert.assertEquals(endpoint.targetAzureResourceId(), publicIpAddress.id());
                Assert.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
                c++;
            }
        }
        Assert.assertEquals(c, 1);

        c = 0;
        for (TrafficManagerNestedProfileEndpoint endpoint : profile.nestedProfileEndpoints().values()) {
            Assert.assertEquals(endpoint.endpointType(), EndpointType.NESTEDPROFILE);
            if (endpoint.name().equalsIgnoreCase(nestedProfileEndpointName)) {
                Assert.assertEquals(endpoint.routingPriority(), 4);
                Assert.assertNotNull(endpoint.monitorStatus());
                Assert.assertEquals(endpoint.minChildEndpoints(), 1);
                Assert.assertEquals(endpoint.nestedProfileId(), nestedProfile.id());
                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.INDIA_CENTRAL);
                c++;
            }
        }
        Assert.assertEquals(c, 1);
        return profile;
    }

    @Override
    public TrafficManagerProfile updateResource(TrafficManagerProfile profile) throws Exception {
        // Remove an endpoint, update two endpoints and add new one
        //
        profile.update()
                .withTtl(600)
                .withHttpMonitoring(8080, "/")
                .withPerformanceBasedRouting()
                .withoutEndpoint(externalEndpointName21)
                .updateAzureEndpoint(azureEndpointName)
                    .withRoutingPriority(5)
                    .withRoutingWeight(2)
                    .parent()
                .updateNestedProfileEndpoint(nestedProfileEndpointName)
                    .withTrafficDisabled()
                    .parent()
                .defineEndpoint(externalEndpointName23)
                    .withExternalFqdn(externalFqdn23)
                    .withSourceTrafficLocation(Region.US_CENTRAL)
                    .withRoutingPriority(6)
                    .attach()
                .apply();

        Assert.assertEquals(profile.monitoringPort(), 8080);
        Assert.assertEquals(profile.monitoringPath(), "/");
        Assert.assertEquals(profile.azureEndpoints().size(), 1);
        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
        Assert.assertEquals(profile.externalEndpoints().size(), 2);
        Assert.assertEquals(profile.ttl(), 600);

        int c = 0;
        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
            Assert.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
             if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
                Assert.assertEquals(endpoint.routingPriority(), 2);
                Assert.assertEquals(endpoint.fqdn(), externalFqdn22);
                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
                Assert.assertNotNull(endpoint.monitorStatus());
                c++;
            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName23)) {
                Assert.assertEquals(endpoint.routingPriority(), 6);
                Assert.assertEquals(endpoint.fqdn(), externalFqdn23);
                Assert.assertNotNull(endpoint.monitorStatus());
                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_CENTRAL);
                c++;
            } else {
                c++;
            }
        }
        Assert.assertEquals(c, 2);

        c = 0;
        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
            Assert.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
                Assert.assertEquals(endpoint.routingPriority(), 5);
                Assert.assertEquals(endpoint.routingWeight(), 2);
                Assert.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
                c++;
            }
        }
        Assert.assertEquals(c, 1);
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
