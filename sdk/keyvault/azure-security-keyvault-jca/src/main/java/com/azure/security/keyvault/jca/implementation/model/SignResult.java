// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Result of sign certificate
 */
public class SignResult implements JsonSerializable<SignResult> {
    private String kid;

    private String value;

    /**
     * get keyId
     * @return keyId
     */
    public String getKid() {
        return kid;
    }

    /**
     * set keyId
     * @param kid keyId
     */
    public void setKid(String kid) {
        this.kid = kid;
    }

    /**
     * get key value
     * @return key value
     */
    public String getValue() {
        return value;
    }

    /**
     * set key value
     * @param value key value
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("kid", kid);
        jsonWriter.writeStringField("value", value);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link SignResult} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link SignResult} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link SignResult}.
     */
    public static SignResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            SignResult deserializedSignResult = new SignResult();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("kid".equals(fieldName)) {
                    deserializedSignResult.kid = reader.getString();
                } else if ("value".equals(fieldName)) {
                    deserializedSignResult.value = unquote(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSignResult;
        });
    }

    private static String unquote(String string) {
        if (string != null && !string.isEmpty()) {
            final char firstCharacter = string.charAt(0);

            if (firstCharacter == '\"' || firstCharacter == '\'') {
                final int base64UrlStringLength = string.length();
                final char lastCharacter = string.charAt(base64UrlStringLength - 1);

                if (lastCharacter == firstCharacter) {
                    return string.substring(1, base64UrlStringLength - 1);
                }
            }
        }

        return string;
    }
}
