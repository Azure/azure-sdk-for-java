// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
//
// package com.azure.management;
//
// import com.microsoft.azure.management.network.PublicIPAddress;
// import com.microsoft.azure.management.network.PublicIPAddresses;
// import com.microsoft.azure.management.resources.ResourceGroup;
// import com.microsoft.azure.management.resources.fluentcore.arm.Region;
// import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
// import com.microsoft.azure.management.trafficmanager.EndpointType;
// import com.microsoft.azure.management.trafficmanager.TargetAzureResourceType;
// import com.microsoft.azure.management.trafficmanager.TrafficManagerAzureEndpoint;
// import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;
// import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;
// import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
// import com.microsoft.azure.management.trafficmanager.TrafficManagerProfiles;
// import org.junit.Assert;
//
// import java.util.Map;
//
/// **
// * Test of traffic manager management.
// */
// public class TestTrafficManager extends TestTemplate<TrafficManagerProfile, TrafficManagerProfiles> {
//
//    private final PublicIPAddresses publicIPAddresses;
//
//    private final String externalEndpointName21 = "external-ep-1";
//    private final String externalEndpointName22 = "external-ep-2";
//    private final String externalEndpointName23 = "external-ep-3";
//
//    private final String externalFqdn21 = "www.azure.microsoft.com";
//    private final String externalFqdn22 = "www.bing.com";
//    private final String externalFqdn23 = "www.github.com";
//
//    private final String azureEndpointName = "azure-ep-1";
//    private final String nestedProfileEndpointName = "nested-profile-ep-1";
//
//    public TestTrafficManager(PublicIPAddresses publicIPAddresses) {
//        this.publicIPAddresses = publicIPAddresses;
//    }
//
//    @Override
//    public TrafficManagerProfile createResource(TrafficManagerProfiles profiles) throws Exception {
//        final Region region = Region.US_EAST;
//        final String groupName = "rg" + this.testId;
//
//        final String pipName = "pip" + this.testId;
//        final String pipDnsLabel = SdkContext.randomResourceName("contoso", 15);
//
//        final String tmProfileName = "tm" + this.testId;
//        final String nestedTmProfileName = "nested" + tmProfileName;
//
//        final String tmProfileDnsLabel = SdkContext.randomResourceName("tmdns", 15);
//        final String nestedTmProfileDnsLabel = "nested" + tmProfileDnsLabel;
//
//        ResourceGroup.DefinitionStages.WithCreate rgCreatable =
// profiles.manager().resourceManager().resourceGroups().define(groupName)
//                .withRegion(region);
//
//        // Creates a TM profile that will be used as a nested profile endpoint in parent TM profile
//        //
//        TrafficManagerProfile nestedProfile = profiles.define(nestedTmProfileName)
//                .withNewResourceGroup(rgCreatable)
//                .withLeafDomainLabel(nestedTmProfileDnsLabel)
//                .withPriorityBasedRouting()
//                .defineExternalTargetEndpoint(externalEndpointName21)
//                    .toFqdn("www.gitbook.com")
//                    .fromRegion(Region.INDIA_CENTRAL)
//                    .attach()
//                .withHttpsMonitoring()
//                .withTimeToLive(500)
//                .create();
//
//        Assert.assertTrue(nestedProfile.isEnabled());
//        Assert.assertNotNull(nestedProfile.monitorStatus());
//        Assert.assertEquals(nestedProfile.monitoringPort(), 443);
//        Assert.assertEquals(nestedProfile.monitoringPath(), "/");
//        Assert.assertEquals(nestedProfile.azureEndpoints().size(), 0);
//        Assert.assertEquals(nestedProfile.nestedProfileEndpoints().size(), 0);
//        Assert.assertEquals(nestedProfile.externalEndpoints().size(), 1);
//        Assert.assertEquals(nestedProfile.fqdn(), nestedTmProfileDnsLabel + ".trafficmanager.net");
//        Assert.assertEquals(nestedProfile.timeToLive(), 500);
//
//        // Creates a public ip to be used as an Azure endpoint
//        //
//        PublicIPAddress publicIPAddress = this.publicIPAddresses.define(pipName)
//                .withRegion(region)
//                .withNewResourceGroup(rgCreatable)
//                .withLeafDomainLabel(pipDnsLabel)
//                .create();
//
//        Assert.assertNotNull(publicIPAddress.fqdn());
//        // Creates a TM profile
//        //
//
//        // bugfix
//        TrafficManagerProfile updatedProfile = nestedProfile.update()
//                                .defineAzureTargetEndpoint(azureEndpointName)
//                                    .toResourceId(publicIPAddress.id())
//                                    .withTrafficDisabled()
//                                    .withRoutingPriority(11)
//                                    .attach()
//                                .apply();
//
//        Assert.assertEquals(1, updatedProfile.azureEndpoints().size());
//        Assert.assertTrue(updatedProfile.azureEndpoints().containsKey(azureEndpointName));
//        Assert.assertEquals(1, updatedProfile.externalEndpoints().size());
//        Assert.assertTrue(updatedProfile.externalEndpoints().containsKey(externalEndpointName21));
//
//        TrafficManagerProfile updatedProfileFromGet = profiles.getById(updatedProfile.id());
//
//        Assert.assertEquals(1, updatedProfileFromGet.azureEndpoints().size());
//        Assert.assertTrue(updatedProfileFromGet.azureEndpoints().containsKey(azureEndpointName));
//        Assert.assertEquals(1, updatedProfileFromGet.externalEndpoints().size());
//        Assert.assertTrue(updatedProfileFromGet.externalEndpoints().containsKey(externalEndpointName21));
//
//        nestedProfile.update()
//                .withoutEndpoint(azureEndpointName)
//                .apply();
//
//        Assert.assertEquals(0, nestedProfile.azureEndpoints().size());
//        Assert.assertEquals(1, nestedProfile.externalEndpoints().size());
//        Assert.assertTrue(nestedProfile.externalEndpoints().containsKey(externalEndpointName21));
//
//        updatedProfileFromGet = profiles.getById(updatedProfile.id());
//        Assert.assertEquals(0, updatedProfileFromGet.azureEndpoints().size());
//        Assert.assertEquals(nestedProfile.azureEndpoints().size(), updatedProfileFromGet.azureEndpoints().size());
//        Assert.assertEquals(1, updatedProfileFromGet.externalEndpoints().size());
//        Assert.assertTrue(updatedProfileFromGet.externalEndpoints().containsKey(externalEndpointName21));
//        // end of bugfix
//
//        TrafficManagerProfile profile = profiles.define(tmProfileName)
//                .withNewResourceGroup(rgCreatable)
//                .withLeafDomainLabel(tmProfileDnsLabel)
//                .withWeightBasedRouting()
//                .defineExternalTargetEndpoint(externalEndpointName21)
//                    .toFqdn(externalFqdn21)
//                    .fromRegion(Region.US_EAST)
//                    .withRoutingPriority(1)
//                    .withRoutingWeight(1)
//                    .attach()
//                .defineExternalTargetEndpoint(externalEndpointName22)
//                    .toFqdn(externalFqdn22)
//                    .fromRegion(Region.US_EAST2)
//                    .withRoutingPriority(2)
//                    .withRoutingWeight(1)
//                    .withTrafficDisabled()
//                    .attach()
//                .defineAzureTargetEndpoint(azureEndpointName)
//                    .toResourceId(publicIPAddress.id())
//                    .withRoutingPriority(3)
//                    .attach()
//                .defineNestedTargetEndpoint(nestedProfileEndpointName)
//                    .toProfile(nestedProfile)
//                    .fromRegion(Region.INDIA_CENTRAL)
//                    .withMinimumEndpointsToEnableTraffic(1)
//                    .withRoutingPriority(4)
//                .attach()
//                .withHttpMonitoring()
//                .create();
//
//        Assert.assertTrue(profile.isEnabled());
//        Assert.assertNotNull(profile.monitorStatus());
//        Assert.assertEquals(profile.monitoringPort(), 80);
//        Assert.assertEquals(profile.monitoringPath(), "/");
//        Assert.assertEquals(profile.azureEndpoints().size(), 1);
//        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
//        Assert.assertEquals(profile.externalEndpoints().size(), 2);
//        Assert.assertEquals(profile.fqdn(), tmProfileDnsLabel + ".trafficmanager.net");
//        Assert.assertEquals(profile.timeToLive(), 300); // Default
//
//        profile = profile.refresh();
//        Assert.assertEquals(profile.azureEndpoints().size(), 1);
//        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
//        Assert.assertEquals(profile.externalEndpoints().size(), 2);
//
//        int c = 0;
//        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
//            Assert.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
//            if (endpoint.name().equalsIgnoreCase(externalEndpointName21)) {
//                Assert.assertEquals(endpoint.routingPriority(), 1);
//                Assert.assertEquals(endpoint.fqdn(), externalFqdn21);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST);
//                c++;
//            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
//                Assert.assertEquals(endpoint.routingPriority(), 2);
//                Assert.assertEquals(endpoint.fqdn(), externalFqdn22);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
//                c++;
//            }
//        }
//        Assert.assertEquals(c, 2);
//
//        c = 0;
//        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
//            Assert.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
//            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
//                Assert.assertEquals(endpoint.routingPriority(), 3);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                Assert.assertEquals(endpoint.targetAzureResourceId(), publicIPAddress.id());
//                Assert.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
//                c++;
//            }
//        }
//        Assert.assertEquals(c, 1);
//
//        c = 0;
//        for (TrafficManagerNestedProfileEndpoint endpoint : profile.nestedProfileEndpoints().values()) {
//            Assert.assertEquals(endpoint.endpointType(), EndpointType.NESTED_PROFILE);
//            if (endpoint.name().equalsIgnoreCase(nestedProfileEndpointName)) {
//                Assert.assertEquals(endpoint.routingPriority(), 4);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                Assert.assertEquals(endpoint.minimumChildEndpointCount(), 1);
//                Assert.assertEquals(endpoint.nestedProfileId(), nestedProfile.id());
//                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.INDIA_CENTRAL);
//                c++;
//            }
//        }
//        Assert.assertEquals(c, 1);
//        return profile;
//    }
//
//    @Override
//    public TrafficManagerProfile updateResource(TrafficManagerProfile profile) throws Exception {
//        // Remove an endpoint, update two endpoints and add new one
//        //
//        profile.update()
//                .withTimeToLive(600)
//                .withHttpMonitoring(8080, "/")
//                .withPerformanceBasedRouting()
//                .withoutEndpoint(externalEndpointName21)
//                .updateAzureTargetEndpoint(azureEndpointName)
//                    .withRoutingPriority(5)
//                    .withRoutingWeight(2)
//                    .parent()
//                .updateNestedProfileTargetEndpoint(nestedProfileEndpointName)
//                    .withTrafficDisabled()
//                    .parent()
//                .defineExternalTargetEndpoint(externalEndpointName23)
//                    .toFqdn(externalFqdn23)
//                    .fromRegion(Region.US_CENTRAL)
//                    .withRoutingPriority(6)
//                    .attach()
//                .apply();
//
//        Assert.assertEquals(profile.monitoringPort(), 8080);
//        Assert.assertEquals(profile.monitoringPath(), "/");
//        Assert.assertEquals(profile.azureEndpoints().size(), 1);
//        Assert.assertEquals(profile.nestedProfileEndpoints().size(), 1);
//        Assert.assertEquals(profile.externalEndpoints().size(), 2);
//        Assert.assertEquals(profile.timeToLive(), 600);
//
//        int c = 0;
//        for (TrafficManagerExternalEndpoint endpoint : profile.externalEndpoints().values()) {
//            Assert.assertEquals(endpoint.endpointType(), EndpointType.EXTERNAL);
//             if (endpoint.name().equalsIgnoreCase(externalEndpointName22)) {
//                Assert.assertEquals(endpoint.routingPriority(), 2);
//                Assert.assertEquals(endpoint.fqdn(), externalFqdn22);
//                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_EAST2);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                c++;
//            } else if (endpoint.name().equalsIgnoreCase(externalEndpointName23)) {
//                Assert.assertEquals(endpoint.routingPriority(), 6);
//                Assert.assertEquals(endpoint.fqdn(), externalFqdn23);
//                Assert.assertNotNull(endpoint.monitorStatus());
//                Assert.assertEquals(endpoint.sourceTrafficLocation(), Region.US_CENTRAL);
//                c++;
//            } else {
//                c++;
//            }
//        }
//        Assert.assertEquals(c, 2);
//
//        c = 0;
//        for (TrafficManagerAzureEndpoint endpoint : profile.azureEndpoints().values()) {
//            Assert.assertEquals(endpoint.endpointType(), EndpointType.AZURE);
//            if (endpoint.name().equalsIgnoreCase(azureEndpointName)) {
//                Assert.assertEquals(endpoint.routingPriority(), 5);
//                Assert.assertEquals(endpoint.routingWeight(), 2);
//                Assert.assertEquals(endpoint.targetResourceType(), TargetAzureResourceType.PUBLICIP);
//                c++;
//            }
//        }
//        Assert.assertEquals(c, 1);
//        return profile;
//    }
//
//    @Override
//    public void print(TrafficManagerProfile profile) {
//        StringBuilder info = new StringBuilder();
//        info.append("Traffic Manager Profile: ").append(profile.id())
//                .append("\n\tName: ").append(profile.name())
//                .append("\n\tResource group: ").append(profile.resourceGroupName())
//                .append("\n\tRegion: ").append(profile.regionName())
//                .append("\n\tTags: ").append(profile.tags())
//                .append("\n\tDNSLabel: ").append(profile.dnsLabel())
//                .append("\n\tFQDN: ").append(profile.fqdn())
//                .append("\n\tTTL: ").append(profile.timeToLive())
//                .append("\n\tEnabled: ").append(profile.isEnabled())
//                .append("\n\tRoutingMethod: ").append(profile.trafficRoutingMethod())
//                .append("\n\tMonitor status: ").append(profile.monitorStatus())
//                .append("\n\tMonitoring port: ").append(profile.monitoringPort())
//                .append("\n\tMonitoring path: ").append(profile.monitoringPath());
//
//        Map<String, TrafficManagerAzureEndpoint> azureEndpoints = profile.azureEndpoints();
//        if (!azureEndpoints.isEmpty()) {
//            info.append("\n\tAzure endpoints:");
//            int idx = 1;
//            for (TrafficManagerAzureEndpoint endpoint : azureEndpoints.values()) {
//                info.append("\n\t\tAzure endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tTarget resourceId: ").append(endpoint.targetAzureResourceId())
//                        .append("\n\t\t\tTarget resourceType: ").append(endpoint.targetResourceType())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//
//        Map<String, TrafficManagerExternalEndpoint> externalEndpoints = profile.externalEndpoints();
//        if (!externalEndpoints.isEmpty()) {
//            info.append("\n\tExternal endpoints:");
//            int idx = 1;
//            for (TrafficManagerExternalEndpoint endpoint : externalEndpoints.values()) {
//                info.append("\n\t\tExternal endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tFQDN: ").append(endpoint.fqdn())
//                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//
//        Map<String, TrafficManagerNestedProfileEndpoint> nestedProfileEndpoints = profile.nestedProfileEndpoints();
//        if (!nestedProfileEndpoints.isEmpty()) {
//            info.append("\n\tNested profile endpoints:");
//            int idx = 1;
//            for (TrafficManagerNestedProfileEndpoint endpoint : nestedProfileEndpoints.values()) {
//                info.append("\n\t\tNested profile endpoint: #").append(idx++)
//                        .append("\n\t\t\tId: ").append(endpoint.id())
//                        .append("\n\t\t\tType: ").append(endpoint.endpointType())
//                        .append("\n\t\t\tNested profileId: ").append(endpoint.nestedProfileId())
//                        .append("\n\t\t\tMinimum child threshold: ").append(endpoint.minimumChildEndpointCount())
//                        .append("\n\t\t\tSource Traffic Location: ").append(endpoint.sourceTrafficLocation())
//                        .append("\n\t\t\tMonitor status: ").append(endpoint.monitorStatus())
//                        .append("\n\t\t\tEnabled: ").append(endpoint.isEnabled())
//                        .append("\n\t\t\tRouting priority: ").append(endpoint.routingPriority())
//                        .append("\n\t\t\tRouting weight: ").append(endpoint.routingWeight());
//            }
//        }
//        System.out.println(info.toString());
//    }
// }
