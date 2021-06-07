// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.deviceprovisioningservices;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.ProvisioningServiceDescriptionInner;
import com.azure.resourcemanager.deviceprovisioningservices.fluent.models.SharedAccessSignatureAuthorizationRuleInner;
import com.azure.resourcemanager.deviceprovisioningservices.models.AccessRightsDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.ErrorDetailsException;
import com.azure.resourcemanager.deviceprovisioningservices.models.IotDpsPropertiesDescription;
import com.azure.resourcemanager.deviceprovisioningservices.models.SharedAccessSignatureAuthorizationRule;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.azure.resourcemanager.deviceprovisioningservices.Constants.OWNER_ACCESS_KEY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class SharedAccessPolicyTests extends DeviceProvisioningTestBase
{
    @Test
    @DoNotRecord(skipInPlayback = true)
    public void SharedAccessPolicyCRUD() {
        ResourceManager resourceManager = createResourceManager();
        IotDpsManager iotDpsManager = createIotDpsManager();
        ResourceGroup resourceGroup = createResourceGroup(resourceManager);

        try {
            ProvisioningServiceDescriptionInner provisioningServiceDescription =
                createProvisioningService(iotDpsManager, resourceGroup);

            // verify owner key has been created
            SharedAccessSignatureAuthorizationRule ownerKey =
                iotDpsManager
                    .iotDpsResources()
                    .listKeysForKeyName(provisioningServiceDescription.name(), OWNER_ACCESS_KEY_NAME, resourceGroup.name());

            assertEquals(OWNER_ACCESS_KEY_NAME, ownerKey.keyName());

            // verify that getting an undefined key makes listKeysForKeyName throw
            try {
                iotDpsManager
                    .iotDpsResources()
                    .listKeysForKeyName(provisioningServiceDescription.name(), "thisKeyDoesNotExist", resourceGroup.name());

                fail("Getting a key that does not exist should have thrown an exception");
            } catch (ErrorDetailsException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getResponse().getStatusCode());
            }

            // verify that you can create a new key
            String newKeyName = "someNewKey";
            AccessRightsDescription expectedAccessRights = AccessRightsDescription.DEVICE_CONNECT;
            SharedAccessSignatureAuthorizationRuleInner newKey =
                new SharedAccessSignatureAuthorizationRuleInner()
                    .withKeyName(newKeyName)
                    .withRights(expectedAccessRights);

            List<SharedAccessSignatureAuthorizationRuleInner> authorizationPolicies = new ArrayList<>(2);
            authorizationPolicies.add(ownerKey.innerModel());
            authorizationPolicies.add(newKey);

            IotDpsPropertiesDescription propertiesWithNewKey = provisioningServiceDescription
                .properties()
                .withAuthorizationPolicies(authorizationPolicies);

            ProvisioningServiceDescriptionInner serviceWithNewProperties =
                provisioningServiceDescription.withProperties(propertiesWithNewKey);

            iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    serviceWithNewProperties.name(),
                    serviceWithNewProperties);

            // after updating the service, the key should be retrievable
            SharedAccessSignatureAuthorizationRule retrievedNewKey = iotDpsManager
                .iotDpsResources()
                .listKeysForKeyName(provisioningServiceDescription.name(), newKeyName, resourceGroup.name());

            assertEquals(expectedAccessRights, retrievedNewKey.rights());

            // verify that the new key can be deleted
            provisioningServiceDescription.properties().authorizationPolicies().remove(newKey);

            iotDpsManager
                .serviceClient()
                .getIotDpsResources()
                .createOrUpdate(
                    resourceGroup.name(),
                    provisioningServiceDescription.name(),
                    provisioningServiceDescription);

            // verify that the new key was deleted by attempting to retrieve it
            try {
                iotDpsManager
                    .iotDpsResources()
                    .listKeysForKeyName(provisioningServiceDescription.name(), newKeyName, resourceGroup.name());

                fail("Getting a key that does not exist should have thrown an exception");
            } catch (ErrorDetailsException ex) {
                assertEquals(HttpURLConnection.HTTP_NOT_FOUND, ex.getResponse().getStatusCode());
            }
        } finally {
            // No matter if the test fails or not, delete the resource group that contains these test resources
            resourceManager.resourceGroups().beginDeleteByName(resourceGroup.name());
        }
    }
}
