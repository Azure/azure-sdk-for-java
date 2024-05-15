// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ChatRequestMessage.
 */
public class ChatRequestMessageUnitTests {
    String content = "You are a helpful assistant.";
    String name = "Alice";
    @Test
    public void testChatRequestUserMessage() {
        ChatRequestUserMessage chatRequestUserMessage = new ChatRequestUserMessage(content).setName(name);

        assertEquals(content, chatRequestUserMessage.getContent().toString());
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
}
