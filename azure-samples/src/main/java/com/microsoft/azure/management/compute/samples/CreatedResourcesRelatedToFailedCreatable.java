/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.samples;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroup;
import com.microsoft.azure.management.resources.fluentcore.dag.TaskGroupEntry;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreateUpdateTask;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.LogLevel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import rx.Observable;
import rx.functions.Func1;

/**
 * Azure compute sample for creating multiple virtual machines in parallel.
 */
public final class CreatedResourcesRelatedToFailedCreatable {

    /**
     * Main function which runs the actual sample.
     *
     * @param azure instance of the azure client
     * @return true if sample runs successfully
     */
    public static boolean runSample(Azure azure) {
        final String rgName = SdkContext.randomResourceName("rgCOPD", 24);
        final String userName = "tirekicker";
        final String sshKey = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSPC2K7LZcFKEO+/t3dzmQYtrJFZNxOsbVgOVKietqHyvmYGHEC0J2wPdAqQ/63g/hhAEFRoyehM+rbeDri4txB3YFfnOK58jqdkyXzupWqXzOrlKY4Wz9SKjjN765+dqUITjKRIaAip1Ri137szRg71WnrmdP3SphTRlCx1Bk2nXqWPsclbRDCiZeF8QOTi4JqbmJyK5+0UqhqYRduun8ylAwKKQJ1NJt85sYIHn9f1Rfr6Tq2zS0wZ7DHbZL+zB5rSlAr8QyUdg/GQD+cmSs6LvPJKL78d6hMGk84ARtFo4A79ovwX/Fj01znDQkU6nJildfkaolH2rWFG/qttD azjava@javalib.com";

        Map<Region, Integer> virtualMachinesByLocation = new HashMap<Region, Integer>();

        virtualMachinesByLocation.put(Region.US_EAST, 2);
        // virtualMachinesByLocation.put(Region.US_SOUTH_CENTRAL, 12);
        // virtualMachinesByLocation.put(Region.US_WEST, 12);
        // virtualMachinesByLocation.put(Region.US_NORTH_CENTRAL, 12);
        // virtualMachinesByLocation.put(Region.BRAZIL_SOUTH, 5);
        // virtualMachinesByLocation.put(Region.EUROPE_NORTH, 5);
        // virtualMachinesByLocation.put(Region.EUROPE_WEST, 5);
        // virtualMachinesByLocation.put(Region.UK_WEST, 5);
        // virtualMachinesByLocation.put(Region.ASIA_SOUTHEAST, 5);
        // virtualMachinesByLocation.put(Region.INDIA_SOUTH, 5);
        // virtualMachinesByLocation.put(Region.JAPAN_EAST, 5);
        // virtualMachinesByLocation.put(Region.JAPAN_WEST, 5);

        try {

            //=============================================================
            // Create a resource group (Where all resources gets created)
            //
            ResourceGroup resourceGroup = azure.resourceGroups().define(rgName)
                    .withRegion(Region.US_EAST)
                    .create();

            System.out.println("Created a new resource group - " + resourceGroup.id());

            List<String> publicIpCreatableKeys = new ArrayList<>();
            // Prepare a batch of Creatable definitions
            //
            List<Creatable<VirtualMachine>> creatableVirtualMachines = new ArrayList<>();

            for (Map.Entry<Region, Integer> entry : virtualMachinesByLocation.entrySet()) {
                Region region = entry.getKey();
                Integer vmCount = entry.getValue();

                //=============================================================
                // Create 1 network creatable per region
                // Prepare Creatable Network definition (Where all the virtual machines get added to)
                //
                String networkName = SdkContext.randomResourceName("vnetCOPD-", 20);
                Creatable<Network> networkCreatable = azure.networks().define(networkName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup)
                        .withAddressSpace("172.16.0.0/16");

                //=============================================================
                // Create 1 storage creatable per region (For storing VMs disk)
                //
                String storageAccountName = SdkContext.randomResourceName("stgcopd", 20);
                Creatable<StorageAccount> storageAccountCreatable = azure.storageAccounts().define(storageAccountName)
                        .withRegion(region)
                        .withExistingResourceGroup(resourceGroup);

                String linuxVMNamePrefix = SdkContext.randomResourceName("vm-", 15);
                for (int i = 1; i <= vmCount; i++) {

                    //=============================================================
                    // Create 1 public IP address creatable
                    //
                    Creatable<PublicIPAddress> publicIPAddressCreatable = azure.publicIPAddresses()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroup)
                            .withLeafDomainLabel(SdkContext.randomResourceName("pip", 10));

                    publicIpCreatableKeys.add(publicIPAddressCreatable.key());

                    //=============================================================
                    // Create 1 virtual machine creatable
                    Creatable<VirtualMachine> virtualMachineCreatable = azure.virtualMachines()
                            .define(String.format("%s-%d", linuxVMNamePrefix, i))
                            .withRegion(region)
                            .withExistingResourceGroup(resourceGroup)
                            .withNewPrimaryNetwork(networkCreatable)
                            .withPrimaryPrivateIPAddressDynamic()
                            .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                            .withRootUsername(userName)
                            .withSsh(sshKey)
                            .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                            .withNewStorageAccount(storageAccountCreatable);
                    creatableVirtualMachines.add(virtualMachineCreatable);
                }
            }

            //=============================================================
            // Create Asynchronously

            StopWatch stopwatch = new StopWatch();
            System.out.println("Creating the virtual machines");
            stopwatch.start();

            final List<String> createdVirtualMachineKeys = new ArrayList<>();
            azure.virtualMachines().createAsync(creatableVirtualMachines)
                    .map(new Func1<Indexable, Indexable>() {
                        @Override
                        public Indexable call(Indexable createdResource) {
                            if (createdResource instanceof Resource) {
                                Resource resource = (Resource) createdResource;
                                System.out.println("Created: " + resource.id());
                                if (resource instanceof VirtualMachine) {
                                    VirtualMachine virtualMachine = (VirtualMachine) resource;
                                    createdVirtualMachineKeys.add(virtualMachine.key());
                                }
                            }
                            return createdResource;
                        }
                    })
                    .onErrorResumeNext(new Func1<Throwable, Observable<? extends Indexable>>() {
                        // To avoid toBlocking() from throwing on error and terminating the process,
                        // we should handle any exception and replace it with an empty observable.
                        //
                        @Override
                        public Observable<? extends Indexable> call(Throwable throwable) {
                            throwable.printStackTrace();
                            return Observable.just(null);
                        }
                    }).toBlocking().last();

            // STAGE-1: COLLECT RELATED CREATED RESOURCES WHICH "MAY BE UNUSED"
            //
            final HashMap<String, Indexable> mayBeUnusedResources = new HashMap<>();
            for (Creatable<VirtualMachine> creatableVirtualMachine : creatableVirtualMachines) {
                if (!createdVirtualMachineKeys.contains(creatableVirtualMachine.key())) {
                    // if a creatable virtual machine's key not found in the list of created virtual
                    // machine keys then it means it is failed to create and there may be left with
                    // successfully created dependency (related) resources.
                    //

                    // Get into dependency graph and visit the underlying sub-graph with root as failed
                    // virtual machine to identify the created dependency (related) resources.
                    //
                    TaskGroup.HasTaskGroup<Indexable, CreateUpdateTask<Indexable>> hasGroup = (TaskGroup.HasTaskGroup<Indexable, CreateUpdateTask<Indexable>>) creatableVirtualMachine;
                    TaskGroup<Indexable, CreateUpdateTask<Indexable>> taskGroup = hasGroup.taskGroup();
                    taskGroup.prepare();
                    TaskGroupEntry<Indexable, CreateUpdateTask<Indexable>> next = taskGroup.getNext();
                    if (next != null) {
                        do {
                            Indexable dependencyResource = next.taskResult();
                            if (dependencyResource != null) {
                                // This is a resource created as part of attempt to create virtual machine (but failed)
                                // but at this point we are not sure this resource is also shared with a successfully
                                // created virtual machine. We will use another iteration to filter out shared dependencies.
                                mayBeUnusedResources.put(dependencyResource.key(), dependencyResource);
                            }
                            taskGroup.reportCompletion(next);
                            next = taskGroup.getNext();
                        } while (next != null);
                    }
                }
            }


            //STAGE-2:  FILTER OUT "USED" RESOURCES
            //
            for (Creatable<VirtualMachine> creatableVirtualMachine : creatableVirtualMachines) {
                if (createdVirtualMachineKeys.contains(creatableVirtualMachine.key())) {
                    // Get into dependency graph and visit the underlying sub-graph with root as successfully
                    // created virtual machine to identify the created dependency resources.
                    //
                    TaskGroup.HasTaskGroup<Indexable, CreateUpdateTask<Indexable>> hasGroup = (TaskGroup.HasTaskGroup<Indexable, CreateUpdateTask<Indexable>>) creatableVirtualMachine;
                    TaskGroup<Indexable, CreateUpdateTask<Indexable>> taskGroup = hasGroup.taskGroup();
                    taskGroup.prepare();
                    TaskGroupEntry<Indexable, CreateUpdateTask<Indexable>> next = taskGroup.getNext();
                    if (next != null) {
                        do {
                            Indexable dependencyResource = next.taskResult();
                            if (dependencyResource != null) {
                                // If this resource is shared between failed virtual machine and successfully
                                // created this virtual machine then remove it from the map.
                                if (mayBeUnusedResources.containsKey(dependencyResource.key())) {
                                    mayBeUnusedResources.remove(dependencyResource.key());
                                }
                            }
                            taskGroup.reportCompletion(next);
                            next = taskGroup.getNext();
                        } while (next != null);
                    }
                }
            }

            System.out.println("Following resources are created but not used by any virtual machines");
            //
            for (Indexable unusedResource : mayBeUnusedResources.values()) {
                if (unusedResource instanceof Resource) {
                    Resource resource = (Resource) unusedResource;
                    System.out.println(resource.id());
                }
            }

            stopwatch.stop();
            return true;
        } catch (Exception f) {

            System.out.println(f.getMessage());
            f.printStackTrace();

        } finally {

            try {
                System.out.println("Deleting Resource Group: " + rgName);
                azure.resourceGroups().deleteByName(rgName);
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
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {

            //=============================================================
            // Authenticate
            //
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));

            Azure azure = Azure
                    .configure()
                    .withLogLevel(LogLevel.NONE)
                    .authenticate(credFile)
                    .withDefaultSubscription();

            // Print selected subscription
            //
            System.out.println("Selected subscription: " + azure.subscriptionId());

            runSample(azure);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    private CreatedResourcesRelatedToFailedCreatable() {

    }
}