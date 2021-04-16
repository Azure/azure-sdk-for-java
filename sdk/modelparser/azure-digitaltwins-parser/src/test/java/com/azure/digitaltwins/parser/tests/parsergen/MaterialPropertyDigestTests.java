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
        Assertions.assertEquals(propertyDigest.getClassType(), null);
        Assertions.assertEquals(propertyDigest.getDataType(), "langString");
        Assertions.assertEquals(propertyDigest.getDictionaryKey(), null);
        Assertions.assertEquals(propertyDigest.getDtmiSegment(),  null);
        Assertions.assertEquals(propertyDigest.isAbstract(),  false);
        Assertions.assertEquals(propertyDigest.isLiteral(),  true);
        Assertions.assertEquals(propertyDigest.isInherited(),  true);
        Assertions.assertEquals(propertyDigest.isKey(),  false);
        Assertions.assertEquals(propertyDigest.isPlural(),  true);
        Assertions.assertEquals(propertyDigest.isOptional(),  true);
        Assertions.assertEquals(propertyDigest.isSegment(),  false);
        Assertions.assertEquals(propertyDigest.isShadowed(),  false);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().containsKey(2),  true);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().containsKey(3),  true);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).isAllowed(),  true);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).isIdRequired(),  false);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getDefaultLanguage(),  "en");
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMaxLength(),  64);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).isTypeRequired(),  true);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMaxCount(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getClassType(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMinCount(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getPattern(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMaxInclusive(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getMinInclusive(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getUniqueProperties(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getValue(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getValues(),  null);
        Assertions.assertEquals(propertyDigest.getPropertyVersions().get(2).getClassVersions(),  null);
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
