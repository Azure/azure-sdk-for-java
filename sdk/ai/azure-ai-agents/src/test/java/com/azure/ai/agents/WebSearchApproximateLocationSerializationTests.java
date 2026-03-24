// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.ai.agents.models.WebSearchApproximateLocation;
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
 * Tests for WebSearchApproximateLocation serialization, focusing on the TimeZone type override.
 */
public class WebSearchApproximateLocationSerializationTests {

    /**
     * Tests serialization with timezone set.
     */
    @Test
    public void testSerializationWithTimezone() throws IOException {
        WebSearchApproximateLocation location = new WebSearchApproximateLocation().setCountry("US")
            .setRegion("California")
            .setCity("San Francisco")
            .setTimezone(TimeZone.getTimeZone("America/Los_Angeles"));

        String json = serializeToJson(location);

        assertNotNull(json);
        assertTrue(json.contains("\"type\":\"approximate\""));
        assertTrue(json.contains("\"country\":\"US\""));
        assertTrue(json.contains("\"timezone\":\"America/Los_Angeles\""));
    }

    /**
     * Tests serialization without timezone.
     */
    @Test
    public void testSerializationWithoutTimezone() throws IOException {
        WebSearchApproximateLocation location = new WebSearchApproximateLocation().setCountry("US");

        String json = serializeToJson(location);

        assertFalse(json.contains("\"timezone\""));
    }

    /**
     * Tests deserialization with timezone present.
     */
    @Test
    public void testDeserializationWithTimezone() throws IOException {
        String json
            = "{\"type\":\"approximate\",\"country\":\"JP\",\"city\":\"Tokyo\"," + "\"timezone\":\"Asia/Tokyo\"}";

        WebSearchApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertEquals("JP", location.getCountry());
        assertEquals("Tokyo", location.getCity());
        assertNotNull(location.getTimezone());
        assertEquals("Asia/Tokyo", location.getTimezone().getID());
    }

    /**
     * Tests deserialization without timezone.
     */
    @Test
    public void testDeserializationWithoutTimezone() throws IOException {
        String json = "{\"type\":\"approximate\",\"country\":\"DE\"}";

        WebSearchApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertNull(location.getTimezone());
    }

    /**
     * Tests round-trip preserves timezone value.
     */
    @Test
    public void testRoundTripWithTimezone() throws IOException {
        WebSearchApproximateLocation original = new WebSearchApproximateLocation().setCountry("GB")
            .setCity("London")
            .setTimezone(TimeZone.getTimeZone("Europe/London"));

        String json = serializeToJson(original);
        WebSearchApproximateLocation deserialized = deserializeFromJson(json);

        assertEquals(original.getCountry(), deserialized.getCountry());
        assertEquals(original.getCity(), deserialized.getCity());
        assertEquals("Europe/London", deserialized.getTimezone().getID());
    }

    /**
     * Tests round-trip with various timezone IDs.
     */
    @Test
    public void testRoundTripWithVariousTimezones() throws IOException {
        String[] timezoneIds
            = { "America/New_York", "Europe/Berlin", "Asia/Shanghai", "Australia/Sydney", "Pacific/Auckland", "UTC" };

        for (String tzId : timezoneIds) {
            WebSearchApproximateLocation original
                = new WebSearchApproximateLocation().setTimezone(TimeZone.getTimeZone(tzId));

            String json = serializeToJson(original);
            WebSearchApproximateLocation deserialized = deserializeFromJson(json);

            assertEquals(tzId, deserialized.getTimezone().getID(), "Round-trip failed for timezone: " + tzId);
        }
    }

    /**
     * Tests that an unknown/invalid timezone ID deserializes as null instead of silently falling back to GMT.
     */
    @Test
    public void testDeserializationWithUnknownTimezoneReturnsNull() throws IOException {
        String json = "{\"type\":\"approximate\",\"country\":\"US\",\"timezone\":\"Not/AZone\"}";

        WebSearchApproximateLocation location = deserializeFromJson(json);

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

        WebSearchApproximateLocation location = deserializeFromJson(json);

        assertNotNull(location);
        assertNotNull(location.getTimezone());
        assertEquals("GMT", location.getTimezone().getID());
    }

    private String serializeToJson(WebSearchApproximateLocation location) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(outputStream)) {
            location.toJson(jsonWriter);
        }
        return outputStream.toString("UTF-8");
    }

    private WebSearchApproximateLocation deserializeFromJson(String json) throws IOException {
        try (JsonReader jsonReader = JsonProviders.createReader(json)) {
            return WebSearchApproximateLocation.fromJson(jsonReader);
        }
    }
}
