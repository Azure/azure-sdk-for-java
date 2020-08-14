// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;

public class TestVirtualMachineSyncPoller extends TestTemplate<VirtualMachine, VirtualMachines> {

    private final NetworkManager networkManager;

    public TestVirtualMachineSyncPoller(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String rgName = virtualMachines.manager().sdkContext().randomResourceName("rg", 10);
        final String vnetName = virtualMachines.manager().sdkContext().randomResourceName("vnet", 10);
        final String nicName = virtualMachines.manager().sdkContext().randomResourceName("nic", 10);
        final String subnetName = "default";
        final String diskName = virtualMachines.manager().sdkContext().randomResourceName("disk", 10);
        final String ipName = virtualMachines.manager().sdkContext().randomResourceName("ip", 10);
        final String vmName = virtualMachines.manager().sdkContext().randomResourceName("vm", 10);
        final Region region = Region.US_EAST;

        // network
        Network network =
            this.networkManager.networks()
                .define(vnetName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/27")
                .withSubnet(subnetName, "10.0.0.0/28")
                .create();

        // public ip address, poll till complete
        Accepted<PublicIpAddress> publicIpAddressAccepted =
            this.networkManager.publicIpAddresses()
                .define(ipName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .beginCreate();
        PollResponse<?> publicIpAddressResponse = publicIpAddressAccepted.getSyncPoller().waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, publicIpAddressResponse.getStatus());
        PublicIpAddress publicIpAddress = publicIpAddressAccepted.getFinalResult();

        // nic and disk
        Accepted<NetworkInterface> networkInterfaceAccepted =
            this.networkManager.networkInterfaces()
                .define(nicName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetwork(network)
                .withSubnet(subnetName)
                .withPrimaryPrivateIPAddressDynamic()
                .withExistingPrimaryPublicIPAddress(publicIpAddress)
                .beginCreate();

        Accepted<Disk> diskAccepted =
            virtualMachines.manager().disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(2)
                .beginCreate();

        // poll nic and disk
        SyncPoller<?, NetworkInterface> networkInterfaceSyncPoller = networkInterfaceAccepted.getSyncPoller();
        SyncPoller<?, Disk> diskSyncPoller = diskAccepted.getSyncPoller();
        PollResponse<?> networkInterfacePollResponse = networkInterfaceSyncPoller.poll();
        PollResponse<?> diskPollResponse = diskSyncPoller.poll();
        while (!networkInterfacePollResponse.getStatus().isComplete() || !diskPollResponse.getStatus().isComplete()) {
            Thread.sleep(Duration.ofSeconds(10).toMillis());

            if (!networkInterfacePollResponse.getStatus().isComplete()) {
                networkInterfacePollResponse = networkInterfaceSyncPoller.poll();
            }
            if (!diskPollResponse.getStatus().isComplete()) {
                diskPollResponse = diskSyncPoller.poll();
            }
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, networkInterfacePollResponse.getStatus());
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, diskPollResponse.getStatus());
        NetworkInterface networkInterface = networkInterfaceSyncPoller.getFinalResult();
        Disk disk = diskSyncPoller.getFinalResult();

        // virtual machine, poll till complete
        Accepted<VirtualMachine> virtualMachineAccepted =
            virtualMachines
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetworkInterface(networkInterface)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword(TestBase.password())
                .withExistingDataDisk(disk)
                .withSize(VirtualMachineSizeTypes.STANDARD_A9)
                .beginCreate();
        PollResponse<?> virtualMachineResponse = virtualMachineAccepted.getSyncPoller().waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, virtualMachineResponse.getStatus());

        return virtualMachineAccepted.getFinalResult();
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine resource) throws Exception {
        return resource;
    }

    @Override
    public void print(VirtualMachine resource) {

    }
}
