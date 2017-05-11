/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management;

import com.microsoft.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.network.*;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.CreatedResources;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests Network Watcher.
 */
public class TestNetworkWatcher extends TestTemplate<NetworkWatcher, NetworkWatchers> {
    static String TEST_ID = "";
    static Region REGION = Region.US_NORTH_CENTRAL;
    static String GROUP_NAME = "";

    private final VirtualMachines vms;

    private static void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        GROUP_NAME = "rg" + TEST_ID;
    }

    public TestNetworkWatcher(VirtualMachines vms) {
        this.vms = vms;
    }

    @Override
    public NetworkWatcher createResource(NetworkWatchers networkWatchers) throws Exception {
        // Network Watcher should be in the same region as monitored resources
        initializeResourceNames();
        final String newNWName = SdkContext.randomResourceName("nw", 13);
        NetworkWatcher nw = networkWatchers.define(newNWName)
                .withRegion(REGION)
                .withNewResourceGroup()
                .withTag("tag1", "value1")
                .create();

        // pre-create VMs to show topology on
        VirtualMachine[] virtualMachines = ensureVMs(networkWatchers.manager().networks(), this.vms, 2);

        Topology topology = nw.topology(GROUP_NAME);
        Assert.assertEquals(7, topology.resources().size());
        Assert.assertTrue(topology.resources().containsKey("subnet1"));
        Assert.assertEquals(2, topology.resources().get(virtualMachines[0].getPrimaryNetworkInterface().name()).associations().size());
        Assert.assertEquals(0, topology.resources().get("subnet2").associations().size());
        return nw;
    }

    @Override
    public NetworkWatcher updateResource(NetworkWatcher resource) throws Exception {
        resource.update()
                .withTag("tag2", "value2")
                .withoutTag("tag1")
                .apply();
        resource.refresh();
        Assert.assertTrue(resource.tags().containsKey("tag2"));
        Assert.assertTrue(!resource.tags().containsKey("tag1"));
        return resource;
    }

    // Ensure VMs for the topology
    private static VirtualMachine[] ensureVMs(Networks networks, VirtualMachines vms, int count) throws Exception {
        // Create a network for the VMs
        Network network = networks.define("net" + TEST_ID)
                .withRegion(REGION)
                .withNewResourceGroup(GROUP_NAME)
                .withAddressSpace("10.0.0.0/28")
                .withSubnet("subnet1", "10.0.0.0/29")
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        // Create the requested number of VM definitions
        String userName = "testuser" + TEST_ID;
        List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String vmName = SdkContext.randomResourceName("vm", 15);

            Creatable<VirtualMachine> vm = vms.define(vmName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(GROUP_NAME)
                    .withExistingPrimaryNetwork(network)
                    .withSubnet(network.subnets().values().iterator().next().name())
                    .withPrimaryPrivateIPAddressDynamic()
                    .withoutPrimaryPublicIPAddress()
                    .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                    .withRootUsername(userName)
                    .withRootPassword("Abcdef.123456")
                    .withSize(VirtualMachineSizeTypes.STANDARD_A1);

            vmDefinitions.add(vm);
        }
        vms.create(vmDefinitions);
        CreatedResources<VirtualMachine> createdVMs2 = vms.create(vmDefinitions);
        VirtualMachine[] array = new VirtualMachine[createdVMs2.size()];
        for (int index = 0; index < createdVMs2.size(); index++) {
            array[index] = createdVMs2.get(vmDefinitions.get(index).key());
        }
        return array;
    }

    @Override
    public void print(NetworkWatcher nw) {
        StringBuilder info = new StringBuilder();
        info.append("Network Watcher: ").append(nw.id())
                .append("\n\tName: ").append(nw.name())
                .append("\n\tResource group: ").append(nw.resourceGroupName())
                .append("\n\tRegion: ").append(nw.regionName())
                .append("\n\tTags: ").append(nw.tags());
        System.out.println(info.toString());
    }
}
