// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgentOptimizationDurationSerializationTests {
    @Test
    public void jobRunDurationDeserializesMilliseconds() throws IOException {
        AgentOptimizationJob job = deserialize("{\"run_duration_ms\":42500}", AgentOptimizationJob::fromJson);

        assertEquals(Duration.ofMillis(42500), job.getRunDuration());
    }

    @Test
    public void candidateAverageLatencyUsesMillisecondsOnWire() throws IOException {
        AgentOptimizationCandidateEvaluation evaluation
            = deserialize("{\"avg_latency_ms\":1500}", AgentOptimizationCandidateEvaluation::fromJson);

        assertEquals(Duration.ofMillis(1500), evaluation.getAverageLatency());
        assertWireName(evaluation, "avg_latency_ms", "average_latency");
    }

    @Test
    public void jobLatencyUsesMillisecondsOnWire() throws IOException {
        AgentOptimizationJobLatency latency
            = deserialize("{\"stage\":\"evaluation\",\"avg_latency_ms\":2750,\"call_count\":3}",
                AgentOptimizationJobLatency::fromJson);

        assertEquals(Duration.ofMillis(2750), latency.getAverageLatency());
        assertWireName(latency, "avg_latency_ms", "average_latency");
    }

    private static void assertWireName(JsonSerializable<?> value, String expected, String unexpected)
        throws IOException {
        String json = serialize(value);
        assertTrue(json.contains("\"" + expected + "\""), "expected wire key '" + expected + "' in " + json);
        assertFalse(json.contains("\"" + unexpected + "\""), "unexpected wire key '" + unexpected + "' in " + json);
    }

    private static String serialize(JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(out)) {
            value.toJson(writer);
        }
        return out.toString("UTF-8");
    }

    private static <T> T deserialize(String json, JsonDeserializer<T> deserializer) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return deserializer.deserialize(reader);
        }
    }

    @FunctionalInterface
    private interface JsonDeserializer<T> {
        T deserialize(JsonReader reader) throws IOException;
    }
}
