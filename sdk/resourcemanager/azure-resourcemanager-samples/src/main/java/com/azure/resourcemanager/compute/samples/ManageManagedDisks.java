// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.compute.models.RunCommandInput;
import com.azure.resourcemanager.compute.models.SnapshotSkuType;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.samples.Utils;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.OperatingSystemTypes;
import com.azure.resourcemanager.compute.models.Snapshot;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineCustomImage;
import com.azure.resourcemanager.compute.models.VirtualMachineDataDisk;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.core.management.Region;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This is sample will not be published, this is just to ensure out blog is honest.
 */
public final class ManageManagedDisks {

    /**
     * Main function which runs the actual sample.
     * @param azureResourceManager instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(AzureResourceManager azureResourceManager) {
        final Region region = Region.US_SOUTH_CENTRAL;
        final String rgName = Utils.randomResourceName(azureResourceManager, "rgCOMV", 15);
        final String userName = "tirekicker";
        final String sshkey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        try {

            // ::==Create a VM
            // Create a virtual machine with an implicit Managed OS disk and explicit Managed data disk

            System.out.println("Creating VM [with an implicit Managed OS disk and explicit Managed data disk]");

            final String linuxVM1Name = Utils.randomResourceName(azureResourceManager, "vm" + "-", 18);
            final String linuxVM1Pip = Utils.randomResourceName(azureResourceManager, "pip" + "-", 18);
            VirtualMachine linuxVM1 = azureResourceManager.virtualMachines()
                    .define(linuxVM1Name)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(linuxVM1Pip)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshkey)
                    .withNewDataDisk(50)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [with an implicit Managed OS disk and explicit Managed data disk]: " + linuxVM1.id());

            // Creation is simplified with implicit creation of managed disks without specifying all the disk details. You will notice that you do not require storage accounts
            // ::== Update the VM
            // Create a VMSS with implicit managed OS disks and explicit managed data disks

            System.out.println("Creating VMSS [with implicit managed OS disks and explicit managed data disks]");

            final String vmScaleSetName = Utils.randomResourceName(azureResourceManager, "vmss" + "-", 18);
            VirtualMachineScaleSet vmScaleSet = azureResourceManager.virtualMachineScaleSets()
                    .define(vmScaleSetName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D5_V2)
                        .withExistingPrimaryNetworkSubnet(prepareNetwork(azureResourceManager, region, rgName), "subnet1")
                        .withExistingPrimaryInternetFacingLoadBalancer(prepareLoadBalancer(azureResourceManager, region, rgName))
                        .withoutPrimaryInternalLoadBalancer()
                        .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                        .withRootUsername("tirekicker")
                        .withSsh(sshkey)
                        .withNewDataDisk(50)
                        .withNewDataDisk(50, 1, CachingTypes.READ_WRITE)
                        .withNewDataDisk(50, 2, CachingTypes.READ_ONLY)
                        .withCapacity(3)
                        .create();

            System.out.println("Created VMSS [with implicit managed OS disks and explicit managed data disks]");
            System.out.println("Created VMSS [with implicit managed OS disks and explicit managed data disks]");

            azureResourceManager.virtualMachineScaleSets().deleteById(vmScaleSet.id());

            // Create an empty disk and attach to a VM (Manage Virtual Machine With Disk)

            System.out.println("Creating empty data disk [to attach to a VM]");

            final String diskName = Utils.randomResourceName(azureResourceManager, "dsk" + "-", 18);
            Disk dataDisk = azureResourceManager.disks().define(diskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created empty data disk [to attach to a VM]");

            System.out.println("Creating VM [with new managed data disks and disk attached]");

            final String linuxVM2Name = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
            final String linuxVM2Pip = Utils.randomResourceName(azureResourceManager, "pip" + "-", 18);
            VirtualMachine linuxVM2 = azureResourceManager.virtualMachines().define(linuxVM2Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(linuxVM2Pip)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withSsh(sshkey)
                    // Begin: Managed data disks
                    .withNewDataDisk(50)
                    .withNewDataDisk(50, 1, CachingTypes.READ_WRITE)
                    .withExistingDataDisk(dataDisk)
                    // End: Managed data disks
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [with new managed data disks and disk attached]");

            // Update a VM

            System.out.println("Updating VM [by detaching a disk and adding empty disk]");

            linuxVM2.update()
                    .withoutDataDisk(2)
                    .withNewDataDisk(200)
                    .apply();

            System.out.println("Updated VM [by detaching a disk and adding empty disk]");

            // Create a VM from an image (Create Virtual Machine Using Custom Image from VM)

            System.out.println("Preparing specialized virtual machine with un-managed disk");

            final VirtualMachine linuxVM = prepareSpecializedUnmanagedVirtualMachine(azureResourceManager, region, rgName);

            System.out.println("Prepared specialized virtual machine with un-managed disk");

            System.out.println("Creating custom image from specialized virtual machine");

            final String customImageName = Utils.randomResourceName(azureResourceManager, "cimg" + "-", 10);
            VirtualMachineCustomImage virtualMachineCustomImage = azureResourceManager.virtualMachineCustomImages()
                    .define(customImageName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .fromVirtualMachine(linuxVM) // from a deallocated and generalized VM
                        .create();

            System.out.println("Created custom image from specialized virtual machine");

            System.out.println("Creating VM [from custom image]");

            final String linuxVM3Name = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
            VirtualMachine linuxVM3 = azureResourceManager.virtualMachines().define(linuxVM3Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withSsh(sshkey)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [from custom image]: " + linuxVM3.id());

            // Create a VM from a VHD (Create Virtual Machine Using Specialized VHD)

            final String linuxVMName4 = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
            final String specializedVhd = linuxVM.osUnmanagedDiskVhdUri();

            azureResourceManager.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Creating VM [by attaching un-managed disk]");

            VirtualMachine linuxVM4 = azureResourceManager.virtualMachines().define(linuxVMName4)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSUnmanagedDisk(specializedVhd, OperatingSystemTypes.LINUX)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [by attaching un-managed disk]: " + linuxVM4.id());

            // Create a Snapshot (Create Virtual Machine using specialized disks from snapshot)

            System.out.println("Preparing specialized virtual machine with managed disks");

            final VirtualMachine linuxVM5 = prepareSpecializedManagedVirtualMachine(azureResourceManager, region, rgName);
            Disk osDisk = azureResourceManager.disks().getById(linuxVM5.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM5.dataDisks().values()) {
                Disk d = azureResourceManager.disks().getById(disk.id());
                dataDisks.add(d);
            }

            System.out.println("Prepared specialized virtual machine with managed disks");

            System.out.println("Deleting VM: " + linuxVM5.id());
            azureResourceManager.virtualMachines().deleteById(linuxVM5.id());
            System.out.println("Deleted the VM: " + linuxVM5.id());

            System.out.println("Creating snapshot [from managed OS disk]");

            // Create a managed snapshot for an OS disk
            final String managedOSSnapshotName = Utils.randomResourceName(azureResourceManager, "snp" + "-", 10);
            Snapshot osSnapshot = azureResourceManager.snapshots().define(managedOSSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromDisk(osDisk)
                    .create();

            System.out.println("Created snapshot [from managed OS disk]");

            System.out.println("Creating managed OS disk [from snapshot]");

            // Create a managed disk from the managed snapshot for the OS disk
            final String managedNewOSDiskName = Utils.randomResourceName(azureResourceManager, "dsk" + "-", 10);
            Disk newOSDisk = azureResourceManager.disks().define(managedNewOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromSnapshot(osSnapshot)
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created managed OS disk [from snapshot]");

            System.out.println("Creating managed data snapshot [from managed data disk]");

            // Create a managed snapshot for a data disk
            final String managedDataDiskSnapshotName = Utils.randomResourceName(azureResourceManager, "dsk" + "-", 10);
            Snapshot dataSnapshot = azureResourceManager.snapshots().define(managedDataDiskSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withDataFromDisk(dataDisks.get(0))
                    .withSku(SnapshotSkuType.STANDARD_LRS)
                    .create();

            System.out.println("Created managed data snapshot [from managed data disk]");

            System.out.println("Creating managed data disk [from managed snapshot]");

            // Create a managed disk from the managed snapshot for the data disk
            final String managedNewDataDiskName = Utils.randomResourceName(azureResourceManager, "dsk" + "-", 10);
            Disk newDataDisk = azureResourceManager.disks().define(managedNewDataDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .fromSnapshot(dataSnapshot)
                    .create();

            System.out.println("Created managed data disk [from managed snapshot]");

            System.out.println("Creating VM [with specialized OS managed disk]");

            final String linuxVM6Name = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
            VirtualMachine linuxVM6 = azureResourceManager.virtualMachines().define(linuxVM6Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSDisk(newOSDisk, OperatingSystemTypes.LINUX)
                    .withExistingDataDisk(newDataDisk)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [with specialized OS managed disk]: " + linuxVM6.id());

            // ::== Migrate a VM to managed disks with a single reboot

            System.out.println("Creating VM [with un-managed disk for migration]");

            final String linuxVM7Name = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
            final String linuxVM7Pip = Utils.randomResourceName(azureResourceManager, "pip" + "-", 18);
            VirtualMachine linuxVM7 = azureResourceManager.virtualMachines().define(linuxVM7Name)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(linuxVM7Pip)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withSsh(sshkey)
                    .withUnmanagedDisks() // uses storage accounts
                    .withNewUnmanagedDataDisk(50)
                    .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                    .create();

            System.out.println("Created VM [with un-managed disk for migration]");

            System.out.println("De-allocating VM :" + linuxVM7.id());

            linuxVM7.deallocate();

            System.out.println("De-allocated VM :" + linuxVM7.id());

            System.out.println("Migrating VM");

            linuxVM7.convertToManaged();

            System.out.println("Migrated VM");

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

    private static VirtualMachine prepareSpecializedUnmanagedVirtualMachine(AzureResourceManager azureResourceManager, Region region, String rgName) {
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
        final String publicIpDnsLabel = Utils.randomResourceName(azureResourceManager, "pip" + "-", 20);

        VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                    .withNewVhd(100)
                    .withLun(1)
                    .attach()
                .defineUnmanagedDataDisk("disk-2")
                    .withNewVhd(50)
                    .withLun(2)
                    .attach()
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        // De-provision the virtual machine
        deprovisionAgentInLinuxVM(linuxVM);
        System.out.println("Deallocate VM: " + linuxVM.id());
        linuxVM.deallocate();
        System.out.println("Deallocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());
        System.out.println("Generalize VM: " + linuxVM.id());
        linuxVM.generalize();
        System.out.println("Generalized VM: " + linuxVM.id());
        return linuxVM;
    }

    private static VirtualMachine prepareSpecializedManagedVirtualMachine(AzureResourceManager azureResourceManager, Region region, String rgName) {
        final String userName = "tirekicker";
        final String sshPublicKey = Utils.sshPublicKey();
        final String linuxVMName1 = Utils.randomResourceName(azureResourceManager, "vm" + "-", 10);
        final String publicIPDnsLabel = Utils.randomResourceName(azureResourceManager, "pip" + "-", 20);

        VirtualMachine linuxVM = azureResourceManager.virtualMachines().define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withSsh(sshPublicKey)
                .withNewDataDisk(100)
                .withNewDataDisk(200)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        // De-provision the virtual machine
        deprovisionAgentInLinuxVM(linuxVM);
        System.out.println("Deallocate VM: " + linuxVM.id());
        linuxVM.deallocate();
        System.out.println("Deallocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());
        System.out.println("Generalize VM: " + linuxVM.id());
        linuxVM.generalize();
        System.out.println("Generalized VM: " + linuxVM.id());
        return linuxVM;
    }

    /**
     * De-provision an Azure linux virtual machine.
     *
     * @param virtualMachine the virtual machine
     */
    protected static void deprovisionAgentInLinuxVM(VirtualMachine virtualMachine) {
        System.out.println("Trying to de-provision");

        virtualMachine.manager().serviceClient().getVirtualMachines().beginRunCommand(
            virtualMachine.resourceGroupName(), virtualMachine.name(),
            new RunCommandInput()
                .withCommandId("RunShellScript")
                .withScript(Collections.singletonList("sudo waagent -deprovision+user --force")));

        // wait as above command will not return as sync
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));
    }

    private static Network prepareNetwork(AzureResourceManager azureResourceManager, Region region, String rgName) {
        final String vnetName = Utils.randomResourceName(azureResourceManager, "vnet", 24);

        Network network = azureResourceManager.networks().define(vnetName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAddressSpace("172.16.0.0/16")
                .defineSubnet("subnet1")
                .withAddressPrefix("172.16.1.0/24")
                .attach()
                .create();
        return network;
    }

    private static LoadBalancer prepareLoadBalancer(AzureResourceManager azureResourceManager, Region region, String rgName) {
        final String loadBalancerName1 = Utils.randomResourceName(azureResourceManager, "intlb" + "-", 18);
        final String frontendName = loadBalancerName1 + "-FE1";
        final String backendPoolName1 = loadBalancerName1 + "-BAP1";
        final String backendPoolName2 = loadBalancerName1 + "-BAP2";
        final String httpProbe = "httpProbe";
        final String httpsProbe = "httpsProbe";
        final String httpLoadBalancingRule = "httpRule";
        final String httpsLoadBalancingRule = "httpsRule";
        final String natPool50XXto22 = "natPool50XXto22";
        final String natPool60XXto23 = "natPool60XXto23";
        final String publicIpName = "pip-" + loadBalancerName1;

        PublicIpAddress publicIPAddress = azureResourceManager.publicIpAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLeafDomainLabel(publicIpName)
                .create();
        LoadBalancer loadBalancer = azureResourceManager.loadBalancers().define(loadBalancerName1)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                // Add two rules that uses above backend and probe
                .defineLoadBalancingRule(httpLoadBalancingRule)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(80)
                    .toBackend(backendPoolName1)
                    .withProbe(httpProbe)
                    .attach()
                .defineLoadBalancingRule(httpsLoadBalancingRule)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPort(443)
                    .toBackend(backendPoolName2)
                    .withProbe(httpsProbe)
                    .attach()
                // Add nat pools to enable direct VM connectivity for
                //  SSH to port 22 and TELNET to port 23
                .defineInboundNatPool(natPool50XXto22)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(5000, 5099)
                    .toBackendPort(22)
                    .attach()
                .defineInboundNatPool(natPool60XXto23)
                    .withProtocol(TransportProtocol.TCP)
                    .fromFrontend(frontendName)
                    .fromFrontendPortRange(6000, 6099)
                    .toBackendPort(23)
                    .attach()
                // Explicitly define a frontend
                .definePublicFrontend(frontendName)
                    .withExistingPublicIpAddress(publicIPAddress)
                    .attach()
                // Add two probes one per rule
                .defineHttpProbe(httpProbe)
                    .withRequestPath("/")
                    .withPort(80)
                    .attach()
                .defineHttpProbe(httpsProbe)
                    .withRequestPath("/")
                    .withPort(443)
                    .attach()

                .create();
        return loadBalancer;
    }

    private ManageManagedDisks() {

    }
}
