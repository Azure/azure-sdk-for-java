/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.core.TestBase;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import rx.Completable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VirtualMachineRelatedResourcesDeletionTests extends ComputeManagementTest {
    public VirtualMachineRelatedResourcesDeletionTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    private static String RG_NAME = "";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        if (resourceManager != null) {
            resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
        }
    }

    @Test
    public void canDeleteRelatedResourcesFromFailedParallelVMCreations() {
        final int desiredVMCount = 40;
        final Region region = Region.US_EAST;
        final String resourceGroupName = RG_NAME;

        // Create one resource group for everything, to ensure no reliance on resource groups
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(resourceGroupName).withRegion(region).create();

        // Needed for tracking related resources
        final Map<String, Collection<Creatable<? extends Resource>>> vmNonNicResourceDefinitions = new HashMap<>();
        final Map<String, Creatable<NetworkInterface>> nicDefinitions = new HashMap<>(); // Tracking NICs separately because they have to be deleted first
        final Map<String, Creatable<VirtualMachine>> vmDefinitions = new HashMap<>();
        final Map<String, String> createdResourceIds = new HashMap<>();
        final List<Throwable> errors = new ArrayList<>();

        // Prepare a number of VM definitions along with their related resource definitions
        for (int i = 0; i < desiredVMCount; i++) {
            Collection<Creatable<? extends Resource>> relatedDefinitions = new ArrayList<>();

            // Define a network for each VM
            String networkName = SdkContext.randomResourceName("net", 14);
            Creatable<Network> networkDefinition = networkManager.networks().define(networkName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0." + i + ".0/29");
            relatedDefinitions.add(networkDefinition);

            // Define a PIP for each VM
            String pipName = SdkContext.randomResourceName("pip", 14);
            PublicIPAddress.DefinitionStages.WithCreate pipDefinition = this.networkManager.publicIPAddresses().define(pipName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(pipDefinition);

            // Define a NIC for each VM
            String nicName = SdkContext.randomResourceName("nic", 14);
            Creatable<NetworkInterface> nicDefinition = networkManager.networkInterfaces().define(nicName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetwork(networkDefinition)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipDefinition);

            // Define a storage account for each VM
            String storageAccountName = SdkContext.randomResourceName("st", 14);
            Creatable<StorageAccount> storageAccountDefinition = storageManager.storageAccounts().define(storageAccountName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(storageAccountDefinition);

            // Define an availability set for each VM
            String availabilitySetName = SdkContext.randomResourceName("as", 14);
            Creatable<AvailabilitySet> availabilitySetDefinition = computeManager.availabilitySets().define(availabilitySetName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(availabilitySetDefinition);

            String vmName = SdkContext.randomResourceName("vm", 14);

            // Define a VM
            String userName;
            if (i == desiredVMCount/2) {
                // Intentionally cause a failure in one of the VMs
                userName = "";
            } else {
                userName = "tester";
            }
            Creatable<VirtualMachine> vmDefinition = computeManager.virtualMachines().define(vmName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetworkInterface(nicDefinition)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456!")
                    .withNewStorageAccount(storageAccountDefinition)
                    .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
                    .withNewAvailabilitySet(availabilitySetDefinition);

            // Keep track of all the related resource definitions based on the VM definition
            vmNonNicResourceDefinitions.put(vmDefinition.key(), relatedDefinitions);
            nicDefinitions.put(vmDefinition.key(), nicDefinition);
            vmDefinitions.put(vmDefinition.key(), vmDefinition);
        }

        // Start the parallel creation of everything
        computeManager.virtualMachines().createAsync(new ArrayList<>(vmDefinitions.values()))
           .map(new Func1<Indexable, Indexable>() {
                @Override
                public Indexable call(Indexable createdResource) {
                    if (createdResource instanceof Resource) {
                        Resource resource = (Resource) createdResource;
                        System.out.println("Created: " + resource.id());
                        if (resource instanceof VirtualMachine) {
                            VirtualMachine virtualMachine = (VirtualMachine) resource;

                            // Record that this VM was created successfully
                            vmDefinitions.remove(virtualMachine.key());

                            // Remove the associated resources from cleanup list
                            vmNonNicResourceDefinitions.remove(virtualMachine.key());

                            // Remove the associated NIC from cleanup list
                            nicDefinitions.remove(virtualMachine.key());
                        } else {
                            // Add this related resource to potential cleanup list
                            createdResourceIds.put(resource.key(), resource.id());
                        }
                    }
                    return createdResource;
                }
           })
           .onErrorReturn(new Func1<Throwable, Indexable>() {
                @Override
                public Indexable call(Throwable throwable) {
                    errors.add(throwable);
                    return null;
                }
            }).toBlocking().last();

        // Delete remaining successfully created NICs of failed VM creations
        Collection<String> nicIdsToDelete = new ArrayList<>();
        for (Creatable<NetworkInterface> nicDefinition : nicDefinitions.values()) {
            String nicId = createdResourceIds.get(nicDefinition.key());
            if (nicId != null) {
                nicIdsToDelete.add(nicId);
            }
        }
        if (!nicIdsToDelete.isEmpty()) {
            networkManager.networkInterfaces().deleteByIds(nicIdsToDelete);
        }

        // Delete remaining successfully created resources of failed VM creations
        Collection<Completable> deleteObservables = new ArrayList<>();
        for (Collection<Creatable<? extends Resource>> relatedResources : vmNonNicResourceDefinitions.values()) {
            for (Creatable<? extends Resource> resource : relatedResources) {
                String createdResourceId = createdResourceIds.get(resource.key());
                if (createdResourceId != null) {
                    deleteObservables.add(resourceManager.genericResources().deleteByIdAsync(createdResourceId));
                }
            }
        }

        // Delete as much as possible, postponing the errors till the end
        Completable.mergeDelayError(deleteObservables).await();

        // Show any errors
        for (Throwable error : errors) {
            System.out.println("\n### ERROR ###\n");
            if (error instanceof CloudException) {
                CloudException ce = (CloudException) error;
                System.out.println("CLOUD EXCEPTION: " + ce.getMessage());
            } else {
                error.printStackTrace();
            }
        }

        System.out.println("Number of failed/cleaned up VM creations: " + vmNonNicResourceDefinitions.size());

        // Verifications
        final int successfulVMCount = desiredVMCount - vmNonNicResourceDefinitions.size();
        final int actualVMCount = computeManager.virtualMachines().listByResourceGroup(resourceGroupName).size();
        System.out.println("Number of actual successful VMs: " + actualVMCount);

        Assert.assertEquals(successfulVMCount, actualVMCount);
        final int actualNicCount = networkManager.networkInterfaces().listByResourceGroup(resourceGroupName).size();
        Assert.assertEquals(successfulVMCount, actualNicCount);
        final int actualNetworkCount = networkManager.networks().listByResourceGroup(resourceGroupName).size();
        Assert.assertEquals(successfulVMCount, actualNetworkCount);
        final int actualPipCount = networkManager.publicIPAddresses().listByResourceGroup(resourceGroupName).size();
        Assert.assertEquals(successfulVMCount, actualPipCount);
        final int actualAvailabilitySetCount = computeManager.availabilitySets().listByResourceGroup(resourceGroupName).size();
        Assert.assertEquals(successfulVMCount, actualAvailabilitySetCount);
        final int actualStorageAccountCount = storageManager.storageAccounts().listByResourceGroup(resourceGroupName).size();
        Assert.assertEquals(successfulVMCount, actualStorageAccountCount);

        // Verify that at least one VM failed.
        // TODO: Ideally only one, but today the internal RX logic terminates eagerly -- need to change that for parallel creation to terminate more "lazily" in the future
        Assert.assertTrue(successfulVMCount < desiredVMCount);
    }
 }
