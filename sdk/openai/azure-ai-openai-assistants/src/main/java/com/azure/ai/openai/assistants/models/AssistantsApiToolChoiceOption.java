// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.models;

import com.azure.core.util.BinaryData;

import java.io.UncheckedIOException;

/**
 * Controls which (if any) tool is called by the model.
 * - `none` means the model will not call any tools and instead generates a message.
 * - `auto` is the default value and means the model can pick between generating a message or calling a tool.
 * Specifying a particular tool like `{"type": "file_search"}` or `{"type": "function", "function": {"name": "my_function"}}`
 * forces the model to call that tool.
 */
public class AssistantsApiToolChoiceOption {

    /**
     * Specifies how the tool choice will be used.
     */
    private final AssistantsApiToolChoiceOptionMode mode;

    /**
     * Specifies a tool the model should use. Use to force the model to call a specific tool.
     */
    private final AssistantsNamedToolChoice toolChoice;

    /**
     * Creates a new instance of AssistantsApiToolChoiceOption.
     *
     * @param mode The mode to use.
     */
    public AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode mode) {
        this.mode = mode;
        this.toolChoice = null;
    }

    /**
     * Creates a new instance of AssistantsApiToolChoiceOption.
     *
     * @param toolChoice The tool choice to use.
     */
    public AssistantsApiToolChoiceOption(AssistantsNamedToolChoice toolChoice) {
        this.mode = null;
        this.toolChoice = toolChoice;
    }

    /**
     * Gets the mode of the model will handle tool choices.
     *
     * @return The serialized JSON string.
     */
    public AssistantsApiToolChoiceOptionMode getMode() {
        return this.mode;
    }

    /**
     * Gets the tool choice to use.
     *
     * @return The tool choice to use.
     */
    public AssistantsNamedToolChoice getToolChoice() {
        return this.toolChoice;
    }

    /**
     * Deserializes the BinaryData into either type this model holds. Note that mode and toolChoice are mutually exclusive.
     *
     * @param toolChoiceBinaryData The binary data to deserialize.
     * @throws IllegalArgumentException If the provided JSON string does not match the expected format.
     * @return The deserialized model.
     */
    public static AssistantsApiToolChoiceOption fromBinaryData(BinaryData toolChoiceBinaryData) {
        if (toolChoiceBinaryData == null) {
            return null;
        }
        try {
            AssistantsNamedToolChoice toolChoice = toolChoiceBinaryData.toObject(AssistantsNamedToolChoice.class);
            if (toolChoice != null) {
                return new AssistantsApiToolChoiceOption(toolChoice);
            }
        } catch (UncheckedIOException e) {
            AssistantsApiToolChoiceOptionMode mode = toolChoiceBinaryData.toObject(AssistantsApiToolChoiceOptionMode.class);
            if (AssistantsApiToolChoiceOptionMode.values().contains(mode)) {
                return new AssistantsApiToolChoiceOption(mode);
            }
        }
        throw new IllegalArgumentException("The provided JSON string does not match the expected format.");
    }
}
