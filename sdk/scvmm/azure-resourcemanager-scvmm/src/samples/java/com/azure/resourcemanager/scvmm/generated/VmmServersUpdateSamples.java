// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.scvmm.generated;

import com.azure.core.util.Context;
import com.azure.resourcemanager.scvmm.models.VmmServer;
import java.util.HashMap;
import java.util.Map;

/** Samples for VmmServers Update. */
public final class VmmServersUpdateSamples {
    /*
     * x-ms-original-file: specification/scvmm/resource-manager/Microsoft.ScVmm/preview/2020-06-05-preview/examples/UpdateVMMServer.json
     */
    /**
     * Sample code: UpdateVMMServer.
     *
     * @param manager Entry point to ScvmmManager.
     */
    public static void updateVMMServer(com.azure.resourcemanager.scvmm.ScvmmManager manager) {
        VmmServer resource =
            manager.vmmServers().getByResourceGroupWithResponse("testrg", "ContosoVMMServer", Context.NONE).getValue();
        resource.update().withTags(mapOf("tag1", "value1", "tag2", "value2")).apply();
    }

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
