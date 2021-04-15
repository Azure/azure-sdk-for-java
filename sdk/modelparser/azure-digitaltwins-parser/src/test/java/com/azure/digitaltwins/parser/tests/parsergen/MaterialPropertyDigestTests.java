// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MaterialPropertyDigestTests {
    static JsonNode complexJsonNode;

    @BeforeAll
    public static void setup() throws JsonProcessingException {
        complexJsonNode = constructJsonNode();
    }

    @Test
    public void testAll() {
        MaterialPropertyDigest propertyDigest = new MaterialPropertyDigest(complexJsonNode);
        propertyDigest.getClassType();
    }


    private static JsonNode constructJsonNode() throws JsonProcessingException {
        final String content =
            "   {"
            + "     \"_\": {"
            + "       \"literal\": true,"
            + "       \"abstract\": false,"
            + "       \"datatype\": \"langString\","
            + "       \"plural\": true,"
            + "       \"optional\": true,"
            + "       \"inherited\": true,"
            + "       \"shadowed\": false,"
            + "       \"isKey\": false,"
            + "       \"isSeg\": false"
            + "     },"
            + "     \"2\": {"
            + "       \"idRequired\": false,"
            + "       \"defaultLanguage\": \"en\","
            + "       \"allowed\": true,"
            + "       \"maxLength\": 64,"
            + "       \"typeRequired\": true"
            + "     },"
            + "     \"3\": {"
            + "       \"idRequired\": false,"
            + "       \"defaultLanguage\": \"en\","
            + "       \"allowed\": true,"
            + "       \"maxLength\": 64,"
            + "       \"typeRequired\": true"
            + "     }"
            + " }";

        return new ObjectMapper().readValue(content, JsonNode.class);
    }
}
