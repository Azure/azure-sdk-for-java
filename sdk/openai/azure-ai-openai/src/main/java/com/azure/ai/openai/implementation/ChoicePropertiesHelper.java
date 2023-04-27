package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsFinishReason;
import com.azure.ai.openai.models.CompletionsLogProbabilityModel;
import com.azure.ai.openai.models.CompletionsUsage;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link Choice} instance.
 */
public final class ChoicePropertiesHelper {
    private static ChoiceAccessor accessor;

    private ChoicePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Choice}
     * instance.
     */
    public interface ChoiceAccessor {
        void setText(Choice choice, String text);
        void setIndex(Choice choice, int index);
        void setLogprobs(Choice choice, CompletionsLogProbabilityModel logprobs);
        void setFinishReason(Choice choice, CompletionsFinishReason finishReason);
    }

    /**
     * The method called from {@link Choice} to set it's accessor.
     *
     * @param choiceAccessor The accessor.
     */
    public static void setAccessor(final ChoiceAccessor choiceAccessor) {
        accessor = choiceAccessor;
    }

    public static void setText(Choice choice, String text) {
        accessor.setText(choice, text);
    }
    public static void setIndex(Choice choice, int index) {
        accessor.setIndex(choice, index);
    }

    public static void setLogprobs(Choice choice, CompletionsLogProbabilityModel logprobs) {
        accessor.setLogprobs(choice, logprobs);
    }
    public static void setFinishReason(Choice choice, CompletionsFinishReason finishReason) {
        accessor.setFinishReason(choice, finishReason);
    }
}
