// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.azure.ai.openai.models.ChatRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(Arrays.asList(chatRequestUserMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void testUserMessageContentSameStructureAfterConverted() {
        // content type: String
        final ChatRequestUserMessage messageInString = new ChatRequestUserMessage(content).setName(name);
        ChatRequestUserMessage stringConverted = BinaryData.fromObject(messageInString).toObject(ChatRequestUserMessage.class);
        assertEquals(content, stringConverted.getContent().toString());
        assertEquals(name, stringConverted.getName());
        assertEquals(USER, stringConverted.getRole());

        // content type: List
        final ChatRequestUserMessage messageInList = new ChatRequestUserMessage(Arrays.asList(
                new ChatMessageTextContentItem("textContent"),
                new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        ));
        final ChatRequestUserMessage convertedMessageInList = BinaryData.fromObject(messageInList)
                .toObject(ChatRequestUserMessage.class);

        // TODO (shawn): Uncomment the following lines after azure-core 1.50.0 is released
//        final List<ChatMessageContentItem> convertedContentMessageInList = convertedMessageInList
//                .getContent()
//                .toObject(new TypeReference<List<ChatMessageContentItem>>() {});
//        assertEquals(2, convertedContentMessageInList.size());
//        assertTrue(convertedContentMessageInList.get(0) instanceof ChatMessageTextContentItem);
//        ChatMessageContentItem chatMessageContentItem = (ChatMessageTextContentItem) convertedContentMessageInList.get(0);
//        assertEquals("textContent", ((ChatMessageTextContentItem) chatMessageContentItem).getText());
//
//        assertTrue(convertedContentMessageInList.get(1) instanceof ChatMessageImageContentItem);
//        ChatMessageImageContentItem imageContentItem = (ChatMessageImageContentItem) convertedContentMessageInList.get(1);
//        assertEquals("testImage", imageContentItem.getImageUrl().getUrl());

        // content type: Array
        final ChatRequestUserMessage messageInArray = new ChatRequestUserMessage(new ChatMessageContentItem[]{
            new ChatMessageTextContentItem("textContent"),
            new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        });
        // TODO (shawn): Currently the conversion from Object to Array doesn't work, so we show how to get the Array

        // TODO (shawn): Uncomment the following lines after azure-core 1.50.0 is released
//        //  from the List as a temporary way to get Array of items
//        final ChatMessageContentItem[] convertedContentMessageInArray = convertedMessageInList.getContent()
//                .toObject(new TypeReference<List<ChatMessageContentItem>>() {})
//                .toArray(new ChatMessageContentItem[0]);
//        assertEquals(2, convertedContentMessageInArray.length);
//        assertTrue(convertedContentMessageInArray[0] instanceof ChatMessageTextContentItem);
//        ChatMessageContentItem chatMessageContentArrayItem = (ChatMessageTextContentItem) convertedContentMessageInArray[0];
//        assertEquals("textContent", ((ChatMessageTextContentItem) chatMessageContentArrayItem).getText());
//
//        assertTrue(convertedContentMessageInArray[1] instanceof ChatMessageImageContentItem);
//        ChatMessageImageContentItem imageContentArrayItem = (ChatMessageImageContentItem) convertedContentMessageInArray[1];
//        assertEquals("testImage", imageContentArrayItem.getImageUrl().getUrl());
    }

    @Test
    public void testChatRequestUserMessageContentInChatMessageContentItemList() {
        ChatRequestUserMessage userMessage = new ChatRequestUserMessage(new ChatMessageContentItem[] {
            new ChatMessageTextContentItem("textContent"),
            new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))
        });
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

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(Arrays.asList(chatRequestSystemMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void testChatRequestAssistantMessage() {
        ChatRequestAssistantMessage chatRequestAssistantMessage = new ChatRequestAssistantMessage(content).setName(name);
        assertEquals(content, chatRequestAssistantMessage.getContent());

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(Arrays.asList(chatRequestAssistantMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void testChatRequestToolMessage() {
        ChatRequestToolMessage chatRequestToolMessage = new ChatRequestToolMessage(content, "tool_call_id_value");
        assertEquals(content, chatRequestToolMessage.getContent());

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(Arrays.asList(chatRequestToolMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void testChatRequestFunctionMessage() {
        ChatRequestFunctionMessage chatRequestFunctionMessage = new ChatRequestFunctionMessage(name, content);
        assertEquals(content, chatRequestFunctionMessage.getContent());

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(Arrays.asList(chatRequestFunctionMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    private void assertChatRequestUserMessage(ChatRequestUserMessage userMessage) {
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        ChatRequestUserMessage converted = BinaryData.fromString(userMessageInString)
                .toObject(ChatRequestUserMessage.class);
        // TODO (shawn): Uncomment the following lines after azure-core 1.50.0 is released
        // assertEquals(userMessage.getContent().toString(), converted.getContent().toString());
        assertEquals(userMessage.getName(), converted.getName());
        assertEquals(USER, converted.getRole());
    }

    private void assertChatCompletionsOptions(ChatCompletionsOptions chatCompletionsOptions) {
        String chatCompletionsOptionsInString = BinaryData.fromObject(chatCompletionsOptions).toString();
        ChatCompletionsOptions converted = BinaryData.fromString(chatCompletionsOptionsInString)
                .toObject(ChatCompletionsOptions.class);

        converted.getMessages().forEach(message -> {
            if (message instanceof ChatRequestUserMessage) {
                ChatRequestUserMessage userMessage = (ChatRequestUserMessage) message;
                ChatRequestUserMessage expectedUserMessage = (ChatRequestUserMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedUserMessage.getContent().toString(), userMessage.getContent().toString());
            } else if (message instanceof ChatRequestSystemMessage) {
                ChatRequestSystemMessage systemMessage = (ChatRequestSystemMessage) message;
                ChatRequestSystemMessage expectedSystemMessage = (ChatRequestSystemMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedSystemMessage.getContent(), systemMessage.getContent());
            } else if (message instanceof ChatRequestAssistantMessage) {
                ChatRequestAssistantMessage assistantMessage = (ChatRequestAssistantMessage) message;
                ChatRequestAssistantMessage expectedAssistantMessage = (ChatRequestAssistantMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedAssistantMessage.getContent(), assistantMessage.getContent());
            } else if (message instanceof ChatRequestToolMessage) {
                ChatRequestToolMessage toolMessage = (ChatRequestToolMessage) message;
                ChatRequestToolMessage expectedToolMessage = (ChatRequestToolMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedToolMessage.getContent(), toolMessage.getContent());
            } else if (message instanceof ChatRequestFunctionMessage) {
                ChatRequestFunctionMessage functionMessage = (ChatRequestFunctionMessage) message;
                ChatRequestFunctionMessage expectedFunctionMessage = (ChatRequestFunctionMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedFunctionMessage.getContent(), functionMessage.getContent());
            } else {
                assertFalse(true, "Unexpected message type");
            }
        });
    }
}
