// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;

import java.io.File;

/**
 * Azure Network sample for managing virtual network gateway.
 * - Create a virtual network with subnets
 * - Create virtual network gateway
 * - Update virtual network gateway with Point-to-Site connection configuration
 * - Generate and download VPN client configuration package. Now it can be used to create VPN connection to Azure.
 * - Revoke a client certificate
 * <p>
 * Please note: in order to run this sample, you need to have:
 * - pre-generated root certificate and public key exported to $CERT_PATH file
 * For more details please see https://docs.microsoft.com/en-us/azure/vpn-gateway/vpn-gateway-certificates-point-to-site for PowerShell instructions
 * and https://docs.microsoft.com/en-us/azure/vpn-gateway/vpn-gateway-certificates-point-to-site-makecert for Makecert instructions.
 * - client certificate generated for this root certificate installed on your machine.
 * Please see: https://docs.microsoft.com/en-us/azure/vpn-gateway/point-to-site-how-to-vpn-client-install-azure-cert
 * - thumbprint for client certificate saved to $CLIENT_CERT_THUMBPRINT
 */

public final class ManageVpnGatewayPoint2SiteConnection {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String certPath = System.getenv("CERT_PATH");
        final String clientCertThumbprint = System.getenv("CLIENT_CERT_THUMBPRINT");
        final Region region = Region.US_WEST2;
        final String rgName = azure.sdkContext().randomResourceName("rg", 20);
        final String vnetName = azure.sdkContext().randomResourceName("vnet", 20);
        final String vpnGatewayName = azure.sdkContext().randomResourceName("vngw", 20);

        try {
            //============================================================
            // Create virtual network with address spaces 192.168.0.0/16 and 10.254.0.0/16 and 3 subnets
            System.out.println("Creating virtual network...");
            Network network = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("192.168.0.0/16")
                    .withAddressSpace("10.254.0.0/16")
                    .withSubnet("GatewaySubnet", "192.168.200.0/24")
                    .withSubnet("FrontEnd", "192.168.1.0/24")
                    .withSubnet("BackEnd", "10.254.1.0/24")
                    .create();
            System.out.println("Created network");
            // Print the virtual network
            Utils.print(network);

            //============================================================
            // Create virtual network gateway
            System.out.println("Creating virtual network gateway...");
            VirtualNetworkGateway vngw1 = azure.virtualNetworkGateways().define(vpnGatewayName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingNetwork(network)
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
            System.out.println("Created virtual network gateway");

            //============================================================
            // Update virtual network gateway with Point-to-Site connection configuration
            System.out.println("Creating Point-to-Site configuration...");
            vngw1.update()
                    .definePointToSiteConfiguration()
                    .withAddressPool("172.16.201.0/24")
                    .withAzureCertificateFromFile("p2scert.cer", new File(certPath))
                    .attach()
                    .apply();
            System.out.println("Created Point-to-Site configuration");

            //============================================================
            // Generate and download VPN client configuration package. Now it can be used to create VPN connection to Azure.
            System.out.println("Generating VPN profile...");
            String profile = vngw1.generateVpnProfile();
            System.out.println(String.format("Profile generation is done. Please download client package at: %s", profile));

            // At this point vpn client package can be downloaded from provided link. Unzip it and run the configuration corresponding to your OS.
            // For Windows machine, VPN client .exe can be run. For non-Windows, please use configuration from downloaded VpnSettings.xml

            //============================================================
            // Revoke a client certificate. After this command, you will no longer available to connect with the corresponding client certificate.
            System.out.println("Revoking client certificate...");
            vngw1.update().updatePointToSiteConfiguration()
                    .withRevokedCertificate("p2sclientcert.cer", clientCertThumbprint)
                    .parent()
                    .apply();
            System.out.println("Revoked client certificate");

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

    private ManageVpnGatewayPoint2SiteConnection() {
    }
}
