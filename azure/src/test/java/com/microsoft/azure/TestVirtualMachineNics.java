package com.microsoft.azure;

import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.KnownVirtualMachineImage;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkInterfaces;
import com.microsoft.azure.management.network.Networks;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.network.PublicIpAddresses;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import org.junit.Assert;

public class TestVirtualMachineNics extends TestTemplate<VirtualMachine, VirtualMachines> {
    private final NetworkInterfaces networkInterfaces;
    private final Networks networks;
    private final ResourceGroups resourceGroups;

    public TestVirtualMachineNics(ResourceGroups resourceGroups,
                                  Networks networks,
                                  NetworkInterfaces networkInterfaces) {
        this.resourceGroups = resourceGroups;
        this.networks = networks;
        this.networkInterfaces = networkInterfaces;
    }

    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        // Prepare the resource group definition
        final String rgName = "rg" + this.testId;
        ResourceGroup.DefinitionCreatable resourceGroupCreatable = this.resourceGroups
                .define(rgName)
                .withRegion(Region.US_EAST);

        // Prepare the virtual network definition [shared by primary and secondary network interfaces]
        final String vnetName = "vnet" + this.testId;
        Network.DefinitionCreatable networkCreatable = this.networks
                .define(vnetName)
                .withRegion(Region.US_EAST)
                .withNewGroup(resourceGroupCreatable)
                .withAddressSpace("10.0.0.0/28");

        // Prepare the secondary network interface definition
        final String secondaryNicName = "nic" + this.testId;
        NetworkInterface.DefinitionCreatable secondaryNetworkInterfaceCreatable = this.networkInterfaces
                .define(secondaryNicName)
                .withRegion(Region.US_EAST)
                .withNewGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.5");
                // .withNewPrimaryPublicIpAddress(secondaryPublicIpCreatable);
                // [Secondary NIC cannot have PublicIp - Only primary network interface can reference a public IP address]

        // Create Virtual Machine
        final String vmName = "vm" + this.testId;
        final String primaryPipName = "pip" + vmName;
        VirtualMachine virtualMachine = virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewGroup(resourceGroupCreatable)
                .withNewPrimaryNetwork(networkCreatable)
                .withPrimaryPrivateIpAddressStatic("10.0.0.4")
                .withNewPrimaryPublicIpAddress(primaryPipName)
                .withMarketplaceImage()
                .popular(KnownVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withWindowsOS()
                .withAdminUserName("testuser")
                .withPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_A9)
                .withNewSecondaryNetworkInterface(secondaryNetworkInterfaceCreatable)
                .create();

        Assert.assertTrue(virtualMachine.networkInterfaceIds().size() == 2);
        NetworkInterface primaryNetworkInterface = virtualMachine.primaryNetworkInterface();
        Assert.assertEquals(primaryNetworkInterface.primaryPrivateIp(), "10.0.0.4");

        PublicIpAddress primaryPublicIpAddress = primaryNetworkInterface.primaryPublicIpAddress();
        Assert.assertTrue(primaryPublicIpAddress.fqdn().startsWith(primaryPipName));
        return virtualMachine;
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine virtualMachine) throws Exception {
        final String secondaryNicName = "nic" + this.testId;
        virtualMachine = virtualMachine.update()
                .withoutSecondaryNetworkInterface(secondaryNicName)
                .apply();

        Assert.assertTrue(virtualMachine.networkInterfaceIds().size() == 1);
        return null;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        Utils.print(virtualMachine);
    }
}
