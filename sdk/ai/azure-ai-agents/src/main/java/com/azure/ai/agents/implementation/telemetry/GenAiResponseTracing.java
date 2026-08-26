// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation.telemetry;

import com.azure.ai.agents.implementation.http.OpenAITracingContextBridge;
import com.azure.ai.agents.models.AgentReference;
import com.azure.ai.agents.models.AzureCreateResponseOptions;
import com.openai.helpers.ResponseAccumulator;
import com.openai.core.http.HttpResponseFor;
import com.openai.core.http.StreamResponse;
import com.openai.models.responses.EasyInputMessage;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCodeInterpreterToolCall;
import com.openai.models.responses.ResponseComputerToolCall;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ResponseFileSearchToolCall;
import com.openai.models.responses.ResponseFunctionToolCall;
import com.openai.models.responses.ResponseFunctionWebSearch;
import com.openai.models.responses.ResponseInputItem;
import com.openai.models.responses.ResponseOutputItem;
import com.openai.models.responses.ResponseOutputMessage;
import com.openai.models.responses.ResponseOutputText;
import com.openai.models.responses.ResponseStreamEvent;
import com.openai.models.responses.ResponseUsage;
import com.openai.models.conversations.Conversation;
import com.openai.models.conversations.ConversationCreateParams;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Tracing for the response convenience methods on {@link com.azure.ai.agents.ResponsesClient}. Wraps response
 * creation with a {@code chat} or {@code invoke_agent} span (based on whether an {@link AgentReference} is present),
 * recording request/response attributes, token usage, input/output messages (content-gated), and metrics. Also
 * traces {@code create_conversation}.
 *
 * <p>Constructed with a per-client {@link GenAiInstrumentation}; there is no global state.</p>
 */
public final class GenAiResponseTracing {

    private final GenAiInstrumentation instrumentation;
    private final OpenAITracingContextBridge contextBridge;

    /**
     * Creates a {@link GenAiResponseTracing}.
     *
     * @param instrumentation the per-client telemetry holder.
     */
    public GenAiResponseTracing(GenAiInstrumentation instrumentation) {
        this(instrumentation, null);
    }

    /**
     * Creates a {@link GenAiResponseTracing} with an asynchronous OpenAI context bridge.
     *
     * @param instrumentation the per-client telemetry holder.
     * @param contextBridge the per-client bridge used by the OpenAI HTTP adapter.
     */
    public GenAiResponseTracing(GenAiInstrumentation instrumentation, OpenAITracingContextBridge contextBridge) {
        this.instrumentation = instrumentation;
        this.contextBridge = contextBridge;
    }

    /**
     * @return whether any span/metric collection is active.
     */
    public boolean isEnabled() {
        return instrumentation.isEnabled();
    }

    private boolean captureContent() {
        return instrumentation.isContentRecordingEnabled();
    }

    /**
     * Traces a non-streaming response operation, extracting tracing parameters from the request objects.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param operation the supplier that performs the actual API call.
     * @return the response from the operation.
     */
    @SuppressWarnings("try")
    public Response traceResponse(AzureCreateResponseOptions createResponse, ResponseCreateParams builtParams,
        Supplier<Response> operation) {
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        GenAiTracingScope scope = startResponseScope(params);
        if (scope == null) {
            return operation.get();
        }

        try {
            Response response;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                response = operation.get();
            }
            recordResponseAttributes(scope, response, params.isInvokeAgent);
            return response;
        } catch (Exception e) {
            scope.recordError(e);
            sneakyThrows(e);
            return null;
        } finally {
            scope.close();
        }
    }

    /**
     * Traces a streaming response operation. The span remains open until the returned iterable is fully consumed.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param operation the supplier that starts the streaming operation.
     * @return a traced iterable that wraps the stream and records attributes on completion.
     */
    @SuppressWarnings("try")
    public TracedStreamIterable traceStreamingResponse(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, Supplier<Iterable<ResponseStreamEvent>> operation) {
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        GenAiTracingScope scope = startResponseScope(params);
        if (scope == null) {
            return new TracedStreamIterable(operation.get(), null, this, false);
        }

        try {
            Iterable<ResponseStreamEvent> stream;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                stream = operation.get();
            }
            return new TracedStreamIterable(stream, scope, this, params.isInvokeAgent);
        } catch (Exception e) {
            scope.recordError(e);
            scope.close();
            sneakyThrows(e);
            return null;
        }
    }

    /**
     * Traces an asynchronous non-streaming response operation.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param operation the function that starts the asynchronous API call with trace context in the request headers.
     * @return a {@link Mono} emitting the response from the operation.
     */
    public Mono<Response> traceResponseAsync(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, Function<ResponseCreateParams, Mono<Response>> operation) {
        if (!instrumentation.isEnabled()) {
            return operation.apply(builtParams);
        }
        ResponseSpanParams params = extractParams(createResponse, builtParams);

        Mono<GenAiTracingScope> resourceSupplier = Mono.fromSupplier(() -> startResponseScope(params));
        Function<GenAiTracingScope, Mono<Response>> resourceClosure
            = scope -> startMonoWithContext(scope, builtParams, operation).map(response -> {
                recordResponseAttributes(scope, response, params.isInvokeAgent);
                return response;
            });
        return Mono.usingWhen(resourceSupplier, resourceClosure, scope -> {
            scope.close();
            return Mono.empty();
        }, (scope, throwable) -> {
            scope.recordError(throwable);
            scope.close();
            return Mono.empty();
        }, scope -> {
            scope.close();
            return Mono.empty();
        });
    }

    /**
     * Traces an asynchronous streaming response operation. The span remains open until the returned {@link Flux} is
     * fully consumed, cancelled, or errors.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the built request parameters.
     * @param operation the function that starts the streaming operation with trace context in the request headers.
     * @return a {@link Flux} that wraps the stream and records attributes on completion.
     */
    public Flux<ResponseStreamEvent> traceStreamingResponseAsync(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, Function<ResponseCreateParams, Flux<ResponseStreamEvent>> operation) {
        if (!instrumentation.isEnabled()) {
            return operation.apply(builtParams);
        }
        ResponseSpanParams params = extractParams(createResponse, builtParams);

        Mono<StreamingState> resourceSupplier
            = Mono.fromSupplier(() -> new StreamingState(startResponseScope(params), params.isInvokeAgent));
        Function<StreamingState, Flux<ResponseStreamEvent>> resourceClosure
            = state -> startFluxWithContext(state.scope, builtParams, operation).doOnNext(state::accumulate);
        return Flux.usingWhen(resourceSupplier, resourceClosure, state -> {
            state.finalizeStream(this);
            return Mono.empty();
        }, (state, throwable) -> {
            state.recordError(throwable);
            return Mono.empty();
        }, state -> {
            state.close();
            return Mono.empty();
        });
    }

    private <T> Mono<T> startMonoWithContext(GenAiTracingScope scope, ResponseCreateParams params,
        Function<ResponseCreateParams, Mono<T>> operation) {
        if (contextBridge == null) {
            return startMono(scope, () -> operation.apply(params));
        }
        String token = contextBridge.register(scope.getSpanContext());
        ResponseCreateParams.Builder builder = params.toBuilder();
        builder.replaceAdditionalHeaders(OpenAITracingContextBridge.TRACE_CONTEXT_HEADER, token);
        return startMono(scope, () -> operation.apply(builder.build()))
            .doFinally(signalType -> contextBridge.discard(token));
    }

    /**
     * Traces a synchronous raw-response operation.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the raw request represented as OpenAI parameters.
     * @param operation the raw-response operation.
     * @return the raw HTTP response.
     */
    @SuppressWarnings("try")
    public HttpResponseFor<Response> traceRawResponse(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, Supplier<HttpResponseFor<Response>> operation) {
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        GenAiTracingScope scope = startResponseScope(params);
        if (scope == null) {
            return operation.get();
        }
        try {
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                return operation.get();
            }
        } catch (Exception e) {
            scope.recordError(e);
            sneakyThrows(e);
            return null;
        } finally {
            scope.close();
        }
    }

    /**
     * Traces an asynchronous raw-response operation.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the raw request represented as OpenAI parameters.
     * @param operation the raw-response operation.
     * @return the raw HTTP response publisher.
     */
    public Mono<HttpResponseFor<Response>> traceRawResponseAsync(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams, Function<ResponseCreateParams, Mono<HttpResponseFor<Response>>> operation) {
        if (!instrumentation.isEnabled()) {
            return operation.apply(builtParams);
        }
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        Mono<GenAiTracingScope> resourceSupplier = Mono.fromSupplier(() -> startResponseScope(params));
        return Mono.usingWhen(resourceSupplier, scope -> startMonoWithContext(scope, builtParams, operation), scope -> {
            scope.close();
            return Mono.empty();
        }, (scope, throwable) -> {
            scope.recordError(throwable);
            scope.close();
            return Mono.empty();
        }, scope -> {
            scope.close();
            return Mono.empty();
        });
    }

    /**
     * Traces a synchronous raw streaming operation. The returned response owns the span until its parsed stream is
     * exhausted, fails, or is closed.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the raw request represented as OpenAI parameters.
     * @param operation the raw streaming operation.
     * @return the traced raw streaming response.
     */
    @SuppressWarnings("try")
    public HttpResponseFor<StreamResponse<ResponseStreamEvent>> traceRawStreamingResponse(
        AzureCreateResponseOptions createResponse, ResponseCreateParams builtParams,
        Supplier<HttpResponseFor<StreamResponse<ResponseStreamEvent>>> operation) {
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        GenAiTracingScope scope = startResponseScope(params);
        if (scope == null) {
            return operation.get();
        }
        try {
            HttpResponseFor<StreamResponse<ResponseStreamEvent>> response;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                response = operation.get();
            }
            return new TracedRawStreamingResponse(response, scope, this, params.isInvokeAgent);
        } catch (Exception e) {
            scope.recordError(e);
            scope.close();
            sneakyThrows(e);
            return null;
        }
    }

    /**
     * Traces an asynchronous raw streaming operation. The emitted response owns the span until its parsed stream is
     * exhausted, fails, or is closed.
     *
     * @param createResponse the Azure-specific create response options.
     * @param builtParams the raw request represented as OpenAI parameters.
     * @param operation the raw streaming operation.
     * @return the traced raw streaming response publisher.
     */
    public Mono<HttpResponseFor<StreamResponse<ResponseStreamEvent>>> traceRawStreamingResponseAsync(
        AzureCreateResponseOptions createResponse, ResponseCreateParams builtParams,
        Function<ResponseCreateParams, Mono<HttpResponseFor<StreamResponse<ResponseStreamEvent>>>> operation) {
        if (!instrumentation.isEnabled()) {
            return operation.apply(builtParams);
        }
        ResponseSpanParams params = extractParams(createResponse, builtParams);
        return Mono.defer(() -> {
            GenAiTracingScope scope = startResponseScope(params);
            AtomicBoolean ownershipTransferred = new AtomicBoolean();
            return startMonoWithContext(scope, builtParams, operation).map(response -> {
                HttpResponseFor<StreamResponse<ResponseStreamEvent>> tracedResponse
                    = new TracedRawStreamingResponse(response, scope, this, params.isInvokeAgent);
                ownershipTransferred.set(true);
                return tracedResponse;
            }).doOnError(throwable -> {
                scope.recordError(throwable);
                scope.close();
            }).doOnCancel(() -> {
                if (!ownershipTransferred.get()) {
                    scope.close();
                }
            });
        });
    }

    private Flux<ResponseStreamEvent> startFluxWithContext(GenAiTracingScope scope, ResponseCreateParams params,
        Function<ResponseCreateParams, Flux<ResponseStreamEvent>> operation) {
        if (contextBridge == null) {
            return startFlux(scope, () -> operation.apply(params));
        }
        String token = contextBridge.register(scope.getSpanContext());
        ResponseCreateParams.Builder builder = params.toBuilder();
        builder.replaceAdditionalHeaders(OpenAITracingContextBridge.TRACE_CONTEXT_HEADER, token);
        return startFlux(scope, () -> operation.apply(builder.build()))
            .doFinally(signalType -> contextBridge.discard(token));
    }

    private Mono<Conversation> startConversationWithContext(GenAiTracingScope scope,
        Function<ConversationCreateParams, Mono<Conversation>> operation) {
        if (contextBridge == null) {
            return startMono(scope, () -> operation.apply(ConversationCreateParams.builder().build()));
        }
        String token = contextBridge.register(scope.getSpanContext());
        ConversationCreateParams.Builder builder = ConversationCreateParams.builder();
        builder.replaceAdditionalHeaders(OpenAITracingContextBridge.TRACE_CONTEXT_HEADER, token);
        return startMono(scope, () -> operation.apply(builder.build()))
            .doFinally(signalType -> contextBridge.discard(token));
    }

    /**
     * Traces a synchronous {@code create_conversation} operation.
     *
     * @param operation the supplier that performs the API call.
     * @return the created conversation.
     */
    @SuppressWarnings("try")
    public Conversation traceCreateConversation(Supplier<Conversation> operation) {
        GenAiTracingScope scope = instrumentation.startCreateConversation();
        if (scope == null) {
            return operation.get();
        }
        try {
            Conversation conversation;
            try (AutoCloseable ignored = scope.makeSpanCurrent()) {
                conversation = operation.get();
            }
            scope.setConversationId(conversation.id());
            return conversation;
        } catch (Exception e) {
            scope.recordError(e);
            sneakyThrows(e);
            return null;
        } finally {
            scope.close();
        }
    }

    /**
     * Traces an asynchronous {@code create_conversation} operation.
     *
     * @param operation the function that performs the asynchronous API call with trace context in the request headers.
     * @return a {@link Mono} emitting the created conversation.
     */
    public Mono<Conversation>
        traceCreateConversationAsync(Function<ConversationCreateParams, Mono<Conversation>> operation) {
        if (!instrumentation.isEnabled()) {
            return operation.apply(ConversationCreateParams.builder().build());
        }

        Mono<GenAiTracingScope> resourceSupplier = Mono.fromSupplier(instrumentation::startCreateConversation);
        Function<GenAiTracingScope, Mono<Conversation>> resourceClosure
            = scope -> startConversationWithContext(scope, operation).map(conversation -> {
                scope.setConversationId(conversation.id());
                return conversation;
            });
        return Mono.usingWhen(resourceSupplier, resourceClosure, scope -> {
            scope.close();
            return Mono.empty();
        }, (scope, throwable) -> {
            scope.recordError(throwable);
            scope.close();
            return Mono.empty();
        }, scope -> {
            scope.close();
            return Mono.empty();
        });
    }

    @SuppressWarnings("try")
    private static <T> Mono<T> startMono(GenAiTracingScope scope, Supplier<Mono<T>> operation) {
        try (AutoCloseable ignored = scope.makeSpanCurrent()) {
            return operation.get();
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @SuppressWarnings("try")
    private static <T> Flux<T> startFlux(GenAiTracingScope scope, Supplier<Flux<T>> operation) {
        try (AutoCloseable ignored = scope.makeSpanCurrent()) {
            return operation.get();
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    /**
     * Extracts and formats input messages from {@link ResponseCreateParams} for tracing.
     */
    String extractInputMessages(ResponseCreateParams builtParams) {
        if (!builtParams.input().isPresent()) {
            return null;
        }

        ResponseCreateParams.Input input = builtParams.input().get();
        if (input.isText()) {
            return GenAiMessageFormatter.formatUserTextInput(captureContent(), input.asText());
        }

        if (input.isResponse()) {
            List<ResponseInputItem> items = input.asResponse();
            for (int i = items.size() - 1; i >= 0; i--) {
                ResponseInputItem item = items.get(i);
                if (item.easyInputMessage().isPresent()) {
                    EasyInputMessage msg = item.easyInputMessage().get();
                    if (EasyInputMessage.Role.USER.equals(msg.role()) && msg.content().isTextInput()) {
                        return GenAiMessageFormatter.formatUserTextInput(captureContent(), msg.content().asTextInput());
                    }
                }
                if (item.functionCallOutput().isPresent()) {
                    ResponseInputItem.FunctionCallOutput fco = item.functionCallOutput().get();
                    String output = fco.output().isString() ? fco.output().asString() : null;
                    return GenAiMessageFormatter.formatToolResponseInput(captureContent(), fco.callId(), output);
                }
            }
        }

        return null;
    }

    void recordResponseAttributes(GenAiTracingScope scope, Response response, boolean isInvokeAgent) {
        if (response == null) {
            return;
        }

        String responseId = response.id();
        String responseModel = extractModelString(response.model());
        Long inputTokens = null;
        Long outputTokens = null;

        Optional<ResponseUsage> usageOpt = response.usage();
        if (usageOpt.isPresent()) {
            ResponseUsage usage = usageOpt.get();
            inputTokens = usage.inputTokens();
            outputTokens = usage.outputTokens();
        }

        String outputMessages = formatOutputFromResponse(response);

        scope.setResponseAttributes(responseId, responseModel, inputTokens, outputTokens, null);
        scope.setOutputMessages(outputMessages);

    }

    String formatOutputFromResponse(Response response) {
        if (response.output() == null || response.output().isEmpty()) {
            return null;
        }

        List<ResponseOutputItem> outputs = response.output();
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (ResponseOutputItem item : outputs) {
            String part = formatOutputItem(item);
            if (part == null) {
                continue;
            }
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(part);
        }

        if (first) {
            return "[{\"role\":\"assistant\",\"parts\":[{\"type\":\"text\"}],\"finish_reason\":\"completed\"}]";
        }

        sb.append("]");
        return sb.toString();
    }

    private String formatOutputItem(ResponseOutputItem item) {
        if (item.isMessage()) {
            return formatOutputMessage(item.asMessage());
        } else if (item.isFunctionCall()) {
            return formatFunctionCall(item.asFunctionCall());
        } else if (item.isFileSearchCall()) {
            return formatFileSearchCall(item.asFileSearchCall());
        } else if (item.isWebSearchCall()) {
            return formatWebSearchCall(item.asWebSearchCall());
        } else if (item.isCodeInterpreterCall()) {
            return formatCodeInterpreterCall(item.asCodeInterpreterCall());
        } else if (item.isComputerCall()) {
            return formatComputerCall(item.asComputerCall());
        } else if (item.isMcpCall()) {
            return formatMcpCall(item.asMcpCall());
        } else if (item.isImageGenerationCall()) {
            return formatImageGenerationCall(item.asImageGenerationCall());
        } else if (item.isMcpApprovalRequest()) {
            return formatMcpApprovalRequest(item.asMcpApprovalRequest());
        } else if (item.isMcpListTools()) {
            return formatMcpListTools(item.asMcpListTools());
        }
        return null;
    }

    private String formatFunctionCall(ResponseFunctionToolCall funcCall) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(funcCall.callId()));
        if (captureContent()) {
            sb.append(",\"name\":").append(GenAiMessageFormatter.jsonEscape(funcCall.name()));
            sb.append(",\"arguments\":").append(GenAiMessageFormatter.jsonEscape(funcCall.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    private String formatFileSearchCall(ResponseFileSearchToolCall fileSearch) {
        StringBuilder sb = new StringBuilder(
            "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"file_search_call\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(fileSearch.id()));
        if (captureContent()) {
            List<String> queries = fileSearch.queries();
            if (queries != null && !queries.isEmpty()) {
                sb.append(",\"queries\":[");
                for (int i = 0; i < queries.size(); i++) {
                    if (i > 0) {
                        sb.append(",");
                    }
                    sb.append(GenAiMessageFormatter.jsonEscape(queries.get(i)));
                }
                sb.append("]");
            }
            Optional<List<ResponseFileSearchToolCall.Result>> results = fileSearch.results();
            if (results.isPresent() && !results.get().isEmpty()) {
                sb.append(",\"results\":[");
                boolean firstResult = true;
                for (ResponseFileSearchToolCall.Result result : results.get()) {
                    if (!firstResult) {
                        sb.append(",");
                    }
                    firstResult = false;
                    sb.append("{");
                    boolean hasField = false;
                    if (result.fileId().isPresent()) {
                        sb.append("\"file_id\":").append(GenAiMessageFormatter.jsonEscape(result.fileId().get()));
                        hasField = true;
                    }
                    if (result.filename().isPresent()) {
                        if (hasField) {
                            sb.append(",");
                        }
                        sb.append("\"filename\":").append(GenAiMessageFormatter.jsonEscape(result.filename().get()));
                        hasField = true;
                    }
                    if (result.score().isPresent()) {
                        if (hasField) {
                            sb.append(",");
                        }
                        sb.append("\"score\":").append(result.score().get());
                    }
                    sb.append("}");
                }
                sb.append("]");
            }
        }
        sb.append("}}]}");
        return sb.toString();
    }

    private String formatWebSearchCall(ResponseFunctionWebSearch webSearch) {
        return "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"web_search_call\",\"id\":"
            + GenAiMessageFormatter.jsonEscape(webSearch.id()) + "}}]}";
    }

    private String formatCodeInterpreterCall(ResponseCodeInterpreterToolCall codeInterpreter) {
        StringBuilder sb = new StringBuilder(
            "{\"role\":\"assistant\",\"parts\":[{\"type\":\"tool_call\",\"content\":{\"type\":\"code_interpreter_call\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(codeInterpreter.id()));
        if (captureContent()) {
            Optional<String> code = codeInterpreter.code();
            if (code.isPresent()) {
                sb.append(",\"code\":").append(GenAiMessageFormatter.jsonEscape(code.get()));
            }
        }
        sb.append("}}]}");
        return sb.toString();
    }

    private String formatComputerCall(ResponseComputerToolCall computerCall) {
        return "{\"role\":\"assistant\",\"parts\":[{\"type\":\"computer_call\",\"id\":"
            + GenAiMessageFormatter.jsonEscape(computerCall.callId()) + "}]}";
    }

    private String formatMcpCall(ResponseOutputItem.McpCall mcpCall) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_call\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(mcpCall.id()));
        if (captureContent()) {
            sb.append(",\"name\":").append(GenAiMessageFormatter.jsonEscape(mcpCall.name()));
            sb.append(",\"server_label\":").append(GenAiMessageFormatter.jsonEscape(mcpCall.serverLabel()));
            sb.append(",\"arguments\":").append(GenAiMessageFormatter.jsonEscape(mcpCall.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    private String formatImageGenerationCall(ResponseOutputItem.ImageGenerationCall imageGen) {
        return "{\"role\":\"assistant\",\"parts\":[{\"type\":\"image_generation_call\",\"id\":"
            + GenAiMessageFormatter.jsonEscape(imageGen.id()) + "}]}";
    }

    private String formatMcpApprovalRequest(ResponseOutputItem.McpApprovalRequest approval) {
        StringBuilder sb
            = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_approval_request\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(approval.id()));
        if (captureContent()) {
            sb.append(",\"name\":").append(GenAiMessageFormatter.jsonEscape(approval.name()));
            sb.append(",\"server_label\":").append(GenAiMessageFormatter.jsonEscape(approval.serverLabel()));
            sb.append(",\"arguments\":").append(GenAiMessageFormatter.jsonEscape(approval.arguments()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    private String formatMcpListTools(ResponseOutputItem.McpListTools mcpListTools) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[{\"type\":\"mcp_list_tools\",\"id\":");
        sb.append(GenAiMessageFormatter.jsonEscape(mcpListTools.id()));
        if (captureContent()) {
            sb.append(",\"server_label\":").append(GenAiMessageFormatter.jsonEscape(mcpListTools.serverLabel()));
        }
        sb.append("}]}");
        return sb.toString();
    }

    private String formatOutputMessage(ResponseOutputMessage message) {
        StringBuilder sb = new StringBuilder("{\"role\":\"assistant\",\"parts\":[");
        boolean firstPart = true;

        if (message.content() != null) {
            for (ResponseOutputMessage.Content contentPart : message.content()) {
                if (!firstPart) {
                    sb.append(",");
                }
                firstPart = false;
                if (contentPart.isOutputText() && captureContent()) {
                    ResponseOutputText textPart = contentPart.asOutputText();
                    sb.append("{\"type\":\"text\",\"content\":")
                        .append(GenAiMessageFormatter.jsonEscape(textPart.text()))
                        .append("}");
                } else {
                    sb.append("{\"type\":\"text\"}");
                }
            }
        }

        sb.append("],\"finish_reason\":\"completed\"}");
        return sb.toString();
    }

    /**
     * Extracts the model name string from a {@code ResponsesModel} union type.
     */
    static String extractModelString(com.openai.models.ResponsesModel model) {
        if (model == null) {
            return null;
        }
        if (model.isString()) {
            return model.asString();
        }
        if (model.isChat()) {
            return model.asChat().asString();
        }
        if (model.isOnly()) {
            return model.asOnly().asString();
        }
        return model.toString();
    }

    private ResponseSpanParams extractParams(AzureCreateResponseOptions createResponse,
        ResponseCreateParams builtParams) {
        String model = builtParams.model().isPresent() ? extractModelString(builtParams.model().get()) : null;
        AgentReference agentRef = createResponse.getAgentReference();
        String agentName = agentRef != null ? agentRef.getName() : null;
        boolean isInvokeAgent = agentName != null;
        String nameForSpan = isInvokeAgent ? agentName : model;
        String inputMessages = extractInputMessages(builtParams);
        String instructions = builtParams.instructions().orElse("");
        String conversationId = builtParams.conversation().isPresent() ? builtParams.conversation().get().asId() : null;
        return new ResponseSpanParams(isInvokeAgent, nameForSpan, agentName, model, inputMessages, instructions,
            conversationId);
    }

    private GenAiTracingScope startResponseScope(ResponseSpanParams params) {
        GenAiTracingScope scope = params.isInvokeAgent
            ? instrumentation.startInvokeAgent(params.nameForSpan)
            : instrumentation.startChat(params.nameForSpan);
        if (scope == null) {
            return null;
        }
        if (params.agentName != null) {
            scope.setAgentAttributes(null, params.agentName, null, null);
        }
        scope.setRequestModelAttributes(params.model, null, null);
        scope.setInputMessages(params.inputMessages);
        if (!params.isInvokeAgent && params.instructions != null && captureContent()) {
            scope.setSystemInstructions(params.instructions);
        }
        if (params.conversationId != null) {
            scope.setConversationId(params.conversationId);
        }
        return scope;
    }

    private static final class ResponseSpanParams {
        private final boolean isInvokeAgent;
        private final String nameForSpan;
        private final String agentName;
        private final String model;
        private final String inputMessages;
        private final String instructions;
        private final String conversationId;

        ResponseSpanParams(boolean isInvokeAgent, String nameForSpan, String agentName, String model,
            String inputMessages, String instructions, String conversationId) {
            this.isInvokeAgent = isInvokeAgent;
            this.nameForSpan = nameForSpan;
            this.agentName = agentName;
            this.model = model;
            this.inputMessages = inputMessages;
            this.instructions = instructions;
            this.conversationId = conversationId;
        }
    }

    private static final class StreamingState {
        private final GenAiTracingScope scope;
        private final boolean isInvokeAgent;
        private final ResponseAccumulator accumulator;
        private volatile boolean finalized;

        StreamingState(GenAiTracingScope scope, boolean isInvokeAgent) {
            this.scope = scope;
            this.isInvokeAgent = isInvokeAgent;
            this.accumulator = ResponseAccumulator.create();
        }

        void accumulate(ResponseStreamEvent event) {
            accumulator.accumulate(event);
        }

        void finalizeStream(GenAiResponseTracing responseTracing) {
            if (finalized) {
                return;
            }
            finalized = true;
            Response response = accumulator.response();
            if (response != null) {
                responseTracing.recordResponseAttributes(scope, response, isInvokeAgent);
            }
            scope.close();
        }

        void recordError(Throwable throwable) {
            if (finalized) {
                return;
            }
            finalized = true;
            scope.recordError(throwable);
            scope.close();
        }

        void close() {
            if (finalized) {
                return;
            }
            finalized = true;
            scope.close();
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }
}
