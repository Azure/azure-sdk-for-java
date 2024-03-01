// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * A wrapper class for deserializing a token payload returned from the Azure CLI.
 */
public final class AzureCliToken implements JsonSerializable<AzureCliToken> {
    private String accessToken;
    private String expiresOn;
    private Long expiresOnUnixTime;
    private String subscription;
    private String tenant;
    private String tokenType;
    private OffsetDateTime tokenExpiry;

    public String getAccessToken() {
        return accessToken;
    }

    public String getExpiresOn() {
        return expiresOn;
    }

    public Long getExpiresOnUnixTime() {
        return expiresOnUnixTime;
    }

    public String getSubscription() {
        return subscription;
    }

    public String getTenant() {
        return tenant;
    }

    public String getTokenType() {
        return tokenType;
    }

    public OffsetDateTime getTokenExpiration() {
        return tokenExpiry;
    }

    private static OffsetDateTime parseExpiresOnTime(String time) {
        OffsetDateTime tokenExpiry;
        // parse the incoming date: 2024-02-28 12:05:53.000000
        tokenExpiry = LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"))
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC);
        return tokenExpiry;
    }

    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("accessToken", accessToken);
        jsonWriter.writeStringField("expiresOn", expiresOn);
        jsonWriter.writeNumberField("expires_on", expiresOnUnixTime);
        jsonWriter.writeStringField("subscription", subscription);
        jsonWriter.writeStringField("tenant", tenant);
        jsonWriter.writeStringField("tokenType", tokenType);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public static AzureCliToken fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            AzureCliToken tokenHolder = new AzureCliToken();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("accessToken".equals(fieldName)) {
                    tokenHolder.accessToken = reader.getString();
                } else if ("expiresOn".equals(fieldName)) {
                    tokenHolder.expiresOn = reader.getString();
                } else if ("expires_on".equals(fieldName)) {
                    tokenHolder.expiresOnUnixTime = reader.getLong();
                } else if ("subscription".equals(fieldName)) {
                    tokenHolder.subscription = reader.getString();
                } else if ("tenant".equals(fieldName)) {
                    tokenHolder.tenant = reader.getString();
                } else if ("tokenType".equals(fieldName)) {
                    tokenHolder.tokenType = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            if (tokenHolder.expiresOnUnixTime != null) {
                tokenHolder.tokenExpiry = Instant.ofEpochSecond(tokenHolder.getExpiresOnUnixTime()).atOffset(ZoneOffset.UTC);
            } else {
                tokenHolder.tokenExpiry = parseExpiresOnTime(tokenHolder.getExpiresOn());
            }

            return tokenHolder;
        });
    }
}
