// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.implementation.parsergen.MaterialPropertyDigest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertNull(propertyDigest.getClassType());
        Assertions.assertEquals(propertyDigest.getDataType(), "langString");
        Assertions.assertNull(propertyDigest.getDictionaryKey());
        Assertions.assertNull(propertyDigest.getDtmiSegment());
        Assertions.assertFalse(propertyDigest.isAbstract());
        Assertions.assertTrue(propertyDigest.isLiteral());
        Assertions.assertTrue(propertyDigest.isInherited());
        Assertions.assertFalse(propertyDigest.isKey());
        Assertions.assertTrue(propertyDigest.isPlural());
        Assertions.assertTrue(propertyDigest.isOptional());
        Assertions.assertFalse(propertyDigest.isSegment());
        Assertions.assertFalse(propertyDigest.isShadowed());
        Assertions.assertTrue(propertyDigest.getPropertyVersions().containsKey(2));
        Assertions.assertTrue(propertyDigest.getPropertyVersions().containsKey(3));
        Assertions.assertTrue(propertyDigest.getPropertyVersions().get(2).isAllowed());
        Assertions.assertFalse(propertyDigest.getPropertyVersions().get(2).isIdRequired());
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getDefaultLanguage(),  "en");
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMaxLength(),  64);
        Assertions.assertTrue(propertyDigest.getPropertyVersions().get(2).isTypeRequired());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getMaxCount());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getClassType());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getMinCount());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getPattern());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getMaxInclusive());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getMinInclusive());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getUniqueProperties());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getValue());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getValues());
        Assertions.assertNull(propertyDigest.getPropertyVersions().get(2).getClassVersions());
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
