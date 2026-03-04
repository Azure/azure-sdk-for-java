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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Verifies that {@link WeeklyRecurrenceSchedule} using {@link java.time.DayOfWeek}
 * produces the same wire-format JSON as the old TSP-generated DayOfWeek (PascalCase strings).
 */
class WeeklyRecurrenceScheduleSerializationTest {

    /**
     * The API uses PascalCase day names ("Monday", "Friday") on the wire.
     * Verify that serialization of java.time.DayOfWeek produces these values.
     */
    @Test
    void serializationProducesPascalCaseDayNames() throws IOException {
        List<DayOfWeek> days = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
        WeeklyRecurrenceSchedule schedule = new WeeklyRecurrenceSchedule(days);

        String json = toJsonString(schedule);

        // The old TSP-generated DayOfWeek serialized as "Monday", "Friday" (PascalCase).
        // The API expects this format on the wire.
        String expected = "{\"daysOfWeek\":[\"Monday\",\"Friday\"],\"type\":\"Weekly\"}";
        assertEquals(expected, json);
    }

    /**
     * Verify that deserialization of PascalCase day names ("Monday", "Friday")
     * from the API correctly maps to java.time.DayOfWeek enum values.
     */
    @Test
    void deserializationParsesPascalCaseDayNames() throws IOException {
        String json = "{\"daysOfWeek\":[\"Monday\",\"Wednesday\",\"Saturday\"],\"type\":\"Weekly\"}";

        WeeklyRecurrenceSchedule schedule;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            schedule = WeeklyRecurrenceSchedule.fromJson(reader);
        }

        assertNotNull(schedule);
        List<DayOfWeek> expected = Arrays.asList(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY);
        assertEquals(expected, schedule.getDaysOfWeek());
        assertEquals(RecurrenceType.WEEKLY, schedule.getType());
    }

    /**
     * Round-trip: serialize → deserialize should yield the same DayOfWeek values.
     */
    @Test
    void roundTripPreservesDayOfWeekValues() throws IOException {
        List<DayOfWeek> allDays = Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);
        WeeklyRecurrenceSchedule original = new WeeklyRecurrenceSchedule(allDays);

        String json = toJsonString(original);

        WeeklyRecurrenceSchedule deserialized;
        try (JsonReader reader = JsonProviders.createReader(json)) {
            deserialized = WeeklyRecurrenceSchedule.fromJson(reader);
        }

        assertNotNull(deserialized);
        assertEquals(allDays, deserialized.getDaysOfWeek());
    }

    private static String toJsonString(WeeklyRecurrenceSchedule schedule) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            schedule.toJson(writer);
        }
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }
}
