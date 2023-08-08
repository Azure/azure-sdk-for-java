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
import com.azure.resourcemanager.network.models.ConnectivityCheck;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.Troubleshooting;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewayConnection;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Azure Network sample for managing virtual network gateway.
 * - Create 2 virtual networks with subnets and 2 virtual network gateways corresponding to each network
 * - Create VPN VNet-to-VNet connection
 * - Troubleshoot the connection
 * - Create network watcher in the same region as virtual network gateway
 * - Create storage account to store troubleshooting information
 * - Run troubleshooting for the connection - result will be 'UnHealthy' as need to create symmetrical connection from second gateway to the first
 * - Create virtual network connection from second gateway to the first and run troubleshooting. Result will be 'Healthy'.
 * - List VPN Gateway connections for the first gateway
 * - Create 2 virtual machines, each one in its network and verify connectivity between them
 */

public final class ManageVpnGatewayVNet2VNetConnection {

    /**
     * Main function which runs the actual sample.
     *
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_WEST2;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rg", 20);
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 20);
        final String vnet2Name = Utils.randomResourceName(azureResourceManager, "vnet", 20);
        final String vpnGatewayName = Utils.randomResourceName(azureResourceManager, "vngw", 20);
        final String vpnGateway2Name = Utils.randomResourceName(azureResourceManager, "vngw2", 20);
        final String connectionName = Utils.randomResourceName(azureResourceManager, "con", 20);
        final String connection2Name = Utils.randomResourceName(azureResourceManager, "con2", 20);
        final String nwName = Utils.randomResourceName(azureResourceManager, "nw", 20);
        final String vm1Name = Utils.randomResourceName(azureResourceManager, "vm1", 20);
        final String vm2Name = Utils.randomResourceName(azureResourceManager, "vm2", 20);
        final String rootname = "tirekicker";
        final String password = Utils.password();
        final String storageContainerName = "results";

        try {
            //============================================================
            // Create 2 virtual networks with subnets and 2 virtual network gateways corresponding to each network
            System.out.println("Creating virtual network...");
            Network network1 = azureResourceManager.networks().define(vnetName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.11.0.0/16")
                    .withSubnet("GatewaySubnet", "10.11.255.0/27")
                    .withSubnet("Subnet1", "10.11.0.0/24")
                    .create();
            System.out.println("Created network");
            // Print the virtual network
            Utils.print(network1);

            System.out.println("Creating virtual network gateway...");
            VirtualNetworkGateway vngw1 = azureResourceManager.virtualNetworkGateways().define(vpnGatewayName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingNetwork(network1)
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
            System.out.println("Created virtual network gateway");

            System.out.println("Creating virtual network...");
            Network network2 = azureResourceManager.networks().define(vnet2Name)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withAddressSpace("10.41.0.0/16")
                    .withSubnet("GatewaySubnet", "10.41.255.0/27")
                    .withSubnet("Subnet2", "10.41.0.0/24")
                    .create();
            System.out.println("Created virtual network");

            System.out.println("Creating virtual network gateway...");
            VirtualNetworkGateway vngw2 = azureResourceManager.virtualNetworkGateways().define(vpnGateway2Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingNetwork(network2)
                    .withRouteBasedVpn()
                    .withSku(VirtualNetworkGatewaySkuName.VPN_GW1)
                    .create();
            System.out.println("Created virtual network gateway");

            System.out.println("Creating virtual network gateway connection...");
            VirtualNetworkGatewayConnection connection = vngw1.connections()
                    .define(connectionName)
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw2)
                    .withSharedKey("MySecretKey")
                    .create();
            System.out.println("Created virtual network gateway connection");

            //============================================================
            // Troubleshoot the connection

            // create Network Watcher
            NetworkWatcher nw = azureResourceManager.networkWatchers().define(nwName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .create();
            // Create storage account to store troubleshooting information
            StorageAccount storageAccount = azureResourceManager.storageAccounts().define("sa" + Utils.randomResourceName(azureResourceManager, "", 8))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .create();

            // Create storage container to store troubleshooting results
            String accountKey = storageAccount.getKeys().get(0).value();
            String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s", storageAccount.name(), accountKey);
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(storageAccount.manager().httpPipeline().getHttpClient())
                .buildClient();
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(storageContainerName);
            containerClient.create();

            // Run troubleshooting for the connection - result will be 'UnHealthy' as need to create symmetrical connection from second gateway to the first
            Troubleshooting troubleshooting = nw.troubleshoot()
                    .withTargetResourceId(connection.id())
                    .withStorageAccount(storageAccount.id())
                    .withStoragePath(storageAccount.endPoints().primary().blob() + storageContainerName)
                    .execute();
            System.out.println("Troubleshooting status is: " + troubleshooting.code());

            //============================================================
            //  Create virtual network connection from second gateway to the first and run troubleshooting. Result will be 'Healthy'.
            vngw2.connections()
                    .define(connection2Name)
                    .withVNetToVNet()
                    .withSecondVirtualNetworkGateway(vngw1)
                    .withSharedKey("MySecretKey")
                    .create();
            ResourceManagerUtils.sleep(Duration.ofSeconds(250));
            troubleshooting = nw.troubleshoot()
                    .withTargetResourceId(connection.id())
                    .withStorageAccount(storageAccount.id())
                    .withStoragePath(storageAccount.endPoints().primary().blob() + storageContainerName)
                    .execute();
            System.out.println("Troubleshooting status is: " + troubleshooting.code());

            //============================================================
            // List VPN Gateway connections for particular gateway
            System.out.println("List connections...");
            vngw1.listConnections().forEach(connection1 -> System.out.println(connection1.name()));
            System.out.println();

            //============================================================
            // Create 2 virtual machines, each one in its network and verify connectivity between them
            List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();

            vmDefinitions.add(azureResourceManager.virtualMachines().define(vm1Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network1)
                    .withSubnet("Subnet1")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(rootname)
                    .withRootPassword(password)
                    // Extension currently needed for network watcher support
                    .defineNewExtension("networkWatcher")
                    .withPublisher("Microsoft.Azure.NetworkWatcher")
                    .withType("NetworkWatcherAgentLinux")
                    .withVersion("1.4")
                    .attach());
            vmDefinitions.add(azureResourceManager.virtualMachines().define(vm2Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withExistingPrimaryNetwork(network2)
                    .withSubnet("Subnet2")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(rootname)
                    .withRootPassword(password)
                    // Extension currently needed for network watcher support
                    .defineNewExtension("networkWatcher")
                    .withPublisher("Microsoft.Azure.NetworkWatcher")
                    .withType("NetworkWatcherAgentLinux")
                    .withVersion("1.4")
                    .attach());
            CreatedResources<VirtualMachine> createdVMs = azureResourceManager.virtualMachines().create(vmDefinitions);
            VirtualMachine vm1 = createdVMs.get(vmDefinitions.get(0).key());
            VirtualMachine vm2 = createdVMs.get(vmDefinitions.get(1).key());

            ConnectivityCheck connectivity = nw.checkConnectivity()
                    .toDestinationResourceId(vm2.id())
                    .toDestinationPort(22)
                    .fromSourceVirtualMachine(vm1.id())
                    .execute();
            System.out.println("Connectivity status: " + connectivity.connectionStatus());
            return true;
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azureResourceManager.resourceGroups().beginDeleteByName(rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
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

    private ManageVpnGatewayVNet2VNetConnection() {
    }
}
