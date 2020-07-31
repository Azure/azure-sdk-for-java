// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.samples;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.Azure;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.samples.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * Azure Compute sample for managing virtual machines -
 *  - Create a virtual machine with managed OS Disk based on Windows OS image
 *  - Once Network is created start creation of virtual machine based on Linux OS image in the same network
 *  - Update both virtual machines in parallel
 *    - for Linux based:
 *      - add Tag
 *    - for Windows based:
 *      - add a data disk
 *  - List virtual machines and print details
 *  - Delete all virtual machines.
 */
public final class ManageVirtualMachineAsync {

    /**
     * Main function which runs the actual sample.
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(final Azure azure) {
        final Region region = Region.US_WEST_CENTRAL;
        final String windowsVMName = azure.sdkContext().randomResourceName("wVM", 15);
        final String linuxVMName = azure.sdkContext().randomResourceName("lVM", 15);
        final String rgName = azure.sdkContext().randomResourceName("rgCOMV", 15);
        final String userName = "tirekicker";
        final String password = Utils.password();
        final String windowsVmKey = "WindowsVM";
        final String linuxVmKey = "LinuxVM";

        try {

            //=============================================================
            // Create a Windows virtual machine

            // Prepare a creatable data disk for VM
            //
            final Date t1 = new Date();

            final Creatable<Disk> dataDiskCreatable = azure.disks().define(azure.sdkContext().randomResourceName("dsk-", 15))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100);

            // Create a data disk to attach to VM
            //
            Flux<Indexable> dataDiskFlux = azure.disks().define(azure.sdkContext().randomResourceName("dsk-", 15))
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .createAsync();

            final Map<String, VirtualMachine> createdVms = new TreeMap<>();

            dataDiskFlux.flatMap(
                createdResource -> {
                    if (createdResource instanceof Disk) {
                        System.out.println("Creating a Windows VM");

                        return azure.virtualMachines().define(windowsVMName)
                            .withRegion(region)
                            .withNewResourceGroup(rgName)
                            .withNewPrimaryNetwork("10.0.0.0/28")
                            .withPrimaryPrivateIPAddressDynamic()
                            .withoutPrimaryPublicIPAddress()
                            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                            .withAdminUsername(userName)
                            .withAdminPassword(password)
                            .withNewDataDisk(10)
                            .withNewDataDisk(dataDiskCreatable)
                            .withExistingDataDisk((Disk) createdResource)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .createAsync();
                    }
                    return Flux.just(createdResource);
                }).flatMap(createdResource -> {
                    if (createdResource instanceof Network) {
                        // Once Network object is created we can start creation of Linux VM in the same network
                        Network network = (Network) createdResource;
                        System.out.println("Created Network: " + network.id());

                        System.out.println("Creating a Linux VM in the same network");

                        return azure.virtualMachines().define(linuxVMName)
                            .withRegion(region)
                            .withExistingResourceGroup(rgName)
                            .withExistingPrimaryNetwork(network)
                            .withSubnet("subnet1") // Referencing the default subnet name when no name specified at creation
                            .withPrimaryPrivateIPAddressDynamic()
                            .withoutPrimaryPublicIPAddress()
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withRootPassword(password)
                            .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                            .createAsync();
                    }
                    return Flux.just(createdResource);
                }
            ).map(
                createdResource -> {
                    if (createdResource instanceof VirtualMachine) {
                        Date t2 = new Date();
                        VirtualMachine virtualMachine = (VirtualMachine) createdResource;
                        if (isWindowsVM(virtualMachine)) {
                            createdVms.put(windowsVmKey, virtualMachine);
                            System.out.println("Created Windows VM: "
                                + virtualMachine.id());
                        } else {
                            createdVms.put(linuxVmKey, virtualMachine);
                            System.out.println("Created a Linux VM (in the same virtual network): "
                                + virtualMachine.id());
                        }
                        System.out.println("Virtual machine creation took "
                            + ((t2.getTime() - t1.getTime()) / 1000)
                            + " seconds");
                    }
                    return createdResource;
                }
            ).last().block();

            final VirtualMachine windowsVM = createdVms.get(windowsVmKey);
            final VirtualMachine linuxVM = createdVms.get(linuxVmKey);

            //=============================================================
            // Update virtual machines

            // - Tag the virtual machine on Linux VM
            Mono<VirtualMachine> updateLinuxVMChain = linuxVM.update()
                    .withTag("who-rocks-on-linux", "java")
                    .withTag("where", "on azure")
                    .applyAsync()
                    .map(virtualMachine -> {
                        System.out.println("Tagged Linux VM: " + virtualMachine.id());
                        return virtualMachine;
                    });

            // - Add a data disk on Windows VM.
            Mono<VirtualMachine> updateWindowsVMChain = windowsVM.update()
                    .withNewDataDisk(200)
                    .applyAsync();

            Flux.merge(updateLinuxVMChain, updateWindowsVMChain)
                    .last().block();

            //=============================================================
            // List virtual machines and print details
            azure.virtualMachines().listByResourceGroupAsync(rgName)
                    .map(virtualMachine -> {
                        System.out.println("Retrieved details for VM: " + virtualMachine.id());
                        return virtualMachine;
                    }).last().block();

            //=============================================================
            // Delete the virtual machines in parallel
            Flux.merge(
                    azure.virtualMachines().deleteByIdAsync(windowsVM.id()),
                    azure.virtualMachines().deleteByIdAsync(linuxVM.id()))
                    .singleOrEmpty().block();

            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName)
                        .block();
                System.out.println("Deleted Resource Group: " + rgName);
            } catch (NullPointerException npe) {
                System.out.println("Did not create any resources in Azure. No clean up is necessary");
            } catch (Exception g) {
                g.printStackTrace();
            }
        }
        return false;
    }

    private static boolean isWindowsVM(VirtualMachine vm) {
        if (vm != null && vm.osProfile() != null && vm.osProfile().windowsConfiguration() != null) {
            return true;
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

    private ManageVirtualMachineAsync() {

    }
}
