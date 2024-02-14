package com.azure.ai.openai.models;

import java.util.List;

/**
 * The ChatCompletionCollection model.
 */
public final class ChatCompletionCollection {

    private List<ChatCompletion> chatCompletions;

    /**
     * @param chatCompletions
     */
    public ChatCompletionCollection(List<ChatCompletion> chatCompletions) {
        this.chatCompletions = chatCompletions;
    }

    /**
     * @return
     */
    public List<ChatCompletion> getChatCompletions() {
        return chatCompletions;
    }
}
