/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management;


import com.azure.core.http.rest.PagedIterable;
import com.azure.management.compute.KnownLinuxVirtualMachineImage;
import com.azure.management.compute.VirtualMachine;
import com.azure.management.compute.VirtualMachineSizeTypes;
import com.azure.management.compute.VirtualMachines;
import com.azure.management.network.Network;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NetworkInterfaces;
import com.azure.management.network.NetworkSecurityGroup;
import com.azure.management.network.NetworkWatcher;
import com.azure.management.network.NetworkWatchers;
import com.azure.management.network.Networks;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.CreatedResources;
import com.azure.management.resources.fluentcore.utils.SdkContext;
import com.azure.management.storage.StorageAccount;
import com.azure.management.storage.StorageAccounts;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests Network Watcher.
 */
public class TestNetworkWatcher extends TestTemplate<NetworkWatcher, NetworkWatchers> {
    private String TEST_ID = "";
    private final static Region REGION = Region.US_SOUTH_CENTRAL;
    private String groupName;
    private String nwName;

    private void initializeResourceNames(SdkContext sdkContext) {
        TEST_ID = sdkContext.randomResourceName("", 8);
        groupName = "rg" + TEST_ID;
        nwName = "nw" + TEST_ID;
    }

    @Override
    public NetworkWatcher createResource(NetworkWatchers networkWatchers) throws Exception {
        // Network Watcher should be in the same region as monitored resources
        initializeResourceNames(networkWatchers.manager().getSdkContext());

        // make sure Network Watcher is disabled in current subscription and region as only one can exist
        PagedIterable<NetworkWatcher> nwList = networkWatchers.list();
        for (NetworkWatcher nw : nwList) {
            if (REGION.equals(nw.region())) {
                networkWatchers.deleteById(nw.id());
            }
        }
        // create Network Watcher
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
        Assertions.assertTrue(resource.tags().containsKey("tag2"));
        Assertions.assertFalse(resource.tags().containsKey("tag1"));

        resource.updateTags()
                .withTag("tag3", "value3")
                .withoutTag("tag2")
                .applyTags();
        Assertions.assertTrue(resource.tags().containsKey("tag3"));
        Assertions.assertFalse(resource.tags().containsKey("tag2"));
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

        Creatable<VirtualMachine> vm1 = vms.define(networks.manager().getSdkContext().randomResourceName("vm", 15))
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

        String vmName = networks.manager().getSdkContext().randomResourceName("vm", 15);

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
        return storageAccounts.define("sa" + TEST_ID)
                .withRegion(REGION)
                .withExistingResourceGroup(groupName)
                .withGeneralPurposeAccountKindV2()
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
