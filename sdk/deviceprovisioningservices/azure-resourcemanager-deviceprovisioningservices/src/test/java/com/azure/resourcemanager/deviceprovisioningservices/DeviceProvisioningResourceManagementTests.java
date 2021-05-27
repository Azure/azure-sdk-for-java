// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Context;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.ErrorDetailsException;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsSkuInfo;
import com.azure.resourcemanager.deviceprovisioningservices.models.NameAvailabilityInfo;
import com.azure.resourcemanager.deviceprovisioningservices.models.OperationInputs;
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeviceProvisioningResourceManagementTests extends DeviceProvisioningTestBase
{
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void ServiceCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        String serviceName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();
        try {
            try {
                iotDpsManager.iotDpsResources()
                    .checkProvisioningServiceNameAvailability(new OperationInputs().withName(serviceName));
            } catch (ErrorDetailsException ex) {
                // error code signifies that the resource name is not available, need to delete it before creating a
                // new one.
                if (ex.getValue().getHttpStatusCode().equals("404307")) {
                    // Delete the service if it already exists
                    iotDpsManager.iotDpsResources().delete(resourceGroup.name(), serviceName, Context.NONE);

                    // After deleting the existing service, check that the name is now available to use
                    NameAvailabilityInfo availabilityInfo = iotDpsManager.iotDpsResources()
                        .checkProvisioningServiceNameAvailability(new OperationInputs().withName(serviceName));

                    assertTrue(
                        availabilityInfo.nameAvailable(),
                        "Service name was unavailable even after deleting the existing service with the name");
                }
            }

            ProvisioningServiceDescription createServiceDescription = iotDpsManager
                .iotDpsResources()
                .define(serviceName)
                .withRegion(DEFAULT_REGION)
                .withExistingResourceGroup(resourceGroup.name())
                .withProperties(new IotDpsPropertiesDescription())
                .withSku(Constants.DefaultSku.INSTANCE)
                .create();

            ProvisioningServiceDescriptionInner updatedProvisioningServiceDescriptionInner =
                iotDpsManager
                    .serviceClient()
                    .getIotDpsResources()
                    .createOrUpdate(resourceGroup.name(), serviceName, createServiceDescription.innerModel());

            assertNotNull(updatedProvisioningServiceDescriptionInner);
            assertEquals(Constants.DefaultSku.NAME, updatedProvisioningServiceDescriptionInner.sku().name().toString());
            assertEquals(serviceName, updatedProvisioningServiceDescriptionInner.name());

            // Try getting the newly created resource
            ProvisioningServiceDescription getResponse = iotDpsManager
                .iotDpsResources()
                .getByResourceGroup(resourceGroup.name(), serviceName);

            assertNotNull(getResponse);
            assertNotNull(getResponse.etag());
            assertEquals(Constants.DefaultSku.INSTANCE.name().toString(), getResponse.sku().name().toString());
            assertEquals(Constants.DefaultSku.INSTANCE.capacity().longValue(), getResponse.sku().capacity().longValue());
            assertEquals(DEFAULT_REGION.toString(), getResponse.location());

            // Delete the service
            iotDpsManager.iotDpsResources().delete(resourceGroup.name(), serviceName, Context.NONE);
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void updateSKU() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            // create the provisioning service
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            // locally increase the SKU capacity by 1
            long expectedSkuCapacity = provisioningServiceDescription.sku().capacity() + 1;
            IotDpsSkuInfo newSku =
                provisioningServiceDescription
                    .sku()
                    .withCapacity(expectedSkuCapacity);

            // update the service representation to use the new SKU
            provisioningServiceDescription = iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    provisioningServiceDescription.withSku(newSku));

            assertEquals(expectedSkuCapacity, provisioningServiceDescription.sku().capacity());
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }

    @Test
    @DoNotRecord(skipInPlayback = true)
    public void CreateFailure() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            iotDpsManager
                .iotDpsResources()
                .define("some invalid service name *&^-#2?")
                .withRegion(DEFAULT_REGION)
                .withExistingResourceGroup(resourceGroup.name())
                .withProperties(new IotDpsPropertiesDescription())
                .withSku(Constants.DefaultSku.INSTANCE)
                .create();

            fail("Creating a device provisioning service with an invalid name should have thrown an exception");
        } catch (ErrorDetailsException ex) {
            // expected throw
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
