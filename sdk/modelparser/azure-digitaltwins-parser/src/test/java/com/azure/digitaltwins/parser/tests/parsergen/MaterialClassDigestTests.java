// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.MaterialClassDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MaterialClassDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        MaterialClassDigest classDigest = new MaterialClassDigest(complexJsonNode);
        Assertions.assertEquals(classDigest.isAbstract(), true);
        Assertions.assertEquals(classDigest.isOvert(), false);
        Assertions.assertEquals(classDigest.isPartition(), false);
        Assertions.assertEquals(classDigest.getParentClass(), "Schema");
        Assertions.assertEquals(classDigest.getDtdlVersions().size(), 2);
        Assertions.assertEquals(classDigest.getTypeIds().size(), 3);

        Assertions.assertEquals(classDigest.getStandardElementIds().size(), 0);

        Assertions.assertEquals(classDigest.getElementalSubclasses().size(), 2);
        Assertions.assertEquals(classDigest.getElementalSubclasses().get(2).size(), 0);
        Assertions.assertEquals(classDigest.getElementalSubclasses().get(3).size(), 0);

        Assertions.assertEquals(classDigest.getConcreteSubclasses().size(), 2);
        Assertions.assertEquals(classDigest.getConcreteSubclasses().get(2).size(), 4);
        Assertions.assertEquals(classDigest.getConcreteSubclasses().get(3).size(), 4);

        Assertions.assertEquals(classDigest.getExtensibleMaterialSubclasses().size(), 2);
        Assertions.assertEquals(classDigest.getExtensibleMaterialSubclasses().get(2).size(), 0);
        Assertions.assertEquals(classDigest.getExtensibleMaterialSubclasses().get(3).size(), 0);

        Assertions.assertEquals(classDigest.getBadTypeActionFormat().size(), 2);
        Assertions.assertNotNull(classDigest.getBadTypeActionFormat().get(2));
        Assertions.assertNotNull(classDigest.getBadTypeActionFormat().get(3));

        Assertions.assertEquals(classDigest.getBadTypeCauseFormat().size(), 2);
        Assertions.assertNotNull(classDigest.getBadTypeCauseFormat().get(2));
        Assertions.assertNotNull(classDigest.getBadTypeCauseFormat().get(3));

        Assertions.assertNotNull(classDigest.getProperties());
        Assertions.assertEquals(classDigest.getProperties().size(), 4);
    }


    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "  \"dtdlVersions\": ["
            + "    2,"
            + "    3"
            + "  ],"
            + "  \"abstract\": true,"
            + "  \"overt\": false,"
            + "  \"partition\": false,"
            + "  \"parentClass\": \"Schema\","
            + "  \"typeIds\": ["
            + "    \"dtmi:dtdl:class:ComplexSchema\","
            + "    \"dtmi:dtdl:class:Entity\","
            + "    \"dtmi:dtdl:class:Schema\""
            + "  ],"
            + "  \"concreteSubclasses\": {"
            + "    \"2\": ["
            + "      \"Array\","
            + "      \"Enum\","
            + "      \"Map\","
            + "      \"Object\""
            + "    ],"
            + "    \"3\": ["
            + "      \"Array\","
            + "      \"Enum\","
            + "      \"Map\","
            + "      \"Object\""
            + "    ]"
            + "  },"
            + "  \"elementalSubclasses\": {"
            + "    \"2\": [],"
            + "    \"3\": []"
            + "  },"
            + "  \"extensibleMaterialSubclasses\": {"
            + "    \"2\": [],"
            + "    \"3\": []"
            + "  },"
            + "  \"badTypeCauseFormat\": {"
            + "    \"2\": \"{primaryId:p} property '{property}' has value{secondaryId:e} that does not have @type of Array, Enum, Map, or Object.\","
            + "    \"3\": \"{primaryId:p} property '{property}' has value{secondaryId:e} that does not have @type of Array, Enum, Map, or Object.\""
            + "  },"
            + "  \"badTypeActionFormat\": {"
            + "    \"2\": \"Provide a value for property '{property}' with @type in the set of allowable types.\","
            + "    \"3\": \"Provide a value for property '{property}' with @type in the set of allowable types.\""
            + "  },"
            + "  \"properties\": {"
            + "    \"comment\": {"
            + "      \"_\": {"
            + "        \"literal\": true,"
            + "        \"abstract\": false,"
            + "        \"datatype\": \"string\","
            + "        \"plural\": false,"
            + "        \"optional\": true,"
            + "        \"inherited\": true,"
            + "        \"shadowed\": false,"
            + "        \"isKey\": false,"
            + "        \"isSeg\": false"
            + "      },"
            + "      \"2\": {"
            + "        \"idRequired\": false,"
            + "        \"allowed\": true,"
            + "        \"maxCount\": 1,"
            + "        \"maxLength\": 512,"
            + "        \"typeRequired\": true"
            + "      },"
            + "      \"3\": {"
            + "        \"idRequired\": false,"
            + "        \"allowed\": true,"
            + "        \"maxCount\": 1,"
            + "        \"maxLength\": 512,"
            + "        \"typeRequired\": true"
            + "      }"
            + "    },"
            + "    \"description\": {"
            + "      \"_\": {"
            + "        \"literal\": true,"
            + "        \"abstract\": false,"
            + "        \"datatype\": \"langString\","
            + "        \"plural\": true,"
            + "        \"optional\": true,"
            + "        \"inherited\": true,"
            + "        \"shadowed\": false,"
            + "        \"isKey\": false,"
            + "        \"isSeg\": false"
            + "      },"
            + "      \"2\": {"
            + "        \"idRequired\": false,"
            + "        \"defaultLanguage\": \"en\","
            + "        \"allowed\": true,"
            + "        \"maxLength\": 512,"
            + "        \"typeRequired\": true"
            + "      },"
            + "      \"3\": {"
            + "        \"idRequired\": false,"
            + "        \"defaultLanguage\": \"en\","
            + "        \"allowed\": true,"
            + "        \"maxLength\": 512,"
            + "        \"typeRequired\": true"
            + "      }"
            + "    },"
            + "    \"displayName\": {"
            + "      \"_\": {"
            + "        \"literal\": true,"
            + "        \"abstract\": false,"
            + "        \"datatype\": \"langString\","
            + "        \"plural\": true,"
            + "        \"optional\": true,"
            + "        \"inherited\": true,"
            + "        \"shadowed\": false,"
            + "        \"isKey\": false,"
            + "        \"isSeg\": false"
            + "      },"
            + "      \"2\": {"
            + "        \"idRequired\": false,"
            + "        \"defaultLanguage\": \"en\","
            + "        \"allowed\": true,"
            + "        \"maxLength\": 64,"
            + "        \"typeRequired\": true"
            + "      },"
            + "      \"3\": {"
            + "        \"idRequired\": false,"
            + "        \"defaultLanguage\": \"en\","
            + "        \"allowed\": true,"
            + "        \"maxLength\": 64,"
            + "        \"typeRequired\": true"
            + "      }"
            + "    },"
            + "    \"languageVersion\": {"
            + "      \"_\": {"
            + "        \"literal\": true,"
            + "        \"abstract\": false,"
            + "        \"datatype\": \"integer\","
            + "        \"plural\": false,"
            + "        \"optional\": false,"
            + "        \"inherited\": true,"
            + "        \"shadowed\": false,"
            + "        \"isKey\": false,"
            + "        \"isSeg\": false"
            + "      },"
            + "      \"2\": {"
            + "        \"idRequired\": false,"
            + "        \"allowed\": false,"
            + "        \"typeRequired\": true,"
            + "        \"value\": 2"
            + "      },"
            + "      \"3\": {"
            + "        \"idRequired\": false,"
            + "        \"allowed\": false,"
            + "        \"typeRequired\": true,"
            + "        \"value\": 3"
            + "      }"
            + "    }"
            + "  }"
            + "}";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
