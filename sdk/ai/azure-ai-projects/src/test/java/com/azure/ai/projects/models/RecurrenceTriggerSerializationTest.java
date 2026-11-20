// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link RecurrenceTrigger} using {@link java.util.TimeZone}
 * serializes and deserializes the timeZone property as a plain string timezone ID on the wire.
 */
class RecurrenceTriggerSerializationTest {

    /**
     * Verify that serialization of java.util.TimeZone produces a plain string timezone ID.
     */
    @Test
    void serializationProducesTimezoneIdString() throws IOException {
        RecurrenceTrigger trigger
            = new RecurrenceTrigger(1, weeklySchedule()).setTimeZone(TimeZone.getTimeZone("America/New_York"));

        String json = toJsonString(trigger);

        assertTrue(json.contains("\"timeZone\":\"America/New_York\""));
    }

    /**
     * Verify that the default UTC timezone serializes correctly.
     */
    @Test
    void serializationWithUtcTimezone() throws IOException {
        RecurrenceTrigger trigger = new RecurrenceTrigger(1, weeklySchedule()).setTimeZone(TimeZone.getTimeZone("UTC"));

        String json = toJsonString(trigger);

        assertTrue(json.contains("\"timeZone\":\"UTC\""));
    }

    /**
     * Verify that null timeZone is not serialized.
     */
    @Test
    void serializationWithoutTimezone() throws IOException {
        RecurrenceTrigger trigger = new RecurrenceTrigger(1, weeklySchedule());

        String json = toJsonString(trigger);

        assertFalse(json.contains("\"timeZone\""));
    }

    /**
     * Verify that deserialization of a timezone ID string correctly maps to java.util.TimeZone.
     */
    @Test
    void deserializationParsesTimezoneIdString() throws IOException {
        String json = "{\"interval\":1,\"schedule\":{\"daysOfWeek\":[\"Monday\"],\"type\":\"Weekly\"},"
            + "\"type\":\"Recurrence\",\"timeZone\":\"Europe/London\"}";

        RecurrenceTrigger trigger;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            trigger = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(trigger);
        assertNotNull(trigger.getTimeZone());
        assertEquals("Europe/London", trigger.getTimeZone().getID());
    }

    /**
     * Verify that deserialization without timeZone results in null.
     */
    @Test
    void deserializationWithoutTimezone() throws IOException {
        String json = "{\"interval\":2,\"schedule\":{\"daysOfWeek\":[\"Friday\"],\"type\":\"Weekly\"},"
            + "\"type\":\"Recurrence\"}";

        RecurrenceTrigger trigger;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            trigger = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(trigger);
        assertNull(trigger.getTimeZone());
    }

    /**
     * Round-trip: serialize → deserialize should preserve the timezone ID.
     */
    @Test
    void roundTripPreservesTimezoneValue() throws IOException {
        RecurrenceTrigger original
            = new RecurrenceTrigger(1, weeklySchedule()).setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

        String json = toJsonString(original);

        RecurrenceTrigger deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(original.getInterval(), deserialized.getInterval());
        assertEquals("Asia/Tokyo", deserialized.getTimeZone().getID());
    }

    /**
     * Round-trip with null timezone.
     */
    @Test
    void roundTripWithNullTimezone() throws IOException {
        RecurrenceTrigger original = new RecurrenceTrigger(3, weeklySchedule());

        String json = toJsonString(original);

        RecurrenceTrigger deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(original.getInterval(), deserialized.getInterval());
        assertNull(deserialized.getTimeZone());
    }

    /**
     * Round-trip with various timezone IDs.
     */
    @Test
    void roundTripWithVariousTimezones() throws IOException {
        String[] timezoneIds = {
            "America/Los_Angeles",
            "Europe/Berlin",
            "Asia/Shanghai",
            "Australia/Sydney",
            "Pacific/Auckland",
            "UTC" };

        for (String tzId : timezoneIds) {
            RecurrenceTrigger original
                = new RecurrenceTrigger(1, weeklySchedule()).setTimeZone(TimeZone.getTimeZone(tzId));

            String json = toJsonString(original);

            RecurrenceTrigger deserialized;
            try (JsonReader reader = JsonProviders.createReader(json)) {
                deserialized = RecurrenceTrigger.fromJson(reader);
            }

            assertEquals(tzId, deserialized.getTimeZone().getID(), "Round-trip failed for timezone: " + tzId);
        }
    }

    /**
     * Verify that an unknown/invalid timezone ID deserializes as null instead of silently falling back to GMT.
     */
    @Test
    void deserializationWithUnknownTimezoneReturnsNull() throws IOException {
        String json = "{\"interval\":1,\"schedule\":{\"daysOfWeek\":[\"Monday\"],\"type\":\"Weekly\"},"
            + "\"type\":\"Recurrence\",\"timeZone\":\"Not/AZone\"}";

        RecurrenceTrigger trigger;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            trigger = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(trigger);
        assertNull(trigger.getTimeZone(), "Unknown timezone ID should deserialize as null, not GMT");
    }

    /**
     * Verify that the valid "GMT" timezone ID is correctly deserialized (not treated as unknown).
     */
    @Test
    void deserializationWithGmtTimezone() throws IOException {
        String json = "{\"interval\":1,\"schedule\":{\"daysOfWeek\":[\"Monday\"],\"type\":\"Weekly\"},"
            + "\"type\":\"Recurrence\",\"timeZone\":\"GMT\"}";

        RecurrenceTrigger trigger;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            trigger = RecurrenceTrigger.fromJson(reader);
        }

        assertNotNull(trigger);
        assertNotNull(trigger.getTimeZone());
        assertEquals("GMT", trigger.getTimeZone().getID());
    }

    private static WeeklyRecurrenceSchedule weeklySchedule() {
        return new WeeklyRecurrenceSchedule(Arrays.asList(DayOfWeek.MONDAY));
    }

    private static String toJsonString(RecurrenceTrigger trigger) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            trigger.toJson(writer);
        }
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }
}
