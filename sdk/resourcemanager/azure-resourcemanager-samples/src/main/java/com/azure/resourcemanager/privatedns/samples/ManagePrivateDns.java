// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.privatedns.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.samples.Utils;

import java.util.Arrays;

/**
 * Azure private DNS sample for managing private DNS zones.
 *  - Creates a private DNS zone (private.contoso.com)
 *  - Creates a virtual network
 *  - Link a virtual network
 *  - Creates test virtual machines
 *  - Creates an additional private DNS record
 *  - Test the private DNS zone
 */
public class ManagePrivateDns {

    private static final String CUSTOM_DOMAIN_NAME = "private.contoso.com";

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = azure.sdkContext().randomResourceName("rgNEMV", 24);
        final String vnetName = azure.sdkContext().randomResourceName("vnetwork-1", 24);
        final String subnetName = azure.sdkContext().randomResourceName("subnet-1", 24);
        final String linkName = azure.sdkContext().randomResourceName("vnlink-1", 24);
        final String vm1Name = azure.sdkContext().randomResourceName("vm1-", 24);
        final String vm2Name = azure.sdkContext().randomResourceName("vm2-", 24);
        final String rsName = azure.sdkContext().randomResourceName("recordset1-", 24);
        final String userName = "tirekicker";
        final String password = Utils.password();

        try {
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
                .withRegion(Region.US_WEST)
                .create();

            //============================================================
            // Creates a private DNS Zone

            System.out.println("Creating private DNS zone " + CUSTOM_DOMAIN_NAME + "...");
            PrivateDnsZone privateDnsZone = azure.privateDnsZones().define(CUSTOM_DOMAIN_NAME)
                .withExistingResourceGroup(resourceGroup)
                .create();

            System.out.println("Created private DNS zone " + privateDnsZone.name());
            Utils.print(privateDnsZone);

            //============================================================
            // Creates a virtual network

            System.out.println("Creating virtual network " + vnetName + "...");
            Network network = azure.networks().define(vnetName)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withAddressSpace("10.2.0.0/16")
                .withSubnet(subnetName, "10.2.0.0/24")
                .create();
            System.out.println("Created virtual network " + network.name());
            Utils.print(network);

            //============================================================
            // Links a virtual network

            System.out.println("Creating virtual network link " + linkName
                + " within private zone " + privateDnsZone.name() + " ...");
            privateDnsZone.update()
                .defineVirtualNetworkLink(linkName)
                    .enableAutoRegistration()
                    .withVirtualNetworkId(network.id())
                    .withETagCheck()
                    .attach()
                .apply();
            System.out.println("Linked a virtual network " + network.id());

            //============================================================
            // Creates test virtual machines

            System.out.println("Creating first virtual machine " + vm1Name + "...");
            VirtualMachine virtualMachine1 = azure.virtualMachines().define(vm1Name)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnetName)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername(userName)
                .withAdminPassword(password)
                .create();
            System.out.println("Created first virtual machine " + virtualMachine1.name());
            Utils.print(virtualMachine1);

            System.out.println("Starting first virtual machine " + virtualMachine1.name() + "...");
            virtualMachine1.start();
            System.out.println("Started first virtual machine " + virtualMachine1.name());

            System.out.println("Creating second virtual machine " + vm2Name + "...");
            VirtualMachine virtualMachine2 = azure.virtualMachines().define(vm2Name)
                .withRegion(Region.US_WEST)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnetName)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername(userName)
                .withAdminPassword(password)
                .create();
            System.out.println("Created second virtual machine " + virtualMachine2.name());

            System.out.println("Starting second virtual machine " + virtualMachine2.name() + "...");
            virtualMachine2.start();
            System.out.println("Started second virtual machine " + virtualMachine2.name());

            //============================================================
            // Creates an additional private DNS record

            System.out.println("Creating additional record set " + rsName + "...");
            privateDnsZone.update()
                .defineARecordSet(rsName)
                    .withIPv4Address(virtualMachine1.getPrimaryNetworkInterface().primaryPrivateIP())
                    .attach()
                .apply();
            System.out.println("Created additional record set " + rsName);
            Utils.print(privateDnsZone);

            //============================================================
            // Tests the private DNS zone

            String script1 = "New-NetFirewallRule -DisplayName \"Allow ICMPv4-In\" -Protocol ICMPv4";
            System.out.println("Preparing first command: " + script1);

            String script2 = "ping " + virtualMachine1.computerName() + "." + CUSTOM_DOMAIN_NAME;
            System.out.println("Preparing second command: " + script2);

            String script3 = "ping " + rsName + "." + CUSTOM_DOMAIN_NAME;
            System.out.println("Preparing third command: " + script3);

            System.out.println("Starting to run commands...");
            RunCommandResult result = virtualMachine2.runPowerShellScript(Arrays.asList(script1, script2, script3), null);
            for (InstanceViewStatus status : result.value()) {
                System.out.println(status.message());
            }
            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
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
}
