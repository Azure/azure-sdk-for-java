// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.iothub;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Context;
import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.ErrorDetailsException;
import com.azure.resourcemanager.iothub.models.IotHubDescription;
import com.azure.resourcemanager.iothub.models.IotHubSkuInfo;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import static com.azure.resourcemanager.iothub.Constants.DEFAULT_INSTANCE_NAME;
import static com.azure.resourcemanager.iothub.Constants.DEFAULT_REGION;
import static org.junit.jupiter.api.Assertions.*;

public class IotHubResourceManagementTests extends IotHubTestBase {
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void ServiceCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotHubManager iotHubManager = createIotHubManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        String serviceName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();

        try {
            // Define the IoT Hub
            IotHubDescription createServiceDescription = iotHubManager
                .iotHubResources()
                .define(serviceName)
                .withRegion(DEFAULT_REGION)
                .withExistingResourceGroup(resourceGroup.name())
                .withSku(Constants.DefaultSku.INSTANCE)
                .create();

            // Deploy the IoT Hub
            IotHubDescriptionInner updatedIotHubServiceDescriptionInner =
                iotHubManager
                    .serviceClient()
                    .getIotHubResources()
                    .createOrUpdate(resourceGroup.name(), serviceName, createServiceDescription.innerModel());

            // Verify that the deployed IoT Hub has the expected SKU and name
            assertNotNull(updatedIotHubServiceDescriptionInner);
            assertEquals(Constants.DefaultSku.NAME, updatedIotHubServiceDescriptionInner.sku().name().toString());
            assertEquals(serviceName, updatedIotHubServiceDescriptionInner.name());

            // Try getting the newly created resource
            IotHubDescription getResponse = iotHubManager
                .iotHubResources()
                .getByResourceGroup(resourceGroup.name(), serviceName);

            assertNotNull(getResponse);
            assertNotNull(getResponse.etag());
            assertEquals(Constants.DefaultSku.INSTANCE.name().toString(), getResponse.sku().name().toString());
            assertEquals(Constants.DefaultSku.INSTANCE.capacity().longValue(), getResponse.sku().capacity().longValue());
            assertEquals(DEFAULT_REGION.toString(), getResponse.location());

            // Delete the service
            iotHubManager.iotHubResources().delete(resourceGroup.name(), serviceName, Context.NONE);
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void updateSKU() {
        ResourceManager resourceManager = createResourceManager();
        IotHubManager iotHubManager = createIotHubManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            // Deploy an IoT Hub
            IotHubDescriptionInner iotHubDescription =
                createIotHub(iotHubManager, resourceGroup);

            // locally increase the SKU capacity by 1
            long expectedSkuCapacity = iotHubDescription.sku().capacity() + 1;
            IotHubSkuInfo newSku =
                iotHubDescription
                    .sku()
                    .withCapacity(expectedSkuCapacity);

            // update the service representation to use the new SKU
            iotHubDescription = iotHubManager
                .serviceClient()
                .getIotHubResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    iotHubDescription.name(),
                    iotHubDescription.withSku(newSku));

            // verify that the returned IoT Hub description has the new capacity
            assertEquals(expectedSkuCapacity, iotHubDescription.sku().capacity());
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void CreateFailure() {
        ResourceManager resourceManager = createResourceManager();
        IotHubManager iotHubManager = createIotHubManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            iotHubManager
                .iotHubResources()
                .define("some invalid service name *&^-#2?")
                .withRegion(DEFAULT_REGION)
                .withExistingResourceGroup(resourceGroup.name())
                .withSku(Constants.DefaultSku.INSTANCE)
                .create();

            fail("Creating an IoT hub with an invalid name should have thrown an exception");
        } catch (ErrorDetailsException ex) {
            // expected throw
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
