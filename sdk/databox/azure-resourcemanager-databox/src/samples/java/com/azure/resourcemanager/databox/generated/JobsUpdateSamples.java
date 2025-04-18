// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.databox.generated;

import com.azure.resourcemanager.databox.models.AddressType;
import com.azure.resourcemanager.databox.models.ContactDetails;
import com.azure.resourcemanager.databox.models.IdentityProperties;
import com.azure.resourcemanager.databox.models.JobResource;
import com.azure.resourcemanager.databox.models.KekType;
import com.azure.resourcemanager.databox.models.KeyEncryptionKey;
import com.azure.resourcemanager.databox.models.ResourceIdentity;
import com.azure.resourcemanager.databox.models.ShippingAddress;
import com.azure.resourcemanager.databox.models.UpdateJobDetails;
import com.azure.resourcemanager.databox.models.UserAssignedIdentity;
import com.azure.resourcemanager.databox.models.UserAssignedProperties;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Jobs Update.
 */
public final class JobsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/databox/resource-manager/Microsoft.DataBox/stable/2025-02-01/examples/JobsPatchCmk.json
     */
    /**
     * Sample code: JobsPatchCmk.
     * 
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatchCmk(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource = manager.jobs()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestJobName1", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDetails(new UpdateJobDetails().withKeyEncryptionKey(new KeyEncryptionKey()
                .withKekType(KekType.CUSTOMER_MANAGED)
                .withKekUrl("https://xxx.xxx.xx")
                .withKekVaultResourceId(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.KeyVault/vaults/YourKeyVaultName")))
            .apply();
    }

    /*
     * x-ms-original-file: specification/databox/resource-manager/Microsoft.DataBox/stable/2025-02-01/examples/
     * JobsPatchSystemAssignedToUserAssigned.json
     */
    /**
     * Sample code: JobsPatchSystemAssignedToUserAssigned.
     * 
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatchSystemAssignedToUserAssigned(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource = manager.jobs()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestJobName1", null,
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withIdentity(new ResourceIdentity().withType("SystemAssigned,UserAssigned")
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testIdentity",
                    new UserAssignedIdentity())))
            .withDetails(new UpdateJobDetails().withKeyEncryptionKey(new KeyEncryptionKey()
                .withKekType(KekType.CUSTOMER_MANAGED)
                .withIdentityProperties(new IdentityProperties().withType("UserAssigned")
                    .withUserAssigned(new UserAssignedProperties().withResourceId(
                        "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testIdentity")))
                .withKekUrl("https://xxx.xxx.xx")
                .withKekVaultResourceId(
                    "/subscriptions/YourSubscriptionId/resourceGroups/YourResourceGroupName/providers/Microsoft.KeyVault/vaults/YourKeyVaultName")))
            .apply();
    }

    /*
     * x-ms-original-file:
     * specification/databox/resource-manager/Microsoft.DataBox/stable/2025-02-01/examples/JobsPatch.json
     */
    /**
     * Sample code: JobsPatch.
     * 
     * @param manager Entry point to DataBoxManager.
     */
    public static void jobsPatch(com.azure.resourcemanager.databox.DataBoxManager manager) {
        JobResource resource = manager.jobs()
            .getByResourceGroupWithResponse("YourResourceGroupName", "TestJobName1", "details",
                com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withDetails(new UpdateJobDetails()
                .withContactDetails(new ContactDetails().withContactName("XXXX XXXX")
                    .withPhone("0000000000")
                    .withPhoneExtension("")
                    .withEmailList(Arrays.asList("xxxx@xxxx.xxx")))
                .withShippingAddress(new ShippingAddress().withStreetAddress1("XXXX XXXX")
                    .withStreetAddress2("XXXX XXXX")
                    .withCity("XXXX XXXX")
                    .withStateOrProvince("XX")
                    .withCountry("XX")
                    .withPostalCode("fakeTokenPlaceholder")
                    .withCompanyName("XXXX XXXX")
                    .withAddressType(AddressType.COMMERCIAL)))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
