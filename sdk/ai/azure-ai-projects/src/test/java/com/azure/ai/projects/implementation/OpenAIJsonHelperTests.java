// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.implementation;

import com.azure.core.util.BinaryData;
import com.openai.core.ObjectMappers;
import com.openai.models.Reasoning;
import com.openai.models.ReasoningEffort;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenAIJsonHelperTests {
    @Test
    public void reasoningSerializationDoesNotIncludeComputedProperties() throws IOException {
        Reasoning reasoning = Reasoning.builder()
            .effort(ReasoningEffort.LOW)
            .summary(Reasoning.Summary.CONCISE)
            .generateSummary(Reasoning.GenerateSummary.AUTO)
            .build();

        BinaryData serialized = OpenAIJsonHelper.toBinaryData(reasoning);
        String expected = "{\"effort\":\"low\",\"summary\":\"concise\",\"generate_summary\":\"auto\"}";

        assertFalse(serialized.toString().contains("\"isValid\""));
        assertEquals(ObjectMappers.jsonMapper().readTree(expected),
            ObjectMappers.jsonMapper().readTree(serialized.toString()));
        assertEquals(reasoning, OpenAIJsonHelper.fromBinaryData(serialized, Reasoning.class));
    }

    @Test
    public void reasoningRoundTripPreservesAdditionalProperties() throws IOException {
        String json = "{\"effort\":\"low\",\"future_setting\":{\"enabled\":true}}";
        Reasoning reasoning = OpenAIJsonHelper.fromBinaryData(BinaryData.fromString(json), Reasoning.class);

        assertEquals(ReasoningEffort.LOW, reasoning.effort().get());
        assertEquals(ObjectMappers.jsonMapper().readTree(json),
            ObjectMappers.jsonMapper().readTree(OpenAIJsonHelper.toBinaryData(reasoning).toString()));
    }

    @Test
    public void listConversionPreservesNullElements() throws IOException {
        Reasoning reasoning = Reasoning.builder().effort(ReasoningEffort.HIGH).build();
        List<Reasoning> values = Arrays.asList(reasoning, null);

        List<BinaryData> serialized = OpenAIJsonHelper.toBinaryDataList(values);

        assertEquals(2, serialized.size());
        assertEquals(ObjectMappers.jsonMapper().readTree("{\"effort\":\"high\"}"),
            ObjectMappers.jsonMapper().readTree(serialized.get(0).toString()));
        assertNull(serialized.get(1));
        assertEquals(values, OpenAIJsonHelper.fromBinaryDataList(serialized, Reasoning.class));
    }

    @Test
    public void nullAndEmptyConversions() {
        assertNull(OpenAIJsonHelper.toBinaryData(null));
        assertNull(OpenAIJsonHelper.fromBinaryData(null, Reasoning.class));
        assertNull(OpenAIJsonHelper.toBinaryDataList(null));
        assertNull(OpenAIJsonHelper.fromBinaryDataList(null, Reasoning.class));
        assertTrue(OpenAIJsonHelper.toBinaryDataList(Collections.emptyList()).isEmpty());
        assertTrue(OpenAIJsonHelper.fromBinaryDataList(Collections.emptyList(), Reasoning.class).isEmpty());
    }
}
