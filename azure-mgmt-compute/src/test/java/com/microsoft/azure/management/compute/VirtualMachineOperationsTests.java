/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import rx.Completable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachineOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_SOUTH_CENTRAL;
    private static final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    @Ignore("Can't be played from recording for some reason...")
    public void canDeleteRelatedResourcesFromFailedParallelVMCreations() {
        final int desiredVMCount = 15;
        final Region region = Region.US_EAST;
        final String resourceGroupName = RG_NAME;

        // Create one resource group for everything, to ensure no reliance on resource groups
        ResourceGroup resourceGroup = resourceManager.resourceGroups().define(resourceGroupName).withRegion(region).create();

        // Needed for tracking related resources
        final Map<String, Collection<Creatable<? extends Resource>>> vmNonNicResourceDefinitions = new HashMap<>();
        final Map<String, Creatable<NetworkInterface>> nicDefinitions = new HashMap<>(); // Tracking NICs separately because they have to be deleted first
        final Map<String, Creatable<VirtualMachine>> vmDefinitions = new HashMap<>();
        final Map<String, String> createdResourceIds = new HashMap<>();

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
            if (i == 7) {
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
                    .withSize(VirtualMachineSizeTypes.BASIC_A1)
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
                    throwable.printStackTrace();
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
        networkManager.networkInterfaces().deleteByIds(nicIdsToDelete);

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
        System.out.println("Number of failed/cleaned up VM creations: " + vmNonNicResourceDefinitions.size());

        // Verifications
        final int successfulVMCount = desiredVMCount - vmNonNicResourceDefinitions.size();
        final int actualVMCount = computeManager.virtualMachines().listByResourceGroup(resourceGroupName).size();
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

    @Test
    public void canCreateVirtualMachine() throws Exception {
        // Create
        computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_DATACENTER)
                .withAdminUsername("Foo12")
                .withAdminPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withOSDiskName("javatest")
                .create();

        VirtualMachine foundVM = null;
        List<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundVM = vm1;
                break;
            }
        }
        Assert.assertNotNull(foundVM);
        Assert.assertEquals(REGION, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(RG_NAME, VMNAME);
        Assert.assertNotNull(foundVM);
        Assert.assertEquals(REGION, foundVM.region());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assert.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assert.assertNotNull(instanceView);
        Assert.assertNotNull(instanceView.statuses().size() > 0);

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
    }

    @Test
    public void canCreateVirtualMachinesAndRelatedResourcesInParallel() throws Exception {
        String vmNamePrefix = "vmz";
        String publicIpNamePrefix = generateRandomResourceName("pip-", 15);
        String networkNamePrefix = generateRandomResourceName("vnet-", 15);
        int count = 5;

        CreatablesInfo creatablesInfo = prepareCreatableVirtualMachines(REGION,
                vmNamePrefix,
                networkNamePrefix,
                publicIpNamePrefix,
                count);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        List<String> networkCreatableKeys = creatablesInfo.networkCreatableKeys;
        List<String> publicIpCreatableKeys = creatablesInfo.publicIpCreatableKeys;

        CreatedResources<VirtualMachine> createdVirtualMachines = computeManager.virtualMachines().create(virtualMachineCreatables);
        Assert.assertTrue(createdVirtualMachines.size() == count);

        Set<String> virtualMachineNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }
        for (VirtualMachine virtualMachine : createdVirtualMachines.values()) {
            Assert.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
            Assert.assertNotNull(virtualMachine.id());
        }

        Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }
        for (String networkCreatableKey : networkCreatableKeys) {
            Network createdNetwork = (Network) createdVirtualMachines.createdRelatedResource(networkCreatableKey);
            Assert.assertNotNull(createdNetwork);
            Assert.assertTrue(networkNames.contains(createdNetwork.name()));
        }

        Set<String> publicIPAddressNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            publicIPAddressNames.add(String.format("%s-%d", publicIpNamePrefix, i));
        }
        for (String publicIpCreatableKey : publicIpCreatableKeys) {
            PublicIPAddress createdPublicIPAddress = (PublicIPAddress) createdVirtualMachines.createdRelatedResource(publicIpCreatableKey);
            Assert.assertNotNull(createdPublicIPAddress);
            Assert.assertTrue(publicIPAddressNames.contains(createdPublicIPAddress.name()));
        }
    }

    @Test
    public void canStreamParallelCreatedVirtualMachinesAndRelatedResources() throws Exception {
        String vmNamePrefix = "vmz";
        String publicIpNamePrefix = generateRandomResourceName("pip-", 15);
        String networkNamePrefix = generateRandomResourceName("vnet-", 15);
        int count = 5;

        final Set<String> virtualMachineNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }

        final Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }

        final Set<String> publicIPAddressNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            publicIPAddressNames.add(String.format("%s-%d", publicIpNamePrefix, i));
        }

        final CreatablesInfo creatablesInfo = prepareCreatableVirtualMachines(REGION,
                vmNamePrefix,
                networkNamePrefix,
                publicIpNamePrefix,
                count);
        final AtomicInteger resourceCount = new AtomicInteger(0);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        computeManager.virtualMachines().createAsync(virtualMachineCreatables)
                .map(new Func1<Indexable, Indexable>() {
                    @Override
                    public Indexable call(Indexable createdResource) {
                        if (createdResource instanceof Resource) {
                            Resource resource = (Resource) createdResource;
                            System.out.println("Created: " + resource.id());
                            if (resource instanceof VirtualMachine) {
                                VirtualMachine virtualMachine = (VirtualMachine) resource;
                                Assert.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
                                Assert.assertNotNull(virtualMachine.id());
                            } else if (resource instanceof Network) {
                                Network network = (Network) resource;
                                Assert.assertTrue(networkNames.contains(network.name()));
                                Assert.assertNotNull(network.id());
                            } else if (resource instanceof PublicIPAddress) {
                                PublicIPAddress publicIPAddress = (PublicIPAddress) resource;
                                Assert.assertTrue(publicIPAddressNames.contains(publicIPAddress.name()));
                                Assert.assertNotNull(publicIPAddress.id());
                            }
                        }
                        resourceCount.incrementAndGet();
                        return createdResource;
                    }
                }).toBlocking().last();
        // 1 resource group, 1 storage, 5 network, 5 publicIp, 5 nic, 5 virtual machines
        // Additional one for CreatableUpdatableResourceRoot.
        // TODO - ans - We should not emit CreatableUpdatableResourceRoot.
        Assert.assertEquals(resourceCount.get(), 23);
    }

    private CreatablesInfo prepareCreatableVirtualMachines(Region region,
                                                           String vmNamePrefix,
                                                           String networkNamePrefix,
                                                           String publicIpNamePrefix,
                                                           int vmCount) {
        Creatable<ResourceGroup> resourceGroupCreatable = resourceManager.resourceGroups()
                .define(RG_NAME)
                .withRegion(region);

        Creatable<StorageAccount> storageAccountCreatable = storageManager.storageAccounts()
                .define(generateRandomResourceName("stg", 20))
                .withRegion(region)
                .withNewResourceGroup(resourceGroupCreatable);

        List<String> networkCreatableKeys = new ArrayList<>();
        List<String> publicIpCreatableKeys = new ArrayList<>();
        List<Creatable<VirtualMachine>> virtualMachineCreatables = new ArrayList<>();
        for (int i = 0; i < vmCount; i++) {
            Creatable<Network> networkCreatable = networkManager.networks()
                    .define(String.format("%s-%d", networkNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withAddressSpace("10.0.0.0/28");
            networkCreatableKeys.add(networkCreatable.key());

            Creatable<PublicIPAddress> publicIPAddressCreatable = networkManager.publicIPAddresses()
                    .define(String.format("%s-%d", publicIpNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable);
            publicIpCreatableKeys.add(publicIPAddressCreatable.key());


            Creatable<VirtualMachine> virtualMachineCreatable = computeManager.virtualMachines()
                    .define(String.format("%s-%d", vmNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withNewPrimaryNetwork(networkCreatable)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withRootPassword("BaR@12!#")
                    .withUnmanagedDisks()
                    .withNewStorageAccount(storageAccountCreatable);

            virtualMachineCreatables.add(virtualMachineCreatable);
        }
        CreatablesInfo creatablesInfo = new CreatablesInfo();
        creatablesInfo.virtualMachineCreatables = virtualMachineCreatables;
        creatablesInfo.networkCreatableKeys = networkCreatableKeys;
        creatablesInfo.publicIpCreatableKeys = publicIpCreatableKeys;
        return creatablesInfo;
    }

    class CreatablesInfo {
        public List<Creatable<VirtualMachine>> virtualMachineCreatables;
        List<String> networkCreatableKeys;
        List<String> publicIpCreatableKeys;
    }
 }
