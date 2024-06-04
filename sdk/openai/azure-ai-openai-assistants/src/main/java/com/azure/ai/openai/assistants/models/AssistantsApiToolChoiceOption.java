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
     * Initializes a new instance of the AssistantsApiToolChoiceOption class.
     *
     * @param mode Specifies how the tool choice will be used. It could be `none` or `auto`.
     */
    public AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode mode) {
        this.mode = mode;
        this.toolChoice = null;
    }

    /**
     * Initializes a new instance of the AssistantsApiToolChoiceOption class.
     *
     * @param toolChoice Specifies a tool the model should use. Use to force the model to call a specific tool choice.
     */
    public AssistantsApiToolChoiceOption(AssistantsNamedToolChoice toolChoice) {
        this.mode = null;
        this.toolChoice = toolChoice;
    }

    /**
     * Get the mode property: Specifies how the tool choice will be used.
     *
     * @return the mode value.
     */
    public AssistantsApiToolChoiceOptionMode getMode() {
        return this.mode;
    }

    /**
     * Get the toolChoice property: Specifies a tool the model should use. Use to force the model to call a specific tool.
     *
     * @return the toolChoice value.
     */
    public AssistantsNamedToolChoice getToolChoice() {
        return this.toolChoice;
    }

    /**
     * Serialize the object to a JSON string.
     *
     * @param toolChoiceBinaryData the object to deserialize.
     * @return the JSON string representation of the object.
     * @throws IllegalArgumentException if the provided JSON string does not match the expected format.
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
