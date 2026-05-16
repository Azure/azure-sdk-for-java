// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ItemType;
import com.azure.ai.voicelive.models.MCPApprovalResponseRequestItem;
import com.azure.ai.voicelive.models.MCPApprovalType;
import com.azure.ai.voicelive.models.MCPServer;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.ResponseMCPApprovalRequestItem;
import com.azure.ai.voicelive.models.ResponseMCPCallItem;
import com.azure.ai.voicelive.models.ServerEventResponseMcpCallArgumentsDone;
import com.azure.ai.voicelive.models.ServerEventResponseMcpCallCompleted;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseOutputItemDone;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionResponseItem;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveToolDefinition;
import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MCP (Model Context Protocol) sample demonstrating how to use VoiceLive with MCP servers.
 *
 * <p>Use this sample when your assistant needs tools that live outside the current process, such as
 * documentation search or repo analysis exposed through an MCP server. It is the right sample for
 * learning approval flows and external tool execution.</p>
 *
 * <p>When you run it, the sample configures one or more MCP-backed tools, starts a voice session,
 * listens for MCP call and approval events, and forwards the user's choices and tool outputs back
 * into the realtime conversation.</p>
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Configure MCP servers for external tool integration</li>
 *   <li>Handle MCP call events and tool execution</li>
 *   <li>Handle MCP approval requests for tool calls</li>
 *   <li>Process MCP call results and continue conversations</li>
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
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.MCPSample" -Dexec.classpathScope=test
 * }</pre>
 *
 * <p><strong>Try asking:</strong></p>
 * <ul>
 *   <li>"Can you summarize the Azure SDK for Java GitHub repo?"</li>
 *   <li>"Can you summarize Azure docs about VoiceLive?"</li>
 * </ul>
 */
public final class MCPSample {

    // Service configuration
    private static final String DEFAULT_MODEL = "gpt-realtime";
    private static final String ENV_ENDPOINT = "AZURE_VOICELIVE_ENDPOINT";

    // Audio format constants
    private static final int SAMPLE_RATE = 24000;
    private static final int CHANNELS = 1;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHUNK_SIZE = 1200;

    private MCPSample() {
    }

    /**
     * Main method to run the MCP sample.
     */
    public static void main(String[] args) {
        System.out.println("🎙️ Voice Assistant with MCP - Azure VoiceLive SDK");
        printSeparator();

        // Get configuration from environment variables
        String endpoint = System.getenv(ENV_ENDPOINT);
        if (endpoint == null || endpoint.trim().isEmpty()) {
            System.err.println("❌ Error: No endpoint provided");
            System.err.println("Please set the " + ENV_ENDPOINT + " environment variable.");
            System.exit(1);
        }

        try {
            runMCPSample(endpoint);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Run the MCP sample.
     */
    private static void runMCPSample(String endpoint) {
        System.out.println("🔌 Connecting to VoiceLive API with MCP support...");
        System.out.println("📡 Endpoint: " + endpoint);
        System.out.println("🤖 Model: " + DEFAULT_MODEL);

        AtomicReference<String> activeMCPCallId = new AtomicReference<>();
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();

        // Latch keeps main alive until the event stream completes (or an error occurs).
        final CountDownLatch completionLatch = new CountDownLatch(1);

        // Create the VoiceLive client using DefaultAzureCredential (Entra ID).
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .serviceVersion(VoiceLiveServiceVersion.V2026_01_01_PREVIEW)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Start the session. Session lifetime is local to this reactive chain — the session is
        // captured by the lambda passed to flatMapMany and then threaded into per-event handling
        // via flatMap, so no instance field or shared holder is needed.
        client.startSession(DEFAULT_MODEL)
            .flatMapMany(session -> {
                System.out.println("✓ Session started successfully");
                audioProcessorRef.set(new AudioProcessor(session));
                return configureSession(session)
                    .thenMany(session.receiveEvents())
                    .flatMap(event -> handleServerEvent(session, event, activeMCPCallId, audioProcessorRef.get()));
            })
            .subscribe(
                ignored -> { },
                error -> {
                    System.err.println("❌ Error processing events: " + error.getMessage());
                    running.set(false);
                    shutdownAudio(audioProcessorRef);
                    completionLatch.countDown();
                },
                () -> {
                    System.out.println("✓ Event stream completed");
                    shutdownAudio(audioProcessorRef);
                    completionLatch.countDown();
                }
            );

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Send the session configuration with MCP tools.
     */
    private static Mono<Void> configureSession(VoiceLiveSessionAsyncClient session) {
        System.out.println("📤 Sending session configuration with MCP tools...");
        return session.sendEvent(createSessionConfigWithMCPTools()).then();
    }

    /**
     * Cleanup audio processor.
     */
    private static void shutdownAudio(AtomicReference<AudioProcessor> audioProcessorRef) {
        AudioProcessor processor = audioProcessorRef.getAndSet(null);
        if (processor != null) {
            processor.cleanup();
        }
    }

    /**
     * Create session configuration with MCP tools.
     */
    private static ClientEventSessionUpdate createSessionConfigWithMCPTools() {
        // Define MCP servers as tools
        List<VoiceLiveToolDefinition> mcpTools = Arrays.asList(
            // DeepWiki MCP server - no approval required
            new MCPServer("deepwiki", "https://mcp.deepwiki.com/mcp")
                .setAllowedTools(Arrays.asList("read_wiki_structure", "ask_question"))
                .setRequireApproval(BinaryData.fromObject(MCPApprovalType.NEVER)),

            // Azure documentation MCP server - approval always required
            new MCPServer("azure_doc", "https://learn.microsoft.com/api/mcp")
                .setRequireApproval(BinaryData.fromObject(MCPApprovalType.ALWAYS))
        );

        // Create session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions(
                "You are a helpful AI assistant with access to some MCP servers. "
                + "You can use MCP tools to search for information when needed. "
                + "When calling MCP tools, explain what you're doing and present the results naturally."
            )
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(SAMPLE_RATE)
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setTurnDetection(new ServerVadTurnDetection()
                .setThreshold(0.5)
                .setPrefixPaddingMs(300)
                .setSilenceDurationMs(500)
                .setInterruptResponse(true)
                .setAutoTruncate(true)
                .setCreateResponse(true))
            .setTools(mcpTools)
            .setInputAudioTranscription(
                new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1)
            );

        return new ClientEventSessionUpdate(sessionOptions);
    }

    /**
     * Handle server events including MCP-specific events. Returns a {@link Mono} so any
     * follow-up events sent back to the service stay inside the reactive chain instead of
     * triggering nested subscribes.
     */
    private static Mono<Void> handleServerEvent(
        VoiceLiveSessionAsyncClient session,
        SessionUpdate event,
        AtomicReference<String> activeMCPCallId,
        AudioProcessor audioProcessor
    ) {
        ServerEventType eventType = event.getType();

        if (event instanceof SessionUpdateSessionUpdated) {
            System.out.println("✅ Session ready");
            audioProcessor.startCapture();
            audioProcessor.startPlayback();

            System.out.println();
            printSeparator();
            System.out.println("🎤 MCP VOICE ASSISTANT READY");
            System.out.println("🎯 Available MCP Tools:");
            System.out.println("  • deepwiki: Can search and read wiki structure");
            System.out.println("  • azure_doc: Requires approval for tool calls");
            System.out.println();
            System.out.println("Try asking:");
            System.out.println("  • 'Can you summary github repo azure sdk for java?'");
            System.out.println("  • 'Can you summary azure docs about voice live?'");
            System.out.println("Press Ctrl+C to exit");
            printSeparator();
            System.out.println();

        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED) {
            System.out.println("🎤 Listening...");
            audioProcessor.skipPendingAudio();

        } else if (eventType == ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED) {
            System.out.println("🤔 Processing...");

        } else if (eventType == ServerEventType.RESPONSE_CREATED) {
            System.out.println("🤖 Assistant response created");

        } else if (event instanceof SessionUpdateResponseAudioDelta) {
            SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
            byte[] audioData = audioDelta.getDelta();
            if (audioData != null && audioData.length > 0) {
                audioProcessor.queueAudio(audioData);
            }

        } else if (eventType == ServerEventType.RESPONSE_DONE) {
            System.out.println("✅ Response completed");
            System.out.println();

        } else if (event instanceof SessionUpdateResponseOutputItemDone) {
            handleOutputItemDone((SessionUpdateResponseOutputItemDone) event);

        } else if (event instanceof SessionUpdateConversationItemCreated) {
            return handleConversationItemCreated(session, (SessionUpdateConversationItemCreated) event, activeMCPCallId);

        } else if (event instanceof ServerEventResponseMcpCallArgumentsDone) {
            handleMCPCallArgumentsDone((ServerEventResponseMcpCallArgumentsDone) event);

        } else if (event instanceof ServerEventResponseMcpCallCompleted) {
            return handleMCPCallCompleted(session, (ServerEventResponseMcpCallCompleted) event);

        } else if (eventType == ServerEventType.RESPONSE_MCP_CALL_FAILED) {
            System.err.println("❌ MCP call failed");

        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error event received");
        }

        return Mono.empty();
    }

    /**
     * Handle output item done event to display MCP results.
     */
    private static void handleOutputItemDone(SessionUpdateResponseOutputItemDone event) {
        SessionResponseItem item = event.getItem();
        if (item != null && item.getType() == ItemType.MCP_CALL) {
            ResponseMCPCallItem mcpCallItem = (ResponseMCPCallItem) item;
            String output = mcpCallItem.getOutput();

            if (output != null && !output.isEmpty()) {
                printSeparator();
                System.out.println("📥 MCP TOOL RESULT:");
                System.out.println("   Server: " + mcpCallItem.getServerLabel());
                System.out.println("   Tool: " + mcpCallItem.getName());
                System.out.println("   Output:");
                System.out.println(output);
                printSeparator();
                System.out.println();
            }

            if (mcpCallItem.getError() != null) {
                System.err.println("❌ MCP call error: " + mcpCallItem.getError().toString());
            }
        }
    }

    /**
     * Handle conversation item created events (MCP calls and approval requests).
     */
    private static Mono<Void> handleConversationItemCreated(
        VoiceLiveSessionAsyncClient session,
        SessionUpdateConversationItemCreated itemCreated,
        AtomicReference<String> activeMCPCallId
    ) {
        if (itemCreated.getItem() == null) {
            return Mono.empty();
        }

        ItemType itemType = itemCreated.getItem().getType();

        if (itemType == ItemType.MCP_LIST_TOOLS) {
            System.out.println("📋 MCP list tools requested: id=" + itemCreated.getItem().getId());

        } else if (itemType == ItemType.MCP_CALL) {
            ResponseMCPCallItem mcpCallItem = (ResponseMCPCallItem) itemCreated.getItem();
            String callId = mcpCallItem.getId();
            activeMCPCallId.set(callId);

            System.out.println("🔧 MCP Call initiated:");
            System.out.println("   Server: " + mcpCallItem.getServerLabel());
            System.out.println("   Tool: " + mcpCallItem.getName());
            System.out.println("   Call ID: " + callId);

        } else if (itemType == ItemType.MCP_APPROVAL_REQUEST) {
            return handleMCPApprovalRequest(session, (ResponseMCPApprovalRequestItem) itemCreated.getItem());
        }

        return Mono.empty();
    }

    /**
     * Handle MCP approval request events.
     */
    private static Mono<Void> handleMCPApprovalRequest(
        VoiceLiveSessionAsyncClient session,
        ResponseMCPApprovalRequestItem approvalItem
    ) {
        String approvalId = approvalItem.getId();
        String serverLabel = approvalItem.getServerLabel();
        String functionName = approvalItem.getName();
        String arguments = approvalItem.getArguments();

        System.out.println("🔐 MCP Approval Request received:");
        System.out.println("   ID: " + approvalId);
        System.out.println("   Server: " + serverLabel);
        System.out.println("   Tool: " + functionName);
        System.out.println("   Arguments: " + arguments);
        System.out.println();

        // Get user approval
        boolean approved = getUserApproval();

        MCPApprovalResponseRequestItem approvalResponse =
            new MCPApprovalResponseRequestItem(approvalId, approved);

        return session.sendEvent(new ClientEventConversationItemCreate().setItem(approvalResponse));
    }

    /**
     * Get user approval for MCP tool call.
     */
    private static boolean getUserApproval() {
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Approve MCP call? (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            switch (input) {
                case "y":
                    return true;
                case "n":
                    return false;
                default:
                    System.out.println("Invalid input. Please type 'y' to approve or 'n' to deny.");
            }
        }
    }

    /**
     * Handle MCP call arguments done event.
     */
    private static void handleMCPCallArgumentsDone(ServerEventResponseMcpCallArgumentsDone event) {
        System.out.println("📦 MCP Call arguments received:");
        System.out.println("   Item ID: " + event.getItemId());
        System.out.println("   Arguments: " + event.getArguments());
    }

    /**
     * Handle MCP call completed event.
     */
    private static Mono<Void> handleMCPCallCompleted(
        VoiceLiveSessionAsyncClient session,
        ServerEventResponseMcpCallCompleted event
    ) {
        System.out.println("✅ MCP call completed for item: " + event.getItemId());

        // Wait for output item done event to get the results
        // In a real implementation, you might want to collect the output
        // For this sample, we'll just trigger a new response
        return session.sendEvent(new ClientEventResponseCreate());
    }

    /**
     * Print a separator line.
     */
    private static void printSeparator() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            sb.append("=");
        }
        System.out.println(sb.toString());
    }

    /**
     * Manages audio capture and playback.
     */
    private static class AudioProcessor {
        private final VoiceLiveSessionAsyncClient session;
        private final AudioFormat audioFormat;

        // volatile: shared between the reactor event thread (startCapture/Playback) and the
        // audio capture/playback worker threads
        private volatile TargetDataLine microphone;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

        private volatile SourceDataLine speaker;
        private final BlockingQueue<byte[]> playbackQueue = new LinkedBlockingQueue<>(1000);
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);
        private volatile Thread captureThread;
        private volatile Thread playbackThread;

        AudioProcessor(VoiceLiveSessionAsyncClient session) {
            this.session = session;
            this.audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
        }

        void startCapture() {
            if (isCapturing.get()) {
                return;
            }

            try {
                DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
                microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
                microphone.open(audioFormat, CHUNK_SIZE * 4);
                microphone.start();
                isCapturing.set(true);

                // Start capture thread
                captureThread = new Thread(() -> {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    while (isCapturing.get()) {
                        try {
                            int bytesRead = microphone.read(buffer, 0, buffer.length);
                            if (bytesRead > 0) {
                                byte[] audioData = Arrays.copyOf(buffer, bytesRead);
                                // sendInputAudio returns a cold Mono - it must be subscribed
                                // for the audio to actually be sent over the WebSocket.
                                session.sendInputAudio(BinaryData.fromBytes(audioData))
                                    .subscribe(
                                        noValueEmitted -> { /* sendInputAudio returns Mono<Void>; no onNext values are ever emitted */ },
                                        error -> System.err.println("Error sending audio: " + error.getMessage())
                                    );
                            }
                        } catch (Exception e) {
                            if (isCapturing.get()) {
                                System.err.println("Error capturing audio: " + e.getMessage());
                            }
                            break;
                        }
                    }
                }, "AudioCapture");
                captureThread.setDaemon(true);
                captureThread.start();

                System.out.println("✅ Audio capture started");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to start audio capture: " + e.getMessage());
                isCapturing.set(false);
            }
        }

        void startPlayback() {
            if (isPlaying.get()) {
                return;
            }

            try {
                DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
                speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
                speaker.open(audioFormat, CHUNK_SIZE * 4);
                speaker.start();
                isPlaying.set(true);

                // Start playback thread
                playbackThread = new Thread(() -> {
                    while (isPlaying.get()) {
                        try {
                            byte[] audioData = playbackQueue.take();
                            speaker.write(audioData, 0, audioData.length);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        } catch (Exception e) {
                            if (isPlaying.get()) {
                                System.err.println("Error playing audio: " + e.getMessage());
                            }
                            break;
                        }
                    }
                }, "AudioPlayback");
                playbackThread.setDaemon(true);
                playbackThread.start();

                System.out.println("✅ Audio playback started");
            } catch (LineUnavailableException e) {
                System.err.println("Failed to start audio playback: " + e.getMessage());
                isPlaying.set(false);
            }
        }

        void skipPendingAudio() {
            playbackQueue.clear();
        }

        void queueAudio(byte[] audioData) {
            if (isPlaying.get()) {
                // offer() returns false if the bounded queue is full; warn so a slow consumer is visible
                if (!playbackQueue.offer(audioData)) {
                    System.err.println("Warning: playback queue full, dropping audio chunk of " + audioData.length + " bytes");
                }
            }
        }

        void cleanup() {
            isCapturing.set(false);
            isPlaying.set(false);

            if (microphone != null) {
                microphone.stop();
                microphone.close();
                microphone = null;
            }
            if (captureThread != null) {
                captureThread.interrupt();
                captureThread = null;
            }

            if (speaker != null) {
                speaker.drain();
                speaker.stop();
                speaker.close();
                speaker = null;
            }
            if (playbackThread != null) {
                playbackThread.interrupt();
                playbackThread = null;
            }

            playbackQueue.clear();
        }
    }
}
