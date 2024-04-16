// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * Unit tests for ChatCompletionsToolCall.
 */
public class ChatCompletionsToolCallUnitTests {

    @Test
    public void testSerialization() {
        ChatCompletionsToolCall functionToolCall =
                new ChatCompletionsFunctionToolCall("id", new FunctionCall("name", "arguments"));

        ChatCompletionsOptions options = new ChatCompletionsOptions(Collections.singletonList(
                new ChatRequestAssistantMessage("content").setToolCalls(Collections.singletonList(functionToolCall))));

        String json = BinaryData.fromObject(options).toString();
        // Ensure property `type` is serialized and included.
        Assertions.assertTrue(json.contains("\"type\":\"function\""));
    }

    @Test
    public void testDeserialization() {
        String jsonWithoutType = "{\"messages\":[{\"role\":\"assistant\",\"content\":\"content\",\"tool_calls\":[{\"id\":\"id\",\"function\":{\"name\":\"name\",\"arguments\":\"arguments\"}}]}]}";

        ChatCompletionsOptions options =
                BinaryData.fromString(jsonWithoutType).toObject(ChatCompletionsOptions.class);

        Assertions.assertInstanceOf(ChatRequestAssistantMessage.class, options.getMessages().get(0));
        ChatRequestAssistantMessage assistantMessage = (ChatRequestAssistantMessage) options.getMessages().get(0);
        Assertions.assertInstanceOf(ChatCompletionsFunctionToolCall.class, assistantMessage.getToolCalls().get(0));
        Assertions.assertEquals("function", assistantMessage.getToolCalls().get(0).getType());
    }
}
