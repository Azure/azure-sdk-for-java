// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance;

import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.Network;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ContainerGroupTest extends ContainerInstanceManagementTest {
    @Test
    public void testContainerGroupWithVirtualNetwork() {
        String containerGroupName = generateRandomResourceName("container", 20);
        Region region = Region.US_EAST;

        ContainerGroup containerGroup =
            containerInstanceManager
                .containerGroups()
                .define(containerGroupName)
                .withRegion(region)
                .withNewResourceGroup(rgName)
                .withLinux()
                .withPublicImageRegistryOnly()
                .withoutVolume()
                .withContainerInstance("nginx", 80)
                .withNewVirtualNetwork("10.0.0.0/24")
                .create();

        Assertions.assertNotNull(containerGroup.networkProfileId());

        containerInstanceManager.containerGroups().deleteById(containerGroup.id());

        final String subnetName = "default";
        final String networkProfileName = "aci-vnet-profile";
        final String containerGroupName1 = generateRandomResourceName("container", 20);
        final String containerGroupName2 = generateRandomResourceName("container", 20);

        Network vnet = containerInstanceManager.networkManager().networks().define("vnet1")
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withAddressSpace("10.1.0.0/24")
            .defineSubnet(subnetName)
                .withAddressPrefix("10.1.0.0/24")
                .withDelegation("Microsoft.ContainerInstance/containerGroups")
                .attach()
            .create();

        ContainerGroup containerGroup1 = containerInstanceManager.containerGroups().define(containerGroupName1)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withLinux()
            .withPublicImageRegistryOnly()
            .withoutVolume()
            .withContainerInstance("nginx", 80)
            .withNewNetworkProfileOnExistingVirtualNetwork(networkProfileName, vnet.id(), subnetName)
            .create();

        ContainerGroup containerGroup2 = containerInstanceManager.containerGroups().define(containerGroupName2)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withLinux()
            .withPublicImageRegistryOnly()
            .withoutVolume()
            .withContainerInstance("nginx", 80)
            .withExistingNetworkProfile(containerGroup1.networkProfileId())
            .create();

        Assertions.assertEquals(containerGroup1.networkProfileId(), containerGroup2.networkProfileId());
    }
}
