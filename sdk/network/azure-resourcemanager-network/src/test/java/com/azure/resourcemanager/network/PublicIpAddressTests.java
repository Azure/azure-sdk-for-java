// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network;

import com.azure.core.management.Region;
import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PublicIpAddressTests extends NetworkTests {

    private static final Region REGION = Region.US_WEST3;

    @Test
    public void testStandardSku() {
        String pipName = generateRandomResourceName("pip", 10);
        // STANDARD
        PublicIpAddress pip = networkManager.publicIpAddresses()
            .define(pipName)
            .withRegion(REGION)
            .withNewResourceGroup(rgName)
            .withSku(PublicIPSkuType.STANDARD)
            .create();
        Assertions.assertEquals(IpAllocationMethod.STATIC, pip.ipAllocationMethod());

        networkManager.publicIpAddresses().deleteById(pip.id());

        // STANDARD_V2
        pip = networkManager.publicIpAddresses()
            .define(pipName)
            .withRegion(REGION)
            .withExistingResourceGroup(rgName)
            .withSku(PublicIPSkuType.STANDARD_V2)
            .create();
        Assertions.assertEquals(IpAllocationMethod.STATIC, pip.ipAllocationMethod());

        networkManager.publicIpAddresses().deleteById(pip.id());

        // If user explicitly sets "withDynamicIP", SDK will use it to create Public IP Address.
        // In this case, service will return 400 StandardAndStandardV2SkuPublicIPAddressesMustBeStatic
        Assertions.assertThrows(ManagementException.class, () -> {
            PublicIpAddress pipToFail = networkManager.publicIpAddresses()
                .define(pipName)
                .withRegion(REGION)
                .withNewResourceGroup(rgName)
                .withDynamicIP()
                .withSku(PublicIPSkuType.STANDARD)
                .create();
        });
    }
}
