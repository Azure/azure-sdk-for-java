package com.microsoft.agentserver;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.builder()
                .responseHandler(new EchoHandler())
                .build()
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}
