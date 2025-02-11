// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.fluent.models.BastionHostInner;
import com.azure.resourcemanager.network.models.BastionHostIpConfiguration;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;

public class NetworkTests extends NetworkManagementTest {

    private static final Region REGIN = Region.US_WEST3;

    @Test
    public void testSubnetNetworkInterfaceIPConfigurationWithBastion() {
        String vnetName = generateRandomResourceName("vnet", 15);
        String pipName = generateRandomResourceName("pip", 15);
        String bastionName = generateRandomResourceName("bastion", 15);

        Network network = networkManager.networks()
            .define(vnetName)
            .withRegion(REGIN)
            .withNewResourceGroup(rgName)
            .withAddressSpace("10.0.0.0/24")
            .withSubnet("default", "10.0.0.0/25")
            .withSubnet("AzureBastionSubnet", "10.0.0.128/25")
            .create();

        PublicIpAddress publicIpBastion = networkManager.publicIpAddresses()
            .define(pipName)
            .withRegion(REGIN)
            .withExistingResourceGroup(rgName)
            .withSku(PublicIPSkuType.STANDARD)
            .withStaticIP()
            .create();

        networkManager.networks()
            .manager()
            .serviceClient()
            .getBastionHosts()
            .createOrUpdate(rgName, bastionName, new BastionHostInner().withLocation(REGIN.toString())
                .withIpConfigurations(Collections.singletonList(new BastionHostIpConfiguration().withName("ipconfig1")
                    .withSubnet(network.subnets().get("AzureBastionSubnet").innerModel())
                    .withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC)
                    .withPublicIpAddress(new SubResource().withId(publicIpBastion.id())))));

        // get a new instance of the network after creating bastion
        network = networkManager.networks().getById(network.id());

        Assertions.assertEquals(2, network.subnets().size());
        for (Subnet subnet : network.subnets().values()) {
            Collection<NicIpConfiguration> nicIpConfigurations = subnet.listNetworkInterfaceIPConfigurations();
            // no NIC on either of the 2 subnets
            Assertions.assertEquals(0, nicIpConfigurations.size());
        }
    }
}
