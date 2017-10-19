/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.network.ExpressRouteCircuit;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuFamily;
import com.microsoft.azure.management.network.ExpressRouteCircuitSkuTier;
import com.microsoft.azure.management.network.ExpressRouteCircuits;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

/**
 * Tests Express Route Circuit.
 */
public class TestExpressRouteCircuit {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private static String CIRCUIT_NAME;

    private static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        CIRCUIT_NAME = "erc" + TEST_ID;
    }

    /**
     * Test Virtual Network Gateway Create and Update.
     */
    public static class Basic extends TestTemplate<ExpressRouteCircuit, ExpressRouteCircuits> {
        @Override
        public ExpressRouteCircuit createResource(ExpressRouteCircuits expressRouteCircuits) throws Exception {
            initializeResourceNames();

            // create Express Route Circuit
            ExpressRouteCircuit erc = expressRouteCircuits.define(CIRCUIT_NAME)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withServiceProvidet("Equinix")
                    .withPeeringLocation("Silicon Valley")
                    .withBandwidthInMbps(50)
                    .withSkuTier(ExpressRouteCircuitSkuTier.STANDARD)
                    .withSkuFamily(ExpressRouteCircuitSkuFamily.METERED_DATA)
                    .withTag("tag1", "value1")
                    .create();
            return erc;
        }

        @Override
        public ExpressRouteCircuit updateResource(ExpressRouteCircuit resource) throws Exception {
            resource.update()
                    .withTag("tag2", "value2")
                    .withoutTag("tag1")
                    .withBandwidthInMbps(200)
                    .withSkuFamily(ExpressRouteCircuitSkuFamily.UNLIMITED_DATA)
                    .withSkuTier(ExpressRouteCircuitSkuTier.PREMIUM)
                    .apply();
            resource.refresh();
            Assert.assertTrue(resource.tags().containsKey("tag2"));
            Assert.assertTrue(!resource.tags().containsKey("tag1"));
            Assert.assertEquals(Integer.valueOf(200), resource.serviceProviderProperties().bandwidthInMbps());
            Assert.assertEquals(ExpressRouteCircuitSkuFamily.UNLIMITED_DATA, resource.sku().family());
            Assert.assertEquals(ExpressRouteCircuitSkuTier.PREMIUM, resource.sku().tier());
            return resource;
        }

        @Override
        public void print(ExpressRouteCircuit resource) {
            StringBuilder info = new StringBuilder();
            info.append("Express Route Circuit: ").append(resource.id())
                    .append("\n\tName: ").append(resource.name())
                    .append("\n\tResource group: ").append(resource.resourceGroupName())
                    .append("\n\tRegion: ").append(resource.regionName())
                    .append("\n\tTags: ").append(resource.tags());
            System.out.println(info.toString());
        }
    }

    /**
     * Test Virtual Network Gateway Create and Update.
     */
    public static class ExpressRouteCircuitPeering extends TestTemplate<ExpressRouteCircuit, ExpressRouteCircuits> {
        @Override
        public ExpressRouteCircuit createResource(ExpressRouteCircuits expressRouteCircuits) throws Exception {
            initializeResourceNames();

            // create Express Route Circuit
            ExpressRouteCircuit erc = expressRouteCircuits.define(CIRCUIT_NAME)
                    .withRegion(REGION)
                    .withNewResourceGroup()
                    .withServiceProvidet("Equinix")
                    .withPeeringLocation("Silicon Valley")
                    .withBandwidthInMbps(50)
                    .withSkuTier(ExpressRouteCircuitSkuTier.PREMIUM)
                    .withSkuFamily(ExpressRouteCircuitSkuFamily.METERED_DATA)
                    .withTag("tag1", "value1")
                    .create();
            erc.peerings().defineMicrosoftPeering()
                    .withAdvertisedPublicPrefixes("123.1.0.0/24")
                    .withPrimaryPeerAddressPrefix("123.0.0.0/30")
                    .withSecondaryPeerAddressPrefix("123.0.0.4/30")
                    .withVlanId(200)
                    .withPeerAsn(100)
                    .create();
            return erc;
        }

        @Override
        public ExpressRouteCircuit updateResource(ExpressRouteCircuit resource) throws Exception {
            return resource;
        }

        @Override
        public void print(ExpressRouteCircuit resource) {
            StringBuilder info = new StringBuilder();
            info.append("Express Route Circuit: ").append(resource.id())
                    .append("\n\tName: ").append(resource.name())
                    .append("\n\tResource group: ").append(resource.resourceGroupName())
                    .append("\n\tRegion: ").append(resource.regionName())
                    .append("\n\tTags: ").append(resource.tags());
            System.out.println(info.toString());
        }
    }
}

