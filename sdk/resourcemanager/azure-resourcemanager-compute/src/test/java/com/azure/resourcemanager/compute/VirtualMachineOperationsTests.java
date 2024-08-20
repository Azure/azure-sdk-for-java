// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInner;
import com.azure.resourcemanager.compute.models.ApiErrorException;
import com.azure.resourcemanager.compute.models.AvailabilitySet;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DeleteOptions;
import com.azure.resourcemanager.compute.models.DiffDiskPlacement;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskEncryptionSet;
import com.azure.resourcemanager.compute.models.DiskEncryptionSetType;
import com.azure.resourcemanager.compute.models.DiskState;
import com.azure.resourcemanager.compute.models.EncryptionType;
import com.azure.resourcemanager.compute.models.InstanceViewStatus;
import com.azure.resourcemanager.compute.models.InstanceViewTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.KnownWindowsVirtualMachineImage;
import com.azure.resourcemanager.compute.models.PowerState;
import com.azure.resourcemanager.compute.models.ProximityPlacementGroupType;
import com.azure.resourcemanager.compute.models.RunCommandInputParameter;
import com.azure.resourcemanager.compute.models.RunCommandResult;
import com.azure.resourcemanager.compute.models.SecurityTypes;
import com.azure.resourcemanager.compute.models.UpgradeMode;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineDiskOptions;
import com.azure.resourcemanager.compute.models.VirtualMachineEvictionPolicyTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineInstanceView;
import com.azure.resourcemanager.compute.models.VirtualMachinePriorityTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachineUnmanagedDataDisk;
import com.azure.resourcemanager.keyvault.models.Key;
import com.azure.resourcemanager.keyvault.models.KeyPermissions;
import com.azure.resourcemanager.keyvault.models.Vault;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerSkuType;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.CreatedResources;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import com.azure.security.keyvault.keys.models.KeyType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class VirtualMachineOperationsTests extends ComputeManagementTest {
    private static final ClientLogger LOGGER = new ClientLogger(VirtualMachineOperationsTests.class);

    private String rgName = "";
    private String rgName2 = "";
    private final Region region = Region.US_WEST2;
    private final Region regionProxPlacementGroup = Region.US_WEST2;
    private final Region regionProxPlacementGroup2 = Region.US_WEST3;
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
        if (rgName != null) {
            resourceManager.resourceGroups().beginDeleteByName(rgName);
        }
    }

    @Test
    public void canCreateAndUpdateVirtualMachineWithUserData() {
        String userDataForCreate = "N0ZBN0MxRkYtMkNCMC00RUM3LUE1RDctMDY2MUI0RTdDNzY4";
        String userDataForUpdate = "Njc5MDI3MUItQ0RGRC00RjdELUI5NTEtMTA4QjA2RTNGNDRE";

        // Create
        VirtualMachine vm = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER_GEN2)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withNewDataDisk(127)
            .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
            .withUserData(userDataForCreate)
            .create();
        Response<VirtualMachineInner> response = computeManager.serviceClient().getVirtualMachines()
            .getByResourceGroupWithResponse(rgName, vmName, InstanceViewTypes.USER_DATA, Context.NONE);
        Assertions.assertEquals(userDataForCreate, response.getValue().userData());

        // Update
        vm.update().withUserData(userDataForUpdate).apply();
        response = computeManager.serviceClient().getVirtualMachines()
            .getByResourceGroupWithResponse(rgName, vmName, InstanceViewTypes.USER_DATA, Context.NONE);
        Assertions.assertEquals(userDataForUpdate, response.getValue().userData());
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
        Assertions.assertNotNull(foundVM.timeCreated());

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
    public void cannotCreateVirtualMachineSyncPoll() throws Exception {
        final String mySqlInstallScript = "https://raw.githubusercontent.com/Azure/azure-quickstart-templates/4397e808d07df60ff3cdfd1ae40999f0130eb1b3/mysql-standalone-server-ubuntu/scripts/install_mysql_server_5.6.sh";
        final String installCommand = "bash install_mysql_server_5.6.sh Abc.123x(";

        Assertions.assertThrows(IllegalStateException.class, () -> {
            Accepted<VirtualMachine> acceptedVirtualMachine =
                this.computeManager.virtualMachines()
                    .define(vmName)
                    .withRegion(region)
                    .withNewResourceGroup(rgName)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                    .withRootUsername("Foo12")
                    .withSsh(sshPublicKey())
                    // virtual machine extensions is not compatible with "beginCreate" method
                    .defineNewExtension("CustomScriptForLinux")
                        .withPublisher("Microsoft.OSTCExtensions")
                        .withType("CustomScriptForLinux")
                        .withVersion("1.4")
                        .withMinorVersionAutoUpgrade()
                        .withPublicSetting("fileUris", Collections.singletonList(mySqlInstallScript))
                        .withPublicSetting("commandToExecute", installCommand)
                        .attach()
                    .beginCreate();
        });

        // verify dependent resources is not created in the case of above failed "beginCreate" method
        boolean dependentResourceCreated = computeManager.resourceManager().serviceClient().getResourceGroups().checkExistence(rgName);
        Assertions.assertFalse(dependentResourceCreated);

        // skip cleanup
        rgName = null;
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
                        LOGGER.log(LogLevel.VERBOSE, () -> "Created: " + resource.id());
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
            .withAdminPassword(password())
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

    @Test
    public void canCreateVirtualMachineWithDeleteOption() throws Exception {
        Region region = Region.US_WEST3;

        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);

        Network network = this
            .networkManager
            .networks()
            .define("network1")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet("default", "10.0.0.0/24")
            .create();

        // 1. VM with NIC and Disk
        VirtualMachine vm1 = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("default")
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress(publicIpDnsLabel/*, DeleteOptions.DELETE*/)
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10)
            .withNewDataDisk(computeManager.disks()
                .define("datadisk2")
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(10))
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DELETE)
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertEquals(DeleteOptions.DELETE, vm1.osDiskDeleteOptions());
        Assertions.assertEquals(DeleteOptions.DELETE, vm1.dataDisks().get(0).deleteOptions());
        Assertions.assertEquals(DeleteOptions.DELETE, vm1.dataDisks().get(1).deleteOptions());
        Assertions.assertEquals(vm1.id(), computeManager.virtualMachines().getById(vm1.id()).id());

        computeManager.virtualMachines().deleteById(vm1.id());
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        // verify that nic, os/data disk is deleted
        // only Network and PublicIpAddress remains in the resource group, others is deleted together with the virtual machine resource
        Assertions.assertEquals(2, computeManager.resourceManager().genericResources().listByResourceGroup(rgName).stream().count());
        // delete PublicIpAddress
        PublicIpAddress publicIpAddress = computeManager.networkManager().publicIpAddresses().listByResourceGroup(rgName).stream().findFirst().get();
        computeManager.networkManager().publicIpAddresses().deleteById(publicIpAddress.id());


        // 2. VM with secondary NIC
        String secondaryNicName = generateRandomResourceName("nic", 10);
        Creatable<NetworkInterface> secondaryNetworkInterfaceCreatable =
            this
                .networkManager
                .networkInterfaces()
                .define(secondaryNicName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet("default")
                .withPrimaryPrivateIPAddressDynamic();

        VirtualMachine vm2 = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("default")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable, DeleteOptions.DELETE)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        computeManager.virtualMachines().deleteById(vm2.id());
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        // verify nic and disk is deleted
        Assertions.assertEquals(1, computeManager.resourceManager().genericResources().listByResourceGroup(rgName).stream().count());


        // 3. VM without DeleteOptions
        secondaryNetworkInterfaceCreatable =
            this
                .networkManager
                .networkInterfaces()
                .define(secondaryNicName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet("default")
                .withPrimaryPrivateIPAddressDynamic();

        VirtualMachine vm3 = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("default")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10)
            .withNewDataDisk(computeManager.disks()
                .define("datadisk2")
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(10))
            .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertEquals(DeleteOptions.DETACH, vm3.osDiskDeleteOptions());
        Assertions.assertEquals(DeleteOptions.DETACH, vm3.dataDisks().get(0).deleteOptions());
        Assertions.assertEquals(DeleteOptions.DETACH, vm3.dataDisks().get(1).deleteOptions());

        computeManager.virtualMachines().deleteById(vm3.id());
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        // verify nic and disk is not deleted
        Assertions.assertEquals(3, computeManager.disks().listByResourceGroup(rgName).stream().count());
        Assertions.assertEquals(2, computeManager.networkManager().networkInterfaces().listByResourceGroup(rgName).stream().count());
    }

    @Test
    public void canUpdateVirtualMachineWithDeleteOption() throws Exception {
        Region region = Region.US_WEST3;

        Network network = this
            .networkManager
            .networks()
            .define("network1")
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet("default", "10.0.0.0/24")
            .create();

        // 1. VM with DeleteOptions=DELETE, to be updated
        VirtualMachine vm1 = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("default")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DELETE)
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        // update with new Disk without DeleteOptions
        vm1.update()
            .withNewDataDisk(10)
            .apply();

        computeManager.virtualMachines().deleteById(vm1.id());
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        // verify disk in update is not deleted
        Assertions.assertEquals(1, computeManager.disks().listByResourceGroup(rgName).stream().count());
        Disk disk = computeManager.disks().listByResourceGroup(rgName).stream().findFirst().get();
        computeManager.disks().deleteById(disk.id());


        // 2. VM with DeleteOptions=null for Disk, to be updated
        VirtualMachine vm2 = computeManager
            .virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("default")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("testuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        // update with new Disk with DeleteOptions=DELETE
        vm2.update()
            .withNewDataDisk(10)
            .withDataDiskDefaultDeleteOptions(DeleteOptions.DELETE)
            .apply();

        computeManager.virtualMachines().deleteById(vm2.id());
        ResourceManagerUtils.sleep(Duration.ofSeconds(10));

        // verify disk in create is not deleted
        Assertions.assertEquals(2, computeManager.disks().listByResourceGroup(rgName).stream().count());
    }

    @Test
    public void canHibernateVirtualMachine() {
        // preview feature

        // create to enable hibernation
        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion("eastus2euap")
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
//            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
//            .withRootUsername("Foo12")
//            .withSsh(sshPublicKey())
            .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2019_DATACENTER)
            .withAdminUsername("Foo12")
            .withAdminPassword(password())
            .withSize("Standard_D2as_v5")
            .enableHibernation()
            .create();

        Assertions.assertTrue(vm.isHibernationEnabled());

        // deallocate with hibernate
        vm.deallocate(true);

        InstanceViewStatus hibernationStatus = vm.instanceView().statuses().stream()
            .filter(status -> "HibernationState/Hibernated".equals(status.code()))
            .findFirst().orElse(null);
        Assertions.assertNotNull(hibernationStatus);

        vm.start();

        // update to disable hibernation
        vm.deallocate();
        vm.update()
            .disableHibernation()
            .apply();

        Assertions.assertFalse(vm.isHibernationEnabled());
    }

    @Test
    public void canOperateVirtualMachine() {
        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.STANDARD_A1_V2)
            .create();

        Assertions.assertEquals(PowerState.RUNNING, vm.powerState());

        vm.redeploy();

        vm.powerOff(true);
        vm.refreshInstanceView();
        Assertions.assertEquals(PowerState.STOPPED, vm.powerState());

        vm.start();
        vm.refreshInstanceView();
        Assertions.assertEquals(PowerState.RUNNING, vm.powerState());

        vm.restart();
        vm.refreshInstanceView();
        Assertions.assertEquals(PowerState.RUNNING, vm.powerState());

        vm.deallocate();
        vm.refreshInstanceView();
        Assertions.assertEquals(PowerState.DEALLOCATED, vm.powerState());
    }

    @Test
    public void canCreateVirtualMachineWithEphemeralOSDisk() {
        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
            .withEphemeralOSDisk()
            .withPlacement(DiffDiskPlacement.CACHE_DISK)
            .withNewDataDisk(1, 1, CachingTypes.READ_WRITE)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .create();

        Assertions.assertNull(vm.osDiskDiskEncryptionSetId());
        Assertions.assertTrue(vm.osDiskSize() > 0);
        Assertions.assertEquals(vm.osDiskDeleteOptions(), DeleteOptions.DELETE);
        Assertions.assertEquals(vm.osDiskCachingType(), CachingTypes.READ_ONLY);
        Assertions.assertFalse(CoreUtils.isNullOrEmpty(vm.dataDisks()));
        Assertions.assertTrue(vm.isOSDiskEphemeral());
        Assertions.assertNotNull(vm.osDiskId());

        String osDiskId = vm.osDiskId();

        vm.update()
            .withoutDataDisk(1)
            .withNewDataDisk(1, 2, CachingTypes.NONE)
            .withNewDataDisk(1)
            .apply();
        Assertions.assertEquals(vm.dataDisks().size(), 2);

        vm.powerOff();
        vm.start();
        vm.refresh();
        Assertions.assertEquals(osDiskId, vm.osDiskId());

        // deallocate not supported on vm with ephemeral os disk
        Assertions.assertThrows(Exception.class, vm::deallocate);
    }

    @Test
    public void canCreateVirtualMachineWithExistingScaleSet() throws Exception {
        // can add regular vm to vmss
        final String vmssName = generateRandomResourceName("vmss", 10);
        Network network =
            this
                .networkManager
                .networks()
                .define("vmssvnet")
                .withRegion(region.name())
                .withNewResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/28")
                .create();
        ResourceGroup resourceGroup = this.resourceManager.resourceGroups().getByName(rgName);
        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, resourceGroup, "1", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);
        VirtualMachineScaleSet flexibleVMSS = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_DS1_V2)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withCapacity(1)
            .withUpgradeMode(UpgradeMode.AUTOMATIC)
            .create();

        String regularVMName = generateRandomResourceName("vm", 10);
        final String pipDnsLabel = generateRandomResourceName("pip", 10);
        VirtualMachine regularVM = this.computeManager
            .virtualMachines()
            .define(regularVMName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.1.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withNewPrimaryPublicIPAddress(pipDnsLabel)
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
            .withRootUsername("jvuser2")
            .withSsh(sshPublicKey())
            .withExistingVirtualMachineScaleSet(flexibleVMSS)
            .create();
        flexibleVMSS.refresh();
        Assertions.assertEquals(flexibleVMSS.id(), regularVM.virtualMachineScaleSetId());
        Assertions.assertEquals(2, flexibleVMSS.capacity());
//        // Flexible vmss vm instance ids are all null, which means VMs in flexible vmss can only be operated by individual `VirtualMachine` APIs.
//        Assertions.assertTrue(flexibleVMSS.virtualMachines().list().stream().allMatch(vm -> vm.instanceId() == null));
        // as 2023-03-01, instanceId is not null for FlexibleOrchestrationMode

        regularVM.deallocate();
        Assertions.assertEquals(regularVM.powerState(), PowerState.DEALLOCATED);

        this.computeManager
            .virtualMachines().deleteById(regularVM.id());
        flexibleVMSS.refresh();
        Assertions.assertEquals(flexibleVMSS.capacity(), 1);

        // can't add vm with unmanaged disk to vmss
        final String storageAccountName = generateRandomResourceName("stg", 17);
        Assertions.assertThrows(
            ApiErrorException.class,
            () -> computeManager
                .virtualMachines()
                .define(vmName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.1.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withLatestLinuxImage("Canonical", "UbuntuServer", "14.04.2-LTS")
                .withRootUsername("jvuser3")
                .withSsh(sshPublicKey())
                .withUnmanagedDisks() /* UN-MANAGED OS and DATA DISKS */
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .withNewStorageAccount(storageAccountName)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withExistingVirtualMachineScaleSet(flexibleVMSS)
                .create()
        );

        // can't add vm to `UNIFORM` vmss
        final String vmssName2 = generateRandomResourceName("vmss", 10);
        Network network2 =
            this
                .networkManager
                .networks()
                .define("vmssvnet2")
                .withRegion(region.name())
                .withExistingResourceGroup(rgName)
                .withAddressSpace("192.168.0.0/28")
                .withSubnet("subnet2", "192.168.0.0/28")
                .create();
        LoadBalancer publicLoadBalancer2 = createHttpLoadBalancers(region, resourceGroup, "2", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);
        VirtualMachineScaleSet uniformVMSS = this.computeManager
            .virtualMachineScaleSets()
            .define(vmssName2)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network2, "subnet2")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer2)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser4")
            .withSsh(sshPublicKey())
            .withCapacity(1)
            .create();
        Assertions.assertTrue(uniformVMSS.virtualMachines().list().stream().allMatch(v -> v.instanceId() != null));

        String regularVMName2 = generateRandomResourceName("vm", 10);
        Assertions.assertThrows(
            ApiErrorException.class,
            () -> this.computeManager
                .virtualMachines()
                .define(regularVMName2)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withNewPrimaryNetwork("10.0.1.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("jvuser5")
                .withSsh(sshPublicKey())
                .withExistingVirtualMachineScaleSet(uniformVMSS)
                .create()
        );
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void canSwapOSDiskWithManagedDisk() {
        String storageAccountName = generateRandomResourceName("sa", 15);
        StorageAccount storageAccount = this.storageManager
            .storageAccounts()
            .define(storageAccountName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .create();

        // create vm with os disk encrypted with platform managed key
        String vm1Name = generateRandomResourceName("vm", 15);
        VirtualMachine vm1 = this.computeManager
            .virtualMachines()
            .define(vm1Name)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/24")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10, 1, CachingTypes.READ_WRITE)
            .withOSDiskDeleteOptions(DeleteOptions.DETACH)
            .withExistingStorageAccount(storageAccount)
            .withSize(VirtualMachineSizeTypes.STANDARD_D2S_V3)
            .create();
        Disk vm1OSDisk = this.computeManager.disks().getById(vm1.osDiskId());
        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_PLATFORM_KEY, vm1OSDisk.encryption().type());

        // create vm with os disk encrypted with customer managed key (cmk)
        String vaultName = generateRandomResourceName("vault", 15);
        Vault vault = this.keyVaultManager
            .vaults()
            .define(vaultName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .defineAccessPolicy()
            .forUser(azureCliSignedInUser().userPrincipalName())
            .allowKeyPermissions(KeyPermissions.CREATE)
            .attach()
            .create();

        String keyName = generateRandomResourceName("key", 15);
        Key key = vault.keys()
            .define(keyName)
            .withKeyTypeToCreate(KeyType.RSA)
            .withKeySize(4096)
            .create();

        String desName = generateRandomResourceName("des", 15);
        DiskEncryptionSet des = this.computeManager.diskEncryptionSets()
            .define(desName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withEncryptionType(DiskEncryptionSetType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY)
            .withExistingKeyVault(vault.id())
            .withExistingKey(key.id())
            .withSystemAssignedManagedServiceIdentity()
            .create();

        vault.update()
            .defineAccessPolicy()
            .forObjectId(des.systemAssignedManagedServiceIdentityPrincipalId())
            .allowKeyPermissions(KeyPermissions.GET, KeyPermissions.UNWRAP_KEY, KeyPermissions.WRAP_KEY)
            .attach()
            .withPurgeProtectionEnabled()
            .apply();

        String vm2Name = generateRandomResourceName("vm", 15);
        VirtualMachine vm2 = this.computeManager.virtualMachines()
            .define(vm2Name)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/24")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withOSDiskDiskEncryptionSet(des.id())
            .withOSDiskDeleteOptions(DeleteOptions.DETACH)
            .create();
        String vm2OSDiskId = vm2.osDiskId();
        this.computeManager.virtualMachines().deleteById(vm2.id());
        Disk vm2OSDisk = this.computeManager.disks().getById(vm2OSDiskId);
        Assertions.assertEquals(EncryptionType.ENCRYPTION_AT_REST_WITH_CUSTOMER_KEY, vm2OSDisk.encryption().type());

        // swap vm1's os disk(encrypted with pmk) with vm2's os disk(encrypted with cmk)
        vm1.deallocate();
        vm1.update()
            .withOSDisk(vm2OSDiskId)
            .apply();
        vm1.start();
        vm1.refresh();
        Assertions.assertEquals(vm1.osDiskId(), vm2OSDiskId);
        Assertions.assertTrue(des.id().equalsIgnoreCase(vm1.storageProfile().osDisk().managedDisk().diskEncryptionSet().id()));

        // swap back vm1's os disk(encrypted with pmk)
        vm1.deallocate();
        vm1.update()
            .withOSDisk(vm1OSDisk)
            .apply();
        vm1.start();
        vm1.refresh();
        Assertions.assertEquals(vm1.osDiskId(), vm1OSDisk.id());
        Assertions.assertNull(vm1.storageProfile().osDisk().managedDisk().diskEncryptionSet());
    }

    @Test
    public void canCRUDTrustedLaunchVM() {
        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(Region.US_WEST3)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withTrustedLaunch()
            .withSecureBoot()
            .withVTpm()
            .withSize(VirtualMachineSizeTypes.STANDARD_DS1_V2)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .create();

        Assertions.assertEquals(SecurityTypes.TRUSTED_LAUNCH, vm.securityType());
        Assertions.assertTrue(vm.isSecureBootEnabled());
        Assertions.assertTrue(vm.isVTpmEnabled());

        vm.update()
            .withoutSecureBoot()
            .withoutVTpm()
            .applyAsync()
            // security features changes need restart to take effect
            .flatMap(VirtualMachine::restartAsync)
            .block();

        // Let virtual machine finish restarting.
        ResourceManagerUtils.sleep(Duration.ofMinutes(1));

        vm = computeManager.virtualMachines().getById(vm.id());

        Assertions.assertEquals(SecurityTypes.TRUSTED_LAUNCH, vm.securityType());
        Assertions.assertFalse(vm.isSecureBootEnabled());
        Assertions.assertFalse(vm.isVTpmEnabled());

        computeManager.virtualMachines().deleteById(vm.id());
    }

    @Test
    public void canUpdateDeleteOptions() {
        String networkName = generateRandomResourceName("network", 15);
        String nicName = generateRandomResourceName("nic", 15);
        String nicName2 = generateRandomResourceName("nic", 15);

        Network network = this
            .networkManager
            .networks()
            .define(networkName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.1.0/24")
            .withSubnet("subnet1", "10.0.1.0/28")
            .withSubnet("subnet2", "10.0.1.16/28")
            .create();

        // OS disk, primary and secondary nics, data disk delete options all set to DELETE
        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withExistingPrimaryNetwork(network)
            .withSubnet("subnet1")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_20_04_LTS_GEN2)
            .withRootUsername("Foo12")
            .withSsh(sshPublicKey())
            .withNewDataDisk(10, 1, new VirtualMachineDiskOptions().withDeleteOptions(DeleteOptions.DELETE))
            .withSize(VirtualMachineSizeTypes.STANDARD_D8S_V3)
            .withNewSecondaryNetworkInterface(this
                .networkManager
                .networkInterfaces()
                .define(nicName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet1")
                .withPrimaryPrivateIPAddressDynamic(), DeleteOptions.DELETE)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withOSDiskDeleteOptions(DeleteOptions.DELETE)
            .create();

        Assertions.assertEquals(DeleteOptions.DELETE, vm.osDiskDeleteOptions());
        Assertions.assertEquals(DeleteOptions.DELETE, vm.primaryNetworkInterfaceDeleteOptions());
        Assertions.assertTrue(vm.dataDisks().values().stream().allMatch(disk -> DeleteOptions.DELETE.equals(disk.deleteOptions())));

        // update delete options all to DETACH, except for secondary nic
        vm.update()
            .withOsDiskDeleteOptions(DeleteOptions.DETACH)
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DETACH)
            .withDataDisksDeleteOptions(DeleteOptions.DETACH, 1)
            .apply();

        Assertions.assertEquals(DeleteOptions.DETACH, vm.osDiskDeleteOptions());
        Assertions.assertEquals(DeleteOptions.DETACH, vm.primaryNetworkInterfaceDeleteOptions());
        // secondary nic delete options remains unchanged
        Assertions.assertTrue(vm.networkInterfaceIds().stream()
            .filter(nicId -> !nicId.equals(vm.primaryNetworkInterfaceId())).allMatch(nicId -> DeleteOptions.DELETE.equals(vm.networkInterfaceDeleteOptions(nicId))));
        Assertions.assertTrue(vm.dataDisks().values().stream().allMatch(disk -> DeleteOptions.DETACH.equals(disk.deleteOptions())));

        NetworkInterface secondaryNic2 =
            this
                .networkManager
                .networkInterfaces()
                .define(nicName2)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet("subnet2")
                .withPrimaryPrivateIPAddressDynamic()
                .create();

        vm.powerOff();
        vm.deallocate();

        // attach a new network interface and a new data disk, with delete options "DETACH"
        vm.update()
            .withNewDataDisk(1, 2, new VirtualMachineDiskOptions().withDeleteOptions(DeleteOptions.DETACH))
            .withExistingSecondaryNetworkInterface(secondaryNic2)
            .apply();

        // update all back to DELETE, including the newly added data disk and the secondary nic
        vm.update()
            .withPrimaryNetworkInterfaceDeleteOptions(DeleteOptions.DELETE)
            .withDataDisksDeleteOptions(DeleteOptions.DELETE, new ArrayList<>(vm.dataDisks().keySet()).toArray(new Integer[0]))
            .withNetworkInterfacesDeleteOptions(
                DeleteOptions.DELETE,
                vm.networkInterfaceIds().stream().filter(nic -> !nic.equals(vm.primaryNetworkInterfaceId())).toArray(String[]::new))
            .apply();

        Assertions.assertEquals(DeleteOptions.DELETE, vm.primaryNetworkInterfaceDeleteOptions());
        Assertions.assertTrue(vm.networkInterfaceIds().stream().allMatch(nicId -> DeleteOptions.DELETE.equals(vm.networkInterfaceDeleteOptions(nicId))));
        Assertions.assertTrue(vm.dataDisks().values().stream().allMatch(disk -> DeleteOptions.DELETE.equals(disk.deleteOptions())));

        // update all to DETACH
        vm.update()
            .withDataDisksDeleteOptions(DeleteOptions.DETACH)
            .withNetworkInterfacesDeleteOptions(DeleteOptions.DETACH)
            .apply();

        Assertions.assertTrue(vm.networkInterfaceIds().stream().allMatch(nicId -> DeleteOptions.DETACH.equals(vm.networkInterfaceDeleteOptions(nicId))));
        Assertions.assertTrue(vm.dataDisks().values().stream().allMatch(disk -> DeleteOptions.DETACH.equals(disk.deleteOptions())));
    }

    @Test
    public void testListVmByVmssId() {
        String vmssName = generateRandomResourceName("vmss", 15);
        String vmName = generateRandomResourceName("vm", 15);
        String vmName2 = generateRandomResourceName("vm", 15);

        VirtualMachineScaleSet vmss = computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .create();

        Assertions.assertEquals(0, computeManager.virtualMachines().listByVirtualMachineScaleSetId(vmss.id()).stream().count());

        VirtualMachine vm = computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.0/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withExistingVirtualMachineScaleSet(vmss)
            .create();

        Assertions.assertNotNull(vm.virtualMachineScaleSetId());

        VirtualMachine vm2 = computeManager.virtualMachines()
            .define(vmName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.0.16/28")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .create();

        Assertions.assertNull(vm2.virtualMachineScaleSetId());

        Assertions.assertEquals(1, computeManager.virtualMachines().listByVirtualMachineScaleSetId(vmss.id()).stream().count());
        Assertions.assertTrue(vm.id().equalsIgnoreCase(computeManager.virtualMachines().listByVirtualMachineScaleSetId(vmss.id()).stream().iterator().next().id()));
        Assertions.assertEquals(2, computeManager.virtualMachines().listByResourceGroup(rgName).stream().count());
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    @Disabled("This test is for listByVirtualMachineScaleSetId nextLink encoding. Backend pageSize may change, so we don't want to assert that.")
    public void testListByVmssIdNextLink() throws Exception {
        String vmssName = generateRandomResourceName("vmss", 15);
        String vnetName = generateRandomResourceName("vnet", 15);
        String vmName = generateRandomResourceName("vm", 15);
        int vmssCapacity = 70;

        // vm that's not in VMSS
        computeManager.virtualMachines()
            .define(vmName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .withNewPrimaryNetwork("10.0.1.0/24")
            .withPrimaryPrivateIPAddressDynamic()
            .withoutPrimaryPublicIPAddress()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .create();

        Network network = networkManager.networks().define(vnetName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet("subnet1", "10.0.0.0/24")
            .create();
        LoadBalancer publicLoadBalancer = createHttpLoadBalancers(region, this.resourceManager.resourceGroups().getByName(rgName), "1", LoadBalancerSkuType.STANDARD, PublicIPSkuType.STANDARD, true);
        VirtualMachineScaleSet vmss = computeManager.virtualMachineScaleSets()
            .define(vmssName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withFlexibleOrchestrationMode()
            .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
            .withExistingPrimaryNetworkSubnet(network, "subnet1")
            .withExistingPrimaryInternetFacingLoadBalancer(publicLoadBalancer)
            .withoutPrimaryInternalLoadBalancer()
            .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_16_04_LTS)
            .withRootUsername("jvuser")
            .withSsh(sshPublicKey())
            .withCapacity(vmssCapacity)
            .create();

        PagedIterable<VirtualMachine> vmPaged = computeManager.virtualMachines().listByVirtualMachineScaleSetId(vmss.id());
        Iterable<PagedResponse<VirtualMachine>> vmIterable = vmPaged.iterableByPage();
        int pageCount = 0;
        for (PagedResponse<VirtualMachine> response : vmIterable) {
            pageCount++;
            Assertions.assertEquals(200, response.getStatusCode());
        }

        Assertions.assertEquals(vmssCapacity, vmPaged.stream().count());
        Assertions.assertEquals(2, pageCount);
    }

    @Test
    public void canCreateVMWithEncryptionAtHost() {
        VirtualMachine vm = computeManager
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
            .withEncryptionAtHost()
            .create();

        Assertions.assertNotNull(vm.innerModel().securityProfile());
        Assertions.assertTrue(vm.isEncryptionAtHost());
    }

    @Test
    public void canUpdateVMWithEncryptionAtHost() {
        VirtualMachine vm = computeManager
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

        vm.deallocate();
        vm.update().withEncryptionAtHost().apply();

        Assertions.assertNotNull(vm.innerModel().securityProfile());
        Assertions.assertTrue(vm.isEncryptionAtHost());
    }

    @Test
    public void canUpdateVMWithoutEncryptionAtHost() {
        VirtualMachine vm = computeManager
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
            .withEncryptionAtHost()
            .create();

        vm.deallocate();
        vm.update().withoutEncryptionAtHost().apply();

        Assertions.assertNotNull(vm.innerModel().securityProfile());
        Assertions.assertFalse(vm.isEncryptionAtHost());
    }

    // *********************************** helper methods ***********************************

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
