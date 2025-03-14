// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.models.DatasetFolder;
import com.azure.resourcemanager.datafactory.models.LinkedServiceReference;
import com.azure.resourcemanager.datafactory.models.ParameterSpecification;
import com.azure.resourcemanager.datafactory.models.ParameterType;
import com.azure.resourcemanager.datafactory.models.SalesforceObjectDataset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class SalesforceObjectDatasetTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SalesforceObjectDataset model = BinaryData.fromString(
            "{\"type\":\"SalesforceObject\",\"typeProperties\":{\"objectApiName\":\"dataxrltqmmijgpqfkwn\"},\"description\":\"ikczscymqfv\",\"structure\":\"datawpq\",\"schema\":\"dataumz\",\"linkedServiceName\":{\"referenceName\":\"pd\",\"parameters\":{\"zk\":\"datazvp\"}},\"parameters\":{\"wed\":{\"type\":\"Bool\",\"defaultValue\":\"datazbflbqmhbiyxx\"},\"lmsy\":{\"type\":\"SecureString\",\"defaultValue\":\"dataqbbseseayu\"},\"zk\":{\"type\":\"String\",\"defaultValue\":\"datacrolrzesbomp\"}},\"annotations\":[\"datanwjivtbusz\"],\"folder\":{\"name\":\"rdf\"},\"\":{\"sdeqngcaydzinlo\":\"dataywdal\",\"xrsi\":\"dataulpozmdahyc\"}}")
            .toObject(SalesforceObjectDataset.class);
        Assertions.assertEquals("ikczscymqfv", model.description());
        Assertions.assertEquals("pd", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.BOOL, model.parameters().get("wed").type());
        Assertions.assertEquals("rdf", model.folder().name());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SalesforceObjectDataset model = new SalesforceObjectDataset().withDescription("ikczscymqfv")
            .withStructure("datawpq")
            .withSchema("dataumz")
            .withLinkedServiceName(
                new LinkedServiceReference().withReferenceName("pd").withParameters(mapOf("zk", "datazvp")))
            .withParameters(mapOf("wed",
                new ParameterSpecification().withType(ParameterType.BOOL).withDefaultValue("datazbflbqmhbiyxx"), "lmsy",
                new ParameterSpecification().withType(ParameterType.SECURE_STRING).withDefaultValue("dataqbbseseayu"),
                "zk", new ParameterSpecification().withType(ParameterType.STRING).withDefaultValue("datacrolrzesbomp")))
            .withAnnotations(Arrays.asList("datanwjivtbusz"))
            .withFolder(new DatasetFolder().withName("rdf"))
            .withObjectApiName("dataxrltqmmijgpqfkwn");
        model = BinaryData.fromObject(model).toObject(SalesforceObjectDataset.class);
        Assertions.assertEquals("ikczscymqfv", model.description());
        Assertions.assertEquals("pd", model.linkedServiceName().referenceName());
        Assertions.assertEquals(ParameterType.BOOL, model.parameters().get("wed").type());
        Assertions.assertEquals("rdf", model.folder().name());
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
