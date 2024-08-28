// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.io.UncheckedIOException;

/**
 * Represents the tool selection for chat completions. The input can be either an enum value from {@link ChatCompletionsToolSelectionPreset}
 * or a named tool selection passed in the form of {@link ChatCompletionsNamedToolSelection}.
 */
@Immutable
public class ChatCompletionsToolSelection {

    /**
     * The preset tool selection.
     */
    private final ChatCompletionsToolSelectionPreset preset;

    /**
     * The named tool selection.
     */
    private final ChatCompletionsNamedToolSelection namedToolSelection;

    /**
     * Creates a {@link ChatCompletionsToolSelection} with the provided preset.
     *
     * @param preset The preset tool selection.
     */
    public ChatCompletionsToolSelection(ChatCompletionsToolSelectionPreset preset) {
        this.preset = preset;
        this.namedToolSelection = null;
    }

    /**
     * Creates a {@link ChatCompletionsToolSelection} with the provided named tool selection.
     *
     * @param namedToolSelection The named tool selection.
     */
    public ChatCompletionsToolSelection(ChatCompletionsNamedToolSelection namedToolSelection) {
        this.preset = null;
        this.namedToolSelection = namedToolSelection;
    }

    /**
     * Gets the preset tool selection.
     *
     * @return The preset tool selection.
     */
    public ChatCompletionsToolSelectionPreset getPreset() {
        return preset;
    }

    /**
     * Gets the named tool selection.
     *
     * @return The named tool selection.
     */
    public ChatCompletionsNamedToolSelection getNamedToolSelection() {
        return namedToolSelection;
    }

    /**
     * Converts a JSON string to a {@link ChatCompletionsToolSelection}.
     *
     * @throws IllegalArgumentException If the provided JSON string does not match the expected format.
     * @param chatCompletionsToolSelectionJson The JSON string representing the chat completions tool selection.
     * @return The {@link ChatCompletionsToolSelection} represented by the provided JSON string.
     */
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
