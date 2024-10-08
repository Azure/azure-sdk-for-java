// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.Dataset;
import com.azure.resourcemanager.datafactory.models.DatasetFolder;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class DatasetTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        Dataset model = BinaryData.fromString(
            "{\"type\":\"Dataset\",\"description\":\"eburu\",\"structure\":\"datamovsmzlxwabmqoe\",\"schema\":\"dataifrvtpu\",\"linkedServiceName\":{\"referenceName\":\"ujmqlgkfbtndoa\",\"parameters\":{\"ed\":\"databjcntujitc\",\"qouicybxarzgsz\":\"datatwwaezkojvdcpzf\",\"p\":\"datafoxciq\",\"xkhnzbonlwnto\":\"datadoamciodhkha\"}},\"parameters\":{\"kszzcmrvexztv\":{\"type\":\"Bool\",\"defaultValue\":\"datawbw\"},\"lmnguxaw\":{\"type\":\"Object\",\"defaultValue\":\"datagsfraoyzkoow\"}},\"annotations\":[\"datadsyuuximerqfob\",\"datayznkby\",\"datautwpfhp\",\"datagmhrskdsnfdsdoak\"],\"folder\":{\"name\":\"lmkk\"},\"\":{\"sdsttwvog\":\"datadlhewp\",\"akufgmjz\":\"databbejdcngqqm\",\"grtwae\":\"datawr\",\"zkopb\":\"datau\"}}")
            .toObject(Dataset.class);
        Assertions.assertEquals("eburu", model.description());
        Assertions.assertEquals("ujmqlgkfbtndoa", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.BOOL, model.parameters().get("kszzcmrvexztv").type());
        Assertions.assertEquals("lmkk", model.folder().name());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        Dataset model = new Dataset().withDescription("eburu")
            .withStructure("datamovsmzlxwabmqoe")
            .withSchema("dataifrvtpu")
            .withLinkedServiceName(new LinkedServiceReference().withReferenceName("ujmqlgkfbtndoa")
                .withParameters(mapOf("ed", "databjcntujitc", "qouicybxarzgsz", "datatwwaezkojvdcpzf", "p",
                    "datafoxciq", "xkhnzbonlwnto", "datadoamciodhkha")))
            .withParameters(mapOf("kszzcmrvexztv",
                new ParameterSpecification().withType(ParameterType.BOOL).withDefaultValue("datawbw"), "lmnguxaw",
                new ParameterSpecification().withType(ParameterType.OBJECT).withDefaultValue("datagsfraoyzkoow")))
            .withAnnotations(Arrays.asList("datadsyuuximerqfob", "datayznkby", "datautwpfhp", "datagmhrskdsnfdsdoak"))
            .withFolder(new DatasetFolder().withName("lmkk"))
            .withAdditionalProperties(mapOf("type", "Dataset"));
        model = BinaryData.fromObject(model).toObject(Dataset.class);
        Assertions.assertEquals("eburu", model.description());
        Assertions.assertEquals("ujmqlgkfbtndoa", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.BOOL, model.parameters().get("kszzcmrvexztv").type());
        Assertions.assertEquals("lmkk", model.folder().name());
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
