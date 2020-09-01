/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management;

import com.microsoft.azure.management.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.azure.resourcemanager.trafficmanager.models.EndpointPropertiesSubnetsItem;
import com.azure.resourcemanager.trafficmanager.models.GeographicLocation;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerProfile;
import com.azure.resourcemanager.trafficmanager.models.TrafficRoutingMethod;
import com.azure.resourcemanager.trafficmanager.TrafficManager;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

public class TrafficManagerTests extends TestBase {
    private static String RG_NAME = null;

    public TrafficManagerTests() {
        super(TestBase.RunCondition.BOTH);
    }

    protected ResourceManager resourceManager;
    protected TrafficManager trafficManager;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(defaultSubscription);
        trafficManager = TrafficManager
                .authenticate(restClient, defaultSubscription);
    }

    @Override
    protected void cleanUpResources() {
        if (RG_NAME != null) {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
        }
    }

    @Test
    public void canGetGeographicHierarchy() {
        GeographicLocation rootLocation = this.trafficManager
                .profiles()
                .getGeographicHierarchyRoot();
        Assert.assertNotNull(rootLocation);
        Assert.assertNotNull(rootLocation.code());
        Assert.assertNotNull(rootLocation.name());
        Assert.assertNotNull(rootLocation.childLocations());
        Assert.assertNotNull(rootLocation.descendantLocations());
        Assert.assertFalse(rootLocation.childLocations().isEmpty());
        Assert.assertFalse(rootLocation.descendantLocations().isEmpty());
    }

    @Test
    public void canCreateUpdateProfileWithGeographicEndpoint() {
        RG_NAME = generateRandomResourceName("tmergtest", 15);
        final String tmProfileName = generateRandomResourceName("tmpr", 15);
        final String tmProfileDnsLabel = SdkContext.randomResourceName("tmdns", 15);

        GeographicLocation geographicLocation = this.trafficManager
                .profiles()
                .getGeographicHierarchyRoot();

        GeographicLocation california = null;
        GeographicLocation bangladesh = null;
        for (GeographicLocation location : geographicLocation.descendantLocations()) {
            if (california == null && location.name().equalsIgnoreCase("California")) {
                california = location;
            }
            if (bangladesh == null && location.name().equalsIgnoreCase("Bangladesh")) {
                bangladesh = location;
            }
            if (california != null && bangladesh != null) {
                break;
            }
        }
        Assert.assertNotNull(california);
        Assert.assertNotNull(bangladesh);

        TrafficManagerProfile profile = this.trafficManager.profiles().define(tmProfileName)
                .withNewResourceGroup(RG_NAME, Region.US_EAST)
                .withLeafDomainLabel(tmProfileDnsLabel)
                .withGeographicBasedRouting()
                .defineExternalTargetEndpoint("external-ep-1")
                    .toFqdn("www.gitbook.com")
                    .fromRegion(Region.ASIA_EAST)
                    .withGeographicLocation(california)
                    .withGeographicLocation(bangladesh)
                    .attach()
                .withHttpsMonitoring()
                .withTimeToLive(500)
                .create();

        Assert.assertNotNull(profile.inner());
        Assert.assertTrue(profile.trafficRoutingMethod().equals(TrafficRoutingMethod.GEOGRAPHIC));
        Assert.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        TrafficManagerExternalEndpoint endpoint = profile.externalEndpoints().get("external-ep-1");
        Assert.assertNotNull(endpoint.geographicLocationCodes());
        Assert.assertEquals(2, endpoint.geographicLocationCodes().size());

        profile.update()
                .updateExternalTargetEndpoint("external-ep-1")
                    .withoutGeographicLocation(california)
                    .parent()
                .apply();

        Assert.assertTrue(profile.trafficRoutingMethod().equals(TrafficRoutingMethod.GEOGRAPHIC));
        Assert.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assert.assertNotNull(endpoint.geographicLocationCodes());
        Assert.assertEquals(1, endpoint.geographicLocationCodes().size());
    }

    @Test
    public void canCreateTrafficManagerWithSubnetRouting() {
        RG_NAME = generateRandomResourceName("tmergtest", 15);
        final String tmProfileName = generateRandomResourceName("tmpr", 15);
        final String tmProfileDnsLabel = SdkContext.randomResourceName("tmdns", 15);

        EndpointPropertiesSubnetsItem subnetCidr = new EndpointPropertiesSubnetsItem();
        subnetCidr.withFirst("80.83.228.0").withScope(22);

        EndpointPropertiesSubnetsItem subnetRange = new EndpointPropertiesSubnetsItem();
        subnetRange.withFirst("25.26.27.28").withLast("29.30.31.32");

        TrafficManagerProfile profile = this.trafficManager.profiles().define(tmProfileName)
                .withNewResourceGroup(RG_NAME, Region.US_EAST)
                .withLeafDomainLabel(tmProfileDnsLabel)
                .withTrafficRoutingMethod(TrafficRoutingMethod.SUBNET)
                .defineExternalTargetEndpoint("external-ep-1")
                    .toFqdn("www.gitbook.com")
                    .fromRegion(Region.ASIA_EAST)
                    .withSubnet(subnetCidr.first(), subnetCidr.scope())
                    .withSubnet(subnetRange.first(), subnetRange.last())
                    .attach()
                .create();

        Assert.assertNotNull(profile.inner());
        Assert.assertTrue(profile.trafficRoutingMethod().equals(TrafficRoutingMethod.SUBNET));
        Assert.assertTrue(profile.externalEndpoints().containsKey("external-ep-1"));
        TrafficManagerExternalEndpoint endpoint = profile.externalEndpoints().get("external-ep-1");
        Assert.assertTrue(endpoint.subnets().size() == 2);
        boolean foundCidr = false;
        boolean foundRange = false;
        for (EndpointPropertiesSubnetsItem subnet : endpoint.subnets()) {
            if (subnet.first() != null && subnet.scope() != null) {
                if (subnet.first().equalsIgnoreCase(subnetCidr.first()) && subnet.scope() == subnetCidr.scope()) {
                    foundCidr = true;
                }
            }
            if (subnet.first() != null && subnet.last() != null) {
                if (subnet.first().equalsIgnoreCase(subnetRange.first()) && subnet.last().equalsIgnoreCase(subnetRange.last())) {
                    foundRange = true;
                }
            }
        }

        Assert.assertTrue(String.format("The subnet %s/%d not found in the endpoint.", subnetCidr.first(), subnetCidr.scope()), foundCidr);
        Assert.assertTrue(String.format("The subnet range %s-%s not found in the endpoint.", subnetCidr.first(), subnetCidr.last()), foundRange);

        profile = profile.update()
                .updateExternalTargetEndpoint("external-ep-1")
                    .withoutSubnet(subnetRange.first(), subnetRange.last())
                    .parent()
                .apply();

        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assert.assertTrue(endpoint.subnets().size() == 1);

        profile = this.trafficManager.profiles().getById(profile.id());

        endpoint = profile.externalEndpoints().get("external-ep-1");
        Assert.assertTrue(endpoint.subnets().size() == 1);
    }
}
