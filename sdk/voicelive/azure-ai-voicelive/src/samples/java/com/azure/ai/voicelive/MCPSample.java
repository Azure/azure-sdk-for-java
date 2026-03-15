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
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * MCP (Model Context Protocol) sample demonstrating how to use VoiceLive with MCP servers.
 *
 * <p>This sample shows how to:</p>
 * <ul>
 *   <li>Configure MCP servers for external tool integration</li>
 *   <li>Handle MCP call events and tool execution</li>
 *   <li>Handle MCP approval requests for tool calls</li>
 *   <li>Process MCP call results and continue conversations</li>
 * </ul>
 *
 * <p><strong>Environment Variables Required:</strong></p>
 * <ul>
 *   <li>AZURE_VOICELIVE_ENDPOINT - The VoiceLive service endpoint URL</li>
 *   <li>AZURE_VOICELIVE_API_KEY - The API key for authentication</li>
 * </ul>
 *
 * <p><strong>How to Run:</strong></p>
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="com.azure.ai.voicelive.MCPSample" -Dexec.classpathScope=test
 * }</pre>
 *
 * <p><strong>Try asking:</strong></p>
 * <ul>
 *   <li>"Can you summary github repo azure sdk for java?"</li>
 *   <li>"Can you summary azure docs about voice live?"</li>
 * </ul>
 */
public final class MCPSample {

    // Service configuration
    private static final String DEFAULT_MODEL = "gpt-4o-realtime-preview";
    private static final String ENV_ENDPOINT = "AZURE_VOICELIVE_ENDPOINT";
    private static final String ENV_API_KEY = "AZURE_VOICELIVE_API_KEY";

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

        String apiKey = System.getenv(ENV_API_KEY);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("❌ Error: No API key provided");
            System.err.println("Please set the " + ENV_API_KEY + " environment variable.");
            System.exit(1);
        }

        try {
            runMCPSample(endpoint, apiKey);
        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Run the MCP sample.
     */
    private static void runMCPSample(String endpoint, String apiKey) {
        System.out.println("🔌 Connecting to VoiceLive API with MCP support...");
        System.out.println("📡 Endpoint: " + endpoint);
        System.out.println("🤖 Model: " + DEFAULT_MODEL);

        KeyCredential credential = new KeyCredential(apiKey);
        AtomicReference<String> activeMCPCallId = new AtomicReference<>();
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicReference<AudioProcessor> audioProcessorRef = new AtomicReference<>();

        // Create VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(credential)
            .serviceVersion(VoiceLiveServiceVersion.V2026_01_01_PREVIEW)
            .buildAsyncClient();

        // Start the session
        client.startSession(DEFAULT_MODEL)
            .flatMap(session -> {
                System.out.println("✓ Session started successfully");

                // Create audio processor
                AudioProcessor audioProcessor = new AudioProcessor(session);
                audioProcessorRef.set(audioProcessor);

                // Subscribe to receive server events
                session.receiveEvents()
                    .subscribe(
                        event -> handleServerEvent(session, event, activeMCPCallId, audioProcessor),
                        error -> {
                            System.err.println("❌ Error processing events: " + error.getMessage());
                            running.set(false);
                        },
                        () -> System.out.println("✓ Event stream completed")
                    );

                // Send session configuration with MCP tools
                System.out.println("📤 Sending session configuration with MCP tools...");
                ClientEventSessionUpdate sessionConfig = createSessionConfigWithMCPTools();
                session.sendEvent(sessionConfig)
                    .doOnSuccess(v -> {
                        System.out.println("✓ Session configured with MCP tools");
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

                        // Start audio playback
                        audioProcessor.startPlayback();
                    })
                    .doOnError(error -> System.err.println("❌ Failed to send session.update: " + error.getMessage()))
                    .subscribe();

                // Add shutdown hook
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    System.out.println("\n👋 Shutting down MCP voice assistant...");
                    running.set(false);
                    AudioProcessor processor = audioProcessorRef.get();
                    if (processor != null) {
                        processor.cleanup();
                    }
                }));

                // Keep the reactive chain alive
                return Mono.never();
            })
            .doOnError(error -> System.err.println("❌ Error: " + error.getMessage()))
            .block();
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
     * Handle server events including MCP-specific events.
     */
    private static void handleServerEvent(
        VoiceLiveSessionAsyncClient session,
        SessionUpdate event,
        AtomicReference<String> activeMCPCallId,
        AudioProcessor audioProcessor
    ) {
        ServerEventType eventType = event.getType();

        if (event instanceof SessionUpdateSessionUpdated) {
            System.out.println("✅ Session ready");
            audioProcessor.startCapture();
            System.out.println("🎤 Start speaking. Try asking about Azure documentation or wiki information.");

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
            handleConversationItemCreated(session, (SessionUpdateConversationItemCreated) event, activeMCPCallId);

        } else if (event instanceof ServerEventResponseMcpCallArgumentsDone) {
            handleMCPCallArgumentsDone((ServerEventResponseMcpCallArgumentsDone) event);

        } else if (event instanceof ServerEventResponseMcpCallCompleted) {
            handleMCPCallCompleted(session, (ServerEventResponseMcpCallCompleted) event);

        } else if (eventType == ServerEventType.RESPONSE_MCP_CALL_FAILED) {
            System.err.println("❌ MCP call failed");

        } else if (eventType == ServerEventType.ERROR) {
            System.err.println("❌ Error event received");
        }
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
    private static void handleConversationItemCreated(
        VoiceLiveSessionAsyncClient session,
        SessionUpdateConversationItemCreated itemCreated,
        AtomicReference<String> activeMCPCallId
    ) {
        if (itemCreated.getItem() == null) {
            return;
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
            handleMCPApprovalRequest(session, (ResponseMCPApprovalRequestItem) itemCreated.getItem());
        }
    }

    /**
     * Handle MCP approval request events.
     */
    private static void handleMCPApprovalRequest(
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

        // Send approval response
        @SuppressWarnings("unused")
        MCPApprovalResponseRequestItem approvalResponse =
            new MCPApprovalResponseRequestItem(approvalId, approved);

        session.sendEvent(new ClientEventConversationItemCreate().setItem(approvalResponse))
            .doOnSuccess(v -> {
                if (approved) {
                    System.out.println("✅ MCP call approved and response sent");
                } else {
                    System.out.println("❌ MCP call denied and response sent");
                }
            })
            .doOnError(error -> System.err.println("❌ Error sending approval response: " + error.getMessage()))
            .subscribe();
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
    private static void handleMCPCallCompleted(
        VoiceLiveSessionAsyncClient session,
        ServerEventResponseMcpCallCompleted event
    ) {
        System.out.println("✅ MCP call completed for item: " + event.getItemId());

        // Wait for output item done event to get the results
        // In a real implementation, you might want to collect the output
        // For this sample, we'll just trigger a new response
        session.sendEvent(new ClientEventResponseCreate())
            .doOnSuccess(v -> System.out.println("📤 New response created to process MCP output"))
            .doOnError(error -> System.err.println("❌ Error creating response: " + error.getMessage()))
            .subscribe();
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

        private TargetDataLine microphone;
        private final AtomicBoolean isCapturing = new AtomicBoolean(false);

        private SourceDataLine speaker;
        private final BlockingQueue<byte[]> playbackQueue = new LinkedBlockingQueue<>();
        private final AtomicBoolean isPlaying = new AtomicBoolean(false);

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
                new Thread(() -> {
                    byte[] buffer = new byte[CHUNK_SIZE];
                    while (isCapturing.get()) {
                        int bytesRead = microphone.read(buffer, 0, buffer.length);
                        if (bytesRead > 0) {
                            byte[] audioData = Arrays.copyOf(buffer, bytesRead);
                            session.sendInputAudio(BinaryData.fromBytes(audioData)).subscribe();
                        }
                    }
                }, "AudioCapture").start();

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
                new Thread(() -> {
                    while (isPlaying.get()) {
                        try {
                            byte[] audioData = playbackQueue.poll();
                            if (audioData != null) {
                                speaker.write(audioData, 0, audioData.length);
                            } else {
                                Thread.sleep(10);
                            }
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }, "AudioPlayback").start();

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
                playbackQueue.offer(audioData);
            }
        }

        void cleanup() {
            isCapturing.set(false);
            isPlaying.set(false);

            if (microphone != null) {
                microphone.stop();
                microphone.close();
            }

            if (speaker != null) {
                speaker.drain();
                speaker.stop();
                speaker.close();
            }

            playbackQueue.clear();
        }
    }
}
