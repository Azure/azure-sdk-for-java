// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.management.Region;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DeviceProvisioningResourceManagementTests extends DeviceProvisioningResourceManagementTestBase
{
    private static final Region DEFAULT_REGION = Region.US_WEST_CENTRAL;

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void CreateAndDelete() {
        if (skipInPlayback()) {
            return;
        }

        // try to create a DPS service
        ProvisioningServiceDescription createServiceDescription =
            iotDpsManager.iotDpsResources().getByResourceGroup(resourceGroupName, provisioningServiceName);

        ProvisioningServiceDescriptionInner updatedProvisioningServiceDescriptionInner =
            iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(resourceGroupName, provisioningServiceName, createServiceDescription.innerModel());

        assertNotNull(updatedProvisioningServiceDescriptionInner);
        assertEquals(Constants.DefaultSku.NAME, updatedProvisioningServiceDescriptionInner.sku().name().toString());
        assertEquals(provisioningServiceName, updatedProvisioningServiceDescriptionInner.name());
    }
}
