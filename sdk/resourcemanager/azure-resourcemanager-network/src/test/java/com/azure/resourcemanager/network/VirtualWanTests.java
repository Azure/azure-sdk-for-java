// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.network.models.VirtualWan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VirtualWanTests extends NetworkManagementTest {
    private final Region region = Region.US_WEST;
    private String vwName = "";

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        vwName = generateRandomResourceName("vw", 12);
        super.initializeClients(httpPipeline, profile);
    }

    @Test
    public void testCreateAndUpdateVirtualWan() {
        VirtualWan virtualWan = networkManager.virtualWans()
            .define(vwName)
            .withRegion(region)
            .withNewResourceGroup(rgName)
            .enableVpnEncryption()
            .withAllowBranchToBranchTraffic(true)
            .withVirtualWanType("Standard")
            .create();

        Assertions.assertEquals("Standard", virtualWan.virtualWanType());
        Assertions.assertTrue(virtualWan.allowBranchToBranchTraffic());
        Assertions.assertFalse(virtualWan.disabledVpnEncryption());

        virtualWan.update()
            .disableVpnEncryption()
            .withAllowBranchToBranchTraffic(false)
            .apply();
        Assertions.assertFalse(virtualWan.allowBranchToBranchTraffic());
        Assertions.assertTrue(virtualWan.disabledVpnEncryption());
    }
}
