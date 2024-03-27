// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;

/** This class contains convenience methods and constants for operations related to Embeddings */
public final class EmbeddingsUtils {
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    // This method applies to both AOAI and OAI clients
    public static BinaryData addEncodingFormat(BinaryData inputJson) throws JsonProcessingException {
        JsonNode jsonNode = JSON_MAPPER.readTree(inputJson.toString());
        if (jsonNode instanceof ObjectNode) {
            ObjectNode objectNode = (ObjectNode) jsonNode;
            objectNode.put("encoding_format", "base64");
            inputJson = BinaryData.fromBytes(objectNode.toString().getBytes(StandardCharsets.UTF_8));
        }

        return inputJson;
    }
}
