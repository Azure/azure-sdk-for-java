package com.microsoft.agentserver.sample5;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 5: Conversation history — starts a server with the StudyTutorHandler.
 * Demonstrates using previous_response_id to build multi-turn conversations.
 */
public class Sample5Snippets {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(
                new StudyTutorHandler()
            )
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}

