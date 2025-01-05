// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.MockHttpResponse;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpLogOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.util.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.parseLogMessages;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.setupLogLevelAndGetLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpInstrumentationPolicyLoggingTests {
    private static final String URI = "https://example.com?param=value&api-version=42";
    private static final String REDACTED_URI = "https://example.com?param=REDACTED&api-version=42";
    private static final Set<String> DEFAULT_ALLOWED_QUERY_PARAMS = new HttpLogOptions().getAllowedQueryParamNames();
    private static final Set<HttpHeaderName> DEFAULT_ALLOWED_HEADERS = new HttpLogOptions().getAllowedHeaderNames();
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");
    private static final InstrumentationOptions<?> DEFAULT_INSTRUMENTATION_OPTIONS = null;

    private final AccessibleByteArrayOutputStream logCaptureStream;

    public HttpInstrumentationPolicyLoggingTests() {
        this.logCaptureStream = new AccessibleByteArrayOutputStream();
    }

    @ParameterizedTest
    @MethodSource("disabledHttpLoggingSource")
    public void testDisabledHttpLogging(ClientLogger.LogLevel logLevel, HttpLogOptions.HttpLogDetailLevel httpLogLevel)
        throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(logLevel, logCaptureStream);

        HttpPipeline pipeline
            = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, new HttpLogOptions().setLogLevel(httpLogLevel));
        HttpRequest request = new HttpRequest(HttpMethod.GET, URI);
        request.setRequestOptions(new RequestOptions().setLogger(logger));

        pipeline.send(request).close();

        assertEquals(0, parseLogMessages(logCaptureStream).size());
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
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC)
            .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), expectedUri, request);
        assertEquals(8, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), expectedUri, response);
        assertEquals(12, logMessages.get(1).size());
    }

    @Test
    public void testHttpLoggingTracingDisabled() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions<?> instrumentationOptions = new InstrumentationOptions<>().setTracingEnabled(false);
        HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC);

        HttpPipeline pipeline = createPipeline(instrumentationOptions, logOptions);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), REDACTED_URI, request);
        assertEquals(6, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), REDACTED_URI, response);
        assertEquals(10, logMessages.get(1).size());
    }

    @Test
    public void testHttpLoggingTracingDisabledCustomContext() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        InstrumentationOptions<?> instrumentationOptions = new InstrumentationOptions<>().setTracingEnabled(false);
        HttpLogOptions logOptions = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC);

        HttpPipeline pipeline = createPipeline(instrumentationOptions, logOptions);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        request.setRequestOptions(new RequestOptions().setLogger(logger)
            .setInstrumentationContext(
                createInstrumentationContext("1234567890abcdef1234567890abcdef", "1234567890abcdef")));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), REDACTED_URI, request);
        assertEquals(8, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), REDACTED_URI, response);
        assertEquals(12, logMessages.get(1).size());
    }

    @Test
    public void testTryCount() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        HttpRequestAccessHelper.setRetryCount(request, 42);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertEquals(42, logMessages.get(0).get("http.request.resend_count"));
        assertEquals(42, logMessages.get(1).get("http.request.resend_count"));
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testConnectionException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS);

        RuntimeException expectedException = new RuntimeException("socket error");
        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options, request -> {
            throw expectedException;
        });

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), REDACTED_URI, request, expectedException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testRequestBodyException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(1024, new IOException("socket error"));
        BinaryData requestBody = BinaryData.fromStream(requestStream, 1024L);
        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        request.setBody(requestBody);

        Exception actualException = assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), REDACTED_URI, request, actualException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testResponseBodyException(ClientLogger.LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream responseStream = new TestStream(1024, new IOException("socket error"));
        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromStream(responseStream, 1024L)));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);
        Exception actualException = assertThrows(RuntimeException.class, () -> response.getBody().toString());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertResponseAndExceptionLog(logMessages.get(0), REDACTED_URI, response, actualException);
        }
    }

    @Test
    public void testResponseBodyLoggingOnClose() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);
        assertEquals(0, parseLogMessages(logCaptureStream).size());

        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertResponseLog(logMessages.get(0), REDACTED_URI, response);
    }

    @Test
    public void testResponseBodyRequestedMultipleTimes() {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);

        for (int i = 0; i < 3; i++) {
            BinaryData data = response.getBody();
            assertEquals(1, parseLogMessages(logCaptureStream).size());
            assertEquals("Response body", data.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("allowQueryParamSource")
    public void testBasicHttpLoggingRequestOff(Set<String> allowedParams, String expectedUri) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.INFORMATIONAL, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BASIC)
            .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(1, logMessages.size());

        assertResponseLog(logMessages.get(0), expectedUri, response);
        assertEquals(12, logMessages.get(0).size());
    }

    @ParameterizedTest
    @MethodSource("allowedHeaders")
    public void testHeadersHttpLogging(Set<HttpHeaderName> allowedHeaders) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.HEADERS)
            .setAllowedHeaderNames(allowedHeaders);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        request.getHeaders().set(CUSTOM_REQUEST_ID, "12345");
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
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
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.setBody(BinaryData.fromString("Request body"));

        Response<?> response = pipeline.send(request);
        response.close();

        assertEquals("Response body", response.getBody().toString());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertEquals("Request body", requestLog.get("http.request.body.content"));

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertEquals("Response body", responseLog.get("http.request.body.content"));
    }

    @Test
    public void testStreamBodyLogging() {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        BinaryData responseBody = BinaryData.fromString("Response body");
        TestStream responseStream = new TestStream(responseBody);

        HttpPipeline pipeline
            = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options, request -> new MockHttpResponse(request, 200,
                BinaryData.fromStream(responseStream, responseBody.getLength())));

        BinaryData requestBody = BinaryData.fromString("Request body");
        TestStream requestStream = new TestStream(requestBody);
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.setBody(BinaryData.fromStream(requestStream, requestBody.getLength()));
        assertFalse(request.getBody().isReplayable());

        Response<?> response = pipeline.send(request);
        assertTrue(request.getBody().isReplayable());
        assertTrue(response.getBody().isReplayable());

        assertEquals("Response body", response.getBody().toString());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertEquals("Request body", requestLog.get("http.request.body.content"));

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertEquals("Response body", responseLog.get("http.request.body.content"));

        assertEquals(requestBody.getLength(), requestStream.getPosition());
        assertEquals(responseBody.getLength(), responseStream.getPosition());
    }

    @Test
    public void testHugeBodyNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(1024 * 1024);
        TestStream responseStream = new TestStream(1024 * 1024);
        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromStream(responseStream, (long) 1024 * 1024)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);

        request.setBody(BinaryData.fromStream(requestStream, 1024 * 1024L));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertNull(requestLog.get("http.request.body.content"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertNull(responseLog.get("http.request.body.content"));
        assertEquals(0, responseStream.getPosition());
    }

    @Test
    public void testBodyWithUnknownLengthNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(ClientLogger.LogLevel.VERBOSE, logCaptureStream);
        HttpLogOptions options = new HttpLogOptions().setLogLevel(HttpLogOptions.HttpLogDetailLevel.BODY);

        TestStream requestStream = new TestStream(1024);
        TestStream responseStream = new TestStream(1024);
        HttpPipeline pipeline = createPipeline(DEFAULT_INSTRUMENTATION_OPTIONS, options,
            request -> new MockHttpResponse(request, 200, BinaryData.fromStream(responseStream)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "1024");

        request.setBody(BinaryData.fromStream(requestStream));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, REDACTED_URI, request);
        assertNull(requestLog.get("http.request.body.content"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, REDACTED_URI, response);
        assertNull(responseLog.get("http.request.body.content"));
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
        private final byte[] content;
        private int length;
        private final IOException throwOnRead;
        private int position = 0;

        TestStream(int length) {
            this.length = length;
            this.throwOnRead = null;
            this.content = new byte[length];
        }

        TestStream(BinaryData content) {
            this.length = content.getLength().intValue();
            this.throwOnRead = null;
            this.content = content.toBytes();
        }

        TestStream(int length, IOException throwOnRead) {
            this.length = length;
            this.throwOnRead = throwOnRead;
            this.content = new byte[length];
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
            return content[position - 1];
        }

        public long getPosition() {
            return position;
        }
    }

    private void assertRequestLog(Map<String, Object> log, String expectedUri, HttpRequest request) {
        assertEquals("http.request", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(0, (int) log.get("http.request.resend_count"));

        assertEquals(getLength(request.getBody(), request.getHeaders()), (int) log.get("http.request.body.size"));
        assertEquals(request.getHttpMethod().toString(), log.get("http.request.method"));
        assertEquals("", log.get("message"));

        assertTraceContext(log, request);
    }

    private void assertTraceContext(Map<String, Object> log, HttpRequest request) {
        InstrumentationContext context = request.getRequestOptions().getInstrumentationContext();
        if (context != null) {
            assertTrue(log.get("trace.id").toString().matches("[0-9a-f]{32}"));
            assertTrue(log.get("span.id").toString().matches("[0-9a-f]{16}"));

            assertEquals(context.getTraceId(), log.get("trace.id"));
            assertEquals(context.getSpanId(), log.get("span.id"));
        } else {
            assertNull(log.get("trace.id"));
            assertNull(log.get("span.id"));
        }
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
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(0, (int) log.get("http.request.resend_count"));

        Long expectedRequestLength = getLength(response.getRequest().getBody(), response.getRequest().getHeaders());

        assertEquals(expectedRequestLength, (int) log.get("http.request.body.size"));
        assertEquals(response.getRequest().getHttpMethod().toString(), log.get("http.request.method"));

        assertEquals(response.getStatusCode(), log.get("http.response.status_code"));

        Long expectedResponseLength = getLength(response.getBody(), response.getHeaders());
        assertEquals(expectedResponseLength, (int) log.get("http.response.body.size"));
        assertInstanceOf(Double.class, log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
        assertEquals("", log.get("message"));
        assertTraceContext(log, response.getRequest());
    }

    private void assertResponseAndExceptionLog(Map<String, Object> log, String expectedUri, Response<?> response,
        Throwable error) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(0, (int) log.get("http.request.resend_count"));

        Long expectedRequestLength = getLength(response.getRequest().getBody(), response.getRequest().getHeaders());

        assertEquals(expectedRequestLength, (int) log.get("http.request.body.size"));
        assertEquals(response.getRequest().getHttpMethod().toString(), log.get("http.request.method"));

        assertEquals(response.getStatusCode(), log.get("http.response.status_code"));

        assertInstanceOf(Double.class, log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
        assertEquals(error.getMessage(), log.get("exception.message"));
        assertEquals(error.getClass().getCanonicalName(), log.get("exception.type"));
        assertEquals("", log.get("message"));
        assertTraceContext(log, response.getRequest());
    }

    private void assertExceptionLog(Map<String, Object> log, String expectedUri, HttpRequest request, Throwable error) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(0, (int) log.get("http.request.resend_count"));

        Long expectedRequestLength = getLength(request.getBody(), request.getHeaders());
        assertEquals(expectedRequestLength, (int) log.get("http.request.body.size"));
        assertEquals(request.getHttpMethod().toString(), log.get("http.request.method"));

        assertNull(log.get("http.response.status_code"));
        assertNull(log.get("http.response.body.size"));
        assertNull(log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
        assertEquals(error.getMessage(), log.get("exception.message"));
        assertEquals(error.getClass().getCanonicalName(), log.get("exception.type"));

        assertEquals("", log.get("message"));
        assertTraceContext(log, request);
    }

    private HttpPipeline createPipeline(InstrumentationOptions<?> instrumentationOptions, HttpLogOptions options) {
        return createPipeline(instrumentationOptions, options, request -> {
            if (request.getBody() != null) {
                request.getBody().toString();
            }
            return new MockHttpResponse(request, 200, BinaryData.fromString("Hello, world!"));
        });
    }

    private HttpPipeline createPipeline(InstrumentationOptions<?> instrumentationOptions, HttpLogOptions options,
        Function<HttpRequest, Response<?>> httpClient) {
        return new HttpPipelineBuilder().policies(new HttpInstrumentationPolicy(instrumentationOptions, options))
            .httpClient(httpClient::apply)
            .build();
    }

    private HttpRequest createRequest(HttpMethod method, String url, ClientLogger logger) {
        HttpRequest request = new HttpRequest(method, url);
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer {token}");
        request.setRequestOptions(new RequestOptions().setLogger(logger));

        return request;
    }
}
