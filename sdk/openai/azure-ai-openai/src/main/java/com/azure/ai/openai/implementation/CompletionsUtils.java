// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.CompletionsOptions;

import java.util.ArrayList;
import java.util.List;

public class CompletionsUtils {

    /**
     * Convenience method for minimal initialization for the CompletionsOptions class
     * @param prompt from which Completions will be generated
     * @return A CompletionsOptions object */
    public static CompletionsOptions DefaultCompletionsOptions(String prompt) {
        List<String> prompts = new ArrayList<>();
        prompts.add(prompt);
        return new CompletionsOptions(prompts).setMaxTokens(DEFAULT_MAX_COMPLETION_TOKENS);
    }

    public static int DEFAULT_MAX_COMPLETION_TOKENS = 100;
}
