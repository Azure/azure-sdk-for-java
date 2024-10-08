// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.DatasetFolder;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import com.azure.resourcemanager.datafactory.models.TeradataTableDataset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class TeradataTableDatasetTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        TeradataTableDataset model = BinaryData.fromString(
            "{\"type\":\"TeradataTable\",\"typeProperties\":{\"database\":\"datalnzffewvqky\",\"table\":\"datacgeipqxxsdyaf\"},\"description\":\"ydsmmabh\",\"structure\":\"datalejqzhpvhxp\",\"schema\":\"datadjze\",\"linkedServiceName\":{\"referenceName\":\"llgfy\",\"parameters\":{\"mwdz\":\"dataqscjpvqerqxk\",\"x\":\"datazlhcu\",\"kfrwxohlydsnjz\":\"dataqpwwvmbjecfwlbgh\",\"hi\":\"datachiypbfhm\"}},\"parameters\":{\"srjzgkbrau\":{\"type\":\"SecureString\",\"defaultValue\":\"dataewb\"},\"rukbuudrizwkw\":{\"type\":\"Int\",\"defaultValue\":\"dataufqnnqbjxgjwsr\"}},\"annotations\":[\"datalaacedikqe\"],\"folder\":{\"name\":\"yb\"},\"\":{\"rommkiqhypwt\":\"datavgb\"}}")
            .toObject(TeradataTableDataset.class);
        Assertions.assertEquals("ydsmmabh", model.description());
        Assertions.assertEquals("llgfy", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.SECURE_STRING, model.parameters().get("srjzgkbrau").type());
        Assertions.assertEquals("yb", model.folder().name());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        TeradataTableDataset model = new TeradataTableDataset().withDescription("ydsmmabh")
            .withStructure("datalejqzhpvhxp")
            .withSchema("datadjze")
            .withLinkedServiceName(new LinkedServiceReference().withReferenceName("llgfy")
                .withParameters(mapOf("mwdz", "dataqscjpvqerqxk", "x", "datazlhcu", "kfrwxohlydsnjz",
                    "dataqpwwvmbjecfwlbgh", "hi", "datachiypbfhm")))
            .withParameters(mapOf("srjzgkbrau",
                new ParameterSpecification().withType(ParameterType.SECURE_STRING).withDefaultValue("dataewb"),
                "rukbuudrizwkw",
                new ParameterSpecification().withType(ParameterType.INT).withDefaultValue("dataufqnnqbjxgjwsr")))
            .withAnnotations(Arrays.asList("datalaacedikqe"))
            .withFolder(new DatasetFolder().withName("yb"))
            .withDatabase("datalnzffewvqky")
            .withTable("datacgeipqxxsdyaf");
        model = BinaryData.fromObject(model).toObject(TeradataTableDataset.class);
        Assertions.assertEquals("ydsmmabh", model.description());
        Assertions.assertEquals("llgfy", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.SECURE_STRING, model.parameters().get("srjzgkbrau").type());
        Assertions.assertEquals("yb", model.folder().name());
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
