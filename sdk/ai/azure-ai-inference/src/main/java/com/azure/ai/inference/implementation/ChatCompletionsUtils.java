// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.implementation;

import com.azure.ai.openai.inference.CompleteOptions;
import com.azure.ai.openai.inference.ChatRequestUserMessage;

import com.azure.core.util.BinaryData;
import java.util.ArrayList;
import java.util.List;

/** This class contains convenience methods and constants for operations related to ChatCompletions */
public final class ChatCompletionsUtils {

    private ChatCompletionsUtils() {
    }

    /**
     * Convenience method for minimal initialization for the CompleteOptions class
     * @param prompt from which ChatCompletions will be generated
     * @return A CompleteOptions object
     * */
    public static CompleteOptions defaultCompleteOptions(String prompt) {
        List<ChatRequestUserMessage> messages = new ArrayList<>();
        String contentString = String.format("{\"content\":\"%s\"}", prompt);
        ChatRequestUserMessage message = new ChatRequestUserMessage(
            BinaryData.fromString(contentString)
        );
        messages.add(message);
        return new CompleteOptions(messages);
    }

}
