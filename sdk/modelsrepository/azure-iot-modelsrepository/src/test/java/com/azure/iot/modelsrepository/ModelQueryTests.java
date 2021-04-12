// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.iot.modelsrepository.implementation.ModelsQuery;
import com.azure.iot.modelsrepository.implementation.models.ModelMetadata;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ModelQueryTests {
    public static final String MODEL_TEMPLATE = ""
        +
        "{\n"
        +
        "%s \n"
        +
        "\"@type\": \"Interface\",\n"
        +
        "\"displayName\": \"Phone\",\n"
        +
        "%s \n"
        +
        "%s \n"
        +
        "\"@context\": \"dtmi:dtdl:context;2\"\n"
        +
        "}";

    @ParameterizedTest
    @CsvSource(
        value = {
            "\"@id\":\"dtmi:com:example:thermostat;1\", | dtmi:com:example:thermostat;1",
            "\"@id\": \"\", | ''"
        },
        delimiter = '|')
    public void getIdTests(String id, String expected) throws JsonProcessingException {
        String modelContent = String.format(MODEL_TEMPLATE, id, "", "");
        ModelsQuery query = new ModelsQuery(modelContent);
        String modelId = query.parseModel().getId();

        Assertions.assertEquals(expected, modelId);
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "\"contents\": "
                +
                " ["
                +
                "     {"
                +
                "         \"@type\": \"Property\","
                +
                "         \"name\": \"capacity\","
                +
                "         \"schema\": \"integer\""
                +
                "     },"
                +
                "     {"
                +
                "             \"@type\": \"Component\","
                +
                "         \"name\": \"frontCamera\","
                +
                "         \"schema\": \"dtmi:com:example:Camera;3\""
                +
                "     },"
                +
                "     {"
                +
                "         \"@type\": \"Component\","
                +
                "         \"name\": \"backCamera\","
                +
                "         \"schema\": \"dtmi:com:example:Camera;3\""
                +
                "     },"
                +
                "     {"
                +
                "         \"@type\": \"Component\","
                +
                "         \"name\": \"deviceInfo\","
                +
                "         \"schema\": \"dtmi:azure:DeviceManagement:DeviceInformation;1\""
                +
                "     }"
                +
                " ],"
                +
                "| dtmi:com:example:Camera;3,dtmi:com:example:Camera;3,dtmi:azure:DeviceManagement:DeviceInformation;1",
            "\"contents\": "
                +
                "["
                +
                "   {"
                +
                "       \"@type\": \"Property\","
                +
                "       \"name\": \"capacity\","
                +
                "       \"schema\": \"integer\""
                +
                "   }"
                +
                "],"
                +
                "| ''",
        },
        delimiter = '|')
    public void getComponentSchemaTests(String contents, String expected) throws JsonProcessingException {
        List<String> expectedDtmis;

        if (expected.isEmpty()) {
            expectedDtmis = new ArrayList<>();
        } else {
            expectedDtmis = Arrays.asList(expected.split(","));
        }

        String modelContent = String.format(MODEL_TEMPLATE, "", "", contents);
        ModelsQuery query = new ModelsQuery(modelContent);
        List<String> componentSchemas = query.parseModel().getComponentSchemas();

        Assertions.assertEquals(expectedDtmis.size(), componentSchemas.size(), "number of expected dtmis should match the parsed version");

        Assertions.assertTrue(expectedDtmis.containsAll(componentSchemas));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "\"extends\": [\"dtmi:com:example:Camera;3\",\"dtmi:azure:DeviceManagement:DeviceInformation;1\"],"
                +
                "|"
                + "dtmi:com:example:Camera;3,dtmi:azure:DeviceManagement:DeviceInformation;1",
            "\"extends\":[], | ''",
            "\"extends\": \"dtmi:com:example:Camera;3\", | dtmi:com:example:Camera;3"
        },
        delimiter = '|')
    public void getExtendsTests(String extend, String expected) throws JsonProcessingException {
        List<String> expectedDtmis;

        if (expected.isEmpty()) {
            expectedDtmis = new ArrayList<>();
        } else {
            expectedDtmis = Arrays.asList(expected.split(","));
        }

        String modelContent = String.format(MODEL_TEMPLATE, "", extend, "");
        ModelsQuery query = new ModelsQuery(modelContent);
        List<String> componentSchemas = query.parseModel().getExtend();

        Assertions.assertEquals(expectedDtmis.size(), componentSchemas.size(), "number of expected dtmis should match the parsed value");

        Assertions.assertTrue(expectedDtmis.containsAll(componentSchemas));
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "\"@id\": \"dtmi:com:example:thermostat;1\","
                +
                "|"
                +
                "\"extends\": [\"dtmi:com:example:Camera;3\",\"dtmi:azure:DeviceManagement:DeviceInformation;1\"],"
                +
                "|"
                +
                "\"contents\": "
                +
                "   ["
                +
                "       {"
                +
                "           \"@type\": \"Property\","
                +
                "           \"name\": \"capacity\","
                +
                "           \"schema\": \"integer\""
                +
                "       },"
                +
                "       {"
                +
                "           \"@type\": \"Component\","
                +
                "           \"name\": \"frontCamera\","
                +
                "           \"schema\": \"dtmi:com:example:Camera;3\""
                +
                "       },"
                +
                "       {"
                +
                "           \"@type\": \"Component\","
                +
                "           \"name\": \"backCamera\","
                +
                "           \"schema\": \"dtmi:com:example:Camera;3\""
                +
                "       },"
                +
                "       {"
                +
                "           \"@type\": \"Component\","
                +
                "           \"name\": \"deviceInfo\","
                +
                "           \"schema\": \"dtmi:azure:DeviceManagement:DeviceInformation;1\""
                +
                "       }],"
                +
                "|"
                +
                "dtmi:com:example:Camera;3,dtmi:azure:DeviceManagement:DeviceInformation;1",

            "\"@id\": \"dtmi:example:Interface1;1\","
                +
                "|"
                +
                "\"extends\": ["
                +
                "   \"dtmi:example:Interface2;1\", "
                +
                "   {"
                +
                "       \"@id\": \"dtmi:example:Interface3;1\","
                +
                "       \"@type\": \"Interface\","
                +
                "       \"contents\": "
                +
                "        ["
                +
                "           {"
                +
                "               \"@type\": \"Component\","
                +
                "               \"name\": \"comp1\","
                +
                "               \"schema\": [\"dtmi:example:Interface4;1\"]"
                +
                "            },"
                +
                "            {"
                +
                "               \"@type\": \"Component\","
                +
                "               \"name\": \"comp2\","
                +
                "               \"schema\": "
                +
                "               {"
                +
                "                   \"@id\": \"dtmi:example:Interface5;1\","
                +
                "                   \"@type\": \"Interface\","
                +
                "                   \"extends\": \"dtmi:example:Interface6;1\""
                +
                "               }"
                +
                "             }"
                +
                "       ]"
                +
                "   }],"
                +
                "| ''"
                +
                "|"
                +
                "dtmi:example:Interface2;1,dtmi:example:Interface4;1,dtmi:example:Interface6;1"
        },
        delimiter = '|')
    public void getModelDependenciesTests(String id, String extend, String contents, String expected) throws JsonProcessingException {
        List<String> expectedDtmis;

        if (expected.isEmpty()) {
            expectedDtmis = new ArrayList<>();
        } else {
            expectedDtmis = Arrays.asList(expected.split(","));
        }

        String modelContent = String.format(MODEL_TEMPLATE, id, extend, contents);
        ModelsQuery query = new ModelsQuery(modelContent);
        ModelMetadata metadata = query.parseModel();

        Assertions.assertEquals(expectedDtmis.size(), metadata.getDependencies().size());
        Assertions.assertTrue(expectedDtmis.containsAll(metadata.getDependencies()));
    }
}
