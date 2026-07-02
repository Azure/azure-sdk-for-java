package com.microsoft.agentserver.sample2;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 2: Streaming text deltas — starts a server with a handler that
 * simulates token-by-token streaming from an LLM.
 * <p>
 * Set CA_LOG_REQUESTS=true to enable request/exception logging.
 */
public class Sample2Snippets {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(
                new StreamingHandler()
            )
        );

        System.out.println("Server started. Test with:");
        System.out.println("  curl -X POST http://localhost:8088/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"streaming\", \"stream\": true, \"input\": \"Hello!\"}' \\");
        System.out.println("    --no-buffer");

        Thread.sleep(Long.MAX_VALUE);
    }
}

