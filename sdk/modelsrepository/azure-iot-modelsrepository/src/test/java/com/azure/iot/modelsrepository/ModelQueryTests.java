package com.azure.iot.modelsrepository;

import com.azure.iot.modelsrepository.implementation.ModelsQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

public class ModelQueryTests {

    @Test
    public void parseRootDtmi() {
        String content = "{\n" +
            "  \"@id\": \"dtmi:com:example:Building;1\",\n" +
            "  \"@type\": \"Interface\",\n" +
            "  \"displayName\": \"Building\",\n" +
            "  \"contents\": [\n" +
            "    {\n" +
            "      \"@type\": \"Property\",\n" +
            "      \"name\": \"name\",\n" +
            "      \"schema\": \"string\",\n" +
            "      \"writable\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"@type\": \"Relationship\",\n" +
            "      \"name\": \"contains\",\n" +
            "      \"target\": \"dtmi:com:example:Room;1\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"@context\": \"dtmi:dtdl:context;2\"\n" +
            "}";
        ModelsQuery mq = new ModelsQuery(content);
        try {
            mq.parseModel();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
