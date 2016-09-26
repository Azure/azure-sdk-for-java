package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceNamer;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Create n Linux virtual machines
 *
 * Create a virtual network with two Subnets – frontend and backend
 * Frontend allows HTTP in and denies Internet out
 * Backend denies Internet in and Internet out
 * Create m Linux virtual machines in the frontend
 * Create m Windows virtual machines in the backend.
 */
public final class ParallelCreate {
    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        final String rgName = ResourceNamer.randomResourceName("rgCOMV", 24);
        final String networkName1 = ResourceNamer.randomResourceName("vnetCOMV", 24);
        final String networkName2 = ResourceNamer.randomResourceName("vnetCOMV", 24);
        final String storageAccountName1 = ResourceNamer.randomResourceName("stgCOMV", 20);
        final String storageAccountName2 = ResourceNamer.randomResourceName("stgCOMV", 20);
        final String frontEndNSGName = ResourceNamer.randomResourceName("fensg", 24);
        final String backEndNSGName = ResourceNamer.randomResourceName("bensg", 24);
        final int vmCount = 5;
        final int frontendVmCount = 5;
        final int backendVmCount = 5;
        final String userName = "tirekicker";
        final String password = "12NewPA$$w0rd!";

        try {

            //=============================================================
            // Authenticate

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(HttpLoggingInterceptor.Level.BASIC)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            System.out.println("Selected subscription: " + azure.subscriptionId());
            try {
                // Create a resource group [Where all resources gets created]
                ResourceGroup resourceGroup = azure.resourceGroups()
                        .define(rgName)
                        .withRegion(Region.US_EAST)
                        .create();

                // Prepare Creatable Network definition [Where all the virtual machines get added to]
                Network.DefinitionStages.WithCreate creatableNetwork = azure.networks()
                        .define(networkName1)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16");

                // Prepare Creatable Storage account definition [Where VMs disk will be stored]
                StorageAccount.DefinitionStages.WithCreate creatableStorageAccount = azure.storageAccounts()
                        .define(storageAccountName1)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup);

                // Prepare a batch of Creatable Virtual Machines definitions
                List<Creatable<VirtualMachine>> creatableVirtualMachines1 = new ArrayList<>();
                for (int i = 0; i < vmCount; i++) {
                    VirtualMachine.DefinitionStages.WithCreate creatableVirtualMachine = azure.virtualMachines()
                            .define("VM-" + i)
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(resourceGroup)
                            .withNewPrimaryNetwork(creatableNetwork)
                            .withPrimaryPrivateIpAddressDynamic()
                            .withoutPrimaryPublicIpAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUserName("tirekicker")
                            .withPassword("12NewPA$$w0rd!")
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .withNewStorageAccount(creatableStorageAccount);
                    creatableVirtualMachines1.add(creatableVirtualMachine);
                }

                System.out.println("Creating the virtual machines");
                CreatedResources<VirtualMachine> virtualMachines1 = azure.virtualMachines().create(creatableVirtualMachines1);
                System.out.println("Created virtual machines");
                for (VirtualMachine virtualMachine : virtualMachines1) {
                    System.out.println(virtualMachine.id());
                }

                // Create a virtual network with two Subnets – frontend and backend
                //
                Network network = azure.networks()
                        .define(networkName2)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16")
                        .defineSubnet("Front-end")
                        .withAddressPrefix("172.16.1.0/24")
                        .attach()
                        .defineSubnet("Back-End")
                        .withAddressPrefix("172.16.2.0/24")
                        .attach()
                        .create();

                //============================================================
                // Define a network security group for the front end of a subnet
                // front end subnet contains two rules
                // - ALLOW-SSH - allows SSH traffic into the front end subnet
                // - ALLOW-WEB- allows HTTP traffic into the front end subnet

                NetworkSecurityGroup.DefinitionStages.WithCreate frontEndNSGCreatable = azure.networkSecurityGroups()
                        .define(frontEndNSGName)
                        .withRegion(Region.US_EAST)
                        .withNewResourceGroup(rgName)
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

                NetworkSecurityGroup.DefinitionStages.WithCreate backEndNSGCreatable = azure.networkSecurityGroups()
                        .define(backEndNSGName)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(rgName)
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

                System.out.println("Creating a security group for the front ends - allows SSH and HTTP");
                System.out.println("Creating a security group for the back ends - allows SSH and denies all outbound internet traffic");

                CreatedResources<NetworkSecurityGroup> networkSecurityGroups = azure.networkSecurityGroups()
                        .create(frontEndNSGCreatable, backEndNSGCreatable);

                NetworkSecurityGroup frontendNSG = null;
                NetworkSecurityGroup backendNSG = null;
                for (NetworkSecurityGroup networkSecurityGroup : networkSecurityGroups) {
                    if (networkSecurityGroup.name().equalsIgnoreCase(frontEndNSGName)) {
                        frontendNSG = networkSecurityGroup;
                    }

                    if (networkSecurityGroup.name().equalsIgnoreCase(backEndNSGName)) {
                        backendNSG = networkSecurityGroup;
                    }
                }

                System.out.println("Created a security group for the front end: " + frontendNSG.id());
                Utils.print(frontendNSG);

                System.out.println("Created a security group for the back end: " + backendNSG.id());
                Utils.print(backendNSG);

                //============================================================
                // Define a bunch of network interfaces that uses frontend NSG [for frontend virtual machines]

                List<Creatable<NetworkInterface>> creatableFrontendNICs = new ArrayList<>();
                for (int i = 0; i < frontendVmCount; i++) {

                    NetworkInterface.DefinitionStages.WithCreate creatableFrontendNIC = azure.networkInterfaces()
                            .define(ResourceNamer.randomResourceName("frontendNic" + i, 10))
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(network)
                            .withSubnet("Front-end")
                            .withPrimaryPrivateIpAddressDynamic()
                            .withNewPrimaryPublicIpAddress(ResourceNamer.randomResourceName("pip" + i, 10))
                            .withIpForwarding()
                            .withExistingNetworkSecurityGroup(frontendNSG);
                    creatableFrontendNICs.add(creatableFrontendNIC);
                }

                //============================================================
                // Define a bunch of network interfaces that uses backend NSG [for backend virtual machines]

                List<Creatable<NetworkInterface>> creatableBackendNIC = new ArrayList<>();
                for (int i = 0; i < backendVmCount; i++) {

                    NetworkInterface.DefinitionStages.WithCreate creatableFrontendNIC = azure.networkInterfaces()
                            .define(ResourceNamer.randomResourceName("backendNic" + i, 10))
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(network)
                            .withSubnet("Back-end")
                            .withPrimaryPrivateIpAddressDynamic()
                            .withIpForwarding()
                            .withExistingNetworkSecurityGroup(frontendNSG);
                    creatableBackendNIC.add(creatableFrontendNIC);
                }

                System.out.println("Creating network interfaces for the frontend virtual machines");
                CreatedResources<NetworkInterface> frontendVmNICs = azure.networkInterfaces().create(creatableFrontendNICs);
                System.out.println("Created network interfaces");
                for (NetworkInterface networkInterface : frontendVmNICs) {
                    System.out.println(networkInterface.id());
                }

                System.out.println("Creating network interfaces for the backend virtual machines");
                CreatedResources<NetworkInterface> backendVmNICs = azure.networkInterfaces().create(creatableBackendNIC);
                System.out.println("Created network interfaces");
                for (NetworkInterface networkInterface : backendVmNICs) {
                    System.out.println(networkInterface.id());
                }

                // Prepare Creatable Storage account definition [Where VMs disk will be stored]
                //
                StorageAccount.DefinitionStages.WithCreate creatableStorageAccount2 = azure.storageAccounts()
                        .define(storageAccountName2)
                        .withRegion(Region.US_EAST)
                        .withExistingResourceGroup(resourceGroup);


                List<Creatable<VirtualMachine>> creatableVirtualMachines2 = new ArrayList<>();
                //============================================================
                // Define frontend Linux Virtual machines

                for (int i = 0; i < frontendVmCount; i++) {
                    VirtualMachine.DefinitionStages.WithCreate creatableVirtualMachine = azure.virtualMachines()
                            .define(ResourceNamer.randomResourceName("VM-FE" + i, 15))
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(resourceGroup)
                            .withExistingPrimaryNetworkInterface(frontendVmNICs.get(i))
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUserName(userName)
                            .withPassword(password)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .withNewStorageAccount(creatableStorageAccount2);
                    creatableVirtualMachines2.add(creatableVirtualMachine);
                }

                //============================================================
                // Define backend Windows Virtual machines

                for (int i = 0; i < frontendVmCount; i++) {
                    VirtualMachine.DefinitionStages.WithCreate creatableVirtualMachine = azure.virtualMachines()
                            .define(ResourceNamer.randomResourceName("VM-BE" + i, 15))
                            .withRegion(Region.US_EAST)
                            .withExistingResourceGroup(resourceGroup)
                            .withExistingPrimaryNetworkInterface(backendVmNICs.get(i))
                            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                            .withAdminUserName(userName)
                            .withPassword(password)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .withNewStorageAccount(creatableStorageAccount);
                    creatableVirtualMachines2.add(creatableVirtualMachine);
                }

                System.out.println("Creating frontend and backend virtual machines");
                CreatedResources<VirtualMachine> virtualMachines2 = azure.virtualMachines().create(creatableVirtualMachines2);
                System.out.println("Created virtual machines");
                for (VirtualMachine virtualMachine : virtualMachines2) {
                    System.out.println(virtualMachine.id());
                }
            } catch (Exception f) {

                System.out.println(f.getMessage());
                f.printStackTrace();

            } finally {

                try {
                    System.out.println("Deleting Resource Group: " + rgName);
                    azure.resourceGroups().delete(rgName);
                    System.out.println("Deleted Resource Group: " + rgName);
                } catch (NullPointerException npe) {
                    System.out.println("Did not create any resources in Azure. No clean up is necessary");
                } catch (Exception g) {
                    g.printStackTrace();
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private ParallelCreate() {
    }
}

