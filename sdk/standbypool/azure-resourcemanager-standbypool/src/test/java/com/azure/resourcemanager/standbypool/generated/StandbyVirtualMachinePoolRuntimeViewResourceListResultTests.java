// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.

package com.azure.resourcemanager.standbypool.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.standbypool.implementation.models.StandbyVirtualMachinePoolRuntimeViewResourceListResult;
import org.junit.jupiter.api.Assertions;

public final class StandbyVirtualMachinePoolRuntimeViewResourceListResultTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        StandbyVirtualMachinePoolRuntimeViewResourceListResult model = BinaryData.fromString(
            "{\"value\":[{\"properties\":{\"instanceCountSummary\":[{\"zone\":435793093682081047,\"instanceCountsByState\":[{\"state\":\"izuckyfihrfidfvz\",\"count\":576595030144008235},{\"state\":\"uht\",\"count\":6116347242940995661}]}],\"provisioningState\":\"Succeeded\"},\"id\":\"kfthwxmntei\",\"name\":\"aop\",\"type\":\"km\"}],\"nextLink\":\"c\"}")
            .toObject(StandbyVirtualMachinePoolRuntimeViewResourceListResult.class);
        Assertions.assertEquals("c", model.nextLink());
    }
}
