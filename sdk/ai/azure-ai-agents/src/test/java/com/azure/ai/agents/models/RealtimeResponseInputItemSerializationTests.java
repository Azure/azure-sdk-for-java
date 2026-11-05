// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.openai.models.realtime.RealtimeConversationItemUserMessage.Content.Detail;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeResponseInputItemSerializationTests {
    @Test
    public void heterogeneousResponseInputRoundTrips() throws IOException {
        List<RealtimeResponseInputContent> content = Arrays.asList(
            RealtimeResponseInputContent.fromText(new RealtimeResponseInputText("hello")),
            RealtimeResponseInputContent.fromAudio(new RealtimeResponseInputAudio("YXVkaW8=").setTranscript("audio")),
            RealtimeResponseInputContent
                .fromImage(new RealtimeResponseInputImage("data:image/png;base64,aW1hZ2U=").setDetail(Detail.HIGH)));
        List<RealtimeResponseInputItem> input = Arrays.asList(RealtimeResponseInputItem.fromMessage(
            new RealtimeResponseInputMessageSystem(Collections.singletonList(new RealtimeResponseInputText("system")))
                .setStatus(RealtimeResponseInputItemStatus.COMPLETED)),
            RealtimeResponseInputItem.fromMessage(new RealtimeResponseInputMessageDeveloper(
                Collections.singletonList(new RealtimeResponseInputText("developer")))
                    .setStatus(RealtimeResponseInputItemStatus.INCOMPLETE)),
            RealtimeResponseInputItem.fromMessage(
                new RealtimeResponseInputMessageUser(content).setStatus(RealtimeResponseInputItemStatus.COMPLETED)),
            RealtimeResponseInputItem.fromMessage(new RealtimeResponseInputMessageAssistant(
                Collections.singletonList(new RealtimeResponseInputMessageAssistantContent("assistant")))
                    .setStatus(RealtimeResponseInputItemStatus.INCOMPLETE)),
            RealtimeResponseInputItem.fromFunctionCall(new RealtimeResponseInputFunctionCall("call_1", "lookup", "{}")
                .setStatus(RealtimeResponseInputItemStatus.COMPLETED)),
            RealtimeResponseInputItem
                .fromFunctionCallOutput(new RealtimeResponseInputFunctionCallOutput("call_1", "done")
                    .setStatus(RealtimeResponseInputItemStatus.INCOMPLETE)),
            RealtimeResponseInputItem.fromItemReference(new RealtimeResponseItemReference("item_1")));

        String json = UnionTypeSerializationTestUtils.serialize(new VoiceAgentResponseCreateOptions().setInput(input));
        VoiceAgentResponseCreateOptions result
            = UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentResponseCreateOptions::fromJson);
        assertEquals(json, UnionTypeSerializationTestUtils.serialize(result));
        assertEquals(7, result.getInput().size());

        RealtimeResponseInputMessageSystem system
            = assertInstanceOf(RealtimeResponseInputMessageSystem.class, result.getInput().get(0).getMessage());
        assertEquals("system", system.getContent().get(0).getText());
        assertEquals(RealtimeResponseInputItemStatus.COMPLETED, system.getStatus());
        RealtimeResponseInputMessageDeveloper developer
            = assertInstanceOf(RealtimeResponseInputMessageDeveloper.class, result.getInput().get(1).getMessage());
        assertEquals("developer", developer.getContent().get(0).getText());
        assertEquals(RealtimeResponseInputItemStatus.INCOMPLETE, developer.getStatus());
        RealtimeResponseInputMessageUser user
            = assertInstanceOf(RealtimeResponseInputMessageUser.class, result.getInput().get(2).getMessage());
        assertEquals(RealtimeResponseInputItemStatus.COMPLETED, user.getStatus());
        assertEquals("hello", user.getContent().get(0).getText().getText());
        assertEquals("YXVkaW8=", user.getContent().get(1).getAudio().getAudio());
        assertEquals("audio", user.getContent().get(1).getAudio().getTranscript());
        assertEquals("data:image/png;base64,aW1hZ2U=", user.getContent().get(2).getImage().getImageUrl());
        assertEquals(Detail.HIGH, user.getContent().get(2).getImage().getDetail());
        RealtimeResponseInputMessageAssistant assistant
            = assertInstanceOf(RealtimeResponseInputMessageAssistant.class, result.getInput().get(3).getMessage());
        assertEquals("assistant", assistant.getContent().get(0).getText());
        assertEquals(RealtimeResponseInputItemStatus.INCOMPLETE, assistant.getStatus());
        assertEquals("lookup", result.getInput().get(4).getFunctionCall().getName());
        assertEquals("{}", result.getInput().get(4).getFunctionCall().getArguments());
        assertEquals(RealtimeResponseInputItemStatus.COMPLETED, result.getInput().get(4).getFunctionCall().getStatus());
        assertEquals("done", result.getInput().get(5).getFunctionCallOutput().getOutput());
        assertEquals(RealtimeResponseInputItemStatus.INCOMPLETE,
            result.getInput().get(5).getFunctionCallOutput().getStatus());
        assertEquals("item_1", result.getInput().get(6).getItemReference().getId());
        assertTrue(json.contains("\"type\":\"message\""));
        assertTrue(json.contains("\"type\":\"input_text\""));
        assertTrue(json.contains("\"type\":\"input_audio\""));
        assertTrue(json.contains("\"type\":\"input_image\""));
        assertTrue(json.contains("\"type\":\"output_text\""));
        assertTrue(json.contains("\"type\":\"function_call\""));
        assertTrue(json.contains("\"type\":\"function_call_output\""));
        assertTrue(json.contains("\"type\":\"item_reference\""));
    }

    @Test
    public void factoriesExposeOnlyTheirVariant() {
        RealtimeResponseInputMessage message = new RealtimeResponseInputMessageUser(
            Collections.singletonList(RealtimeResponseInputContent.fromText(new RealtimeResponseInputText("text"))));
        RealtimeResponseInputItem messageItem = RealtimeResponseInputItem.fromMessage(message);
        assertSame(message, messageItem.getMessage());
        assertNull(messageItem.getFunctionCall());
        assertNull(messageItem.getFunctionCallOutput());
        assertNull(messageItem.getItemReference());

        RealtimeResponseInputFunctionCall call = new RealtimeResponseInputFunctionCall("call_1", "lookup", "{}");
        RealtimeResponseInputItem callItem = RealtimeResponseInputItem.fromFunctionCall(call);
        assertSame(call, callItem.getFunctionCall());
        assertNull(callItem.getMessage());
        assertNull(callItem.getFunctionCallOutput());
        assertNull(callItem.getItemReference());

        RealtimeResponseInputFunctionCallOutput output = new RealtimeResponseInputFunctionCallOutput("call_1", "done");
        RealtimeResponseInputItem outputItem = RealtimeResponseInputItem.fromFunctionCallOutput(output);
        assertSame(output, outputItem.getFunctionCallOutput());
        assertNull(outputItem.getMessage());
        assertNull(outputItem.getFunctionCall());
        assertNull(outputItem.getItemReference());

        RealtimeResponseItemReference reference = new RealtimeResponseItemReference("item_1");
        RealtimeResponseInputItem referenceItem = RealtimeResponseInputItem.fromItemReference(reference);
        assertSame(reference, referenceItem.getItemReference());
        assertNull(referenceItem.getMessage());
        assertNull(referenceItem.getFunctionCall());
        assertNull(referenceItem.getFunctionCallOutput());
    }

    @Test
    public void readsDiscriminatorsAfterOtherFields() throws IOException {
        RealtimeResponseInputItem item = UnionTypeSerializationTestUtils.deserialize(
            "{\"content\":[{\"text\":\"hello\",\"type\":\"input_text\"}],\"role\":\"user\",\"type\":\"message\"}",
            RealtimeResponseInputItem::fromJson);
        RealtimeResponseInputMessageUser message
            = assertInstanceOf(RealtimeResponseInputMessageUser.class, item.getMessage());
        assertEquals("hello", message.getContent().get(0).getText().getText());

    }

    @Test
    public void absentNullAndEmptyInputArePreserved() throws IOException {
        assertNull(
            UnionTypeSerializationTestUtils.deserialize("{}", VoiceAgentResponseCreateOptions::fromJson).getInput());
        assertNull(
            UnionTypeSerializationTestUtils.deserialize("{\"input\":null}", VoiceAgentResponseCreateOptions::fromJson)
                .getInput());
        VoiceAgentResponseCreateOptions options
            = new VoiceAgentResponseCreateOptions().setInput(Collections.emptyList());
        String json = UnionTypeSerializationTestUtils.serialize(options);
        assertTrue(json.contains("\"input\":[]"));
        assertTrue(UnionTypeSerializationTestUtils.deserialize(json, VoiceAgentResponseCreateOptions::fromJson)
            .getInput()
            .isEmpty());
        assertNull(UnionTypeSerializationTestUtils.deserialize("null", RealtimeResponseInputItem::fromJson));
    }

    @Test
    public void invalidVariantsFailExplicitly() {
        for (String json : Arrays.asList("{}", "{\"type\":\"unsupported\"}")) {
            assertThrows(IllegalStateException.class,
                () -> UnionTypeSerializationTestUtils.deserialize(json, RealtimeResponseInputItem::fromJson));
        }
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputItem.fromMessage(null));
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputItem.fromFunctionCall(null));
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputItem.fromFunctionCallOutput(null));
        assertThrows(NullPointerException.class, () -> RealtimeResponseInputItem.fromItemReference(null));
    }

    @Test
    public void messageBaseConstructorIsProtected() throws NoSuchMethodException {
        assertTrue(Modifier.isProtected(RealtimeResponseInputMessage.class.getDeclaredConstructor().getModifiers()));
    }
}
