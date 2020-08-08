// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.core.TestUtilities;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.storage.models.StorageAccount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class VirtualMachineRelatedResourcesDeletionTests extends ComputeManagementTest {
    public VirtualMachineRelatedResourcesDeletionTests() {
        super(TestBase.RunCondition.LIVE_ONLY);
    }

    private String rgName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        if (resourceManager != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canDeleteRelatedResourcesFromFailedParallelVMCreations() {
        final int desiredVMCount = 40;
        final Region region = Region.US_EAST;
        final String resourceGroupName = rgName;

        // Create one resource group for everything, to ensure no reliance on resource groups
        ResourceGroup resourceGroup =
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(region).create();

        // Needed for tracking related resources
        final Map<String, Collection<Creatable<? extends Resource>>> vmNonNicResourceDefinitions = new HashMap<>();
        final Map<String, Creatable<NetworkInterface>> nicDefinitions =
            new HashMap<>(); // Tracking NICs separately because they have to be deleted first
        final Map<String, Creatable<VirtualMachine>> vmDefinitions = new HashMap<>();
        final Map<String, String> createdResourceIds = new HashMap<>();
        final List<Throwable> errors = new ArrayList<>();

        // Prepare a number of VM definitions along with their related resource definitions
        for (int i = 0; i < desiredVMCount; i++) {
            Collection<Creatable<? extends Resource>> relatedDefinitions = new ArrayList<>();

            // Define a network for each VM
            String networkName = sdkContext.randomResourceName("net", 14);
            Creatable<Network> networkDefinition =
                networkManager
                    .networks()
                    .define(networkName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withAddressSpace("10.0." + i + ".0/29");
            relatedDefinitions.add(networkDefinition);

            // Define a PIP for each VM
            String pipName = sdkContext.randomResourceName("pip", 14);
            PublicIpAddress.DefinitionStages.WithCreate pipDefinition =
                this
                    .networkManager
                    .publicIpAddresses()
                    .define(pipName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(pipDefinition);

            // Define a NIC for each VM
            String nicName = sdkContext.randomResourceName("nic", 14);
            Creatable<NetworkInterface> nicDefinition =
                networkManager
                    .networkInterfaces()
                    .define(nicName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup)
                    .withNewPrimaryNetwork(networkDefinition)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(pipDefinition);

            // Define a storage account for each VM
            String storageAccountName = sdkContext.randomResourceName("st", 14);
            Creatable<StorageAccount> storageAccountDefinition =
                storageManager
                    .storageAccounts()
                    .define(storageAccountName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(storageAccountDefinition);

            // Define an availability set for each VM
            String availabilitySetName = sdkContext.randomResourceName("as", 14);
            Creatable<AvailabilitySet> availabilitySetDefinition =
                computeManager
                    .availabilitySets()
                    .define(availabilitySetName)
                    .withRegion(region)
                    .withExistingResourceGroup(resourceGroup);
            relatedDefinitions.add(availabilitySetDefinition);

            String vmName = sdkContext.randomResourceName("vm", 14);

            // Define a VM
            String userName;
            if (i == desiredVMCount / 2) {
                // Intentionally cause a failure in one of the VMs
                userName = "";
            } else {
                userName = "tester";
            }
            Creatable<VirtualMachine> vmDefinition =
                computeManager
                    .virtualMachines()
                    .define(vmName)
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
        computeManager
            .virtualMachines()
            .createAsync(new ArrayList<>(vmDefinitions.values()))
            .map(
                createdResource -> {
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
                })
            .onErrorResume(
                e -> {
                    errors.add(e);
                    return Mono.empty();
                })
            .singleOrEmpty();

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
        Collection<Mono<?>> deleteObservables = new ArrayList<>();
        for (Collection<Creatable<? extends Resource>> relatedResources : vmNonNicResourceDefinitions.values()) {
            for (Creatable<? extends Resource> resource : relatedResources) {
                String createdResourceId = createdResourceIds.get(resource.key());
                if (createdResourceId != null) {
                    deleteObservables.add(resourceManager.genericResources().deleteByIdAsync(createdResourceId));
                }
            }
        }

        // Delete as much as possible, postponing the errors till the end
        Flux.mergeSequentialDelayError(deleteObservables, 5, 3);

        // Show any errors
        for (Throwable error : errors) {
            System.out.println("\n### ERROR ###\n");
            if (error instanceof ManagementException) {
                ManagementException ce = (ManagementException) error;
                System.out.println("CLOUD EXCEPTION: " + ce.getMessage());
            } else {
                error.printStackTrace();
            }
        }

        System.out.println("Number of failed/cleaned up VM creations: " + vmNonNicResourceDefinitions.size());

        // Verifications
        final int successfulVMCount = desiredVMCount - vmNonNicResourceDefinitions.size();
        final int actualVMCount =
            TestUtilities.getSize(computeManager.virtualMachines().listByResourceGroup(resourceGroupName));
        System.out.println("Number of actual successful VMs: " + actualVMCount);

        Assertions.assertEquals(successfulVMCount, actualVMCount);
        final int actualNicCount =
            TestUtilities.getSize(networkManager.networkInterfaces().listByResourceGroup(resourceGroupName));
        Assertions.assertEquals(successfulVMCount, actualNicCount);
        final int actualNetworkCount =
            TestUtilities.getSize(networkManager.networks().listByResourceGroup(resourceGroupName));
        Assertions.assertEquals(successfulVMCount, actualNetworkCount);
        final int actualPipCount =
            TestUtilities.getSize(networkManager.publicIpAddresses().listByResourceGroup(resourceGroupName));
        Assertions.assertEquals(successfulVMCount, actualPipCount);
        final int actualAvailabilitySetCount =
            TestUtilities.getSize(computeManager.availabilitySets().listByResourceGroup(resourceGroupName));
        Assertions.assertEquals(successfulVMCount, actualAvailabilitySetCount);
        final int actualStorageAccountCount =
            TestUtilities.getSize(storageManager.storageAccounts().listByResourceGroup(resourceGroupName));
        Assertions.assertEquals(successfulVMCount, actualStorageAccountCount);

        // Verify that at least one VM failed.
        // TODO: Ideally only one, but today the internal RX logic terminates eagerly -- need to change that for
        // parallel creation to terminate more "lazily" in the future
        Assertions.assertTrue(successfulVMCount < desiredVMCount);
    }
}
