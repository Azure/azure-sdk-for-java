// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.http.rest;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.SdkRequestContext;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.utils.ProgressReporter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static io.clientcore.core.instrumentation.tracing.SpanKind.CLIENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SdkRequestContextTests {
    private static final Tracer TRACER
        = Instrumentation.create(null, new LibraryInstrumentationOptions("test-library")).getTracer();

    @Test
    public void basicSdkRequestContext() {
        ClientLogger logger = new ClientLogger(SdkRequestContextTests.class);

        RequestOptions options = new RequestOptions().setLogger(logger)
            .addRequestCallback(r -> r.getHeaders().add(HttpHeaderName.fromString("x-ms-pet-version"), "2021-06-01"))
            .setInstrumentationContext(startSpan().getInstrumentationContext())
            .addQueryParam("param1", "value1")
            .addQueryParam("param2", "value with space", true)
            .putData("key", "value");

        SdkRequestContext requestContext = SdkRequestContext.create(options);

        assertNotSame(options, requestContext);
        assertSame(options.getInstrumentationContext(), requestContext.getInstrumentationContext());
        assertSame(options.getLogger(), requestContext.getLogger());
        assertSame(options.getRequestCallback(), requestContext.getRequestCallback());
        assertEquals("value", requestContext.getData("key"));
        assertNull(requestContext.getData("missing key"));

        assertNull(requestContext.getProgressReporter());
    }

    public static Stream<RequestOptions> emptyRequestOptionsProvider() {
        return Stream.of(RequestOptions.none(), new RequestOptions(), null);
    }

    @ParameterizedTest
    @MethodSource("emptyRequestOptionsProvider")
    public void emptyRequestOptions(RequestOptions options) {
        SdkRequestContext requestContext = SdkRequestContext.create(options);

        assertNull(requestContext.getData("key"));
        assertNotNull(requestContext.getRequestCallback());
        assertNull(requestContext.getLogger());
        assertNull(requestContext.getInstrumentationContext());
        assertNull(requestContext.getProgressReporter());
    }

    @Test
    public void requestContextIsLocked() {
        RequestOptions options = new RequestOptions();
        SdkRequestContext requestContext = SdkRequestContext.create(options);
        assertThrows(IllegalStateException.class, () -> requestContext.putData("key", "value"));
        assertThrows(IllegalStateException.class, () -> requestContext.addRequestCallback(request -> {
        }));
        assertThrows(IllegalStateException.class, () -> requestContext.addQueryParam("key", "value"));
        assertThrows(IllegalStateException.class, () -> requestContext.addQueryParam("key", "value", true));
        assertThrows(IllegalStateException.class, () -> requestContext.setLogger(new ClientLogger("test")));
        assertThrows(IllegalStateException.class, () -> requestContext.setInstrumentationContext(null));
    }

    @Test
    public void invalidParams() {
        SdkRequestContext requestContext = SdkRequestContext.none();
        assertThrows(NullPointerException.class, () -> requestContext.getData(null));
    }

    @Test
    public void requestOptionsAreCopied() {
        RequestOptions options = new RequestOptions();

        ProgressReporter progressReporter = ProgressReporter.withProgressListener(value -> {
        });
        options.putData("progressReporter", progressReporter);

        SdkRequestContext requestContext = SdkRequestContext.create(options);
        assertSame(progressReporter, requestContext.getProgressReporter());

        options.putData("progressReporter", null);
        assertNull(options.getData("progressReporter"));

        assertSame(progressReporter, requestContext.getProgressReporter());
    }

    @Test
    public void updateInstrumentationContext() {
        SdkRequestContext outerRequestContext
            = SdkRequestContext.create(RequestOptions.none(), startSpan().getInstrumentationContext());

        Span innerSpan = startSpan();
        SdkRequestContext innerRequestContext
            = SdkRequestContext.create(outerRequestContext, innerSpan.getInstrumentationContext());

        assertSame(innerSpan.getInstrumentationContext(), innerRequestContext.getInstrumentationContext());

        SdkRequestContext noInstrumentationContext = SdkRequestContext.create(innerRequestContext);
        assertSame(innerRequestContext.getInstrumentationContext(),
            noInstrumentationContext.getInstrumentationContext());
    }

    @Test
    public void updateProgressReporter() {
        ProgressReporter progressReporter = ProgressReporter.withProgressListener(value -> {
        });
        SdkRequestContext outerRequestContext = SdkRequestContext.create(RequestOptions.none(), progressReporter);
        assertSame(progressReporter, outerRequestContext.getProgressReporter());

        ProgressReporter childReporter = progressReporter.createChild();
        SdkRequestContext innerRequestContext = SdkRequestContext.create(outerRequestContext, childReporter);

        assertSame(childReporter, innerRequestContext.getProgressReporter());

        SdkRequestContext noExplicitProgressReporter  = SdkRequestContext.create(innerRequestContext);
        assertSame(childReporter, noExplicitProgressReporter.getProgressReporter());
    }

    private static Span startSpan() {
        return TRACER.spanBuilder("span", CLIENT, null).startSpan();
    }
}
