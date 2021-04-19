// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.SupplementalConstraintDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SupplementalConstraintsDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        SupplementalConstraintDigest constraintDigest = new SupplementalConstraintDigest(complexJsonNode);
        Assertions.assertEquals(constraintDigest.getPropertyName(), "schema");
        Assertions.assertEquals(constraintDigest.getRequiredValues().size(), 1);
        Assertions.assertEquals(constraintDigest.getRequiredValueString(), "vector");
        Assertions.assertNull(constraintDigest.getRequiredTypes());
        Assertions.assertNull(constraintDigest.getRequiredTypesString());
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "       \"property\": \"schema\","
            + "       \"requiredValues\": ["
            + "         \"dtmi:iotcentral:schema:vector;2\""
            + "       ],"
            + "       \"requiredValuesString\": \"vector\""
            + "   }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
