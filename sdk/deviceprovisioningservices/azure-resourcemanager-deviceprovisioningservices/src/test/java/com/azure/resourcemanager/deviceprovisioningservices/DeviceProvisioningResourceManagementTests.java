// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.NameAvailabilityInfo;
import com.azure.resourcemanager.deviceprovisioningservices.models.OperationInputs;
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceProvisioningResourceManagementTests extends TestBase {

    private static final Region DEFAULT_REGION = Region.US_WEST_CENTRAL;

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void CreateAndDelete() {
        String testName = "testingDPSCreateUpdate";
        String resourceGroupName = testName;
        String provisioningServiceName = testName;

        ResourceManager resourceManager = ResourceManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
            .withDefaultSubscription();

        IotDpsManager iotDpsManager = IotDpsManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));

        ResourceGroup group = resourceManager.resourceGroups()
            .define(resourceGroupName)
            .withRegion(DEFAULT_REGION)
            .create();

        Assertions.assertNotNull(group);

        NameAvailabilityInfo nameAvailabilityInfo =
            iotDpsManager
                .iotDpsResources()
                .checkProvisioningServiceNameAvailability(new OperationInputs().withName(testName));

        if (nameAvailabilityInfo.nameAvailable() != null && !nameAvailabilityInfo.nameAvailable())
        {
            // it exists, so test deleting it
            iotDpsManager.iotDpsResources().delete(testName, resourceGroupName, null);

            // check the name is now available
            nameAvailabilityInfo = iotDpsManager
                .iotDpsResources()
                .checkProvisioningServiceNameAvailability(new OperationInputs().withName(testName));

            assertTrue(nameAvailabilityInfo.nameAvailable());
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
        assertEquals(testName, updatedProvisioningServiceDescriptionInner.name());
    }
}
