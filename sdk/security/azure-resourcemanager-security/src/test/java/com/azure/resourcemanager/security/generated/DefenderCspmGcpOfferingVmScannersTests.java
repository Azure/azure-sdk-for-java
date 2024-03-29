// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.security.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.security.models.DefenderCspmGcpOfferingVmScanners;
import com.azure.resourcemanager.security.models.DefenderCspmGcpOfferingVmScannersConfiguration;
import com.azure.resourcemanager.security.models.ScanningMode;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class DefenderCspmGcpOfferingVmScannersTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        DefenderCspmGcpOfferingVmScanners model = BinaryData.fromString(
            "{\"enabled\":false,\"configuration\":{\"scanningMode\":\"Default\",\"exclusionTags\":{\"t\":\"apvd\",\"rnrnjrcufmbgacnr\":\"peerscd\",\"eubkqiqmlf\":\"fdtncmspsanma\",\"skkqjmxptuei\":\"hlq\"}}}")
            .toObject(DefenderCspmGcpOfferingVmScanners.class);
        Assertions.assertEquals(false, model.enabled());
        Assertions.assertEquals(ScanningMode.DEFAULT, model.configuration().scanningMode());
        Assertions.assertEquals("apvd", model.configuration().exclusionTags().get("t"));
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        DefenderCspmGcpOfferingVmScanners model = new DefenderCspmGcpOfferingVmScanners().withEnabled(false)
            .withConfiguration(new DefenderCspmGcpOfferingVmScannersConfiguration()
                .withScanningMode(ScanningMode.DEFAULT).withExclusionTags(mapOf("t", "apvd", "rnrnjrcufmbgacnr",
                    "peerscd", "eubkqiqmlf", "fdtncmspsanma", "skkqjmxptuei", "hlq")));
        model = BinaryData.fromObject(model).toObject(DefenderCspmGcpOfferingVmScanners.class);
        Assertions.assertEquals(false, model.enabled());
        Assertions.assertEquals(ScanningMode.DEFAULT, model.configuration().scanningMode());
        Assertions.assertEquals("apvd", model.configuration().exclusionTags().get("t"));
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
