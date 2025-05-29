// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.pipeline;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.implementation.AccessibleByteArrayOutputStream;
import io.clientcore.core.implementation.http.HttpRequestAccessHelper;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import static io.clientcore.core.http.models.HttpHeaderName.TRACEPARENT;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.createRandomInstrumentationContext;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.parseLogMessages;
import static io.clientcore.core.instrumentation.logging.InstrumentationTestUtils.setupLogLevelAndGetLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class HttpInstrumentationLoggingTests {
    private static final String URI = "https://example.com?param=value&api-version=42";
    private static final String REDACTED_URI = "https://example.com?param=REDACTED&api-version=42";
    private static final Set<String> DEFAULT_ALLOWED_QUERY_PARAMS
        = new HttpInstrumentationOptions().getAllowedQueryParamNames();
    private static final Set<HttpHeaderName> DEFAULT_ALLOWED_HEADERS
        = new HttpInstrumentationOptions().getAllowedHeaderNames();
    private static final HttpHeaderName CUSTOM_REQUEST_ID = HttpHeaderName.fromString("custom-request-id");

    private final AccessibleByteArrayOutputStream logCaptureStream;

    public HttpInstrumentationLoggingTests() {
        this.logCaptureStream = new AccessibleByteArrayOutputStream();
    }

    @ParameterizedTest
    @MethodSource("disabledHttpLoggingSource")
    public void testDisabledHttpLogging(LogLevel logLevel, HttpInstrumentationOptions.HttpLogLevel detailLevel)
        throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(logLevel, logCaptureStream);

        RequestContext context = RequestContext.builder().setLogger(logger).build();

        HttpPipeline pipeline = createPipeline(new HttpInstrumentationOptions().setHttpLogLevel(detailLevel));
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri(URI);
        request.setContext(context);

        pipeline.send(request).close();

        assertEquals(0, parseLogMessages(logCaptureStream).size());
    }

    public static Stream<Arguments> disabledHttpLoggingSource() {
        return Stream.of(Arguments.of(LogLevel.VERBOSE, HttpInstrumentationOptions.HttpLogLevel.NONE),
            Arguments.of(LogLevel.WARNING, HttpInstrumentationOptions.HttpLogLevel.HEADERS),
            Arguments.of(LogLevel.WARNING, HttpInstrumentationOptions.HttpLogLevel.BODY),
            Arguments.of(LogLevel.WARNING, HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS));
    }

    @ParameterizedTest
    @MethodSource("allowQueryParamSource")
    public void testBasicHttpLogging(Set<String> allowedParams, String expectedUri) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)
                .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), expectedUri, request, null, 0);
        assertEquals(10, logMessages.get(0).size());
        assertEquals("REDACTED", logMessages.get(0).get("Authorization"));
        assertEquals("application/json", logMessages.get(0).get("Content-Type"));

        assertResponseLog(logMessages.get(1), expectedUri, response, 0);
        assertEquals(14, logMessages.get(1).size());
        assertEquals("13", logMessages.get(1).get("Content-Length"));
        assertEquals("application/text", logMessages.get(1).get("Content-Type"));
        assertEquals("REDACTED", logMessages.get(1).get("not-safe-to-log"));
    }

    @Test
    public void testBasicHttpLoggingNoRedactedHeaders() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)
                .setRedactedHeaderNamesLoggingEnabled(false);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), request);
        assertEquals(9, logMessages.get(0).size());
        assertEquals("application/json", logMessages.get(0).get("Content-Type"));

        assertResponseLog(logMessages.get(1), response);
        assertEquals(13, logMessages.get(1).size());
        assertEquals("13", logMessages.get(1).get("Content-Length"));
        assertEquals("application/text", logMessages.get(1).get("Content-Type"));
    }

    @Test
    public void testHttpLoggingTracingDisabled() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);

        HttpInstrumentationOptions options = new HttpInstrumentationOptions().setTracingEnabled(false)
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)
            .setRedactedHeaderNamesLoggingEnabled(false);
        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), request);
        assertEquals(6, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), response);
        assertEquals(11, logMessages.get(1).size());
    }

    @Test
    public void testHttpLoggingTracingDisabledCustomContext() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions().setTracingEnabled(false)
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS);

        HttpPipeline pipeline = createPipeline(options);

        InstrumentationContext instrumentationContext
            = createInstrumentationContext("1234567890abcdef1234567890abcdef", "1234567890abcdef");
        HttpRequest request = createRequest(HttpMethod.GET, URI, logger, instrumentationContext);

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertRequestLog(logMessages.get(0), request);
        assertEquals(10, logMessages.get(0).size());

        assertResponseLog(logMessages.get(1), response);
        assertEquals(14, logMessages.get(1).size());
    }

    @Test
    public void testTryCount() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        HttpRequestAccessHelper.setTryCount(request, 42);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        assertEquals(42, logMessages.get(0).get("http.request.resend_count"));
        assertEquals(42, logMessages.get(1).get("http.request.resend_count"));
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testConnectionException(LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS);

        RuntimeException expectedException = new RuntimeException("socket error");
        HttpPipeline pipeline = createPipeline(options, request -> {
            throw expectedException;
        });

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), request, expectedException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testRequestBodyException(LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        IOException expectedException = new IOException("socket error");
        TestStream requestStream = new TestStream(1024, expectedException);
        BinaryData requestBody = BinaryData.fromStream(requestStream, 1024L);
        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        request.setBody(requestBody);

        assertThrows(RuntimeException.class, () -> pipeline.send(request));

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertExceptionLog(logMessages.get(0), request, expectedException);
        }
    }

    @ParameterizedTest
    @MethodSource("testExceptionSeverity")
    public void testResponseBodyException(LogLevel level, boolean expectExceptionLog) {
        ClientLogger logger = setupLogLevelAndGetLogger(level, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        IOException expectedException = new IOException("socket error");
        TestStream responseStream = new TestStream(1024, expectedException);
        HttpPipeline pipeline = createPipeline(options,
            request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.fromStream(responseStream, 1024L)));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<BinaryData> response = pipeline.send(request);
        assertThrows(RuntimeException.class, () -> response.getValue().toBytes());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        if (!expectExceptionLog) {
            assertEquals(0, logMessages.size());
        } else {
            assertResponseAndExceptionLog(logMessages.get(0), REDACTED_URI, response, expectedException);
        }
    }

    @Test
    public void testResponseBodyLoggingOnClose() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.INFORMATIONAL, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        HttpPipeline pipeline = createPipeline(options,
            request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<?> response = pipeline.send(request);
        assertEquals(0, parseLogMessages(logCaptureStream).size());

        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertResponseLog(logMessages.get(0), response);
    }

    @Test
    public void testResponseBodyRequestedMultipleTimes() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.INFORMATIONAL, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        HttpPipeline pipeline = createPipeline(options,
            request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);

        Response<BinaryData> response = pipeline.send(request);

        for (int i = 0; i < 3; i++) {
            BinaryData data = response.getValue();
            assertEquals(1, parseLogMessages(logCaptureStream).size());
            assertEquals("Response body", data.toString());
        }
    }

    @ParameterizedTest
    @MethodSource("allowQueryParamSource")
    public void testBasicHttpLoggingRequestOff(Set<String> allowedParams, String expectedUri) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.INFORMATIONAL, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)
                .setAllowedQueryParamNames(allowedParams);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.POST, URI, logger);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(1, logMessages.size());

        assertResponseLog(logMessages.get(0), expectedUri, response, 0);
        assertEquals(14, logMessages.get(0).size());
    }

    @ParameterizedTest
    @MethodSource("allowedHeaders")
    public void testHeadersHttpLogging(Set<HttpHeaderName> allowedHeaders) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options
            = new HttpInstrumentationOptions().setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.HEADERS)
                .setAllowedHeaderNames(allowedHeaders);

        HttpPipeline pipeline = createPipeline(options);

        HttpRequest request = createRequest(HttpMethod.GET, URI, logger);
        request.getHeaders().set(CUSTOM_REQUEST_ID, "12345");
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, request);
        request.getHeaders().stream().forEach(header -> {
            if (allowedHeaders.contains(header.getName())) {
                assertEquals(header.getValue(), requestLog.get(header.getName().toString()));
            } else {
                assertEquals("REDACTED", requestLog.get(header.getName().toString()));
            }
        });

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, response);
        response.getHeaders().stream().forEach(header -> {
            if (allowedHeaders.contains(header.getName())) {
                assertEquals(header.getValue(), responseLog.get(header.getName().toString()));
            } else {
                assertEquals("REDACTED", responseLog.get(header.getName().toString()));
            }
        });
    }

    @Test
    public void testStringBodyLogging() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        HttpPipeline pipeline = createPipeline(options,
            request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.fromString("Response body")));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.setBody(BinaryData.fromString("Request body"));

        Response<BinaryData> response = pipeline.send(request);
        response.close();

        assertEquals("Response body", response.getValue().toString());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, request);
        assertEquals("Request body", requestLog.get("http.request.body.content"));

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, response);
        assertEquals("Response body", responseLog.get("http.response.body.content"));
    }

    @Test
    public void testStreamBodyLogging() {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        BinaryData responseBody = BinaryData.fromString("Response body");
        TestStream responseStream = new TestStream(responseBody);

        HttpPipeline pipeline = createPipeline(options, request -> new Response<>(request, 200, new HttpHeaders(),
            BinaryData.fromStream(responseStream, responseBody.getLength())));

        BinaryData requestBody = BinaryData.fromString("Request body");
        TestStream requestStream = new TestStream(requestBody);
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.setBody(BinaryData.fromStream(requestStream, requestBody.getLength()));
        assertFalse(request.getBody().isReplayable());

        Response<BinaryData> response = pipeline.send(request);
        assertTrue(request.getBody().isReplayable());
        assertTrue(response.getValue().isReplayable());

        assertEquals("Response body", response.getValue().toString());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, request);
        assertEquals("Request body", requestLog.get("http.request.body.content"));

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, response);
        assertEquals("Response body", responseLog.get("http.response.body.content"));

        assertEquals(requestBody.getLength(), requestStream.getPosition());
        assertEquals(responseBody.getLength(), responseStream.getPosition());
    }

    @Test
    public void testHugeBodyNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        TestStream requestStream = new TestStream(1024 * 1024);
        TestStream responseStream = new TestStream(1024 * 1024);
        HttpPipeline pipeline = createPipeline(options, request -> new Response<>(request, 200, new HttpHeaders(),
            BinaryData.fromStream(responseStream, (long) 1024 * 1024)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);

        request.setBody(BinaryData.fromStream(requestStream, 1024 * 1024L));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, request);
        assertNull(requestLog.get("http.request.body.content"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, response);
        assertNull(responseLog.get("http.request.body.content"));
        assertEquals(0, responseStream.getPosition());
    }

    @Test
    public void testBodyWithUnknownLengthNotLogged() throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        TestStream requestStream = new TestStream(1024);
        TestStream responseStream = new TestStream(1024);
        HttpPipeline pipeline = createPipeline(options,
            request -> new Response<>(request, 200, new HttpHeaders(), BinaryData.fromStream(responseStream)));

        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger);
        request.getHeaders().set(HttpHeaderName.CONTENT_LENGTH, "1024");

        request.setBody(BinaryData.fromStream(requestStream));

        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(2, logMessages.size());

        Map<String, Object> requestLog = logMessages.get(0);
        assertRequestLog(requestLog, request);
        assertNull(requestLog.get("http.request.body.content"));
        assertEquals(0, requestStream.getPosition());

        Map<String, Object> responseLog = logMessages.get(1);
        assertResponseLog(responseLog, response);
        assertNull(responseLog.get("http.request.body.content"));
        assertEquals(0, responseStream.getPosition());
    }

    @SuppressWarnings("try")
    @Test
    public void tracingWithRetriesException() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        AtomicReference<InstrumentationContext> firstTryContext = new AtomicReference<>();
        UnknownHostException expectedException = new UnknownHostException("test exception");
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(options))
            .httpClient(request -> {
                assertEquals(traceparent(request.getContext().getInstrumentationContext()),
                    request.getHeaders().get(TRACEPARENT).getValue());
                if (count.getAndIncrement() == 0) {
                    firstTryContext.set(request.getContext().getInstrumentationContext());
                    throw CoreException.from(expectedException);
                } else {
                    return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                }
            })
            .build();

        InstrumentationContext parentContext
            = createInstrumentationContext("1234567890abcdef1234567890abcdef", "1234567890abcdef");
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();

        assertEquals(2, count.get());
        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(5, logMessages.size());
        assertRequestLog(logMessages.get(0), REDACTED_URI, request, firstTryContext.get(), 0);
        assertExceptionLog(logMessages.get(1), REDACTED_URI, request, expectedException, firstTryContext.get(), 0);

        assertRetryLog(logMessages.get(2), 0, 3, true, parentContext);

        assertRequestLog(logMessages.get(3), REDACTED_URI, request, null, 1);
        assertResponseLog(logMessages.get(4), REDACTED_URI, response, 1);
    }

    @Test
    public void tracingWithRetriesStatusCode() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        AtomicReference<InstrumentationContext> firstTryContext = new AtomicReference<>();

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy())
            .addPolicy(new HttpInstrumentationPolicy(options))
            .httpClient(request -> {
                if (count.getAndIncrement() == 0) {
                    firstTryContext.set(request.getContext().getInstrumentationContext());
                    return new Response<>(request, 500, new HttpHeaders(), BinaryData.empty());
                } else {
                    return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                }
            })
            .build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();

        assertEquals(2, count.get());
        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);
        assertEquals(5, logMessages.size());
        assertResponseLog(logMessages.get(1), REDACTED_URI, 0, 500, firstTryContext.get());
        assertRetryLog(logMessages.get(2), 0, 3, true, parentContext);
        assertResponseLog(logMessages.get(4), REDACTED_URI, response, 1);
    }

    @ParameterizedTest
    @MethodSource("logLevels")
    public void retryPolicyLoggingRetriesExhausted(LogLevel logLevel, boolean expectRetryingLogs,
        boolean expectExhaustedLog) throws IOException {
        ClientLogger logger = setupLogLevelAndGetLogger(logLevel, logCaptureStream);

        int maxRetries = 3;
        HttpRetryOptions retryOptions = new HttpRetryOptions(maxRetries, Duration.ofMillis(5));

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRetryPolicy(retryOptions))
            .httpClient(request -> new Response<>(request, 500, new HttpHeaders(), BinaryData.empty()))
            .build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        int expectedLogCount = expectRetryingLogs ? maxRetries : 0;
        if (expectExhaustedLog) {
            expectedLogCount++;
        }

        assertEquals(expectedLogCount, logMessages.size());

        if (expectRetryingLogs) {
            for (int i = 0; i < maxRetries; i++) {
                assertRetryLog(logMessages.get(i), i, 3, true, parentContext);
            }
        }

        if (expectExhaustedLog) {
            Map<String, Object> lastLog = logMessages.get(logMessages.size() - 1);
            assertRetryLog(lastLog, 3, 3, false, parentContext);
        }
    }

    @Test
    public void tracingWithRedirects() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpInstrumentationOptions options = new HttpInstrumentationOptions()
            .setHttpLogLevel(HttpInstrumentationOptions.HttpLogLevel.BODY_AND_HEADERS);

        AtomicReference<InstrumentationContext> firstRedirectContext = new AtomicReference<>();

        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRedirectPolicy())
            .addPolicy(new HttpInstrumentationPolicy(options))
            .httpClient(request -> {
                if (count.getAndIncrement() == 0) {
                    firstRedirectContext.set(request.getContext().getInstrumentationContext());
                    HttpHeaders httpHeaders = new HttpHeaders().set(HttpHeaderName.LOCATION,
                        "http://redirecthost/" + count.get() + "?param=value&api-version=42");
                    return new Response<>(request, 302, httpHeaders, BinaryData.empty());
                } else {
                    return new Response<>(request, 200, new HttpHeaders(), BinaryData.empty());
                }
            })
            .build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.GET, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();
        assertEquals(2, count.get());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        assertEquals(5, logMessages.size());
        assertResponseLog(logMessages.get(1), REDACTED_URI, 0, 302, firstRedirectContext.get());

        assertRedirectLog(logMessages.get(2), 0, 3, true, "http://redirecthost/1?param=REDACTED&api-version=REDACTED",
            HttpMethod.GET, null, parentContext);
        assertResponseLog(logMessages.get(4), "http://redirecthost/1?param=REDACTED&api-version=42", response, 0);
    }

    @Test
    public void redirectLoggingMethodNotSupported() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRedirectPolicy()).httpClient(request -> {
            count.getAndIncrement();
            HttpHeaders httpHeaders = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
            return new Response<>(request, 302, httpHeaders, BinaryData.empty());
        }).build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.PUT, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();
        assertEquals(1, count.get());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        assertEquals(1, logMessages.size());
        assertRedirectLog(logMessages.get(0), 0, 3, false, "http://redirecthost/", HttpMethod.PUT,
            "Request redirection is not enabled for this HTTP method.", parentContext);
    }

    @Test
    public void redirectToTheSameUri() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRedirectPolicy()).httpClient(request -> {
            count.getAndIncrement();
            HttpHeaders httpHeaders = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/");
            return new Response<>(request, 302, httpHeaders, BinaryData.empty());
        }).build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.GET, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();
        assertEquals(2, count.get());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        assertEquals(2, logMessages.size());
        assertRedirectLog(logMessages.get(0), 0, 3, true, "http://redirecthost/", HttpMethod.GET, null, parentContext);
        assertRedirectLog(logMessages.get(1), 1, 3, false, "http://redirecthost/", HttpMethod.GET,
            "Request was redirected more than once to the same URI.", parentContext);
    }

    @Test
    public void redirectAttemptsExhausted() throws IOException {
        AtomicInteger count = new AtomicInteger(0);
        ClientLogger logger = setupLogLevelAndGetLogger(LogLevel.VERBOSE, logCaptureStream);
        HttpPipeline pipeline = new HttpPipelineBuilder().addPolicy(new HttpRedirectPolicy()).httpClient(request -> {
            count.getAndIncrement();
            HttpHeaders httpHeaders
                = new HttpHeaders().set(HttpHeaderName.LOCATION, "http://redirecthost/" + count.get());
            return new Response<>(request, 302, httpHeaders, BinaryData.empty());
        }).build();

        InstrumentationContext parentContext = createRandomInstrumentationContext();
        HttpRequest request = createRequest(HttpMethod.GET, URI, logger, parentContext);
        Response<?> response = pipeline.send(request);
        response.close();
        assertEquals(3, count.get());

        List<Map<String, Object>> logMessages = parseLogMessages(logCaptureStream);

        assertEquals(3, logMessages.size());
        assertRedirectLog(logMessages.get(0), 0, 3, true, "http://redirecthost/1", HttpMethod.GET, null, parentContext);
        assertRedirectLog(logMessages.get(1), 1, 3, true, "http://redirecthost/2", HttpMethod.GET, null, parentContext);
        assertRedirectLog(logMessages.get(2), 2, 3, false, "http://redirecthost/3", HttpMethod.GET,
            "Redirect attempts have been exhausted.", parentContext);
    }

    public static Stream<Arguments> logLevels() {
        return Stream.of(Arguments.of(LogLevel.ERROR, false, false), Arguments.of(LogLevel.WARNING, false, true),
            Arguments.of(LogLevel.INFORMATIONAL, false, true), Arguments.of(LogLevel.VERBOSE, true, true));
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
        return Stream.of(Arguments.of(LogLevel.INFORMATIONAL, true), Arguments.of(LogLevel.WARNING, true),
            Arguments.of(LogLevel.ERROR, false));
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

    private void assertRequestLog(Map<String, Object> log, HttpRequest request) {
        assertRequestLog(log, REDACTED_URI, request, null, 0);
    }

    private void assertRequestLog(Map<String, Object> log, String expectedUri, HttpRequest request,
        InstrumentationContext context, int tryCount) {
        assertEquals("http.request", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(tryCount, (int) log.get("http.request.resend_count"));

        assertEquals(getLength(request.getBody(), request.getHeaders()), (int) log.get("http.request.body.size"));
        assertEquals(request.getHttpMethod().toString(), log.get("http.request.method"));
        assertNull(log.get("message"));

        if (context == null) {
            context = request.getContext().getInstrumentationContext();
        }

        assertTraceContext(log, context);
    }

    private void assertRetryLog(Map<String, Object> log, int tryCount, int maxAttempts, boolean isRetrying,
        InstrumentationContext context) {
        assertEquals("http.retry", log.get("event.name"));
        assertEquals(tryCount, (int) log.get("http.request.resend_count"));
        if (isRetrying) {
            assertInstanceOf(Integer.class, log.get("retry.delay"));
            assertFalse((boolean) log.get("retry.was_last_attempt"));
        } else {
            assertNull(log.get("retry.delay"));
            assertTrue((boolean) log.get("retry.was_last_attempt"));
        }
        assertEquals(maxAttempts, log.get("retry.max_attempt_count"));
        assertNull(log.get("message"));
        assertTraceContext(log, context);
    }

    private void assertRedirectLog(Map<String, Object> log, int tryCount, int maxAttempts, boolean shouldRedirect,
        String redirectUri, HttpMethod method, String message, InstrumentationContext context) {
        assertEquals("http.redirect", log.get("event.name"));
        assertEquals(tryCount, (int) log.get("http.request.resend_count"));
        assertEquals(method.toString(), log.get("http.request.method"));
        assertEquals(redirectUri, log.get("http.response.header.location"));
        if (shouldRedirect) {
            assertFalse((boolean) log.get("retry.was_last_attempt"));
        } else {
            assertTrue((boolean) log.get("retry.was_last_attempt"));
        }
        assertEquals(maxAttempts, log.get("retry.max_attempt_count"));
        assertEquals(message, log.get("message"));
        assertTraceContext(log, context);
    }

    private void assertTraceContext(Map<String, Object> log, InstrumentationContext context) {
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

    private void assertResponseLog(Map<String, Object> log, Response<?> response) {
        assertResponseLog(log, REDACTED_URI, response, 0);
    }

    private void assertResponseLog(Map<String, Object> log, String expectedUri, Response<?> response, int tryCount) {
        assertResponseLog(log, expectedUri, tryCount, response.getStatusCode(),
            response.getRequest().getContext().getInstrumentationContext());

        Long expectedRequestLength = getLength(response.getRequest().getBody(), response.getRequest().getHeaders());

        assertEquals(expectedRequestLength, (int) log.get("http.request.body.size"));
        assertEquals(response.getRequest().getHttpMethod().toString(), log.get("http.request.method"));

        assertInstanceOf(Double.class, log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
    }

    private void assertResponseLog(Map<String, Object> log, String expectedUri, int tryCount, int statusCode,
        InstrumentationContext context) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(tryCount, (int) log.get("http.request.resend_count"));

        assertEquals(statusCode, log.get("http.response.status_code"));

        assertInstanceOf(Double.class, log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
        assertNull(log.get("message"));
        assertTraceContext(log, context);
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
        assertNull(log.get("message"));
        assertTraceContext(log, response.getRequest().getContext().getInstrumentationContext());
    }

    private void assertExceptionLog(Map<String, Object> log, HttpRequest request, Throwable error) {
        assertExceptionLog(log, REDACTED_URI, request, error, null, 0);
    }

    private void assertExceptionLog(Map<String, Object> log, String expectedUri, HttpRequest request, Throwable error,
        InstrumentationContext context, int tryCount) {
        assertEquals("http.response", log.get("event.name"));
        assertEquals(expectedUri, log.get("url.full"));
        assertEquals(tryCount, (int) log.get("http.request.resend_count"));

        Long expectedRequestLength = getLength(request.getBody(), request.getHeaders());
        assertEquals(expectedRequestLength, (int) log.get("http.request.body.size"));
        assertEquals(request.getHttpMethod().toString(), log.get("http.request.method"));

        assertNull(log.get("http.response.status_code"));
        assertNull(log.get("http.response.body.size"));
        assertNull(log.get("http.request.time_to_response"));
        assertInstanceOf(Double.class, log.get("http.request.duration"));
        assertEquals(error.getMessage(), log.get("exception.message"));
        assertEquals(error.getClass().getCanonicalName(), log.get("exception.type"));
        assertNull(log.get("message"));

        if (context == null) {
            context = request.getContext().getInstrumentationContext();
        }
        assertTraceContext(log, context);
    }

    private HttpPipeline createPipeline(HttpInstrumentationOptions instrumentationOptions) {
        return createPipeline(instrumentationOptions, request -> {
            if (request.getBody() != null) {
                request.getBody().toString();
            }
            BinaryData responseBody = BinaryData.fromString("Hello, world!");
            Response<BinaryData> response = new Response<>(request, 200, new HttpHeaders(), responseBody);
            response.getHeaders()
                .set(HttpHeaderName.CONTENT_TYPE, "application/text")
                .set(HttpHeaderName.CONTENT_LENGTH, responseBody.getLength().toString())
                .set(HttpHeaderName.fromString("not-safe-to-log"), "12345");
            return response;
        });
    }

    private HttpPipeline createPipeline(HttpInstrumentationOptions instrumentationOptions,
        Function<HttpRequest, Response<BinaryData>> httpClient) {
        return new HttpPipelineBuilder().addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
            .httpClient(httpClient::apply)
            .build();
    }

    private HttpRequest createRequest(HttpMethod method, String url, ClientLogger logger) {
        return createRequest(method, url, logger, null);
    }

    private HttpRequest createRequest(HttpMethod method, String url, ClientLogger logger,
        InstrumentationContext context) {
        HttpRequest request = new HttpRequest().setMethod(method).setUri(url);
        request.getHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json");
        request.getHeaders().set(HttpHeaderName.AUTHORIZATION, "Bearer {token}");
        request.setContext(RequestContext.builder().setLogger(logger).setInstrumentationContext(context).build());

        return request;
    }

    private String traceparent(InstrumentationContext instrumentationContext) {
        return String.format("00-%s-%s-%s", instrumentationContext.getTraceId(), instrumentationContext.getSpanId(),
            instrumentationContext.getTraceFlags());
    }

}
