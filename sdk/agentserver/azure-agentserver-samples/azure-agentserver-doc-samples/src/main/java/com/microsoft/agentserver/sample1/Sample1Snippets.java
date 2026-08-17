package com.microsoft.agentserver.sample1;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;

/**
 * Sample 1: Getting started — starts an echo agent server.
 * <p>
 * Test with:
 * <pre>{@code
 * curl -X POST http://localhost:8088/responses \
 *   -H "Content-Type: application/json" \
 *   -d '{"model": "echo", "stream": true, "input": "Hello, world!"}' \
 *   --no-buffer
 * }</pre>
 */
public class Sample1Snippets {
    public static void main(String[] args) throws InterruptedException {
        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(new EchoHandler())
        );

        System.out.println("Server started. Test with:");
        System.out.println("  # Non-streaming:");
        System.out.println("  curl -X POST http://localhost:8088/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"echo\", \"input\": \"Hello, world!\"}'");
        System.out.println();
        System.out.println("  # Streaming:");
        System.out.println("  curl -X POST http://localhost:8088/responses \\");
        System.out.println("    -H \"Content-Type: application/json\" \\");
        System.out.println("    -d '{\"model\": \"echo\", \"stream\": true, \"input\": \"Hello, world!\"}' \\");
        System.out.println("    --no-buffer");

        Thread.sleep(Long.MAX_VALUE);
    }
}

