// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeServerEventConversationItemInputAudioTranscriptionCompletedSerializationTests {

    @Test
    public void tokenUsageRoundTrips() throws IOException {
        TranscriptTextUsageTokens usage = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"tokens\",\"input_tokens\":2,\"output_tokens\":3,\"total_tokens\":5}",
            TranscriptTextUsageTokens::fromJson);
        RealtimeServerEventConversationItemInputAudioTranscriptionCompleted event
            = new RealtimeServerEventConversationItemInputAudioTranscriptionCompleted("event", "item", 0, "hello",
                usage);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeServerEventConversationItemInputAudioTranscriptionCompleted result = UnionTypeSerializationTestUtils
            .deserialize(json, RealtimeServerEventConversationItemInputAudioTranscriptionCompleted::fromJson);

        assertTrue(json.contains("\"usage\":{\"input_tokens\":2"));
        assertEquals(5L, result.getUsageAsTranscriptTextUsageTokens().getTotalTokens());
        assertNull(result.getUsageAsTranscriptTextUsageDuration());
    }

    @Test
    public void durationUsageRoundTrips() throws IOException {
        TranscriptTextUsageDuration usage = UnionTypeSerializationTestUtils
            .deserialize("{\"type\":\"duration\",\"seconds\":7}", TranscriptTextUsageDuration::fromJson);
        RealtimeServerEventConversationItemInputAudioTranscriptionCompleted event
            = new RealtimeServerEventConversationItemInputAudioTranscriptionCompleted("event", "item", 0, "hello",
                usage);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeServerEventConversationItemInputAudioTranscriptionCompleted result = UnionTypeSerializationTestUtils
            .deserialize(json, RealtimeServerEventConversationItemInputAudioTranscriptionCompleted::fromJson);

        assertTrue(json.contains("\"usage\":{\"seconds\":7"));
        assertEquals(Duration.ofSeconds(7), result.getUsageAsTranscriptTextUsageDuration().getSeconds());
        assertNull(result.getUsageAsTranscriptTextUsageTokens());
    }

    @Test
    public void absentUsageReturnsNull() throws IOException {
        RealtimeServerEventConversationItemInputAudioTranscriptionCompleted event
            = UnionTypeSerializationTestUtils.deserialize(
                "{\"type\":\"conversation.item.input_audio_transcription.completed\",\"event_id\":\"event\","
                    + "\"item_id\":\"item\",\"content_index\":0,\"transcript\":\"hello\"}",
                RealtimeServerEventConversationItemInputAudioTranscriptionCompleted::fromJson);

        assertNull(event.getUsageAsTranscriptTextUsageTokens());
        assertNull(event.getUsageAsTranscriptTextUsageDuration());
    }
}
