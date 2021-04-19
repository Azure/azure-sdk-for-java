// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.azure.digitaltwins.parser.implementation.parsergen.SupplementalTypeDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SuplementalTypeDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        SupplementalTypeDigest typeDigest = new SupplementalTypeDigest(complexJsonNode);
        Assertions.assertTrue(typeDigest.getConstraints().size() == 0);
        Assertions.assertTrue(typeDigest.getProperties().size() == 0);
        Assertions.assertTrue(typeDigest.getCoTypes().size() == 1);
        Assertions.assertTrue(typeDigest.getCoTypeVersions().size() == 2);
        Assertions.assertTrue(typeDigest.getExtensionContext().equals("dtmi:dtdl:extension:historization;1"));
        Assertions.assertTrue(typeDigest.getExtensionKind().equals("AdjunctType"));
        Assertions.assertTrue(typeDigest.getParent().equals("dtmi:dtdl:class:AdjunctType;3"));
        Assertions.assertFalse(typeDigest.isAbstract());
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "      \"abstract\": false,"
            + "      \"parent\": \"dtmi:dtdl:class:AdjunctType;3\","
            + "      \"extensionKind\": \"AdjunctType\","
            + "      \"extensionContext\": \"dtmi:dtdl:extension:historization;1\","
            + "      \"cotypes\": ["
            + "        \"Property\""
            + "      ],"
            + "      \"cotypeVersions\": ["
            + "        2,"
            + "        3"
            + "      ],"
            + "      \"properties\": {},"
            + "      \"constraints\": []"
            + "   }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
