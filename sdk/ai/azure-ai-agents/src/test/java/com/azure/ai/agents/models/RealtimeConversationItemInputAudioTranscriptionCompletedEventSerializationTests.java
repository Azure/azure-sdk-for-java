// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.core.util.BinaryData;
import com.openai.models.realtime.ConversationItemInputAudioTranscriptionCompletedEvent.Usage;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeConversationItemInputAudioTranscriptionCompletedEventSerializationTests {

    @Test
    public void tokenUsageRoundTrips() throws IOException {
        Usage usage = OpenAIJsonHelper.fromBinaryData(BinaryData.fromString(
            "{\"type\":\"tokens\",\"input_tokens\":2,\"output_tokens\":3,\"total_tokens\":5}"), Usage.class);
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent event
            = new RealtimeConversationItemInputAudioTranscriptionCompletedEvent("event", "item", 0, "hello", usage);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent result = UnionTypeSerializationTestUtils
            .deserialize(json, RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);

        assertTrue(json.contains("\"input_tokens\":2"));
        assertTrue(result.getUsage().isTranscriptTextUsageTokens());
        assertEquals(5L, result.getUsage().asTranscriptTextUsageTokens().totalTokens());
        assertEquals(usage, result.getUsage());
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
    }

    @Test
    public void durationUsageRoundTrips() throws IOException {
        Usage usage = OpenAIJsonHelper.fromBinaryData(
            BinaryData.fromString("{\"type\":\"duration\",\"seconds\":7.5}"), Usage.class);
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent event
            = new RealtimeConversationItemInputAudioTranscriptionCompletedEvent("event", "item", 0, "hello", usage);

        String json = UnionTypeSerializationTestUtils.serialize(event);
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent result = UnionTypeSerializationTestUtils
            .deserialize(json, RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);

        assertTrue(json.contains("\"seconds\":7.5"));
        assertTrue(result.getUsage().isTranscriptTextUsageDuration());
        assertEquals(7.5, result.getUsage().asTranscriptTextUsageDuration().seconds());
        assertEquals(usage, result.getUsage());
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
    }

    @Test
    public void absentUsageReturnsNull() throws IOException {
        RealtimeConversationItemInputAudioTranscriptionCompletedEvent event
            = UnionTypeSerializationTestUtils.deserialize(
                "{\"type\":\"conversation.item.input_audio_transcription.completed\",\"event_id\":\"event\","
                    + "\"item_id\":\"item\",\"content_index\":0,\"transcript\":\"hello\"}",
                RealtimeConversationItemInputAudioTranscriptionCompletedEvent::fromJson);

        assertNull(event.getUsage());
        assertTrue(UnionTypeSerializationTestUtils.serialize(event).contains("\"usage\":null"));
    }

    @Test
    public void usageSurvivesServerEventDispatch() throws IOException {
        RealtimeServerEvent event = UnionTypeSerializationTestUtils.deserialize(
            "{\"type\":\"conversation.item.input_audio_transcription.completed\",\"event_id\":\"event\","
                + "\"item_id\":\"item\",\"content_index\":0,\"transcript\":\"hello\","
                + "\"usage\":{\"type\":\"duration\",\"seconds\":7.5}}",
            RealtimeServerEvent::fromJson);

        RealtimeConversationItemInputAudioTranscriptionCompletedEvent completed
            = (RealtimeConversationItemInputAudioTranscriptionCompletedEvent) event;
        assertEquals(7.5, completed.getUsage().asTranscriptTextUsageDuration().seconds());
    }
}
