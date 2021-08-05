// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.azure.resourcemanager.compute.models.VirtualMachines;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import org.junit.jupiter.api.Assertions;

public class TestVirtualMachineNics extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final NetworkManager networkManager;
    private String secondaryNicName;

    public TestVirtualMachineNics(NetworkManager networkManager) {
        this.networkManager = networkManager;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        // Prepare the resource group definition
        final String rgName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("rg", 10);

        Creatable<ResourceGroup> resourceGroupCreatable =
            virtualMachines.manager().resourceManager().resourceGroups().define(rgName).withRegion(Region.US_EAST);

        // Prepare the virtual network definition [shared by primary and secondary network interfaces]
        final String vnetName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vnet", 10);

        Creatable<Network> networkCreatable =
            this
                .networkManager
                .networks()
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare the secondary network interface definition
        secondaryNicName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("nic", 10);

        Creatable<NetworkInterface> secondaryNetworkInterfaceCreatable =
            this
                .networkManager
                .networkInterfaces()
                .define(secondaryNicName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.5");
        // .withNewPrimaryPublicIPAddress();
        // [Secondary NIC cannot have PublicIP - Only primary network interface can reference a public IP address]

        // Prepare the secondary network interface definition
        final String secondaryNicName2 = virtualMachines.manager().resourceManager().internalContext().randomResourceName("nic2", 10);

        Creatable<NetworkInterface> secondaryNetworkInterfaceCreatable2 =
            this
                .networkManager
                .networkInterfaces()
                .define(secondaryNicName2)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.6");

        // Create Virtual Machine
        final String vmName = virtualMachines.manager().resourceManager().internalContext().randomResourceName("vm", 10);

        final String primaryPipName = "pip" + vmName;
        VirtualMachine virtualMachine =
            virtualMachines
                .define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIPAddressStatic("10.0.0.4")
                .withNewPrimaryPublicIPAddress(primaryPipName)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("testuser")
                .withRootPassword(ResourceManagerTestBase.password())
                .withSize(VirtualMachineSizeTypes.fromString("Standard_D8a_v4"))
                .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable)
                .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable2)
                .create();

        Assertions.assertTrue(virtualMachine.networkInterfaceIds().size() == 3);
        NetworkInterface primaryNetworkInterface = virtualMachine.getPrimaryNetworkInterface();
        Assertions.assertEquals(primaryNetworkInterface.primaryPrivateIP(), "10.0.0.4");

        PublicIpAddress primaryPublicIpAddress = primaryNetworkInterface.primaryIPConfiguration().getPublicIpAddress();
        Assertions.assertTrue(primaryPublicIpAddress.fqdn().startsWith(primaryPipName));
        return virtualMachine;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        virtualMachine.powerOff();
        virtualMachine.deallocate();
        virtualMachine = virtualMachine.update().withoutSecondaryNetworkInterface(secondaryNicName).apply();

        Assertions.assertTrue(virtualMachine.networkInterfaceIds().size() == 2);
        return virtualMachine;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }
}
