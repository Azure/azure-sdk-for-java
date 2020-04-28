// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.management.network.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.management.Azure;
import com.azure.management.network.ExpressRouteCircuit;
import com.azure.management.network.ExpressRouteCircuitSkuType;
import com.azure.management.network.Network;
import com.azure.management.network.VirtualNetworkGateway;
import com.azure.management.network.VirtualNetworkGatewaySkuName;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.profile.AzureProfile;

/**
 * Azure Network sample for managing express route circuits.
 * - Create Express Route circuit
 * - Create Express Route circuit peering. Please note: express route circuit should be provisioned by connectivity provider before this step.
 * - Adding authorization to express route circuit
 * - Create virtual network to be associated with virtual network gateway
 * - Create virtual network gateway
 * - Create virtual network gateway connection
 */
public final class ManageExpressRoute {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_NORTH_CENTRAL;
        final String rgName = azure.sdkContext().randomResourceName("rg", 20);
        final String ercName = azure.sdkContext().randomResourceName("erc", 20);
        final String gatewayName = azure.sdkContext().randomResourceName("gtw", 20);
        final String connectionName = azure.sdkContext().randomResourceName("con", 20);
        final String vnetName = azure.sdkContext().randomResourceName("vnet", 20);

        try {
            //============================================================
            // create Express Route Circuit
            System.out.println("Creating express route circuit...");
            ExpressRouteCircuit erc = azure.expressRouteCircuits().define(ercName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withServiceProvider("Equinix")
                    .withPeeringLocation("Silicon Valley")
                    .withBandwidthInMbps(50)
                    .withSku(ExpressRouteCircuitSkuType.PREMIUM_METEREDDATA)
                    .create();
            System.out.println("Created express route circuit");

            //============================================================
            // Create Express Route circuit peering. Please note: express route circuit should be provisioned by connectivity provider before this step.
            System.out.println("Creating express route circuit peering...");
            erc.peerings().defineAzurePrivatePeering()
                    .withPrimaryPeerAddressPrefix("123.0.0.0/30")
                    .withSecondaryPeerAddressPrefix("123.0.0.4/30")
                    .withVlanId(200)
                    .withPeerASN(100)
                    .create();
            System.out.println("Created express route circuit peering");

            //============================================================
            // Adding authorization to express route circuit
            erc.update()
                    .withAuthorization("myAuthorization")
                    .apply();

            //============================================================
            // Create virtual network to be associated with virtual network gateway
            System.out.println("Creating virtual network...");
            Network network = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace("192.168.0.0/16")
                    .withSubnet("GatewaySubnet", "192.168.200.0/26")
                    .withSubnet("FrontEnd", "192.168.1.0/24")
                    .create();

            //============================================================
            // Create virtual network gateway
            System.out.println("Creating virtual network gateway...");
            VirtualNetworkGateway vngw1 = azure.virtualNetworkGateways().define(gatewayName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withExistingNetwork(network)
                    .withExpressRoute()
                    .withSku(VirtualNetworkGatewaySkuName.STANDARD)
                    .create();
            System.out.println("Created virtual network gateway");

            //============================================================
            // Create virtual network gateway connection
            System.out.println("Creating virtual network gateway connection...");
            vngw1.connections().define(connectionName)
                    .withExpressRoute(erc)
                    // Note: authorization key is required only in case express route circuit and virtual network gateway are in different subscriptions
                    // .withAuthorization(erc.inner().authorizations().get(0).authorizationKey())
                    .create();
            System.out.println("Created virtual network gateway connection");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE, true);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.environment().getActiveDirectoryEndpoint())
                .build();

            Azure azure = Azure
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManageExpressRoute() {
    }
}
