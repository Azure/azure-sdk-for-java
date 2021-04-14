// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.StringRestriction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class StringRestrictionTests {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    public void allPresent() throws JsonProcessingException {
        final String input =
            "   {\n"
            + "     \"maxLength\": 2,\n"
            + "     \"pattern\": \"^dtmi:[A-Za-z]\"\n"
            + " }";

        JsonNode inputNode = convertToNode(input);
        StringRestriction restriction = new StringRestriction(inputNode);
        Assertions.assertNotNull(restriction.getMaxLength());
        Assertions.assertNotNull(restriction.getPattern());
        Assertions.assertEquals(restriction.getMaxLength(), 2);
        Assertions.assertEquals(restriction.getPattern(), "^dtmi:[A-Za-z]");
    }

    @Test
    public void patternPresentOnly() throws JsonProcessingException {
        final String input =
            "   {\n"
            +   "   \"pattern\": \"^dtmi:[A-Za-z]\"\n"
            +   "}";

        JsonNode inputNode = convertToNode(input);
        StringRestriction restriction = new StringRestriction(inputNode);
        Assertions.assertNull(restriction.getMaxLength());
        Assertions.assertNotNull(restriction.getPattern());
        Assertions.assertEquals(restriction.getPattern(), "^dtmi:[A-Za-z]");
    }

    @Test
    public void maxLengthPresentOnly() throws JsonProcessingException {
        final String input =
            "   {\n"
            + "     \"maxLength\": 2\n"
            + " }";

        JsonNode inputNode = convertToNode(input);
        StringRestriction restriction = new StringRestriction(inputNode);
        Assertions.assertNotNull(restriction.getMaxLength());
        Assertions.assertNull(restriction.getPattern());
        Assertions.assertEquals(restriction.getMaxLength(), 2);
    }

    public static JsonNode convertToNode(String input) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(input, JsonNode.class);
    }
}
