// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datafactory.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.datafactory.fluent.models.SynapseNotebookActivityTypeProperties;
import com.azure.resourcemanager.datafactory.models.BigDataPoolParametrizationReference;
import com.azure.resourcemanager.datafactory.models.BigDataPoolReferenceType;
import com.azure.resourcemanager.datafactory.models.ConfigurationType;
import com.azure.resourcemanager.datafactory.models.NotebookParameter;
import com.azure.resourcemanager.datafactory.models.NotebookParameterType;
import com.azure.resourcemanager.datafactory.models.NotebookReferenceType;
import com.azure.resourcemanager.datafactory.models.SparkConfigurationParametrizationReference;
import com.azure.resourcemanager.datafactory.models.SparkConfigurationReferenceType;
import com.azure.resourcemanager.datafactory.models.SynapseNotebookReference;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;

public final class SynapseNotebookActivityTypePropertiesTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        SynapseNotebookActivityTypeProperties model = BinaryData.fromString(
            "{\"notebook\":{\"type\":\"NotebookReference\",\"referenceName\":\"datahymdaeshjjqc\"},\"sparkPool\":{\"type\":\"BigDataPoolReference\",\"referenceName\":\"datan\"},\"parameters\":{\"whbkxzqryovlhm\":{\"value\":\"datae\",\"type\":\"int\"},\"aufmsyf\":{\"value\":\"dataobiagwuefmyiw\",\"type\":\"string\"},\"fyarl\":{\"value\":\"dataoreibce\",\"type\":\"int\"}},\"executorSize\":\"datalg\",\"conf\":\"datarqlbzcsffagun\",\"driverSize\":\"datateyh\",\"numExecutors\":\"datapkmkvkm\",\"configurationType\":\"Default\",\"targetSparkConfiguration\":{\"type\":\"SparkConfigurationReference\",\"referenceName\":\"datacw\"},\"sparkConfig\":{\"fdbahxcwjqtfs\":\"datavyosmxov\",\"uay\":\"datacakbezdvnez\",\"nkwhiyusjhmjlkkn\":\"dataejwqeypaoa\"}}")
            .toObject(SynapseNotebookActivityTypeProperties.class);
        Assertions.assertEquals(NotebookReferenceType.NOTEBOOK_REFERENCE, model.notebook().type());
        Assertions.assertEquals(BigDataPoolReferenceType.BIG_DATA_POOL_REFERENCE, model.sparkPool().type());
        Assertions.assertEquals(NotebookParameterType.INT, model.parameters().get("whbkxzqryovlhm").type());
        Assertions.assertEquals(ConfigurationType.DEFAULT, model.configurationType());
        Assertions.assertEquals(SparkConfigurationReferenceType.SPARK_CONFIGURATION_REFERENCE,
            model.targetSparkConfiguration().type());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        SynapseNotebookActivityTypeProperties model = new SynapseNotebookActivityTypeProperties()
            .withNotebook(new SynapseNotebookReference().withType(NotebookReferenceType.NOTEBOOK_REFERENCE)
                .withReferenceName("datahymdaeshjjqc"))
            .withSparkPool(
                new BigDataPoolParametrizationReference().withType(BigDataPoolReferenceType.BIG_DATA_POOL_REFERENCE)
                    .withReferenceName("datan"))
            .withParameters(mapOf("whbkxzqryovlhm",
                new NotebookParameter().withValue("datae").withType(NotebookParameterType.INT), "aufmsyf",
                new NotebookParameter().withValue("dataobiagwuefmyiw").withType(NotebookParameterType.STRING), "fyarl",
                new NotebookParameter().withValue("dataoreibce").withType(NotebookParameterType.INT)))
            .withExecutorSize("datalg")
            .withConf("datarqlbzcsffagun")
            .withDriverSize("datateyh")
            .withNumExecutors("datapkmkvkm")
            .withConfigurationType(ConfigurationType.DEFAULT)
            .withTargetSparkConfiguration(new SparkConfigurationParametrizationReference()
                .withType(SparkConfigurationReferenceType.SPARK_CONFIGURATION_REFERENCE)
                .withReferenceName("datacw"))
            .withSparkConfig(
                mapOf("fdbahxcwjqtfs", "datavyosmxov", "uay", "datacakbezdvnez", "nkwhiyusjhmjlkkn", "dataejwqeypaoa"));
        model = BinaryData.fromObject(model).toObject(SynapseNotebookActivityTypeProperties.class);
        Assertions.assertEquals(NotebookReferenceType.NOTEBOOK_REFERENCE, model.notebook().type());
        Assertions.assertEquals(BigDataPoolReferenceType.BIG_DATA_POOL_REFERENCE, model.sparkPool().type());
        Assertions.assertEquals(NotebookParameterType.INT, model.parameters().get("whbkxzqryovlhm").type());
        Assertions.assertEquals(ConfigurationType.DEFAULT, model.configurationType());
        Assertions.assertEquals(SparkConfigurationReferenceType.SPARK_CONFIGURATION_REFERENCE,
            model.targetSparkConfiguration().type());
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
