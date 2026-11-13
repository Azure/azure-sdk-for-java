// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScheduleTriggerTimeZoneSerializationTests {
    @ParameterizedTest
    @ValueSource(strings = { "UTC", "America/New_York", "GMT+05:30", "PST" })
    void cronTimeZoneRoundTripsAsString(String timeZoneId) {
        OffsetDateTime startAt = OffsetDateTime.parse("2026-11-11T00:00:00Z");
        OffsetDateTime endAt = startAt.plusDays(1);
        CronTrigger original = new CronTrigger("0 * * * *").setTimeZone(TimeZone.getTimeZone(timeZoneId))
            .setStartAt(startAt)
            .setEndAt(endAt);

        BinaryData serialized = BinaryData.fromObject(original);
        assertEquals(timeZoneId, serialized.toObject(Map.class).get("timeZone"));
        assertEquals("2026-11-11T00:00:00Z", serialized.toObject(Map.class).get("startTime"));
        assertEquals("2026-11-12T00:00:00Z", serialized.toObject(Map.class).get("endTime"));

        CronTrigger restored = serialized.toObject(CronTrigger.class);
        assertEquals(timeZoneId, restored.getTimeZone().getID());
        assertEquals(startAt, restored.getStartAt());
        assertEquals(endAt, restored.getEndAt());
    }

    @ParameterizedTest
    @ValueSource(strings = { "UTC", "America/New_York", "GMT+05:30", "PST" })
    void oneTimeTimeZoneRoundTripsAsString(String timeZoneId) {
        OffsetDateTime triggerAt = OffsetDateTime.parse("2026-11-11T00:00:00Z");
        OneTimeTrigger original = new OneTimeTrigger(triggerAt).setTimeZone(TimeZone.getTimeZone(timeZoneId));

        BinaryData serialized = BinaryData.fromObject(original);
        assertEquals(timeZoneId, serialized.toObject(Map.class).get("timeZone"));

        OneTimeTrigger restored = serialized.toObject(OneTimeTrigger.class);
        assertEquals(timeZoneId, restored.getTimeZone().getID());
        assertEquals(triggerAt, restored.getTriggerAt());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ",\"timeZone\":null" })
    void absentAndNullTimeZonesRemainNull(String timeZoneField) {
        String cronJson = "{\"type\":\"Cron\",\"expression\":\"0 * * * *\"" + timeZoneField + "}";
        String oneTimeJson = "{\"type\":\"OneTime\",\"triggerAt\":\"2026-11-11T00:00:00Z\"" + timeZoneField + "}";
        CronTrigger cron = BinaryData.fromString(cronJson).toObject(CronTrigger.class);
        OneTimeTrigger oneTime = BinaryData.fromString(oneTimeJson).toObject(OneTimeTrigger.class);

        assertNull(cron.getTimeZone());
        assertNull(oneTime.getTimeZone());
        assertNull(BinaryData.fromObject(cron).toObject(CronTrigger.class).getTimeZone());
        assertNull(BinaryData.fromObject(oneTime).toObject(OneTimeTrigger.class).getTimeZone());
    }

    @Test
    void unknownTimeZonesFailInsteadOfSilentlyBecomingGmt() throws IOException {
        try (JsonReader reader = JsonProviders
            .createReader("{\"type\":\"Cron\",\"expression\":\"0 * * * *\",\"timeZone\":\"Not/AZone\"}")) {
            assertThrows(IllegalArgumentException.class, () -> CronTrigger.fromJson(reader));
        }
        try (JsonReader reader = JsonProviders
            .createReader("{\"type\":\"OneTime\",\"triggerAt\":\"2026-11-11T00:00:00Z\",\"timeZone\":\"Not/AZone\"}")) {
            assertThrows(IllegalArgumentException.class, () -> OneTimeTrigger.fromJson(reader));
        }
    }
}
