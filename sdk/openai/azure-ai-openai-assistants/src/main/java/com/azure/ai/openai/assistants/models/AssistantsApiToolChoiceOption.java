package com.azure.ai.openai.assistants.models;

import com.azure.core.util.BinaryData;

import java.io.IOException;
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

    public AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode mode) {
        this.mode = mode;
        this.toolChoice = null;
    }

    public AssistantsApiToolChoiceOption(AssistantsNamedToolChoice toolChoice) {
        this.mode = null;
        this.toolChoice = toolChoice;
    }

    public AssistantsApiToolChoiceOptionMode getMode() {
        return this.mode;
    }

    public AssistantsNamedToolChoice getToolChoice() {
        return this.toolChoice;
    }

//    public static AssistantsApiToolChoiceOption fromBinaryData(BinaryData toolChoiceBinaryData) {
////        try {
////            return new AssistantsApiToolChoiceOption(AssistantsNamedToolChoice.fromBinaryData(toolChoiceBinaryData));
////        } catch (UncheckedIOException e) {
////            throw new IllegalArgumentException("Failed to parse JSON string.", e);
////        }
//    }
}
