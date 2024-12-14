// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference;

import com.azure.ai.inference.implementation.ChatCompletionClientTracer;
import com.azure.ai.inference.implementation.models.CompleteRequest;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatCompletionsToolCall;
import com.azure.ai.inference.models.ChatRequestAssistantMessage;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.data.EventData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ChatCompletionClientTracerTest {
    private static final String MODEL_ENDPOINT_HOST = "contoso.openai.azure.com";
    private static final String MODEL_ENDPOINT = "https://" + MODEL_ENDPOINT_HOST;
    private static final String INFERENCE_GEN_AI_SYSTEM_NAME = "az.ai.inference";
    private static final String GEN_AI_REQUEST_CHAT_MODEL = "chat";
    private static final String GEN_AI_CHAT_OPERATION_NAME = "chat";
    private static final String AZ_NAMESPACE_NAME = "Microsoft.CognitiveServices";

    private static final AttributeKey<String> AZ_NAMESPACE = AttributeKey.stringKey("az.namespace");
    private static final AttributeKey<String> GEN_AI_SYSTEM = AttributeKey.stringKey("gen_ai.system");
    private static final AttributeKey<String> GEN_AI_OPERATION_NAME = AttributeKey.stringKey("gen_ai.operation.name");
    private static final AttributeKey<Double> GEN_AI_REQUEST_TOP_P = AttributeKey.doubleKey("gen_ai.request.top_p");
    private static final AttributeKey<String> GEN_AI_REQUEST_MODEL = AttributeKey.stringKey("gen_ai.request.model");
    private static final AttributeKey<Long> GEN_AI_REQUEST_MAX_TOKENS
        = AttributeKey.longKey("gen_ai.request.max_tokens");
    private static final AttributeKey<Double> GEN_AI_REQUEST_TEMPERATURE
        = AttributeKey.doubleKey("gen_ai.request.temperature");
    private static final AttributeKey<String> GEN_AI_RESPONSE_ID = AttributeKey.stringKey("gen_ai.response.id");
    private static final AttributeKey<String> GEN_AI_RESPONSE_MODEL = AttributeKey.stringKey("gen_ai.response.model");
    private static final AttributeKey<String> GEN_AI_RESPONSE_FINISH_REASONS
        = AttributeKey.stringKey("gen_ai.response.finish_reasons");
    private static final AttributeKey<Long> GEN_AI_USAGE_OUTPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.output_tokens");
    private static final AttributeKey<Long> GEN_AI_USAGE_INPUT_TOKENS
        = AttributeKey.longKey("gen_ai.usage.input_tokens");
    private static final AttributeKey<String> GEN_AI_EVENT_CONTENT = AttributeKey.stringKey("gen_ai.event.content");

    private static final String GEN_AI_CHOICE_EVENT_NAME = "gen_ai.choice";
    private static final String GEN_AI_SYSTEM_MESSAGE_EVENT_NAME = "gen_ai.system.message";
    private static final String GEN_AI_USER_MESSAGE_EVENT_NAME = "gen_ai.user.message";
    private static final String GEN_AI_ASSISTANT_MESSAGE_EVENT_NAME = "gen_ai.assistant.message";
    private static final String GEN_AI_TOOL_MESSAGE_EVENT_NAME = "gen_ai.tool.message";

    private static final String SYSTEM_MESSAGE = "You are a helpful assistant.";
    private static final String USER_MESSAGE = "What is the weather in Seattle?";
    private static final String SYSTEM_EVENT_CONTENT
        = String.format("{\"content\":\"%s\",\"role\":\"system\"}", SYSTEM_MESSAGE);
    private static final String USER_MESSAGE_CONTENT
        = String.format("{\"content\":\"%s\",\"role\":\"user\"}", USER_MESSAGE);

    private ClientOptions clientOptions;
    private TestSpanProcessor spanProcessor;
    private Tracer tracer;

    @BeforeEach
    public void setup() {
        spanProcessor = new TestSpanProcessor(MODEL_ENDPOINT_HOST);
        final OpenTelemetryTracingOptions tracingOptions
            = new OpenTelemetryTracingOptions().setOpenTelemetry(OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder().addSpanProcessor(spanProcessor).build())
                .build());
        tracer = TracerProvider.getDefaultProvider()
            .createTracer("test", null, "Microsoft.CognitiveServices", tracingOptions);
    }

    @AfterEach
    public void teardown() {
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldTraceSyncChatComplete(boolean captureContent) {
        final ChatCompletionClientTracer inferenceTracer
            = new ChatCompletionClientTracer(MODEL_ENDPOINT, configuration(captureContent), tracer);

        final List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestSystemMessage(SYSTEM_MESSAGE));
        messages.add(new ChatRequestUserMessage(USER_MESSAGE));
        final ChatCompletionsOptions completionsOptions
            = new ChatCompletionsOptions(messages).setTopP(5.0).setMaxTokens(100).setTemperature(75.4);
        final ChatCompletions toolCallsResponse = getChatCompletionsModelResponse(true);

        inferenceTracer.traceSyncComplete(completionsOptions, (arg0, arg1) -> toolCallsResponse,
            toCompleteRequest(completionsOptions), new RequestOptions());

        final List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        final ReadableSpan chatSpan = getChatSpan(spans);

        final Attributes chatAttributes = chatSpan.getAttributes();
        assertChatSpanRequestAttributes(chatAttributes, completionsOptions);
        if (captureContent) {
            assertChatEventContent(chatSpan, GEN_AI_SYSTEM_MESSAGE_EVENT_NAME, SYSTEM_EVENT_CONTENT);
            assertChatEventContent(chatSpan, GEN_AI_USER_MESSAGE_EVENT_NAME, USER_MESSAGE_CONTENT);
        } else {
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_SYSTEM_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_USER_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_ASSISTANT_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_TOOL_MESSAGE_EVENT_NAME).isPresent());
        }
        assertChatSpanResponseAttributes(chatAttributes, toolCallsResponse);
        assertEquals("[tool_calls]", chatAttributes.get(GEN_AI_RESPONSE_FINISH_REASONS));
        assertChatEventContent(chatSpan, GEN_AI_CHOICE_EVENT_NAME, getExpectedChoiceEventContent(true, captureContent));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void shouldTraceChatComplete(boolean captureContent) {
        final ChatCompletionClientTracer inferenceTracer
            = new ChatCompletionClientTracer(MODEL_ENDPOINT, configuration(captureContent), tracer);

        final List<ChatRequestMessage> messages = new ArrayList<>();
        messages.add(new ChatRequestSystemMessage(SYSTEM_MESSAGE));
        messages.add(new ChatRequestUserMessage(USER_MESSAGE));
        messages.add(new ChatRequestAssistantMessage("").setToolCalls(getModelToolCalls()));
        final ChatCompletionsOptions completionsOptions
            = new ChatCompletionsOptions(messages).setTopP(5.0).setMaxTokens(100).setTemperature(75.4);
        final ChatCompletions modelResponse = getChatCompletionsModelResponse(false);

        final Mono<ChatCompletions> r = inferenceTracer.traceComplete(completionsOptions,
            (arg0, arg1) -> Mono.just(modelResponse), toCompleteRequest(completionsOptions), new RequestOptions());

        StepVerifier.create(r).expectNextCount(1).verifyComplete();

        final List<ReadableSpan> spans = spanProcessor.getEndedSpans();
        final ReadableSpan chatSpan = getChatSpan(spans);

        final Attributes chatAttributes = chatSpan.getAttributes();
        assertChatSpanRequestAttributes(chatAttributes, completionsOptions);
        if (captureContent) {
            assertChatEventContent(chatSpan, GEN_AI_SYSTEM_MESSAGE_EVENT_NAME, SYSTEM_EVENT_CONTENT);
            assertChatEventContent(chatSpan, GEN_AI_USER_MESSAGE_EVENT_NAME, USER_MESSAGE_CONTENT);
            assertChatEventContent(chatSpan, GEN_AI_ASSISTANT_MESSAGE_EVENT_NAME, getExpectedAssistantMessageContent());
        } else {
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_SYSTEM_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_USER_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_ASSISTANT_MESSAGE_EVENT_NAME).isPresent());
            Assertions.assertFalse(getChatEvent(chatSpan, GEN_AI_TOOL_MESSAGE_EVENT_NAME).isPresent());
        }
        assertChatSpanResponseAttributes(chatAttributes, modelResponse);
        assertEquals("[stop]", chatAttributes.get(GEN_AI_RESPONSE_FINISH_REASONS));
        assertChatEventContent(chatSpan, GEN_AI_CHOICE_EVENT_NAME,
            getExpectedChoiceEventContent(false, captureContent));
    }

    private static ReadableSpan getChatSpan(List<ReadableSpan> spans) {
        Assertions.assertFalse(spans.isEmpty());
        final Optional<ReadableSpan> chatSpan = spans.stream().filter(s -> s.getName().equals("chat")).findFirst();
        Assertions.assertTrue(chatSpan.isPresent());
        return chatSpan.get();
    }

    private static Optional<EventData> getChatEvent(ReadableSpan span, String eventName) {
        Assertions.assertEquals("chat", span.getName());
        final List<EventData> events = span.toSpanData().getEvents();
        Assertions.assertFalse(events.isEmpty());
        return events.stream().filter(s -> s.getName().equals(eventName)).findFirst();
    }

    private static void assertChatSpanRequestAttributes(Attributes chatAttributes,
        ChatCompletionsOptions completionsOptions) {
        assertEquals(AZ_NAMESPACE_NAME, chatAttributes.get(AZ_NAMESPACE));
        assertEquals(INFERENCE_GEN_AI_SYSTEM_NAME, chatAttributes.get(GEN_AI_SYSTEM));
        assertEquals(GEN_AI_CHAT_OPERATION_NAME, chatAttributes.get(GEN_AI_OPERATION_NAME));
        assertEquals(GEN_AI_REQUEST_CHAT_MODEL, chatAttributes.get(GEN_AI_REQUEST_MODEL));
        assertEquals(completionsOptions.getTopP(), chatAttributes.get(GEN_AI_REQUEST_TOP_P));
        assertEquals(completionsOptions.getMaxTokens().longValue(), chatAttributes.get(GEN_AI_REQUEST_MAX_TOKENS));
        assertEquals(completionsOptions.getTemperature(), chatAttributes.get(GEN_AI_REQUEST_TEMPERATURE));
    }

    private static void assertChatSpanResponseAttributes(Attributes chatAttributes, ChatCompletions modelResponse) {
        assertEquals(modelResponse.getId(), chatAttributes.get(GEN_AI_RESPONSE_ID));
        assertEquals(modelResponse.getModel(), chatAttributes.get(GEN_AI_RESPONSE_MODEL));
        assertEquals(modelResponse.getUsage().getCompletionTokens(), chatAttributes.get(GEN_AI_USAGE_OUTPUT_TOKENS));
        assertEquals(modelResponse.getUsage().getPromptTokens(), chatAttributes.get(GEN_AI_USAGE_INPUT_TOKENS));
    }

    private static void assertChatEventContent(ReadableSpan span, String eventName, String expectedContent) {
        final Optional<EventData> systemMessageEvent = getChatEvent(span, eventName);
        Assertions.assertTrue(systemMessageEvent.isPresent());
        final Attributes eventAttributes = systemMessageEvent.get().getAttributes();
        Assertions.assertEquals(expectedContent, eventAttributes.get(GEN_AI_EVENT_CONTENT));
        assertEquals(INFERENCE_GEN_AI_SYSTEM_NAME, eventAttributes.get(GEN_AI_SYSTEM));
    }

    private static BinaryData toCompleteRequest(ChatCompletionsOptions options) {
        final CompleteRequest completeRequest
            = new CompleteRequest(options.getMessages()).setFrequencyPenalty(options.getFrequencyPenalty())
                .setStream(options.isStream())
                .setPresencePenalty(options.getPresencePenalty())
                .setTemperature(options.getTemperature())
                .setTopP(options.getTopP())
                .setMaxTokens(options.getMaxTokens())
                .setResponseFormat(options.getResponseFormat())
                .setStop(options.getStop())
                .setTools(options.getTools())
                .setToolChoice(options.getToolChoice())
                .setSeed(options.getSeed())
                .setModel(options.getModel());
        return BinaryData.fromObject(completeRequest);
    }

    private static ChatCompletions getChatCompletionsModelResponse(boolean isToolCalls) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            writer.writeStartObject();
            writer.writeStringField("id", "model_uuid_0");
            writer.writeStringField("model", "gpt-4-turbo-2024-04-09");
            writer.writeStartObject("usage");
            writer.writeLongField("completion_tokens", 14);
            writer.writeLongField("prompt_tokens", 115);
            writer.writeLongField("total_tokens", 129);
            writer.writeEndObject();
            writer.writeStartArray("choices");
            writer.writeStartObject();
            if (isToolCalls) {
                writer.writeStringField("finish_reason", "tool_calls");
                writer.writeLongField("index", 0);
                writer.writeStartObject("message");
                writer.writeStringField("role", "assistant");
                writer.writeStartArray("tool_calls");
                writer.writeStartObject();
                writer.writeStringField("id", "tool_call_uuid0");
                writer.writeStringField("type", "function");
                writer.writeStartObject("function");
                writer.writeStringField("name", "get_weather");
                writer.writeStringField("arguments", "{\"city\":\"Seattle\"}");
                writer.writeEndObject();
                writer.writeEndObject();
                writer.writeEndArray();
                writer.writeEndObject();
            } else {
                writer.writeStringField("finish_reason", "stop");
                writer.writeLongField("index", 0);
                writer.writeStartObject("message");
                writer.writeStringField("role", "assistant");
                writer.writeStringField("content", "The weather in Seattle is nice.");
                writer.writeEndObject();
            }
            writer.writeEndObject();
            writer.writeEndArray();
            writer.writeEndObject();
            writer.flush();
            final BinaryData binaryData
                = BinaryData.fromString(new String(stream.toByteArray(), StandardCharsets.UTF_8));
            return binaryData.toObject(ChatCompletions.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String getExpectedChoiceEventContent(boolean toolCalls, boolean captureContent) {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            writer.writeStartObject();
            if (toolCalls) {
                writer.writeStartObject("message");
                writer.writeStartArray("tool_calls");
                writer.writeStartObject();
                writer.writeStringField("id", "tool_call_uuid0");
                writer.writeStringField("type", "function");
                if (captureContent) {
                    writer.writeStartObject("function");
                    writer.writeStringField("name", "get_weather");
                    writer.writeStringField("arguments", "{\"city\":\"Seattle\"}");
                    writer.writeEndObject();
                }
                writer.writeEndObject();
                writer.writeEndArray();
                writer.writeEndObject();
                writer.writeStringField("finish_reason", "tool_calls");
                writer.writeLongField("index", 0);
            } else {
                writer.writeStartObject("message");
                if (captureContent) {
                    writer.writeStringField("content", "The weather in Seattle is nice.");
                }
                writer.writeEndObject();
                writer.writeStringField("finish_reason", "stop");
                writer.writeLongField("index", 0);
            }
            writer.writeEndObject();
            writer.flush();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String getExpectedAssistantMessageContent() {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(stream)) {
            writer.writeStartObject();
            writer.writeStringField("role", "assistant");
            writer.writeStringField("content", "");
            writer.writeStartArray("tool_calls");
            writer.writeStartObject();
            writer.writeStringField("id", "tool_call_uuid0");
            writer.writeStringField("type", "function");
            writer.writeStartObject("function");
            writer.writeStringField("name", "get_weather");
            writer.writeStringField("arguments", "{\"city\":\"Seattle\"}");
            writer.writeEndObject();
            writer.writeEndObject();
            writer.writeEndArray();
            writer.writeEndObject();
            writer.flush();
            return new String(stream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static List<ChatCompletionsToolCall> getModelToolCalls() {
        final ChatCompletions toolCallsResponse = getChatCompletionsModelResponse(true);
        return toolCallsResponse.getChoice().getMessage().getToolCalls();
    }

    private static Configuration configuration(boolean captureContent) {
        if (captureContent) {
            return new com.azure.core.util.ConfigurationBuilder()
                .putProperty("azure.tracing.gen_ai.content_recording_enabled", "true")
                .build();
        } else {
            return new com.azure.core.util.ConfigurationBuilder().build();
        }
    }

    private static final class TestSpanProcessor implements SpanProcessor {
        private static final AttributeKey<String> NET_PEER_NAME = AttributeKey.stringKey("net.peer.name");
        private static final AttributeKey<String> SERVER_ADDRESS = AttributeKey.stringKey("server.address");

        private final ClientLogger logger;
        private final String modelEndpointHost;
        private final ConcurrentLinkedDeque<ReadableSpan> spans = new ConcurrentLinkedDeque<>();

        TestSpanProcessor(String modelEndpointHost) {
            this.logger = new ClientLogger(TestSpanProcessor.class);
            this.modelEndpointHost = modelEndpointHost;
        }

        public List<ReadableSpan> getEndedSpans() {
            return spans.stream().collect(Collectors.toList());
        }

        @Override
        public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        }

        @Override
        public boolean isStartRequired() {
            return false;
        }

        @Override
        public void onEnd(ReadableSpan readableSpan) {
            logger.info(readableSpan.toString());
            assertEquals(modelEndpointHost, getEndpoint(readableSpan));
            spans.add(readableSpan);
        }

        @Override
        public boolean isEndRequired() {
            return true;
        }

        private String getEndpoint(ReadableSpan readableSpan) {
            // Depending on the OpenTelemetry version being used, the attribute name for the peer name may be different.
            // The attribute name was changed from "net.peer.name" to "server.address".
            final String endpoint = readableSpan.getAttribute(NET_PEER_NAME);
            if (endpoint != null) {
                return endpoint;
            }
            return readableSpan.getAttribute(SERVER_ADDRESS);
        }
    }
}
