// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca.implementation.model;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.OffsetDateTime;

/**
 * An OAuth2 token.
 */
public class AccessToken implements JsonSerializable<AccessToken> {
    /**
     * Stores the access token.
     */
    private String accessToken;

    /**
     * Stores the life duration of the access token.
     */
    private long expiresIn;

    /**
     * Get the life duration of the access token in seconds.
     *
     * @return The life duration of the access token in seconds.
     */
    public long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Set the life duration of the access token in seconds.
     *
     * @param expiresIn The life duration of the access token in seconds.
     */
    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    /**
     * Stores the time when the token is retrieved for the first time.
     */
    private final OffsetDateTime creationDate = OffsetDateTime.now();

    /**
     * Get the access token.
     *
     * @return The access token.
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Set the access token.
     *
     * @param accessToken The access token.
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * Indicates whether the token is expired or not.
     *
     * <p>Reserve 60 seconds, in case that the time the token is used when it is still valid but when it gets to the
     * server side, it expires.</p>
     *
     * @return A value indicating whether the token is expired or not.
     */
    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(creationDate.plusSeconds(expiresIn - 60));
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("accessToken", this.accessToken);
        jsonWriter.writeNumberField("expiresIn", this.expiresIn);

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of {@link AccessToken} from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     *
     * @return An instance of {@link AccessToken} if the {@link JsonReader} was pointing to an instance of it, or
     * {@code null} if it was pointing to JSON {@code null}.
     *
     * @throws IOException If an error occurs while reading the {@link AccessToken}.
     */
    public static AccessToken fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AccessToken deserializedAccessToken = new AccessToken();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();

                reader.nextToken();

                if ("accessToken".equals(fieldName)) {
                    deserializedAccessToken.accessToken = reader.getString();
                } else if ("expiresIn".equals(fieldName)) {
                    deserializedAccessToken.expiresIn = reader.getLong();
                } else {
                    reader.skipChildren();
                }
            }

            return deserializedAccessToken;
        });
    }
}
