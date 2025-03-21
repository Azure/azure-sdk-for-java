// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.migration.assessment.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.migration.assessment.models.AzureVirtualMachineSkuDto;

public final class AzureVirtualMachineSkuDtoTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        AzureVirtualMachineSkuDto model = BinaryData.fromString(
            "{\"azureVmFamily\":\"D_series\",\"cores\":1814498181,\"azureSkuName\":\"Standard_M64s_v2\",\"availableCores\":2136201648,\"maxNetworkInterfaces\":1242659668}")
            .toObject(AzureVirtualMachineSkuDto.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        AzureVirtualMachineSkuDto model = new AzureVirtualMachineSkuDto();
        model = BinaryData.fromObject(model).toObject(AzureVirtualMachineSkuDto.class);
    }
}
