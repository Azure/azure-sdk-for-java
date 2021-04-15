// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.JsonNodeHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class JsonNodeHelperTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void getNullableIntegerValueTest() {
        Integer output = JsonNodeHelper.getNullableIntegerValue(complexJsonNode, "intValue");
        Assertions.assertEquals(2, output);

        Assertions.assertNull(JsonNodeHelper.getNullableIntegerValue(complexJsonNode, "doesNotExist"));
    }

    @Test
    public void getTextValueTest() {
        String output = JsonNodeHelper.getTextValue(complexJsonNode, "stringValue");
        Assertions.assertEquals("hello", output);

        Assertions.assertNull(JsonNodeHelper.getTextValue(complexJsonNode, "doesNotExist"));
    }

    @Test
    public void getBooleanValue() {
        boolean output = JsonNodeHelper.getNotNullableBooleanValue(complexJsonNode, "booleanValue");
        Assertions.assertEquals(true, output);

        boolean output2 = JsonNodeHelper.getNotNullableBooleanValue(complexJsonNode, "doesNotExist");
        Assertions.assertEquals(false, output2);

        boolean output3 = JsonNodeHelper.getNullableBooleanValue(complexJsonNode, "booleanValue");
        Assertions.assertEquals(true, output3);

        Boolean output4 = JsonNodeHelper.getNullableBooleanValue(complexJsonNode, "doesNotExist");
        Assertions.assertEquals(null, output4);
    }

    @Test
    public void getStringArrayValues() {
        List<String> output = JsonNodeHelper.getArrayValues(complexJsonNode, "stringArray", String.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(3, output.size());
        Assertions.assertTrue(output.containsAll(Arrays.asList("a", "b", "c")));
    }

    @Test
    public void getIntegerArrayValues() {
        List<Integer> output = JsonNodeHelper.getArrayValues(complexJsonNode, "integerArray", Integer.class);
        Assertions.assertNotNull(output);
        Assertions.assertEquals(3, output.size());
        Assertions.assertTrue(output.containsAll(Arrays.asList(1, 2, 3)));
    }

    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "     \"intValue\": 2,"
            + "     \"stringValue\": \"hello\","
            + "     \"booleanValue\": true,"
            + "     \"stringArray\": ["
            + "         \"a\","
            + "         \"b\","
            + "         \"c\""
            + "     ],"
            + "     \"integerArray\": ["
            + "         1,"
            + "         2,"
            + "         3"
            + "     ]"
            + " }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
