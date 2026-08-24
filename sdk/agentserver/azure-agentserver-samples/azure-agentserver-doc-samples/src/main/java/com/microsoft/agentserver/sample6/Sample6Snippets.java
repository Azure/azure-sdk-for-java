package com.microsoft.agentserver.sample6;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 6: Multi-output — starts a server with the MathSolverHandler.
 * Demonstrates emitting reasoning + text message as multiple output items.
 */
public class Sample6Snippets {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(
                new MathSolverHandler()
            )
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}

