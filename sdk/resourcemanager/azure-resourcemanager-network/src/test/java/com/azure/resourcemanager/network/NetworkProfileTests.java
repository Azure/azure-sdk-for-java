// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.resourcemanager.network.models.ContainerNetworkInterfaceConfiguration;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkProfile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NetworkProfileTests extends NetworkManagementTest {

    @Test
    public void canCRUDNetworkProfile() {
        Network network = networkManager.networks().define("vnet1")
            .withRegion(Region.US_EAST)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet("default", "10.0.0.0/24")
            .create();

        NetworkProfile networkProfile = networkManager.networkProfiles().define("profile1")
            .withRegion(Region.US_EAST)
            .withExistingResourceGroup(rgName)
            .withContainerNetworkInterfaceConfiguration("eth1", "ipconfig1", network.id(), "default")
            .withTag("tag.1", "value.1")
            .create();

        Assertions.assertEquals(1, networkProfile.containerNetworkInterfaceConfigurations().size());
        ContainerNetworkInterfaceConfiguration configuration = networkProfile.containerNetworkInterfaceConfigurations().iterator().next();
        Assertions.assertEquals("eth1", configuration.name());
        Assertions.assertEquals("ipconfig1", configuration.ipConfigurations().iterator().next().name());
        Assertions.assertEquals(network.subnets().get("default").id(), configuration.ipConfigurations().iterator().next().subnet().id());

        Assertions.assertEquals(1, networkManager.networkProfiles().listByResourceGroup(rgName).stream().count());
        Assertions.assertEquals(networkProfile.name(), networkManager.networkProfiles().getById(networkProfile.id()).name());

        networkProfile.update()
            .withTag("tag.2", "value.2")
            .apply();

        Assertions.assertEquals(1, networkProfile.containerNetworkInterfaceConfigurations().size());
    }
}
