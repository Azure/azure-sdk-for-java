// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.ApproximateLocation;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests for ApproximateLocation serialization, focusing on the TimeZone type override.
 */
public class ApproximateLocationSerializationTests {

    /**
     * Tests serialization with timezone set.
     */
    @Test
    public void testSerializationWithTimezone() throws IOException {
        ApproximateLocation location = new ApproximateLocation().setCountry("US")
            .setRegion("California")
            .setCity("San Francisco")
            .setTimezone(TimeZone.getTimeZone("America/Los_Angeles"));

        String json = serializeToJson(location);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"approximate\""));
        assertTrue(json.contains("\"country\":\"US\""));
        assertTrue(json.contains("\"region\":\"California\""));
        assertTrue(json.contains("\"city\":\"San Francisco\""));
        assertTrue(json.contains("\"timezone\":\"America/Los_Angeles\""));
    }

    /**
     * Tests serialization with timezone not set (null).
     */
    @Test
    public void testSerializationWithoutTimezone() throws IOException {
        ApproximateLocation location = new ApproximateLocation().setCountry("US");

        String json = serializeToJson(location);

        assertNotNull(json);
        assertTrue(json.contains("\"country\":\"US\""));
        assertFalse(json.contains("\"timezone\""));
    }

    /**
     * Tests serialization with a UTC timezone.
     */
    @Test
    public void testSerializationWithUtcTimezone() throws IOException {
        ApproximateLocation location = new ApproximateLocation().setTimezone(TimeZone.getTimeZone("UTC"));

        String json = serializeToJson(location);

        assertNotNull(json);
        assertTrue(json.contains("\"timezone\":\"UTC\""));
    }

    /**
     * Tests deserialization with timezone present.
     */
    @Test
    public void testDeserializationWithTimezone() throws IOException {
        String json = "{\"type\":\"approximate\",\"country\":\"US\",\"region\":\"California\","
            + "\"city\":\"San Francisco\",\"timezone\":\"America/Los_Angeles\"}";

        ApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertEquals("approximate", location.getType());
        assertEquals("US", location.getCountry());
        assertEquals("California", location.getRegion());
        assertEquals("San Francisco", location.getCity());
        assertNotNull(location.getTimezone());
        assertEquals("America/Los_Angeles", location.getTimezone().getID());
    }

    /**
     * Tests deserialization with timezone absent.
     */
    @Test
    public void testDeserializationWithoutTimezone() throws IOException {
        String json = "{\"type\":\"approximate\",\"country\":\"JP\",\"city\":\"Tokyo\"}";

        ApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertEquals("JP", location.getCountry());
        assertEquals("Tokyo", location.getCity());
        assertNull(location.getTimezone());
    }

    /**
     * Tests deserialization with a UTC timezone.
     */
    @Test
    public void testDeserializationWithUtcTimezone() throws IOException {
        String json = "{\"type\":\"approximate\",\"timezone\":\"UTC\"}";

        ApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertNotNull(location.getTimezone());
        assertEquals("UTC", location.getTimezone().getID());
    }

    /**
     * Tests round-trip serialization/deserialization preserves timezone value.
     */
    @Test
    public void testRoundTripWithTimezone() throws IOException {
        ApproximateLocation original = new ApproximateLocation().setCountry("DE")
            .setRegion("Bavaria")
            .setCity("Munich")
            .setTimezone(TimeZone.getTimeZone("Europe/Berlin"));

        String json = serializeToJson(original);
        ApproximateLocation deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getCountry(), deserialized.getCountry());
        assertEquals(original.getRegion(), deserialized.getRegion());
        assertEquals(original.getCity(), deserialized.getCity());
        assertEquals(original.getTimezone().getID(), deserialized.getTimezone().getID());
    }

    /**
     * Tests round-trip serialization/deserialization with null timezone.
     */
    @Test
    public void testRoundTripWithoutTimezone() throws IOException {
        ApproximateLocation original = new ApproximateLocation().setCountry("GB").setCity("London");

        String json = serializeToJson(original);
        ApproximateLocation deserialized = deserializeFromJson(json);

        assertNotNull(deserialized);
        assertEquals(original.getCountry(), deserialized.getCountry());
        assertEquals(original.getCity(), deserialized.getCity());
        assertNull(deserialized.getTimezone());
    }

    /**
     * Tests round-trip with multiple different timezone IDs.
     */
    @Test
    public void testRoundTripWithVariousTimezones() throws IOException {
        String[] timezoneIds
            = { "America/New_York", "Europe/London", "Asia/Tokyo", "Australia/Sydney", "Pacific/Auckland" };

        for (String tzId : timezoneIds) {
            ApproximateLocation original = new ApproximateLocation().setTimezone(TimeZone.getTimeZone(tzId));

            String json = serializeToJson(original);
            ApproximateLocation deserialized = deserializeFromJson(json);

            assertEquals(tzId, deserialized.getTimezone().getID(), "Round-trip failed for timezone: " + tzId);
        }
    }

    /**
     * Tests that an unknown/invalid timezone ID deserializes as null instead of silently falling back to GMT.
     */
    @Test
    public void testDeserializationWithUnknownTimezoneReturnsNull() throws IOException {
        String json = "{\"type\":\"approximate\",\"country\":\"US\",\"timezone\":\"Not/AZone\"}";

        ApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertEquals("US", location.getCountry());
        assertNull(location.getTimezone(), "Unknown timezone ID should deserialize as null, not GMT");
    }

    /**
     * Tests that the valid "GMT" timezone ID is correctly deserialized (not treated as unknown).
     */
    @Test
    public void testDeserializationWithGmtTimezone() throws IOException {
        String json = "{\"type\":\"approximate\",\"timezone\":\"GMT\"}";

        ApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertNotNull(location.getTimezone());
        assertEquals("GMT", location.getTimezone().getID());
    }

    // Helper method to serialize ApproximateLocation to JSON string
    private String serializeToJson(ApproximateLocation location) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            location.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    // Helper method to deserialize JSON string to ApproximateLocation
    private ApproximateLocation deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return ApproximateLocation.fromJson(jsonReader);
        }
    }
}
