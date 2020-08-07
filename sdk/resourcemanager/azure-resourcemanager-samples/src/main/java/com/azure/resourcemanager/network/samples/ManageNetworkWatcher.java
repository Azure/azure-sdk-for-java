// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Direction;
import com.azure.resourcemanager.network.models.FlowLogSettings;
import com.azure.resourcemanager.network.models.IpFlowProtocol;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkWatcher;
import com.azure.resourcemanager.network.models.NextHop;
import com.azure.resourcemanager.network.models.PacketCapture;
import com.azure.resourcemanager.network.models.PcProtocol;
import com.azure.resourcemanager.network.models.SecurityGroupView;
import com.azure.resourcemanager.network.models.Topology;
import com.azure.resourcemanager.network.models.VerificationIPFlow;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.SdkContext;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;

/**
 * Azure Network sample for managing network watcher.
 * - Create Network Watcher
 * - Manage packet capture - track traffic to and from a virtual machine
 * Create a VM
 * Start a packet capture
 * Stop a packet capture
 * Get a packet capture
 * Delete a packet capture
 * - Verify IP flow - verify if traffic is allowed to or from a virtual machine
 * Get the IP address of a NIC on a virtual machine
 * Test IP flow on the NIC
 * - Analyze next hop - get the next hop type and IP address for a virtual machine
 * - Retrieve network topology for a resource group
 * - Analyze Virtual Machine Security by examining effective network security rules applied to a VM
 * Get security group view for the VM
 * - Configure Network Security Group Flow Logs
 * Get flow log settings
 * Enable NSG flow log
 * Disable NSG flow log
 * - Download a packet capture
 * - Download a flow log
 * - Delete network watcher
 */

public final class ManageNetworkWatcher {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_NORTH_CENTRAL;
        final String nwName = azure.sdkContext().randomResourceName("nw", 8);

        final String userName = "tirekicker";
        final String vnetName = azure.sdkContext().randomResourceName("vnet", 20);
        final String dnsLabel = azure.sdkContext().randomResourceName("pipdns", 20);
        final String subnetName = "subnet1";
        final String nsgName = azure.sdkContext().randomResourceName("nsg", 20);
        final String rgName = azure.sdkContext().randomResourceName("rg", 24);
        final String saName = azure.sdkContext().randomResourceName("sa", 24);
        final String vmName = azure.sdkContext().randomResourceName("vm", 24);
        final String packetCaptureName = azure.sdkContext().randomResourceName("pc", 8);
        final String packetCaptureStorageContainer = "packetcapture";
        // file name to save packet capture log locally
        final String packetCaptureFile = "packetcapture.cap";
        // file name to save flow log locally
        final String flowLogFile = "flowLog.json";

        NetworkWatcher nw = null;
        try {
            //============================================================
            // Create network watcher
            System.out.println("Creating network watcher...");
            nw = azure.networkWatchers().define(nwName)
                    .withRegion(region)
                    .withNewResourceGroup()
                    .create();
            System.out.println("Created network watcher");
            // Print the network watcher
            Utils.print(nw);

            //============================================================
            // Manage packet capture - track traffic to and from a virtual machine

            // Create network security group, virtual network and VM; add packetCapture extension to enable
            System.out.println("Creating network security group...");
            NetworkSecurityGroup nsg = azure.networkSecurityGroups().define(nsgName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .defineRule("DenyInternetInComing")
                    .denyInbound()
                    .fromAddress("INTERNET")
                    .fromAnyPort()
                    .toAnyAddress()
                    .toPort(443)
                    .withAnyProtocol()
                    .attach()
                    .create();

            System.out.println("Defining a virtual network...");
            Creatable<Network> virtualNetworkDefinition = azure.networks().define(vnetName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withAddressSpace("192.168.0.0/16")
                    .defineSubnet(subnetName)
                    .withAddressPrefix("192.168.2.0/24")
                    .withExistingNetworkSecurityGroup(nsg)
                    .attach();

            System.out.println("Creating a virtual machine...");
            VirtualMachine vm = azure.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork(virtualNetworkDefinition)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(dnsLabel)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456")
                    .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                    // This extension is needed to enable packet capture
                    .defineNewExtension("packetCapture")
                    .withPublisher("Microsoft.Azure.NetworkWatcher")
                    .withType("NetworkWatcherAgentLinux")
                    .withVersion("1.4")
                    .withMinorVersionAutoUpgrade()
                    .attach()
                    .create();

            // Create storage account
            System.out.println("Creating storage account...");
            StorageAccount storageAccount = azure.storageAccounts().define(saName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withGeneralPurposeAccountKindV2()
                    .create();

            // Start a packet capture
            System.out.println("Creating packet capture...");
            PacketCapture packetCapture = nw.packetCaptures()
                    .define(packetCaptureName)
                    .withTarget(vm.id())
                    .withStorageAccountId(storageAccount.id())
                    .withStoragePath(storageAccount.endPoints().primary().blob() + packetCaptureStorageContainer)
                    .withTimeLimitInSeconds(1500)
                    .definePacketCaptureFilter()
                    .withProtocol(PcProtocol.TCP)
                    .attach()
                    .create();
            System.out.println("Created packet capture");
            Utils.print(packetCapture);

            // Stop a packet capture
            System.out.println("Stopping packet capture...");
            packetCapture.stop();
            Utils.print(packetCapture);

            // Get a packet capture
            System.out.printf("Getting packet capture...");
            PacketCapture packetCapture1 = nw.packetCaptures().getByName(packetCaptureName);
            Utils.print(packetCapture1);

            // Delete a packet capture
            System.out.println("Deleting packet capture...");
            nw.packetCaptures().deleteByName(packetCapture.name());

            //============================================================
            // Verify IP flow - verify if traffic is allowed to or from a virtual machine
            // Get the IP address of a NIC on a virtual machine
            String ipAddress = vm.getPrimaryNetworkInterface().primaryIPConfiguration().privateIpAddress();
            // Test IP flow on the NIC
            System.out.println("Verifying IP flow for VM ID " + vm.id() + "...");
            VerificationIPFlow verificationIPFlow = nw.verifyIPFlow()
                    .withTargetResourceId(vm.id())
                    .withDirection(Direction.INBOUND)
                    .withProtocol(IpFlowProtocol.TCP)
                    .withLocalIPAddress(ipAddress)
                    .withRemoteIPAddress("8.8.8.8")
                    .withLocalPort("443")
                    .withRemotePort("443")
                    .execute();
            Utils.print(verificationIPFlow);

            //============================================================
            // Analyze next hop - get the next hop type and IP address for a virtual machine
            System.out.println("Calculating next hop...");
            NextHop nextHop = nw.nextHop().withTargetResourceId(vm.id())
                    .withSourceIpAddress(ipAddress)
                    .withDestinationIpAddress("8.8.8.8")
                    .execute();
            Utils.print(nextHop);

            //============================================================
            // Retrieve network topology for a resource group
            System.out.println("Getting topology...");
            Topology topology = nw.topology().withTargetResourceGroup(rgName).execute();
            Utils.print(topology);

            //============================================================
            // Analyze Virtual Machine Security by examining effective network security rules applied to a VM
            // Get security group view for the VM
            System.out.println("Getting security group view for a VM...");
            SecurityGroupView sgViewResult = nw.getSecurityGroupView(vm.id());
            Utils.print(sgViewResult);

            //============================================================
            // Configure Network Security Group Flow Logs

            // Get flow log settings
            FlowLogSettings flowLogSettings = nw.getFlowLogSettings(nsg.id());
            Utils.print(flowLogSettings);

            // Enable NSG flow log
            flowLogSettings.update()
                    .withLogging()
                    .withStorageAccount(storageAccount.id())
                    .withRetentionPolicyDays(5)
                    .withRetentionPolicyEnabled()
                    .apply();
            Utils.print(flowLogSettings);

            // wait for flow log to log an event
            System.out.println("Waiting for flow log to log an event...");
            SdkContext.sleep(250000);

            // Disable NSG flow log
            System.out.println("Disabling flow log...");
            flowLogSettings.update()
                    .withoutLogging()
                    .apply();
            Utils.print(flowLogSettings);

            // TODO: Verify the below azure storage code based on Azure core.
            //============================================================
            // Download a packet capture
            String accountKey = storageAccount.getKeys().get(0).value();
            String connectionString = String.format("DefaultEndpointsProtocol=https;AccountName=%s;AccountKey=%s",
                    storageAccount.name(), accountKey);

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .httpClient(storageAccount.manager().httpPipeline().getHttpClient())
                .buildClient();
            BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(packetCaptureStorageContainer);
            // iterate over subfolders structure to get the file
            BlobItem item = blobContainerClient.listBlobs().iterator().next();
//            while (item instanceof Blob) {
//                item = ((CloudBlobDirectory) item).listBlobs().iterator().next();
//            }
            // download packet capture file
            BlobClient blobClient = blobContainerClient.getBlobClient(item.getName());
            blobClient.downloadToFile(packetCaptureFile);
            System.out.println("Packet capture log saved to ./" + packetCaptureFile);

            //============================================================
            // Download a flow log
            blobContainerClient = blobServiceClient.getBlobContainerClient("insights-logs-networksecuritygroupflowevent");
            // iterate over subfolders structure to get the file
            item = blobContainerClient.listBlobs().iterator().next();
//            while (item instanceof CloudBlobDirectory) {
//                item = ((CloudBlobDirectory) item).listBlobs().iterator().next();
//            }
            blobClient = blobContainerClient.getBlobClient(item.getName());
            System.out.println("Flow log:");
            blobClient.download(System.out);
            // download flow file; note: this will download only one of the files
            blobClient.downloadToFile(flowLogFile);
            System.out.println("Flow log saved to ./" + flowLogFile);

            //============================================================
            // Delete network watcher
            System.out.println("Deleting network watcher...");
            azure.networkWatchers().deleteById(nw.id());
            System.out.println("Deleted network watcher");

            return true;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().beginDeleteByName(rgName);
                if (nw != null) {
                    System.out.println("Deleting network watcher resource group: " + nw.name());
                    azure.resourceGroups().beginDeleteByName(nw.resourceGroupName());
                }
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

    private ManageNetworkWatcher() {
    }
}
