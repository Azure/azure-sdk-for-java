// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.implementation;

import com.azure.ai.inference.implementation.models.CompleteOptions;
import com.azure.ai.inference.models.ChatRequestMessage;

import com.azure.ai.inference.models.ChatRequestUserMessage;
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
        messages.add(ChatRequestUserMessage.fromString(prompt));
        return new CompleteOptions(messages);
    }

}
