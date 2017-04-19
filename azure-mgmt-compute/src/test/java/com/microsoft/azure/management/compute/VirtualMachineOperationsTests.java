/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        for (int i = 0; i < count; i ++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }
        for (VirtualMachine virtualMachine : createdVirtualMachines.values()) {
            Assert.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
            Assert.assertNotNull(virtualMachine.id());
        }

        Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }
        for (String networkCreatableKey : networkCreatableKeys) {
            Network createdNetwork = (Network) createdVirtualMachines.createdRelatedResource(networkCreatableKey);
            Assert.assertNotNull(createdNetwork);
            Assert.assertTrue(networkNames.contains(createdNetwork.name()));
        }

        Set<String> publicIPAddressNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
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
        for (int i = 0; i < count; i ++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }

        final Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }

        final Set<String> publicIPAddressNames = new HashSet<>();
        for (int i = 0; i < count; i ++) {
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
