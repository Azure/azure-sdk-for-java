// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.openai.models.ChatRole.ASSISTANT;
import static com.azure.ai.openai.models.ChatRole.DEVELOPER;
import static com.azure.ai.openai.models.ChatRole.FUNCTION;
import static com.azure.ai.openai.models.ChatRole.SYSTEM;
import static com.azure.ai.openai.models.ChatRole.TOOL;
import static com.azure.ai.openai.models.ChatRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        ChatCompletionsOptions chatCompletionsOptions
            = new ChatCompletionsOptions(Arrays.asList(chatRequestUserMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void testUserMessageContentSameStructureAfterConverted() {
        // content type: String
        final ChatRequestUserMessage messageInString = new ChatRequestUserMessage(content).setName(name);
        ChatRequestUserMessage stringConverted
            = BinaryData.fromObject(messageInString).toObject(ChatRequestUserMessage.class);
        assertEquals(content, stringConverted.getContent().toString());
        assertEquals(name, stringConverted.getName());
        assertEquals(USER, stringConverted.getRole());

        // content type: List
        final ChatRequestUserMessage messageInList
            = new ChatRequestUserMessage(Arrays.asList(new ChatMessageTextContentItem("textContent"),
                new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))));
        final ChatRequestUserMessage convertedMessageInList
            = BinaryData.fromObject(messageInList).toObject(ChatRequestUserMessage.class);

        final List<ChatMessageContentItem> convertedContentMessageInList
            = convertedMessageInList.getContent().toObject(new TypeReference<List<ChatMessageContentItem>>() {
            });
        assertEquals(2, convertedContentMessageInList.size());
        assertInstanceOf(ChatMessageTextContentItem.class, convertedContentMessageInList.get(0));
        ChatMessageContentItem chatMessageContentItem
            = (ChatMessageTextContentItem) convertedContentMessageInList.get(0);
        assertEquals("textContent", ((ChatMessageTextContentItem) chatMessageContentItem).getText());

        assertInstanceOf(ChatMessageImageContentItem.class, convertedContentMessageInList.get(1));
        ChatMessageImageContentItem imageContentItem
            = (ChatMessageImageContentItem) convertedContentMessageInList.get(1);
        assertEquals("testImage", imageContentItem.getImageUrl().getUrl());

        // content type: Array
        final ChatRequestUserMessage messageInArray = new ChatRequestUserMessage(new ChatMessageContentItem[] {
            new ChatMessageTextContentItem("textContent"),
            new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage")) });

        final ChatRequestUserMessage convertedMessageInArray
            = BinaryData.fromObject(messageInArray).toObject(ChatRequestUserMessage.class);

        final ChatMessageContentItem[] convertedContentMessageInArray
            = convertedMessageInArray.getContent().toObject(new TypeReference<List<ChatMessageContentItem>>() {
            }).toArray(new ChatMessageContentItem[0]);
        assertEquals(2, convertedContentMessageInArray.length);
        assertInstanceOf(ChatMessageTextContentItem.class, convertedContentMessageInArray[0]);
        ChatMessageContentItem chatMessageContentArrayItem
            = (ChatMessageTextContentItem) convertedContentMessageInArray[0];
        assertEquals("textContent", ((ChatMessageTextContentItem) chatMessageContentArrayItem).getText());

        assertInstanceOf(ChatMessageImageContentItem.class, convertedContentMessageInArray[1]);
        ChatMessageImageContentItem imageContentArrayItem
            = (ChatMessageImageContentItem) convertedContentMessageInArray[1];
        assertEquals("testImage", imageContentArrayItem.getImageUrl().getUrl());
    }

    @Test
    public void testChatRequestUserMessageContentInChatMessageContentItemList() {
        ChatRequestUserMessage userMessage = new ChatRequestUserMessage(new ChatMessageContentItem[] {
            new ChatMessageTextContentItem("textContent"),
            new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage")) });
        assertChatRequestUserMessage(userMessage);
        // Test it in ChatCompletionsOptions with ChatMessageContentItem
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(userMessage)));
    }

    @Test
    public void testChatRequestUserMessageContentInChatMessageContentItemArray() {
        ChatRequestUserMessage userMessage
            = new ChatRequestUserMessage(Arrays.asList(new ChatMessageTextContentItem("textContent"),
                new ChatMessageImageContentItem(new ChatMessageImageUrl("testImage"))));
        assertChatRequestUserMessage(userMessage);
        // Test it in ChatCompletionsOptions with ChatMessageContentItem
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(userMessage)));
    }

    @Test
    public void testChatRequestSystemMessage() {
        ChatRequestSystemMessage chatRequestSystemMessage = new ChatRequestSystemMessage(content).setName(name);
        assertEquals(content, chatRequestSystemMessage.getContent().toString());
        // Test it in ChatCompletionsOptions
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(chatRequestSystemMessage)));
    }

    @Test
    public void testChatRequestSystemMessageContentInChatMessageContentItemList() {
        ArrayList<ChatMessageContentItem> expectedContentItems = new ArrayList<>();
        expectedContentItems.add(new ChatMessageTextContentItem("textContent"));
        ChatRequestSystemMessage messageInChatMessageContentItems
            = new ChatRequestSystemMessage(expectedContentItems).setName(name);
        assertEquals(BinaryData.fromObject(expectedContentItems).toString(),
            messageInChatMessageContentItems.getContent().toString());
        // Test it in ChatCompletionsOptions with ChatMessageContentItem
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(messageInChatMessageContentItems)));
    }

    @Test
    public void testChatRequestAssistantMessage() {
        ChatRequestAssistantMessage chatRequestAssistantMessage
            = new ChatRequestAssistantMessage(content).setName(name);
        assertEquals(content, chatRequestAssistantMessage.getContent().toString());
        // Test it in ChatCompletionsOptions
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(chatRequestAssistantMessage)));
    }

    @Test
    public void testChatRequestAssistantMessageContentInChatMessageContentItemList() {
        ArrayList<ChatMessageContentItem> expectedContentItems = new ArrayList<>();
        expectedContentItems.add(new ChatMessageTextContentItem("textContent"));
        expectedContentItems.add(new ChatMessageRefusalContentItem("refusalContent"));
        ChatRequestAssistantMessage messageInChatMessageContentItems
            = new ChatRequestAssistantMessage(expectedContentItems).setName(name);
        assertEquals(BinaryData.fromObject(expectedContentItems).toString(),
            messageInChatMessageContentItems.getContent().toString());
        // Test it in ChatCompletionsOptions with ChatMessageContentItem
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(messageInChatMessageContentItems)));
    }

    @Test
    public void testChatRequestToolMessage() {
        ChatRequestToolMessage chatRequestToolMessage = new ChatRequestToolMessage(content, "tool_call_id_value");
        assertEquals(content, chatRequestToolMessage.getContent().toString());
        // Test it in ChatCompletionsOptions
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(chatRequestToolMessage)));
    }

    @Test
    public void testChatRequestToolMessageContentInChatMessageContentItemList() {
        ArrayList<ChatMessageContentItem> expectedContentItems = new ArrayList<>();
        expectedContentItems.add(new ChatMessageTextContentItem("textContent"));
        ChatRequestToolMessage messageInChatMessageContentItems
            = new ChatRequestToolMessage(expectedContentItems, "tool_call_id_value");
        assertEquals(BinaryData.fromObject(expectedContentItems).toString(),
            messageInChatMessageContentItems.getContent().toString());
        // Test it in ChatCompletionsOptions with ChatMessageContentItem
        assertChatCompletionsOptions(new ChatCompletionsOptions(Arrays.asList(messageInChatMessageContentItems)));
    }

    @Test
    public void testChatRequestFunctionMessageString() {
        ChatRequestFunctionMessage chatRequestFunctionMessage = new ChatRequestFunctionMessage(name, content);
        assertEquals(content, chatRequestFunctionMessage.getContent());

        // Test it in ChatCompletionsOptions
        ChatCompletionsOptions chatCompletionsOptions
            = new ChatCompletionsOptions(Arrays.asList(chatRequestFunctionMessage));
        assertChatCompletionsOptions(chatCompletionsOptions);
    }

    @Test
    public void chatRequestUserNullMessageJsonRoundTrip() {
        String userMessageJson = "{\"role\": \"user\",\"content\": null}";

        ChatRequestUserMessage userMessage
            = BinaryData.fromString(userMessageJson).toObject(ChatRequestUserMessage.class);

        // Deserialization
        assertEquals(USER, userMessage.getRole());
        assertNull(userMessage.getStringContent());
        assertNull(userMessage.getListContent());

        // Serialization
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        assertTrue(userMessageInString.contains("role"));
        assertTrue(userMessageInString.contains("user"));
        assertTrue(userMessageInString.contains("content"));
        assertTrue(userMessageInString.contains("null"));
    }

    @Test
    public void chatRequestUserStringMessageJsonRoundTrip() {
        String userMessageJson = "{\"role\": \"user\",\"content\": \"this is a test message.\"}";

        ChatRequestUserMessage userMessage
            = BinaryData.fromString(userMessageJson).toObject(ChatRequestUserMessage.class);

        // Deserialization
        assertEquals(USER, userMessage.getRole());
        assertEquals("this is a test message.", userMessage.getStringContent());
        assertEquals("this is a test message.", userMessage.getContent().toString());
        assertNull(userMessage.getListContent());

        // Serialization
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        assertTrue(userMessageInString.contains("role"));
        assertTrue(userMessageInString.contains("user"));
        assertTrue(userMessageInString.contains("content"));
        assertTrue(userMessageInString.contains("this is a test message."));

    }

    @Test
    public void chatRequestUserStructuredMessageJsonRoundTrip() {
        String userMessageJson
            = "{\"role\": \"user\",\"content\": [{\"type\": \"text\",\"text\": \"this is a test message.\"},{\n"
                + "\"type\": \"image_url\", \"image_url\": {\"url\": \"https://example.com/image.png\"}}]}";

        ChatRequestUserMessage userMessage
            = BinaryData.fromString(userMessageJson).toObject(ChatRequestUserMessage.class);

        // Deserialization
        assertEquals(USER, userMessage.getRole());
        assertNull(userMessage.getStringContent());
        List<ChatMessageContentItem> listContent = userMessage.getListContent();
        ChatMessageContentItem[] arrayContent = userMessage.getArrayContent();

        assertEquals(2, listContent.size());
        assertInstanceOf(ChatMessageTextContentItem.class, listContent.get(0));
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) listContent.get(0)).getText());
        assertInstanceOf(ChatMessageImageContentItem.class, listContent.get(1));
        assertEquals("https://example.com/image.png",
            ((ChatMessageImageContentItem) listContent.get(1)).getImageUrl().getUrl());

        assertNotNull(arrayContent);
        assertEquals(2, arrayContent.length);
        assertInstanceOf(ChatMessageTextContentItem.class, arrayContent[0]);
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) arrayContent[0]).getText());
        assertInstanceOf(ChatMessageImageContentItem.class, arrayContent[1]);
        assertEquals("https://example.com/image.png",
            ((ChatMessageImageContentItem) arrayContent[1]).getImageUrl().getUrl());

        // Serialization
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        assertTrue(userMessageInString.contains("role"));
        assertTrue(userMessageInString.contains("user"));
        assertTrue(userMessageInString.contains("content"));
        assertTrue(userMessageInString.contains("type"));
        assertTrue(userMessageInString.contains("text"));
        assertTrue(userMessageInString.contains("this is a test message."));
        assertTrue(userMessageInString.contains("image_url"));
        assertTrue(userMessageInString.contains("url"));
        assertTrue(userMessageInString.contains("https://example.com/image.png"));
    }

    @Test
    public void chatRequestToolNullMessageJsonRoundTrip() {
        String toolMessageJson
            = "{\n\"tool_call_id\": \"tool_call_id_value\"," + "\"role\": \"tool\",\"content\": null}";

        ChatRequestToolMessage toolMessage
            = BinaryData.fromString(toolMessageJson).toObject(ChatRequestToolMessage.class);

        // Deserialization
        assertEquals(TOOL, toolMessage.getRole());
        assertNull(toolMessage.getStringContent());
        assertNull(toolMessage.getContent());
        assertNull(toolMessage.getListContent());

        // Serialization
        String toolMessageInString = BinaryData.fromObject(toolMessage).toString();
        assertTrue(toolMessageInString.contains("role"));
        assertTrue(toolMessageInString.contains("tool"));
        assertTrue(toolMessageInString.contains("content"));
        assertTrue(toolMessageInString.contains("null"));

    }

    @Test
    public void chatRequestToolStringMessageJsonRoundTrip() {
        String toolMessageJson
            = "{\"tool_call_id\": \"tool_call_id_value\",\"role\": \"tool\",\"content\": \"this is a test message.\"}";

        ChatRequestToolMessage toolMessage
            = BinaryData.fromString(toolMessageJson).toObject(ChatRequestToolMessage.class);

        // Deserialization
        assertEquals(TOOL, toolMessage.getRole());
        assertEquals("this is a test message.", toolMessage.getStringContent());
        assertEquals("this is a test message.", toolMessage.getContent().toString());
        assertNull(toolMessage.getListContent());

        // Serialization
        String toolMessageInString = BinaryData.fromObject(toolMessage).toString();
        assertTrue(toolMessageInString.contains("role"));
        assertTrue(toolMessageInString.contains("tool"));
        assertTrue(toolMessageInString.contains("content"));
        assertTrue(toolMessageInString.contains("this is a test message."));

    }

    @Test
    public void chatRequestToolStructuredMessageJsonRoundTrip() {
        String toolMessageJson = "{\"tool_call_id\": \"tool_call_id_value\",\"role\": \"tool\",\n"
            + "\"content\": [{\"type\": \"text\",\"text\": \"this is a test message.\"},{\n"
            + "\"type\": \"image_url\",\"image_url\": {\"url\": \"https://example.com/image.png\"}}]}";

        ChatRequestToolMessage toolMessage
            = BinaryData.fromString(toolMessageJson).toObject(ChatRequestToolMessage.class);

        // Deserialization
        assertEquals(TOOL, toolMessage.getRole());
        assertNull(toolMessage.getStringContent());
        List<ChatMessageContentItem> listContent = toolMessage.getListContent();
        ChatMessageContentItem[] arrayContent = toolMessage.getArrayContent();

        assertEquals(2, listContent.size());
        assertInstanceOf(ChatMessageTextContentItem.class, listContent.get(0));
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) listContent.get(0)).getText());
        assertInstanceOf(ChatMessageImageContentItem.class, listContent.get(1));
        assertEquals("https://example.com/image.png",
            ((ChatMessageImageContentItem) listContent.get(1)).getImageUrl().getUrl());

        assertNotNull(arrayContent);
        assertEquals(2, arrayContent.length);
        assertInstanceOf(ChatMessageTextContentItem.class, arrayContent[0]);
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) arrayContent[0]).getText());
        assertInstanceOf(ChatMessageImageContentItem.class, arrayContent[1]);
        assertEquals("https://example.com/image.png",
            ((ChatMessageImageContentItem) arrayContent[1]).getImageUrl().getUrl());

        // Serialization
        String toolMessageInString = BinaryData.fromObject(toolMessage).toString();
        assertTrue(toolMessageInString.contains("role"));
        assertTrue(toolMessageInString.contains("tool"));
        assertTrue(toolMessageInString.contains("content"));
        assertTrue(toolMessageInString.contains("type"));
        assertTrue(toolMessageInString.contains("text"));
        assertTrue(toolMessageInString.contains("this is a test message."));
        assertTrue(toolMessageInString.contains("image_url"));
        assertTrue(toolMessageInString.contains("url"));
        assertTrue(toolMessageInString.contains("https://example.com/image.png"));
    }

    @Test
    public void chatRequestAssistantNullMessageJsonRoundTrip() {
        String assistantMessageJson = "{\"role\": \"assistant\",\"name\": \"Alice\",\"content\": null}";

        ChatRequestAssistantMessage assistantMessage
            = BinaryData.fromString(assistantMessageJson).toObject(ChatRequestAssistantMessage.class);

        // Deserialization
        assertEquals(ASSISTANT, assistantMessage.getRole());
        assertEquals("Alice", assistantMessage.getName());
        assertNull(assistantMessage.getStringContent());
        assertNull(assistantMessage.getContent());
        assertNull(assistantMessage.getListContent());

        // Serialization
        String assistantMessageInString = BinaryData.fromObject(assistantMessage).toString();
        assertTrue(assistantMessageInString.contains("role"));
        assertTrue(assistantMessageInString.contains("assistant"));
        assertTrue(assistantMessageInString.contains("content"));
        assertTrue(assistantMessageInString.contains("null"));
        assertTrue(assistantMessageInString.contains("name"));
        assertTrue(assistantMessageInString.contains("Alice"));
    }

    @Test
    public void chatRequestAssistantStringMessageJsonRoundTrip() {
        String assistantMessageJson
            = "{\"role\": \"assistant\",\"name\": \"Alice\",\"content\": \"this is a test message.\"}";

        ChatRequestAssistantMessage assistantMessage
            = BinaryData.fromString(assistantMessageJson).toObject(ChatRequestAssistantMessage.class);

        // Deserialization
        assertEquals(ASSISTANT, assistantMessage.getRole());
        assertEquals("this is a test message.", assistantMessage.getStringContent());
        assertEquals("this is a test message.", assistantMessage.getContent().toString());
        assertEquals("Alice", assistantMessage.getName());
        assertNull(assistantMessage.getListContent());

        // Serialization
        String assistantMessageInString = BinaryData.fromObject(assistantMessage).toString();
        assertTrue(assistantMessageInString.contains("role"));
        assertTrue(assistantMessageInString.contains("assistant"));
        assertTrue(assistantMessageInString.contains("content"));
        assertTrue(assistantMessageInString.contains("this is a test message."));
        assertTrue(assistantMessageInString.contains("name"));
        assertTrue(assistantMessageInString.contains("Alice"));
    }

    @Test
    public void chatRequestAssistantStructuredMessageJsonRoundTrip() {
        String assistantMessageJson = "{\"role\": \"assistant\",\"name\": \"Alice\",\"content\": [{\"type\": \"text\","
            + "\"text\": \"this is a test message.\"},{\"type\": \"refusal\",\"refusal\": \"refusal message\"}]}";

        ChatRequestAssistantMessage assistantMessage
            = BinaryData.fromString(assistantMessageJson).toObject(ChatRequestAssistantMessage.class);

        // Deserialization
        assertEquals(ASSISTANT, assistantMessage.getRole());
        assertNull(assistantMessage.getStringContent());
        List<ChatMessageContentItem> listContent = assistantMessage.getListContent();
        ChatMessageContentItem[] arrayContent = assistantMessage.getArrayContent();

        assertEquals(2, listContent.size());
        assertInstanceOf(ChatMessageTextContentItem.class, listContent.get(0));
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) listContent.get(0)).getText());
        assertInstanceOf(ChatMessageRefusalContentItem.class, listContent.get(1));
        assertEquals("refusal message", ((ChatMessageRefusalContentItem) listContent.get(1)).getRefusal());

        assertNotNull(arrayContent);
        assertEquals(2, arrayContent.length);
        assertInstanceOf(ChatMessageTextContentItem.class, arrayContent[0]);
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) arrayContent[0]).getText());
        assertInstanceOf(ChatMessageRefusalContentItem.class, arrayContent[1]);
        assertEquals("refusal message", ((ChatMessageRefusalContentItem) arrayContent[1]).getRefusal());

        // Serialization
        String assistantMessageInString = BinaryData.fromObject(assistantMessage).toString();
        assertTrue(assistantMessageInString.contains("role"));
        assertTrue(assistantMessageInString.contains("assistant"));
        assertTrue(assistantMessageInString.contains("content"));
        assertTrue(assistantMessageInString.contains("type"));
        assertTrue(assistantMessageInString.contains("text"));
        assertTrue(assistantMessageInString.contains("this is a test message."));
        assertTrue(assistantMessageInString.contains("refusal"));
        assertTrue(assistantMessageInString.contains("refusal message"));
    }

    @Test
    public void chatRequestDeveloperNullMessageJsonRoundTrip() {
        String developerMessageJson = "{\"role\": \"developer\",\"name\": \"Bob\",\"content\": null}";

        ChatRequestDeveloperMessage developerMessage
            = BinaryData.fromString(developerMessageJson).toObject(ChatRequestDeveloperMessage.class);

        // Deserialization
        assertEquals(DEVELOPER, developerMessage.getRole());
        assertEquals("Bob", developerMessage.getName());
        assertNull(developerMessage.getStringContent());
        assertNull(developerMessage.getContent());
        assertNull(developerMessage.getListContent());

        // Serialization
        String developerMessageInString = BinaryData.fromObject(developerMessage).toString();
        assertTrue(developerMessageInString.contains("role"));
        assertTrue(developerMessageInString.contains("developer"));
        assertTrue(developerMessageInString.contains("content"));
        assertTrue(developerMessageInString.contains("null"));
        assertTrue(developerMessageInString.contains("name"));
        assertTrue(developerMessageInString.contains("Bob"));
    }

    @Test
    public void chatRequestDeveloperStringMessageJsonRoundTrip() {
        String developerMessageJson
            = "{\"role\": \"developer\",\"name\": \"Bob\",\"content\": \"this is a test message.\"}";

        ChatRequestDeveloperMessage developerMessage
            = BinaryData.fromString(developerMessageJson).toObject(ChatRequestDeveloperMessage.class);

        // Deserialization
        assertEquals(DEVELOPER, developerMessage.getRole());
        assertEquals("this is a test message.", developerMessage.getStringContent());
        assertEquals("this is a test message.", developerMessage.getContent().toString());
        assertEquals("Bob", developerMessage.getName());
        assertNull(developerMessage.getListContent());

        // Serialization
        String developerMessageInString = BinaryData.fromObject(developerMessage).toString();
        assertTrue(developerMessageInString.contains("role"));
        assertTrue(developerMessageInString.contains("developer"));
        assertTrue(developerMessageInString.contains("content"));
        assertTrue(developerMessageInString.contains("this is a test message."));
        assertTrue(developerMessageInString.contains("name"));
        assertTrue(developerMessageInString.contains("Bob"));
    }

    @Test
    public void chatRequestDeveloperStructuredMessageJsonRoundTrip() {
        String developerMessageJson = "{\"role\": \"developer\",\"name\": \"Bob\",\"content\": [{\"type\": \"text\","
            + "\"text\": \"this is a test message.\" }]}";

        ChatRequestAssistantMessage developerMessage
            = BinaryData.fromString(developerMessageJson).toObject(ChatRequestAssistantMessage.class);

        // Deserialization
        assertEquals(DEVELOPER, developerMessage.getRole());
        assertEquals("Bob", developerMessage.getName());
        assertNull(developerMessage.getStringContent());
        List<ChatMessageContentItem> listContent = developerMessage.getListContent();
        ChatMessageContentItem[] arrayContent = developerMessage.getArrayContent();

        assertEquals(1, listContent.size());
        assertInstanceOf(ChatMessageTextContentItem.class, listContent.get(0));
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) listContent.get(0)).getText());

        assertNotNull(arrayContent);
        assertEquals(1, arrayContent.length);
        assertInstanceOf(ChatMessageTextContentItem.class, arrayContent[0]);
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) arrayContent[0]).getText());

        // Serialization
        String developerMessageInString = BinaryData.fromObject(developerMessage).toString();
        assertTrue(developerMessageInString.contains("role"));
        assertTrue(developerMessageInString.contains("developer"));
        assertTrue(developerMessageInString.contains("content"));
        assertTrue(developerMessageInString.contains("type"));
        assertTrue(developerMessageInString.contains("text"));
        assertTrue(developerMessageInString.contains("this is a test message."));
    }

    @Test
    public void chatRequestFunctionNullMessageJsonRoundTrip() {
        String functionMessageJson = "{\"role\": \"function\",\"content\": null}";

        ChatRequestFunctionMessage functionMessage
            = BinaryData.fromString(functionMessageJson).toObject(ChatRequestFunctionMessage.class);

        // Deserialization
        assertEquals(FUNCTION, functionMessage.getRole());
        assertNull(functionMessage.getContent());

        // Serialization
        String functionMessageInString = BinaryData.fromObject(functionMessage).toString();
        assertTrue(functionMessageInString.contains("role"));
        assertTrue(functionMessageInString.contains("function"));
    }

    @Test
    public void chatRequestFunctionStringMessageJsonRoundTrip() {
        String functionMessageJson = " {\"role\": \"function\",\"content\": \"this is a test message.\"}";

        ChatRequestFunctionMessage functionMessage
            = BinaryData.fromString(functionMessageJson).toObject(ChatRequestFunctionMessage.class);

        // Deserialization
        assertEquals(FUNCTION, functionMessage.getRole());
        assertEquals("this is a test message.", functionMessage.getContent());

        // Serialization
        String functionMessageInString = BinaryData.fromObject(functionMessage).toString();
        assertTrue(functionMessageInString.contains("role"));
        assertTrue(functionMessageInString.contains("function"));
        assertTrue(functionMessageInString.contains("content"));
        assertTrue(functionMessageInString.contains("this is a test message."));

    }

    @Test
    public void chatRequestSystemNullMessageJsonRoundTrip() {
        String systemMessageJson = "{\"role\": \"system\",\"name\": \"Carlos\",\"content\": null}";

        ChatRequestSystemMessage systemMessage
            = BinaryData.fromString(systemMessageJson).toObject(ChatRequestSystemMessage.class);

        // Deserialization
        assertEquals(SYSTEM, systemMessage.getRole());
        assertEquals("Carlos", systemMessage.getName());
        assertNull(systemMessage.getStringContent());
        assertNull(systemMessage.getContent());
        assertNull(systemMessage.getListContent());

        // Serialization
        String systemMessageInString = BinaryData.fromObject(systemMessage).toString();
        assertTrue(systemMessageInString.contains("role"));
        assertTrue(systemMessageInString.contains("system"));
        assertTrue(systemMessageInString.contains("content"));
        assertTrue(systemMessageInString.contains("null"));
        assertTrue(systemMessageInString.contains("name"));
        assertTrue(systemMessageInString.contains("Carlos"));
    }

    @Test
    public void chatRequestSystemStringMessageJsonRoundTrip() {
        String systemMessageJson
            = "{\"role\": \"system\",\"name\": \"Carlos\",\"content\": \"this is a test message.\"}";

        ChatRequestSystemMessage systemMessage
            = BinaryData.fromString(systemMessageJson).toObject(ChatRequestSystemMessage.class);

        // Deserialization
        assertEquals(SYSTEM, systemMessage.getRole());
        assertEquals("this is a test message.", systemMessage.getStringContent());
        assertEquals("this is a test message.", systemMessage.getContent().toString());
        assertEquals("Carlos", systemMessage.getName());
        assertNull(systemMessage.getListContent());

        // Serialization
        String systemMessageInString = BinaryData.fromObject(systemMessage).toString();
        assertTrue(systemMessageInString.contains("role"));
        assertTrue(systemMessageInString.contains("system"));
        assertTrue(systemMessageInString.contains("content"));
        assertTrue(systemMessageInString.contains("this is a test message."));
        assertTrue(systemMessageInString.contains("name"));
        assertTrue(systemMessageInString.contains("Carlos"));
    }

    @Test
    public void chatRequestSystemStructuredMessageJsonRoundTrip() {
        String systemMessageJson = "{\"role\": \"system\",\"name\": \"Carlos\",\"content\": [{\"type\": \"text\","
            + "\"text\": \"this is a test message.\"}]}";

        ChatRequestSystemMessage systemMessage
            = BinaryData.fromString(systemMessageJson).toObject(ChatRequestSystemMessage.class);

        // Deserialization
        assertEquals(SYSTEM, systemMessage.getRole());
        assertEquals("Carlos", systemMessage.getName());
        assertNull(systemMessage.getStringContent());
        List<ChatMessageContentItem> listContent = systemMessage.getListContent();
        ChatMessageContentItem[] arrayContent = systemMessage.getArrayContent();

        assertEquals(1, listContent.size());
        assertInstanceOf(ChatMessageTextContentItem.class, listContent.get(0));
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) listContent.get(0)).getText());

        assertNotNull(arrayContent);
        assertEquals(1, arrayContent.length);
        assertInstanceOf(ChatMessageTextContentItem.class, arrayContent[0]);
        assertEquals("this is a test message.", ((ChatMessageTextContentItem) arrayContent[0]).getText());

        // Serialization
        String systemMessageInString = BinaryData.fromObject(systemMessage).toString();
        assertTrue(systemMessageInString.contains("role"));
        assertTrue(systemMessageInString.contains("system"));
        assertTrue(systemMessageInString.contains("content"));
        assertTrue(systemMessageInString.contains("type"));
        assertTrue(systemMessageInString.contains("text"));
        assertTrue(systemMessageInString.contains("this is a test message."));
        assertTrue(systemMessageInString.contains("name"));
        assertTrue(systemMessageInString.contains("Carlos"));
    }

    private void assertChatRequestUserMessage(ChatRequestUserMessage userMessage) {
        String userMessageInString = BinaryData.fromObject(userMessage).toString();
        ChatRequestUserMessage converted
            = BinaryData.fromString(userMessageInString).toObject(ChatRequestUserMessage.class);
        assertEquals(userMessage.getContent().toString(), converted.getContent().toString());
        assertEquals(userMessage.getName(), converted.getName());
        assertEquals(USER, converted.getRole());
    }

    private void assertChatCompletionsOptions(ChatCompletionsOptions chatCompletionsOptions) {
        String chatCompletionsOptionsInString = BinaryData.fromObject(chatCompletionsOptions).toString();
        ChatCompletionsOptions converted
            = BinaryData.fromString(chatCompletionsOptionsInString).toObject(ChatCompletionsOptions.class);

        converted.getMessages().forEach(message -> {
            if (message instanceof ChatRequestUserMessage) {
                ChatRequestUserMessage userMessage = (ChatRequestUserMessage) message;
                ChatRequestUserMessage expectedUserMessage
                    = (ChatRequestUserMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedUserMessage.getContent().toString(), userMessage.getContent().toString());
            } else if (message instanceof ChatRequestSystemMessage) {
                ChatRequestSystemMessage systemMessage = (ChatRequestSystemMessage) message;
                ChatRequestSystemMessage expectedSystemMessage
                    = (ChatRequestSystemMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedSystemMessage.getContent().toString(), systemMessage.getContent().toString());
            } else if (message instanceof ChatRequestAssistantMessage) {
                ChatRequestAssistantMessage assistantMessage = (ChatRequestAssistantMessage) message;
                ChatRequestAssistantMessage expectedAssistantMessage
                    = (ChatRequestAssistantMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedAssistantMessage.getContent().toString(),
                    assistantMessage.getContent().toString());
            } else if (message instanceof ChatRequestToolMessage) {
                ChatRequestToolMessage toolMessage = (ChatRequestToolMessage) message;
                ChatRequestToolMessage expectedToolMessage
                    = (ChatRequestToolMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedToolMessage.getContent().toString(), toolMessage.getContent().toString());
            } else if (message instanceof ChatRequestFunctionMessage) {
                ChatRequestFunctionMessage functionMessage = (ChatRequestFunctionMessage) message;
                ChatRequestFunctionMessage expectedFunctionMessage
                    = (ChatRequestFunctionMessage) chatCompletionsOptions.getMessages().get(0);
                assertEquals(expectedFunctionMessage.getContent(), functionMessage.getContent());
            } else {
                assertFalse(true, "Unexpected message type");
            }
        });
    }
}
