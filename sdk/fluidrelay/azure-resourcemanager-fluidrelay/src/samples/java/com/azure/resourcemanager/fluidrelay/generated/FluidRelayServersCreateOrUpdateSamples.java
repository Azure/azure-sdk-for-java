// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.fluidrelay.generated;

import com.azure.resourcemanager.fluidrelay.models.CmkIdentityType;
import com.azure.resourcemanager.fluidrelay.models.CustomerManagedKeyEncryptionProperties;
import com.azure.resourcemanager.fluidrelay.models.CustomerManagedKeyEncryptionPropertiesKeyEncryptionKeyIdentity;
import com.azure.resourcemanager.fluidrelay.models.EncryptionProperties;
import com.azure.resourcemanager.fluidrelay.models.Identity;
import com.azure.resourcemanager.fluidrelay.models.ResourceIdentityType;
import com.azure.resourcemanager.fluidrelay.models.StorageSku;
import com.azure.resourcemanager.fluidrelay.models.UserAssignedIdentitiesValue;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FluidRelayServers CreateOrUpdate.
 */
public final class FluidRelayServersCreateOrUpdateSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/
     * FluidRelayServers_CreateOrUpdate.json
     */
    /**
     * Sample code: Create a Fluid Relay server.
     * 
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .withStoragesku(StorageSku.BASIC)
            .create();
    }

    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/
     * FluidRelayServers_CreateWithAmi.json
     */
    /**
     * Sample code: Create a Fluid Relay server with AMI.
     * 
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServerWithAMI(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(new Identity().withType(ResourceIdentityType.SYSTEM_ASSIGNED_USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id1",
                    new UserAssignedIdentitiesValue(),
                    "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/id2",
                    new UserAssignedIdentitiesValue())))
            .withStoragesku(StorageSku.BASIC)
            .create();
    }

    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/
     * FluidRelayServers_CreateWithCmk.json
     */
    /**
     * Sample code: Create a Fluid Relay server with CMK.
     * 
     * @param manager Entry point to FluidRelayManager.
     */
    public static void createAFluidRelayServerWithCMK(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        manager.fluidRelayServers()
            .define("myFluidRelayServer")
            .withRegion("west-us")
            .withExistingResourceGroup("myResourceGroup")
            .withTags(mapOf("Category", "sales"))
            .withIdentity(new Identity().withType(ResourceIdentityType.USER_ASSIGNED)
                .withUserAssignedIdentities(mapOf(
                    "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityForCMK",
                    new UserAssignedIdentitiesValue())))
            .withEncryption(
                new EncryptionProperties().withCustomerManagedKeyEncryption(new CustomerManagedKeyEncryptionProperties()
                    .withKeyEncryptionKeyIdentity(new CustomerManagedKeyEncryptionPropertiesKeyEncryptionKeyIdentity()
                        .withIdentityType(CmkIdentityType.USER_ASSIGNED)
                        .withUserAssignedIdentityResourceId(
                            "/subscriptions/xxxx-xxxx-xxxx-xxxx/resourceGroups/myResourceGroup/providers/Microsoft.ManagedIdentity/userAssignedIdentities/identityForCMK"))
                    .withKeyEncryptionKeyUrl("fakeTokenPlaceholder")))
            .withStoragesku(StorageSku.BASIC)
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
