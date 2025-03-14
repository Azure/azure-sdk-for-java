// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.DatasetFolder;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import com.azure.resourcemanager.datafactory.models.ServiceNowObjectDataset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class ServiceNowObjectDatasetTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ServiceNowObjectDataset model = BinaryData.fromString(
            "{\"type\":\"ServiceNowObject\",\"typeProperties\":{\"tableName\":\"datamfvybfmpotal\"},\"description\":\"figrxxtrco\",\"structure\":\"dataqe\",\"schema\":\"dataldmxxbjh\",\"linkedServiceName\":{\"referenceName\":\"pvamsxrwqlwdf\",\"parameters\":{\"bboffgxtae\":\"datarplzeqzv\",\"fcyatbxdwr\":\"dataxt\",\"fbpeigkflvovriq\":\"datayvtkmxvztshnu\"}},\"parameters\":{\"txur\":{\"type\":\"Float\",\"defaultValue\":\"datakqcgzygtdjhtbar\"}},\"annotations\":[\"datayyumhzpst\",\"datacqacvttyh\",\"databilnszyjbuw\"],\"folder\":{\"name\":\"sydsci\"},\"\":{\"l\":\"dataayioxpqgqs\",\"akqsjymcfv\":\"datalefeombodvdgf\",\"nbpkfnxrlncmlzvv\":\"datazceuyuqktck\",\"cjqzrevfwcba\":\"datamesfhqs\"}}")
            .toObject(ServiceNowObjectDataset.class);
        Assertions.assertEquals("figrxxtrco", model.description());
        Assertions.assertEquals("pvamsxrwqlwdf", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.FLOAT, model.parameters().get("txur").type());
        Assertions.assertEquals("sydsci", model.folder().name());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ServiceNowObjectDataset model = new ServiceNowObjectDataset().withDescription("figrxxtrco")
            .withStructure("dataqe")
            .withSchema("dataldmxxbjh")
            .withLinkedServiceName(new LinkedServiceReference().withReferenceName("pvamsxrwqlwdf")
                .withParameters(mapOf("bboffgxtae", "datarplzeqzv", "fcyatbxdwr", "dataxt", "fbpeigkflvovriq",
                    "datayvtkmxvztshnu")))
            .withParameters(mapOf("txur",
                new ParameterSpecification().withType(ParameterType.FLOAT).withDefaultValue("datakqcgzygtdjhtbar")))
            .withAnnotations(Arrays.asList("datayyumhzpst", "datacqacvttyh", "databilnszyjbuw"))
            .withFolder(new DatasetFolder().withName("sydsci"))
            .withTableName("datamfvybfmpotal");
        model = BinaryData.fromObject(model).toObject(ServiceNowObjectDataset.class);
        Assertions.assertEquals("figrxxtrco", model.description());
        Assertions.assertEquals("pvamsxrwqlwdf", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.FLOAT, model.parameters().get("txur").type());
        Assertions.assertEquals("sydsci", model.folder().name());
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
