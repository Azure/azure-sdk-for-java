// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Verifies that {@link AgentOptimizationJobProgress#getElapsed()} is exposed as a {@link Duration}
 * (per the {@code client.java.tsp} {@code @@alternateType} customization) while the underlying JSON
 * wire format remains a plain number of fractional seconds under the key {@code elapsed_seconds}.
 */
public class AgentOptimizationJobProgressSerializationTests {

    /**
     * The wire key stays {@code elapsed_seconds} even though the Java getter is {@code getElapsed()}.
     */
    @Test
    public void wireKeyIsElapsedSeconds() throws IOException {
        String json = "{\"candidates_completed\":1,\"best_score\":0.9,\"elapsed_seconds\":42.5}";
        AgentOptimizationJobProgress progress = deserialize(json);

        String roundTripped = serialize(progress);
        assertTrue(roundTripped.contains("\"elapsed_seconds\""),
            "expected wire key 'elapsed_seconds' in " + roundTripped);
        assertFalse(roundTripped.contains("\"elapsed\":"),
            "elapsed_seconds should not be renamed on the wire in " + roundTripped);
    }

    /**
     * Whole-second values deserialize to {@link Duration#ofSeconds(long)}.
     */
    @Test
    public void deserializesWholeSecondsToDuration() throws IOException {
        String json = "{\"candidates_completed\":3,\"best_score\":0.75,\"elapsed_seconds\":42}";
        AgentOptimizationJobProgress progress = deserialize(json);

        assertNotNull(progress.getElapsed());
        assertEquals(Duration.ofSeconds(42), progress.getElapsed());
        assertEquals(3, progress.getCandidatesCompleted());
        assertEquals(0.75, progress.getBestScore());
    }

    /**
     * Fractional-second wire values (float64) preserve sub-second precision when mapped to {@link Duration}.
     * A value of {@code 42.5} means 42.5 seconds = 42500 ms.
     */
    @Test
    public void deserializesFractionalSecondsPreservingSubSecondPrecision() throws IOException {
        String json = "{\"candidates_completed\":1,\"best_score\":0.9,\"elapsed_seconds\":42.5}";
        AgentOptimizationJobProgress progress = deserialize(json);

        assertEquals(42500L, progress.getElapsed().toMillis());
    }

    /**
     * Very small sub-second values (well below one second) still round-trip through nanos correctly.
     */
    @Test
    public void deserializesSmallFractionalSecondsToNanos() throws IOException {
        // 0.001 seconds = 1_000_000 nanos = 1 ms
        String json = "{\"candidates_completed\":0,\"best_score\":0.0,\"elapsed_seconds\":0.001}";
        AgentOptimizationJobProgress progress = deserialize(json);

        assertEquals(1_000_000L, progress.getElapsed().toNanos());
    }

    /**
     * Absent {@code elapsed_seconds} defaults to {@link Duration#ZERO} (the generated fallback for missing wire values).
     */
    @Test
    public void missingElapsedSecondsDefaultsToZero() throws IOException {
        String json = "{\"candidates_completed\":2,\"best_score\":0.5}";
        AgentOptimizationJobProgress progress = deserialize(json);

        assertEquals(Duration.ZERO, progress.getElapsed());
    }

    /**
     * Round-trip: fractional-second wire value survives {@code fromJson -> toJson -> fromJson} without loss.
     */
    @Test
    public void roundTripPreservesFractionalSeconds() throws IOException {
        String originalJson = "{\"candidates_completed\":5,\"best_score\":0.88,\"elapsed_seconds\":1.5}";

        AgentOptimizationJobProgress first = deserialize(originalJson);
        String reserialized = serialize(first);
        AgentOptimizationJobProgress second = deserialize(reserialized);

        assertEquals(first.getElapsed(), second.getElapsed());
        assertEquals(1500L, second.getElapsed().toMillis());
        assertEquals(first.getCandidatesCompleted(), second.getCandidatesCompleted());
        assertEquals(first.getBestScore(), second.getBestScore());
    }

    /**
     * Round-trip: whole-second wire value survives {@code fromJson -> toJson -> fromJson} without loss.
     */
    @Test
    public void roundTripPreservesWholeSeconds() throws IOException {
        String originalJson = "{\"candidates_completed\":10,\"best_score\":0.99,\"elapsed_seconds\":120}";

        AgentOptimizationJobProgress first = deserialize(originalJson);
        String reserialized = serialize(first);
        AgentOptimizationJobProgress second = deserialize(reserialized);

        assertEquals(Duration.ofSeconds(120), first.getElapsed());
        assertEquals(first.getElapsed(), second.getElapsed());
    }

    private static String serialize(AgentOptimizationJobProgress progress) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(out)) {
            progress.toJson(writer);
        }
        return out.toString("UTF-8");
    }

    private static AgentOptimizationJobProgress deserialize(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return AgentOptimizationJobProgress.fromJson(reader);
        }
    }
}
