// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.network.models.VirtualNetworkGateway;
import com.azure.resourcemanager.network.models.VirtualNetworkGatewaySkuName;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VirtualNetworkGatewayTests extends NetworkTests {

    private final static Region region = Region.US_WEST3;

    @Test
    public void testListBySubscription() {
        String group1 = generateRandomResourceName("rg1", 15);
        String group2 = generateRandomResourceName("rg2", 15);
        String gatewayName1 = generateRandomResourceName("vngw1", 15);
        String gatewayName2 = generateRandomResourceName("vngw2", 15);

        try {
            VirtualNetworkGateway gateway1 = createVirtualNetworkGateway(group1, gatewayName1);
            VirtualNetworkGateway gateway2 = createVirtualNetworkGateway(group2, gatewayName2);

            List<VirtualNetworkGateway> gatewayList
                = networkManager.virtualNetworkGateways().list().stream().collect(Collectors.toList());
            Assertions.assertTrue(gatewayList.size() >= 2);
            Set<String> gatewayNames = gatewayList.stream().map(HasName::name).collect(Collectors.toSet());
            Assertions.assertTrue(gatewayNames.containsAll(Arrays.asList(gatewayName1, gatewayName2)));
        } finally {
            try {
                resourceManager.resourceGroups().beginDeleteByName(group1);
            } catch (ManagementException e) {
                // ignore
            }
            try {
                resourceManager.resourceGroups().beginDeleteByName(group2);
            } catch (ManagementException e) {
                // ignore
            }
        }
    }

    private VirtualNetworkGateway createVirtualNetworkGateway(String groupName, String name) {
        return networkManager.virtualNetworkGateways()
            .define(name)
            .withRegion(region)
            .withNewResourceGroup(groupName)
            .withNewNetwork("10.0.0.0/25", "10.0.0.0/27")
            .withRouteBasedVpn()
            .withSku(VirtualNetworkGatewaySkuName.BASIC)
            .create();
    }
}
