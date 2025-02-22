// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devtestlabs.generated;

import com.azure.resourcemanager.devtestlabs.models.IdentityProperties;
import com.azure.resourcemanager.devtestlabs.models.ManagedIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for ServiceRunners CreateOrUpdate.
 */
public final class ServiceRunnersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/devtestlabs/resource-manager/Microsoft.DevTestLab/stable/2018-09-15/examples/
     * ServiceRunners_CreateOrUpdate.json
     */
    /**
     * Sample code: ServiceRunners_CreateOrUpdate.
     * 
     * @param manager Entry point to DevTestLabsManager.
     */
    public static void serviceRunnersCreateOrUpdate(com.azure.resourcemanager.devtestlabs.DevTestLabsManager manager) {
        manager.serviceRunners()
            .define("{servicerunnerName}")
            .withRegion("{location}")
            .withExistingLab("resourceGroupName", "{devtestlabName}")
            .withTags(mapOf("tagName1", "tagValue1"))
            .withIdentity(new IdentityProperties().withType(ManagedIdentityType.fromString("{identityType}"))
                .withPrincipalId("{identityPrincipalId}")
                .withTenantId("{identityTenantId}")
                .withClientSecretUrl("fakeTokenPlaceholder"))
            .create();
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
