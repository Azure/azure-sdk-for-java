package com.microsoft.agentserver.sample4;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 4: Function calling — starts a server with the WeatherHandler.
 * Demonstrates emitting function calls and processing function call outputs.
 */
public class Sample4Snippets {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(
                new WeatherHandler()
            )
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}

