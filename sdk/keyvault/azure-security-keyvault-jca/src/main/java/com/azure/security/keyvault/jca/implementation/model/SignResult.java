// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.security.keyvault.jca.implementation.utils.Base64Url;

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
                    deserializedSignResult.value = new Base64Url(reader.getString()).toString();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedSignResult;
        });
    }
}
