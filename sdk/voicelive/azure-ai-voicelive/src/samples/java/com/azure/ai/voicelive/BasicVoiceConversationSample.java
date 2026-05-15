// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateResponseTextDelta;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Basic voice conversation sample demonstrating a minimal text exchange with the VoiceLive service.
 *
 * <p><strong>Start here if you're new to VoiceLive!</strong> This is the simplest end-to-end sample:
 * it sends one user message and prints the model's text reply, then exits.</p>
 *
 * <p>Use this sample to verify that your endpoint, credential, and basic realtime session setup are
 * working before you add microphone input, speaker output, tool calls, or tracing.</p>
 *
 * <p>When you run it, the sample:</p>
 * <ol>
 *   <li>Opens a realtime session and configures it for text-only output.</li>
 *   <li>Sends a fixed user message ("Say hello in one sentence.").</li>
 *   <li>Asks the model to generate a response.</li>
 *   <li>Prints text deltas as they stream in.</li>
 *   <li>Exits automatically when the response completes (or after a timeout).</li>
 * </ol>
 *
 * <p>This sample shows the simplest way to:</p>
 * <ul>
 *   <li>Create a VoiceLive client</li>
 *   <li>Start a session with basic configuration</li>
 *   <li>Send a user message and request a response</li>
 *   <li>Subscribe to receive events using the reactor {@code subscribe(onNext, onError, onComplete)} pattern</li>
 *   <li>Keep {@code main} alive with a {@link CountDownLatch} until the response finishes</li>
 * </ul>
 *
 * <p><strong>Reactor pattern used by this sample:</strong></p>
 * <pre>{@code
 * client.startSession(model)
 *     .flatMap(session -> session.sendEvent(sessionUpdate).thenReturn(session))
 *     .flatMap(session -> session.sendEvent(conversationItemCreate).thenReturn(session))
 *     .flatMap(session -> session.sendEvent(responseCreate).thenReturn(session))
 *     .flatMapMany(session -> session.receiveEvents())
 *     .subscribe(this::handleEvent, this::onError, this::onComplete);
 * }</pre>
 * <p>Because {@code subscribe} is non-blocking, {@code main} would otherwise return immediately.
 * The {@code CountDownLatch} blocks {@code main} until {@code RESPONSE_DONE} (or an error) fires.</p>
 *
 * <p><strong>How to stop:</strong> The sample exits automatically once the model's response is
 * complete. You can also press {@code Ctrl+C} at any time to abort.</p>
 *
 * <p><strong>Next Steps - Learn More:</strong></p>
 * <ul>
 *   <li>{@link AuthenticationMethodsSample} - Explore authentication options (API Key vs Token Credential)</li>
 *   <li>{@link MicrophoneInputSample} - Add real-time microphone audio input</li>
 *   <li>{@link AudioPlaybackSample} - Add audio response playback</li>
 *   <li>{@link VoiceAssistantSample} - See a complete production-ready voice assistant</li>
 * </ul>
 *
 * <p><strong>Environment Variables:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - (Required) The VoiceLive service endpoint URL</li>
 * </ul>
 *
 * <p>This sample uses {@link DefaultAzureCredentialBuilder} (Entra ID, recommended). For an example
 * of API key authentication, see {@link AuthenticationMethodsSample}.</p>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.BasicVoiceConversationSample" -Dexec.classpathScope=test
 * }</pre>
 */
public final class BasicVoiceConversationSample {

    private static final long COMPLETION_TIMEOUT_SECONDS = 30;

    /**
     * Main method to run the basic voice conversation sample.
     *
     * @param args Unused command line arguments
     */
    public static void main(String[] args) {
        // Get endpoint from environment variable
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        if (endpoint == null) {
            System.err.println("Please set AZURE_VOICELIVE_ENDPOINT environment variable");
            return;
        }

        // Create the VoiceLive client using DefaultAzureCredential (Entra ID).
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        System.out.println("Starting basic voice conversation...");

        // Configure session for text-only output (no audio modality, so no speaker required).
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI assistant. Reply concisely.")
            .setModalities(Collections.singletonList(InteractionModality.TEXT));

        // Latch keeps main alive until the response completes (or an error occurs).
        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Open a WebSocket session against the realtime model.
        client.startSession("gpt-realtime")
            // Configure the session (text-only modality, instructions).
            .flatMap(session -> {
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent).thenReturn(session);
            })
            // Send a user message into the conversation.
            .flatMap(session -> {
                InputTextContentPart textContent = new InputTextContentPart("Say hello in one sentence.");
                UserMessageItem messageItem = new UserMessageItem(Collections.singletonList(textContent));
                ClientEventConversationItemCreate createEvent = new ClientEventConversationItemCreate()
                    .setItem(messageItem);
                return session.sendEvent(createEvent).thenReturn(session);
            })
            // Ask the model to generate a response for the queued message.
            .flatMap(session -> {
                ClientEventResponseCreate responseEvent = new ClientEventResponseCreate();
                return session.sendEvent(responseEvent).thenReturn(session);
            })
            // Subscribe to the server event stream (text deltas, response.done, etc.).
            .flatMapMany(session -> session.receiveEvents())
            .subscribe(
                event -> handleEvent(event, completionLatch),
                error -> {
                    System.err.println("Error: " + error.getMessage());
                    completionLatch.countDown();
                },
                completionLatch::countDown
            );

        try {
            if (!completionLatch.await(COMPLETION_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("Timed out waiting for response to complete.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Handle incoming server events: print text deltas as they stream, and release the latch when
     * the response is complete or an error occurs.
     */
    private static void handleEvent(SessionUpdate event, CountDownLatch completionLatch) {
        ServerEventType eventType = event.getType();

        if (eventType == ServerEventType.SESSION_CREATED) {
            System.out.println("✓ Session created");
        } else if (eventType == ServerEventType.SESSION_UPDATED) {
            System.out.println("✓ Session configured");
            System.out.print("Assistant: ");
        } else if (eventType == ServerEventType.RESPONSE_TEXT_DELTA) {
            // Stream text deltas to the console as they arrive.
            if (event instanceof SessionUpdateResponseTextDelta) {
                String delta = ((SessionUpdateResponseTextDelta) event).getDelta();
                if (delta != null) {
                    System.out.print(delta);
                }
            }
        } else if (eventType == ServerEventType.RESPONSE_TEXT_DONE) {
            System.out.println();
        } else if (eventType == ServerEventType.RESPONSE_DONE) {
            System.out.println("✓ Response complete");
            completionLatch.countDown();
        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error: " + ((SessionUpdateError) event).getError().getMessage());
            completionLatch.countDown();
        }
    }

    // Private constructor to prevent instantiation
    private BasicVoiceConversationSample() {
    }
}
