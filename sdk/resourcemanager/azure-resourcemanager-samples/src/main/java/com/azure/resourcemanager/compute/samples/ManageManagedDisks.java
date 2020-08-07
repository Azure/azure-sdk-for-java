// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import com.jcraft.jsch.JSchException;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
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
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.samples.SSHShell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is sample will not be published, this is just to ensure out blog is honest.
 */
public final class ManageManagedDisks {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final Region region = Region.US_SOUTH_CENTRAL;
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String userName = "tirekicker";
        final String sshkey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        try {

            // ::==Create a VM
            // Create a virtual machine with an implicit Managed OS disk and explicit Managed data disk

            System.out.println("Creating VM [with an implicit Managed OS disk and explicit Managed data disk]");

            final String linuxVM1Name = azure.sdkContext().randomResourceName("vm" + "-", 18);
            final String linuxVM1Pip = azure.sdkContext().randomResourceName("pip" + "-", 18);
            VirtualMachine linuxVM1 = azure.virtualMachines()
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
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created VM [with an implicit Managed OS disk and explicit Managed data disk]: " + linuxVM1.id());

            // Creation is simplified with implicit creation of managed disks without specifying all the disk details. You will notice that you do not require storage accounts
            // ::== Update the VM
            // Create a VMSS with implicit managed OS disks and explicit managed data disks

            System.out.println("Creating VMSS [with implicit managed OS disks and explicit managed data disks]");

            final String vmScaleSetName = azure.sdkContext().randomResourceName("vmss" + "-", 18);
            VirtualMachineScaleSet vmScaleSet = azure.virtualMachineScaleSets()
                    .define(vmScaleSetName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_D5_V2)
                        .withExistingPrimaryNetworkSubnet(prepareNetwork(azure, region, rgName), "subnet1")
                        .withExistingPrimaryInternetFacingLoadBalancer(prepareLoadBalancer(azure, region, rgName))
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

            azure.virtualMachineScaleSets().deleteById(vmScaleSet.id());

            // Create an empty disk and attach to a VM (Manage Virtual Machine With Disk)

            System.out.println("Creating empty data disk [to attach to a VM]");

            final String diskName = azure.sdkContext().randomResourceName("dsk" + "-", 18);
            Disk dataDisk = azure.disks().define(diskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created empty data disk [to attach to a VM]");

            System.out.println("Creating VM [with new managed data disks and disk attached]");

            final String linuxVM2Name = azure.sdkContext().randomResourceName("vm" + "-", 10);
            final String linuxVM2Pip = azure.sdkContext().randomResourceName("pip" + "-", 18);
            VirtualMachine linuxVM2 = azure.virtualMachines().define(linuxVM2Name)
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
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
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

            final VirtualMachine linuxVM = prepareSpecializedUnmanagedVirtualMachine(azure, region, rgName);

            System.out.println("Prepared specialized virtual machine with un-managed disk");

            System.out.println("Creating custom image from specialized virtual machine");

            final String customImageName = azure.sdkContext().randomResourceName("cimg" + "-", 10);
            VirtualMachineCustomImage virtualMachineCustomImage = azure.virtualMachineCustomImages()
                    .define(customImageName)
                        .withRegion(region)
                        .withExistingResourceGroup(rgName)
                        .fromVirtualMachine(linuxVM) // from a deallocated and generalized VM
                        .create();

            System.out.println("Created custom image from specialized virtual machine");

            System.out.println("Creating VM [from custom image]");

            final String linuxVM3Name = azure.sdkContext().randomResourceName("vm" + "-", 10);
            VirtualMachine linuxVM3 = azure.virtualMachines().define(linuxVM3Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withGeneralizedLinuxCustomImage(virtualMachineCustomImage.id())
                    .withRootUsername(userName)
                    .withSsh(sshkey)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created VM [from custom image]: " + linuxVM3.id());

            // Create a VM from a VHD (Create Virtual Machine Using Specialized VHD)

            final String linuxVMName4 = azure.sdkContext().randomResourceName("vm" + "-", 10);
            final String specializedVhd = linuxVM.osUnmanagedDiskVhdUri();

            azure.virtualMachines().deleteById(linuxVM.id());

            System.out.println("Creating VM [by attaching un-managed disk]");

            VirtualMachine linuxVM4 = azure.virtualMachines().define(linuxVMName4)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSUnmanagedDisk(specializedVhd, OperatingSystemTypes.LINUX)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created VM [by attaching un-managed disk]: " + linuxVM4.id());

            // Create a Snapshot (Create Virtual Machine using specialized disks from snapshot)

            System.out.println("Preparing specialized virtual machine with managed disks");

            final VirtualMachine linuxVM5 = prepareSpecializedManagedVirtualMachine(azure, region, rgName);
            Disk osDisk = azure.disks().getById(linuxVM5.osDiskId());
            List<Disk> dataDisks = new ArrayList<>();
            for (VirtualMachineDataDisk disk : linuxVM5.dataDisks().values()) {
                Disk d = azure.disks().getById(disk.id());
                dataDisks.add(d);
            }

            System.out.println("Prepared specialized virtual machine with managed disks");

            System.out.println("Deleting VM: " + linuxVM5.id());
            azure.virtualMachines().deleteById(linuxVM5.id());
            System.out.println("Deleted the VM: " + linuxVM5.id());

            System.out.println("Creating snapshot [from managed OS disk]");

            // Create a managed snapshot for an OS disk
            final String managedOSSnapshotName = azure.sdkContext().randomResourceName("snp" + "-", 10);
            Snapshot osSnapshot = azure.snapshots().define(managedOSSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromDisk(osDisk)
                    .create();

            System.out.println("Created snapshot [from managed OS disk]");

            System.out.println("Creating managed OS disk [from snapshot]");

            // Create a managed disk from the managed snapshot for the OS disk
            final String managedNewOSDiskName = azure.sdkContext().randomResourceName("dsk" + "-", 10);
            Disk newOSDisk = azure.disks().define(managedNewOSDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withLinuxFromSnapshot(osSnapshot)
                    .withSizeInGB(50)
                    .create();

            System.out.println("Created managed OS disk [from snapshot]");

            System.out.println("Creating managed data snapshot [from managed data disk]");

            // Create a managed snapshot for a data disk
            final String managedDataDiskSnapshotName = azure.sdkContext().randomResourceName("dsk" + "-", 10);
            Snapshot dataSnapshot = azure.snapshots().define(managedDataDiskSnapshotName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withDataFromDisk(dataDisks.get(0))
                    .withSku(DiskSkuTypes.STANDARD_LRS)
                    .create();

            System.out.println("Created managed data snapshot [from managed data disk]");

            System.out.println("Creating managed data disk [from managed snapshot]");

            // Create a managed disk from the managed snapshot for the data disk
            final String managedNewDataDiskName = azure.sdkContext().randomResourceName("dsk" + "-", 10);
            Disk newDataDisk = azure.disks().define(managedNewDataDiskName)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .fromSnapshot(dataSnapshot)
                    .create();

            System.out.println("Created managed data disk [from managed snapshot]");

            System.out.println("Creating VM [with specialized OS managed disk]");

            final String linuxVM6Name = azure.sdkContext().randomResourceName("vm" + "-", 10);
            VirtualMachine linuxVM6 = azure.virtualMachines().define(linuxVM6Name)
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withSpecializedOSDisk(newOSDisk, OperatingSystemTypes.LINUX)
                    .withExistingDataDisk(newDataDisk)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created VM [with specialized OS managed disk]: " + linuxVM6.id());

            // ::== Migrate a VM to managed disks with a single reboot

            System.out.println("Creating VM [with un-managed disk for migration]");

            final String linuxVM7Name = azure.sdkContext().randomResourceName("vm" + "-", 10);
            final String linuxVM7Pip = azure.sdkContext().randomResourceName("pip" + "-", 18);
            VirtualMachine linuxVM7 = azure.virtualMachines().define(linuxVM7Name)
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
                    .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                    .create();

            System.out.println("Created VM [with un-managed disk for migration]");

            System.out.println("De-allocating VM :" + linuxVM7.id());

            linuxVM7.deallocate();

            System.out.println("De-allocated VM :" + linuxVM7.id());

            System.out.println("Migrating VM");

            linuxVM7.convertToManaged();

            System.out.println("Migrated VM");

            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

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

    private static VirtualMachine prepareSpecializedUnmanagedVirtualMachine(Azure azure, Region region, String rgName) {
        final String userName = "tirekicker";
        final String password = Utils.password();
        final String linuxVMName1 = azure.sdkContext().randomResourceName("vm" + "-", 10);
        final String publicIpDnsLabel = azure.sdkContext().randomResourceName("pip" + "-", 20);

        VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withRootPassword(password)
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk-1")
                    .withNewVhd(100)
                    .withLun(1)
                    .attach()
                .defineUnmanagedDataDisk("disk-2")
                    .withNewVhd(50)
                    .withLun(2)
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        // De-provision the virtual machine
        deprovisionAgentInLinuxVM(linuxVM.getPrimaryPublicIPAddress().fqdn(), 22, userName, password);
        System.out.println("Deallocate VM: " + linuxVM.id());
        linuxVM.deallocate();
        System.out.println("Deallocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());
        System.out.println("Generalize VM: " + linuxVM.id());
        linuxVM.generalize();
        System.out.println("Generalized VM: " + linuxVM.id());
        return linuxVM;
    }

    private static VirtualMachine prepareSpecializedManagedVirtualMachine(Azure azure, Region region, String rgName) {
        final String userName = "tirekicker";
        final String password = Utils.password();
        final String linuxVMName1 = azure.sdkContext().randomResourceName("vm" + "-", 10);
        final String publicIPDnsLabel = azure.sdkContext().randomResourceName("pip" + "-", 20);

        VirtualMachine linuxVM = azure.virtualMachines().define(linuxVMName1)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress(publicIPDnsLabel)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername(userName)
                .withRootPassword(password)
                .withNewDataDisk(100)
                .withNewDataDisk(200)
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        // De-provision the virtual machine
        deprovisionAgentInLinuxVM(linuxVM.getPrimaryPublicIPAddress().fqdn(), 22, userName, password);
        System.out.println("Deallocate VM: " + linuxVM.id());
        linuxVM.deallocate();
        System.out.println("Deallocated VM: " + linuxVM.id() + "; state = " + linuxVM.powerState());
        System.out.println("Generalize VM: " + linuxVM.id());
        linuxVM.generalize();
        System.out.println("Generalized VM: " + linuxVM.id());
        return linuxVM;
    }

    private static void deprovisionAgentInLinuxVM(String host, int port, String userName, String password) {
        SSHShell shell = null;
        try {
            System.out.println("Trying to de-provision: " + host);
            shell = SSHShell.open(host, port, userName, password);
            List<String> deprovisionCommand = new ArrayList<>();
            deprovisionCommand.add("sudo waagent -deprovision+user --force");
            String output = shell.runCommands(deprovisionCommand);
            System.out.println(output);
        } catch (JSchException jSchException) {
            System.out.println(jSchException.getMessage());
        } catch (IOException ioException) {
            System.out.println(ioException.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } finally {
            if (shell != null) {
                shell.close();
            }
        }
    }

    private static Network prepareNetwork(Azure azure, Region region, String rgName) {
        final String vnetName = azure.sdkContext().randomResourceName("vnet", 24);

        Network network = azure.networks().define(vnetName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAddressSpace("172.16.0.0/16")
                .defineSubnet("subnet1")
                .withAddressPrefix("172.16.1.0/24")
                .attach()
                .create();
        return network;
    }

    private static LoadBalancer prepareLoadBalancer(Azure azure, Region region, String rgName) {
        final String loadBalancerName1 = azure.sdkContext().randomResourceName("intlb" + "-", 18);
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

        PublicIpAddress publicIPAddress = azure.publicIpAddresses().define(publicIpName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withLeafDomainLabel(publicIpName)
                .create();
        LoadBalancer loadBalancer = azure.loadBalancers().define(loadBalancerName1)
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
