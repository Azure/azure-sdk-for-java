/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.CloudException;
import com.azure.management.RestClient;
import com.azure.management.network.Network;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NetworkSecurityGroup;
import com.azure.management.network.NicIPConfiguration;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.SecurityRuleProtocol;
import com.azure.management.network.Subnet;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.CreatedResources;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.SkuName;
import com.azure.management.storage.StorageAccount;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachineOperationsTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private String RG_NAME2 = "";
    private final Region REGION = Region.US_EAST;
    private final Region REGIONPROXPLACEMENTGROUP = Region.US_WEST_CENTRAL;
    private final Region REGIONPROXPLACEMENTGROUP2 = Region.US_SOUTH_CENTRAL;
    private final String VMNAME = "javavm";
    private final String PROXGROUPNAME = "testproxgroup1";
    private final String PROXGROUPNAME2 = "testproxgroup2";
    private final String AVAILABILITYSETNAME = "availset1";
    private final String AVAILABILITYSETNAME2 = "availset2";
    private final ProximityPlacementGroupType PROXGROUPTYPE = ProximityPlacementGroupType.STANDARD;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        RG_NAME2 = generateRandomResourceName("javacsmrg2", 15);
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
        Assertions.assertNotNull(primaryNic);
        NicIPConfiguration primaryIpConfig = primaryNic.primaryIPConfiguration();
        Assertions.assertNotNull(primaryIpConfig);

        // Fetch the NSG the way before v1.2
        Assertions.assertNotNull(primaryIpConfig.networkId());
        Network network = primaryIpConfig.getNetwork();
        Assertions.assertNotNull(primaryIpConfig.subnetName());
        Subnet subnet = network.subnets().get(primaryIpConfig.subnetName());
        Assertions.assertNotNull(subnet);
        nsg = subnet.getNetworkSecurityGroup();
        Assertions.assertNotNull(nsg);
        Assertions.assertEquals("nsg", nsg.name());
        Assertions.assertEquals(1, nsg.securityRules().size());

        // Fetch the NSG the v1.2 way
        nsg = primaryIpConfig.getNetworkSecurityGroup();
        Assertions.assertEquals("nsg", nsg.name());
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
                .withLicenseType("Windows_Server")
            .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGION, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(RG_NAME, VMNAME);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGION, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assertions.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assertions.assertNotNull(instanceView);
        Assertions.assertNotNull(instanceView.statuses().size() > 0);

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
    }

    @Test
    public void canCreateUpdatePriorityAndPrice() throws Exception {
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
                .withSize(VirtualMachineSizeTypes.STANDARD_A2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withOSDiskName("javatest")
                .withLowPriority(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
                .withMaxPrice(1000.0)
                .withLicenseType("Windows_Server")
                .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGION, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(RG_NAME, VMNAME);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGION, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());
        Assertions.assertEquals((Double) 1000.0, foundVM.billingProfile().maxPrice());
        Assertions.assertEquals(VirtualMachineEvictionPolicyTypes.DEALLOCATE, foundVM.evictionPolicy());

        // change max price
        try {
            foundVM.update()
                    .withMaxPrice(1500.0)
                    .apply();
            // not run to assert
            Assertions.assertEquals((Double) 1500.0, foundVM.billingProfile().maxPrice());
            Assertions.fail();
        } catch (CloudException e) {} // cannot change max price when vm is running

        foundVM.deallocate();
        foundVM.update()
                .withMaxPrice(2000.0)
                .apply();
        foundVM.start();

        Assertions.assertEquals((Double) 2000.0, foundVM.billingProfile().maxPrice());

        // change priority types
        foundVM = foundVM.update()
                .withPriority(VirtualMachinePriorityTypes.SPOT)
                .apply();

        Assertions.assertEquals(VirtualMachinePriorityTypes.SPOT, foundVM.priority());

        foundVM = foundVM.update()
                .withPriority(VirtualMachinePriorityTypes.LOW)
                .apply();

        Assertions.assertEquals(VirtualMachinePriorityTypes.LOW, foundVM.priority());
        try {
            foundVM.update()
                    .withPriority(VirtualMachinePriorityTypes.REGULAR)
                    .apply();
            // not run to assert
            Assertions.assertEquals(VirtualMachinePriorityTypes.REGULAR, foundVM.priority());
            Assertions.fail();
        } catch (CloudException e) {} // cannot change priority from low to regular

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
    }

    @Test
    public void cannotUpdateProximityPlacementGroupForVirtualMachine() throws Exception {
        AvailabilitySet setCreated = computeManager.availabilitySets()
                .define(AVAILABILITYSETNAME)
                .withRegion(REGIONPROXPLACEMENTGROUP)
                .withNewResourceGroup(RG_NAME)
                .withNewProximityPlacementGroup(PROXGROUPNAME, PROXGROUPTYPE)
                .create();

        Assertions.assertEquals(AVAILABILITYSETNAME, setCreated.name());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, setCreated.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated.regionName(), setCreated.proximityPlacementGroup().location());


        AvailabilitySet setCreated2 = computeManager.availabilitySets()
                .define(AVAILABILITYSETNAME2)
                .withRegion(REGIONPROXPLACEMENTGROUP2)
                .withNewResourceGroup(RG_NAME2)
                .withNewProximityPlacementGroup(PROXGROUPNAME2, PROXGROUPTYPE)
                .create();

        Assertions.assertEquals(AVAILABILITYSETNAME2, setCreated2.name());
        Assertions.assertNotNull(setCreated2.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, setCreated2.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated2.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated2.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated2.id().equalsIgnoreCase(setCreated2.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated2.regionName(), setCreated2.proximityPlacementGroup().location());

        // Create
        computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGIONPROXPLACEMENTGROUP)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withProximityPlacementGroup(setCreated.proximityPlacementGroup().id())
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_DATACENTER)
                .withAdminUsername("Foo12")
                .withAdminPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withOSDiskName("javatest")
                .withLicenseType("Windows_Server")
                .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGIONPROXPLACEMENTGROUP, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(RG_NAME, VMNAME);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGIONPROXPLACEMENTGROUP, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assertions.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assertions.assertNotNull(instanceView);
        Assertions.assertNotNull(instanceView.statuses().size() > 0);

        Assertions.assertNotNull(foundVM.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, foundVM.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated.id().equalsIgnoreCase(foundVM.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().virtualMachineIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().virtualMachineIds().isEmpty());
        Assertions.assertTrue(foundVM.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().virtualMachineIds().get(0)));

        try {
            //Update Vm to remove it from proximity placement group
            VirtualMachine updatedVm = foundVM.update()
                    .withProximityPlacementGroup(setCreated2.proximityPlacementGroup().id())
                    .apply();
        } catch (CloudException clEx) {
            Assertions.assertTrue(clEx.getMessage().contains("Updating proximity placement group of VM javavm is not allowed while the VM is running. Please stop/deallocate the VM and retry the operation."));
        }

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
        computeManager.availabilitySets().deleteById(setCreated.id());
    }

    @Test
    public void canCreateVirtualMachinesAndAvailabilitySetInSameProximityPlacementGroup() throws Exception {
        AvailabilitySet setCreated = computeManager.availabilitySets()
                .define(AVAILABILITYSETNAME)
                .withRegion(REGIONPROXPLACEMENTGROUP)
                .withNewResourceGroup(RG_NAME)
                .withNewProximityPlacementGroup(PROXGROUPNAME, PROXGROUPTYPE)
                .create();

        Assertions.assertEquals(AVAILABILITYSETNAME, setCreated.name());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, setCreated.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated.regionName(), setCreated.proximityPlacementGroup().location());

        // Create
        computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGIONPROXPLACEMENTGROUP)
                .withExistingResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withProximityPlacementGroup(setCreated.proximityPlacementGroup().id())
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_DATACENTER)
                .withAdminUsername("Foo12")
                .withAdminPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withSize(VirtualMachineSizeTypes.STANDARD_DS3_V2)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withOSDiskName("javatest")
                .withLicenseType("Windows_Server")
                .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(RG_NAME);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(VMNAME)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGIONPROXPLACEMENTGROUP, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(RG_NAME, VMNAME);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(REGIONPROXPLACEMENTGROUP, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assertions.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assertions.assertNotNull(instanceView);
        Assertions.assertNotNull(instanceView.statuses().size() > 0);

        Assertions.assertNotNull(foundVM.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, foundVM.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated.id().equalsIgnoreCase(foundVM.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().virtualMachineIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().virtualMachineIds().isEmpty());
        Assertions.assertTrue(foundVM.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().virtualMachineIds().get(0)));

        //Update Vm to remove it from proximity placement group
        VirtualMachine updatedVm = foundVM.update()
                .withoutProximityPlacementGroup()
                .apply();

        Assertions.assertNotNull(updatedVm.proximityPlacementGroup());
        Assertions.assertEquals(PROXGROUPTYPE, updatedVm.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(updatedVm.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(updatedVm.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions.assertTrue(setCreated.id().equalsIgnoreCase(updatedVm.proximityPlacementGroup().availabilitySetIds().get(0)));

        //TODO: this does not work... can not remove cvm from the placement group
        //Assertions.assertNull(foundVM.proximityPlacementGroup().virtualMachineIds());

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
        computeManager.availabilitySets().deleteById(setCreated.id());
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
        Assertions.assertTrue(createdVirtualMachines.size() == count);

        Set<String> virtualMachineNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            virtualMachineNames.add(String.format("%s-%d", vmNamePrefix, i));
        }
        for (VirtualMachine virtualMachine : createdVirtualMachines.values()) {
            Assertions.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
            Assertions.assertNotNull(virtualMachine.id());
        }

        Set<String> networkNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            networkNames.add(String.format("%s-%d", networkNamePrefix, i));
        }
        for (String networkCreatableKey : networkCreatableKeys) {
            Network createdNetwork = (Network) createdVirtualMachines.createdRelatedResource(networkCreatableKey);
            Assertions.assertNotNull(createdNetwork);
            Assertions.assertTrue(networkNames.contains(createdNetwork.name()));
        }

        Set<String> publicIPAddressNames = new HashSet<>();
        for (int i = 0; i < count; i++) {
            publicIPAddressNames.add(String.format("%s-%d", publicIpNamePrefix, i));
        }
        for (String publicIpCreatableKey : publicIpCreatableKeys) {
            PublicIPAddress createdPublicIPAddress = (PublicIPAddress) createdVirtualMachines.createdRelatedResource(publicIpCreatableKey);
            Assertions.assertNotNull(createdPublicIPAddress);
            Assertions.assertTrue(publicIPAddressNames.contains(createdPublicIPAddress.name()));
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
                .map(createdResource -> {
                    if (createdResource instanceof Resource) {
                        Resource resource = (Resource) createdResource;
                        System.out.println("Created: " + resource.id());
                        if (resource instanceof VirtualMachine) {
                            VirtualMachine virtualMachine = (VirtualMachine) resource;
                            Assertions.assertTrue(virtualMachineNames.contains(virtualMachine.name()));
                            Assertions.assertNotNull(virtualMachine.id());
                        } else if (resource instanceof Network) {
                            Network network = (Network) resource;
                            Assertions.assertTrue(networkNames.contains(network.name()));
                            Assertions.assertNotNull(network.id());
                        } else if (resource instanceof PublicIPAddress) {
                            PublicIPAddress publicIPAddress = (PublicIPAddress) resource;
                            Assertions.assertTrue(publicIPAddressNames.contains(publicIPAddress.name()));
                            Assertions.assertNotNull(publicIPAddress.id());
                        }
                    }
                    resourceCount.incrementAndGet();
                    return createdResource;
                })
                .collectList().block();
        // 1 resource group, 1 storage, 5 network, 5 publicIp, 5 nic, 5 virtual machines
        // Additional one for CreatableUpdatableResourceRoot.
        // TODO - ans - We should not emit CreatableUpdatableResourceRoot.
        Assertions.assertEquals(resourceCount.get(), 23);
    }

    @Test
    public void canSetStorageAccountForUnmanagedDisk() {
        final String storageName = sdkContext.randomResourceName("st", 14);
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
        Assertions.assertNotNull(unmanagedDataDisks);
        Assertions.assertEquals(2, unmanagedDataDisks.size());
        VirtualMachineUnmanagedDataDisk firstUnmanagedDataDisk = unmanagedDataDisks.get(2);
        Assertions.assertNotNull(firstUnmanagedDataDisk);
        VirtualMachineUnmanagedDataDisk secondUnmanagedDataDisk = unmanagedDataDisks.get(3);
        Assertions.assertNotNull(secondUnmanagedDataDisk);
        String createdVhdUri1 = firstUnmanagedDataDisk.vhdUri();
        String createdVhdUri2 = secondUnmanagedDataDisk.vhdUri();
        Assertions.assertNotNull(createdVhdUri1);
        Assertions.assertNotNull(createdVhdUri2);
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
        Assertions.assertNotNull(unmanagedDataDisks);
        Assertions.assertEquals(1, unmanagedDataDisks.size());
        firstUnmanagedDataDisk = null;
        for(VirtualMachineUnmanagedDataDisk unmanagedDisk : unmanagedDataDisks.values()) {
            firstUnmanagedDataDisk = unmanagedDisk;
            break;
        }
        Assertions.assertNotNull(firstUnmanagedDataDisk.vhdUri());
        Assertions.assertTrue(firstUnmanagedDataDisk.vhdUri().equalsIgnoreCase(createdVhdUri1));
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
        Assertions.assertNotNull(unmanagedDataDisks);
        Assertions.assertEquals(2, unmanagedDataDisks.size());
    }


    @Test
    public void canUpdateTagsOnVM() {
        //Create
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("firstuser")
                .withRootPassword("afh123RVS!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        //checking to see if withTag correctly update
        virtualMachine.update().withTag("test", "testValue").apply();
        Assertions.assertEquals("testValue", virtualMachine.inner().getTags().get("test"));

        //checking to see if withTags correctly updates
        Map<String, String> testTags = new HashMap<String, String>();
        testTags.put("testTag", "testValue");
        virtualMachine.update().withTags(testTags).apply();
        Assertions.assertEquals(testTags, virtualMachine.inner().getTags());

    }



    @Test
    public void canRunScriptOnVM() {
        // Create
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("firstuser")
                .withRootPassword("afh123RVS!")
                .create();

        List<String> installGit = new ArrayList<>();
        installGit.add("sudo apt-get update");
        installGit.add("sudo apt-get install -y git");

        RunCommandResult runResult = virtualMachine.runShellScript(installGit, new ArrayList<RunCommandInputParameter>());
        Assertions.assertNotNull(runResult);
        Assertions.assertNotNull(runResult.value());
        Assertions.assertTrue(runResult.value().size() > 0);
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
