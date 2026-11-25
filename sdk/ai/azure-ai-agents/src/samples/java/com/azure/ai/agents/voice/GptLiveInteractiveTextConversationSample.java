// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionClient;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import reactor.core.scheduler.Schedulers;

/**
 * Advanced synchronous console conversation with spoken greetings, speaker playback, and frontend transcripts.
 * Uses the same environment variables as {@link GptLiveTextConversationSample}.
 * Silent PCM input keeps the voice session active while typing and consumes voice-session usage.
 * Greeting and transcript boundaries use a one-second speech-inactivity heuristic, not a service completion event.
 * Blank input, EOF, exit, or quit requests graceful closure. A noninterruptible console read may require Enter
 * after remote closure. Standard input is not closed. The standalone main shuts down shared Reactor schedulers.
 */
public class GptLiveInteractiveTextConversationSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = GptLiveSampleUtils.requireSetting(configuration, "FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("FOUNDRY_GPT_LIVE_MODEL must not be blank.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("FOUNDRY_GPT_LIVE_BACKEND_MODEL must not be blank.");
        }
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));
        boolean store = Boolean.parseBoolean(configuration.get("FOUNDRY_GPT_LIVE_STORE", "false"));
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build()).allowPreview(true);
        Console console = System.console();
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "interactive", false, keepAgent);
            BetaVoiceAgentsLiveWebSocketSessionClient session = builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store));
            VoiceAgentRealtimeSampleUtils.SpeakerPlayer player = new VoiceAgentRealtimeSampleUtils.SpeakerPlayer()) {
            runInteractiveConversation(session, console == null ? new InputStreamReader(System.in) : console.reader(),
                player::play, player::drain);
            player.drain();
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static void runInteractiveConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        System.out.println("Type a message and press Enter. Blank line, 'exit' or 'quit' ends the session.");
        System.out.println("Interactive voice streams silent audio while you type; voice usage is billable.");
        BufferedReader console = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
        ExecutorService workers = Executors.newFixedThreadPool(2, task -> {
            Thread thread = new Thread(task, "gpt-live-interactive-text");
            thread.setDaemon(true);
            return thread;
        });
        BlockingQueue<ServerEvent> pending = new LinkedBlockingQueue<>(256);
        AtomicReference<RuntimeException> receiveFailure = new AtomicReference<>();
        AtomicBoolean receiveEnded = new AtomicBoolean();
        GptLiveSampleUtils.AudioTranscriptPrinter transcripts
            = new GptLiveSampleUtils.AudioTranscriptPrinter(System.console() != null, true);
        Future<?> receiver = workers.submit(() -> {
            try {
                for (ServerEvent event : session.receiveEvents(GptLiveSampleUtils.TIMEOUT)) {
                    pending.put(event);
                    if (event.isSessionClosed()) {
                        break;
                    }
                }
            } catch (InterruptedException error) {
                Thread.currentThread().interrupt();
            } catch (RuntimeException error) {
                receiveFailure.set(error);
            } finally {
                receiveEnded.set(true);
            }
        });
        Future<String> prompt = null;
        boolean started = false;
        boolean awaitingResponse = false;
        boolean closing = false;
        boolean greetingComplete = false;
        boolean greetingAudio = false;
        boolean greetingTranscript = false;
        long deadline = System.nanoTime() + GptLiveSampleUtils.TIMEOUT.toNanos();
        long nextAudio = 0;
        long quietSince = 0;
        ClientEvent silence = GptLiveSampleUtils.audio(new byte[960]);
        try {
            while (true) {
                ServerEvent event = pending.poll(20, TimeUnit.MILLISECONDS);
                long now = System.nanoTime();
                if (event != null) {
                    transcripts.print(event);
                    if (event.isSessionOutputAudioDelta()) {
                        byte[] pcm = GptLiveSampleUtils.decodeAudio(event);
                        playAudio.accept(pcm);
                        if (GptLiveSampleUtils.hasSpeech(pcm)) {
                            greetingAudio = true;
                            quietSince = now;
                        }
                    } else if (event.isSessionOutputTranscriptDelta()
                        && !event.asSessionOutputTranscriptDelta().delta().trim().isEmpty()) {
                        greetingTranscript = true;
                        quietSince = now;
                    }
                    if (event.isSessionStarted()) {
                        if (started) {
                            throw new IllegalStateException("Duplicate session.started event.");
                        }
                        started = true;
                        session.sendEvent(silence);
                        nextAudio = now + TimeUnit.MILLISECONDS.toNanos(20);
                        session.sendEvent(GptLiveSampleUtils.commentary("Hi, how can I help you today?"));
                    } else if (event.isResponseEvent()) {
                        ResponseStreamEvent response = GptLiveSampleUtils.backendEvent(event);
                        if (response.isCompleted()) {
                            if (!started || (awaitingResponse && closing)) {
                                throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                            }
                            awaitingResponse = false;
                        }
                    } else if (event.isSessionClosed()) {
                        String reason = event.asSessionClosed().reason().toString();
                        if (!(started && "remote_hangup".equals(reason))
                            && !(closing && "close_requested".equals(reason))) {
                            throw new IllegalStateException("GPT Live closed before all turns completed. Event: "
                                + event + "; transport close code=" + session.getCloseCode()
                                + ", reason=" + session.getCloseReason());
                        }
                        System.out.println("Live session closed (" + reason + ").");
                        return;
                    }
                } else if (receiveEnded.get()) {
                    if (receiveFailure.get() != null) {
                        throw receiveFailure.get();
                    }
                    throw new IllegalStateException("GPT Live disconnected without session.closed.");
                }
                if (started && !closing && now >= nextAudio) {
                    session.sendEvent(silence);
                    nextAudio = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(20);
                }
                if (greetingAudio && greetingTranscript && quietSince != 0
                    && now - quietSince >= TimeUnit.SECONDS.toNanos(1)) {
                    transcripts.endUtterance();
                    quietSince = 0;
                    if (!greetingComplete) {
                        drainAudio.run();
                        greetingComplete = true;
                    }
                }
                boolean canReadPrompt = started && greetingComplete && !awaitingResponse && !closing;
                if (canReadPrompt && prompt == null) {
                    prompt = workers.submit(() -> GptLiveSampleUtils.readPromptBlocking(console, transcripts));
                }
                if (prompt != null && prompt.isDone()) {
                    String text = prompt.get();
                    prompt = null;
                    deadline = System.nanoTime() + GptLiveSampleUtils.TIMEOUT.toNanos();
                    if (text == null) {
                        closing = true;
                        session.sendEvent(GptLiveSampleUtils.closeSession());
                    } else {
                        awaitingResponse = true;
                        session.sendEvent(GptLiveSampleUtils.userMessage(text));
                        session.sendEvent(GptLiveSampleUtils.responseCreate());
                    }
                }
                if (!canReadPrompt && now >= deadline) {
                    throw new IllegalStateException("Timed out waiting for the GPT Live conversation.");
                }
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("GPT Live conversation interrupted.", error);
        } catch (ExecutionException error) {
            if (error.getCause() instanceof RuntimeException) {
                throw (RuntimeException) error.getCause();
            }
            throw new IllegalStateException("Unable to read console input.", error.getCause());
        } finally {
            receiver.cancel(true);
            if (prompt != null) {
                prompt.cancel(true);
            }
            transcripts.endInput();
            workers.shutdownNow();
        }
    }
}
