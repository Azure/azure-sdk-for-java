// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.SupplementalPropertyDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SupplementalPropertyDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        SupplementalPropertyDigest propertyDigest = new SupplementalPropertyDigest(complexJsonNode);
        Assertions.assertEquals((int) propertyDigest.getMaxCount(), 1);
        Assertions.assertEquals((int) propertyDigest.getMinCount(), 1);
        Assertions.assertNull(propertyDigest.getDictionaryKey());
        Assertions.assertEquals(propertyDigest.getInstanceOfProperty(), "schema");
        Assertions.assertEquals(propertyDigest.getTypeUri(), "http://www.w3.org/1999/02/22-rdf-syntax-ns#JSON");
        Assertions.assertFalse(propertyDigest.isOptional());
        Assertions.assertFalse(propertyDigest.isPlural());
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            " {"
            + "     \"maxCount\": 1,"
            + "     \"minCount\": 1,"
            + "     \"plural\": false,"
            + "     \"optional\": false,"
            + "     \"instanceOf\": \"schema\","
            + "     \"type\": \"http://www.w3.org/1999/02/22-rdf-syntax-ns#JSON\""
            + " }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
