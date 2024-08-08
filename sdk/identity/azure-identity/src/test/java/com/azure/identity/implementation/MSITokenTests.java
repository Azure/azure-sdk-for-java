// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MSITokenTests {
    private OffsetDateTime expected = OffsetDateTime.of(2020, 1, 10, 15, 3, 28, 0, ZoneOffset.UTC);



    @Test
    public void canParseLong() {
        MSIToken token = new MSIToken("fake_token", "1578668608", null);
        MSIToken token2 = new MSIToken("fake_token", null, "3599");
        MSIToken token3 = new MSIToken("fake_token", "1578668608", "3599");
        MSIToken token4 = new MSIToken("fake_token", null, "8000");

        assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        assertTrue((token2.getExpiresAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond()) > 3500);
        assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        assertTrue((token4.getRefreshAt().toEpochSecond() - OffsetDateTime.now().toEpochSecond()) > 3900);
    }

    @Test
    public void canDeserialize() {
        String json = "{\n"
            + "  \"access_token\": \"fake_token\",\n"
            + "  \"expires_in\": \"3599\",\n"
            + "  \"expires_on\": \"1506484173\""
            + "}";
        MSIToken token;
        try {
            try (JsonReader reader = JsonProviders.createReader(json)) {
                token = MSIToken.fromJson(reader);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertEquals(1506484173, token.getExpiresAt().toEpochSecond());
        assertEquals("fake_token", token.getToken());
    }

    @Test
    public void canSerialize() {
        MSIToken token = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00", "3599");
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            JsonWriter writer = JsonProviders.createWriter(stream);
            token.toJson(writer);
            writer.flush();
            String json = stream.toString();
            assertEquals("{\"access_token\":\"fake_token\",\"expires_on\":\"01/10/2020 15:03:28 +00:00\",\"expires_in\":\"3599\"}", json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void canParseDateTime24Hr() {
        MSIToken token = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00", null);
        MSIToken token2 = new MSIToken("fake_token", null, "01/10/2020 15:03:28 +00:00");
        MSIToken token3 = new MSIToken("fake_token", "01/10/2020 15:03:28 +00:00",
            "86500");
        MSIToken token4 = new MSIToken("fake_token", null, "43219");

        assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 12L);
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getRefreshAt()) == 6L);
    }

    @Test
    public void canParseDateTime12Hr() {
        MSIToken token = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00", null);
        MSIToken token2 = new MSIToken("fake_token", null, "1/10/2020 3:03:28 PM +00:00");
        MSIToken token3 = new MSIToken("fake_token", "1/10/2020 3:03:28 PM +00:00",
            "86500");
        MSIToken token4 = new MSIToken("fake_token", null, "86500");

        assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 24L);
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getRefreshAt()) == 12L);

        token = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00", null);
        token2 = new MSIToken("fake_token", null, "12/20/2019 4:58:20 AM +00:00");
        token3 = new MSIToken("fake_token", "12/20/2019 4:58:20 AM +00:00",
            "105500");
        token4 = new MSIToken("fake_token", null, "105500");
        expected = OffsetDateTime.of(2019, 12, 20, 4, 58, 20, 0, ZoneOffset.UTC);

        assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 29L);
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getRefreshAt()) == 14L);

        token = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00", null);
        token2 = new MSIToken("fake_token", null, "1/1/2020 0:00:00 PM +00:00");
        token3 = new MSIToken("fake_token", "1/1/2020 0:00:00 PM +00:00",
            "220800");
        token4 = new MSIToken("fake_token", null, "220800");

        expected = OffsetDateTime.of(2020, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(expected.toEpochSecond(), token.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token2.getExpiresAt().toEpochSecond());
        assertEquals(expected.toEpochSecond(), token3.getExpiresAt().toEpochSecond());
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getExpiresAt()) == 61L);
        assertTrue(ChronoUnit.HOURS.between(OffsetDateTime.now(), token4.getRefreshAt()) == 30L);
    }
}
