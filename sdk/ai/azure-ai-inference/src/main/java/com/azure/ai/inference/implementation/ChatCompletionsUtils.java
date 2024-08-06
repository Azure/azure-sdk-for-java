// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.implementation;

import com.azure.ai.inference.implementation.models.CompleteOptions;
import com.azure.ai.inference.models.ChatRequestMessage;

import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import java.io.IOException;

/** This class contains convenience methods and constants for operations related to ChatCompletions */
public final class ChatCompletionsUtils {

    private static final ClientLogger LOGGER = new ClientLogger(ChatCompletionsUtils.class);
    private ChatCompletionsUtils() {
    }

    /**
     * Convenience method for minimal initialization for the CompleteOptions class
     * @param prompt from which ChatCompletions will be generated
     * @return A CompleteOptions object
     * */
    public static CompleteOptions defaultCompleteOptions(String prompt) {
        List<ChatRequestMessage> messages = new ArrayList<>();
        String jsonPrompt = "{"
            + "\"role\":\"user\","
            + "\"content\":\"%s\""
            + "}";
        String contentString = String.format(jsonPrompt, prompt);
        try {
            ChatRequestMessage message = ChatRequestMessage.fromJson(
                JsonProviders.createReader(new StringReader(contentString))
            );
            messages.add(message);
        } catch (IOException ex) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                "prompt string not accepted for JSON parsing"));
        }
        return new CompleteOptions(messages);
    }

}
