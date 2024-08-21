package com.azure.ai.openai.models;

import com.azure.core.util.BinaryData;

import java.io.UncheckedIOException;

public class ChatCompletionsToolSelection {

    private final ChatCompletionsToolSelectionPreset preset;

    private final ChatCompletionsNamedToolSelection namedToolSelection;

    public ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset preset) {
        this.preset = preset;
        this.namedToolSelection = null;
    }

    public ChatCompletionsToolSelection(ChatCompletionsNamedToolSelection namedToolSelection) {
        this.preset = null;
        this.namedToolSelection = namedToolSelection;
    }

    public ChatCompletionsToolSelectionPreset getPreset() {
        return preset;
    }

    public ChatCompletionsNamedToolSelection getNamedToolSelection() {
        return namedToolSelection;
    }

    public static ChatCompletionsToolSelection fromBinaryData(BinaryData chatCompletionsToolSelectionJson) {
        if (chatCompletionsToolSelectionJson == null) {
            return null;
        }

        try {
            ChatCompletionsNamedToolSelection namedToolSelection = chatCompletionsToolSelectionJson.toObject(ChatCompletionsNamedToolSelection.class);
            if (namedToolSelection != null) {
                return new ChatCompletionsToolSelection(namedToolSelection);
            }
        } catch (UncheckedIOException e) {
            ChatCompletionsToolSelectionPreset preset = chatCompletionsToolSelectionJson.toObject(ChatCompletionsToolSelectionPreset.class);
            if (ChatCompletionsToolSelectionPreset.values().contains(preset)) {
                return new ChatCompletionsToolSelection(preset);
            }
        }
        throw new IllegalArgumentException("The provided JSON string does not match the expected format.");
    }
}
