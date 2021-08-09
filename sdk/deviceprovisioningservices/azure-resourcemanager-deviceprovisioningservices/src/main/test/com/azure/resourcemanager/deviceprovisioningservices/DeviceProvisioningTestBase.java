// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.ErrorDetailsException;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.NameAvailabilityInfo;
import com.azure.resourcemanager.deviceprovisioningservices.models.OperationInputs;
import com.azure.resourcemanager.deviceprovisioningservices.models.ProvisioningServiceDescription;
import com.azure.resourcemanager.iothub.IotHubManager;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeviceProvisioningTestBase extends TestBase {
    protected static final String DEFAULT_INSTANCE_NAME = "JavaDpsControlPlaneSDKTest";
    protected static final Region DEFAULT_REGION = Region.US_WEST_CENTRAL;

    public ResourceManager createResourceManager() {
        return ResourceManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE))
            .withDefaultSubscription();
    }

    public IotDpsManager createIotDpsManager() {
        return IotDpsManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));
    }

    public ResourceGroup createResourceGroup(ResourceManager resourceManager) {
        String resourceGroupName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();
        return resourceManager.resourceGroups()
            .define(resourceGroupName)
            .withRegion(DEFAULT_REGION)
            .create();
    }

    public IotHubManager createIotHubManager() {
        return IotHubManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), new AzureProfile(AzureEnvironment.AZURE));
    }

    public ProvisioningServiceDescriptionInner createProvisioningService(IotDpsManager iotDpsManager, ResourceGroup resourceGroup) {
        String serviceName = DEFAULT_INSTANCE_NAME + "-" + createRandomSuffix();

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

        ProvisioningServiceDescription provisioningServiceDescription = iotDpsManager
            .iotDpsResources()
            .define(serviceName)
            .withRegion(DEFAULT_REGION)
            .withExistingResourceGroup(resourceGroup.name())
            .withProperties(new IotDpsPropertiesDescription())
            .withSku(Constants.DefaultSku.INSTANCE)
            .create();

        ProvisioningServiceDescriptionInner inner = iotDpsManager
            .serviceClient()
            .getIotDpsResources()
            .createOrUpdate(resourceGroup.name(), serviceName, provisioningServiceDescription.innerModel());

        return inner;
    }

    public String createRandomSuffix() {
        // need to shorten the UUID since max service name is 50 characters
        return UUID.randomUUID().toString().substring(0, 18);
    }
}
