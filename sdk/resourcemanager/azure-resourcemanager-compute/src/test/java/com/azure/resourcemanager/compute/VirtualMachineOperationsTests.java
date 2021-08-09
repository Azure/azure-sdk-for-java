// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskState;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachineOperationsTests extends ComputeManagementTest {
    private String rgName = "";
    private String rgName2 = "";
    private final Region region = Region.US_EAST;
    private final Region regionProxPlacementGroup = Region.US_WEST;
    private final Region regionProxPlacementGroup2 = Region.US_EAST;
    private final String vmName = "javavm";
    private final String proxGroupName = "testproxgroup1";
    private final String proxGroupName2 = "testproxgroup2";
    private final String availabilitySetName = "availset1";
    private final String availabilitySetName2 = "availset2";
    private final ProximityPlacementGroupType proxGroupType = ProximityPlacementGroupType.STANDARD;

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        rgName = generateRandomResourceName("javacsmrg", 15);
        rgName2 = generateRandomResourceName("javacsmrg2", 15);
        super.initializeClients(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateVirtualMachineWithNetworking() throws Exception {
        NetworkSecurityGroup nsg =
            this
                .networkManager
                .networkSecurityGroups()
                .define("nsg")
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .defineRule("rule1")
                .allowInbound()
                .fromAnyAddress()
                .fromPort(80)
                .toAnyAddress()
                .toPort(80)
                .withProtocol(SecurityRuleProtocol.TCP)
                .attach()
                .create();

        Creatable<Network> networkDefinition =
            this
                .networkManager
                .networks()
                .define("network1")
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/28")
                .defineSubnet("subnet1")
                .withAddressPrefix("10.0.0.0/29")
                .withExistingNetworkSecurityGroup(nsg)
                .attach();

        // Create
        VirtualMachine vm =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork(networkDefinition)
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .create();

        NetworkInterface primaryNic = vm.getPrimaryNetworkInterface();
        Assertions.assertNotNull(primaryNic);
        NicIpConfiguration primaryIpConfig = primaryNic.primaryIPConfiguration();
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
        computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withUnmanagedDisks()
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskName("javatest")
            .withLicenseType("Windows_Server")
            .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(rgName);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(vmName)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(region, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(rgName, vmName);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(region, foundVM.region());
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
    public void canCreateVirtualMachineSyncPoll() throws Exception {
        final long defaultDelayInMillis = 10 * 1000;

        Accepted<VirtualMachine> acceptedVirtualMachine = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2016_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withUnmanagedDisks()
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskName("javatest")
            .withLicenseType("Windows_Server")
            .beginCreate();
        VirtualMachine createdVirtualMachine = acceptedVirtualMachine.getActivationResponse().getValue();
        Assertions.assertNotEquals("Succeeded", createdVirtualMachine.provisioningState());

        LongRunningOperationStatus pollStatus = acceptedVirtualMachine.getActivationResponse().getStatus();
        long delayInMills = acceptedVirtualMachine.getActivationResponse().getRetryAfter() == null
            ? defaultDelayInMillis
            : acceptedVirtualMachine.getActivationResponse().getRetryAfter().toMillis();
        while (!pollStatus.isComplete()) {
            ResourceManagerUtils.sleep(Duration.ofMillis(delayInMills));

            PollResponse<?> pollResponse = acceptedVirtualMachine.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? defaultDelayInMillis
                : pollResponse.getRetryAfter().toMillis();
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, pollStatus);
        VirtualMachine virtualMachine = acceptedVirtualMachine.getFinalResult();
        Assertions.assertEquals("Succeeded", virtualMachine.provisioningState());

        Accepted<Void> acceptedDelete = computeManager.virtualMachines()
            .beginDeleteByResourceGroup(virtualMachine.resourceGroupName(), virtualMachine.name());

        pollStatus = acceptedDelete.getActivationResponse().getStatus();
        delayInMills = acceptedDelete.getActivationResponse().getRetryAfter() == null
            ? defaultDelayInMillis
            : (int) acceptedDelete.getActivationResponse().getRetryAfter().toMillis();

        while (!pollStatus.isComplete()) {
            ResourceManagerUtils.sleep(Duration.ofMillis(delayInMills));

            PollResponse<?> pollResponse = acceptedDelete.getSyncPoller().poll();
            pollStatus = pollResponse.getStatus();
            delayInMills = pollResponse.getRetryAfter() == null
                ? defaultDelayInMillis
                : (int) pollResponse.getRetryAfter().toMillis();
        }

        boolean deleted = false;
        try {
            computeManager.virtualMachines().getById(virtualMachine.id());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404
                && ("NotFound".equals(e.getValue().getCode()) || "ResourceNotFound".equals(e.getValue().getCode()))) {
                deleted = true;
            }
        }
        Assertions.assertTrue(deleted);
    }

    @Test
    public void canCreateUpdatePriorityAndPrice() throws Exception {
        // Create
        computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2016_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withUnmanagedDisks()
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskName("javatest")
            .withLowPriority(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
            .withMaxPrice(1000.0)
            .withLicenseType("Windows_Server")
            .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(rgName);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(vmName)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(region, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(rgName, vmName);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(region, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());
        Assertions.assertEquals((Double) 1000.0, foundVM.billingProfile().maxPrice());
        Assertions.assertEquals(VirtualMachineEvictionPolicyTypes.DEALLOCATE, foundVM.evictionPolicy());

        // change max price
        try {
            foundVM.update().withMaxPrice(1500.0).apply();
            // not run to assert
            Assertions.assertEquals((Double) 1500.0, foundVM.billingProfile().maxPrice());
            Assertions.fail();
        } catch (ManagementException e) {
        } // cannot change max price when vm is running

        foundVM.deallocate();
        foundVM.update().withMaxPrice(2000.0).apply();
        foundVM.start();

        Assertions.assertEquals((Double) 2000.0, foundVM.billingProfile().maxPrice());

        // change priority types
        foundVM = foundVM.update().withPriority(VirtualMachinePriorityTypes.SPOT).apply();

        Assertions.assertEquals(VirtualMachinePriorityTypes.SPOT, foundVM.priority());

        foundVM = foundVM.update().withPriority(VirtualMachinePriorityTypes.LOW).apply();

        Assertions.assertEquals(VirtualMachinePriorityTypes.LOW, foundVM.priority());
        try {
            foundVM.update().withPriority(VirtualMachinePriorityTypes.REGULAR).apply();
            // not run to assert
            Assertions.assertEquals(VirtualMachinePriorityTypes.REGULAR, foundVM.priority());
            Assertions.fail();
        } catch (ManagementException e) {
        } // cannot change priority from low to regular

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
    }

    @Test
    public void cannotUpdateProximityPlacementGroupForVirtualMachine() throws Exception {
        AvailabilitySet setCreated =
            computeManager
                .availabilitySets()
                .define(availabilitySetName)
                .withRegion(regionProxPlacementGroup)
                .withNewResourceGroup(rgName)
                .withNewProximityPlacementGroup(proxGroupName, proxGroupType)
                .create();

        Assertions.assertEquals(availabilitySetName, setCreated.name());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, setCreated.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated.regionName(), setCreated.proximityPlacementGroup().location());

        AvailabilitySet setCreated2 =
            computeManager
                .availabilitySets()
                .define(availabilitySetName2)
                .withRegion(regionProxPlacementGroup2)
                .withNewResourceGroup(rgName2)
                .withNewProximityPlacementGroup(proxGroupName2, proxGroupType)
                .create();

        Assertions.assertEquals(availabilitySetName2, setCreated2.name());
        Assertions.assertNotNull(setCreated2.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, setCreated2.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated2.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated2.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated2.id().equalsIgnoreCase(setCreated2.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated2.regionName(), setCreated2.proximityPlacementGroup().location());

        // Create
        computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(regionProxPlacementGroup)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withProximityPlacementGroup(setCreated.proximityPlacementGroup().id())
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withUnmanagedDisks()
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskName("javatest")
            .withLicenseType("Windows_Server")
            .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(rgName);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(vmName)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(regionProxPlacementGroup, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(rgName, vmName);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(regionProxPlacementGroup, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assertions.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assertions.assertNotNull(instanceView);
        Assertions.assertNotNull(instanceView.statuses().size() > 0);

        Assertions.assertNotNull(foundVM.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, foundVM.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated.id().equalsIgnoreCase(foundVM.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().virtualMachineIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().virtualMachineIds().isEmpty());
        Assertions
            .assertTrue(foundVM.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().virtualMachineIds().get(0)));

        try {
            // Update Vm to remove it from proximity placement group
            VirtualMachine updatedVm =
                foundVM.update().withProximityPlacementGroup(setCreated2.proximityPlacementGroup().id()).apply();
        } catch (ManagementException clEx) {
            Assertions
                .assertTrue(
                    clEx
                        .getMessage()
                        .contains(
                            "Updating proximity placement group of VM javavm is not allowed while the VM is running."
                                + " Please stop/deallocate the VM and retry the operation."));
        }

        // Delete VM
        computeManager.virtualMachines().deleteById(foundVM.id());
        computeManager.availabilitySets().deleteById(setCreated.id());
    }

    @Test
    public void canCreateVirtualMachinesAndAvailabilitySetInSameProximityPlacementGroup() throws Exception {
        AvailabilitySet setCreated =
            computeManager
                .availabilitySets()
                .define(availabilitySetName)
                .withRegion(regionProxPlacementGroup)
                .withNewResourceGroup(rgName)
                .withNewProximityPlacementGroup(proxGroupName, proxGroupType)
                .create();

        Assertions.assertEquals(availabilitySetName, setCreated.name());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, setCreated.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(setCreated.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(setCreated.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertEquals(setCreated.regionName(), setCreated.proximityPlacementGroup().location());

        // Create
        computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(regionProxPlacementGroup)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withProximityPlacementGroup(setCreated.proximityPlacementGroup().id())
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withUnmanagedDisks()
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withOSDiskCaching(CachingTypes.READ_WRITE)
            .withOSDiskName("javatest")
            .withLicenseType("Windows_Server")
            .create();

        VirtualMachine foundVM = null;
        PagedIterable<VirtualMachine> vms = computeManager.virtualMachines().listByResourceGroup(rgName);
        for (VirtualMachine vm1 : vms) {
            if (vm1.name().equals(vmName)) {
                foundVM = vm1;
                break;
            }
        }
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(regionProxPlacementGroup, foundVM.region());
        // Get
        foundVM = computeManager.virtualMachines().getByResourceGroup(rgName, vmName);
        Assertions.assertNotNull(foundVM);
        Assertions.assertEquals(regionProxPlacementGroup, foundVM.region());
        Assertions.assertEquals("Windows_Server", foundVM.licenseType());

        // Fetch instance view
        PowerState powerState = foundVM.powerState();
        Assertions.assertEquals(powerState, PowerState.RUNNING);
        VirtualMachineInstanceView instanceView = foundVM.instanceView();
        Assertions.assertNotNull(instanceView);
        Assertions.assertNotNull(instanceView.statuses().size() > 0);

        Assertions.assertNotNull(foundVM.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, foundVM.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated.id().equalsIgnoreCase(foundVM.proximityPlacementGroup().availabilitySetIds().get(0)));
        Assertions.assertNotNull(foundVM.proximityPlacementGroup().virtualMachineIds());
        Assertions.assertFalse(foundVM.proximityPlacementGroup().virtualMachineIds().isEmpty());
        Assertions
            .assertTrue(foundVM.id().equalsIgnoreCase(setCreated.proximityPlacementGroup().virtualMachineIds().get(0)));

        // Update Vm to remove it from proximity placement group
        VirtualMachine updatedVm = foundVM.update().withoutProximityPlacementGroup().apply();

        Assertions.assertNotNull(updatedVm.proximityPlacementGroup());
        Assertions.assertEquals(proxGroupType, updatedVm.proximityPlacementGroup().proximityPlacementGroupType());
        Assertions.assertNotNull(updatedVm.proximityPlacementGroup().availabilitySetIds());
        Assertions.assertFalse(updatedVm.proximityPlacementGroup().availabilitySetIds().isEmpty());
        Assertions
            .assertTrue(
                setCreated.id().equalsIgnoreCase(updatedVm.proximityPlacementGroup().availabilitySetIds().get(0)));

        // TODO: this does not work... can not remove cvm from the placement group
        // Assertions.assertNull(foundVM.proximityPlacementGroup().virtualMachineIds());

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

        CreatablesInfo creatablesInfo =
            prepareCreatableVirtualMachines(region, vmNamePrefix, networkNamePrefix, publicIpNamePrefix, count);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        List<String> networkCreatableKeys = creatablesInfo.networkCreatableKeys;
        List<String> publicIpCreatableKeys = creatablesInfo.publicIpCreatableKeys;

        CreatedResources<VirtualMachine> createdVirtualMachines =
            computeManager.virtualMachines().create(virtualMachineCreatables);
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
            PublicIpAddress createdPublicIpAddress =
                (PublicIpAddress) createdVirtualMachines.createdRelatedResource(publicIpCreatableKey);
            Assertions.assertNotNull(createdPublicIpAddress);
            Assertions.assertTrue(publicIPAddressNames.contains(createdPublicIpAddress.name()));
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

        final CreatablesInfo creatablesInfo =
            prepareCreatableVirtualMachines(region, vmNamePrefix, networkNamePrefix, publicIpNamePrefix, count);
        final AtomicInteger resourceCount = new AtomicInteger(0);
        List<Creatable<VirtualMachine>> virtualMachineCreatables = creatablesInfo.virtualMachineCreatables;
        computeManager
            .virtualMachines()
            .createAsync(virtualMachineCreatables)
            .map(
                createdResource -> {
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
                        } else if (resource instanceof PublicIpAddress) {
                            PublicIpAddress publicIPAddress = (PublicIpAddress) resource;
                            Assertions.assertTrue(publicIPAddressNames.contains(publicIPAddress.name()));
                            Assertions.assertNotNull(publicIPAddress.id());
                        }
                    }
                    resourceCount.incrementAndGet();
                    return createdResource;
                })
            .blockLast();

        networkNames.forEach(name -> {
            Assertions.assertNotNull(networkManager.networks().getByResourceGroup(rgName, name));
        });

        publicIPAddressNames.forEach(name -> {
            Assertions.assertNotNull(networkManager.publicIpAddresses().getByResourceGroup(rgName, name));
        });

        Assertions.assertEquals(1, storageManager.storageAccounts().listByResourceGroup(rgName).stream().count());
        Assertions.assertEquals(count, networkManager.networkInterfaces().listByResourceGroup(rgName).stream().count());

        Assertions.assertEquals(count, resourceCount.get());
    }

    @Test
    public void canSetStorageAccountForUnmanagedDisk() {
        final String storageName = generateRandomResourceName("st", 14);
        // Create a premium storage account for virtual machine data disk
        //
        StorageAccount storageAccount =
            storageManager
                .storageAccounts()
                .define(storageName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withSku(StorageAccountSkuType.PREMIUM_LRS)
                .create();

        // Creates a virtual machine with an unmanaged data disk that gets stored in the above
        // premium storage account
        //
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
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
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2as_v4"))
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
        virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks()
                .withExistingUnmanagedDataDisk(storageAccount.name(), "diskvhds", "datadisk1vhd.vhd")
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2as_v4"))
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
        for (VirtualMachineUnmanagedDataDisk unmanagedDisk : unmanagedDataDisks.values()) {
            firstUnmanagedDataDisk = unmanagedDisk;
            break;
        }
        Assertions.assertNotNull(firstUnmanagedDataDisk.vhdUri());
        Assertions.assertTrue(firstUnmanagedDataDisk.vhdUri().equalsIgnoreCase(createdVhdUri1));
        // Update the VM by attaching another existing data disk
        //
        virtualMachine
            .update()
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
        // Create
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("firstuser")
                .withSsh(sshPublicKey())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .create();

        // checking to see if withTag correctly update
        virtualMachine.update().withTag("test", "testValue").apply();
        Assertions.assertEquals("testValue", virtualMachine.innerModel().tags().get("test"));

        // checking to see if withTags correctly updates
        Map<String, String> testTags = new HashMap<String, String>();
        testTags.put("testTag", "testValue");
        virtualMachine.update().withTags(testTags).apply();
        Assertions.assertEquals(testTags.get("testTag"), virtualMachine.innerModel().tags().get("testTag"));
    }

    @Test
    public void canRunScriptOnVM() {
        // Create
        VirtualMachine virtualMachine =
            computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                .withRootUsername("firstuser")
                .withSsh(sshPublicKey())
                .create();

        List<String> installGit = new ArrayList<>();
        installGit.add("sudo apt-get update");
        installGit.add("sudo apt-get install -y git");

        RunCommandResult runResult =
            virtualMachine.runShellScript(installGit, new ArrayList<RunCommandInputParameter>());
        Assertions.assertNotNull(runResult);
        Assertions.assertNotNull(runResult.value());
        Assertions.assertTrue(runResult.value().size() > 0);
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canPerformSimulateEvictionOnSpotVirtualMachine() {
        VirtualMachine virtualMachine = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("firstuser")
            .withSsh(sshPublicKey())
            .withSpotPriority(VirtualMachineEvictionPolicyTypes.DEALLOCATE)
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .create();

        Assertions.assertNotNull(virtualMachine.osDiskStorageAccountType());
        Assertions.assertTrue(virtualMachine.osDiskSize() > 0);
        Disk disk = computeManager.disks().getById(virtualMachine.osDiskId());
        Assertions.assertNotNull(disk);
        Assertions.assertEquals(DiskState.ATTACHED, disk.innerModel().diskState());

        // call simulate eviction
        virtualMachine.simulateEviction();
        boolean deallocated = false;
        int pollIntervalInMinutes = 5;
        for (int i = 0; i < 30; i += pollIntervalInMinutes) {
            ResourceManagerUtils.sleep(Duration.ofMinutes(pollIntervalInMinutes));

            virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
            if (virtualMachine.powerState() == PowerState.DEALLOCATED) {
                deallocated = true;
                break;
            }
        }
        Assertions.assertTrue(deallocated);

        virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertNull(virtualMachine.osDiskStorageAccountType());
        Assertions.assertEquals(0, virtualMachine.osDiskSize());
        disk = computeManager.disks().getById(virtualMachine.osDiskId());
        Assertions.assertEquals(DiskState.RESERVED, disk.innerModel().diskState());
    }

    @Test
    public void canForceDeleteVirtualMachine() {
        // Create
        computeManager.virtualMachines()
            .define(vmName)
            .withRegion("eastus2euap")
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword("abc!@#F0orL")
            .create();
        // Get
        VirtualMachine virtualMachine = computeManager.virtualMachines().getByResourceGroup(rgName, vmName);
        Assertions.assertNotNull(virtualMachine);
        Assertions.assertEquals(Region.fromName("eastus2euap"), virtualMachine.region());
        String nicId = virtualMachine.primaryNetworkInterfaceId();

        // Force delete
        computeManager.virtualMachines().deleteById(virtualMachine.id(), true);

        try {
            virtualMachine = computeManager.virtualMachines().getById(virtualMachine.id());
        } catch (ManagementException ex) {
            virtualMachine = null;
            Assertions.assertEquals(404, ex.getResponse().getStatusCode());
        }
        Assertions.assertNull(virtualMachine);

        // check if nic exists after force delete vm
        NetworkInterface nic = networkManager.networkInterfaces().getById(nicId);
        Assertions.assertNotNull(nic);
    }

    private CreatablesInfo prepareCreatableVirtualMachines(
        Region region, String vmNamePrefix, String networkNamePrefix, String publicIpNamePrefix, int vmCount) {

        Creatable<ResourceGroup> resourceGroupCreatable =
            resourceManager.resourceGroups().define(rgName).withRegion(region);

        Creatable<StorageAccount> storageAccountCreatable =
            storageManager
                .storageAccounts()
                .define(generateRandomResourceName("stg", 20))
                .withRegion(region)
                .withNewResourceGroup(resourceGroupCreatable);

        List<String> networkCreatableKeys = new ArrayList<>();
        List<String> publicIpCreatableKeys = new ArrayList<>();
        List<Creatable<VirtualMachine>> virtualMachineCreatables = new ArrayList<>();
        for (int i = 0; i < vmCount; i++) {
            Creatable<Network> networkCreatable =
                networkManager
                    .networks()
                    .define(String.format("%s-%d", networkNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withAddressSpace("10.0.0.0/28");
            networkCreatableKeys.add(networkCreatable.key());

            Creatable<PublicIpAddress> publicIPAddressCreatable =
                networkManager
                    .publicIpAddresses()
                    .define(String.format("%s-%d", publicIpNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable);
            publicIpCreatableKeys.add(publicIPAddressCreatable.key());

            Creatable<VirtualMachine> virtualMachineCreatable =
                computeManager
                    .virtualMachines()
                    .define(String.format("%s-%d", vmNamePrefix, i))
                    .withRegion(region)
                    .withNewResourceGroup(resourceGroupCreatable)
                    .withNewPrimaryNetwork(networkCreatable)
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIPAddressCreatable)
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
                    .withRootUsername("tirekicker")
                    .withSsh(sshPublicKey())
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
        private List<Creatable<VirtualMachine>> virtualMachineCreatables;
        List<String> networkCreatableKeys;
        List<String> publicIpCreatableKeys;
    }
}
