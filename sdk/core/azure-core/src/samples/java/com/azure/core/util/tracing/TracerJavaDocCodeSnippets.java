// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.Context;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.PARENT_SPAN_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_BUILDER_KEY;

/**
 * Contains code snippets when generating javadocs through doclets for {@link Tracer}.
 */
public class TracerJavaDocCodeSnippets {
    final Tracer tracer = new TracerImplementation();

    /**
     * Code snippet for {@link Tracer#start(String, Context, ProcessKind)} and {@link Tracer#start(String, Context)}
     */
    public void startTracingSpan() {
        // BEGIN: com.azure.core.util.tracing.start#string-context
        // pass the current tracing span context to the calling method
        Context traceContext = new Context(PARENT_SPAN_KEY, "<user-current-span>");
        // start a new tracing span with the given method name and explicit parent span
        Context updatedContext = tracer.start("azure.keyvault.secrets/setsecret", traceContext);
        System.out.printf("Span returned in the context object: %s%n",
            updatedContext.getData(PARENT_SPAN_KEY).get());
        // END: com.azure.core.util.tracing.start#string-context

        // BEGIN: com.azure.core.util.tracing.start#options-context
        // start a new CLIENT tracing span with the given start options and explicit parent span
        StartSpanOptions options = new StartSpanOptions(StartSpanOptions.Kind.CLIENT)
            .setAttribute("key", "value")
            .setMakeCurrent(true);
        Context updatedClientSpanContext = tracer.start("azure.keyvault.secrets/setsecret", options, traceContext);
        System.out.printf("Span returned in the context object: %s%n",
            updatedClientSpanContext.getData(PARENT_SPAN_KEY).get());
        // END: com.azure.core.util.tracing.start#options-context

        // BEGIN: com.azure.core.util.tracing.start#string-context-processKind-SEND
        // pass the current tracing span and request metadata to the calling method
        Context sendContext = new Context(PARENT_SPAN_KEY, "<user-current-span>")
            .addData(ENTITY_PATH_KEY, "entity-path").addData(HOST_NAME_KEY, "hostname");

        // start a new tracing span with explicit parent, sets the request attributes on the span and sets the span
        // kind to client when process kind SEND
        Context updatedSendContext = tracer.start("azure.eventhubs.send", sendContext, ProcessKind.SEND);
        System.out.printf("Span returned in the context object: %s%n",
            updatedSendContext.getData(PARENT_SPAN_KEY).get());
        // END: com.azure.core.util.tracing.start#string-context-processKind-SEND

        // BEGIN: com.azure.core.util.tracing.start#string-context-processKind-MESSAGE
        String diagnosticIdKey = "diagnostic-id";
        // start a new tracing span with explicit parent, sets the diagnostic Id (traceparent headers) on the current
        // context when process kind MESSAGE
        Context updatedReceiveContext = tracer.start("azure.eventhubs.receive", traceContext,
            ProcessKind.MESSAGE);
        System.out.printf("Diagnostic Id: %s%n", updatedReceiveContext.getData(diagnosticIdKey).get().toString());
        // END: com.azure.core.util.tracing.start#string-context-processKind-MESSAGE

        // BEGIN: com.azure.core.util.tracing.start#string-context-processKind-PROCESS
        String spanImplContext = "span-context";
        // start a new tracing span with remote parent and uses the span in the current context to return a scope
        // when process kind PROCESS
        Context processContext = new Context(PARENT_SPAN_KEY, "<user-current-span>")
            .addData(spanImplContext, "<user-current-span-context>");
        Context updatedProcessContext = tracer.start("azure.eventhubs.process", processContext,
            ProcessKind.PROCESS);
        System.out.printf("Scope: %s%n", updatedProcessContext.getData("scope").get());
        // END: com.azure.core.util.tracing.start#string-context-processKind-PROCESS
    }

    /**
     * Code snippet for {@link Tracer#end(int, Throwable, Context)} and {@link Tracer#end(String, Throwable, Context)}
     */
    public void endTracingSpan() {
        // BEGIN: com.azure.core.util.tracing.end#int-throwable-context
        // context containing the current tracing span to end
        String openTelemetrySpanKey = "openTelemetry-span";
        Context traceContext = new Context(PARENT_SPAN_KEY, "<user-current-span>");

        // completes the tracing span with the passed response status code
        tracer.end(200, null, traceContext);
        // END: com.azure.core.util.tracing.end#int-throwable-context

        // BEGIN: com.azure.core.util.tracing.end#string-throwable-context
        // context containing the current tracing span to end
        // completes the tracing span with the passed status message
        tracer.end("success", null, traceContext);
        // END: com.azure.core.util.tracing.end#string-throwable-context
    }

    /**
     * Code snippet for {@link Tracer#setSpanName(String, Context)}
     */
    public void setSpanName() {
        // BEGIN: com.azure.core.util.tracing.setSpanName#string-context
        // Sets the span name of the returned span on the context object, with key PARENT_SPAN_KEY
        String openTelemetrySpanKey = "openTelemetry-span";
        Context context = tracer.setSpanName("test-span-method", Context.NONE);
        System.out.printf("Span name: %s%n", context.getData(PARENT_SPAN_KEY).get().toString());
        // END: com.azure.core.util.tracing.setSpanName#string-context
    }

    /**
     * Code snippet for {@link Tracer#addLink(Context)}
     */
    public void addLink() {
        // BEGIN: com.azure.core.util.tracing.addLink#context
        // use the parent context containing the current tracing span to start a child span
        Context parentContext = new Context(PARENT_SPAN_KEY, "<user-current-span>");
        // use the returned span context information of the current tracing span to link
        Context spanContext = tracer.start("test.method", parentContext, ProcessKind.MESSAGE);

        // Adds a link between multiple span's using the span context information of the Span
        // For each event processed, add a link with the created spanContext
        tracer.addLink(spanContext);
        // END: com.azure.core.util.tracing.addLink#context
    }

    /**
     * Code snippet for {@link Tracer#extractContext(String, Context)}
     */
    public void extractContext() {
        // BEGIN: com.azure.core.util.tracing.extractContext#string-context
        // Extracts the span context information from the passed diagnostic Id that can be used for linking spans.
        String spanImplContext = "span-context";
        Context spanContext = tracer.extractContext("valid-diagnostic-id", Context.NONE);
        System.out.printf("Span context of the current tracing span: %s%n", spanContext.getData(spanImplContext).get());
        // END: com.azure.core.util.tracing.extractContext#string-context
    }

    /**
     * Code snippet for {@link Tracer#getSharedSpanBuilder(String, Context)}
     */
    public void getSharedSpanBuilder() {
        // BEGIN: com.azure.core.util.tracing.getSpanBuilder#string-context
        // Returns a span builder with the provided name
        String methodName = "message-span";
        Context spanContext = tracer.getSharedSpanBuilder(methodName, Context.NONE);
        System.out.printf("Span context of the current tracing span: %s%n", spanContext.getData(SPAN_BUILDER_KEY).get());
        // END: com.azure.core.util.tracing.getSpanBuilder#string-context
    }

    //Noop Tracer
    private static final class TracerImplementation implements Tracer {
        @Override
        public Context start(String methodName, Context context) {
            return null;
        }

        @Override
        public Context start(String methodName, Context context, ProcessKind processKind) {
            return null;
        }

        @Override
        public void end(int responseCode, Throwable error, Context context) {

        }

        @Override
        public void end(String errorCondition, Throwable error, Context context) {

        }

        @Override
        public void setAttribute(String key, String value, Context context) {

        }

        @Override
        public Context setSpanName(String spanName, Context context) {
            return null;
        }

        @Override
        public void addLink(Context context) {

        }

        @Override
        public Context extractContext(String diagnosticId, Context context) {
            return null;
        }

        @Override
        public Context getSharedSpanBuilder(String spanName, Context context) {
            return null;
        }
    }
}
