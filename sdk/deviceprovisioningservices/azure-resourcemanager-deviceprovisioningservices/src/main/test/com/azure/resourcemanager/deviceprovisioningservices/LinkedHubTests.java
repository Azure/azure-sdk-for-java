// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotHubDefinitionDescription;
import com.azure.resourcemanager.iothub.IotHubManager;
import com.azure.resourcemanager.iothub.fluent.models.IotHubDescriptionInner;
import com.azure.resourcemanager.iothub.models.IotHubSku;
import com.azure.resourcemanager.iothub.models.IotHubSkuInfo;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.azure.resourcemanager.deviceprovisioningservices.Constants.DEFAULT_LOCATION;
import static com.azure.resourcemanager.deviceprovisioningservices.Constants.IOTHUB_OWNER_ACCESS_KEY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinkedHubTests extends DeviceProvisioningTestBase
{
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void LinkedHubsCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        IotHubManager iotHubManager = createIotHubManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            // Create an Iot Hub in the same resource group as the DPS instance
            String hubName = "JavaDpsControlPlaneSDKTestHub" + createRandomSuffix();
            IotHubDescriptionInner iotHubDescriptionInner =
                new IotHubDescriptionInner()
                    .withLocation(DEFAULT_LOCATION)
                    .withSku(new IotHubSkuInfo().withCapacity(1L).withName(IotHubSku.B1));

            iotHubDescriptionInner = iotHubManager
                .serviceClient()
                .getIotHubResources()
                .createOrUpdate(resourceGroup.name(), hubName, iotHubDescriptionInner);

            // Link that Iot Hub to the DPS instance
            List<IotHubDefinitionDescription> linkedHubs = new ArrayList<>();
            String hubKey = iotHubManager.iotHubResources().getKeysForKeyName(resourceGroup.name(), iotHubDescriptionInner.name(), IOTHUB_OWNER_ACCESS_KEY_NAME).primaryKey();
            String hubConnectionString = "HostName=" + hubName + ".azure-devices.net;SharedAccessKeyName=" + IOTHUB_OWNER_ACCESS_KEY_NAME + ";SharedAccessKey=" + hubKey;

            linkedHubs.add(
                new IotHubDefinitionDescription()
                    .withConnectionString(hubConnectionString)
                    .withLocation(iotHubDescriptionInner.location())
                    .withAllocationWeight(1)
                    .withApplyAllocationPolicy(true));

            IotDpsPropertiesDescription propertiesDescription = new IotDpsPropertiesDescription();
            propertiesDescription.withIotHubs(linkedHubs);

            provisioningServiceDescription = iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    provisioningServiceDescription.withProperties(propertiesDescription));

            // verify that the service returned view of the DPS instance has the right linked hubs
            assertEquals(1, provisioningServiceDescription.properties().iotHubs().size());
            assertEquals(hubName + ".azure-devices.net", provisioningServiceDescription.properties().iotHubs().iterator().next().name());
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
