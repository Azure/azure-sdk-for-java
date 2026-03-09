// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.annotation.processor.test;

import io.clientcore.annotation.processor.test.implementation.EncodeDurationService;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.models.binarydata.BinaryData;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests duration encoding in HTTP requests, covering the scenarios from the
 * <a href="https://github.com/microsoft/typespec/tree/main/packages/http-specs/specs/encode/duration">
 * encode/duration http-specs</a>.
 * <p>
 * These tests use a capturing mock HTTP client to verify the encoded values sent in HTTP requests,
 * including the "larger unit" scenarios where a {@link Duration} expressed in minutes is serialized
 * as the equivalent number of seconds or milliseconds.
 */
public class EncodeDurationTests {

    private static final String ENDPOINT = "http://localhost";

    private static final Duration SECOND35_625 = Duration.ofSeconds(35, 625_000_000);
    private static final Duration SECOND36 = Duration.ofSeconds(36);
    private static final Duration MILLIS36000 = Duration.ofMillis(36000);

    // =========== Query parameter tests ===========

    /**
     * Tests int32 seconds encoding: {@code Duration.ofSeconds(36)} encodes to query param {@code input=36}.
     */
    @Test
    public void testQueryInt32Seconds() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);

        service.queryInt32Seconds(ENDPOINT, SECOND36.getSeconds(), RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/int32-seconds"),
            "Expected path /encode/duration/query/int32-seconds");
        assertTrue(capturedUri.get().contains("input=36"), "Expected query param input=36");
    }

    /**
     * Tests int32 seconds encoding with a larger-unit Duration: {@code Duration.ofMinutes(2)} encodes to
     * query param {@code input=120}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes but must be serialized
     * as the equivalent number of seconds (120).
     */
    @Test
    public void testQueryInt32SecondsLargerUnit() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);
        Duration twoMinutes = Duration.ofMinutes(2);

        service.queryInt32SecondsLargerUnit(ENDPOINT, twoMinutes.getSeconds(), RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/int32-seconds-larger-unit"),
            "Expected path /encode/duration/query/int32-seconds-larger-unit");
        assertTrue(capturedUri.get().contains("input=120"),
            "Expected query param input=120 for Duration.ofMinutes(2) in seconds");
    }

    /**
     * Tests float seconds encoding: {@code Duration.ofSeconds(35, 625_000_000)} encodes to
     * query param {@code input=35.625}.
     */
    @Test
    public void testQueryFloatSeconds() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);
        double seconds = SECOND35_625.getSeconds() + SECOND35_625.getNano() / 1e9;

        service.queryFloatSeconds(ENDPOINT, seconds, RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/float-seconds"),
            "Expected path /encode/duration/query/float-seconds");
        assertTrue(capturedUri.get().contains("input=35.625"), "Expected query param input=35.625");
    }

    /**
     * Tests float seconds encoding with a larger-unit Duration: {@code Duration.ofMinutes(2).plusSeconds(30)} encodes
     * to query param {@code input=150.0}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes and seconds but must be
     * serialized as the equivalent number of float seconds (150.0).
     */
    @Test
    public void testQueryFloatSecondsLargerUnit() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);
        Duration twoMinutesThirtySeconds = Duration.ofMinutes(2).plusSeconds(30);
        double seconds = (double) twoMinutesThirtySeconds.getSeconds();

        service.queryFloatSecondsLargerUnit(ENDPOINT, seconds, RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/float-seconds-larger-unit"),
            "Expected path /encode/duration/query/float-seconds-larger-unit");
        assertTrue(capturedUri.get().contains("input=150.0"),
            "Expected query param input=150.0 for Duration.ofMinutes(2).plusSeconds(30) in seconds");
    }

    /**
     * Tests int32 milliseconds encoding: {@code Duration.ofMillis(36000)} encodes to query param {@code input=36000}.
     */
    @Test
    public void testQueryInt32Milliseconds() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);

        service.queryInt32Milliseconds(ENDPOINT, MILLIS36000.toMillis(), RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/int32-milliseconds"),
            "Expected path /encode/duration/query/int32-milliseconds");
        assertTrue(capturedUri.get().contains("input=36000"), "Expected query param input=36000");
    }

    /**
     * Tests int32 milliseconds encoding with a larger-unit Duration: {@code Duration.ofMinutes(3)} encodes to
     * query param {@code input=180000}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes but must be serialized
     * as the equivalent number of milliseconds (180000).
     */
    @Test
    public void testQueryInt32MillisecondsLargerUnit() {
        AtomicReference<String> capturedUri = new AtomicReference<>();
        EncodeDurationService service = getService(capturedUri);
        Duration threeMinutes = Duration.ofMinutes(3);

        service.queryInt32MillisecondsLargerUnit(ENDPOINT, threeMinutes.toMillis(), RequestContext.none());

        assertNotNull(capturedUri.get());
        assertTrue(capturedUri.get().contains("/encode/duration/query/int32-milliseconds-larger-unit"),
            "Expected path /encode/duration/query/int32-milliseconds-larger-unit");
        assertTrue(capturedUri.get().contains("input=180000"),
            "Expected query param input=180000 for Duration.ofMinutes(3) in milliseconds");
    }

    // =========== Header parameter tests ===========

    /**
     * Tests int32 seconds header encoding: {@code Duration.ofSeconds(36)} encodes to header {@code duration: 36}.
     */
    @Test
    public void testHeaderInt32Seconds() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);

        service.headerInt32Seconds(ENDPOINT, SECOND36.getSeconds(), RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("36", capturedDurationHeader.get(), "Expected duration header value 36");
    }

    /**
     * Tests int32 seconds header encoding with a larger-unit Duration: {@code Duration.ofMinutes(2)} encodes to
     * header {@code duration: 120}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes but must be serialized
     * as the equivalent number of seconds (120).
     */
    @Test
    public void testHeaderInt32SecondsLargerUnit() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);
        Duration twoMinutes = Duration.ofMinutes(2);

        service.headerInt32SecondsLargerUnit(ENDPOINT, twoMinutes.getSeconds(), RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("120", capturedDurationHeader.get(),
            "Expected duration header value 120 for Duration.ofMinutes(2) in seconds");
    }

    /**
     * Tests float seconds header encoding: {@code Duration.ofSeconds(35, 625_000_000)} encodes to
     * header {@code duration: 35.625}.
     */
    @Test
    public void testHeaderFloatSeconds() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);
        double seconds = SECOND35_625.getSeconds() + SECOND35_625.getNano() / 1e9;

        service.headerFloatSeconds(ENDPOINT, seconds, RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("35.625", capturedDurationHeader.get(), "Expected duration header value 35.625");
    }

    /**
     * Tests float seconds header encoding with a larger-unit Duration: {@code Duration.ofMinutes(2).plusSeconds(30)}
     * encodes to header {@code duration: 150.0}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes and seconds but must be
     * serialized as the equivalent number of float seconds (150.0).
     */
    @Test
    public void testHeaderFloatSecondsLargerUnit() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);
        Duration twoMinutesThirtySeconds = Duration.ofMinutes(2).plusSeconds(30);
        double seconds = (double) twoMinutesThirtySeconds.getSeconds();

        service.headerFloatSecondsLargerUnit(ENDPOINT, seconds, RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("150.0", capturedDurationHeader.get(),
            "Expected duration header value 150.0 for Duration.ofMinutes(2).plusSeconds(30) in seconds");
    }

    /**
     * Tests int32 milliseconds header encoding: {@code Duration.ofMillis(36000)} encodes to
     * header {@code duration: 36000}.
     */
    @Test
    public void testHeaderInt32Milliseconds() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);

        service.headerInt32Milliseconds(ENDPOINT, MILLIS36000.toMillis(), RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("36000", capturedDurationHeader.get(), "Expected duration header value 36000");
    }

    /**
     * Tests int32 milliseconds header encoding with a larger-unit Duration: {@code Duration.ofMinutes(3)} encodes to
     * header {@code duration: 180000}.
     * <p>
     * This is the "larger unit" scenario where the Duration is expressed in minutes but must be serialized
     * as the equivalent number of milliseconds (180000).
     */
    @Test
    public void testHeaderInt32MillisecondsLargerUnit() {
        AtomicReference<String> capturedDurationHeader = new AtomicReference<>();
        EncodeDurationService service = getServiceWithHeaderCapture(capturedDurationHeader);
        Duration threeMinutes = Duration.ofMinutes(3);

        service.headerInt32MillisecondsLargerUnit(ENDPOINT, threeMinutes.toMillis(), RequestContext.none());

        assertNotNull(capturedDurationHeader.get());
        assertEquals("180000", capturedDurationHeader.get(),
            "Expected duration header value 180000 for Duration.ofMinutes(3) in milliseconds");
    }

    // =========== Helpers ===========

    /**
     * Creates an {@link EncodeDurationService} backed by a mock HTTP client that captures the request URI.
     *
     * @param capturedUri An {@link AtomicReference} to hold the captured URI string.
     * @return The service instance.
     */
    private static EncodeDurationService getService(AtomicReference<String> capturedUri) {
        return EncodeDurationService.getNewInstance(new HttpPipelineBuilder().httpClient(request -> {
            capturedUri.set(request.getUri().toString());
            return new Response<>(request, 204, new HttpHeaders(), BinaryData.empty());
        }).build());
    }

    /**
     * Creates an {@link EncodeDurationService} backed by a mock HTTP client that captures the {@code duration}
     * header value.
     *
     * @param capturedDurationHeader An {@link AtomicReference} to hold the captured duration header value.
     * @return The service instance.
     */
    private static EncodeDurationService getServiceWithHeaderCapture(AtomicReference<String> capturedDurationHeader) {
        return EncodeDurationService.getNewInstance(new HttpPipelineBuilder().httpClient(request -> {
            String durationValue = request.getHeaders().getValue(HttpHeaderName.fromString("duration"));
            capturedDurationHeader.set(durationValue);
            return new Response<>(request, 204, new HttpHeaders(), BinaryData.empty());
        }).build());
    }
}
