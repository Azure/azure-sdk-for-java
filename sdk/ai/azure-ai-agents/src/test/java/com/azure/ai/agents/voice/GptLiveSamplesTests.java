// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionAsyncClient;
import com.azure.ai.agents.BetaVoiceAgentLiveWebSocketSessionClient;
import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.models.VoiceAgentLiveWebSocketConnectionOptions;
import com.azure.core.credential.AccessToken;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.openai.models.live.ClientEvent;
import com.openai.models.live.ServerEvent;
import java.io.ByteArrayOutputStream;
import java.io.FilterReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.netty.DisposableServer;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ResourceLock("voice-agent-websocket-tls")
@Timeout(20)
public class GptLiveSamplesTests {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final String STARTED = "{\"type\":\"session.started\",\"session\":{\"id\":\"live-1\"}}";
    private static final String CLOSED = "{\"type\":\"session.closed\",\"reason\":\"close_requested\"}";
    private static final String COMPLETED = "{\"type\":\"response.event\",\"delegation_id\":\"del-1\",\"event\":"
        + "{\"type\":\"response.completed\",\"response\":{\"id\":\"resp-1\",\"output\":[]}}}";
    private static final String MODERATED = "{\"type\":\"error\",\"event_id\":\"moderated-1\","
        + "\"error\":{\"type\":\"content_filter\",\"code\":\"content_filter\","
        + "\"message\":\"The generation was stopped due to moderation.\"}}";
    private final List<Map<?, ?>> commands = new CopyOnWriteArrayList<>();
    private DisposableServer server;

    @ParameterizedTest
    @ValueSource(strings = { "text", "text-async", "audio-async", "tools" })
    public void agentNamesStayBelowServiceLimit(String scenario) {
        String prefix = "gpt-live-" + scenario + "-";
        String name = GptLiveSampleUtils.createAgentName(scenario);
        assertTrue(name.startsWith(prefix));
        assertTrue(name.length() <= 57, "Sample agent names must leave headroom below the 63-character limit");
        assertTrue(name.matches("[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9]"));
        assertEquals(36, name.substring(prefix.length()).length());
        assertEquals(4, UUID.fromString(name.substring(prefix.length())).version());
        assertFalse(name.equals(GptLiveSampleUtils.createAgentName(scenario)));
    }

    @Test
    @ResourceLock("java.lang.System.out")
    public void audioTranscriptsLabelSpeakersWithoutPrintingBackendText() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            GptLiveSampleUtils.AudioTranscriptPrinter printer = new GptLiveSampleUtils.AudioTranscriptPrinter();
            printer.print(typedEvent(BinaryData.fromString(STARTED)));
            printer.print(typedEvent(GptLiveSampleUtils.event("session.input_transcript.delta", "delta", "Hello")));
            printer.print(typedEvent(GptLiveSampleUtils.event("session.input_transcript.delta", "delta", " there")));
            printer.print(typedEvent(BinaryData.fromString("{\"type\":\"response.event\","
                + "\"event\":{\"type\":\"response.output_text.delta\",\"delta\":\"backend-only text\"}}")));
            printer.print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "Hi!")));
            printer.beginTurn();
            printer.print(
                typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "How can I help?")));
        } finally {
            System.setOut(original);
        }
        String transcript = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertEquals("Live session started; conversation_id not provided (persistence may be disabled)."
            + System.lineSeparator() + "You: Hello there" + System.lineSeparator() + "Assistant: Hi!"
            + System.lineSeparator() + "Assistant: How can I help?", transcript);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void startupPrintsFoundryConversationIdInScriptedAndInteractiveModes(boolean interactive) {
        ServerEvent event = typedEvent(BinaryData.fromString(
            "{\"type\":\"session.started\",\"conversation_id\":\"conversation-1\",\"session\":{\"id\":\"native-1\"}}"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            if (interactive) {
                new GptLiveSampleUtils.AudioTranscriptPrinter(false, true).print(event);
            } else {
                GptLiveSampleUtils.printEvent(event);
            }
        } finally {
            System.setOut(original);
        }
        assertEquals("Live session started; conversation_id=conversation-1" + System.lineSeparator(),
            new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void startupWithoutPersistenceDoesNotSubstituteNativeSessionId(boolean interactive) {
        ServerEvent event = typedEvent(BinaryData.fromString(STARTED));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            if (interactive) {
                new GptLiveSampleUtils.AudioTranscriptPrinter(false, true).print(event);
            } else {
                GptLiveSampleUtils.printEvent(event);
            }
        } finally {
            System.setOut(original);
        }
        assertEquals("Live session started; conversation_id not provided (persistence may be disabled)."
            + System.lineSeparator(), new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    @ResourceLock("java.lang.System.out")
    public void transcriptBufferIsBoundedWhileReadingInput() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        GptLiveSampleUtils.AudioTranscriptPrinter printer = new GptLiveSampleUtils.AudioTranscriptPrinter();
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            printer.print(
                typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "Previous response.")));
            printer.beginInput();
            assertTrue(new String(output.toByteArray(), StandardCharsets.UTF_8)
                .endsWith("Previous response." + System.lineSeparator() + "You: "));
            int promptLength = output.size();
            char[] characters = new char[GptLiveSampleUtils.AudioTranscriptPrinter.MAX_BUFFERED_TRANSCRIPT_CHARACTERS
                - "Assistant: ".length()];
            Arrays.fill(characters, 'a');
            printer.print(typedEvent(
                GptLiveSampleUtils.event("session.output_transcript.delta", "delta", new String(characters))));
            assertEquals(promptLength, output.size(), "Transcript output must not overwrite the input line");
            IllegalStateException error = assertThrows(IllegalStateException.class, () -> printer
                .print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "x"))));
            assertTrue(error.getMessage().contains("65536"));
            printer.endInput();
            assertTrue(output.size() > promptLength, "Buffered text must be flushed after input ends");
        } finally {
            System.setOut(original);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    @ResourceLock("java.lang.System.out")
    public void consoleTranscriptUsesSingleLineBreaks(boolean inputEchoed) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            GptLiveSampleUtils.AudioTranscriptPrinter printer
                = new GptLiveSampleUtils.AudioTranscriptPrinter(inputEchoed);
            printer.print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "Welcome.")));
            printer.beginInput();
            if (inputEchoed) {
                System.out.println("testing");
            }
            printer.print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "Reply.")));
            printer.endInput();
            printer.beginInput();
            if (inputEchoed) {
                System.out.println("exit");
            }
            printer.endInput();
            printer.print(typedEvent(BinaryData.fromString(CLOSED)));
            System.out.println("Live session closed.");
        } finally {
            System.setOut(original);
        }
        String newline = System.lineSeparator();
        assertEquals(
            "Assistant: Welcome." + newline + "You: " + (inputEchoed ? "testing" : "") + newline + "Assistant: Reply."
                + newline + "You: " + (inputEchoed ? "exit" : "") + newline + "Live session closed." + newline,
            new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    @ResourceLock("java.lang.System.out")
    public void partialUtteranceSurvivesInputSubmissionWithoutBeingPrinted() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            GptLiveSampleUtils.AudioTranscriptPrinter printer
                = new GptLiveSampleUtils.AudioTranscriptPrinter(false, true);
            printer.beginInput();
            printer.print(
                typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "This sentence ")));
            printer.endInput();
            assertFalse(new String(output.toByteArray(), StandardCharsets.UTF_8).contains("This sentence"));
            printer.beginTurn();
            printer.beginInput();
            printer.print(typedEvent(
                GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "continues after Enter.")));
            printer.endUtterance();
            assertTrue(new String(output.toByteArray(), StandardCharsets.UTF_8).endsWith("You: "));
            printer.endInput();
        } finally {
            System.setOut(original);
        }
        String newline = System.lineSeparator();
        assertEquals(
            "You: " + newline + "You: " + newline + "Assistant: This sentence continues after Enter." + newline,
            new String(output.toByteArray(), StandardCharsets.UTF_8));
    }

    @Test
    @ResourceLock("java.lang.System.out")
    public void completedAndPartialUtterancesShareTheTranscriptBufferLimit() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            GptLiveSampleUtils.AudioTranscriptPrinter printer
                = new GptLiveSampleUtils.AudioTranscriptPrinter(false, true);
            printer.beginInput();
            printer
                .print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "Completed.")));
            printer.endUtterance();
            int queuedLength = ("Assistant: Completed." + System.lineSeparator()).length();
            char[] characters
                = new char[GptLiveSampleUtils.AudioTranscriptPrinter.MAX_BUFFERED_TRANSCRIPT_CHARACTERS - queuedLength];
            Arrays.fill(characters, 'a');
            printer.print(typedEvent(
                GptLiveSampleUtils.event("session.output_transcript.delta", "delta", new String(characters))));
            assertThrows(IllegalStateException.class, () -> printer
                .print(typedEvent(GptLiveSampleUtils.event("session.output_transcript.delta", "delta", "x"))));
            printer.endInput();
            assertTrue(new String(output.toByteArray(), StandardCharsets.UTF_8).contains("Assistant: Completed."));
            assertFalse(new String(output.toByteArray(), StandardCharsets.UTF_8).contains("aaaa"));
        } finally {
            System.setOut(original);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void spokenSentenceIsNotSplitByBackendCompletionOrAnotherTypedTurn(boolean async) {
        AtomicInteger responses = new AtomicInteger();
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound, outbound) -> outbound
                .sendString(Flux.concat(Mono.just(STARTED), inbound.receive().asString().concatMap(json -> {
                    Map<?, ?> command = GptLiveSampleUtils.object(BinaryData.fromString(json));
                    commands.add(command);
                    if ("session.commentary.append".equals(command.get("type"))) {
                        return Flux.just("{\"type\":\"session.output_transcript.delta\",\"delta\":\"Welcome.\"}",
                            "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}");
                    }
                    if ("response.create".equals(command.get("type"))) {
                        String fragment
                            = responses.incrementAndGet() == 1 ? "This sentence " : "continues across typed turns.";
                        return Flux.just(
                            GptLiveSampleUtils.event("session.output_transcript.delta", "delta", fragment).toString(),
                            "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}", COMPLETED);
                    }
                    return "session.close".equals(command.get("type")) ? Flux.just(CLOSED) : Flux.empty();
                })))
                .then()))
            .bindNow();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            runInteractiveConversation(async, new StringReader("one\ntwo\nexit\n"));
        } finally {
            System.setOut(original);
        }
        assertEquals(2, responses.get());
        String transcript = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Assistant: This sentence continues across typed turns."));
        assertFalse(transcript.contains("Assistant: continues"));
        assertEquals(2, transcript.split("Assistant: ", -1).length - 1);
        assertEquals(3, transcript.split("You: ", -1).length - 1, "Input must remain available between backend turns");
    }

    @Test
    @ResourceLock("java.lang.System.out")
    public void unknownLiveEventsDoNotInterruptTranscriptHandlers() {
        ServerEvent event = typedEvent(GptLiveSampleUtils.event("session.future_event", "value", "future"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            GptLiveSampleUtils.printEvent(event);
            new GptLiveSampleUtils.AudioTranscriptPrinter().print(event);
        } finally {
            System.setOut(original);
        }
        assertEquals(0, output.size());
    }

    @ParameterizedTest
    @ValueSource(strings = { "response.failed", "response.incomplete" })
    public void responseFailuresAreReportedForUnknownAndDelegatedEvents(String type) {
        BinaryData failure = GptLiveSampleUtils.event(type, "event_id", "failure-1");
        for (boolean delegated : new boolean[] { false, true }) {
            Map<String, Object> wrapper = new LinkedHashMap<>();
            wrapper.put("type", "response.event");
            wrapper.put("event", GptLiveSampleUtils.object(failure));
            ServerEvent event = typedEvent(delegated ? BinaryData.fromObject(wrapper) : failure);
            IllegalStateException error
                = assertThrows(IllegalStateException.class, () -> GptLiveSampleUtils.printEvent(event));
            assertTrue(error.getMessage().contains(type));
            assertTrue(error.getMessage().contains("failure-1"));
            assertThrows(IllegalStateException.class,
                () -> new GptLiveSampleUtils.AudioTranscriptPrinter().print(event));
        }
    }

    @Test
    public void moderationIsReportedExplicitlyAndPreservesServiceDiagnostics() {
        Map<?, ?> event = GptLiveSampleUtils.object(BinaryData.fromString(MODERATED));
        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> new GptLiveSampleUtils.AudioTranscriptPrinter().print(typedEvent(BinaryData.fromString(MODERATED))));
        assertTrue(error.getMessage().contains("generation stopped by service moderation"));
        assertTrue(error.getMessage().contains("content_filter"));
        assertTrue(error.getMessage().contains("moderated-1"));
        Map<String, Object> wrapper = new java.util.LinkedHashMap<>();
        wrapper.put("type", "response.event");
        wrapper.put("event", event);
        assertThrows(IllegalStateException.class,
            () -> new GptLiveSampleUtils.AudioTranscriptPrinter().print(typedEvent(BinaryData.fromObject(wrapper))));
    }

    @ParameterizedTest
    @MethodSource("invalidSampleSettingCases")
    @ResourceLock("com.azure.core.util.Configuration")
    public void examplesValidateSettingsBeforeCreatingClients(String sampleName, Consumer<String[]> main,
        String setting, String value) {
        Configuration configuration = Configuration.getGlobalConfiguration();
        Map<String, String> original = new LinkedHashMap<>();
        for (String name : Arrays.asList("FOUNDRY_PROJECT_ENDPOINT", "FOUNDRY_GPT_LIVE_MODEL",
            "FOUNDRY_GPT_LIVE_BACKEND_MODEL")) {
            original.put(name, configuration.get(name));
        }
        try {
            configuration.put("FOUNDRY_PROJECT_ENDPOINT", "not-an-endpoint");
            configuration.put("FOUNDRY_GPT_LIVE_MODEL", "gpt-live-1");
            configuration.put("FOUNDRY_GPT_LIVE_BACKEND_MODEL", "gpt-5.6-terra");
            configuration.put(setting, value);
            IllegalArgumentException error
                = assertThrows(IllegalArgumentException.class, () -> main.accept(new String[0]), sampleName);
            assertTrue(error.getMessage().contains(setting));
        } finally {
            original.forEach((name, previous) -> {
                if (previous == null) {
                    configuration.remove(name);
                } else {
                    configuration.put(name, previous);
                }
            });
        }
    }

    private static Stream<Arguments> invalidSampleSettingCases() {
        Map<String, Consumer<String[]>> samples = new LinkedHashMap<>();
        samples.put("audio-async", GptLiveAudioConversationAsyncSample::main);
        samples.put("text", GptLiveTextConversationSample::main);
        samples.put("text-async", GptLiveTextConversationAsyncSample::main);
        samples.put("tools", GptLiveFunctionToolSample::main);
        return samples.entrySet()
            .stream()
            .flatMap(sample -> Stream
                .of("FOUNDRY_PROJECT_ENDPOINT", "FOUNDRY_GPT_LIVE_MODEL", "FOUNDRY_GPT_LIVE_BACKEND_MODEL")
                .flatMap(setting -> Stream.of("", "   ")
                    .map(value -> Arguments.of(sample.getKey(), sample.getValue(), setting, value))));
    }

    @BeforeAll
    static void installTlsCertificate() {
        VoiceAgentWebSocketSessionTests.installTlsCertificate();
    }

    @AfterAll
    static void restoreTlsConfiguration() {
        VoiceAgentWebSocketSessionTests.restoreTlsConfiguration();
    }

    @AfterEach
    void closeServer() {
        if (server != null) {
            server.disposeNow();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void textSamplesWaitForStartupAndCompleteMultipleTurns(boolean async) {
        startTextServer(false, false);
        if (async) {
            Mono.usingWhen(builder().beta().buildBetaVoiceAgentLiveWebSocketAsyncClient().openWebSocketSession("agent"),
                session -> GptLiveSampleUtils.runConversation(session, Arrays.asList("one", "two")),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block(TIMEOUT);
        } else {
            try (BetaVoiceAgentLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
                GptLiveSampleUtils.runConversation(session, Arrays.asList("one", "two"));
            }
        }
        List<String> expected = Arrays.asList("response.item.create", "response.create", "response.item.create",
            "response.create", "session.close");
        assertEquals(expected, commandTypes());
        Map<?, ?> item = GptLiveSampleUtils.object(commands.get(0).get("item"));
        assertEquals("user", item.get("role"));
        assertEquals("message", item.get("type"));
        assertEquals("one", GptLiveSampleUtils.object(((List<?>) item.get("content")).get(0)).get("text"));
    }

    @ParameterizedTest
    @MethodSource("interactiveExitCases")
    public void interactiveTextSamplesReadMultipleTurnsAndCloseOnExit(boolean async, String exit) {
        startTextServer(false, false);
        AtomicBoolean inputClosed = new AtomicBoolean();
        Reader input = new StringReader("one\r\ntwo\n" + exit) {
            @Override
            public void close() {
                inputClosed.set(true);
                super.close();
            }
        };
        runInteractiveConversation(async, input);
        assertEquals(Arrays.asList("session.commentary.append", "response.item.create", "response.create",
            "response.item.create", "response.create", "session.close"), commandsWithoutInputAudio());
        List<String> sentPrompts = commands.stream()
            .filter(command -> "response.item.create".equals(command.get("type")))
            .map(command -> GptLiveSampleUtils.object(command.get("item")))
            .map(item -> GptLiveSampleUtils.object(((List<?>) item.get("content")).get(0)))
            .map(part -> GptLiveSampleUtils.string(part, "text"))
            .collect(java.util.stream.Collectors.toList());
        assertEquals(Arrays.asList("one", "two"), sentPrompts);
        assertFalse(inputClosed.get(), "Conversation cleanup must not close standard input");
    }

    @ParameterizedTest
    @MethodSource("interactiveExitCases")
    public void interactiveTextSamplesCanExitWithoutSendingAPrompt(boolean async, String text) {
        startTextServer(false, false);
        runInteractiveConversation(async, new StringReader(text));
        assertEquals(Arrays.asList("session.commentary.append", "session.close"), commandsWithoutInputAudio());
    }

    private static Stream<Arguments> interactiveExitCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of("", "\n", "exit\n", " QuIt \r\n").map(exit -> Arguments.of(async, exit)));
    }

    private List<String> commandsWithoutInputAudio() {
        return commandTypes().stream()
            .filter(type -> !"session.input_audio.append".equals(type))
            .collect(java.util.stream.Collectors.toList());
    }

    private void runInteractiveConversation(boolean async, Reader input) {
        runInteractiveConversation(async, input, pcm -> {
        }, () -> {
        });
    }

    private void runInteractiveConversation(boolean async, Reader input, Consumer<byte[]> playAudio,
        Runnable drainAudio) {
        if (async) {
            Mono.usingWhen(builder().beta().buildBetaVoiceAgentLiveWebSocketAsyncClient().openWebSocketSession("agent"),
                session -> GptLiveSampleUtils.runConversation(session, input, playAudio, drainAudio),
                BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync).block(TIMEOUT);
        } else {
            try (BetaVoiceAgentLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
                GptLiveSampleUtils.runConversation(session, input, playAudio, drainAudio);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interactiveTextSampleReceivesServiceErrorsWhileWaitingForInput(boolean async) {
        Sinks.Empty<Void> inputStarted = Sinks.empty();
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound, outbound) -> outbound
                .sendString(Flux.concat(Mono.just(STARTED), inbound.receive().asString().concatMap(json -> {
                    Map<?, ?> command = GptLiveSampleUtils.object(BinaryData.fromString(json));
                    return "session.commentary.append".equals(command.get("type"))
                        ? Flux.just("{\"type\":\"session.output_transcript.delta\",\"delta\":\"Welcome.\"}",
                            "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}")
                        : Flux.<String>empty();
                })).mergeWith(inputStarted.asMono().thenReturn(MODERATED)))
                .then()))
            .bindNow();
        CountDownLatch releaseRead = new CountDownLatch(1);
        Reader idleInput = new Reader() {
            @Override
            public boolean ready() {
                throw new AssertionError("Console input must use a blocking line read, not readiness polling");
            }

            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                inputStarted.tryEmitEmpty();
                try {
                    releaseRead.await();
                    return -1;
                } catch (InterruptedException error) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Console read interrupted", error);
                }
            }

            @Override
            public void close() {
                throw new AssertionError("Standard input must not be closed");
            }
        };
        try {
            StepVerifier
                .create(Mono.<Void>fromRunnable(() -> runInteractiveConversation(async, idleInput))
                    .subscribeOn(Schedulers.boundedElastic()))
                .expectErrorMatches(
                    error -> error instanceof IllegalStateException && error.getMessage().contains("content_filter"))
                .verify(TIMEOUT);
        } finally {
            releaseRead.countDown();
        }
    }

    @Test
    public void consoleReadFailuresPropagate() {
        Reader input = new StringReader("") {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw new IOException("Console unavailable");
            }
        };
        StepVerifier.create(GptLiveSampleUtils.readPrompt(input))
            .expectErrorMatches(
                error -> error instanceof IOException && "Console unavailable".equals(error.getMessage()))
            .verify(TIMEOUT);
    }

    @Test
    public void consoleInputIsSubmittedOnlyAfterEnterWithoutLosingCharacters() throws Exception {
        try (PipedReader input = new PipedReader(); PipedWriter keyboard = new PipedWriter(input)) {
            CompletableFuture<String> prompt = GptLiveSampleUtils.readPrompt(input).toFuture();
            try {
                keyboard.write("testtesttes testing");
                keyboard.flush();
                assertThrows(TimeoutException.class, () -> prompt.get(200, TimeUnit.MILLISECONDS));
                keyboard.write("\r\n");
                keyboard.flush();
                assertEquals("testtesttes testing", prompt.get(5, TimeUnit.SECONDS));
            } finally {
                prompt.cancel(true);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void greetingIsPrintedAndPlayedBeforeReadingInput(boolean async) throws Exception {
        Sinks.Empty<Void> allowGreeting = Sinks.empty();
        CountDownLatch greetingRequested = new CountDownLatch(1);
        CountDownLatch inputStarted = new CountDownLatch(1);
        startTextServer(false, false, Mono.fromRunnable(greetingRequested::countDown).then(allowGreeting.asMono()));
        CountDownLatch greetingPlayed = new CountDownLatch(1);
        CountDownLatch replyPlayed = new CountDownLatch(1);
        CountDownLatch drainStarted = new CountDownLatch(1);
        CountDownLatch allowDrain = new CountDownLatch(1);
        AtomicBoolean drained = new AtomicBoolean();
        AtomicInteger played = new AtomicInteger();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PipedReader pipe = new PipedReader();
            PipedWriter keyboard = new PipedWriter(pipe);
            PrintStream capture = new PrintStream(output)) {
            Reader input = new FilterReader(pipe) {
                @Override
                public int read(char[] buffer, int offset, int length) throws IOException {
                    assertTrue(drained.get(), "Queued greeting audio must finish before input begins");
                    inputStarted.countDown();
                    return super.read(buffer, offset, length);
                }
            };
            System.setOut(capture);
            CompletableFuture<Void> conversation
                = Mono.<Void>fromRunnable(() -> runInteractiveConversation(async, input, pcm -> {
                    if (played.incrementAndGet() == 1) {
                        greetingPlayed.countDown();
                    } else {
                        replyPlayed.countDown();
                    }
                }, () -> {
                    drainStarted.countDown();
                    try {
                        assertTrue(allowDrain.await(5, TimeUnit.SECONDS));
                    } catch (InterruptedException error) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(error);
                    }
                    drained.set(true);
                })).subscribeOn(Schedulers.boundedElastic()).toFuture();
            try {
                assertTrue(greetingRequested.await(5, TimeUnit.SECONDS));
                assertFalse(inputStarted.await(200, TimeUnit.MILLISECONDS));
                allowGreeting.tryEmitEmpty();
                assertTrue(greetingPlayed.await(5, TimeUnit.SECONDS));
                assertFalse(inputStarted.await(200, TimeUnit.MILLISECONDS),
                    "The quiet period must elapse before prompting");
                assertTrue(drainStarted.await(5, TimeUnit.SECONDS));
                assertFalse(inputStarted.await(200, TimeUnit.MILLISECONDS),
                    "The first prompt must wait for queued speaker playback");
                allowDrain.countDown();
                assertTrue(inputStarted.await(5, TimeUnit.SECONDS));
                String whileTyping = new String(output.toByteArray(), StandardCharsets.UTF_8);
                assertTrue(whileTyping.endsWith("You: "));
                assertTrue(whileTyping.contains("Assistant: Welcome." + System.lineSeparator() + "You: "));
                keyboard.write("testing\n");
                keyboard.flush();
                assertTrue(replyPlayed.await(5, TimeUnit.SECONDS));
                keyboard.write("exit\n");
                keyboard.flush();
                conversation.get(5, TimeUnit.SECONDS);
                String transcript = new String(output.toByteArray(), StandardCharsets.UTF_8);
                assertTrue(transcript.indexOf("Assistant: Welcome.") < transcript.indexOf("You: "));
                assertTrue(transcript.contains("Spoken reply."));
                assertFalse(transcript.contains(System.lineSeparator() + System.lineSeparator()));
            } finally {
                allowDrain.countDown();
                conversation.cancel(true);
            }
        } finally {
            System.setOut(original);
        }
    }

    @Test
    public void greetingWaitsForBothOutputsAndRestartsQuietPeriodForEachChunk() {
        AtomicReference<GptLiveSampleUtils.Greeting> greeting = new AtomicReference<>();
        AtomicBoolean drained = new AtomicBoolean();
        StepVerifier.withVirtualTime(() -> {
            GptLiveSampleUtils.Greeting pending = new GptLiveSampleUtils.Greeting();
            greeting.set(pending);
            return pending.awaitPlayback(() -> drained.set(true));
        })
            .expectSubscription()
            .then(() -> greeting.get().onTranscript())
            .expectNoEvent(Duration.ofSeconds(2))
            .then(() -> greeting.get().onAudio(new byte[] { 1, 2 }))
            .expectNoEvent(Duration.ofMillis(750))
            .then(() -> greeting.get().onAudio(new byte[] { 1, 2 }))
            .expectNoEvent(Duration.ofMillis(750))
            .then(() -> greeting.get().onTranscript())
            .expectNoEvent(Duration.ofMillis(999))
            .then(() -> assertFalse(drained.get()))
            .thenAwait(Duration.ofMillis(1))
            .verifyComplete();
        assertTrue(drained.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "session.output_transcript.delta", "session.output_audio.delta" })
    public void missingGreetingOutputTimesOutInsteadOfStartingInput(String type) {
        AtomicBoolean drained = new AtomicBoolean();
        StepVerifier.withVirtualTime(() -> {
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            if ("session.output_audio.delta".equals(type)) {
                greeting.onAudio(new byte[] { 1, 2 });
            } else {
                greeting.onTranscript();
            }
            return greeting.awaitPlayback(() -> drained.set(true));
        }).expectSubscription().thenAwait(GptLiveSampleUtils.TIMEOUT).expectError(TimeoutException.class).verify();
        assertFalse(drained.get());
    }

    @Test
    public void greetingDrainFailureIsReported() {
        StepVerifier.withVirtualTime(() -> {
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            greeting.onAudio(new byte[] { 1, 2 });
            greeting.onTranscript();
            return greeting.awaitPlayback(() -> {
                throw new IllegalStateException("Speaker drain failed");
            });
        }).expectSubscription().thenAwait(Duration.ofSeconds(1)).expectErrorMessage("Speaker drain failed").verify();
    }

    @Test
    public void continuousSilentOutputDoesNotKeepTheGreetingOpen() {
        AtomicBoolean drained = new AtomicBoolean();
        StepVerifier.withVirtualTime(() -> {
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            greeting.onAudio(new byte[] { 1, 2 });
            greeting.onTranscript();
            Mono<Void> silence = Flux.interval(Duration.ofMillis(20))
                .take(50)
                .doOnNext(tick -> greeting.onAudio(new byte[960]))
                .then();
            return Mono.when(silence, greeting.awaitPlayback(() -> drained.set(true)));
        }).expectSubscription().thenAwait(Duration.ofSeconds(1)).verifyComplete();
        assertTrue(drained.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void continuousSilentAudioStillAllowsInputAfterTheGreeting(boolean async) {
        startTextServer(false, false, Mono.empty(), true);
        AtomicInteger silentFrames = new AtomicInteger();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            runInteractiveConversation(async, new StringReader("exit\n"), pcm -> {
                if (pcm.length == 960) {
                    assertArrayEquals(new byte[960], pcm);
                    silentFrames.incrementAndGet();
                }
            }, () -> {
            });
        } finally {
            System.setOut(original);
        }
        assertTrue(silentFrames.get() >= 20, "The server must keep streaming silence throughout the quiet period");
        String transcript = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Assistant: Welcome." + System.lineSeparator() + "You: "));
        assertEquals(Arrays.asList("session.commentary.append", "session.close"), commandsWithoutInputAudio());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 256, -256, 257, -257, 32767, -32768 })
    public void greetingSpeechDetectionHonorsSignedPcm16PeakThreshold(int peak) {
        boolean audible = Math.abs(peak) > GptLiveSampleUtils.Greeting.SILENCE_PEAK_THRESHOLD;
        AtomicBoolean drained = new AtomicBoolean();
        StepVerifier.Step<Void> verification = StepVerifier.withVirtualTime(() -> {
            GptLiveSampleUtils.Greeting greeting = new GptLiveSampleUtils.Greeting();
            greeting.onTranscript();
            greeting.onAudio(new byte[] { (byte) peak, (byte) (peak >> 8) });
            return greeting.awaitPlayback(() -> drained.set(true));
        }).expectSubscription().thenAwait(audible ? Duration.ofSeconds(1) : GptLiveSampleUtils.TIMEOUT);
        if (audible) {
            verification.verifyComplete();
        } else {
            verification.expectError(TimeoutException.class).verify();
        }
        assertEquals(audible, drained.get());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "\r\n", "exit\n", "quit\n" })
    public void consoleEofBlankAndExitProduceNoPrompt(String input) {
        StepVerifier.create(GptLiveSampleUtils.readPrompt(new StringReader(input))).verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void interactiveTextPlaybackFailureTerminatesTheConversation(boolean async) {
        startTextServer(false, false);
        StepVerifier.create(
            Mono.<Void>fromRunnable(() -> runInteractiveConversation(async, new StringReader("testing\n"), pcm -> {
                throw new IllegalStateException("Speaker failed");
            }, () -> {
            })).subscribeOn(Schedulers.boundedElastic()))
            .expectErrorMatches(
                error -> error instanceof IllegalStateException && "Speaker failed".equals(error.getMessage()))
            .verify(TIMEOUT);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    @ResourceLock("java.lang.System.out")
    public void interactiveTextPlaysGreetingAndRepliesWithoutPrintingBackendNotes(boolean async) {
        startTextServer(false, false);
        List<byte[]> played = new CopyOnWriteArrayList<>();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream capture = new PrintStream(output)) {
            System.setOut(capture);
            runInteractiveConversation(async, new StringReader("testing\nexit\n"), played::add, () -> {
            });
        } finally {
            System.setOut(original);
        }
        assertEquals(2, played.size());
        assertArrayEquals(new byte[] { 1, 2 }, played.get(0));
        assertArrayEquals(new byte[] { 1, 2 }, played.get(1));
        String transcript = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(transcript.contains("Assistant: Welcome."));
        assertTrue(transcript.contains("Spoken reply."));
        assertFalse(transcript.contains("Hello"), "Backend text must not be printed as the spoken answer");
        assertFalse(transcript.contains("[GPT Live diagnostics]"));
        assertFalse(transcript.contains("\"type\":\"session.closed\""));
        Map<?, ?> greeting = commands.stream()
            .filter(command -> "session.commentary.append".equals(command.get("type")))
            .findFirst()
            .get();
        assertEquals("session.commentary.append", greeting.get("type"));
        assertEquals("Hi, how can I help you today?", greeting.get("content"));
        assertTrue(greeting.containsKey("delegation_id"));
        assertEquals(null, greeting.get("delegation_id"));
        assertEquals("session.input_audio.append", commands.get(0).get("type"));
        assertEquals("session.close", commands.get(commands.size() - 1).get("type"));
    }

    @Test
    public void silentAudioFramesArePacedAtRealTimePcmRate() {
        StepVerifier.withVirtualTime(() -> GptLiveSampleUtils.silenceFrames().take(3))
            .expectSubscription()
            .thenAwait(Duration.ZERO)
            .assertNext(this::assertSilentAudioFrame)
            .expectNoEvent(Duration.ofMillis(19))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(this::assertSilentAudioFrame)
            .expectNoEvent(Duration.ofMillis(19))
            .thenAwait(Duration.ofMillis(1))
            .assertNext(this::assertSilentAudioFrame)
            .verifyComplete();
    }

    @Test
    public void silentAudioFramesWaitForDemandWithoutTimerOverflow() {
        StepVerifier.withVirtualTime(() -> GptLiveSampleUtils.silenceFrames().take(3), 0)
            .expectSubscription()
            .expectNoEvent(Duration.ofSeconds(40))
            .thenRequest(1)
            .assertNext(this::assertSilentAudioFrame)
            .expectNoEvent(Duration.ofSeconds(40))
            .thenRequest(1)
            .thenAwait(Duration.ofMillis(20))
            .assertNext(this::assertSilentAudioFrame)
            .expectNoEvent(Duration.ofSeconds(40))
            .thenRequest(1)
            .thenAwait(Duration.ofMillis(20))
            .assertNext(this::assertSilentAudioFrame)
            .verifyComplete();
    }

    @Test
    public void silentAudioToleratesSendsSlowerThanItsFrameRate() {
        StepVerifier
            .withVirtualTime(() -> GptLiveSampleUtils.silenceFrames()
                .concatMap(frame -> Mono.delay(Duration.ofMillis(75)).thenReturn(frame), 1)
                .take(500))
            .expectSubscription()
            .thenAwait(Duration.ofSeconds(40))
            .expectNextCount(500)
            .verifyComplete();
    }

    private void assertSilentAudioFrame(ClientEvent frame) {
        assertTrue(frame.isSessionInputAudioAppend());
        assertArrayEquals(new byte[960], Base64.getDecoder().decode(frame.asSessionInputAudioAppend().audio()));
    }

    @ParameterizedTest
    @MethodSource("interactiveClosureCases")
    public void interactiveTextPreservesServerClosureReason(boolean async, String reason) {
        String closed = "{\"type\":\"session.closed\",\"reason\":\"" + reason + "\",\"event_id\":\"closed-1\"}";
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket((inbound, outbound) -> outbound
                .sendString(Flux.concat(Mono.just(STARTED), inbound.receive().asString().concatMap(json -> {
                    Map<?, ?> command = GptLiveSampleUtils.object(BinaryData.fromString(json));
                    commands.add(command);
                    if ("session.commentary.append".equals(command.get("type"))) {
                        return Flux.just("{\"type\":\"session.output_transcript.delta\",\"delta\":\"Welcome.\"}",
                            "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}");
                    }
                    return "response.create".equals(command.get("type")) ? Mono.just(closed) : Mono.empty();
                })))
                .then()))
            .bindNow();
        if ("remote_hangup".equals(reason)) {
            runInteractiveConversation(async, new StringReader("testing\n"));
        } else {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> runInteractiveConversation(async, new StringReader("testing\n")));
            assertTrue(error.getMessage().contains(reason));
            assertTrue(error.getMessage().contains("closed-1"));
        }
        assertFalse(commandTypes().contains("session.close"));
    }

    private static Stream<Arguments> interactiveClosureCases() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of("remote_hangup", "expired", "content", "connection_lost")
                .map(reason -> Arguments.of(async, reason)));
    }

    @Test
    public void synchronousConsoleReadFailuresPropagateAndReleaseTheSession() {
        startTextServer(false, false);
        IOException failure = new IOException("Console unavailable");
        Reader input = new Reader() {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                throw failure;
            }

            @Override
            public void close() {
                throw new AssertionError("Standard input must not be closed");
            }
        };
        try (BetaVoiceAgentLiveWebSocketSessionClient session
            = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
            IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> GptLiveSampleUtils.runConversation(session, input, pcm -> {
                }, () -> {
                }));
            assertEquals("Unable to read console input.", error.getMessage());
            assertEquals(failure, error.getCause());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void serviceErrorsFailBothTextSamples(boolean async) {
        startTextServer(false, true);
        if (async) {
            StepVerifier
                .create(Mono.usingWhen(
                    builder().beta().buildBetaVoiceAgentLiveWebSocketAsyncClient().openWebSocketSession("agent"),
                    session -> GptLiveSampleUtils.runConversation(session, Collections.singletonList("one")),
                    BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                    && error.getMessage().contains("GPT Live service error"))
                .verify(TIMEOUT);
        } else {
            try (BetaVoiceAgentLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
                assertThrows(IllegalStateException.class,
                    () -> GptLiveSampleUtils.runConversation(session, Collections.singletonList("one")));
            }
        }
        assertFalse(commandTypes().contains("session.close"));
    }

    @Test
    public void toolsUseGranularEventsAndReturnResultsBeforeContinuation() {
        startTextServer(true, false);
        try (BetaVoiceAgentLiveWebSocketSessionClient session
            = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
            GptLiveSampleUtils.runConversation(session, Collections.singletonList("Weather in Seattle?"),
                GptLiveFunctionToolSample::toolResult);
        }
        assertEquals(Arrays.asList("response.item.create", "response.create", "response.item.create", "response.create",
            "session.close"), commandTypes());
        Map<?, ?> item = GptLiveSampleUtils.object(commands.get(2).get("item"));
        assertEquals("function_call_output", item.get("type"));
        assertEquals("call-1", item.get("call_id"));
        Map<?, ?> result = GptLiveSampleUtils.object(BinaryData.fromString((String) item.get("output")));
        assertEquals("Seattle", result.get("city"));
        assertEquals(true, result.get("simulated"));
        assertFalse(item.containsKey("delegation_id"));
    }

    @Test
    public void toolConversationUsesSharedMultiTurnLoop() {
        startTextServer(true, false);
        try (BetaVoiceAgentLiveWebSocketSessionClient session
            = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
            GptLiveSampleUtils.runConversation(session, Arrays.asList("Weather in Seattle?", "Thanks!"),
                GptLiveFunctionToolSample::toolResult);
        }
        assertEquals(Arrays.asList("response.item.create", "response.create", "response.item.create", "response.create",
            "response.item.create", "response.create", "session.close"), commandTypes());
        Map<?, ?> followUp = GptLiveSampleUtils.object(commands.get(4).get("item"));
        assertEquals("user", followUp.get("role"));
        assertEquals("Thanks!", GptLiveSampleUtils.object(((List<?>) followUp.get("content")).get(0)).get("text"));
    }

    @Test
    public void toolSchemaAndArgumentsAreValidated() {
        Map<?, ?> tool = GptLiveSampleUtils.object(GptLiveFunctionToolSample.weatherTool());
        assertEquals("function", tool.get("type"));
        assertEquals("get_weather", tool.get("name"));
        assertEquals(true, tool.get("strict"));
        assertEquals(false, GptLiveSampleUtils.object(tool.get("parameters")).get("additionalProperties"));
        Map<?, ?> unknown = GptLiveSampleUtils
            .object(BinaryData.fromString("{\"name\":\"unknown\",\"call_id\":\"c\",\"arguments\":\"{}\"}"));
        assertThrows(IllegalArgumentException.class, () -> GptLiveFunctionToolSample.toolResult(unknown));
        Map<?, ?> invalid = GptLiveSampleUtils
            .object(BinaryData.fromString("{\"name\":\"get_weather\",\"call_id\":\"c\",\"arguments\":\"{}\"}"));
        assertThrows(IllegalArgumentException.class, () -> GptLiveFunctionToolSample.toolResult(invalid));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void prematureClosureDoesNotReportSuccessfulConversation(boolean async) {
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request, response) -> response.sendWebsocket(
                (inbound, outbound) -> outbound.sendString(Flux.just(STARTED, CLOSED)).then(inbound.receive().then())))
            .bindNow();
        if (async) {
            StepVerifier
                .create(Mono.usingWhen(
                    builder().beta().buildBetaVoiceAgentLiveWebSocketAsyncClient().openWebSocketSession("agent"),
                    session -> GptLiveSampleUtils.runConversation(session, Collections.singletonList("one")),
                    BetaVoiceAgentLiveWebSocketSessionAsyncClient::closeAsync))
                .expectError(IllegalStateException.class)
                .verify(TIMEOUT);
        } else {
            try (BetaVoiceAgentLiveWebSocketSessionClient session
                = builder().beta().buildBetaVoiceAgentLiveWebSocketClient().openWebSocketSession("agent")) {
                assertThrows(IllegalStateException.class,
                    () -> GptLiveSampleUtils.runConversation(session, Collections.singletonList("one")));
            }
        }
    }

    @Test
    public void typedCommentaryPreservesExplicitNullDelegationId() throws IOException {
        ClientEvent event = GptLiveSampleUtils.commentary("Hello");
        assertTrue(event.isSessionCommentaryAppend());
        BinaryData data = OpenAIJsonHelper.toBinaryData(event);
        Map<?, ?> command = GptLiveSampleUtils.object(data);
        assertEquals("session.commentary.append", command.get("type"));
        assertEquals("Hello", command.get("content"));
        assertTrue(command.containsKey("delegation_id"));
        assertEquals(null, command.get("delegation_id"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (com.azure.json.JsonWriter writer = com.azure.json.JsonProviders.createWriter(output)) {
            data.writeTo(writer);
        }
        assertEquals(command, GptLiveSampleUtils.object(BinaryData.fromBytes(output.toByteArray())));
    }

    @Test
    public void nativeAudioEncodingAndDecodingPreservePcmBytes() {
        byte[] pcm = new byte[] { 0, 1, -1, 127 };
        ClientEvent command = GptLiveSampleUtils.audio(pcm);
        assertTrue(command.isSessionInputAudioAppend());
        assertArrayEquals(pcm, Base64.getDecoder().decode(command.asSessionInputAudioAppend().audio()));
        ServerEvent event = typedEvent(
            GptLiveSampleUtils.event("session.output_audio.delta", "delta", Base64.getEncoder().encodeToString(pcm)));
        assertArrayEquals(pcm, GptLiveSampleUtils.decodeAudio(event));
        assertThrows(IllegalArgumentException.class, () -> GptLiveSampleUtils
            .decodeAudio(typedEvent(GptLiveSampleUtils.event("session.output_audio.delta", "delta", "%%%"))));
        assertThrows(IllegalArgumentException.class, () -> GptLiveSampleUtils
            .decodeAudio(typedEvent(GptLiveSampleUtils.event("session.output_audio.delta", "delta", "AA=="))));
    }

    private static ServerEvent typedEvent(BinaryData data) {
        return OpenAIJsonHelper.fromBinaryData(data, ServerEvent.class);
    }

    private static Stream<Arguments> audioSessionCases() {
        return Stream.of(false, true)
            .flatMap(moderated -> Stream.of(false, true).map(store -> Arguments.of(moderated, store)));
    }

    @ParameterizedTest
    @MethodSource("audioSessionCases")
    public void audioSampleGreetsCapturesPlaysAndReleasesDevices(boolean moderated, boolean store) {
        AtomicBoolean captured = new AtomicBoolean();
        AtomicInteger closed = new AtomicInteger();
        CountDownLatch unblockRead = new CountDownLatch(1);
        AtomicReference<byte[]> played = new AtomicReference<>();
        AtomicBoolean firstRead = new AtomicBoolean(true);
        TargetDataLine microphone = (TargetDataLine) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] { TargetDataLine.class }, (proxy, method, args) -> {
                if ("read".equals(method.getName())) {
                    if (firstRead.compareAndSet(true, false)) {
                        byte[] bytes = (byte[]) args[0];
                        bytes[0] = 1;
                        bytes[1] = 2;
                        return 2;
                    }
                    unblockRead.await(5, TimeUnit.SECONDS);
                    return 0;
                } else if ("close".equals(method.getName())) {
                    closed.incrementAndGet();
                    unblockRead.countDown();
                }
                return null;
            });
        SourceDataLine speaker = (SourceDataLine) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[] { SourceDataLine.class }, (proxy, method, args) -> {
                if ("write".equals(method.getName())) {
                    played.set(((byte[]) args[0]).clone());
                    return args[2];
                } else if ("close".equals(method.getName())) {
                    closed.incrementAndGet();
                }
                return null;
            });
        AtomicReference<String> requestUri = new AtomicReference<>();
        String started = store
            ? "{\"type\":\"session.started\",\"conversation_id\":\"conversation-1\",\"session\":{\"id\":\"live-1\"}}"
            : STARTED;
        server = VoiceAgentWebSocketSessionTests.tlsServer().host("localhost").port(0).handle((request, response) -> {
            requestUri.set(request.uri());
            return response
                .sendWebsocket(
                    (inbound,
                        outbound) -> outbound.sendString(Flux.concat(
                            Flux.just(started, "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}"),
                            inbound.receive().asString().concatMap(json -> {
                                Map<?, ?> command = GptLiveSampleUtils.object(BinaryData.fromString(json));
                                commands.add(command);
                                if ("session.input_audio.append".equals(command.get("type"))) {
                                    captured.set(true);
                                }
                                if (moderated && "response.create".equals(command.get("type"))) {
                                    return Mono.just(MODERATED);
                                }
                                return "session.close".equals(command.get("type")) ? Mono.just(CLOSED) : Mono.empty();
                            }))).then());
        }).bindNow();
        BetaVoiceAgentLiveWebSocketSessionAsyncClient session = builder().beta()
            .buildBetaVoiceAgentLiveWebSocketAsyncClient()
            .openWebSocketSession("agent", new VoiceAgentLiveWebSocketConnectionOptions().setStoreEnabled(store))
            .block(TIMEOUT);
        assertNotNull(session);
        assertTrue(requestUri.get().contains("store=" + store), requestUri.get());
        InputStream enterAfterAudio = new InputStream() {
            @Override
            public int available() {
                return captured.get() && played.get() != null && commandTypes().contains("response.create") ? 1 : 0;
            }

            @Override
            public int read() {
                return '\n';
            }
        };
        try {
            GptLiveSampleUtils.AudioProcessor processor = new GptLiveSampleUtils.AudioProcessor(microphone, speaker,
                pcm -> session.sendEvent(GptLiveSampleUtils.audio(pcm.toBytes())));
            Mono<Void> conversation = GptLiveSampleUtils.runConversation(session, processor, enterAfterAudio);
            if (moderated) {
                IllegalStateException error
                    = assertThrows(IllegalStateException.class, () -> conversation.block(TIMEOUT));
                assertTrue(error.getMessage().contains("content_filter"));
                assertTrue(error.getMessage().contains("moderated-1"));
            } else {
                conversation.block(TIMEOUT);
                assertTrue(captured.get());
                assertArrayEquals(new byte[] { 1, 2 }, played.get());
                assertTrue(commandTypes().contains("session.close"));
            }
            assertEquals(2, closed.get());
            List<String> greetingCommands = commandTypes().stream()
                .filter(type -> "response.item.create".equals(type) || "response.create".equals(type))
                .collect(java.util.stream.Collectors.toList());
            assertEquals(Arrays.asList("response.item.create", "response.create"), greetingCommands);
            Map<?, ?> greeting = commands.stream()
                .filter(command -> "response.item.create".equals(command.get("type")))
                .findFirst()
                .get();
            Map<?, ?> item = GptLiveSampleUtils.object(greeting.get("item"));
            assertEquals("Greet me briefly with: Hi, how can I help you today?",
                GptLiveSampleUtils.object(((List<?>) item.get("content")).get(0)).get("text"));
        } finally {
            session.closeAsync().block(TIMEOUT);
        }
    }

    private List<String> commandTypes() {
        return commands.stream().map(GptLiveSampleUtils::type).collect(java.util.stream.Collectors.toList());
    }

    private AgentsClientBuilder builder() {
        return new AgentsClientBuilder().endpoint("https://localhost:" + server.port())
            .credential(request -> Mono.just(new AccessToken("test-token", OffsetDateTime.now().plusHours(1))))
            .configuration(Configuration.NONE);
    }

    private void startTextServer(boolean tools, boolean fail) {
        startTextServer(tools, fail, Mono.empty());
    }

    private void startTextServer(boolean tools, boolean fail, Mono<Void> beforeGreeting) {
        startTextServer(tools, fail, beforeGreeting, false);
    }

    private void startTextServer(boolean tools, boolean fail, Mono<Void> beforeGreeting, boolean continuousSilence) {
        AtomicInteger responses = new AtomicInteger();
        String silentOutput = "{\"type\":\"session.output_audio.delta\",\"delta\":\""
            + Base64.getEncoder().encodeToString(new byte[960]) + "\"}";
        server = VoiceAgentWebSocketSessionTests.tlsServer()
            .host("localhost")
            .port(0)
            .handle((request,
                response) -> response.sendWebsocket((inbound, outbound) -> outbound
                    .sendString(Flux.concat(Mono.just(STARTED).delayElement(Duration.ofMillis(50)),
                        inbound.receive().asString().concatMap(json -> {
                            Map<?, ?> command = GptLiveSampleUtils.object(BinaryData.fromString(json));
                            commands.add(command);
                            if ("session.commentary.append".equals(command.get("type"))) {
                                return beforeGreeting.thenMany(
                                    Flux.just("{\"type\":\"session.output_transcript.delta\",\"delta\":\"Welcome.\"}",
                                        "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}"));
                            }
                            if ("response.create".equals(command.get("type"))) {
                                int responseIndex = responses.incrementAndGet();
                                if (fail) {
                                    return Flux.just("{\"type\":\"error\",\"error\":{\"message\":\"failed\"}}");
                                }
                                if (tools && responseIndex == 1) {
                                    return Flux.just("{\"type\":\"response.event\",\"delegation_id\":\"del-1\","
                                        + "\"event\":{\"type\":\"response.output_item.done\",\"item\":"
                                        + "{\"type\":\"function_call\",\"call_id\":\"call-1\",\"name\":\"get_weather\","
                                        + "\"arguments\":\"{\\\"city\\\":\\\"Seattle\\\"}\"}}}", COMPLETED);
                                }
                                return Flux.just(
                                    "{\"type\":\"response.event\",\"event\":"
                                        + "{\"type\":\"response.output_text.delta\",\"delta\":\"Hello\"}}",
                                    "{\"type\":\"session.output_transcript.delta\",\"delta\":\"Spoken reply.\"}",
                                    "{\"type\":\"session.output_audio.delta\",\"delta\":\"AQI=\"}", COMPLETED);
                            }
                            if (continuousSilence && "session.input_audio.append".equals(command.get("type"))) {
                                return Flux.just(silentOutput);
                            }
                            return "session.close".equals(command.get("type")) ? Flux.just(CLOSED) : Flux.empty();
                        })))
                    .then()))
            .bindNow();
    }
}
