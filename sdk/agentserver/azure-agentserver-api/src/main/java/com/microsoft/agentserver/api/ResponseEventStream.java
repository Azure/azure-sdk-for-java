// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseCustomToolCall;
import com.openai.models.responses.ResponseError;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseReasoningItem;
import com.openai.models.responses.ResponseStatus;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseUsage;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public interface ResponseEventStream {

    /**
     * Creates a new {@link Builder} for constructing {@link ResponseEventStream} instances.
     *
     * @return a new builder instance.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new event stream from a request and context.
     *
     * @param context the response context (required).
     * @param request the create-response request (required).
     * @return a new {@link ResponseEventStream}.
     */
    static ResponseEventStream create(ResponseContext context, AgentServerCreateResponse request) {
        return AgentServerResponseEventStream.create(context, request);
    }

    List<ResponseEvent> getEvents();

    Response getResponse();

    void subscribe(Consumer<ResponseEvent> onEvent, Consumer<Throwable> onFailure, Runnable onComplete);

    void awaitSubscription() throws InterruptedException;

    ResponseEventStream emitQueued();

    ResponseEventStream emitCreated();

    ResponseEventStream emitCreated(ResponseStatus status);

    ResponseEventStream emitInProgress();

    ResponseEventStream emitCompleted();

    ResponseEventStream emitCompleted(ResponseUsage usage);

    ResponseEventStream emitFailed();

    ResponseEventStream emitFailed(ResponseError.Code code, String message);

    ResponseEventStream emitFailed(ResponseError.Code code, String message, ResponseUsage usage);

    ResponseEventStream emitIncomplete();

    ResponseEventStream emitIncomplete(Response.IncompleteDetails.Reason reason);

    ResponseEventStream emitIncomplete(Response.IncompleteDetails.Reason reason, ResponseUsage usage);

    ResponseEventStream addOutputMessage(Consumer<OutputMessageBuilder> config);

    ResponseEventStream addOutputFunctionCall(Consumer<OutputFunctionCallBuilder> config);

    ResponseEventStream addOutputReasoningItem(Consumer<OutputItemBuilder<ResponseReasoningItem>> config);

    ResponseEventStream addOutputFileSearchCall(Consumer<OutputItemBuilder<ResponseFileSearchToolCall>> config);

    ResponseEventStream addOutputWebSearchCall(Consumer<OutputItemBuilder<ResponseFunctionWebSearch>> config);

    ResponseEventStream addOutputCodeInterpreterCall(
        Consumer<OutputItemBuilder<ResponseCodeInterpreterToolCall>> config);

    ResponseEventStream addOutputImageGenCall(
        Consumer<OutputItemBuilder<ResponseOutputItem.ImageGenerationCall>> config);

    ResponseEventStream addOutputMcpCall(Consumer<OutputItemBuilder<ResponseOutputItem.McpCall>> config);

    ResponseEventStream addOutputMcpListTools(
        Consumer<OutputItemBuilder<ResponseOutputItem.McpListTools>> config);

    ResponseEventStream addOutputCustomToolCall(Consumer<OutputItemBuilder<ResponseCustomToolCall>> config);

    <T> ResponseEventStream addOutputItem(String itemId, Consumer<OutputItemBuilder<T>> config);

    ResponseEventStream emit(ResponseEvent event);

    ResponseEventStream forwardUpstream(java.util.stream.Stream<ResponseStreamEvent> upstreamEvents);

    void trackCompletedOutputItem(ResponseOutputItem outputItem, long outputIdx);

    void addEvent(ResponseEvent responseEvent);

    long nextSequenceNumber();

    // ── Nested builder interfaces ───────────────────────────────

    /**
     * Fluent builder for a generic output item. Provides methods to emit
     * {@code output_item.added} and {@code output_item.done} events.
     *
     * @param <T> the output item model type
     */
    interface OutputItemBuilder<T> {

        /**
         * Emits a {@code response.output_item.added} event for this item.
         *
         * @param item the output item to emit.
         * @return this builder.
         */
        OutputItemBuilder<T> emitAdded(T item);

        /**
         * Emits a {@code response.output_item.done} event for this item
         * and tracks it in the parent stream's output list.
         *
         * @param item the completed output item.
         * @return this builder.
         */
        OutputItemBuilder<T> emitDone(T item);

        /**
         * Returns the auto-generated item ID for this output item.
         */
        String getItemId();
    }

    /**
     * Fluent builder for a function call output item. Provides convenience methods
     * for the common function call lifecycle: added, argument deltas, argument done,
     * and item done.
     *
     * <pre>{@code
     * stream.addOutputFunctionCall(func -> func
     *     .emitAdded("get_weather", "call_123")
     *     .emitArgumentsDelta("{\"location\":")
     *     .emitArgumentsDelta("\"Seattle\"}")
     *     .emitArgumentsDone("get_weather", "{\"location\":\"Seattle\"}")
     *     .emitDone());
     * }</pre>
     */
    interface OutputFunctionCallBuilder {

        /**
         * Emits a {@code response.output_item.added} event with an in-progress function call.
         *
         * @param name   the function name (e.g. "get_weather").
         * @param callId the call ID for this function invocation.
         * @return this builder.
         */
        OutputFunctionCallBuilder emitAdded(String name, String callId);

        /**
         * Emits a {@code response.function_call_arguments.delta} event and accumulates
         * the arguments text.
         *
         * @param delta the argument text chunk.
         * @return this builder.
         */
        OutputFunctionCallBuilder emitArgumentsDelta(String delta);

        /**
         * Emits a {@code response.function_call_arguments.done} event with the final
         * function name and complete arguments string.
         *
         * @param name      the function name.
         * @param arguments the complete arguments JSON string.
         * @return this builder.
         */
        OutputFunctionCallBuilder emitArgumentsDone(String name, String arguments);

        /**
         * Emits a {@code response.output_item.done} event with the completed function call
         * and tracks it in the parent stream's output list.
         *
         * @return this builder.
         */
        OutputFunctionCallBuilder emitDone();

        /**
         * Returns the auto-generated item ID for this function call.
         */
        String getItemId();
    }

    /**
     * Fluent builder for a message output item. Provides convenience methods
     * for the common message lifecycle and lexically scoped text-part builders.
     */
    interface OutputMessageBuilder {

        /**
         * Emits a {@code response.output_item.added} event with an in-progress message.
         *
         * @return this builder.
         */
        OutputMessageBuilder emitAdded();

        /**
         * Adds a text content part. The consumer configures delta emissions within
         * a lexically scoped block.
         *
         * @param config consumer that configures the text part builder.
         * @return this builder.
         */
        OutputMessageBuilder addTextPart(Consumer<TextPartBuilder> config);

        /**
         * Convenience method that emits a complete text message in one call.
         * Equivalent to:
         * <pre>{@code
         * msg.emitAdded()
         *    .addTextPart(text -> text.emitAdded().emitDelta(content).emitDone(content))
         *    .emitDone();
         * }</pre>
         *
         * @param content the full text content to emit as a single delta and done event.
         * @return this builder.
         */
        OutputMessageBuilder outputItemMessage(String content);

        /**
         * Emits a {@code response.output_item.done} event with the completed message
         * and tracks it in the parent stream's output list.
         *
         * @return this builder.
         */
        OutputMessageBuilder emitDone();

        /**
         * Returns the auto-generated item ID for this message.
         */
        String getItemId();
    }

    /**
     * Fluent builder for a text content part within a message.
     * Tracks accumulated delta text for the final {@code content_part.done} event.
     */
    interface TextPartBuilder {

        /**
         * Emits a {@code response.content_part.added} event with an empty text part.
         *
         * @return this builder.
         */
        TextPartBuilder emitAdded();

        /**
         * Emits a {@code response.output_text.delta} event and accumulates the text.
         *
         * @param delta the text chunk.
         * @return this builder.
         */
        TextPartBuilder emitDelta(String delta);

        /**
         * Emits a {@code response.output_text.done} event with the final text.
         *
         * @param text the final text value.
         * @return this builder.
         */
        TextPartBuilder emitDone(String text);
    }

    // ── ResponseEventStream.Builder ─────────────────────────────

    /**
     * Builder for constructing {@link ResponseEventStream} instances.
     * <p>
     * All parameters are set via fluent setters. Required parameters
     * ({@code context}, {@code request}) are validated when {@link #build()} is called.
     *
     * <pre>{@code
     * ResponseEventStream stream = ResponseEventStream.builder()
     *     .context(responseContext)
     *     .request(createResponse)
     *     .build();
     * }</pre>
     */
    final class Builder {
        private ResponseContext context;
        private AgentServerCreateResponse request;

        Builder() {
        }

        /**
         * Sets the response context (required).
         *
         * @param context the response context.
         * @return this builder.
         */
        public Builder context(ResponseContext context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the create-response request (required).
         *
         * @param request the create-response request.
         * @return this builder.
         */
        public Builder request(AgentServerCreateResponse request) {
            this.request = request;
            return this;
        }

        /**
         * Builds and returns a new {@link ResponseEventStream} instance.
         *
         * @return the constructed {@link ResponseEventStream}.
         * @throws NullPointerException if {@code context} or {@code request} is null.
         */
        public ResponseEventStream build() {
            Objects.requireNonNull(context, "context must not be null");
            Objects.requireNonNull(request, "request must not be null");
            return AgentServerResponseEventStream.create(context, request);
        }
    }
}
