// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.fluidrelay.generated;

import com.azure.resourcemanager.fluidrelay.models.FluidRelayServer;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for FluidRelayServers Update.
 */
public final class FluidRelayServersUpdateSamples {
    /*
     * x-ms-original-file: specification/fluidrelay/resource-manager/Microsoft.FluidRelay/stable/2022-06-01/examples/
     * FluidRelayServers_Update.json
     */
    /**
     * Sample code: Update a Fluid Relay server.
     * 
     * @param manager Entry point to FluidRelayManager.
     */
    public static void updateAFluidRelayServer(com.azure.resourcemanager.fluidrelay.FluidRelayManager manager) {
        FluidRelayServer resource = manager.fluidRelayServers()
            .getByResourceGroupWithResponse("myResourceGroup", "myFluidRelayServer", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("Category", "sales")).apply();
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
