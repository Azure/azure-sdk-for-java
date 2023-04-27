package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.Choice;
import com.azure.ai.openai.models.Completions;
import com.azure.ai.openai.models.CompletionsUsage;

import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link Completions} instance.
 */
public final class CompletionsPropertiesHelper {
    private static CompletionsAccessor accessor;

    private CompletionsPropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link Completions}
     * instance.
     */
    public interface CompletionsAccessor {
        void setId(Completions completions, String id);
        void setCreated(Completions completions, int created);
        void setChoices(Completions completions, List<Choice> choices);
        void setUsage(Completions completions, CompletionsUsage usage);
    }

    /**
     * The method called from {@link Completions} to set it's accessor.
     *
     * @param completionsAccessor The accessor.
     */
    public static void setAccessor(final CompletionsAccessor completionsAccessor) {
        accessor = completionsAccessor;
    }

    public static void setText(Completions completions, String id) {
        accessor.setId(completions, id);
    }
    public static void setCreated(Completions completions, int created) {
        accessor.setCreated(completions, created);
    }

    public static void setChoices(Completions completions, List<Choice> choices) {
        accessor.setChoices(completions, choices);
    }
    public static void setUsage(Completions completions, CompletionsUsage usage) {
        accessor.setUsage(completions, usage);
    }
}
