// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.AgentsClientBuilder;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GptLiveConversationSamplesTests {
    private static final String ROOT = "/agents/live-agent/endpoint/protocols/voice/live/conversations";
    private static final String PATH = ROOT + "/conversation-1";
    private static final String CONVERSATION
        = "{\"id\":\"conversation-1\",\"status\":\"completed\",\"created_at\":1700000000,\"usage\":{\"seconds\":2.5}}";
    private static final String INPUT = "{\"id\":\"input-1\",\"type\":\"input\",\"transcription\":\"Hello\"}";
    private static final String OUTPUT = "{\"id\":\"output-1\",\"type\":\"output\",\"transcription\":\"Hi\"}";
    private static final String DELEGATION
        = "{\"id\":\"delegation-1\",\"type\":\"delegation\",\"target\":\"responses\",\"response_id\":\"response-1\"}";
    private static final byte[] WAV = { 82, 73, 70, 70, 0, 0, 0, 0, 87, 65, 86, 69 };
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @TempDir
    Path directory;

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void readsCursorPagesAndFetchesSpeechAndDelegationDetails(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.json(ROOT, null, 200, page(CONVERSATION));
        transport.json(ROOT, "conversation-1", 200, page());
        transport.json(PATH, null, 200, CONVERSATION);
        transport.json(PATH + "/items", null, 200, page(INPUT));
        transport.json(PATH + "/items/input-1", null, 200, INPUT);
        transport.json(PATH + "/items", "input-1", 200, page(OUTPUT, DELEGATION));
        transport.json(PATH + "/items/output-1", null, 200, OUTPUT);
        transport.json(PATH + "/items/delegation-1", null, 200, DELEGATION);
        transport.json(PATH + "/items", "delegation-1", 200, page());
        readConversation(builder(transport), async);
        transport.assertConsumed();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void missingConversationPropagates(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.json(ROOT, null, 200, page());
        transport.json(PATH, null, 404, error("not_found"));
        HttpResponseException failure
            = assertThrows(HttpResponseException.class, () -> readConversation(builder(transport), async));
        assertEquals(404, failure.getResponse().getStatusCode());
        transport.assertConsumed();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void repeatedCursorFailsRatherThanLooping(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.json(ROOT, null, 200, page(CONVERSATION));
        transport.json(ROOT, "conversation-1", 200, page(CONVERSATION));
        assertThrows(IllegalStateException.class, () -> readConversation(builder(transport), async));
        transport.assertConsumed();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void downloadsMergedAndItemBytesWithoutOverwritingFiles(boolean async) throws IOException {
        Path existing = directory.resolve("existing.wav");
        byte[] original = { 1, 2, 3 };
        Files.write(existing, original);
        ScriptedTransport transport = audioTransport(async, false);
        downloadAudio(builder(transport), async, "input-1");
        transport.assertConsumed();
        assertEquals(3, files().size());
        assertArrayEquals(original, Files.readAllBytes(existing));
        for (Path file : files()) {
            if (!file.equals(existing)) {
                assertArrayEquals(WAV, Files.readAllBytes(file));
                assertTrue(file.getFileName().toString().endsWith(".wav"));
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void byosMetadataDoesNotCallFoundryContentEndpoints(boolean async) throws IOException {
        ScriptedTransport transport = audioTransport(async, true);
        downloadAudio(builder(transport), async, "input-1");
        transport.assertConsumed();
        assertTrue(files().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void optionalItemCanBeOmitted(boolean async) throws IOException {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.json(PATH + "/audio", null, 200, recording(false));
        transport.audio(PATH + "/audio/content");
        downloadAudio(builder(transport), async, null);
        transport.assertConsumed();
        assertEquals(1, files().size());
    }

    @ParameterizedTest
    @MethodSource("audioFailures")
    public void audioErrorsPreserveStatusAndRetryAfter(boolean async, int status, boolean content) throws IOException {
        ScriptedTransport transport = new ScriptedTransport(async);
        if (content) {
            transport.json(PATH + "/audio", null, 200, recording(false));
        }
        transport.json(PATH + (content ? "/audio/content" : "/audio"), null, status, error("recording_not_ready"));
        HttpResponseException failure
            = assertThrows(HttpResponseException.class, () -> downloadAudio(builder(transport), async, null));
        assertEquals(status, failure.getResponse().getStatusCode());
        assertEquals("3", failure.getResponse().getHeaderValue("Retry-After"));
        assertTrue(files().isEmpty());
        transport.assertConsumed();
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void blankItemFailsBeforeNetworkAccess(boolean async) {
        ScriptedTransport transport = new ScriptedTransport(async);
        assertThrows(IllegalArgumentException.class, () -> downloadAudio(builder(transport), async, " "));
        transport.assertConsumed();
    }

    @Test
    public void failedFileWriteRemovesPartialOutput() throws IOException {
        InputStream failingStream = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated read failure");
            }
        };
        assertThrows(IOException.class, () -> GptLiveReadConversationAudioSample
            .saveAudio(BinaryData.fromStream(failingStream), directory, "failure-"));
        assertTrue(files().isEmpty());
    }

    @Test
    public void configurationDirectoryAndCursorValidation() {
        assertThrows(IllegalArgumentException.class,
            () -> GptLiveSampleUtils.requireSetting(Configuration.NONE, "FOUNDRY_PROJECT_ENDPOINT"));
        assertThrows(IllegalArgumentException.class,
            () -> GptLiveReadConversationAudioSample.outputDirectory(new String[0]));
        assertThrows(IllegalArgumentException.class, () -> GptLiveReadConversationAudioSample
            .outputDirectory(new String[] { directory.resolve("missing").toString() }));
        assertEquals(directory,
            GptLiveReadConversationAudioSample.outputDirectory(new String[] { directory.toString() }));
        assertThrows(IllegalStateException.class, () -> GptLiveReadConversationSample.nextCursor(null, null));
        assertThrows(IllegalStateException.class, () -> GptLiveReadConversationSample.nextCursor("", null));
    }

    private static Stream<Arguments> audioFailures() {
        return Stream.of(false, true)
            .flatMap(async -> Stream.of(403, 404, 409)
                .flatMap(status -> Stream.of(false, true).map(content -> Arguments.of(async, status, content))));
    }

    private static void readConversation(AgentsClientBuilder builder, boolean async) {
        if (async) {
            GptLiveReadConversationAsyncSample
                .readConversation(builder.buildBetaVoiceAgentsLiveConversationsAsyncClient(), "live-agent",
                    "conversation-1")
                .block(TIMEOUT);
        } else {
            GptLiveReadConversationSample.readConversation(builder.buildBetaVoiceAgentsLiveConversationsClient(),
                "live-agent", "conversation-1");
        }
    }

    private void downloadAudio(AgentsClientBuilder builder, boolean async, String itemId) throws IOException {
        if (async) {
            GptLiveReadConversationAudioAsyncSample
                .downloadAudio(builder.buildBetaVoiceAgentsLiveConversationsAsyncClient(), "live-agent",
                    "conversation-1", itemId, directory)
                .block(TIMEOUT);
        } else {
            GptLiveReadConversationAudioSample.downloadAudio(builder.buildBetaVoiceAgentsLiveConversationsClient(),
                "live-agent", "conversation-1", itemId, directory);
        }
    }

    private static ScriptedTransport audioTransport(boolean async, boolean byos) {
        ScriptedTransport transport = new ScriptedTransport(async);
        transport.json(PATH + "/audio", null, 200, recording(byos));
        if (!byos) {
            transport.audio(PATH + "/audio/content");
        }
        transport.json(PATH + "/items/input-1/audio", null, 200,
            "{\"conversation_id\":\"conversation-1\",\"item_id\":\"input-1\",\"format\":\"wav\",\"sample_rate\":24000"
                + (byos ? ",\"blob_uri\":\"https://storage.example/item.wav\"" : "") + "}");
        if (!byos) {
            transport.audio(PATH + "/items/input-1/audio/content");
        }
        return transport;
    }

    private List<Path> files() throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.collect(Collectors.toList());
        }
    }

    private static String recording(boolean byos) {
        return "{\"conversation_id\":\"conversation-1\",\"format\":\"wav\",\"sample_rate\":24000,\"channels\":2,"
            + "\"channel_layout\":{\"left\":\"user\",\"right\":\"assistant\"},\"duration_ms\":2000"
            + (byos ? ",\"blob_uri\":\"https://storage.example/call.wav\"" : "") + "}";
    }

    private static String page(String... entries) {
        return "{\"data\":[" + String.join(",", entries) + "]}";
    }

    private static String error(String code) {
        return "{\"error\":{\"code\":\"" + code + "\",\"message\":\"Sample service error\"}}";
    }

    private static AgentsClientBuilder builder(HttpClient transport) {
        return new AgentsClientBuilder().endpoint("https://localhost")
            .credential(new MockTokenCredential())
            .configuration(Configuration.NONE)
            .addPolicy(GptLiveReadConversationSample.previewHeaders())
            .httpClient(transport)
            .retryOptions(new RetryOptions(new ExponentialBackoffOptions().setMaxRetries(0)));
    }

    private static final class ScriptedTransport implements HttpClient {
        private final Deque<Function<HttpRequest, HttpResponse>> requests = new ArrayDeque<>();
        private final boolean async;

        ScriptedTransport(boolean async) {
            this.async = async;
        }

        void json(String path, String after, int status, String body) {
            expect(path, after, status, "application/json", BinaryData.fromString(body).toBytes());
        }

        void audio(String path) {
            expect(path, null, 200, "audio/wav", WAV);
        }

        private void expect(String path, String after, int status, String contentType, byte[] body) {
            requests.add(request -> {
                assertEquals(HttpMethod.GET, request.getHttpMethod(), "Samples must not modify or delete data");
                assertEquals(path, request.getUrl().getPath());
                assertEquals("VoiceAgents=V1Preview", request.getHeaders().getValue("Foundry-Features"));
                String query = request.getUrl().getQuery();
                assertTrue(query.contains("api-version="));
                if (path.equals(ROOT) || path.endsWith("/items")) {
                    assertTrue(query.contains("limit=100"));
                    assertTrue(query.contains("order=asc"));
                }
                if (after != null) {
                    assertTrue(query.contains("after=" + after), "Missing cursor: " + query);
                } else {
                    assertFalse(query.contains("after="), "Unexpected cursor: " + query);
                }
                return new MockHttpResponse(request, status,
                    new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType)
                        .set(HttpHeaderName.fromString("Retry-After"), "3"),
                    body);
            });
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            assertTrue(async, "Synchronous examples must use synchronous HTTP");
            return Mono.fromSupplier(() -> respond(request));
        }

        @Override
        public HttpResponse sendSync(HttpRequest request, Context context) {
            assertFalse(async, "Asynchronous examples must use asynchronous HTTP");
            return respond(request);
        }

        private HttpResponse respond(HttpRequest request) {
            assertFalse(requests.isEmpty(), "Unexpected request: " + request.getUrl());
            return requests.removeFirst().apply(request);
        }

        void assertConsumed() {
            assertTrue(requests.isEmpty(), "Not all expected requests were made");
        }
    }
}
