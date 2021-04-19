// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.SupplementalTypeDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SupplementalTypeDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        SupplementalTypeDigest typeDigest = new SupplementalTypeDigest(complexJsonNode);
        Assertions.assertEquals(typeDigest.getConstraints().size(), 0);
        Assertions.assertEquals(typeDigest.getProperties().size(), 0);
        Assertions.assertEquals(typeDigest.getCoTypes().size(), 1);
        Assertions.assertEquals(typeDigest.getCoTypeVersions().size(), 2);
        Assertions.assertEquals(typeDigest.getExtensionContext(), "dtmi:dtdl:extension:historization;1");
        Assertions.assertEquals(typeDigest.getExtensionKind(), "AdjunctType");
        Assertions.assertEquals(typeDigest.getParent(), "dtmi:dtdl:class:AdjunctType;3");
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
