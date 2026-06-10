package com.microsoft.agentserver.sample10;

import com.microsoft.agentserver.api.ResponsesApi;
import com.microsoft.agentserver.server.jersey.JerseyAgentServerAdaptorService;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

/**
 * Sample 10: Streaming OpenAI upstream — starts a server that proxies requests
 * to an upstream OpenAI-compatible endpoint and streams the response back.
 */
public class Sample10Snippets {
    public static void main(String[] args) throws InterruptedException {
        // Build the upstream OpenAI client.
        String apiKey = System.getenv("OPENAI_API_KEY") != null
            ? System.getenv("OPENAI_API_KEY") : "your-api-key";
        String endpoint = System.getenv("UPSTREAM_ENDPOINT") != null
            ? System.getenv("UPSTREAM_ENDPOINT") : "https://api.openai.com/v1";

        OpenAIClient upstream = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .baseUrl(endpoint)
            .build();

        JerseyAgentServerAdaptorService.buildAgent(
            ResponsesApi.create(
                new StreamingUpstreamHandler(upstream)
            )
        );

        Thread.sleep(Long.MAX_VALUE);
    }
}

