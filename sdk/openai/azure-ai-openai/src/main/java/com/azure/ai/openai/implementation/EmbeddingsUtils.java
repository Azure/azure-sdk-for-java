// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.core.util.Base64Util;
import com.azure.core.util.BinaryData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** This class contains convenience methods and constants for operations related to Embeddings */
public final class EmbeddingsUtils {

    // This method applies to both AOAI and OAI clients
    @SuppressWarnings("unchecked")
    public static BinaryData addEncodingFormat(BinaryData inputJson) {
        Map<String, Object> mapJson = inputJson.toObject(Map.class);

        if (mapJson.containsKey("encoding_format")) {
            return inputJson;
        }

        mapJson.put("encoding_format", "base64");
        return BinaryData.fromObject(mapJson);
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
