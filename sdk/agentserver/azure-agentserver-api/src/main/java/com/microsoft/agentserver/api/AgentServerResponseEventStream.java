// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.openai.core.JsonNull;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCompletedEvent;
import com.openai.models.responses.ResponseContentPartAddedEvent;
import com.openai.models.responses.ResponseContentPartDoneEvent;
import com.openai.models.responses.ResponseCreatedEvent;
import com.openai.models.responses.ResponseCustomToolCall;
import com.openai.models.responses.ResponseError;
import com.openai.models.responses.ResponseFailedEvent;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionCallArgumentsDeltaEvent;
import com.openai.models.responses.ResponseFunctionCallArgumentsDoneEvent;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseInProgressEvent;
import com.openai.models.responses.ResponseIncompleteEvent;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputItemAddedEvent;
import com.openai.models.responses.ResponseOutputItemDoneEvent;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseQueuedEvent;
import com.openai.models.responses.ResponseReasoningItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseTextDeltaEvent;
import com.openai.models.responses.ResponseTextDoneEvent;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.responses.ToolChoiceOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * Fluent builder for constructing a streaming response event sequence.
 * <p>
 * Manages a global sequence counter, output index counter, and an accumulated
 * {@link Response} snapshot. Every {@code emit*()} method appends a {@link ResponseEvent}
 * to the internal list and returns {@code this} for chaining.
 * <p>
 * Nested output-item scopes use the {@link Consumer} callback pattern so that
 * builder lifecycles are lexically scoped and automatically finalized.
 * <p>
 * Call {@link #subscribe(Consumer, Consumer, Runnable)} to receive events
 * in real-time as they are produced — including text deltas from deeply nested builders.
 * Events are delivered via a {@link SubmissionPublisher} from the JDK's
 * {@link Flow} reactive-streams API.
 *
 * <h3>Threading model</h3>
 * <p>
 * This class is designed for a <strong>single-writer</strong> pattern: all {@code emit*()}
 * and {@code addOutput*()} methods must be called from a single thread (or with external
 * serialization). The {@link #subscribe(Consumer, Consumer, Runnable)} callbacks are
 * invoked by the {@link SubmissionPublisher}'s executor. Internal synchronization ensures
 * safe hand-off between the writer and subscriber.
 *
 * <h3>Example usage (synchronous)</h3>
 * <pre>{@code
 * ResponseEventStream stream = ResponseEventStream.create(request, context)
 *     .emitCreated()
 *     .emitInProgress()
 *     .addOutputMessage(msg -> msg
 *         .emitAdded()
 *         .addTextPart(text -> text
 *             .emitAdded()
 *             .emitDelta("Hello!")
 *             .emitDone("Hello!"))
 *         .emitDone())
 *     .emitCompleted();
 *
 * List<ResponseEvent> events = stream.getEvents();
 * }</pre>
 *
 * <h3>Example usage (reactive streaming)</h3>
 * <pre>{@code
 * ResponseEventStream stream = ResponseEventStream.create(request, context);
 * stream.subscribe(
 *     event -> System.out.println(event.eventName()),
 *     failure -> failure.printStackTrace(),
 *     () -> System.out.println("Done"));
 *
 * // Build on another thread — subscribers see events immediately.
 * // awaitSubscription() blocks until subscribe() has been called,
 * // preventing events from being emitted before a subscriber is ready.
 * executor.submit(() -> {
 *     stream.awaitSubscription();
 *     stream.emitCreated()
 *         .emitInProgress()
 *         .addOutputMessage(msg -> msg
 *             .emitAdded()
 *             .addTextPart(text -> text
 *                 .emitAdded()
 *                 .emitDelta("Hello!")
 *                 .emitDone("Hello!"))
 *             .emitDone())
 *         .emitCompleted();
 * });
 * }</pre>
 */
final class AgentServerResponseEventStream implements ResponseEventStream {

    private final IdGenerator idGenerator;
    private final String responseId;
    // Accumulated events and output items.
    private final CopyOnWriteArrayList<ResponseEvent> events = new CopyOnWriteArrayList<>();
    // Concurrent map because trackCompletedOutputItem() may be called from nested
    // builders or forwardUpstream() while a subscriber is consuming events on a
    // different thread (e.g., via the SubmissionPublisher executor).
    private final Map<Long, ResponseOutputItem> outputItems = new java.util.concurrent.ConcurrentSkipListMap<>();
    // JDK reactive publisher for push-based event delivery.
    // Synchronous executor (Runnable::run) ensures events are delivered inline
    // on the writer thread, matching the original Multi emitter semantics.
    private final SubmissionPublisher<ResponseEvent> publisher =
        new SubmissionPublisher<>(Runnable::run, Flow.defaultBufferSize());
    private final CountDownLatch subscriptionLatch = new CountDownLatch(1);
    private final AtomicLong sequenceNumber = new AtomicLong();
    private final AtomicLong outputIndex = new AtomicLong();
    private final AtomicBoolean terminated = new AtomicBoolean();
    // Mutable response builder – rebuilt into an immutable snapshot per event.
    // The OpenAI SDK builder is mutable (setters return `this`), but we reassign
    // defensively so the code also works if the builder were ever replaced with
    // an immutable variant.
    // THREADING: all reads/writes of responseBuilder MUST hold builderLock.
    private Response.Builder responseBuilder;
    private final Object builderLock = new Object();

    // ── Construction ──────────────────────────────────────────

    private AgentServerResponseEventStream(AgentServerCreateResponse request, ResponseContext context) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(request, "request must not be null");

        this.idGenerator = new IdGenerator(IdGenerator.extractPartitionKey(context.getResponseId()));
        this.responseId = context.getResponseId();

        String conversationId = request.responseCreateParams().conversation()
            .flatMap(conv -> {
                if (conv.isId()) {
                    return Optional.of(conv.asId());
                }
                if (conv.isResponseConversationParam()) {
                    return Optional.of(conv.asResponseConversationParam().id());
                }
                return Optional.empty();
            })
            .orElse(null);

        this.responseBuilder = Response.builder()
            .id(context.getResponseId())
            .createdAt(System.currentTimeMillis() / 1000.0)
            .status(ResponseStatus.QUEUED)
            .error(Optional.empty())
            .incompleteDetails(Optional.empty())
            .instructions(Optional.empty())
            .metadata(Optional.empty())
            .parallelToolCalls(false)
            .temperature(Optional.empty())
            .toolChoice(ToolChoiceOptions.AUTO)
            .tools(List.of())
            .topP(Optional.empty())
            .output(List.of())
            .previousResponseId(request.responseCreateParams().previousResponseId().orElse(null))
            .background(request.responseCreateParams().background().orElse(null))
            .putAdditionalProperty("agent_id", new JsonNull());

        if (request.responseCreateParams().model().isPresent()) {
            request.responseCreateParams().model().ifPresent(this.responseBuilder::model);
        } else {
            String modelName = FoundryEnvironment.AGENT_NAME != null
                ? FoundryEnvironment.AGENT_NAME : "unknown-agent";
            this.responseBuilder.model(modelName);
        }

        if (conversationId != null) {
            this.responseBuilder = this.responseBuilder.conversation(
                Response.Conversation.builder().id(conversationId).build());
        }
    }

    /**
     * Creates a new event stream from a request and context.
     */
    public static ResponseEventStream create(ResponseContext context, AgentServerCreateResponse request) {
        return new AgentServerResponseEventStream(request, context);
    }

    // ── Results ───────────────────────────────────────────────

    /**
     * Resolves the SSE event name for a given {@link ResponseStreamEvent}.
     * Returns {@code null} for unrecognized or lifecycle events.
     */
    private static String resolveEventName(ResponseStreamEvent event) {
        if (event.isOutputItemAdded()) return "response.output_item.added";
        if (event.isOutputItemDone()) return "response.output_item.done";
        if (event.isOutputTextDelta()) return "response.output_text.delta";
        if (event.isOutputTextDone()) return "response.output_text.done";
        if (event.isContentPartAdded()) return "response.content_part.added";
        if (event.isContentPartDone()) return "response.content_part.done";
        if (event.isFunctionCallArgumentsDelta()) return "response.function_call_arguments.delta";
        if (event.isFunctionCallArgumentsDone()) return "response.function_call_arguments.done";
        // Add more as needed for reasoning, file search, etc.
        return null;
    }

    /**
     * Creates a default zero-usage object with all required fields populated.
     * The OpenAI SDK requires {@code inputTokensDetails} and {@code outputTokensDetails}
     * even when usage is not tracked.
     */
    private static ResponseUsage defaultUsage() {
        return ResponseUsage.builder()
            .inputTokens(0)
            .outputTokens(0)
            .totalTokens(0)
            .inputTokensDetails(ResponseUsage.InputTokensDetails.builder()
                .cachedTokens(0)
                .build())
            .outputTokensDetails(ResponseUsage.OutputTokensDetails.builder()
                .reasoningTokens(0)
                .build())
            .build();
    }

    // ── Reactive Stream ───────────────────────────────────────

    /**
     * Returns an unmodifiable view of all events emitted so far.
     */
    @Override
    public List<ResponseEvent> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns a snapshot of the current {@link Response} being constructed.
     */
    @Override
    public Response getResponse() {
        synchronized (builderLock) {
            return responseBuilder.build();
        }
    }

    /**
     * Stamps the resolved {@code agent_session_id} onto every snapshot
     * produced by this stream from this point forward. No-op for null/empty input.
     * Package-private — invoked by {@link AgentServerResponsesApi} once the
     * session ID is resolved for the current request.
     */
    void setAgentSessionId(String sessionId) {
        if (sessionId == null || sessionId.isEmpty()) {
            return;
        }
        synchronized (builderLock) {
            responseBuilder = responseBuilder.putAdditionalProperty(
                "agent_session_id", com.openai.core.JsonValue.from(sessionId));
        }
    }

    // ── Response Lifecycle Events ─────────────────────────────

    /**
     * Subscribes to this event stream with the given callbacks.
     * <p>
     * Every event produced by any builder — including deeply nested text deltas —
     * is pushed to the {@code onEvent} callback immediately. If events have already
     * been emitted before subscription, they are replayed first.
     * <p>
     * Terminal events ({@code emitCompleted}, {@code emitFailed}, {@code emitIncomplete})
     * automatically invoke {@code onComplete}.
     * <p>
     * This method is synchronized on the same monitor as {@link #addEvent} to
     * guarantee that no events are lost between the replay loop and the
     * {@link SubmissionPublisher} subscription.
     *
     * @param onEvent    called for each {@link ResponseEvent} as it is produced
     * @param onFailure  called if the stream encounters an error
     * @param onComplete called when the stream terminates normally
     */
    @Override
    public synchronized void subscribe(Consumer<ResponseEvent> onEvent, Consumer<Throwable> onFailure, Runnable onComplete) {
        // Replay events already emitted before subscription.
        for (ResponseEvent event : events) {
            onEvent.accept(event);
        }

        // If the stream already terminated, signal completion immediately.
        if (terminated.get()) {
            subscriptionLatch.countDown();
            onComplete.run();
            return;
        }

        // Wire up a Flow.Subscriber that delegates to the provided callbacks.
        // Because addEvent() is also synchronized on `this`, no submit() can
        // happen between the replay above and the registration below.
        publisher.subscribe(new Flow.Subscriber<>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(ResponseEvent event) {
                onEvent.accept(event);
            }

            @Override
            public void onError(Throwable throwable) {
                onFailure.accept(throwable);
            }

            @Override
            public void onComplete() {
                onComplete.run();
            }
        });

        // Signal any threads waiting in awaitSubscription().
        subscriptionLatch.countDown();
    }

    /**
     * Blocks the calling thread until {@link #subscribe} has been called,
     * ensuring that no events are emitted before a subscriber is ready.
     * Times out after 30 seconds to avoid deadlocks.
     *
     * @throws InterruptedException if the thread is interrupted while waiting
     */
    @Override
    public void awaitSubscription() throws InterruptedException {
        if (!subscriptionLatch.await(30, TimeUnit.SECONDS)) {
            throw new IllegalStateException(
                "Timed out waiting for a subscriber to be registered. " +
                    "Ensure subscribe() is called within 30 seconds of stream creation.");
        }
    }

    /**
     * Emits a {@code response.queued} event. Sets status to {@link ResponseStatus#QUEUED}.
     */
    @Override
    public ResponseEventStream emitQueued() {
        synchronized (builderLock) {
            responseBuilder = responseBuilder.status(ResponseStatus.QUEUED);
            addEvent(new ResponseEvent("response.queued",
                ResponseStreamEvent.ofQueued(new ResponseQueuedEvent.Builder()
                    .sequenceNumber(nextSequenceNumber())
                    .response(snapshot())
                    .build())));
        }
        return this;
    }

    /**
     * Emits a {@code response.created} event with status {@link ResponseStatus#IN_PROGRESS}.
     */
    @Override
    public ResponseEventStream emitCreated() {
        return emitCreated(ResponseStatus.IN_PROGRESS);
    }

    /**
     * Emits a {@code response.created} event with the given status.
     */
    @Override
    public ResponseEventStream emitCreated(ResponseStatus status) {
        synchronized (builderLock) {
            responseBuilder = responseBuilder.status(status);
        }
        addEvent(new ResponseEvent("response.created",
            ResponseStreamEvent.ofCreated(new ResponseCreatedEvent.Builder()
                .sequenceNumber(nextSequenceNumber())
                .response(snapshot())
                .build())));
        return this;
    }

    /**
     * Emits a {@code response.in_progress} event. Sets status to {@link ResponseStatus#IN_PROGRESS}.
     */
    @Override
    public ResponseEventStream emitInProgress() {
        synchronized (builderLock) {
            responseBuilder = responseBuilder.status(ResponseStatus.IN_PROGRESS);
        }
        addEvent(new ResponseEvent("response.in_progress",
            ResponseStreamEvent.ofInProgress(new ResponseInProgressEvent.Builder()
                .sequenceNumber(nextSequenceNumber())
                .response(snapshot())
                .build())));
        return this;
    }

    /**
     * Emits a {@code response.completed} event with no usage data.
     */
    @Override
    public ResponseEventStream emitCompleted() {
        return emitCompleted(null);
    }

    /**
     * Emits a {@code response.completed} event with optional usage data.
     *
     * @throws IllegalStateException if a terminal event has already been emitted
     */
    @Override
    public ResponseEventStream emitCompleted(ResponseUsage usage) {
        guardTerminal();
        // Default to zero-usage if none provided — clients may require the field.
        ResponseUsage effectiveUsage = usage != null ? usage : defaultUsage();
        synchronized (builderLock) {
            responseBuilder = responseBuilder
                .status(ResponseStatus.COMPLETED)
                .completedAt(System.currentTimeMillis() / 1000.0)
                .output(collectOutputItems())
                .usage(effectiveUsage);
        }
        addEvent(new ResponseEvent("response.completed",
            ResponseStreamEvent.ofCompleted(new ResponseCompletedEvent.Builder()
                .sequenceNumber(nextSequenceNumber())
                .response(snapshot())
                .build())));
        completeMulti();
        return this;
    }

    /**
     * Emits a {@code response.failed} event with a server error.
     */
    @Override
    public ResponseEventStream emitFailed() {
        return emitFailed(ResponseError.Code.SERVER_ERROR, "An internal server error occurred.", null);
    }

    /**
     * Emits a {@code response.failed} event with the given error.
     */
    @Override
    public ResponseEventStream emitFailed(ResponseError.Code code, String message) {
        return emitFailed(code, message, null);
    }

    /**
     * Emits a {@code response.failed} event with the given error and optional usage data.
     *
     * @throws IllegalStateException if a terminal event has already been emitted
     */
    @Override
    public ResponseEventStream emitFailed(ResponseError.Code code, String message, ResponseUsage usage) {
        guardTerminal();
        ResponseUsage effectiveUsage = usage != null ? usage : defaultUsage();
        synchronized (builderLock) {
            responseBuilder = responseBuilder
                .status(ResponseStatus.FAILED)
                .completedAt(System.currentTimeMillis() / 1000.0)
                .error(ResponseError.builder().code(code).message(message).build())
                .output(collectOutputItems())
                .usage(effectiveUsage);
        }
        addEvent(new ResponseEvent("response.failed",
            ResponseStreamEvent.ofFailed(new ResponseFailedEvent.Builder()
                .sequenceNumber(nextSequenceNumber())
                .response(snapshot())
                .build())));
        completeMulti();
        return this;
    }

    /**
     * Emits a {@code response.incomplete} event with no details.
     */
    @Override
    public ResponseEventStream emitIncomplete() {
        return emitIncomplete(null, null);
    }

    // ── Output Item Scope Factories (Consumer pattern) ────────

    /**
     * Emits a {@code response.incomplete} event with the given reason.
     */
    @Override
    public ResponseEventStream emitIncomplete(Response.IncompleteDetails.Reason reason) {
        return emitIncomplete(reason, null);
    }

    /**
     * Emits a {@code response.incomplete} event with optional reason and usage.
     *
     * @throws IllegalStateException if a terminal event has already been emitted
     */
    @Override
    public ResponseEventStream emitIncomplete(Response.IncompleteDetails.Reason reason, ResponseUsage usage) {
        guardTerminal();
        ResponseUsage effectiveUsage = usage != null ? usage : defaultUsage();
        synchronized (builderLock) {
            responseBuilder = responseBuilder
                .status(ResponseStatus.INCOMPLETE)
                .completedAt(System.currentTimeMillis() / 1000.0)
                .output(collectOutputItems())
                .usage(effectiveUsage);
        }
        if (reason != null) {
            synchronized (builderLock) {
                responseBuilder = responseBuilder.incompleteDetails(
                    Response.IncompleteDetails.builder().reason(reason).build());
            }
        }
        addEvent(new ResponseEvent("response.incomplete",
            ResponseStreamEvent.ofIncomplete(new ResponseIncompleteEvent.Builder()
                .sequenceNumber(nextSequenceNumber())
                .response(snapshot())
                .build())));
        completeMulti();
        return this;
    }

    /**
     * Adds a message output item. The consumer configures the message builder
     * (emit added/done, add text parts, etc.) within a lexically scoped block.
     */
    @Override
    public ResponseEventStream addOutputMessage(Consumer<ResponseEventStream.OutputMessageBuilder> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateMessageItemId();
        OutputMessageBuilder builder = new OutputMessageBuilder(this, idx, itemId);
        config.accept(builder);
        return this;
    }

    /**
     * Adds a function call output item. The consumer configures the function call builder
     * (emit added/done, emit argument deltas, etc.) within a lexically scoped block.
     */
    @Override
    public ResponseEventStream addOutputFunctionCall(Consumer<ResponseEventStream.OutputFunctionCallBuilder> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateFunctionCallItemId();
        config.accept(new OutputFunctionCallBuilder(this, idx, itemId));
        return this;
    }

    /**
     * Adds a reasoning output item.
     */
    @Override
    public ResponseEventStream addOutputReasoningItem(Consumer<ResponseEventStream.OutputItemBuilder<ResponseReasoningItem>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateReasoningItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds a file search call output item.
     */
    @Override
    public ResponseEventStream addOutputFileSearchCall(Consumer<ResponseEventStream.OutputItemBuilder<ResponseFileSearchToolCall>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateFileSearchCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds a web search call output item.
     */
    @Override
    public ResponseEventStream addOutputWebSearchCall(Consumer<ResponseEventStream.OutputItemBuilder<ResponseFunctionWebSearch>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateWebSearchCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds a code interpreter call output item.
     */
    @Override
    public ResponseEventStream addOutputCodeInterpreterCall(
        Consumer<ResponseEventStream.OutputItemBuilder<ResponseCodeInterpreterToolCall>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateCodeInterpreterCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds an image generation call output item.
     */
    @Override
    public ResponseEventStream addOutputImageGenCall(
        Consumer<ResponseEventStream.OutputItemBuilder<ResponseOutputItem.ImageGenerationCall>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateImageGenCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds an MCP tool call output item.
     */
    @Override
    public ResponseEventStream addOutputMcpCall(Consumer<ResponseEventStream.OutputItemBuilder<ResponseOutputItem.McpCall>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateMcpCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds an MCP list-tools output item.
     */
    @Override
    public ResponseEventStream addOutputMcpListTools(
        Consumer<ResponseEventStream.OutputItemBuilder<ResponseOutputItem.McpListTools>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateMcpListToolsItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds a custom tool call output item.
     */
    @Override
    public ResponseEventStream addOutputCustomToolCall(Consumer<ResponseEventStream.OutputItemBuilder<ResponseCustomToolCall>> config) {
        long idx = outputIndex.getAndIncrement();
        String itemId = idGenerator.generateCustomToolCallItemId();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Adds a generic output item for types with no dedicated factory.
     */
    @Override
    public <T> ResponseEventStream addOutputItem(String itemId, Consumer<ResponseEventStream.OutputItemBuilder<T>> config) {
        Objects.requireNonNull(itemId, "itemId must not be null");
        long idx = outputIndex.getAndIncrement();
        config.accept(new OutputItemBuilder<>(this, idx, itemId));
        return this;
    }

    /**
     * Appends a pre-built event directly. For advanced / interop scenarios.
     */
    @Override
    public ResponseEventStream emit(ResponseEvent event) {
        addEvent(event);
        return this;
    }

    // ── Internal helpers ──────────────────────────────────────

    /**
     * Forwards an upstream OpenAI streaming response through this stream.
     * All content events (output items, text deltas, function call arguments,
     * reasoning, etc.) are forwarded directly. Upstream lifecycle events
     * (created, in_progress, completed, queued) are skipped since this stream
     * owns its own lifecycle.
     * <p>
     * Throws {@link RuntimeException} if the upstream reports a failure event.
     *
     * <h3>Example usage</h3>
     * <pre>{@code
     * stream.emitCreated().emitInProgress();
     * try (var upstream = client.responses().createStreaming(params)) {
     *     stream.forwardUpstream(upstream.stream());
     * }
     * stream.emitCompleted();
     * }</pre>
     *
     * @param upstreamEvents the stream of events from the upstream OpenAI client
     * @return this stream for chaining
     * @throws RuntimeException if the upstream reports a failure event
     */
    @Override
    public ResponseEventStream forwardUpstream(java.util.stream.Stream<ResponseStreamEvent> upstreamEvents) {
        upstreamEvents.forEach(event -> {
            // Skip upstream lifecycle events — we own the response envelope.
            if (event.isCreated() || event.isInProgress() || event.isCompleted() || event.isQueued()) {
                return;
            }

            // Detect upstream failure.
            if (event.isFailed()) {
                throw new RuntimeException("Upstream request failed");
            }

            // Forward all content events (output_item.added, output_item.done,
            // text deltas, function_call_arguments, content_part events, etc.)
            // by determining the event name and re-emitting through our stream.
            String eventName = resolveEventName(event);
            if (eventName != null) {
                // Track output items for the terminal completed event.
                if (event.isOutputItemDone()) {
                    ResponseOutputItem item = event.asOutputItemDone().item();
                    long idx = outputIndex.getAndIncrement();
                    trackCompletedOutputItem(item, idx);
                }
                addEvent(new ResponseEvent(eventName, event));
            }
        });
        return this;
    }

    public long nextSequenceNumber() {
        return sequenceNumber.getAndIncrement();
    }

    public synchronized void addEvent(ResponseEvent event) {
        events.add(event);
        publisher.submit(event);
    }

    public void trackCompletedOutputItem(ResponseOutputItem item, long idx) {
        outputItems.put(idx, item);
    }

    /**
     * Returns a copy of the accumulated output items in index order.
     */
    private List<ResponseOutputItem> collectOutputItems() {
        return new ArrayList<>(outputItems.values());
    }

    private Response snapshot() {
        synchronized (builderLock) {
            return responseBuilder.build();
        }
    }

    /**
     * Atomically guards against calling a terminal method more than once.
     *
     * @throws IllegalStateException if a terminal event has already been emitted
     */
    private void guardTerminal() {
        if (!terminated.compareAndSet(false, true)) {
            throw new IllegalStateException(
                "Stream has already been terminated. " +
                    "emitCompleted/emitFailed/emitIncomplete may only be called once.");
        }
    }

    private void completeMulti() {
        publisher.close();
    }

    // ══════════════════════════════════════════════════════════
    //  Scoped builders
    // ══════════════════════════════════════════════════════════

    /**
     * Fluent builder for a generic output item. Returned by the various
     * {@code addOutput*()} factory methods.
     */
    public static class OutputItemBuilder<T> implements ResponseEventStream.OutputItemBuilder<T> {
        protected final ResponseEventStream stream;
        protected final long outputIdx;
        protected final String itemId;

        OutputItemBuilder(ResponseEventStream stream, long outputIdx, String itemId) {
            this.stream = stream;
            this.outputIdx = outputIdx;
            this.itemId = itemId;
        }

        static ResponseOutputItem toOutputItem(Object item) {
            if (item instanceof ResponseOutputItem roi) return roi;
            if (item instanceof ResponseOutputMessage msg) return ResponseOutputItem.ofMessage(msg);
            if (item instanceof ResponseFunctionToolCall ftc) return ResponseOutputItem.ofFunctionCall(ftc);
            if (item instanceof ResponseReasoningItem ri) return ResponseOutputItem.ofReasoning(ri);
            if (item instanceof ResponseFileSearchToolCall fsc) return ResponseOutputItem.ofFileSearchCall(fsc);
            if (item instanceof ResponseFunctionWebSearch ws) return ResponseOutputItem.ofWebSearchCall(ws);
            if (item instanceof ResponseCodeInterpreterToolCall ci) return ResponseOutputItem.ofCodeInterpreterCall(ci);
            if (item instanceof ResponseOutputItem.ImageGenerationCall ig)
                return ResponseOutputItem.ofImageGenerationCall(ig);
            if (item instanceof ResponseOutputItem.McpCall mc) return ResponseOutputItem.ofMcpCall(mc);
            if (item instanceof ResponseOutputItem.McpListTools mlt) return ResponseOutputItem.ofMcpListTools(mlt);
            if (item instanceof ResponseCustomToolCall ct) return ResponseOutputItem.ofCustomToolCall(ct);
            throw new IllegalArgumentException("Unsupported output item type: " + item.getClass());
        }

        /**
         * Emits a {@code response.output_item.added} event for this item.
         */
        public OutputItemBuilder<T> emitAdded(T item) {
            ResponseOutputItem outputItem = toOutputItem(item);
            stream.addEvent(new ResponseEvent("response.output_item.added",
                ResponseStreamEvent.ofOutputItemAdded(new ResponseOutputItemAddedEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(outputItem)
                    .build())));
            return this;
        }

        /**
         * Emits a {@code response.output_item.done} event for this item
         * and tracks it in the parent stream's output list.
         */
        public OutputItemBuilder<T> emitDone(T item) {
            ResponseOutputItem outputItem = toOutputItem(item);
            stream.trackCompletedOutputItem(outputItem, outputIdx);
            stream.addEvent(new ResponseEvent("response.output_item.done",
                ResponseStreamEvent.ofOutputItemDone(new ResponseOutputItemDoneEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(outputItem)
                    .build())));
            return this;
        }

        /**
         * Returns the auto-generated item ID for this output item.
         */
        public String getItemId() {
            return itemId;
        }
    }

    /**
     * Fluent builder for a function call output item. Provides convenience methods
     * for the common function call lifecycle: added, argument deltas, argument done,
     * and item done — without requiring the caller to manage sequence numbers or
     * construct raw event objects.
     *
     * <h3>Example usage</h3>
     * <pre>{@code
     * stream.addOutputFunctionCall(func -> func
     *     .emitAdded("get_weather", "call_123")
     *     .emitArgumentsDelta("{\"location\":")
     *     .emitArgumentsDelta("\"Seattle\"}")
     *     .emitArgumentsDone("get_weather", "{\"location\":\"Seattle\"}")
     *     .emitDone());
     * }</pre>
     */
    public static class OutputFunctionCallBuilder implements ResponseEventStream.OutputFunctionCallBuilder {
        private final AgentServerResponseEventStream stream;
        private final long outputIdx;
        private final String itemId;
        private final StringBuilder accumulatedArguments = new StringBuilder();
        private String name;
        private String callId;

        OutputFunctionCallBuilder(AgentServerResponseEventStream stream, long outputIdx, String itemId) {
            this.stream = stream;
            this.outputIdx = outputIdx;
            this.itemId = itemId;
        }

        /**
         * Emits a {@code response.output_item.added} event with an in-progress function call.
         *
         * @param name   the function name (e.g. "get_weather")
         * @param callId the call ID for this function invocation
         */
        public OutputFunctionCallBuilder emitAdded(String name, String callId) {
            this.name = name;
            this.callId = callId;
            ResponseFunctionToolCall call = ResponseFunctionToolCall.builder()
                .id(itemId)
                .callId(callId)
                .name(name)
                .arguments("")
                .status(ResponseFunctionToolCall.Status.IN_PROGRESS)
                .build();
            stream.addEvent(new ResponseEvent("response.output_item.added",
                ResponseStreamEvent.ofOutputItemAdded(new ResponseOutputItemAddedEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(ResponseOutputItem.ofFunctionCall(call))
                    .build())));
            return this;
        }

        /**
         * Emits a {@code response.function_call_arguments.delta} event and accumulates
         * the arguments text.
         *
         * @param delta the argument text chunk
         */
        public OutputFunctionCallBuilder emitArgumentsDelta(String delta) {
            accumulatedArguments.append(delta);
            stream.addEvent(new ResponseEvent("response.function_call_arguments.delta",
                ResponseStreamEvent.ofFunctionCallArgumentsDelta(
                    ResponseFunctionCallArgumentsDeltaEvent.builder()
                        .sequenceNumber(stream.nextSequenceNumber())
                        .itemId(itemId)
                        .outputIndex(outputIdx)
                        .delta(delta)
                        .build())));
            return this;
        }

        /**
         * Emits a {@code response.function_call_arguments.done} event with the final
         * function name and complete arguments string.
         *
         * @param name      the function name
         * @param arguments the complete arguments JSON string
         */
        public OutputFunctionCallBuilder emitArgumentsDone(String name, String arguments) {
            accumulatedArguments.setLength(0);
            accumulatedArguments.append(arguments);
            stream.addEvent(new ResponseEvent("response.function_call_arguments.done",
                ResponseStreamEvent.ofFunctionCallArgumentsDone(
                    ResponseFunctionCallArgumentsDoneEvent.builder()
                        .sequenceNumber(stream.nextSequenceNumber())
                        .itemId(itemId)
                        .outputIndex(outputIdx)
                        .name(name)
                        .arguments(arguments)
                        .build())));
            return this;
        }

        /**
         * Emits a {@code response.output_item.done} event with the completed function call
         * and tracks it in the parent stream's output list. Uses the accumulated arguments
         * and the name/callId from {@link #emitAdded(String, String)}.
         */
        public OutputFunctionCallBuilder emitDone() {
            ResponseFunctionToolCall completedCall = ResponseFunctionToolCall.builder()
                .id(itemId)
                .callId(callId)
                .name(name)
                .arguments(accumulatedArguments.toString())
                .status(ResponseFunctionToolCall.Status.COMPLETED)
                .build();
            ResponseOutputItem outputItem = ResponseOutputItem.ofFunctionCall(completedCall);
            stream.trackCompletedOutputItem(outputItem, outputIdx);
            stream.addEvent(new ResponseEvent("response.output_item.done",
                ResponseStreamEvent.ofOutputItemDone(new ResponseOutputItemDoneEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(outputItem)
                    .build())));
            return this;
        }

        /**
         * Returns the auto-generated item ID for this function call.
         */
        public String getItemId() {
            return itemId;
        }
    }

    /**
     * Fluent builder for a message output item. Provides convenience methods
     * for the common message lifecycle and lexically scoped text-part builders.
     */
    public static class OutputMessageBuilder implements ResponseEventStream.OutputMessageBuilder {
        private final AgentServerResponseEventStream stream;
        private final long outputIdx;
        private final String itemId;
        private final String responseId;
        private final List<ResponseOutputMessage.Content> contentParts = new ArrayList<>();

        OutputMessageBuilder(AgentServerResponseEventStream stream, long outputIdx, String itemId) {
            this.stream = stream;
            this.outputIdx = outputIdx;
            this.itemId = itemId;
            this.responseId = stream.responseId;
        }

        /**
         * Emits a {@code response.output_item.added} event with an in-progress message.
         */
        public OutputMessageBuilder emitAdded() {
            ResponseOutputItem outputItem = ResponseOutputItem.ofMessage(
                buildMessage(ResponseOutputMessage.Status.IN_PROGRESS));
            stream.addEvent(new ResponseEvent("response.output_item.added",
                ResponseStreamEvent.ofOutputItemAdded(new ResponseOutputItemAddedEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(outputItem)
                    .build())));
            return this;
        }

        /**
         * Adds a text content part. The consumer configures delta emissions within
         * a lexically scoped block.
         * <p>
         * When the consumer returns, the following events are automatically emitted
         * if the consumer did not already emit them:
         * <ul>
         *  <li>{@code response.output_text.done} — if not already emitted by
         *  {@link TextPartBuilder#emitDone(String)}</li>
         *  <li>{@code response.content_part.done} — always emitted</li>
         * </ul>
         */
        public OutputMessageBuilder addTextPart(Consumer<ResponseEventStream.TextPartBuilder> config) {
            long contentIndex = contentParts.size();
            TextPartBuilder textBuilder = new TextPartBuilder(stream, itemId, outputIdx, contentIndex);
            config.accept(textBuilder);

            String finalTextValue = textBuilder.accumulatedText.toString();

            // Auto-emit output_text.done if the consumer didn't call emitDone
            if (!textBuilder.doneEmitted) {
                textBuilder.emitDone(finalTextValue);
            }

            // Always emit content_part.done
            ResponseOutputText finalText = ResponseOutputText.builder()
                .text(finalTextValue)
                .annotations(List.of())
                .logprobs(List.of())
                .build();
            contentParts.add(ResponseOutputMessage.Content.ofOutputText(finalText));

            stream.addEvent(new ResponseEvent("response.content_part.done",
                ResponseStreamEvent.ofContentPartDone(new ResponseContentPartDoneEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .itemId(itemId)
                    .outputIndex(outputIdx)
                    .contentIndex(contentIndex)
                    .part(finalText)
                    .build())));
            return this;
        }

        /**
         * Convenience method that emits a complete text message in one call.
         * Equivalent to:
         * <pre>{@code
         * msg.emitAdded()
         *    .addTextPart(text -> text.emitAdded().emitDelta(content).emitDone(content))
         *    .emitDone();
         * }</pre>
         *
         * @param content The full text content to emit as a single delta and done event.
         * @return this builder for chaining.
         */
        public OutputMessageBuilder outputItemMessage(String content) {
            return emitAdded()
                .addTextPart(text -> text
                    .emitAdded()
                    .emitDelta(content)
                    .emitDone(content))
                .emitDone();
        }

        /**
         * Emits a {@code response.output_item.done} event with the completed message
         * and tracks it in the parent stream's output list.
         */
        public OutputMessageBuilder emitDone() {
            ResponseOutputMessage msg = buildMessage(ResponseOutputMessage.Status.COMPLETED);
            ResponseOutputItem outputItem = ResponseOutputItem.ofMessage(msg);
            stream.trackCompletedOutputItem(outputItem, outputIdx);
            stream.addEvent(new ResponseEvent("response.output_item.done",
                ResponseStreamEvent.ofOutputItemDone(new ResponseOutputItemDoneEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .outputIndex(outputIdx)
                    .item(outputItem)
                    .build())));
            return this;
        }

        /**
         * Returns the auto-generated item ID for this message.
         */
        public String getItemId() {
            return itemId;
        }

        private ResponseOutputMessage buildMessage(ResponseOutputMessage.Status status) {
            // Build the created_by object matching Foundry platform expectations:
            // {"agent": {"type": "agent_id", "name": "", "version": ""}, "response_id": "..."}
            Map<String, Object> agentObj = new LinkedHashMap<>();
            agentObj.put("type", "agent_id");
            agentObj.put("name", "");
            agentObj.put("version", "");
            Map<String, Object> createdBy = new LinkedHashMap<>();
            createdBy.put("agent", agentObj);
            createdBy.put("response_id", responseId);

            return ResponseOutputMessage.builder()
                .id(itemId)
                .content(new ArrayList<>(contentParts))
                .status(status)
                .putAdditionalProperty("created_by", com.openai.core.JsonValue.from(createdBy))
                .build();
        }
    }

    /**
     * Fluent builder for a text content part within a message.
     * Tracks accumulated delta text for the final {@code content_part.done} event
     * (emitted automatically by the parent {@link OutputMessageBuilder}).
     */
    public static class TextPartBuilder implements ResponseEventStream.TextPartBuilder {
        final StringBuilder accumulatedText = new StringBuilder();
        private final AgentServerResponseEventStream stream;
        private final String itemId;
        private final long outputIdx;
        private final long contentIndex;
        boolean doneEmitted;

        TextPartBuilder(AgentServerResponseEventStream stream, String itemId, long outputIdx, long contentIndex) {
            this.stream = stream;
            this.itemId = itemId;
            this.outputIdx = outputIdx;
            this.contentIndex = contentIndex;
        }

        /**
         * Emits a {@code response.content_part.added} event with an empty text part.
         */
        public TextPartBuilder emitAdded() {
            ResponseOutputText empty = ResponseOutputText.builder()
                .text("")
                .annotations(List.of())
                .build();
            stream.addEvent(new ResponseEvent("response.content_part.added",
                ResponseStreamEvent.ofContentPartAdded(new ResponseContentPartAddedEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .itemId(itemId)
                    .outputIndex(outputIdx)
                    .contentIndex(contentIndex)
                    .part(empty)
                    .build())));
            return this;
        }

        /**
         * Emits a {@code response.output_text.delta} event and accumulates the text.
         */
        public TextPartBuilder emitDelta(String delta) {
            accumulatedText.append(delta);
            stream.addEvent(new ResponseEvent("response.output_text.delta",
                ResponseStreamEvent.ofOutputTextDelta(new ResponseTextDeltaEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .itemId(itemId)
                    .outputIndex(outputIdx)
                    .contentIndex(contentIndex)
                    .delta(delta)
                    .logprobs(List.of())
                    .build())));
            return this;
        }

        /**
         * Emits a {@code response.output_text.done} event with the final text.
         * Also updates the accumulated text to the provided value.
         */
        public TextPartBuilder emitDone(String text) {
            accumulatedText.setLength(0);
            accumulatedText.append(text);
            doneEmitted = true;
            stream.addEvent(new ResponseEvent("response.output_text.done",
                ResponseStreamEvent.ofOutputTextDone(new ResponseTextDoneEvent.Builder()
                    .sequenceNumber(stream.nextSequenceNumber())
                    .itemId(itemId)
                    .outputIndex(outputIdx)
                    .contentIndex(contentIndex)
                    .text(text)
                    .logprobs(List.of())
                    .build())));
            return this;
        }
    }
}
