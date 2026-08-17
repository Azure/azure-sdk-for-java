package com.microsoft.agentserver.api.langchain4j;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class demonstrating how to use the openai-java library with the Responses API streaming.
 * <p>
 * Requires the OPENAI_API_KEY environment variable to be set.
 */
@Disabled("For manual execution")
public class OpenAIClientTest {

    @Test
    void testAskOpenAI_WhatIs3Plus4_Streaming() throws InterruptedException {

        // Build the OpenAI client
        OpenAIClient client = OpenAIOkHttpClient.builder()
            .apiKey("Ignore")
            .baseUrl("http://localhost:8088/")
            .build();

        // Create the request using Responses API with streaming
        ResponseCreateParams params = ResponseCreateParams.builder()
            .model("gpt-4o")
            .input("What is 3+4?")
            .build();

        StringBuilder responseText = new StringBuilder();
        AtomicBoolean receivedEvents = new AtomicBoolean(false);

        // Stream the response using try-with-resources
        try (var streamingResponse = client.responses().createStreaming(params)) {
            streamingResponse.stream()
                .forEach(event -> {
                    receivedEvents.set(true);

                    // Handle different event types
                    if (event.isOutputTextDelta()) {
                        String delta = event.asOutputTextDelta().delta();
                        responseText.append(delta);
                        System.out.print(delta);
                    } else if (event.isCompleted()) {
                        System.out.println("\n\nStreaming completed.");
                    }
                });
        }


        Thread.sleep(10000);

        System.out.println("\nFull response: " + responseText);

        assertTrue(receivedEvents.get(), "Should have received streaming events");
    }
}
