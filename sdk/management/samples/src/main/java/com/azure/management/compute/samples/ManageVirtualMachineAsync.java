/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.Disk;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.samples.Utils;
import com.microsoft.rest.LogLevel;
import rx.Observable;
import rx.functions.Func1;

import java.io.File;
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
        final String windowsVMName = Utils.createRandomName("wVM");
        final String linuxVMName = Utils.createRandomName("lVM");
        final String rgName = Utils.createRandomName("rgCOMV");
        final String userName = "tirekicker";
        // [SuppressMessage("Microsoft.Security", "CS002:SecretInNextLine", Justification="Serves as an example, not for deployment. Please change when using this in your code.")]
        final String password = "12NewPA$$w0rd!";
        final String windowsVmKey = "WindowsVM";
        final String linuxVmKey = "LinuxVM";

        try {

            //=============================================================
            // Create a Windows virtual machine

            // Prepare a creatable data disk for VM
            //
            final Date t1 = new Date();

            final Creatable<Disk> dataDiskCreatable = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withExistingResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(100);

            // Create a data disk to attach to VM
            //
            Observable<Indexable> dataDiskObservable = azure.disks().define(Utils.createRandomName("dsk-"))
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withData()
                    .withSizeInGB(50)
                    .createAsync();

            final Map<String, VirtualMachine> createdVms = new TreeMap<>();

            dataDiskObservable.flatMap(new Func1<Indexable, Observable<Indexable>>() {
                        @Override
                        public Observable<Indexable> call(Indexable createdResource) {
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
                            return Observable.just(createdResource);
                        }
                    }).flatMap(new Func1<Indexable, Observable<Indexable>>() {
                        @Override
                        public Observable<Indexable> call(Indexable createdResource) {
                            if (createdResource instanceof Network) {
                                // Once Network object is created we can start creation of Linux VM in the same network
                                Network network = (Network) createdResource;
                                System.out.println("Created Network");
                                Utils.print(network);
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
                            return Observable.just(createdResource);
                        }
                    }).map(new Func1<Indexable, Indexable>() {
                        @Override
                        public Indexable call(Indexable createdResource) {
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
                                // Print virtual machine details
                                Utils.print(virtualMachine);
                            }
                            return createdResource;
                        }
                    }).toBlocking().subscribe();

            final VirtualMachine windowsVM = createdVms.get(windowsVmKey);
            final VirtualMachine linuxVM = createdVms.get(linuxVmKey);

            //=============================================================
            // Update virtual machines

            // - Tag the virtual machine on Linux VM
            Observable<VirtualMachine> updateLinuxVMChain = linuxVM.update()
                    .withTag("who-rocks-on-linux", "java")
                    .withTag("where", "on azure")
                    .applyAsync()
                    .map(new Func1<VirtualMachine, VirtualMachine>() {
                        @Override
                        public VirtualMachine call(VirtualMachine virtualMachine) {
                            System.out.println("Tagged Linux VM: " + virtualMachine.id());
                            return virtualMachine;
                        }
                    });

            // - Add a data disk on Windows VM.
            Observable<VirtualMachine> updateWindowsVMChain = windowsVM.update()
                    .withNewDataDisk(200)
                    .applyAsync();

            Observable.merge(updateLinuxVMChain, updateWindowsVMChain)
                    .toBlocking().subscribe();

            //=============================================================
            // List virtual machines and print details
            azure.virtualMachines().listByResourceGroupAsync(rgName)
                    .map(new Func1<VirtualMachine, VirtualMachine>() {
                        @Override
                        public VirtualMachine call(VirtualMachine virtualMachine) {
                            System.out.println("Retrieved details for VM: " + virtualMachine.id());
                            Utils.print(virtualMachine);
                            return virtualMachine;
                        }
                    }).toBlocking().subscribe();

            //=============================================================
            // Delete the virtual machines in parallel
            Observable.merge(
                    azure.virtualMachines().deleteByIdAsync(windowsVM.id()).toObservable(),
                    azure.virtualMachines().deleteByIdAsync(linuxVM.id()).toObservable())
                    .toBlocking().subscribe();

            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {
            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByNameAsync(rgName)
                        .await();
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

            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure.configure()
                    .withLogLevel(LogLevel.BASIC)
                    .authenticate(credFile)
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
