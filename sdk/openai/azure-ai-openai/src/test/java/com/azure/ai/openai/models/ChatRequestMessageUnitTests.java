// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for ChatRequestMessage.
 */
public class ChatRequestMessageUnitTests {
    String content = "You are a helpful assistant.";
    String name = "Alice";
    @Test
    public void testChatRequestUserMessage() {
        // String type content
        ChatRequestUserMessage chatRequestUserMessage = new ChatRequestUserMessage(content).setName(name);
        assertEquals(content, chatRequestUserMessage.getContent().toString());


        // List<ChatMessageContentItem> type content
        final ChatRequestUserMessage user = new ChatRequestUserMessage(List.of(
                new ChatMessageTextContentItem("text"),
                new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        ));
        final String content = BinaryData.fromObject(user).toString();
        System.out.println(content);
        final ChatRequestUserMessage converted = BinaryData.fromString(content).toObject(ChatRequestUserMessage.class);
        System.out.println(converted);
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
