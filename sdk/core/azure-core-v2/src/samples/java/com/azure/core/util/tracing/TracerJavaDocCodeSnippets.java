// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import io.clientcore.core.http.HttpPipeline;
import io.clientcore.core.http.HttpPipelineBuilder;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import com.azure.core.util.ClientOptions;
import io.clientcore.core.util.Context;
import com.azure.core.util.HttpClientOptions;
import com.azure.core.util.TracingOptions;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Contains code snippets when generating javadocs through doclets for {@link Tracer}.
 */
@SuppressWarnings("deprecation")
public class TracerJavaDocCodeSnippets {
    final Tracer tracer = new NoopTracer();

    /**
     * Code snippet for {@link Tracer#start(String, Context, ProcessKind)} and {@link Tracer#start(String, Context)}
     */
    @SuppressWarnings("try")
    public void startTracingSpan() {
        // BEGIN: com.azure.core.util.tracing.start#name
        // start a new tracing span with given name and parent context implicitly propagated
        // in io.opentelemetry.context.Context.current()

        Throwable throwable = null;
        Context span = tracer.start("keyvault.setsecret", Context.NONE);
        try {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, span);
        }
        // END: com.azure.core.util.tracing.start#name

        // BEGIN: com.azure.core.util.tracing.start#options
        // start a new CLIENT tracing span with the given start options and explicit parent context
        StartSpanOptions options = new StartSpanOptions(SpanKind.CLIENT)
            .setAttribute("key", "value");
        Context spanFromOptions = tracer.start("keyvault.setsecret", options, Context.NONE);
        try {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, spanFromOptions);
        }
        // END: com.azure.core.util.tracing.start#options

        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("traceparent", "00-019a557423720cb4c261df2ef14581cd-93174ec2823511fc-01");
        // BEGIN: com.azure.core.util.tracing.start#remote-parent-extract
        Context parentContext = tracer.extractContext(name -> {
            Object value = messageProperties.get(name);
            return value instanceof String ? (String) value : null;
        });

        StartSpanOptions remoteParentOptions = new StartSpanOptions(SpanKind.CONSUMER)
            .setRemoteParent(parentContext);

        Context spanWithRemoteParent = tracer.start("EventHubs.process", remoteParentOptions, Context.NONE);

        try (AutoCloseable scope = tracer.makeSpanCurrent(spanWithRemoteParent)) {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, spanWithRemoteParent);
        }
        // END: com.azure.core.util.tracing.start#remote-parent-extract

        // BEGIN: com.azure.core.util.tracing.start#explicit-inproc-parent
        Context parentSpan = tracer.start("parent", Context.NONE);
        Context childSpan = tracer.start("child", parentSpan);
        tracer.end(200, null, childSpan);
        tracer.end("success", null, parentSpan);
        // END: com.azure.core.util.tracing.start#explicit-inproc-parent
    }

    /**
     * Code snippet for end span methods.
     */
    @SuppressWarnings("try")
    public void endSpan() {
        Context methodSpan = Context.NONE;
        Throwable throwable = null;

        // BEGIN: com.azure.core.util.tracing.end#success
        Context messageSpan = tracer.start("ServiceBus.message", new StartSpanOptions(SpanKind.PRODUCER), Context.NONE);
        tracer.end(null, null, messageSpan);
        // END: com.azure.core.util.tracing.end#success

        // BEGIN: com.azure.core.util.tracing.end#errorStatus
        Context span = tracer.start("ServiceBus.send", new StartSpanOptions(SpanKind.CLIENT), Context.NONE);
        tracer.end("amqp:not-found", null, span);
        // END: com.azure.core.util.tracing.end#errorStatus

        // BEGIN: com.azure.core.util.tracing.end#exception
        Context sendSpan = tracer.start("ServiceBus.send", new StartSpanOptions(SpanKind.CLIENT), Context.NONE);
        try (AutoCloseable scope = tracer.makeSpanCurrent(sendSpan)) {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, sendSpan);
        }
        // END: com.azure.core.util.tracing.end#exception
    }

    /**
     * Code snippet for {@link Tracer#makeSpanCurrent(Context)}
     */
    @SuppressWarnings("try")
    public void makeCurrent() {
        Throwable throwable = null;
        // BEGIN: com.azure.core.util.tracing.makeCurrent
        Context span = tracer.start("EventHubs.process", new StartSpanOptions(SpanKind.CONSUMER), Context.NONE);
        try (AutoCloseable scope = tracer.makeSpanCurrent(span)) {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, span);
        }
        // END: com.azure.core.util.tracing.makeCurrent
    }

    /**
     * Code snippet for {@link Tracer#isEnabled()} }
     */
    public void isEnabled() {
        Throwable throwable = null;

        // BEGIN: com.azure.core.util.tracing.isEnabled
        if (!tracer.isEnabled()) {
            doWork();
        } else {
            Context span = tracer.start("span", Context.NONE);
            try {
                doWork();
            } catch (Throwable ex) {
                throwable = ex;
            } finally {
                tracer.end(null, throwable, span);
            }
        }
        // END: com.azure.core.util.tracing.isEnabled
    }

    /**
     * Code snippet for {@link StartSpanOptions#addLink(TracingLink)}}
     */
    @SuppressWarnings("try")
    public void addLink() {
        Map<String, Object> messageProperties = new HashMap<>();
        messageProperties.put("traceparent", "00-019a557423720cb4c261df2ef14581cd-93174ec2823511fc-01");
        Throwable throwable = null;

        // BEGIN: com.azure.core.util.tracing.start#links
        Context messageContext = tracer.extractContext(name -> {
            Object value = messageProperties.get(name);
            return value instanceof String ? (String) value : null;
        });

        Map<String, Object> linkAttributes = Collections.singletonMap("enqueued-time", getEnqueuedTime(messageProperties));

        StartSpanOptions processSpanOptions = new StartSpanOptions(SpanKind.CONSUMER)
            .addLink(new TracingLink(messageContext, linkAttributes));

        Context processSpan = tracer.start("EventHubs.process", processSpanOptions, Context.NONE);
        try (AutoCloseable scope = tracer.makeSpanCurrent(processSpan)) {
            doWork();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(null, throwable, processSpan);
        }
        // END: com.azure.core.util.tracing.start#links
    }

    /**
     * Code snippet for setAttribute methods
     */
    public void addAttribute() {
        // BEGIN: com.azure.core.util.tracing.set-attribute#int
        Context span = tracer.start("EventHubs.process", Context.NONE);
        tracer.setAttribute("foo", 42, span);
        // END: com.azure.core.util.tracing.set-attribute#int

        // BEGIN: com.azure.core.util.tracing.set-attribute#string
        span = tracer.start("EventHubs.process", Context.NONE);
        tracer.setAttribute("bar", "baz", span);
        // END: com.azure.core.util.tracing.set-attribute#string
    }

    /**
     * Code snippet for {@link Tracer#addEvent(String, Map, OffsetDateTime, Context)}
     */
    public void addEvent() {
        // BEGIN: com.azure.core.util.tracing.addEvent
        Context span = tracer.start("Cosmos.getItem", Context.NONE);
        tracer.addEvent("trying another endpoint", Collections.singletonMap("endpoint", "westus3"), OffsetDateTime.now(), span);
        // END: com.azure.core.util.tracing.addEvent
    }

    /**
     * Code snippet for {@link TracerProvider#createTracer(String, String, String, TracingOptions)}
     */
    public void createTracer() {
        ClientOptions clientOptions = new HttpClientOptions();
        // BEGIN: com.azure.core.util.tracing.TracerProvider#create-tracer
        Tracer tracer = TracerProvider.getDefaultProvider().createTracer("azure-storage-blobs", "12.20.0",
            "Microsoft.Storage", clientOptions.getTracingOptions());
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .tracer(tracer)
            .clientOptions(clientOptions)
            .build();
        // END: com.azure.core.util.tracing.TracerProvider#create-tracer
    }

    /**
     * Code snippet for {@link Tracer#injectContext(BiConsumer, Context)}
     */
    @SuppressWarnings("try")
    public void injectContext() {
        HttpRequest request = null;
        Throwable throwable = null;
        Context methodSpan = Context.NONE;
        int httpResponseCode = 9;

        // BEGIN: com.azure.core.util.tracing.injectContext
        Context httpSpan = tracer.start("HTTP GET", new StartSpanOptions(SpanKind.CLIENT), methodSpan);
        tracer.injectContext((headerName, headerValue) -> request.setHeader(headerName, headerValue), httpSpan);

        try (AutoCloseable scope = tracer.makeSpanCurrent(httpSpan)) {
            HttpResponse response = getResponse(request);
            httpResponseCode = response.getStatusCode();
        } catch (Throwable ex) {
            throwable = ex;
        } finally {
            tracer.end(httpResponseCode, throwable, httpSpan);
        }
        // END: com.azure.core.util.tracing.injectContext
    }

    private Instant getEnqueuedTime(Map<String, Object> messageProperties) {
        Object enqueuedTime = messageProperties.get("x-enqueued-time");
        return enqueuedTime instanceof Instant ? (Instant) enqueuedTime : null;
    }

    private HttpResponse getResponse(HttpRequest request) {
        return null;
    }

    private void doWork() {
    }
}
