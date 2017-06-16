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
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccounts;
import org.junit.Assert;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests Network Watcher.
 */
public class TestNetworkWatcher extends TestTemplate<NetworkWatcher, NetworkWatchers> {
    private static String TEST_ID = "";
    private static Region REGION = Region.US_NORTH_CENTRAL;
    private String groupName;
    private String nwName;

    private void initializeResourceNames() {
        TEST_ID = SdkContext.randomResourceName("", 8);
        groupName = "rg" + TEST_ID;
        nwName = "nw" + TEST_ID;
    }

    @Override
    public NetworkWatcher createResource(NetworkWatchers networkWatchers) throws Exception {
        // Network Watcher should be in the same region as monitored resources
        initializeResourceNames();
        NetworkWatcher nw = networkWatchers.define(nwName)
                .withRegion(REGION)
                .withNewResourceGroup()
                .withTag("tag1", "value1")
                .create();
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

    // Helper method to pre-create infrastructure to test Network Watcher
    VirtualMachine[] ensureNetwork(Networks networks, VirtualMachines vms, NetworkInterfaces networkInterfaces) throws Exception {
        // Create an NSG
        NetworkSecurityGroup nsg = networks.manager().networkSecurityGroups().define("nsg" + TEST_ID)
                .withRegion(REGION)
                .withNewResourceGroup(groupName)
                .create();

        // Create a network for the VMs
        Network network = networks.define("net" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .withAddressSpace("10.0.0.0/28")
                .defineSubnet("subnet1")
                    .withAddressPrefix("10.0.0.0/29")
                    .withExistingNetworkSecurityGroup(nsg)
                    .attach()
                .withSubnet("subnet2", "10.0.0.8/29")
                .create();

        NetworkInterface nic = networkInterfaces.define("ni" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withNewPrimaryPublicIPAddress("pipdns" + TEST_ID)
                .withIPForwarding()
                .withExistingNetworkSecurityGroup(nsg)
                .create();

        // Create the requested number of VM definitions
        String userName = "testuser" + TEST_ID;
        List<Creatable<VirtualMachine>> vmDefinitions = new ArrayList<>();

        Creatable<VirtualMachine> vm1 = vms.define(SdkContext.randomResourceName("vm", 15))
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .withExistingPrimaryNetworkInterface(nic)
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUsername(userName)
                .withRootPassword("Abcdef.123456")
                .withSize(VirtualMachineSizeTypes.STANDARD_A1)
                .defineNewExtension("packetCapture")
                    .withPublisher("Microsoft.Azure.NetworkWatcher")
                    .withType("NetworkWatcherAgentLinux")
                    .withVersion("1.4")
                    .withMinorVersionAutoUpgrade()
                    .attach();

        String vmName = SdkContext.randomResourceName("vm", 15);

        Creatable<VirtualMachine> vm2 = vms.define(vmName)
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .withExistingPrimaryNetwork(network)
                .withSubnet(network.subnets().values().iterator().next().name())
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_14_04_LTS)
                .withRootUsername(userName)
                .withRootPassword("Abcdef.123456")
                .withSize(VirtualMachineSizeTypes.STANDARD_A1);

        vmDefinitions.add(vm1);
        vmDefinitions.add(vm2);
        vms.create(vmDefinitions);
        CreatedResources<VirtualMachine> createdVMs2 = vms.create(vmDefinitions);
        VirtualMachine[] array = new VirtualMachine[createdVMs2.size()];
        for (int index = 0; index < createdVMs2.size(); index++) {
            array[index] = createdVMs2.get(vmDefinitions.get(index).key());
        }
        return array;
    }

    // create a storage account
    StorageAccount ensureStorageAccount(StorageAccounts storageAccounts) {
        return  storageAccounts.define("sa" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .create();
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

    public String groupName() {
        return groupName;
    }
}
