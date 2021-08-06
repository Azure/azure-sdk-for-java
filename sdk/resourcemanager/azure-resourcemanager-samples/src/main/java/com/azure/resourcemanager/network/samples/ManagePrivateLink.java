// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PrivateDnsZoneGroup;
import com.azure.resourcemanager.network.models.PrivateEndpoint;
import com.azure.resourcemanager.network.models.PrivateLinkSubResourceName;
import com.azure.resourcemanager.privatedns.models.PrivateDnsZone;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.samples.SSHShell;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.jcraft.jsch.JSchException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Azure network sample for managing private link.
 * <p>
 * - Create Storage Account with read-access geo-redundant storage
 * - Create Virtual Network
 * - Create Private Endpoints for both primrary and secondary blob endpoint
 * - Create Private DNS Zone
 * - Create Private DNS Zone Group on the Private Endpoints
 * - (Optional) Create Virtual Machine in the Virtual Network to test DNS configure
 */
public final class ManagePrivateLink {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) throws JSchException, UnsupportedEncodingException, MalformedURLException {
        final boolean validateOnVirtualMachine = true;

        final Region region = Region.US_EAST;
        final String vnAddressSpace = "10.0.0.0/24";
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg", 15);
        final String saName = Utils.randomResourceName(azureResourceManager, "sa", 10);
        final String vnName = Utils.randomResourceName(azureResourceManager, "vn", 10);
        final String subnetName = "default";
        final String peName = Utils.randomResourceName(azureResourceManager, "pe", 10);
        final String pecName = Utils.randomResourceName(azureResourceManager, "pec", 10);
        final String vnlName = Utils.randomResourceName(azureResourceManager, "vnl", 10);
        final String pdzgName = "default";
        final String pdzcName = Utils.randomResourceName(azureResourceManager, "pdzcName", 20);
        final String peName2 = Utils.randomResourceName(azureResourceManager, "pe", 10);
        final String pecName2 = Utils.randomResourceName(azureResourceManager, "pec", 10);
        final String pdzcName2 = Utils.randomResourceName(azureResourceManager, "pdzcName", 20);

        final String vmName = Utils.randomResourceName(azureResourceManager, "vm", 10);
        final String publicSshKey = SSHShell.generateSSHKeys(null, null).getSshPublicKey(); // use your public SSH key

        try {
            System.out.println("Creating storage account...");
            StorageAccount storageAccount = azureResourceManager.storageAccounts().define(saName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.STANDARD_RAGRS)
                .withAccessFromSelectedNetworks()   // deny access (except for private link)
                .create();

            Utils.print(storageAccount);

            String saDomainName = new URL(storageAccount.endPoints().primary().blob()).getHost();
            System.out.println("Domain Name for Storage Account Blob: " + saDomainName);

            String saDomainNameSecondary = new URL(storageAccount.endPoints().secondary().blob()).getHost();
            System.out.println("Domain Name for Storage Account Blob Secondary: " + saDomainNameSecondary);

            List<PrivateLinkResource> privateLinkResources = storageAccount.listPrivateLinkResources().stream().collect(Collectors.toList());
            for (PrivateLinkResource privateLinkResource : privateLinkResources) {
                Utils.print(privateLinkResource);
            }
            String blobDnsZoneName = privateLinkResources.stream()
                .filter(p -> PrivateLinkSubResourceName.STORAGE_BLOB.toString().equals(p.groupId()))
                .findAny()
                .map(p -> p.requiredDnsZoneNames().iterator().next())
                .get();
            System.out.println("DNS Zone Name for Storage Account Blob: " + blobDnsZoneName);
            String blobDnsZoneNameSecondary = privateLinkResources.stream()
                .filter(p -> "blob_secondary".equals(p.groupId()))
                .findAny()
                .map(p -> p.requiredDnsZoneNames().iterator().next())
                .get();
            System.out.println("DNS Zone Name for Storage Account Blob Secondary: " + blobDnsZoneNameSecondary);

            System.out.println("Creating virtual network...");
            Network network = azureResourceManager.networks().define(vnName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withAddressSpace(vnAddressSpace)
                .defineSubnet(subnetName)
                    .withAddressPrefix(vnAddressSpace)
                    .disableNetworkPoliciesOnPrivateEndpoint()  // disable network policies on private endpoint
                    .attach()
                .create();

            System.out.println("Creating private endpoint...");
            // private endpoint
            PrivateEndpoint privateEndpoint = azureResourceManager.privateEndpoints().define(peName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withSubnetId(network.subnets().get(subnetName).id())
                .definePrivateLinkServiceConnection(pecName)
                    .withResourceId(storageAccount.id())
                    .withSubResource(PrivateLinkSubResourceName.STORAGE_BLOB)   // primary blob
                    .attach()
                .create();

            System.out.println("Private Endpoint for Storage Account Blob Endpoint");
            Utils.print(privateEndpoint);

            String saPrivateIp = privateEndpoint.customDnsConfigurations().get(0).ipAddresses().get(0);
            System.out.println("Storage Account Private IP: " + saPrivateIp);

            System.out.println("Creating private endpoint...");
            // private endpoint
            PrivateEndpoint privateEndpoint2 = azureResourceManager.privateEndpoints().define(peName2)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withSubnetId(network.subnets().get(subnetName).id())
                .definePrivateLinkServiceConnection(pecName2)
                    .withResourceId(storageAccount.id())
                    .withSubResource(PrivateLinkSubResourceName.fromString("blob_secondary"))   // secondary blob
                    .attach()
                .create();

            System.out.println("Private Endpoint for Storage Account Blob Secondary Endpoint");
            Utils.print(privateEndpoint2);

            String saPrivateIpSecondary = privateEndpoint2.customDnsConfigurations().get(0).ipAddresses().get(0);
            System.out.println("Storage Account Private IP for Secondary Endpoint: " + saPrivateIpSecondary);

            VirtualMachine virtualMachine = null;
            if (validateOnVirtualMachine) {
                System.out.println("Creating virtual machine...");
                // create a test virtual machine
                virtualMachine = azureResourceManager.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(subnetName)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername("testUser")
                    .withSsh(publicSshKey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2as_v4"))
                    .create();

                // verify private endpoint not yet works
                RunCommandResult commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainName), null);
                for (InstanceViewStatus status : commandResult.value()) {
                    System.out.println(status.message());
                }
                commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainNameSecondary), null);
                for (InstanceViewStatus status : commandResult.value()) {
                    System.out.println(status.message());
                }
                // DNS should not resolve the Private IP
            }

            System.out.println("Creating private dns zone...");
            // private dns zone
            PrivateDnsZone privateDnsZone = azureResourceManager.privateDnsZones().define(blobDnsZoneName)
                .withExistingResourceGroup(rgName)
                .defineARecordSet(saDomainName.split(Pattern.quote("."))[0])
                    .withIPv4Address(saPrivateIp)
                    .attach()
                .defineARecordSet(saDomainNameSecondary.split(Pattern.quote("."))[0])
                    .withIPv4Address(saPrivateIpSecondary)
                    .attach()
                .defineVirtualNetworkLink(vnlName)
                    .withVirtualNetworkId(network.id())
                    .attach()
                .create();

            Utils.print(privateDnsZone);

            System.out.println("Creating private dns zone group...");
            // private dns zone group on private endpoint
            PrivateDnsZoneGroup privateDnsZoneGroup = privateEndpoint.privateDnsZoneGroups().define(pdzgName)
                .withPrivateDnsZoneConfigure(pdzcName, privateDnsZone.id())
                .create();

            System.out.println("Private DNS Zone Group Name: " + privateDnsZoneGroup.name());

            System.out.println("Creating private dns zone group...");
            PrivateDnsZoneGroup privateDnsZoneGroup2 = privateEndpoint2.privateDnsZoneGroups().define(pdzgName)
                .withPrivateDnsZoneConfigure(pdzcName2, privateDnsZone.id())
                .create();

            System.out.println("Private DNS Zone Group Name: " + privateDnsZoneGroup2.name());

            if (validateOnVirtualMachine) {
                // verify private endpoint works
                RunCommandResult commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainName), null);
                for (InstanceViewStatus status : commandResult.value()) {
                    System.out.println(status.message());
                }
                commandResult = virtualMachine.runShellScript(Collections.singletonList("nslookup " + saDomainNameSecondary), null);
                for (InstanceViewStatus status : commandResult.value()) {
                    System.out.println(status.message());
                }
                // DNS should now resolve to the Private IP
            }
        } finally {
            try {
                System.out.println("Deleting resource group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }

        return true;
    }

    /**
     * Main entry point.
     *
     * @param args parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate

            final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);
            final TokenCredential credential = new DefaultAzureCredentialBuilder()
                .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
                .build();

            AzureResourceManager azureResourceManager = AzureResourceManager
                .configure()
                .withLogLevel(HttpLogDetailLevel.BASIC)
                .authenticate(credential, profile)
                .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azureResourceManager.subscriptionId());

            runSample(azureResourceManager);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ManagePrivateLink() {
    }
}
