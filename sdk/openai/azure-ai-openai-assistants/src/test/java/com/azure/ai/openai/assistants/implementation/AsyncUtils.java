// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.OpenAIFile;

public class AsyncUtils {

    private Assistant assistant;
    private AssistantThread thread;
    private OpenAIFile file;

    public Assistant getAssistant() {
        return assistant;
    }

    public void setAssistant(Assistant assistant) {
        this.assistant = assistant;
    }

    public AssistantThread getThread() {
        return thread;
    }

    public void setThread(AssistantThread thread) {
        this.thread = thread;
    }

    public OpenAIFile getFile() {
        return file;
    }

    public void setFile(OpenAIFile file) {
        this.file = file;
    }
}
