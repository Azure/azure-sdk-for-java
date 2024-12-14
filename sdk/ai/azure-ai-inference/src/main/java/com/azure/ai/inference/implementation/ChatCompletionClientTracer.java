// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.implementation;

import com.azure.ai.inference.models.ChatChoice;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatCompletionsToolCall;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRole;
import com.azure.ai.inference.models.CompletionsFinishReason;
import com.azure.ai.inference.models.CompletionsUsage;
import com.azure.ai.inference.models.StreamingChatChoiceUpdate;
import com.azure.ai.inference.models.StreamingChatCompletionsUpdate;
import com.azure.ai.inference.models.StreamingChatResponseMessageUpdate;
import com.azure.ai.inference.models.StreamingChatResponseToolCallUpdate;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tracing for the convenience methods in {@link com.azure.ai.inference.ChatCompletionsClient} and
 * {@link com.azure.ai.inference.ChatCompletionsAsyncClient}.
 * <p>
 * For more about the OTel semantic conventions this type enables, see
 * <a href="https://github.com/open-telemetry/semantic-conventions/blob/v1.29.0/docs/gen-ai/azure-ai-inference.md">Azure AI Inference semantic conventions</a>.
 * </p>
 */
public final class ChatCompletionClientTracer {
    public static final String OTEL_SCHEMA_URL = "https://opentelemetry.io/schemas/1.29.0";

    private static final ClientLogger LOGGER = new ClientLogger(ChatCompletionClientTracer.class);
    private static final String INFERENCE_GEN_AI_SYSTEM_NAME = "az.ai.inference";
    private static final String FINISH_REASON_ERROR = "{\"finish_reason\": \"error\"}";
    private static final String FINISH_REASON_CANCELLED = "{\"finish_reason\": \"cancelled\"}";
    private static final StartSpanOptions START_SPAN_OPTIONS = new StartSpanOptions(SpanKind.CLIENT);

    private static final ConfigurationProperty<Boolean> CAPTURE_MESSAGE_CONTENT
        = ConfigurationPropertyBuilder.ofBoolean("azure.tracing.gen_ai.content_recording_enabled")
            .environmentVariableName("AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED")
            .systemPropertyName("azure.tracing.gen_ai.content_recording_enabled")
            .shared(true)
            .defaultValue(false)
            .build();
    private static final Configuration GLOBAL_CONFIG = Configuration.getGlobalConfiguration();

    private final String host;
    private final int port;
    private final boolean captureContent;
    private final Tracer tracer;

    //<editor-fold desc="operation contracts">
    /**
     * Reference to the operation performing the actual completion call.
     */
    @FunctionalInterface
    public interface SyncCompleteOperation {
        /**
         * invokes the operation.
         *
         * @param completeRequest The completeRequest parameter for the {@code operation}.
         * @param requestOptions The requestOptions parameter for the {@code operation}.
         * @return chat completions for the provided chat messages.
         */
        ChatCompletions invoke(BinaryData completeRequest, RequestOptions requestOptions);
    }

    /**
     * Reference to the async operation performing the actual completion call.
     */
    @FunctionalInterface
    public interface CompleteOperation {
        /**
         * invokes the operation.
         *
         * @param completeRequest The completeRequest parameter for the {@code operation}.
         * @param requestOptions The requestOptions parameter for the {@code operation}.
         * @return chat completions for the provided chat messages.
         */
        Mono<ChatCompletions> invoke(BinaryData completeRequest, RequestOptions requestOptions);
    }

    /**
     * Reference to the async operation performing the actual completion streaming call.
     */
    @FunctionalInterface
    public interface StreamingCompleteOperation {
        /**
         * invokes the operation.
         *
         * @param completeRequest The completeRequest parameter for the {@code operation}.
         * @param requestOptions The requestOptions parameter for the {@code operation}.
         * @return chat completions streaming for the provided chat messages.
         */
        Flux<StreamingChatCompletionsUpdate> invoke(BinaryData completeRequest, RequestOptions requestOptions);
    }
    //</editor-fold>

    /**
     * Creates ChatCompletionClientTracer.
     *
     * @param endpoint the service endpoint.
     * @param configuration the {@link Configuration} instance to check if message content needs to be captured,
     *     if {@code null} is passed then {@link Configuration#getGlobalConfiguration()} will be used.
     * @param tracer the Tracer instance.
     */
    public ChatCompletionClientTracer(String endpoint, Configuration configuration, Tracer tracer) {
        final URL url = parse(endpoint);
        if (url != null) {
            this.host = url.getHost();
            this.port = url.getPort() == -1 ? url.getDefaultPort() : url.getPort();
        } else {
            this.host = null;
            this.port = -1;
        }
        this.captureContent = configuration == null
            ? GLOBAL_CONFIG.get(CAPTURE_MESSAGE_CONTENT)
            : configuration.get(CAPTURE_MESSAGE_CONTENT);
        this.tracer = tracer;
    }

    /**
     * Traces the synchronous convenience API - {@link com.azure.ai.inference.ChatCompletionsClient#complete(ChatCompletionsOptions)}.
     *
     * @param request input options containing chat options for complete API.
     * @param operation the operation performing the actual completion call.
     * @param completeRequest The completeRequest parameter for the {@code operation}.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return chat completions for the provided chat messages.
     */
    @SuppressWarnings("try")
    public ChatCompletions traceSyncComplete(ChatCompletionsOptions request, SyncCompleteOperation operation,
        BinaryData completeRequest, RequestOptions requestOptions) {
        if (!tracer.isEnabled()) {
            return operation.invoke(completeRequest, requestOptions);
        }
        final Context span = tracer.start(spanName(request), START_SPAN_OPTIONS, parentSpan(requestOptions));
        if (tracer.isRecording(span)) {
            traceCompletionRequestAttributes(request, span);
            traceCompletionRequestEvents(request.getMessages(), span);
        }

        try (AutoCloseable ignored = tracer.makeSpanCurrent(span)) {
            final ChatCompletions response = operation.invoke(completeRequest, requestOptions.setContext(span));
            if (tracer.isRecording(span)) {
                traceCompletionResponseAttributes(response, span);
                traceCompletionResponseEvents(response, span);
            }
            tracer.end(null, null, span);
            return response;
        } catch (Exception e) {
            tracer.end(null, e, span);
            sneakyThrows(e);
        }
        return null;
    }

    /**
     * Traces the convenience API - {@link com.azure.ai.inference.ChatCompletionsAsyncClient#complete(ChatCompletionsOptions)}.
     *
     * @param request input options containing chat options for complete API.
     * @param operation the operation performing the actual completion call.
     * @param completeRequest The completeRequest parameter for the {@code operation}.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return chat completions for the provided chat messages.
     */
    public Mono<ChatCompletions> traceComplete(ChatCompletionsOptions request, CompleteOperation operation,
        BinaryData completeRequest, RequestOptions requestOptions) {
        if (!tracer.isEnabled()) {
            return operation.invoke(completeRequest, requestOptions);
        }

        final Mono<Context> resourceSupplier = Mono.fromSupplier(() -> {
            final Context span = tracer.start(spanName(request), START_SPAN_OPTIONS, parentSpan(requestOptions));
            if (tracer.isRecording(span)) {
                traceCompletionRequestAttributes(request, span);
                traceCompletionRequestEvents(request.getMessages(), span);
            }
            return span;
        });

        final Function<Context, Mono<ChatCompletions>> resourceClosure = span -> {
            final RequestOptions rOptions = requestOptions.setContext(span);

            return operation.invoke(completeRequest, rOptions).map(response -> {
                if (tracer.isRecording(span)) {
                    traceCompletionResponseAttributes(response, span);
                    traceCompletionResponseEvents(response, span);
                }
                return response;
            });
        };

        final Function<Context, Mono<Void>> asyncComplete = (span) -> {
            tracer.end(null, null, span);
            return Mono.empty();
        };

        final BiFunction<Context, Throwable, Mono<Void>> asyncError = (span, throwable) -> {
            if (tracer.isRecording(span)) {
                traceChoiceEvent(FINISH_REASON_ERROR, span);
            }
            tracer.end(null, throwable, span);
            return Mono.empty();
        };

        final Function<Context, Mono<Void>> asyncCancel = span -> {
            if (tracer.isRecording(span)) {
                traceChoiceEvent(FINISH_REASON_CANCELLED, span);
            }
            tracer.end("cancelled", null, span);
            return Mono.empty();
        };

        return Mono.usingWhen(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel);
    }

    /**
     * Traces the convenience APIs - {@link com.azure.ai.inference.ChatCompletionsClient#completeStream(ChatCompletionsOptions)}
     * and {@link com.azure.ai.inference.ChatCompletionsAsyncClient#completeStream(ChatCompletionsOptions)}}.
     *
     * @param request input options containing chat options for complete streaming API.
     * @param operation the operation performing the actual streaming completion call.
     * @param completeRequest The completeRequest parameter for the {@code operation}.
     * @param requestOptions The requestOptions parameter for the {@code operation}.
     * @return chat completions streaming for the provided chat messages.
     */
    public Flux<StreamingChatCompletionsUpdate> traceStreamingCompletion(ChatCompletionsOptions request,
        StreamingCompleteOperation operation, BinaryData completeRequest, RequestOptions requestOptions) {
        if (!tracer.isEnabled()) {
            return operation.invoke(completeRequest, requestOptions);
        }
        final StreamingChatCompletionsState state
            = new StreamingChatCompletionsState(captureContent, request, operation, completeRequest, requestOptions);

        final Mono<StreamingChatCompletionsState> resourceSupplier = Mono.fromSupplier(() -> {
            final StreamingChatCompletionsState resource = state;

            final Context span
                = tracer.start(spanName(resource.request), START_SPAN_OPTIONS, parentSpan(resource.requestOptions));
            if (tracer.isRecording(span)) {
                traceCompletionRequestAttributes(resource.request, span);
                traceCompletionRequestEvents(resource.request.getMessages(), span);
            }
            return resource.setSpan(span);
        });

        final Function<StreamingChatCompletionsState, Flux<StreamingChatCompletionsUpdate>> resourceClosure
            = resource -> {
                final Context span = resource.span;

                final RequestOptions rOptions = resource.requestOptions.setContext(span);
                final Flux<StreamingChatCompletionsUpdate> completionChunks
                    = resource.operation.invoke(resource.completeRequest, rOptions);
                if (tracer.isRecording(span)) {
                    return completionChunks.doOnNext(resource::onNextChunk);
                } else {
                    return completionChunks;
                }
            };

        final Function<StreamingChatCompletionsState, Mono<Void>> asyncComplete = resource -> {
            final Context span = resource.span;
            if (tracer.isRecording(span)) {
                final StreamingChatCompletionsUpdate lastChunk = resource.lastChunk;
                final String finishReasons = resource.getFinishReasons();
                traceCompletionResponseAttributes(lastChunk, finishReasons, span);
                traceChoiceEvent(resource.toJson(), span);
            }
            tracer.end(null, null, span);
            return Mono.empty();
        };

        final BiFunction<StreamingChatCompletionsState, Throwable, Mono<Void>> asyncError = (resource, throwable) -> {
            final Context span = resource.span;

            if (tracer.isRecording(span)) {
                traceChoiceEvent(FINISH_REASON_ERROR, span);
            }
            tracer.end(null, throwable, span);
            return Mono.empty();
        };

        final Function<StreamingChatCompletionsState, Mono<Void>> asyncCancel = resource -> {
            final Context span = resource.span;

            if (tracer.isRecording(span)) {
                traceChoiceEvent(FINISH_REASON_CANCELLED, span);
            }
            tracer.end("cancelled", null, span);
            return Mono.empty();
        };

        return Flux.usingWhen(resourceSupplier, resourceClosure, asyncComplete, asyncError, asyncCancel);
    }

    //<editor-fold desc="Private util types, methods">
    private String spanName(ChatCompletionsOptions completeRequest) {
        return CoreUtils.isNullOrEmpty(completeRequest.getModel()) ? "chat" : "chat " + completeRequest.getModel();
    }

    private void traceCompletionRequestAttributes(ChatCompletionsOptions request, Context span) {
        final String modelId = request.getModel();
        tracer.setAttribute("gen_ai.operation.name", "chat", span);
        tracer.setAttribute("gen_ai.system", INFERENCE_GEN_AI_SYSTEM_NAME, span);
        tracer.setAttribute("gen_ai.request.model", CoreUtils.isNullOrEmpty(modelId) ? "chat" : modelId, span);
        if (request.getFrequencyPenalty() != null) {
            tracer.setAttribute("gen_ai.request.frequency_penalty", request.getFrequencyPenalty(), span);
        }
        if (request.getMaxTokens() != null) {
            tracer.setAttribute("gen_ai.request.max_tokens", request.getMaxTokens(), span);
        }
        if (request.getPresencePenalty() != null) {
            tracer.setAttribute("gen_ai.request.presence_penalty", request.getPresencePenalty(), span);
        }
        if (request.getStop() != null) {
            final StringJoiner stopSequence = new StringJoiner(",", "[", "]");
            for (String stop : request.getStop()) {
                stopSequence.add(stop);
            }
            tracer.setAttribute("gen_ai.request.stop_sequences", stopSequence.toString(), span);
        }
        if (request.getTemperature() != null) {
            tracer.setAttribute("gen_ai.request.temperature", request.getTemperature(), span);
        }
        if (request.getTopP() != null) {
            tracer.setAttribute("gen_ai.request.top_p", request.getTopP(), span);
        }
        if (host != null) {
            tracer.setAttribute("server.address", host, span);
            if (port != 443) {
                tracer.setAttribute("server.port", port, span);
            }
        }
    }

    private void traceCompletionRequestEvents(List<ChatRequestMessage> messages, Context span) {
        if (!captureContent || messages == null) {
            return;
        }
        for (ChatRequestMessage message : messages) {
            final ChatRole role = message.getRole();
            if (role != null) {
                final String eventName = "gen_ai." + role.getValue() + ".message";
                final String eventContent = toJsonString(message);
                if (eventContent != null) {
                    final Map<String, Object> eventAttributes = new HashMap<>(2);
                    eventAttributes.put("gen_ai.system", INFERENCE_GEN_AI_SYSTEM_NAME);
                    eventAttributes.put("gen_ai.event.content", eventContent);
                    tracer.addEvent(eventName, eventAttributes, null, span);
                }
            }
        }
    }

    private void traceCompletionResponseAttributes(ChatCompletions response, Context span) {
        tracer.setAttribute("gen_ai.response.id", response.getId(), span);
        tracer.setAttribute("gen_ai.response.model", response.getModel(), span);
        final CompletionsUsage usage = response.getUsage();
        if (usage != null) {
            tracer.setAttribute("gen_ai.usage.input_tokens", usage.getPromptTokens(), span);
            tracer.setAttribute("gen_ai.usage.output_tokens", usage.getCompletionTokens(), span);
        }
        final List<ChatChoice> choices = response.getChoices();
        if (choices != null) {
            tracer.setAttribute("gen_ai.response.finish_reasons", getFinishReasons(choices), span);
        }
    }

    private void traceCompletionResponseAttributes(StreamingChatCompletionsUpdate response, String finishReasons,
        Context span) {
        tracer.setAttribute("gen_ai.response.id", response.getId(), span);
        tracer.setAttribute("gen_ai.response.model", response.getModel(), span);
        final CompletionsUsage usage = response.getUsage();
        if (usage != null) {
            tracer.setAttribute("gen_ai.usage.input_tokens", usage.getPromptTokens(), span);
            tracer.setAttribute("gen_ai.usage.output_tokens", usage.getCompletionTokens(), span);
        }
        tracer.setAttribute("gen_ai.response.finish_reasons", finishReasons, span);
    }

    private void traceCompletionResponseEvents(ChatCompletions response, Context span) {
        final List<ChatChoice> choices = response.getChoices();
        if (choices != null) {
            for (ChatChoice choice : choices) {
                traceChoiceEvent(toJsonString(choice), span);
            }
        }
    }

    private void traceChoiceEvent(String choiceContent, Context span) {
        final Map<String, Object> eventAttributes = new HashMap<>(2);
        eventAttributes.put("gen_ai.system", INFERENCE_GEN_AI_SYSTEM_NAME);
        eventAttributes.put("gen_ai.event.content", choiceContent);
        tracer.addEvent("gen_ai.choice", eventAttributes, null, span);
    }

    private String toJsonString(ChatRequestMessage message) {
        try {
            return message.toJsonString();
        } catch (IOException e) {
            LOGGER.verbose("'ChatRequestMessage' serialization error", e);
        }
        return null;
    }

    private String toJsonString(ChatChoice choice) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            writer.writeStartObject();
            writer.writeStartObject("message");
            if (captureContent) {
                writer.writeStringField("content", choice.getMessage().getContent());
            }
            if (choice.getMessage() != null) {
                final List<ChatCompletionsToolCall> toolCalls = choice.getMessage().getToolCalls();
                if (toolCalls != null && !toolCalls.isEmpty()) {
                    writer.writeArrayField("tool_calls", toolCalls, (w, toolCall) -> {
                        if (captureContent) {
                            toolCall.toJson(w);
                        } else {
                            w.writeStartObject();
                            w.writeStringField("id", toolCall.getId());
                            w.writeStringField("type", toolCall.getType());
                            w.writeEndObject();
                        }
                    });
                }
            }
            writer.writeEndObject();
            final CompletionsFinishReason finishReason = choice.getFinishReason();
            if (finishReason != null) {
                writer.writeStringField("finish_reason", finishReason.getValue());
            }
            writer.writeIntField("index", choice.getIndex());
            writer.writeEndObject();
            writer.flush();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOGGER.verbose("'ChatChoice' serialization error", e);
        }
        return null;
    }

    private static String getFinishReasons(List<ChatChoice> choices) {
        final StringJoiner finishReasons = new StringJoiner(",", "[", "]");
        for (ChatChoice choice : choices) {
            final CompletionsFinishReason finishReason = choice.getFinishReason();
            if (finishReason != null) {
                finishReasons.add(finishReason.getValue());
            }
        }
        return finishReasons.toString();
    }

    private static URL parse(String endpoint) {
        if (CoreUtils.isNullOrEmpty(endpoint)) {
            return null;
        }
        try {
            final URI uri = new URI(endpoint);
            return uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.atWarning().log("service endpoint uri parse error.", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrows(Throwable e) throws E {
        throw (E) e;
    }

    private static Context parentSpan(RequestOptions requestOptions) {
        return requestOptions.getContext() == null ? Context.NONE : requestOptions.getContext();
    }

    private static final class StreamingChatCompletionsState {
        private final boolean captureContent;
        private final ChatCompletionsOptions request;
        private final StreamingCompleteOperation operation;
        private final BinaryData completeRequest;
        private final RequestOptions requestOptions;
        // mutable part of the state to accumulate partial data from Completion chunks.
        private final StringBuilder content;
        private final ArrayDeque<StreamingChatResponseToolCallUpdate> toolCalls; // uses Dequeue to release slots once consumed.
        private final ArrayDeque<String> toolCallIds;
        private final ArrayDeque<CompletionsFinishReason> finishReasons;
        private Context span;
        private StreamingChatCompletionsUpdate lastChunk;
        private CompletionsFinishReason finishReason;
        private int index;

        StreamingChatCompletionsState(boolean captureContent, ChatCompletionsOptions request,
            StreamingCompleteOperation operation, BinaryData completeRequest, RequestOptions requestOptions) {
            this.captureContent = captureContent;
            this.request = request;
            this.operation = operation;
            this.completeRequest = completeRequest;
            this.requestOptions = requestOptions;
            this.content = new StringBuilder();
            this.toolCalls = new ArrayDeque<>();
            this.toolCallIds = new ArrayDeque<>();
            this.finishReasons = new ArrayDeque<>();
        }

        StreamingChatCompletionsState setSpan(Context context) {
            this.span = context;
            return this;
        }

        void onNextChunk(StreamingChatCompletionsUpdate chunk) {
            this.lastChunk = chunk;
            final List<StreamingChatChoiceUpdate> choices = chunk.getChoices();
            if (choices == null || choices.isEmpty()) {
                return;
            }
            for (StreamingChatChoiceUpdate choice : choices) {
                this.finishReason = choice.getFinishReason();
                this.index = choice.getIndex();
                if (choice.getFinishReason() != null) {
                    this.finishReasons.add(choice.getFinishReason());
                }
                final StreamingChatResponseMessageUpdate delta = choice.getDelta();
                if (delta == null) {
                    continue;
                }
                final List<StreamingChatResponseToolCallUpdate> toolCalls = delta.getToolCalls();
                if (this.captureContent) {
                    if (delta.getContent() != null) {
                        this.content.append(delta.getContent());
                    }
                    if (toolCalls != null) {
                        this.toolCalls.addAll(toolCalls);
                    }
                } else {
                    if (toolCalls != null) {
                        final List<String> ids = toolCalls.stream()
                            .map(StreamingChatResponseToolCallUpdate::getId)
                            .filter(s -> !CoreUtils.isNullOrEmpty(s))
                            .collect(Collectors.toList());
                        this.toolCallIds.addAll(ids);
                    }
                }
            }
        }

        String toJson() {
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
                JsonWriter writer = JsonProviders.createWriter(stream)) {
                writer.writeStartObject();
                writer.writeStartObject("message");
                if (this.captureContent) {
                    writer.writeStringField("content", this.content.toString());
                    writer.writeStartArray("tool_calls");
                    StreamingChatResponseToolCallUpdate toolCall;
                    while ((toolCall = this.toolCalls.poll()) != null) {
                        toolCall.toJson(writer);
                    }
                    writer.writeEndArray();
                } else {
                    writer.writeStartArray("tool_calls");
                    String toolCallId;
                    while ((toolCallId = this.toolCallIds.poll()) != null) {
                        writer.writeStartObject();
                        writer.writeStringField("id", toolCallId);
                        writer.writeEndObject();
                    }
                    writer.writeEndArray();
                }
                writer.writeEndObject();
                if (this.finishReason != null) {
                    writer.writeStringField("finish_reason", this.finishReason.getValue());
                }
                writer.writeIntField("index", this.index);
                writer.writeEndObject();
                writer.flush();
                return new String(stream.toByteArray(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOGGER.verbose("'StreamingChatCompletionsState' serialization error", e);
            }
            return null;
        }

        String getFinishReasons() {
            final StringJoiner finishReasonsSj = new StringJoiner(",", "[", "]");
            CompletionsFinishReason reason;
            while ((reason = finishReasons.poll()) != null) {
                finishReasonsSj.add(reason.getValue());
            }
            return finishReasonsSj.toString();
        }
    }
    //</editor-fold>
}
