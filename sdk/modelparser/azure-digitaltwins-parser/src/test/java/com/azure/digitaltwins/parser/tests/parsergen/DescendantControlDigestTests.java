// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.DescendantControlDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DescendantControlDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        DescendantControlDigest controlDigest = new DescendantControlDigest(complexJsonNode);

        Assertions.assertEquals(controlDigest.getDtdlVersion(), 2);
        Assertions.assertEquals((int) controlDigest.getMaxDepth(), 5);
        Assertions.assertEquals(controlDigest.getDefiningClass(), "ComplexSchema");
        Assertions.assertNull(controlDigest.getExcludeType());
        Assertions.assertEquals(controlDigest.getPropertyNames().size(), 2);
        Assertions.assertFalse(controlDigest.isNarrow());
        Assertions.assertEquals(controlDigest.getRootClass(), "Array");
        Assertions.assertNull(controlDigest.getDataTypeProperty());
        Assertions.assertNull(controlDigest.getImportProperties());
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "     \"dtdlVersion\": 2,"
            + "     \"rootClass\": \"Array\","
            + "     \"definingClass\": \"ComplexSchema\","
            + "     \"properties\": ["
            + "       \"elementSchema\","
            + "       \"schema\""
            + "     ],"
            + "     \"narrow\": false,"
            + "     \"maxDepth\": 5"
            + " }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
