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

class DataGenerationJobSerializationTests {
    private static final long CREATED_AT = 1704067200L;
    private static final long FINISHED_AT = 1704153600L;

    @Test
    void evaluationJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("evaluation", true))) {
            DataGenerationJob job = DataGenerationJob.fromJson(reader);

            assertEquals(EvaluationDataGenerationJob.class, job.getClass());
            assertTimestamps(job);
        }
    }

    @Test
    void supervisedFineTuningJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("supervised_finetuning_preview", true))) {
            DataGenerationJob job = DataGenerationJob.fromJson(reader);

            assertEquals(SupervisedFineTuningDataGenerationJob.class, job.getClass());
            assertEquals(DataGenerationJobScenario.SUPERVISED_FINETUNING_PREVIEW, job.getScenario());
            assertTimestamps(job);
        }
    }

    @Test
    void reinforcementFineTuningJobDeserializesUnixTimestamps() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("reinforcement_finetuning_preview", true))) {
            DataGenerationJob job = DataGenerationJob.fromJson(reader);

            assertEquals(ReinforcementFineTuningDataGenerationJob.class, job.getClass());
            assertEquals(DataGenerationJobScenario.REINFORCEMENT_FINETUNING_PREVIEW, job.getScenario());
            assertTimestamps(job);
        }
    }

    @Test
    void absentFinishedAtDeserializesAsNull() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("evaluation", false))) {
            DataGenerationJob job = DataGenerationJob.fromJson(reader);

            assertEquals(toOffsetDateTime(CREATED_AT), job.getCreatedAt());
            assertNull(job.getFinishedAt());
        }
    }

    @Test
    void responseFieldsAndResultDeserializeFromBaseType() throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json("evaluation", true))) {
            DataGenerationJob job = DataGenerationJob.fromJson(reader);

            assertEquals("job-id", job.getId());
            assertEquals("job", job.getName());
            assertEquals(JobStatus.SUCCEEDED, job.getStatus());
            assertEquals(DataGenerationJobScenario.EVALUATION, job.getScenario());
            assertEquals(3, job.getResult().getGeneratedSamples());
            DatasetDataGenerationJobOutput output
                = (DatasetDataGenerationJobOutput) job.getResult().getOutputs().get(0);
            assertEquals("dataset-id", output.getId());
            assertEquals("dataset", output.getName());
            assertEquals("1", output.getVersion());
        }
    }

    private static String json(String scenario, boolean includeFinishedAt) {
        return "{\"id\":\"job-id\",\"status\":\"succeeded\",\"name\":\"job\",\"sources\":[],\"scenario\":\"" + scenario
            + "\",\"created_at\":" + CREATED_AT
            + ",\"result\":{\"generated_samples\":3,\"outputs\":[{\"type\":\"dataset\",\"id\":\"dataset-id\","
            + "\"name\":\"dataset\",\"version\":\"1\"}]}" + (includeFinishedAt ? ",\"finished_at\":" + FINISHED_AT : "")
            + "}";
    }

    private static void assertTimestamps(DataGenerationJob job) {
        assertEquals(toOffsetDateTime(CREATED_AT), job.getCreatedAt());
        assertEquals(toOffsetDateTime(FINISHED_AT), job.getFinishedAt());
    }

    private static OffsetDateTime toOffsetDateTime(long epochSeconds) {
        return OffsetDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneOffset.UTC);
    }
}
