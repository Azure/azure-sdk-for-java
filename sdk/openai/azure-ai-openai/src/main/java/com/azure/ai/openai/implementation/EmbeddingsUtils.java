// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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

    // This method converts a base64 string to a list of floats
    public static List<Float> convertBase64ToFloatList(String embedding) {
        byte[] bytes = Base64Util.decodeString(embedding);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        List<Float> floatList = new ArrayList<>(floatBuffer.remaining());
        while (floatBuffer.hasRemaining()) {
            floatList.add(floatBuffer.get());
        }
        return floatList;
    }
}
