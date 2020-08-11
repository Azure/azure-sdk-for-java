// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.samples;


import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.network.models.ExpressRouteCrossConnection;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;

/**
 * Azure Network sample for managing express route cross connections.
 * - List Express Route Cross Connections
 * - Get Express Route Cross Connection by id
 * - Create private peering
 * - Create Microsoft peering
 * - Update Microsoft peering
 * - Delete peerings
 */
public final class ManageExpressRouteCrossConnection {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String connectionId = "<crossconnection_id>";
        try {
            //============================================================
            // list Express Route Cross Connections
            System.out.println("List express route cross connection...");
            azure.expressRouteCrossConnections().list().forEach(expressRouteCrossConnection ->
                System.out.println(expressRouteCrossConnection.name()));
            System.out.println();

            //============================================================
            // get Express Route Cross Connection by id
            ExpressRouteCrossConnection crossConnection = azure.expressRouteCrossConnections().getById(connectionId);

            //============================================================
            // create Express Route Cross Connection private peering
            crossConnection.peerings()
                    .defineAzurePrivatePeering()
                    .withPrimaryPeerAddressPrefix("10.0.0.0/30")
                    .withSecondaryPeerAddressPrefix("10.0.0.4/30")
                    .withVlanId(100)
                    .withPeerAsn(500)
                    .withSharedKey("A1B2C3D4")
                    .create();

            //============================================================
            // create Express Route Cross Connection Microsoft peering
            crossConnection.peerings()
                    .defineMicrosoftPeering()
                    .withAdvertisedPublicPrefixes("123.1.0.0/24")
                    .withCustomerAsn(45)
                    .withRoutingRegistryName("ARIN")
                    .withPrimaryPeerAddressPrefix("10.0.0.0/30")
                    .withSecondaryPeerAddressPrefix("10.0.0.4/30")
                    .withVlanId(600)
                    .withPeerAsn(500)
                    .withSharedKey("A1B2C3D4")
                    .defineIpv6Config()
                    .withAdvertisedPublicPrefix("3FFE:FFFF:0:CD31::/120")
                    .withCustomerAsn(23)
                    .withRoutingRegistryName("ARIN")
                    .withPrimaryPeerAddressPrefix("3FFE:FFFF:0:CD30::/126")
                    .withSecondaryPeerAddressPrefix("3FFE:FFFF:0:CD30::4/126")
                    .attach()
                    .create();

            //============================================================
            // update Microsoft peering
            crossConnection.peerings()
                    .getByName("MicrosoftPeering")
                    .update()
                    .withoutIpv6Config()
                    .withAdvertisedPublicPrefixes("123.1.0.0/30")
                    .apply();

            //============================================================
            // update private peering from crossconnection resource
            crossConnection.peeringsMap().get("AzurePrivatePeering")
                    .update()
                    .withPrimaryPeerAddressPrefix("10.1.0.0/30")
                    .apply();

            //============================================================
            // delete peerings
            crossConnection.peerings().deleteByName("AzurePrivatePeering");
            crossConnection.peerings().deleteByName("MicrosoftPeering");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
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
            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
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

    private ManageExpressRouteCrossConnection() {
    }
}
