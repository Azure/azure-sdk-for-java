// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.InstanceValidationDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class InstanceValidationDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        InstanceValidationDigest instanceValidationDigest = new InstanceValidationDigest(complexJsonNode);
        Assertions.assertEquals("is a string value that conforms to the RFC 3339 definition of 'date'", instanceValidationDigest.getCriteriaText());
        Assertions.assertEquals(instanceValidationDigest.getElementConditions().size(), 2);
        Assertions.assertEquals(instanceValidationDigest.getElementConditions().get(2).getDataType(), "date");
        Assertions.assertEquals(instanceValidationDigest.getElementConditions().get(2).getJsonType(), "string");
        Assertions.assertEquals(instanceValidationDigest.getElementConditions().get(2).getPattern(), "^[0-9]{4}-[0-9]{2}-[0-9]{2}$");

        Assertions.assertEquals(instanceValidationDigest.getChildConditions().size(), 2);
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getDataType());
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getJsonType());
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getHasValue());
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getInstanceOf());
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getNamePattern());
        Assertions.assertNull(instanceValidationDigest.getChildConditions().get(2).getNameHasValue());
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "  {"
            + "   \"criteriaText\": \"is a string value that conforms to the RFC 3339 definition of 'date'\","
            + "   \"2\": {"
            + "     \"element\": {"
            + "       \"jsonType\": \"string\","
            + "       \"datatype\": \"date\","
            + "       \"pattern\": \"^[0-9]{4}-[0-9]{2}-[0-9]{2}$\""
            + "     },"
            + "     \"eachChild\": {}"
            + "   },"
            + "   \"3\": {"
            + "     \"element\": {"
            + "       \"jsonType\": \"string\","
            + "       \"datatype\": \"date\","
            + "       \"pattern\": \"^[0-9]{4}-[0-9]{2}-[0-9]{2}$\""
            + "     },"
            + "     \"eachChild\": {}"
            + "   }"
            + "}";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
