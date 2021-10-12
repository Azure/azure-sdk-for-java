// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Contract that all tracers must implement to be pluggable into the SDK.
 *
 * @see TracerProxy
 */
public interface Tracer {
    /**
     * Key for {@link Context} which indicates that the context contains parent span data. This span will be used
     * as the parent span for all spans the SDK creates.
     * <p>
     * If no span data is listed when the span is created it will default to using this span key as the parent span.
     */
    String PARENT_SPAN_KEY = "parent-span";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the user spans that are
     * created.
     * <p>
     * If no span name is listed when the span is created it will default to using the calling method's name.
     */
    String USER_SPAN_NAME_KEY = "user-span-name";

    /**
     * Key for {@link Context} which indicates that the context contains an entity path.
     */
    String ENTITY_PATH_KEY = "entity-path";

    /**
     * Key for {@link Context} which indicates that the context contains the hostname.
     */
    String HOST_NAME_KEY = "hostname";

    /**
     * Key for {@link Context} which indicates that the context contains a message span context.
     */
    String SPAN_CONTEXT_KEY = "span-context";

    /**
     * Key for {@link Context} which indicates that the context contains a "Diagnostic Id" for the service call.
     */
    String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";

    /**
     * Key for {@link Context} the scope of code where the given Span is in the current Context.
     */
    String SCOPE_KEY = "scope";

    /**
     * Key for {@link Context} which indicates that the context contains the Azure resource provider namespace.
     */
    String AZ_TRACING_NAMESPACE_KEY = "az.namespace";

    /**
     * Key for {@link Context} which indicates the shared span builder that is in the current Context.
     */
    String SPAN_BUILDER_KEY = "builder";

    /**
     * Key for {@link Context} which indicates the time of the last enqueued message in the partition's stream.
     */
    String MESSAGE_ENQUEUED_TIME = "x-opt-enqueued-time";

    /**
     * Key for {@link Context} which disables tracing for the request associated with the current context.
     */
    String DISABLE_TRACING_KEY = "disable-tracing";

    /**
     * Creates a new tracing span.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and explicit parent span</p>
     * <!-- src_embed com.azure.core.util.tracing.start#string-context -->
     * <pre>
     * &#47;&#47; pass the current tracing span context to the calling method
     * Context traceContext = new Context&#40;PARENT_SPAN_KEY, &quot;&lt;user-current-span&gt;&quot;&#41;;
     * &#47;&#47; start a new tracing span with the given method name and explicit parent span
     * Context updatedContext = tracer.start&#40;&quot;azure.keyvault.secrets&#47;setsecret&quot;, traceContext&#41;;
     * System.out.printf&#40;&quot;Span returned in the context object: %s%n&quot;,
     *     updatedContext.getData&#40;PARENT_SPAN_KEY&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.start#string-context -->
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} is {@code null}.
     */
    Context start(String methodName, Context context);

    /**
     * Creates a new tracing span.
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and explicit parent span</p>
     * <!-- src_embed com.azure.core.util.tracing.start#options-context -->
     * <pre>
     * &#47;&#47; start a new CLIENT tracing span with the given start options and explicit parent span
     * StartSpanOptions options = new StartSpanOptions&#40;SpanKind.CLIENT&#41;
     *     .setAttribute&#40;&quot;key&quot;, &quot;value&quot;&#41;;
     * Context updatedClientSpanContext = tracer.start&#40;&quot;azure.keyvault.secrets&#47;setsecret&quot;, options, traceContext&#41;;
     * System.out.printf&#40;&quot;Span returned in the context object: %s%n&quot;,
     *     updatedClientSpanContext.getData&#40;PARENT_SPAN_KEY&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.start#options-context -->
     *
     * @param methodName Name of the method triggering the span creation.
     * @param options span creation options.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code options} or {@code context} is {@code null}.
     */
    default Context start(String methodName, StartSpanOptions options, Context context) {
        // fall back to old API if not overridden.
        return start(methodName, context);
    }

    /**
     * Creates a new tracing span for AMQP calls.
     *
     * <p>
     * The {@code context} will be checked for information about a parent span. If a parent span is found, the new span
     * will be added as a child. Otherwise, the parent span will be created and added to the {@code context} and any
     * downstream {@code start()} calls will use the created span as the parent.
     *
     * <p>
     * Sets additional request attributes on the created span when {@code processKind} is
     * {@link ProcessKind#SEND ProcessKind.SEND}.
     *
     * <p>
     * Returns the diagnostic Id and span context of the returned span when {@code processKind} is
     * {@link ProcessKind#MESSAGE ProcessKind.MESSAGE}.
     *
     * <p>
     * Creates a new tracing span with remote parent and returns that scope when the given when {@code processKind}
     * is {@link ProcessKind#PROCESS ProcessKind.PROCESS}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span with provided method name and AMQP operation SEND</p>
     * <!-- src_embed com.azure.core.util.tracing.start#string-context-processKind-SEND -->
     * <pre>
     * &#47;&#47; pass the current tracing span and request metadata to the calling method
     * Context sendContext = new Context&#40;PARENT_SPAN_KEY, &quot;&lt;user-current-span&gt;&quot;&#41;
     *     .addData&#40;ENTITY_PATH_KEY, &quot;entity-path&quot;&#41;.addData&#40;HOST_NAME_KEY, &quot;hostname&quot;&#41;;
     *
     * &#47;&#47; start a new tracing span with explicit parent, sets the request attributes on the span and sets the span
     * &#47;&#47; kind to client when process kind SEND
     * Context updatedSendContext = tracer.start&#40;&quot;azure.eventhubs.send&quot;, sendContext, ProcessKind.SEND&#41;;
     * System.out.printf&#40;&quot;Span returned in the context object: %s%n&quot;,
     *     updatedSendContext.getData&#40;PARENT_SPAN_KEY&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.start#string-context-processKind-SEND -->
     *
     * <p>Starts a tracing span with provided method name and AMQP operation MESSAGE</p>
     * <!-- src_embed com.azure.core.util.tracing.start#string-context-processKind-MESSAGE -->
     * <pre>
     * String diagnosticIdKey = &quot;Diagnostic-Id&quot;;
     * &#47;&#47; start a new tracing span with explicit parent, sets the diagnostic Id &#40;traceparent headers&#41; on the current
     * &#47;&#47; context when process kind MESSAGE
     * Context updatedReceiveContext = tracer.start&#40;&quot;azure.eventhubs.receive&quot;, traceContext,
     *     ProcessKind.MESSAGE&#41;;
     * System.out.printf&#40;&quot;Diagnostic Id: %s%n&quot;, updatedReceiveContext.getData&#40;diagnosticIdKey&#41;.get&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.start#string-context-processKind-MESSAGE -->
     *
     * <p>Starts a tracing span with provided method name and AMQP operation PROCESS</p>
     * <!-- src_embed com.azure.core.util.tracing.start#string-context-processKind-PROCESS -->
     * <pre>
     * String spanImplContext = &quot;span-context&quot;;
     * &#47;&#47; start a new tracing span with remote parent and uses the span in the current context to return a scope
     * &#47;&#47; when process kind PROCESS
     * Context processContext = new Context&#40;PARENT_SPAN_KEY, &quot;&lt;user-current-span&gt;&quot;&#41;
     *     .addData&#40;spanImplContext, &quot;&lt;user-current-span-context&gt;&quot;&#41;;
     * Context updatedProcessContext = tracer.start&#40;&quot;azure.eventhubs.process&quot;, processContext,
     *     ProcessKind.PROCESS&#41;;
     * System.out.printf&#40;&quot;Scope: %s%n&quot;, updatedProcessContext.getData&#40;&quot;scope&quot;&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.start#string-context-processKind-PROCESS -->
     *
     * @param methodName Name of the method triggering the span creation.
     * @param context Additional metadata that is passed through the call stack.
     * @param processKind AMQP operation kind.
     * @return The updated {@link Context} object containing the returned span.
     * @throws NullPointerException if {@code methodName} or {@code context} or {@code processKind} is {@code null}.
     */
    Context start(String methodName, Context context, ProcessKind processKind);

    /**
     * Completes the current tracing span.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span present in the context, with the corresponding OpenTelemetry status for the given
     * response status code</p>
     * <!-- src_embed com.azure.core.util.tracing.end#int-throwable-context -->
     * <pre>
     * &#47;&#47; context containing the current tracing span to end
     * String openTelemetrySpanKey = &quot;openTelemetry-span&quot;;
     * Context traceContext = new Context&#40;PARENT_SPAN_KEY, &quot;&lt;user-current-span&gt;&quot;&#41;;
     *
     * &#47;&#47; completes the tracing span with the passed response status code
     * tracer.end&#40;200, null, traceContext&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.end#int-throwable-context -->
     *
     * @param responseCode Response status code if the span is in an HTTP call context.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(int responseCode, Throwable error, Context context);

    /**
     * Completes the current tracing span for AMQP calls.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Completes the tracing span with the corresponding OpenTelemetry status for the given status message</p>
     * <!-- src_embed com.azure.core.util.tracing.end#string-throwable-context -->
     * <pre>
     * &#47;&#47; context containing the current tracing span to end
     * &#47;&#47; completes the tracing span with the passed status message
     * tracer.end&#40;&quot;success&quot;, null, traceContext&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.end#string-throwable-context -->
     *
     * @param statusMessage The error or success message that occurred during the call, or {@code null} if no error
     * occurred.
     * @param error {@link Throwable} that happened during the span or {@code null} if no exception occurred.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void end(String statusMessage, Throwable error, Context context);

    /**
     * Adds metadata to the current span. If no span information is found in the context, then no metadata is added.
     *
     * @param key Name of the metadata.
     * @param value Value of the metadata.
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code key} or {@code value} or {@code context} is {@code null}.
     */
    void setAttribute(String key, String value, Context context);

    /**
     * Sets the name for spans that are created.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Retrieve the span name of the returned span</p>
     * <!-- src_embed com.azure.core.util.tracing.setSpanName#string-context -->
     * <pre>
     * &#47;&#47; Sets the span name of the returned span on the context object, with key PARENT_SPAN_KEY
     * String openTelemetrySpanKey = &quot;openTelemetry-span&quot;;
     * Context context = tracer.setSpanName&#40;&quot;test-span-method&quot;, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Span name: %s%n&quot;, context.getData&#40;PARENT_SPAN_KEY&#41;.get&#40;&#41;.toString&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.setSpanName#string-context -->
     *
     * @param spanName Name to give the next span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the name of the returned span.
     * @throws NullPointerException if {@code spanName} or {@code context} is {@code null}.
     */
    Context setSpanName(String spanName, Context context);

    /**
     * Provides a way to link multiple tracing spans.
     * Used in batching operations to relate multiple requests under a single batch.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Link multiple spans using their span context information</p>
     * <!-- src_embed com.azure.core.util.tracing.addLink#context -->
     * <pre>
     * &#47;&#47; use the parent context containing the current tracing span to start a child span
     * Context parentContext = new Context&#40;PARENT_SPAN_KEY, &quot;&lt;user-current-span&gt;&quot;&#41;;
     * &#47;&#47; use the returned span context information of the current tracing span to link
     * Context spanContext = tracer.start&#40;&quot;test.method&quot;, parentContext, ProcessKind.MESSAGE&#41;;
     *
     * &#47;&#47; Adds a link between multiple span's using the span context information of the Span
     * &#47;&#47; For each event processed, add a link with the created spanContext
     * tracer.addLink&#40;spanContext&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.addLink#context -->
     *
     * @param context Additional metadata that is passed through the call stack.
     * @throws NullPointerException if {@code context} is {@code null}.
     */
    void addLink(Context context);

    /**
     * Extracts the span's context as {@link Context} from upstream.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Extracts the corresponding span context information from a valid diagnostic id</p>
     * <!-- src_embed com.azure.core.util.tracing.extractContext#string-context -->
     * <pre>
     * &#47;&#47; Extracts the span context information from the passed diagnostic Id that can be used for linking spans.
     * String spanImplContext = &quot;span-context&quot;;
     * Context spanContext = tracer.extractContext&#40;&quot;valid-diagnostic-id&quot;, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Span context of the current tracing span: %s%n&quot;, spanContext.getData&#40;spanImplContext&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.extractContext#string-context -->
     *
     * @param diagnosticId Unique identifier for the trace information of the span.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span context.
     * @throws NullPointerException if {@code diagnosticId} or {@code context} is {@code null}.
     */
    Context extractContext(String diagnosticId, Context context);

    /**
     * Returns a span builder with the provided name in {@link Context}.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Returns a builder with the provided span name.</p>
     * <!-- src_embed com.azure.core.util.tracing.getSpanBuilder#string-context -->
     * <pre>
     * &#47;&#47; Returns a span builder with the provided name
     * String methodName = &quot;message-span&quot;;
     * Context spanContext = tracer.getSharedSpanBuilder&#40;methodName, Context.NONE&#41;;
     * System.out.printf&#40;&quot;Span context of the current tracing span: %s%n&quot;, spanContext.getData&#40;SPAN_BUILDER_KEY&#41;.get&#40;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.core.util.tracing.getSpanBuilder#string-context -->
     *
     * @param spanName Name to give the span for the created builder.
     * @param context Additional metadata that is passed through the call stack.
     * @return The updated {@link Context} object containing the span builder.
     * @throws NullPointerException if {@code context} or {@code spanName} is {@code null}.
     */
    default Context getSharedSpanBuilder(String spanName, Context context) {
        // no-op
        return Context.NONE;
    }

    /**
     * Adds an event to the current span with the provided {@code timestamp} and {@code attributes}.
     * <p>This API does not provide any normalization if provided timestamps are out of range of the current
     * span timeline</p>
     * <p>Supported attribute values include String, double, boolean, long, String [], double [], long [].
     * Any other Object value type and null values will be silently ignored.</p>
     *
     * @param name the name of the event.
     * @param attributes the additional attributes to be set for the event.
     * @param timestamp The instant, in UTC, at which the event will be associated to the span.
     * @throws NullPointerException if {@code eventName} is {@code null}.
     * @deprecated Use {@link #addEvent(String, Map, OffsetDateTime, Context)}
     */
    @Deprecated
    default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp) {
    }

    /**
     * Adds an event to the span present in the {@code Context} with the provided {@code timestamp}
     * and {@code attributes}.
     * <p>This API does not provide any normalization if provided timestamps are out of range of the current
     * span timeline</p>
     * <p>Supported attribute values include String, double, boolean, long, String [], double [], long [].
     * Any other Object value type and null values will be silently ignored.</p>
     *
     * @param name the name of the event.
     * @param attributes the additional attributes to be set for the event.
     * @param timestamp The instant, in UTC, at which the event will be associated to the span.
     * @param context the call metadata containing information of the span to which the event should be associated with.
     * @throws NullPointerException if {@code eventName} is {@code null}.
     */
    default void addEvent(String name, Map<String, Object> attributes, OffsetDateTime timestamp,
                          Context context) {

    }

    /**
     * Makes span current. Implementations may put it on ThreadLocal.
     * Make sure to always use try-with-resource statement with makeSpanCurrent
     * @param context Context with span.
     *
     * <p><strong>Code samples</strong></p>
     *
     * <p>Starts a tracing span, makes it current and ends it</p>
     * <!-- src_embed com.azure.core.util.tracing.makeSpanCurrent#context -->
     * <pre>
     * &#47;&#47; Starts a span, makes it current and then stops it.
     * Context traceContext = tracer.start&#40;&quot;EventHub.process&quot;, Context.NONE&#41;;
     *
     * &#47;&#47; Make sure to always use try-with-resource statement with makeSpanCurrent
     * try &#40;AutoCloseable ignored = tracer.makeSpanCurrent&#40;traceContext&#41;&#41; &#123;
     *     System.out.println&#40;&quot;doing some work...&quot;&#41;;
     * &#125; catch &#40;Throwable throwable&#41; &#123;
     *     tracer.end&#40;&quot;Failure&quot;, throwable, traceContext&#41;;
     * &#125; finally &#123;
     *     tracer.end&#40;&quot;OK&quot;, null, traceContext&#41;;
     * &#125;
     *
     * </pre>
     * <!-- end com.azure.core.util.tracing.makeSpanCurrent#context -->
     *
     * @return Closeable that should be closed in the same thread with try-with-resource statement.
     */
    default AutoCloseable makeSpanCurrent(Context context) {
        return TracerProxy.NOOP_AUTOCLOSEABLE;
    }
}
