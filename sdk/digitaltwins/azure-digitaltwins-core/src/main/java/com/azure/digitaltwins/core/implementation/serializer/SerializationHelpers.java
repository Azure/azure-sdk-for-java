// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Encodes the continuation token as a json encoded string
 */
public final class SerializationHelpers {
    private static final SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    /**
     * Encodes the continuation token as a json encoded string that the ADT service expects
     * @param continuationToken The continuation token.
     * @return Json encoded String
     * example:
     * Input:   {"_t":2,"_s":null,"_rc":"[{\"token\":\"+RID:~WftkAMiSVqReAQAAAAAAAA==#RT:4#TRC:100#ISV:2#IEO:65551#FPC:AgEAAAAMAFEBAMARQP9/n4Eqjw==\",\"range\":{\"min\":\"\",\"max\":\"05C1DFFFFFFFFC\"}}]","_q":"SELECT * FROM digitaltwins where IsOccupied = true"}
     * Output: "{\"_t\":2,\"_s\":null,\"_rc\":\"[{\\\"token\\\":\\\"+RID:~WftkAMiSVqReAQAAAAAAAA==#RT:4#TRC:100#ISV:2#IEO:65551#FPC:AgEAAAAMAFEBAMARQP9/n4Eqjw==\\\",\\\"range\\\":{\\\"min\\\":\\\"\\\",\\\"max\\\":\\\"05C1DFFFFFFFFC\\\"}}]\",\"_q\":\"SELECT * FROM digitaltwins where IsOccupied = true\"}"
     */
    public static String serializeContinuationToken(String continuationToken) {
        try {
            return SERIALIZER_ADAPTER.serialize(continuationToken, SerializerEncoding.JSON);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid continuation token", e);
        }
    }

    /**
     * Helper method that writes the {@code stringToken} value either as raw JSON content if the string value represents
     * a JSON string, array, or object, otherwise it writes the string value as a JSON string.
     *
     * @param jsonWriter The JSON writer to write the string token to.
     * @param fieldName The field name to write.
     * @param stringToken The string token to write.
     * @throws IOException If an I/O error occurs.
     */
    public static void serializeStringHelper(JsonWriter jsonWriter, String fieldName, String stringToken)
        throws IOException {
        if (shouldWriteStringRawValue(stringToken)) {
            jsonWriter.writeRawField(fieldName, stringToken);
        } else {
            jsonWriter.writeStringField(fieldName, stringToken);
        }
    }

    /**
     * Decides whether a string token should be written as a raw value.
     * <p>
     * For example: a string representation of a json payload should be written as raw value as it's the json part we
     * are interested in. It's important to note that only string tokens will end up in the string serializer. If the
     * token is of a non-string primitive type, it should be written as a string and not as that data type. Take "1234"
     * or "false" as examples, they are both valid json nodes of types Number and Boolean but the token is not intended
     * to be intercepted as primitive types (since it's a string token). The only types we like to treat as json
     * payloads are actual json objects (for when String is chosen as the generic type for APIs) or the token itself is
     * an escaped json string node.
     *
     * @param stringToken The string token to evaluate.
     * @return True if the string token should be treated as a json node and not a string representation.
     */
    private static boolean shouldWriteStringRawValue(String stringToken) {
        try (JsonReader jsonReader = JsonProviders.createReader(stringToken)) {
            JsonToken jsonToken = jsonReader.currentToken();
            if (jsonToken == null) {
                jsonToken = jsonReader.nextToken();
            }

            return jsonToken == JsonToken.STRING
                || jsonToken == JsonToken.START_OBJECT
                || jsonToken == JsonToken.START_ARRAY;
        } catch (IOException ignored) {
            return false;
        }
    }
}
