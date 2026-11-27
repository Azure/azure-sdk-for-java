// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpPipelineNextSyncPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.TestProxyPlaybackClient;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LocationPollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.PollingStrategyOptions;
import com.azure.core.util.polling.SyncLocationPollingStrategy;
import com.azure.core.util.polling.SyncPoller;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_FILE_LOCATION;
import static com.azure.core.test.implementation.TestingHelpers.X_RECORDING_ID;

/**
 * Tests {@link PlaybackPollingUrlRewriter}.
 */
public class PlaybackPollingUrlRewriterTests {
    private static final String REQUEST_URL
        = "https://localhost:8080/documentintelligence/documentModels:build?api-version=2024-11-30";

    @ParameterizedTest
    @ValueSource(strings = { "Operation-Location", "operation-location", "Azure-AsyncOperation" })
    public void rewritesPollingHeaderOntoRequestHost(String headerName) {
        HttpHeaderName name = HttpHeaderName.fromString(headerName);
        HttpResponse response = createResponse(REQUEST_URL, 202, new HttpHeaders().set(name,
            "https://REDACTED.cognitiveservices.azure.com/documentintelligence/operations/31?api-version=2024-11-30"),
            new byte[0]);

        HttpResponse rewritten = PlaybackPollingUrlRewriter.rewritePollingUrls(response);

        String expected = "https://localhost:8080/documentintelligence/operations/31?api-version=2024-11-30";
        assertEquals(expected, rewritten.getHeaderValue(name));
        assertEquals(expected, rewritten.getHeaders().getValue(name));
    }

    @ParameterizedTest
    @MethodSource("rewriteSupplier")
    public void rewritesOnlyHostAndPort(String requestUrl, String recordedUrl, String expectedUrl) {
        HttpResponse response = createResponse(requestUrl, 202,
            new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION, recordedUrl), new byte[0]);

        assertEquals(expectedUrl,
            PlaybackPollingUrlRewriter.rewritePollingUrls(response).getHeaderValue(HttpHeaderName.OPERATION_LOCATION));
    }

    private static Stream<Arguments> rewriteSupplier() {
        return Stream.of(
            // Playback endpoint without a port.
            Arguments.of("https://localhost/contentunderstanding/analyzers/a:analyze",
                "https://REDACTED.services.ai.azure.com/contentunderstanding/analyzerResults/1?api-version=2025-11-01",
                "https://localhost/contentunderstanding/analyzerResults/1?api-version=2025-11-01"),
            // Unsanitized ARM host.
            Arguments.of("https://localhost:1234/subscriptions/0/resourceGroups/rg?api-version=2021-04-01",
                "https://management.azure.com/subscriptions/0/operationresults/op?api-version=2021-04-01",
                "https://localhost:1234/subscriptions/0/operationresults/op?api-version=2021-04-01"),
            // The scheme is kept as recorded, as is the path, including empty segments.
            Arguments.of("http://localhost:5000/fr/path/1", "https://REDACTED/fr/models//905a#fragment",
                "https://localhost:5000/fr/models//905a#fragment"),
            Arguments.of("https://localhost:8080/path", "http://REDACTED.example.com/status",
                "http://localhost:8080/status"),
            // User information and the recorded port aren't kept.
            Arguments.of("https://localhost:8080/path", "https://user:secret@REDACTED.example.com:8443/status",
                "https://localhost:8080/status"),
            Arguments.of("https://localhost:8080/path", "https://recorded.example:65535/status",
                "https://localhost:8080/status"),
            Arguments.of("https://localhost:8080/path", "https://[::1]:8443/status", "https://localhost:8080/status"),
            // URL without a path, such as one replaced by a default test proxy sanitizer.
            Arguments.of("https://localhost:8080/path", "https://example.com", "https://localhost:8080"));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "",
            "/operations/1",
            "REDACTED",
            "Sanitized",
            "https://REDACTED/not a uri",
            "https://recorded.example:not-a-port/operations/1",
            "https://recorded.example:/operations/1",
            "https://recorded.example:65536/operations/1",
            "https://recorded.example:999999999999/operations/1",
            "https://recorded.example:-1/operations/1",
            "https://:443/operations/1",
            "ftp://REDACTED/file",
            "urn:operation:1" })
    public void leavesRelativeInvalidAndNonHttpUrlsAsRecorded(String recordedUrl) {
        HttpResponse response = createResponse(REQUEST_URL, 202,
            new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION, recordedUrl), new byte[0]);

        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response));
    }

    @ParameterizedTest
    @CsvSource({
        // Absolute resource or upload URLs on non-LRO responses, such as Batch's 201 job creation.
        "200,https://recorded.example/schemas/1",
        "201,https://recorded.batch.azure.com/jobs/job-1",
        "202,https://recorded.example/v2/repo/blobs/uploads/1",
        "204,https://recorded.example/schemas/1",
        "400,https://recorded.example/operations/1",
        "500,https://recorded.example/operations/1",
        // Relative values, such as Container Registry upload locations, are never rewritten.
        "201,/v2/repo/blobs/uploads/1",
        "202,/v2/repo/blobs/uploads/1" })
    public void leavesNonLroLocationAsRecorded(int statusCode, String location) {
        HttpResponse response = createResponse(REQUEST_URL, statusCode,
            new HttpHeaders().set(HttpHeaderName.LOCATION, location), new byte[0]);

        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response));
        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response, false));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Operation-Location", "Azure-AsyncOperation" })
    public void rewritesLroFinalResultLocation(String pollingHeader) {
        HttpResponse response = createResponse(REQUEST_URL, 202,
            new HttpHeaders().set(HttpHeaderName.fromString(pollingHeader), "https://recorded.example/operations/1")
                .set(HttpHeaderName.LOCATION, "https://recorded.example/results/1"),
            new byte[0]);

        assertEquals("https://localhost:8080/results/1",
            PlaybackPollingUrlRewriter.rewritePollingUrls(response).getHeaderValue(HttpHeaderName.LOCATION));
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 201, 202, 204 })
    public void rewritesLocationOnOtherResponsesWhenOptedIn(int statusCode) {
        HttpResponse response = createResponse(REQUEST_URL, statusCode,
            new HttpHeaders().set(HttpHeaderName.LOCATION, "https://recorded.example/operations/1"), new byte[0]);

        assertEquals("https://localhost:8080/operations/1",
            PlaybackPollingUrlRewriter.rewritePollingUrls(response, true).getHeaderValue(HttpHeaderName.LOCATION));
    }

    @ParameterizedTest
    @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
    public void playbackClientHonorsLocationOptIn(boolean sync, boolean optIn) throws URISyntaxException {
        TestProxyPlaybackClient client = createPlaybackClient(new ArrayList<>());
        Context context = new Context(TestProxyPlaybackClient.REWRITE_LOCATION_HEADER, optIn);
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://localhost:8080/schemas/1");
        HttpResponse response = sync ? client.sendSync(request, context) : client.send(request, context).block();

        assertEquals(optIn ? "https://localhost:8080/schemas/1" : "https://recorded.example/schemas/1",
            response.getHeaderValue(HttpHeaderName.LOCATION));
        assertEquals("https://localhost:8080/schemas/1", response.getRequest().getUrl().toString());
    }

    @ParameterizedTest
    @CsvSource({
        "true,GET,/schemas/1,https://recorded.example/schemas/1",
        "false,GET,/schemas/1,https://recorded.example/schemas/1",
        "true,POST,/jobs,https://recorded.batch.azure.com/jobs/job-1",
        "false,POST,/jobs,https://recorded.batch.azure.com/jobs/job-1",
        "true,POST,/v2/repo/blobs/uploads/absolute,https://recorded.example/v2/repo/blobs/uploads/1",
        "false,POST,/v2/repo/blobs/uploads/absolute,https://recorded.example/v2/repo/blobs/uploads/1",
        "true,POST,/v2/repo/blobs/uploads/,/v2/repo/blobs/uploads/2",
        "false,POST,/v2/repo/blobs/uploads/,/v2/repo/blobs/uploads/2" })
    public void playbackClientLeavesNonLroLocationWithoutOptInAsRecorded(boolean sync, String method, String path,
        String recorded) throws URISyntaxException {
        TestProxyPlaybackClient client = createPlaybackClient(new ArrayList<>());
        String url = "https://localhost:8080" + path;

        HttpResponse withContext = sync
            ? client.sendSync(new HttpRequest(HttpMethod.valueOf(method), url), Context.NONE)
            : client.send(new HttpRequest(HttpMethod.valueOf(method), url), Context.NONE).block();
        assertEquals(recorded, withContext.getHeaderValue(HttpHeaderName.LOCATION));
        assertEquals(recorded,
            client.send(new HttpRequest(HttpMethod.valueOf(method), url))
                .block()
                .getHeaderValue(HttpHeaderName.LOCATION));
    }

    /**
     * Location-only LROs opt in with a context passed to both the activation request and
     * {@link PollingStrategyOptions}, as generated clients do with {@code RequestOptions}, so the opt-in reaches the
     * activation, every poll, and the final-result request.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void locationOnlyLroPollsOnRequestHostWithOptIn(boolean sync) throws URISyntaxException {
        List<String> transportPaths = new ArrayList<>();
        List<String> pollerUrls = new ArrayList<>();
        HttpPipeline pipeline = createRecordingPipeline(createPlaybackClient(transportPaths), pollerUrls);
        Context context = new Context(TestProxyPlaybackClient.REWRITE_LOCATION_HEADER, true);

        BinaryData result = runLocationPoller(pipeline, REQUEST_URL, context, sync);

        assertEquals("{\"status\":\"succeeded\"}", result.toString());
        assertEquals(
            Arrays.asList("POST " + REQUEST_URL + " opt-in", "GET https://localhost:8080/operations/1 opt-in",
                "GET https://localhost:8080/operations/2 opt-in", "GET https://localhost:8080/operations/2 opt-in"),
            pollerUrls);
        assertEquals(Arrays.asList("/documentintelligence/documentModels:build", "/operations/1", "/operations/2",
            "/operations/2"), transportPaths);
    }

    /**
     * Covers a Location-only LRO whose final poll is a 200 with the result {@code Location}. Without the opt-in, every
     * {@code Location} keeps its recorded host.
     */
    @ParameterizedTest
    @CsvSource({ "true,true", "true,false", "false,true", "false,false" })
    public void locationOnlyLroWithFinal200LocationHonorsOptInAcrossPoller(boolean sync, boolean optIn)
        throws URISyntaxException {
        List<String> transportPaths = new ArrayList<>();
        List<String> pollerUrls = new ArrayList<>();
        HttpPipeline pipeline = createRecordingPipeline(createPlaybackClient(transportPaths), pollerUrls);
        Context context = optIn ? new Context(TestProxyPlaybackClient.REWRITE_LOCATION_HEADER, true) : Context.NONE;

        BinaryData result = runLocationPoller(pipeline, "https://localhost:8080/opt-in/lro", context, sync);

        String suffix = optIn ? " opt-in" : "";
        String pollHost = optIn ? "https://localhost:8080" : "https://recorded.example";
        assertEquals("{\"id\":\"result\"}", result.toString());
        assertEquals(
            Arrays.asList("POST https://localhost:8080/opt-in/lro" + suffix,
                "GET " + pollHost + "/opt-in/operations/1" + suffix, "GET " + pollHost + "/opt-in/results/1" + suffix),
            pollerUrls);
        assertEquals(Arrays.asList("/opt-in/lro", "/opt-in/operations/1", "/opt-in/results/1"), transportPaths);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Operation-Location", "Azure-AsyncOperation", "Location" })
    public void preservesMalformedAuthorityWhenOtherHeadersAreRewritten(String headerName) {
        HttpHeaderName name = HttpHeaderName.fromString(headerName);
        String malformed = "https://recorded.example:not-a-port/operations/1";
        HttpResponse response = createResponse(REQUEST_URL, 202,
            new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION, "https://recorded.example/operations/2")
                .set(HttpHeaderName.AZURE_ASYNCOPERATION, "https://recorded.example/operations/3")
                .set(name, malformed),
            new byte[0]);

        assertEquals(malformed, PlaybackPollingUrlRewriter.rewritePollingUrls(response, true).getHeaderValue(name));
    }

    @ParameterizedTest
    @ValueSource(strings = { "Operation-Location", "Azure-AsyncOperation", "Location" })
    public void preservesHttpsToHttpDowngrade(String headerName) {
        HttpHeaderName name = HttpHeaderName.fromString(headerName);
        HttpResponse response = createResponse(REQUEST_URL, 202,
            new HttpHeaders().set(name, "http://recorded.example/operations/1"), new byte[0]);

        assertEquals("http://localhost:8080/operations/1",
            PlaybackPollingUrlRewriter.rewritePollingUrls(response, true).getHeaderValue(name));
        assertEquals("https", response.getRequest().getUrl().getProtocol());
    }

    @ParameterizedTest
    @ValueSource(ints = { 301, 302, 303, 307, 308 })
    public void leavesRedirectLocationAsRecorded(int statusCode) {
        HttpResponse response = createResponse(REQUEST_URL, statusCode,
            new HttpHeaders().set(HttpHeaderName.LOCATION, "https://REDACTED.blob.core.windows.net/container/blob"),
            new byte[0]);

        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response));
        assertSame(response, PlaybackPollingUrlRewriter.rewritePollingUrls(response, true));
    }

    @Test
    public void returnsResponseWhenNoPollingUrlNeedsRewriting() {
        HttpResponse noPollingUrls = createResponse(REQUEST_URL, 200,
            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"), new byte[0]);
        assertSame(noPollingUrls, PlaybackPollingUrlRewriter.rewritePollingUrls(noPollingUrls));

        HttpResponse alreadyOnRequestHost
            = createResponse(REQUEST_URL, 202, new HttpHeaders().set(HttpHeaderName.OPERATION_LOCATION,
                "https://localhost:8080/documentintelligence/operations/31?api-version=2024-11-30"), new byte[0]);
        assertSame(alreadyOnRequestHost, PlaybackPollingUrlRewriter.rewritePollingUrls(alreadyOnRequestHost));
    }

    @Test
    public void rewrittenResponseKeepsRequestStatusBodyAndOtherHeaders() {
        byte[] body = "{\"status\":\"running\"}".getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders()
            .set(HttpHeaderName.AZURE_ASYNCOPERATION, "https://management.azure.com/operationStatuses/1")
            .set(HttpHeaderName.LOCATION, "https://REDACTED.azure.com/operationResults/1")
            .set(HttpHeaderName.RETRY_AFTER, "0");
        HttpResponse response = createResponse(REQUEST_URL, 202, headers, body);

        HttpResponse rewritten = PlaybackPollingUrlRewriter.rewritePollingUrls(response);

        assertSame(response.getRequest(), rewritten.getRequest());
        assertEquals(202, rewritten.getStatusCode());
        assertEquals("https://localhost:8080/operationStatuses/1",
            rewritten.getHeaderValue(HttpHeaderName.AZURE_ASYNCOPERATION));
        assertEquals("https://localhost:8080/operationResults/1", rewritten.getHeaderValue(HttpHeaderName.LOCATION));
        assertEquals("0", rewritten.getHeaderValue(HttpHeaderName.RETRY_AFTER));
        assertArrayEquals(body, rewritten.getBodyAsBinaryData().toBytes());

        // The recorded response isn't modified.
        assertEquals("https://management.azure.com/operationStatuses/1",
            response.getHeaderValue(HttpHeaderName.AZURE_ASYNCOPERATION));
    }

    private static HttpResponse createResponse(String requestUrl, int statusCode, HttpHeaders headers, byte[] body) {
        return new MockHttpResponse(new HttpRequest(HttpMethod.POST, requestUrl), statusCode, headers, body);
    }

    /**
     * Creates a playback client whose mock test proxy replays a Location-only LRO, a resource URL, and a relative
     * upload URL, recording the path of each replayed request.
     */
    private static TestProxyPlaybackClient createPlaybackClient(List<String> transportPaths) throws URISyntaxException {
        HttpClient transport = new HttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.fromSupplier(() -> sendSync(request, Context.NONE));
            }

            @Override
            public HttpResponse sendSync(HttpRequest request, Context context) {
                String path = request.getUrl().getPath();
                if ("/playback/start".equals(path)) {
                    return new MockHttpResponse(request, 200,
                        new HttpHeaders().set(X_RECORDING_ID, "recording-id")
                            .set(X_RECORDING_FILE_LOCATION,
                                Base64.getUrlEncoder()
                                    .encodeToString("recording.json".getBytes(StandardCharsets.UTF_8))),
                        "{}".getBytes(StandardCharsets.UTF_8));
                }

                if (!path.startsWith("/Admin/")) {
                    transportPaths.add(path);
                }
                switch (path) {
                    case "/documentintelligence/documentModels:build":
                        return locationResponse(request, 202, "https://recorded.example/operations/1");

                    case "/operations/1":
                        return locationResponse(request, 202, "https://recorded.example/operations/2");

                    case "/operations/2":
                        return new MockHttpResponse(request, 200,
                            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                            "{\"status\":\"succeeded\"}".getBytes(StandardCharsets.UTF_8));

                    case "/schemas/1":
                        return locationResponse(request, 200, "https://recorded.example/schemas/1");

                    case "/jobs":
                        return locationResponse(request, 201, "https://recorded.batch.azure.com/jobs/job-1");

                    case "/v2/repo/blobs/uploads/absolute":
                        return locationResponse(request, 202, "https://recorded.example/v2/repo/blobs/uploads/1");

                    case "/opt-in/lro":
                        return locationResponse(request, 202, "https://recorded.example/opt-in/operations/1");

                    case "/opt-in/operations/1":
                        return new MockHttpResponse(request, 200,
                            new HttpHeaders().set(HttpHeaderName.LOCATION, "https://recorded.example/opt-in/results/1")
                                .set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                            "{\"status\":\"succeeded\"}".getBytes(StandardCharsets.UTF_8));

                    case "/opt-in/results/1":
                        return new MockHttpResponse(request, 200,
                            new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                            "{\"id\":\"result\"}".getBytes(StandardCharsets.UTF_8));

                    case "/v2/repo/blobs/uploads/":
                        return locationResponse(request, 202, "/v2/repo/blobs/uploads/2");

                    default:
                        return new MockHttpResponse(request, 200, new byte[0]);
                }
            }
        };
        TestProxyPlaybackClient client = new TestProxyPlaybackClient(transport, false);
        client.startPlayback(new File("src/test/resources/session-records/playback.json"), Paths
            .get(PlaybackPollingUrlRewriterTests.class.getProtectionDomain().getCodeSource().getLocation().toURI()));
        return client;
    }

    private static HttpResponse locationResponse(HttpRequest request, int statusCode, String location) {
        return new MockHttpResponse(request, statusCode, new HttpHeaders().set(HttpHeaderName.LOCATION, location),
            new byte[0]);
    }

    /**
     * Creates a pipeline that records each request's method, URL, and whether its context opts into Location rewriting.
     */
    private static HttpPipeline createRecordingPipeline(HttpClient client, List<String> requests) {
        return new HttpPipelineBuilder().policies(new HttpPipelinePolicy() {
            @Override
            public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
                record(context);
                return next.process();
            }

            @Override
            public HttpResponse processSync(HttpPipelineCallContext context, HttpPipelineNextSyncPolicy next) {
                record(context);
                return next.processSync();
            }

            private void record(HttpPipelineCallContext context) {
                boolean optIn = Boolean.TRUE.equals(
                    context.getContext().getData(TestProxyPlaybackClient.REWRITE_LOCATION_HEADER).orElse(false));
                requests.add(context.getHttpRequest().getHttpMethod() + " " + context.getHttpRequest().getUrl()
                    + (optIn ? " opt-in" : ""));
            }
        }).httpClient(client).build();
    }

    /**
     * Runs a POST Location-only LRO to completion, passing {@code context} to the activation request and the polling
     * strategy as generated clients do with {@code RequestOptions}.
     */
    private static BinaryData runLocationPoller(HttpPipeline pipeline, String url, Context context, boolean sync) {
        Duration interval = Duration.ofMillis(1);
        TypeReference<BinaryData> type = TypeReference.createInstance(BinaryData.class);
        PollingStrategyOptions options = new PollingStrategyOptions(pipeline).setContext(context);

        if (sync) {
            return SyncPoller
                .createPoller(interval,
                    () -> toResponse(pipeline.sendSync(new HttpRequest(HttpMethod.POST, url), context)),
                    new SyncLocationPollingStrategy<>(options), type, type)
                .getFinalResult();
        }

        return PollerFlux
            .create(interval,
                () -> pipeline.send(new HttpRequest(HttpMethod.POST, url), context)
                    .map(PlaybackPollingUrlRewriterTests::toResponse),
                new LocationPollingStrategy<>(options), type, type)
            .last()
            .flatMap(AsyncPollResponse::getFinalResult)
            .block();
    }

    private static Response<?> toResponse(HttpResponse response) {
        return new SimpleResponse<Void>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
    }
}
