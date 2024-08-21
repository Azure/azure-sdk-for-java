package com.azure.ai.openai;

import com.azure.ai.openai.models.ChatCompletionsFunctionToolSelection;
import com.azure.ai.openai.models.ChatCompletionsNamedFunctionToolSelection;
import com.azure.ai.openai.models.ChatCompletionsNamedToolSelection;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatCompletionsToolSelection;
import com.azure.ai.openai.models.ChatCompletionsToolSelectionPreset;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChatCompletionsToolSelectionSerializerTests {
    @Test
    public void chatCompletionsOptionToolChoiceAuto() {
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(new ArrayList<>());
        chatCompletionsOptions.setToolChoice(new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.AUTO));
        ChatCompletionsToolSelectionPreset preset = chatCompletionsOptions.getToolChoice().getPreset();

        BinaryData jsonBinaryData = BinaryData.fromObject(chatCompletionsOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ChatCompletionsToolSelectionPreset.AUTO, preset);
        assertTrue(json.contains("\"tool_choice\":\"auto\""));
    }

    @Test
    public void chatCompletionsOptionToolChoiceNone() {
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(new ArrayList<>());
        chatCompletionsOptions.setToolChoice(new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.NONE));
        ChatCompletionsToolSelectionPreset preset = chatCompletionsOptions.getToolChoice().getPreset();

        BinaryData jsonBinaryData = BinaryData.fromObject(chatCompletionsOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ChatCompletionsToolSelectionPreset.NONE, preset);
        assertTrue(json.contains("\"tool_choice\":\"none\""));
    }

    @Test
    public void chatCompletionsOptionToolChoiceRequired() {
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(new ArrayList<>());
        chatCompletionsOptions.setToolChoice(new ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset.REQUIRED));
        ChatCompletionsToolSelectionPreset preset = chatCompletionsOptions.getToolChoice().getPreset();

        BinaryData jsonBinaryData = BinaryData.fromObject(chatCompletionsOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ChatCompletionsToolSelectionPreset.REQUIRED, preset);
        assertTrue(json.contains("\"tool_choice\":\"required\""));
    }

    @Test
    public void chatCompletionsOptionToolChoiceFunctionName() {
        ChatCompletionsNamedToolSelection namedToolSelection = new ChatCompletionsNamedFunctionToolSelection(
                        new ChatCompletionsFunctionToolSelection("my_function"));
        ChatCompletionsOptions chatCompletionsOptions = new ChatCompletionsOptions(new ArrayList<>());
        chatCompletionsOptions.setToolChoice(new ChatCompletionsToolSelection(namedToolSelection));

        BinaryData jsonBinaryData = BinaryData.fromObject(chatCompletionsOptions);
        String json = jsonBinaryData.toString();

        assertTrue(json.contains("\"type\":\"function\""));
        assertTrue(json.contains("\"name\":\"my_function\""));
    }
}

