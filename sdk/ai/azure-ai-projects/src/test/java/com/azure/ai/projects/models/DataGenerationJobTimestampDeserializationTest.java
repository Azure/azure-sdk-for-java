// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataGenerationJobTimestampDeserializationTest {
    private static final long CREATED_AT = 1704067200L;
    private static final long FINISHED_AT = 1704153600L;

    @Test
    void evaluationJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("evaluation", true))) {
            EvaluationDataGenerationJob job = EvaluationDataGenerationJob.fromJson(reader);

            assertTimestamps(job);
        }
    }

    @Test
    void supervisedFineTuningJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("supervised_finetuning", true))) {
            SupervisedFineTuningDataGenerationJob job = SupervisedFineTuningDataGenerationJob.fromJson(reader);

            assertTimestamps(job);
        }
    }

    @Test
    void reinforcementFineTuningJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("reinforcement_finetuning", true))) {
            ReinforcementFineTuningDataGenerationJob job = ReinforcementFineTuningDataGenerationJob.fromJson(reader);

            assertTimestamps(job);
        }
    }

    @Test
    void absentFinishedAtDeserializesAsNull() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("evaluation", false))) {
            EvaluationDataGenerationJob job = EvaluationDataGenerationJob.fromJson(reader);

            assertEquals(toOffsetDateTime(CREATED_AT), job.getCreatedAt());
            assertNull(job.getFinishedAt());
        }
    }

    private static String json(String scenario, boolean includeFinishedAt) {
        return "{\"name\":\"job\",\"sources\":[],\"scenario\":\"" + scenario + "\",\"created_at\":" + CREATED_AT
            + (includeFinishedAt ? ",\"finished_at\":" + FINISHED_AT : "") + "}";
    }

    private static void assertTimestamps(DataGenerationJob job) {
        assertEquals(toOffsetDateTime(CREATED_AT), job.getCreatedAt());
        assertEquals(toOffsetDateTime(FINISHED_AT), job.getFinishedAt());
    }

    private static OffsetDateTime toOffsetDateTime(long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }
}
