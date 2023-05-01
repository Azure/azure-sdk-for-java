// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.CompletionsOptions;

import java.util.List;

public class CompletionsUtils {

    /**
     * Convenience method for minimal initialization for the CompletionsOptions class
     * @param prompt from which Completions will be generated
     * @return A CompletionsOptions object */
    public static CompletionsOptions DefaultCompletionOptions(List<String> prompt) {
        return new CompletionsOptions(prompt).setMaxTokens(DEFAULT_MAX_COMPLETION_TOKENS);
    }

    public static int DEFAULT_MAX_COMPLETION_TOKENS = 100;
}
