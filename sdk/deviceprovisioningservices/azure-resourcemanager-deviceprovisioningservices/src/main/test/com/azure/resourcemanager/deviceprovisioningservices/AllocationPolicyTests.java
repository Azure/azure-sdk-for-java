// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.AllocationPolicy;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AllocationPolicyTests extends DeviceProvisioningTestBase
{
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void Get() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            AllocationPolicy allocationPolicy =
                provisioningServiceDescription
                    .properties()
                    .allocationPolicy();

            assertTrue(Constants.ALLOCATION_POLICIES.contains(allocationPolicy));
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void Update() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            // pick a new allocation policy that is different from the current allocation policy
            AllocationPolicy newAllocationPolicy = AllocationPolicy.GEO_LATENCY;
            if (provisioningServiceDescription.properties().allocationPolicy() == AllocationPolicy.GEO_LATENCY)
            {
                newAllocationPolicy = AllocationPolicy.HASHED;
            }

            // update the service's allocation policy to the new policy
            IotDpsPropertiesDescription propertiesDescription =
                provisioningServiceDescription
                    .properties()
                    .withAllocationPolicy(newAllocationPolicy);

            provisioningServiceDescription.withProperties(propertiesDescription);

            iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    provisioningServiceDescription);

        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
