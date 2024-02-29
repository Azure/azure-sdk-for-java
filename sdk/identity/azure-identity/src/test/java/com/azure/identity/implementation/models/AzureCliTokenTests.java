// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AzureCliTokenTests {

    String jsonWithExpiresOnUnixTime = "{\n"
        + "  \"accessToken\": \"tokenValue\",\n"
        + "  \"expiresOn\": \"2024-02-28 12:05:53.000000\",\n"
        + "  \"expires_on\": 1709150753,\n"
        + "  \"subscription\": \"subscriptionValue\",\n"
        + "  \"tenant\": \"tenantValue\",\n"
        + "  \"tokenType\": \"Bearer\"\n"
        + "}";

    String jsonWithoutExpires_On =  "{\n"
        + "  \"accessToken\": \"tokenValue\",\n"
        + "  \"expiresOn\": \"2024-02-28 12:05:53.000000\",\n"
        + "  \"subscription\": \"subscriptionValue\",\n"
        + "  \"tenant\": \"tenantValue\",\n"
        + "  \"tokenType\": \"Bearer\"\n"
        + "}";
    @Test
    public void testRoundTripWithoutExpiresOnUnixTime() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            try (JsonReader reader = JsonProviders.createReader(jsonWithoutExpires_On)) {
                AzureCliToken token = AzureCliToken.fromJson(reader);
                JsonWriter writer = JsonProviders.createWriter(stream);
                token.toJson(writer);
                assertNull(token.getExpiresOnUnixTime());
                assertEquals("tokenValue", token.getAccessToken());
                assertEquals("2024-02-28 12:05:53.000000", token.getExpiresOn());
                assertEquals("subscriptionValue", token.getSubscription());
                assertEquals("tenantValue", token.getTenant());
                assertEquals("Bearer", token.getTokenType());
                assertEquals(OffsetDateTime.parse("2024-02-28T20:05:53Z"), token.getTokenExpiration());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testRoundTripWithExpiresOnUnixTime() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            try (JsonReader reader = JsonProviders.createReader(jsonWithExpiresOnUnixTime)) {
                AzureCliToken token = AzureCliToken.fromJson(reader);
                JsonWriter writer = JsonProviders.createWriter(stream);
                token.toJson(writer);
                assertEquals("tokenValue", token.getAccessToken());
                assertEquals("2024-02-28 12:05:53.000000", token.getExpiresOn());
                assertEquals(1709150753, token.getExpiresOnUnixTime());
                assertEquals("subscriptionValue", token.getSubscription());
                assertEquals("tenantValue", token.getTenant());
                assertEquals("Bearer", token.getTokenType());
                assertEquals(OffsetDateTime.parse("2024-02-28T20:05:53Z"), token.getTokenExpiration());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
