// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.resourcemanager.compute.models.Disk;
import com.azure.resourcemanager.compute.models.DiskSkuTypes;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.OffsetDateTime;

public class TestVirtualMachineSyncPoller extends TestTemplate<VirtualMachine, VirtualMachines> {

    private final NetworkManager networkManager;

    private final ClientLogger logger = new ClientLogger(TestVirtualMachineSyncPoller.class);

    public TestVirtualMachineSyncPoller(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String rgName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("rg", 10);
        final String vnetName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vnet", 10);
        final String nicName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("nic", 10);
        final String subnetName = "default";
        final String diskName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("disk", 10);
        final String ipName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("ip", 10);
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);
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
        logger.info("{} {}", OffsetDateTime.now(), "begin create public IP");
        Accepted<PublicIpAddress> publicIpAddressAccepted =
            this.networkManager.publicIpAddresses()
                .define(ipName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .beginCreate();
        logger.info("{} {}", OffsetDateTime.now(), "polling public IP till complete");
        PollResponse<?> publicIpAddressResponse = publicIpAddressAccepted.getSyncPoller().waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, publicIpAddressResponse.getStatus());
        PublicIpAddress publicIpAddress = publicIpAddressAccepted.getFinalResult();
        logger.info("{} {}", OffsetDateTime.now(), "public IP created");

        // nic and disk
        logger.info("{} {}", OffsetDateTime.now(), "begin create nic");
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

        logger.info("{} {}", OffsetDateTime.now(), "begin create data disk");
        Accepted<Disk> diskAccepted =
            virtualMachines.manager().disks()
                .define(diskName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withData()
                .withSizeInGB(100)
                .withSku(DiskSkuTypes.STANDARD_LRS)
                .beginCreate();

        // poll nic and disk
        LongRunningOperationStatus networkInterfaceLroStatus = networkInterfaceAccepted.getActivationResponse().getStatus();
        LongRunningOperationStatus diskLroStatus = diskAccepted.getActivationResponse().getStatus();
        SyncPoller<?, NetworkInterface> networkInterfaceSyncPoller = networkInterfaceAccepted.getSyncPoller();
        SyncPoller<?, Disk> diskSyncPoller = diskAccepted.getSyncPoller();
        while (!networkInterfaceLroStatus.isComplete() || !diskLroStatus.isComplete()) {
            ResourceManagerUtils.sleep(Duration.ofSeconds(1));

            if (!networkInterfaceLroStatus.isComplete()) {
                logger.info("{} {}", OffsetDateTime.now(), "poll network interface");
                networkInterfaceLroStatus = networkInterfaceSyncPoller.poll().getStatus();
            }
            if (!diskLroStatus.isComplete()) {
                logger.info("{} {}", OffsetDateTime.now(), "poll data disk");
                diskLroStatus = diskSyncPoller.poll().getStatus();
            }
        }
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, networkInterfaceLroStatus);
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, diskLroStatus);
        NetworkInterface networkInterface = networkInterfaceSyncPoller.getFinalResult();
        logger.info("{} {}", OffsetDateTime.now(), "network interface created");
        Disk disk = diskSyncPoller.getFinalResult();
        logger.info("{} {}", OffsetDateTime.now(), "data disk created");

        // virtual machine, poll till complete
        logger.info("{} {}", OffsetDateTime.now(), "begin create vm");
        Accepted<VirtualMachine> virtualMachineAccepted =
            virtualMachines
                .define(vmName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withExistingPrimaryNetworkInterface(networkInterface)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword(ResourceManagerTestBase.password())
                .withExistingDataDisk(disk)
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D2a_v4"))
                .beginCreate();
        logger.info("{} {}", OffsetDateTime.now(), "polling virtual machine till complete");
        PollResponse<?> virtualMachineResponse = virtualMachineAccepted.getSyncPoller().waitForCompletion();
        Assertions.assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, virtualMachineResponse.getStatus());
        logger.info("{} {}", OffsetDateTime.now(), "virtual machine created");

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
