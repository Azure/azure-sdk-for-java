/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfiguration;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.storage.SkuName;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Test;

import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachineOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_EAST;
    private static final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(RG_NAME);
    }

    @Test
    public void canCreateVirtualMachineWithNetworking() throws Exception {
        NetworkSecurityGroup nsg = this.networkManager.networkSecurityGroups().define("nsg")
            .withRegion(REGION)
            .withNewResourceGroup(RG_NAME)
            .defineRule("rule1")
                .allowInbound()
                .fromAnyAddress()
                .fromPort(80)
                .toAnyAddress()
                .toPort(80)
                .withProtocol(SecurityRuleProtocol.TCP)
                .attach()
            .create();

        Creatable<Network> networkDefinition = this.networkManager.networks().define("network1")
            .withRegion(REGION)
            .withNewResourceGroup(RG_NAME)
            .withAddressSpace("10.0.0.0/28")
            .defineSubnet("subnet1")
                .withAddressPrefix("10.0.0.0/29")
                .withExistingNetworkSecurityGroup(nsg)
                .attach();

        // Create
        VirtualMachine vm = computeManager.virtualMachines()
            .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork(networkDefinition)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
            .create();

        NetworkInterface primaryNic = vm.getPrimaryNetworkInterface();
        Assert.assertNotNull(primaryNic);
        NicIPConfiguration primaryIpConfig = primaryNic.primaryIPConfiguration();
        Assert.assertNotNull(primaryIpConfig);

        // Fetch the NSG the way before v1.2
        Assert.assertNotNull(primaryIpConfig.networkId());
        Network network = primaryIpConfig.getNetwork();
        Assert.assertNotNull(primaryIpConfig.subnetName());
        Subnet subnet = network.subnets().get(primaryIpConfig.subnetName());
        Assert.assertNotNull(subnet);
        nsg = subnet.getNetworkSecurityGroup();
        Assert.assertNotNull(nsg);
        Assert.assertEquals("nsg", nsg.name());
        Assert.assertEquals(1, nsg.securityRules().size());

        // Fetch the NSG the v1.2 way
        nsg = primaryIpConfig.getNetworkSecurityGroup();
        Assert.assertEquals("nsg", nsg.name());
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

    @Test
    public void canSetStorageAccountForUnmanagedDisk() {
        final String storageName = SdkContext.randomResourceName("st", 14);
        // Create a premium storage account for virtual machine data disk
        //
        StorageAccount storageAccount = storageManager.storageAccounts().define(storageName)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withSku(SkuName.PREMIUM_LRS)
                .create();

        // Creates a virtual machine with an unmanaged data disk that gets stored in the above
        // premium storage account
        //
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .defineUnmanagedDataDisk("disk1")
                    .withNewVhd(100)
                    .withLun(2)
                    .storeAt(storageAccount.name(), "diskvhds", "datadisk1vhd.vhd")
                    .attach()
                .defineUnmanagedDataDisk("disk2")
                    .withNewVhd(100)
                    .withLun(3)
                    .storeAt(storageAccount.name(), "diskvhds", "datadisk2vhd.vhd")
                    .attach()
                .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        // Validate the unmanaged data disks
        //
        Map<Integer, VirtualMachineUnmanagedDataDisk> unmanagedDataDisks = virtualMachine.unmanagedDataDisks();
        Assert.assertNotNull(unmanagedDataDisks);
        Assert.assertEquals(2, unmanagedDataDisks.size());
        VirtualMachineUnmanagedDataDisk firstUnmanagedDataDisk = unmanagedDataDisks.get(2);
        Assert.assertNotNull(firstUnmanagedDataDisk);
        VirtualMachineUnmanagedDataDisk secondUnmanagedDataDisk = unmanagedDataDisks.get(3);
        Assert.assertNotNull(secondUnmanagedDataDisk);
        String createdVhdUri1 = firstUnmanagedDataDisk.vhdUri();
        String createdVhdUri2 = secondUnmanagedDataDisk.vhdUri();
        Assert.assertNotNull(createdVhdUri1);
        Assert.assertNotNull(createdVhdUri2);
        // delete the virtual machine
        //
        computeManager.virtualMachines().deleteById(virtualMachine.id());
        // Creates another virtual machine by attaching existing unmanaged data disk detached from the
        // above virtual machine.
        //
        virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withRootPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withExistingUnmanagedDataDisk(storageAccount.name(), "diskvhds", "datadisk1vhd.vhd")
                .withSize(VirtualMachineSizeTypes.STANDARD_DS2_V2)
                .create();
        // Gets the vm
        //
        virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        // Validate the unmanaged data disks
        //
        unmanagedDataDisks = virtualMachine.unmanagedDataDisks();
        Assert.assertNotNull(unmanagedDataDisks);
        Assert.assertEquals(1, unmanagedDataDisks.size());
        firstUnmanagedDataDisk = null;
        for(VirtualMachineUnmanagedDataDisk unmanagedDisk : unmanagedDataDisks.values()) {
            firstUnmanagedDataDisk = unmanagedDisk;
            break;
        }
        Assert.assertNotNull(firstUnmanagedDataDisk.vhdUri());
        Assert.assertTrue(firstUnmanagedDataDisk.vhdUri().equalsIgnoreCase(createdVhdUri1));
        // Update the VM by attaching another existing data disk
        //
        virtualMachine.update()
                .withExistingUnmanagedDataDisk(storageAccount.name(), "diskvhds", "datadisk2vhd.vhd")
                .apply();
        // Gets the vm
        //
        virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        // Validate the unmanaged data disks
        //
        unmanagedDataDisks = virtualMachine.unmanagedDataDisks();
        Assert.assertNotNull(unmanagedDataDisks);
        Assert.assertEquals(2, unmanagedDataDisks.size());
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
