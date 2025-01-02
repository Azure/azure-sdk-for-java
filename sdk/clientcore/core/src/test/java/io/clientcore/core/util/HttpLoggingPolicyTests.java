// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// we want to access package-private ClientLogger constructor
package io.clientcore.core.util;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpLoggingPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.implementation.util.DefaultLogger;
import io.clientcore.core.serialization.json.JsonOptions;
import io.clientcore.core.serialization.json.JsonProviders;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpLoggingPolicyTests {
    private static final String URI = "https://example.com?param=value&api-version=42";
    private static final String REDACTED_URI = "https://example.com?param=REDACTED&api-version=42";
    private static final Set<String> DEFAULT_ALLOWED_QUERY_PARAMS = new HttpLogOptions().getAllowedQueryParamNames();
    private static final Set<HttpHeaderName> DEFAULT_ALLOWED_HEADERS = new HttpLogOptions().getAllowedHeaderNames();
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    private final AccessibleByteArrayOutputStream logCaptureStream;

    public HttpLoggingPolicyTests() {
        this.logCaptureStream = new AccessibleByteArrayOutputStream();
    }

    @ParameterizedTest
    @MethodSource("disabledHttpLoggingSource")
    public void testDisabledHttpLogging(ClientLogger.LogLevel logLevel, HttpLogOptions.HttpLogDetailLevel httpLogLevel)
        throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(logLevel);

        HttpPipeline pipeline = createPipeline(new HttpLogOptions().setLogLevel(httpLogLevel));
        HttpRequest request = new HttpRequest(HttpMethod.GET, URI);
        request.setRequestOptions(new RequestOptions().setLogger(logger));

        pipeline.send(request).close();

        assertEquals(0, parseLogMessages().size());
    }

    public static Stream<Arguments> disabledHttpLoggingSource() {
        return Stream.of(Arguments.of(ClientLogger.LogLevel.VERBOSE, HttpLogOptions.HttpLogDetailLevel.NONE),
            Arguments.of(ClientLogger.LogLevel.WARNING, HttpLogOptions.HttpLogDetailLevel.BASIC),
            Arguments.of(ClientLogger.LogLevel.WARNING, HttpLogOptions.HttpLogDetailLevel.HEADERS),
            Arguments.of(ClientLogger.LogLevel.WARNING, HttpLogOptions.HttpLogDetailLevel.BODY),
            Arguments.of(ClientLogger.LogLevel.WARNING, HttpLogOptions.HttpLogDetailLevel.BODY_AND_HEADERS));
    }

    @ParameterizedTest
    @MethodSource("allowQueryParamSource")
    public void testBasicHttpLogging(Set<String> allowedParams, String expectedUri) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC)
            .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), expectedUri, request);
        assertEquals(6, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), expectedUri, response);
        assertEquals(10, logMessages.get(1).size());
    }

    @Test
    public void testTryCount() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        HttpRequestAccessHelper.setRetryCount(request, 42);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        assertEquals(42, logMessages.get(0).get("tryCount"));
        assertEquals(42, logMessages.get(1).get("tryCount"));
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testConnectionException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS);

        RuntimeException expectedException = new RuntimeException("socket error");
        HttpPipeline pipeline = createPipeline(options, request -> {
            throw expectedException;
        });

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages();
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), REDACTED_URI, request, expectedException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testRequestBodyException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(1024, true, new IOException("socket error"));
        BinaryData requestBody = BinaryData.fromStream(requestStream, 1024L);
        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        request.setBody(requestBody);

        Exception actualException = assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages();
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), REDACTED_URI, request, actualException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testResponseBodyException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream responseStream = new TestStream(1024, true, new IOException("socket error"));
        HttpPipeline pipeline = createPipeline(options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromStream(responseStream, 1024L)));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);
        Exception actualException = assertThrows(RuntimeException.class, () -> response.getBody().toString());

        List<Map<String, Object>> logMessages = parseLogMessages();
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertResponseAndExceptionLog(logMessages.get(0), REDACTED_URI, response, actualException);
        }
    }

    @Test
    public void testResponseBodyLoggingOnClose() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);
        assertEquals(0, parseLogMessages().size());

        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertResponseLog(logMessages.get(0), REDACTED_URI, response);
    }

    @Test
    public void testResponseBodyRequestedMultipleTimes() {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);

        for (int i = 0; i < 3; i++) {
            BinaryData data = response.getBody();
            assertEquals(1, parseLogMessages().size());
            assertEquals("Response body", data.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("allowQueryParamSource")
    public void testBasicHttpLoggingRequestOff(Set<String> allowedParams, String expectedUri) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC)
            .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(1, logMessages.size());

        assertResponseLog(logMessages.get(0), expectedUri, response);
        assertEquals(10, logMessages.get(0).size());
    }

    @ParameterizedTest
    @MethodSource("allowedHeaders")
    public void testHeadersHttpLogging(Set<HttpHeaderName> allowedHeaders) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS)
            .setAllowedHeaderNames(allowedHeaders);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        request.getHeaders().set(CUSTOM_REQUEST_ID, "12345");
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        for (HttpHeader header : request.getHeaders()) {
            if (allowedHeaders.contains(header.getName())) {
                assertEquals(header.getValue(), requestLog.get(header.getName().toString()));
            } else {
                assertEquals("REDACTED", requestLog.get(header.getName().toString()));
            }
        }

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        for (HttpHeader header : response.getHeaders()) {
            if (allowedHeaders.contains(header.getName())) {
                assertEquals(header.getValue(), responseLog.get(header.getName().toString()));
            } else {
                assertEquals("REDACTED", responseLog.get(header.getName().toString()));
            }
        }
    }

    @Test
    public void testStringBodyLogging() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.setBody(BinaryData.fromString("Request body"));

        Response<?> response = pipeline.send(request);
        response.close();

        assertEquals("Response body", response.getBody().toString());

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertEquals("Request body", requestLog.get("body"));

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertEquals("Response body", responseLog.get("body"));
    }

    @Test
    public void testHugeBodyNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(Integer.MAX_VALUE, true);
        TestStream responseStream = new TestStream(Integer.MAX_VALUE, true);
        HttpPipeline pipeline = createPipeline(options, request -> new MockHttpResponse(request, 200,
            BinaryData.fromStream(responseStream, (long) Integer.MAX_VALUE)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);

        request.setBody(BinaryData.fromStream(requestStream, (long) Integer.MAX_VALUE));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertNull(requestLog.get("body"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertNull(responseLog.get("body"));
        assertEquals(0, responseStream.getPosition());
    }

    @Test
    public void testNonReplayableBodyNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(1024, false);
        TestStream responseStream = new TestStream(1024, false);
        HttpPipeline pipeline = createPipeline(options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromStream(responseStream)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "1024");

        request.setBody(BinaryData.fromStream(requestStream));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages();
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertNull(requestLog.get("body"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertNull(responseLog.get("body"));
        assertEquals(0, responseStream.getPosition());
    }

    public static Stream<Arguments> allowQueryParamSource() {
        Set<String> twoParams = new HashSet<>();
        twoParams.add("param");
        twoParams.add("api-version");

        return Stream.of(Arguments.of(twoParams, "https://example.com?param=value&api-version=42"),
            Arguments.of(DEFAULT_ALLOWED_QUERY_PARAMS, REDACTED_URI),
            Arguments.of(Collections.emptySet(), "https://example.com?param=REDACTED&api-version=REDACTED"));
    }

    public static Stream<Set<HttpHeaderName>> allowedHeaders() {
        Set<HttpHeaderName> reducedSet = new HashSet<>();
        reducedSet.add(CUSTOM_REQUEST_ID);

        Set<HttpHeaderName> expandedSet = new HashSet<>(DEFAULT_ALLOWED_HEADERS);
        expandedSet.add(CUSTOM_REQUEST_ID);

        return Stream.of(reducedSet, DEFAULT_ALLOWED_HEADERS, expandedSet);
    }

    public static Stream<Arguments> testExceptionSeverity() {
        return Stream.of(Arguments.of(ClientLogger.LogLevel.INFORMATIONAL, true),
            Arguments.of(ClientLogger.LogLevel.WARNING, true), Arguments.of(ClientLogger.LogLevel.ERROR, false));
    }

    private static class TestStream extends InputStream {
        private final long length;
        private final boolean replayable;
        private final IOException throwOnRead;
        private long position = 0;

        TestStream(long length, boolean replayable) {
            this.length = length;
            this.throwOnRead = null;
            this.replayable = replayable;
        }

        TestStream(long length, boolean replayable, IOException throwOnRead) {
            this.length = length;
            this.throwOnRead = throwOnRead;
            this.replayable = replayable;
        }

        @Override
        public int read() throws IOException {
            if (throwOnRead != null) {
                throw throwOnRead;
            }

            if (position >= length) {
                return -1;
            }
            position++;

            return 1;
        }

        @Override
        public boolean markSupported() {
            return replayable;
        }

        @Override
        public void mark(int readlimit) {
            // no-op
        }

        @Override
        public void reset() throws IOException {
            if (!replayable) {
                throw new IOException("Stream is not replayable");
            }

            position = 0;
        }

        public long getPosition() {
            return position;
        }
    }

    private void assertRequestLog(Map<String, Object> log, String expectedUri, HttpRequest request) {
        assertEquals("http.request", log.get("event.name"));
        assertEquals(expectedUri, log.get("uri"));
        assertEquals(0, (int) log.get("tryCount"));

        assertEquals(getLength(request.getBody(), request.getHeaders()), (int) log.get("requestContentLength"));
        assertEquals(request.getHttpMethod().toString(), log.get("method"));
        assertEquals("", log.get("message"));
    }

    private long getLength(BinaryData body, HttpHeaders headers) {
        if (body != null && body.getLength() != null) {
            return body.getLength();
        }

        String contentLength = headers.getValue(HttpHeaderName.CONTENT_LENGTH);
        if (contentLength != null) {
            return Long.parseLong(contentLength);
        }

        return 0;
    }

    private void assertResponseLog(Map<String, Object> log, String expectedUri, Response<?> response) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("uri"));
        assertEquals(0, (int) log.get("tryCount"));

        Long expectedRequestLength = getLength(response.getRequest().getBody(), response.getRequest().getHeaders());

        assertEquals(expectedRequestLength, (int) log.get("requestContentLength"));
        assertEquals(response.getRequest().getHttpMethod().toString(), log.get("method"));

        assertEquals(response.getStatusCode(), log.get("statusCode"));

        Long expectedResponseLength = getLength(response.getBody(), response.getHeaders());
        assertEquals(expectedResponseLength, (int) log.get("responseContentLength"));
        assertInstanceOf(Double.class, log.get("timeToHeadersMs"));
        assertInstanceOf(Double.class, log.get("durationMs"));
        assertEquals("", log.get("message"));
    }

    private void assertResponseAndExceptionLog(Map<String, Object> log, String expectedUri, Response<?> response,
        Throwable error) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("uri"));
        assertEquals(0, (int) log.get("tryCount"));

        Long expectedRequestLength = getLength(response.getRequest().getBody(), response.getRequest().getHeaders());

        assertEquals(expectedRequestLength, (int) log.get("requestContentLength"));
        assertEquals(response.getRequest().getHttpMethod().toString(), log.get("method"));

        assertEquals(response.getStatusCode(), log.get("statusCode"));

        Long expectedResponseLength = getLength(response.getBody(), response.getHeaders());
        assertEquals(expectedResponseLength, (int) log.get("responseContentLength"));
        assertInstanceOf(Double.class, log.get("timeToHeadersMs"));
        assertInstanceOf(Double.class, log.get("durationMs"));
        assertEquals(error.getMessage(), log.get("exception.message"));
        assertEquals(error.getClass().getCanonicalName(), log.get("exception.type"));
        assertEquals("", log.get("message"));
    }

    private void assertExceptionLog(Map<String, Object> log, String expectedUri, HttpRequest request, Throwable error) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("uri"));
        assertEquals(0, (int) log.get("tryCount"));

        Long expectedRequestLength = getLength(request.getBody(), request.getHeaders());
        assertEquals(expectedRequestLength, (int) log.get("requestContentLength"));
        assertEquals(request.getHttpMethod().toString(), log.get("method"));

        assertNull(log.get("statusCode"));
        assertNull(log.get("responseContentLength"));
        assertNull(log.get("timeToHeadersMs"));
        assertInstanceOf(Double.class, log.get("durationMs"));
        assertEquals(error.getMessage(), log.get("exception.message"));
        assertEquals(error.getClass().getCanonicalName(), log.get("exception.type"));

        assertEquals("", log.get("message"));
    }

    private ClientLogger setupLogLevelAndGetLogger(ClientLogger.LogLevel logLevelToSet) {
        DefaultLogger logger
            = new DefaultLogger(ClientLogger.class.getName(), new PrintStream(logCaptureStream), logLevelToSet);

        return new ClientLogger(logger, null);
    }

    private HttpPipeline createPipeline(HttpLogOptions options) {
        return createPipeline(options, request -> {
            if (request.getBody() != null) {
                request.getBody().toString();
            }
            return new MockHttpResponse(request, 200, BinaryData.fromString("Hello, world!"));
        });
    }

    private HttpPipeline createPipeline(HttpLogOptions options, Function<HttpRequest, Response<?>> httpClient) {
        return new HttpPipelineBuilder().policies(new HttpLoggingPolicy(options)).httpClient(httpClient::apply).build();
    }

    private List<Map<String, Object>> parseLogMessages() {
        String fullLog = logCaptureStream.toString(StandardCharsets.UTF_8);
        return fullLog.lines().map(this::parseLogLine).toList();
    }

    private Map<String, Object> parseLogLine(String logLine) {
        String messageJson = logLine.substring(logLine.indexOf(" - ") + 3);
        System.out.println(messageJson);
        try (JsonReader reader = JsonProviders.createReader(messageJson, new JsonOptions())) {
            return reader.readMap(JsonReader::readUntyped);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private HttpRequest createRequest(HttpMethod method, String url, ClientLogger logger) {
        HttpRequest request = new HttpRequest(method, url);
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer {token}");
        request.setRequestOptions(new RequestOptions().setLogger(logger));

        return request;
    }
}
