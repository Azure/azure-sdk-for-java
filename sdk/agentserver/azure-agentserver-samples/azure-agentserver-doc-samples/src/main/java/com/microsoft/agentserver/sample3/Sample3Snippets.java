package com.microsoft.agentserver.sample3;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 3: Full control ResponseEventStream — starts a server with the GreetingHandler.
 * Demonstrates convenience, streaming, and full-control handler variants.
 */
public class Sample3Snippets {
    public static void main(String[] args) throws InterruptedException {
        // Port 8088: Convenience handler
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(new GreetingHandler())
        );

        // Port 8089: Streaming greeting handler
        JerseyAgentServerAdaptorService.buildAgent(
            "http://0.0.0.0:8089",
            ResponsesApi.create(new StreamingGreetingHandler())
        );

        // Port 8090: Full control handler
        JerseyAgentServerAdaptorService.buildAgent(
            "http://0.0.0.0:8090",
            ResponsesApi.create(new GreetingHandlerFullControl())
        );

        System.out.println("Servers started. Test with:");
        System.out.println("  # Convenience (port 8088):");
        System.out.println("  curl -X POST http://localhost:8088/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"greeting\", \"stream\": true, \"input\": \"Hi there!\"}' \\");
        System.out.println("    --no-buffer");
        System.out.println();
        System.out.println("  # Streaming deltas (port 8089):");
        System.out.println("  curl -X POST http://localhost:8089/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"greeting\", \"stream\": true, \"input\": \"Hi there!\"}' \\");
        System.out.println("    --no-buffer");
        System.out.println();
        System.out.println("  # Full control (port 8090):");
        System.out.println("  curl -X POST http://localhost:8090/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"greeting\", \"stream\": true, \"input\": \"Hi there!\"}' \\");
        System.out.println("    --no-buffer");

        Thread.sleep(Long.MAX_VALUE);
    }
}

