// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import org.apache.commons.lang.time.StopWatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Azure Network sample for managing virtual machines with virtual network -
 * Create a virtual network with two Subnets - frontend and backend
 * Frontend allows HTTP in and denies Internet out
 * Backend denies Internet in and Internet out
 * Create m Linux virtual machines in the frontend
 * Create m Windows virtual machines in the backend.
 */
public final class ManageVirtualMachinesInParallelWithNetwork {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final int frontendVMCount = 4;
        final int backendVMCount = 4;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgNEPP", 24);
        final String frontEndNsgName = Utils.randomResourceName(azureResourceManager, "fensg", 24);
        final String backEndNsgName = Utils.randomResourceName(azureResourceManager, "bensg", 24);
        final String networkName = Utils.randomResourceName(azureResourceManager, "vnetCOMV", 24);
        final String storageAccountName = Utils.randomResourceName(azureResourceManager, "stgCOMV", 20);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final Region region = Region.US_SOUTH_CENTRAL;
        try {
            // Create a resource group [Where all resources gets created]
            ResourceGroup resourceGroup = azureResourceManager.resourceGroups().define(rgName)
                    .withRegion(region)
                    .create();

            //============================================================
            // Define a network security group for the front end of a subnet
            // front end subnet contains two rules
            // - ALLOW-SSH - allows SSH traffic into the front end subnet
            // - ALLOW-WEB- allows HTTP traffic into the front end subnet

            Creatable<NetworkSecurityGroup> frontEndNSGCreatable = azureResourceManager.networkSecurityGroups().define(frontEndNsgName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .defineRule("ALLOW-SSH")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(22)
                        .withProtocol(SecurityRuleProtocol.TCP)
                        .withPriority(100)
                        .withDescription("Allow SSH")
                        .attach()
                    .defineRule("ALLOW-HTTP")
                        .allowInbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(80)
                        .withProtocol(SecurityRuleProtocol.TCP)
                        .withPriority(101)
                        .withDescription("Allow HTTP")
                        .attach();

            //============================================================
            // Define a network security group for the back end of a subnet
            // back end subnet contains two rules
            // - ALLOW-SQL - allows SQL traffic only from the front end subnet
            // - DENY-WEB - denies all outbound internet traffic from the back end subnet

            Creatable<NetworkSecurityGroup> backEndNSGCreatable = azureResourceManager.networkSecurityGroups().define(backEndNsgName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .defineRule("ALLOW-SQL")
                        .allowInbound()
                        .fromAddress("172.16.1.0/24")
                        .fromAnyPort()
                        .toAnyAddress()
                        .toPort(1433)
                        .withProtocol(SecurityRuleProtocol.TCP)
                        .withPriority(100)
                        .withDescription("Allow SQL")
                        .attach()
                    .defineRule("DENY-WEB")
                        .denyOutbound()
                        .fromAnyAddress()
                        .fromAnyPort()
                        .toAnyAddress()
                        .toAnyPort()
                        .withAnyProtocol()
                        .withDescription("Deny Web")
                        .withPriority(200)
                        .attach();

            System.out.println("Creating security group for the front ends - allows SSH and HTTP");
            System.out.println("Creating security group for the back ends - allows SSH and denies all outbound internet traffic");

            @SuppressWarnings("unchecked")
            Collection<NetworkSecurityGroup> networkSecurityGroups = azureResourceManager.networkSecurityGroups()
                    .create(frontEndNSGCreatable, backEndNSGCreatable).values();

            NetworkSecurityGroup frontendNSG = null;
            NetworkSecurityGroup backendNSG = null;
            for (NetworkSecurityGroup nsg : networkSecurityGroups) {
                if (nsg.name().equalsIgnoreCase(frontEndNsgName)) {
                    frontendNSG = nsg;
                }

                if (nsg.name().equalsIgnoreCase(backEndNsgName)) {
                    backendNSG = nsg;
                }
            }

            System.out.println("Created a security group for the front end: " + frontendNSG.id());
            Utils.print(frontendNSG);

            System.out.println("Created a security group for the back end: " + backendNSG.id());
            Utils.print(backendNSG);

            // Create Network [Where all the virtual machines get added to]
            Network network = azureResourceManager.networks().define(networkName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("172.16.0.0/16")
                    .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .withExistingNetworkSecurityGroup(frontendNSG)
                        .attach()
                    .defineSubnet("Back-end")
                        .withAddressPrefix("172.16.2.0/24")
                        .withExistingNetworkSecurityGroup(backendNSG)
                        .attach()
                    .create();

            // Prepare Creatable Storage account definition [For storing VMs disk]
            Creatable<StorageAccount> creatableStorageAccount = azureResourceManager.storageAccounts().define(storageAccountName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);

            // Prepare a batch of Creatable Virtual Machines definitions
            List<Creatable<VirtualMachine>> frontendCreatableVirtualMachines = new ArrayList<>();
            for (int i = 0; i < frontendVMCount; i++) {
                Creatable<VirtualMachine> creatableVirtualMachine = azureResourceManager.virtualMachines().define("VM-FE-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("Front-end")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                        .withNewStorageAccount(creatableStorageAccount);
                frontendCreatableVirtualMachines.add(creatableVirtualMachine);
            }

            List<Creatable<VirtualMachine>> backendCreatableVirtualMachines = new ArrayList<>();

            for (int i = 0; i < backendVMCount; i++) {
                Creatable<VirtualMachine> creatableVirtualMachine = azureResourceManager.virtualMachines().define("VM-BE-" + i)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withExistingPrimaryNetwork(network)
                        .withSubnet("Back-end")
                        .withPrimaryPrivateIPAddressDynamic()
                        .withoutPrimaryPublicIPAddress()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername(userName)
                        .withRootPassword(password)
                        .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                        .withNewStorageAccount(creatableStorageAccount);
                backendCreatableVirtualMachines.add(creatableVirtualMachine);
            }

            System.out.println("Creating the virtual machines");

            List<Creatable<VirtualMachine>> allCreatableVirtualMachines = new ArrayList<>();
            allCreatableVirtualMachines.addAll(frontendCreatableVirtualMachines);
            allCreatableVirtualMachines.addAll(backendCreatableVirtualMachines);
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            Collection<VirtualMachine> virtualMachines = azureResourceManager.virtualMachines().create(allCreatableVirtualMachines).values();

            stopwatch.stop();
            System.out.println("Created virtual machines");

            for (VirtualMachine virtualMachine : virtualMachines) {
                System.out.println(virtualMachine.id());
            }

            System.out.println("Virtual Machines create: (took " + (stopwatch.getTime() / 1000) + " seconds) ");
            return true;
        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }

        }
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

    private ManageVirtualMachinesInParallelWithNetwork() {
    }
}
