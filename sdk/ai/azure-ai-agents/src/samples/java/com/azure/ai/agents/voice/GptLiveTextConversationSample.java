// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.BetaVoiceAgentsLiveWebSocketSessionClient;
import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import com.openai.models.responses.ResponseStreamEvent;
import java.io.Console;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import reactor.core.scheduler.Schedulers;
import java.io.BufferedReader;
import java.io.IOException;
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
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * Runs a synchronous, multi-turn GPT Live text conversation using Responses delegation.
 * Type messages interactively; a blank line, exit or quit ends the session.
 * Alternatively, pass prompts as command-line arguments for a scripted conversation.
 * Interactive mode plays a spoken greeting and replies, and prints frontend transcripts instead of backend notes.
 * It streams silent PCM while typing; voice usage is billable. Greeting completion uses the same speech-inactivity
 * heuristic and speaker drain as the asynchronous sample. Assistant transcript fragments are assembled into
 * utterances independently of typed turns; completed text waits until Enter when a You: line is active.
 * Synchronous session I/O runs on workers coordinated by this sample.
 * After a remote disconnect, a pending console read may require Enter to release the input worker.
 * Scripted command-line prompts print backend text without speaker playback.
 * A uniquely named agent is deleted on exit unless FOUNDRY_KEEP_GPT_LIVE_AGENT=true.
 * The standalone main method shuts down shared Reactor schedulers on exit; do not invoke it inside a shared application.
 *
 * <p>Before running the sample, set these environment variables:</p>
 * <ul>
 *   <li>{@code FOUNDRY_PROJECT_ENDPOINT} - The Azure AI Project endpoint.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_MODEL} - Optional. The GPT Live deployment name. Defaults to {@code gpt-live-1}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_BACKEND_MODEL} - Optional. The Responses backend deployment name in the Live
 *   resource. Defaults to {@code gpt-5.6-terra}.</li>
 *   <li>{@code FOUNDRY_GPT_LIVE_STORE} - Optional. Set to {@code true} to persist conversation content and audio
 *   and request a Foundry conversation ID. Defaults to {@code false}. Also keep the agent for later REST reads.</li>
 *   <li>{@code FOUNDRY_KEEP_GPT_LIVE_AGENT} - Optional. Set to {@code true} to keep the agent after the sample.
 *   Defaults to {@code false}.</li>
 * </ul>
 */
public class GptLiveTextConversationSample {
    public static void main(String[] args) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        String endpoint = configuration.get("FOUNDRY_PROJECT_ENDPOINT");
        String model = configuration.get("FOUNDRY_GPT_LIVE_MODEL");
        String backendModel = configuration.get("FOUNDRY_GPT_LIVE_BACKEND_MODEL");
        boolean keepAgent = Boolean.parseBoolean(configuration.get("FOUNDRY_KEEP_GPT_LIVE_AGENT", "false"));
        boolean store = Boolean.parseBoolean(configuration.get("FOUNDRY_GPT_LIVE_STORE", "false"));

        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_PROJECT_ENDPOINT before running this sample.");
        }
        if (model == null) {
            model = "gpt-live-1";
        } else if (model.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_MODEL before running this sample.");
        }
        if (backendModel == null) {
            backendModel = "gpt-5.6-terra";
        } else if (backendModel.trim().isEmpty()) {
            throw new IllegalArgumentException("Set environment variable FOUNDRY_GPT_LIVE_BACKEND_MODEL before running this sample.");
        }

        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(endpoint)
            .credential(new DefaultAzureCredentialBuilder().build())
            .allowPreview(true);
        try (GptLiveSampleUtils.AgentResource agent
            = GptLiveSampleUtils.createAgent(builder, model, backendModel, "text", false, keepAgent);
            BetaVoiceAgentsLiveWebSocketSessionClient session = agent.builder.beta()
                .buildBetaVoiceAgentsLiveWebSocketClient()
                .openWebSocketSession(agent.name, new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store))) {
            if (args.length == 0) {
                Console console = System.console();
                runInteractiveConversation(session,
                    console == null ? new InputStreamReader(System.in) : console.reader());
            } else {
                runConversation(session, Arrays.asList(args));
            }
        } finally {
            Schedulers.shutdownNow();
        }
    }

    static void runConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, List<String> prompts) {
        runConversation(session.receiveEvents(GptLiveSampleUtils.TIMEOUT), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(), prompts);
    }

    static void runConversation(Iterable<ServerEvent> events, Consumer<ClientEvent> sendEvent,
        Supplier<String> closeDetails, List<String> prompts) {
        Iterator<String> pending = prompts.iterator();
        boolean started = false;
        boolean awaitingResponse = false;
        boolean closing = false;
        for (ServerEvent event : events) {
            GptLiveSampleUtils.printEvent(event);
            boolean nextTurn = false;
            if (event.isSessionStarted()) {
                if (started) {
                    throw new IllegalStateException("Duplicate session.started event.");
                }
                started = true;
                nextTurn = true;
            } else if (event.isResponseEvent()) {
                ResponseStreamEvent response = GptLiveSampleUtils.backendEvent(event);
                if (response.isCompleted()) {
                    if (!started || !awaitingResponse || closing) {
                        throw new IllegalStateException("Unexpected GPT Live response lifecycle event.");
                    }
                    awaitingResponse = false;
                    System.out.println();
                    nextTurn = true;
                }
            } else if (event.isSessionClosed()) {
                String reason = event.asSessionClosed().reason().toString();
                if (!(started && "remote_hangup".equals(reason))
                    && !(closing && "close_requested".equals(reason))) {
                    throw new IllegalStateException("GPT Live closed before all turns completed. Event: " + event
                        + "; " + closeDetails.get());
                }
                System.out.println("Live session closed (" + reason + ").");
                return;
            }
            if (nextTurn) {
                if (pending.hasNext()) {
                    String prompt = pending.next();
                    System.out.println("You: " + prompt);
                    System.out.print("Assistant: ");
                    awaitingResponse = true;
                    sendEvent.accept(GptLiveSampleUtils.userMessage(prompt));
                    sendEvent.accept(GptLiveSampleUtils.responseCreate());
                } else {
                    closing = true;
                    sendEvent.accept(GptLiveSampleUtils.closeSession());
                }
            }
        }
        throw new IllegalStateException("GPT Live disconnected without session.closed.");
    }

    static void runInteractiveConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, Reader input) {
        try (VoiceAgentRealtimeSampleUtils.SpeakerPlayer player = new VoiceAgentRealtimeSampleUtils.SpeakerPlayer()) {
            runInteractiveConversation(session, input, player::play, player::drain);
            player.drain();
            System.out.printf("(received %.2fs of reply audio%s)%n", player.getSecondsReceived(),
                player.isEnabled() ? " and played it" : "");
        }
    }

    static void runInteractiveConversation(BetaVoiceAgentsLiveWebSocketSessionClient session, Reader input,
        Consumer<byte[]> playAudio, Runnable drainAudio) {
        System.out.println("Type a message and press Enter. Blank line, 'exit' or 'quit' ends the session.");
        System.out.println("Interactive voice streams silent audio while you type; voice usage is billable.");
        BufferedReader console = input instanceof BufferedReader ? (BufferedReader) input : new BufferedReader(input);
        runConversationCore(session.receiveEvents(GptLiveSampleUtils.TIMEOUT), session::sendEvent,
            () -> "transport close code=" + session.getCloseCode() + ", reason=" + session.getCloseReason(), transcripts -> {
                try {
                    return GptLiveSampleUtils.readPromptBlocking(console, transcripts);
                } catch (IOException error) {
                    throw new IllegalStateException("Unable to read console input.", error);
                }
            }, playAudio, drainAudio, true, () -> { }, backend -> false, reason -> false);
    }

    static void runConversationCore(Iterable<ServerEvent> events, Consumer<ClientEvent> sendEvent,
        Supplier<String> closeDetails, Function<GptLiveSampleUtils.AudioTranscriptPrinter, String> nextPrompt,
        Consumer<byte[]> playAudio, Runnable drainAudio, boolean spoken,
        Runnable onStarted, Predicate<ResponseStreamEvent> handleResponse, Predicate<String> acceptClose) {
        GptLiveSampleUtils.ConversationEventHandler<Boolean> handler
            = new GptLiveSampleUtils.ConversationEventHandler<Boolean>(spoken, closeDetails, acceptClose) {
                @Override
                Boolean onStarted() {
                    onStarted.run();
                    return false;
                }

                @Override
                Boolean onResponse(ResponseStreamEvent event) {
                    boolean continuation = handleResponse.test(event);
                    if (event.isCompleted()) {
                        completeResponse(continuation);
                    }
                    return false;
                }

                @Override
                Boolean onClosed() {
                    return true;
                }

                @Override
                Boolean ignored() {
                    return false;
                }
            };
        ExecutorService workers = Executors.newFixedThreadPool(2, task -> {
            Thread thread = new Thread(task, "gpt-live-sync-conversation");
            thread.setDaemon(true);
            return thread;
        });
        BlockingQueue<ServerEvent> pending = new LinkedBlockingQueue<>(256);
        AtomicReference<RuntimeException> receiveFailure = new AtomicReference<>();
        AtomicBoolean receiveEnded = new AtomicBoolean();
        GptLiveSampleUtils.AudioTranscriptPrinter transcripts = new GptLiveSampleUtils.AudioTranscriptPrinter(System.console() != null, spoken);
        Future<?> receiver = workers.submit(() -> {
            try {
                for (ServerEvent event : events) {
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
        boolean greetingComplete = !spoken;
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
                    if (spoken) {
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
                    } else {
                        GptLiveSampleUtils.printEvent(event);
                    }
                    if (handler.process(event)) {
                        return;
                    }
                    if (event.isSessionStarted()) {
                        if (spoken) {
                            sendEvent.accept(silence);
                            nextAudio = now + TimeUnit.MILLISECONDS.toNanos(20);
                            sendEvent.accept(GptLiveSampleUtils.commentary("Hi, how can I help you today?"));
                        }
                    }
                } else if (receiveEnded.get()) {
                    if (receiveFailure.get() != null) {
                        throw receiveFailure.get();
                    }
                    throw new IllegalStateException("GPT Live disconnected without session.closed.");
                }

                if (handler.canStreamAudio() && now >= nextAudio) {
                    sendEvent.accept(silence);
                    nextAudio = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(20);
                }
                if (spoken && greetingAudio && greetingTranscript && quietSince != 0
                    && now - quietSince >= TimeUnit.SECONDS.toNanos(1)) {
                    transcripts.endUtterance();
                    quietSince = 0;
                    if (!greetingComplete) {
                        drainAudio.run();
                        greetingComplete = true;
                    }
                }
                if (handler.canReadPrompt(greetingComplete) && prompt == null) {
                    prompt = workers.submit(() -> nextPrompt.apply(transcripts));
                }
                if (prompt != null && prompt.isDone()) {
                    String text = prompt.get();
                    prompt = null;
                    deadline = System.nanoTime() + GptLiveSampleUtils.TIMEOUT.toNanos();
                    if (text == null) {
                        handler.requestClose();
                        sendEvent.accept(GptLiveSampleUtils.closeSession());
                    } else {
                        handler.beginResponse();
                        if (!spoken) {
                            System.out.print("Assistant: ");
                        }
                        sendEvent.accept(GptLiveSampleUtils.userMessage(text));
                        sendEvent.accept(GptLiveSampleUtils.responseCreate());
                    }
                }
                if (!handler.canReadPrompt(greetingComplete) && now >= deadline) {
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
