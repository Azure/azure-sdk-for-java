// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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

    // This is the payload that gets parsed in the fallback case. It does not have time zone information.
    // For test purposes, we need to inject the current time here, so the test works in different regions.
    String jsonWithoutExpiresOnUnixTime =  "{\n"
        + "  \"accessToken\": \"tokenValue\",\n"
        + "  \"expiresOn\": \"%s\",\n"
        + "  \"subscription\": \"subscriptionValue\",\n"
        + "  \"tenant\": \"tenantValue\",\n"
        + "  \"tokenType\": \"Bearer\"\n"
        + "}";
    @Test
    public void testRoundTripWithoutExpiresOnUnixTime() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        // This test is largely testing the round trip and conversion of the local time returned from az
        // to a UTC time. Set up the current time to allow this to work in all time zones.
        Clock clock = Clock.fixed(Instant.parse("2024-02-28T20:05:53.123456Z"), ZoneId.of("Z"));
        OffsetDateTime expected = OffsetDateTime.now(clock);
        LocalDateTime localNow = expected.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        expected = expected.atZoneSameInstant(ZoneId.of("Z")).toOffsetDateTime();
        // recreate the incorrect date format from az
        String nowString = localNow.format(DateTimeFormatter.ISO_DATE) + " " + localNow.format(DateTimeFormatter.ISO_TIME);
        String localJson = String.format(jsonWithoutExpiresOnUnixTime, nowString);

        try {
            try (JsonReader reader = JsonProviders.createReader(localJson)) {
                AzureCliToken token = AzureCliToken.fromJson(reader);
                JsonWriter writer = JsonProviders.createWriter(stream);
                token.toJson(writer);
                assertNull(token.getExpiresOnUnixTime());
                assertEquals("tokenValue", token.getAccessToken());
                assertEquals(nowString, token.getExpiresOn());
                assertEquals("subscriptionValue", token.getSubscription());
                assertEquals("tenantValue", token.getTenant());
                assertEquals("Bearer", token.getTokenType());
                assertEquals(expected, token.getTokenExpiration());
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
