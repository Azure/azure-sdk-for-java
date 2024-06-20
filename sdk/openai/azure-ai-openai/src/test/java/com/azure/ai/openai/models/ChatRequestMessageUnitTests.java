// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.azure.ai.openai.models.ChatRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ChatRequestMessage.
 */
public class ChatRequestMessageUnitTests {
    String content = "You are a helpful assistant.";
    String name = "Alice";
    @Test
    public void testChatRequestUserMessageContentInString() {
        // String type content
        ChatRequestUserMessage chatRequestUserMessage = new ChatRequestUserMessage(content).setName(name);
        assertEquals(content, chatRequestUserMessage.getContent().toString());
        assertEquals(name, chatRequestUserMessage.getName());
        assertEquals(USER, chatRequestUserMessage.getRole());
    }

    @Test
    public void testChatRequestUserMessageContentInChatMessageContentItemList() {
        ChatMessageContentItem[] content = new ChatMessageContentItem[] {
            new ChatMessageTextContentItem("textContent"),
            new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        };
        ChatRequestUserMessage userMessage = new ChatRequestUserMessage(content);
        assertChatRequestUserMessage(userMessage);
    }

    @Test
    public void testChatRequestUserMessageContentInChatMessageContentItemArray() {
        ChatRequestUserMessage userMessage = new ChatRequestUserMessage(Arrays.asList(
                new ChatMessageTextContentItem("textContent"),
                new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        ));
        assertChatRequestUserMessage(userMessage);
    }

    @Test
    public void testChatRequestSystemMessage() {
        ChatRequestSystemMessage chatRequestSystemMessage = new ChatRequestSystemMessage(content).setName(name);
        assertEquals(content, chatRequestSystemMessage.getContent());
    }

    @Test
    public void testChatRequestAssistantMessage() {
        ChatRequestAssistantMessage chatRequestAssistantMessage = new ChatRequestAssistantMessage(content).setName(name);
        assertEquals(content, chatRequestAssistantMessage.getContent());
    }

    @Test
    public void testChatRequestToolMessage() {
        ChatRequestToolMessage chatRequestToolMessage = new ChatRequestToolMessage(content, "tool_call_id_value");
        assertEquals(content, chatRequestToolMessage.getContent());
    }

    @Test
    public void testChatRequestFunctionMessage() {
        ChatRequestFunctionMessage chatRequestFunctionMessage = new ChatRequestFunctionMessage(name, content);
        assertEquals(content, chatRequestFunctionMessage.getContent());
    }

    private void assertChatRequestUserMessage(ChatRequestUserMessage userMessage) {
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        assertEquals(
                "{\"content\":[{\"text\":\"textContent\",\"type\":\"text\"},{\"image_url\":{\"url\":\"testImage\"},\"type\":\"image_url\"}],\"role\":\"user\"}",
                userMessageInString);
        ChatRequestUserMessage converted = BinaryData.fromString(userMessageInString)
                .toObject(ChatRequestUserMessage.class);
        assertEquals(userMessage.getContent().toString(), converted.getContent().toString());
        assertEquals(userMessage.getName(), converted.getName());
        assertEquals(USER, converted.getRole());
    }
}
