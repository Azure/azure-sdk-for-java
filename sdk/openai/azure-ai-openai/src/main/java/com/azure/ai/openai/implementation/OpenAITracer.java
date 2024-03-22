package com.azure.ai.openai.implementation;

import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationProperty;
import com.azure.core.util.ConfigurationPropertyBuilder;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.stream.Collectors;

public class OpenAITracer {
    private static final ClientLogger LOGGER = new ClientLogger(OpenAITracer.class);

    private static final ConfigurationProperty<Boolean> ENABLE_EVENT_COLLECTION_PROPERTY = ConfigurationPropertyBuilder
        .ofBoolean("openai.distributed_tracing.event_collection_enabled")
        .defaultValue(false)
        .environmentVariableName("AZURE_OPENAI_DISTRIBUTED_TRACING_EVENT_COLLECTION_ENABLED")
        .build();

    private final Tracer tracer;
    private final String serverAddress;
    private final int serverPort;
    private final boolean isEventCollectionEnabled;
    private final SerializerAdapter serializer;
    public OpenAITracer(Tracer tracer, String endpoint, Configuration configuration, SerializerAdapter serializer) {
        this.tracer = tracer;
        // TODO optimize
        URI uri = URI.create(endpoint);
        this.serverAddress = uri.getHost();
        this.serverPort = uri.getPort() > 0 ? uri.getPort() : 443;
        if (configuration == null) {
            configuration = Configuration.getGlobalConfiguration();
        }

        this.isEventCollectionEnabled = configuration.get(ENABLE_EVENT_COLLECTION_PROPERTY);
        // TODO: get rid of it
        this.serializer = serializer;
    }

    public Context startChatCompletionsSpan(String modelName, BinaryData chatCompletionOptions, Context parent) {
        if (!tracer.isEnabled()) {
            return parent;
        }

        if (parent == null) {
            parent = Context.NONE;
        }

        // TODO spec: define sampling-relevant attributes
        // TODO Java tracing API facade: add IsSampled API
        // TODO: teach core to suppress client->internal spans
        StartSpanOptions options = new StartSpanOptions(SpanKind.INTERNAL)
            .setAttribute("server.address", serverAddress)
            .setAttribute("server.port", serverPort)
            .setAttribute("gen_ai.request.model", modelName)
            .setAttribute("gen_ai.system", "openai");

        // TODO: standard operation names. openai has object
        Context span = tracer.start("chat_completion " + modelName, options, parent);

        // TODO: SerializableContent.toObject optimization - should return underlying object if the type is the same
        ChatCompletionsOptions completionsOptions = chatCompletionOptions.toObject(ChatCompletionsOptions.class);
        if (completionsOptions.getMaxTokens() != null) {
            tracer.setAttribute("gen_ai.request.max_tokens", completionsOptions.getMaxTokens(), span);
        }

        // TODO Java tracing API facade: add setAttribute(String, Double, Context)
        if (completionsOptions.getTemperature() != null) {
            tracer.setAttribute("gen_ai.request.temperature", completionsOptions.getTemperature().toString(), span);
        }

        if (completionsOptions.getTopP() != null) {
            tracer.setAttribute("gen_ai.request.top_p", completionsOptions.getTopP().toString(), span);
        }

        recordPrompt(completionsOptions, span);
        return span;
    }

    public void setResponse(BinaryData response, Context span) {
        if (!tracer.isEnabled()) {
            return;
        }

        ChatCompletions completions = response.toObject(ChatCompletions.class);
        tracer.setAttribute("gen_ai.response.id", completions.getId(), span);
        // TODO Azure OpenAI or spec: response model is not exposed
        // tracer.setAttribute("gen_ai.response.model", ?, span);
        // TODO Java tracing API: add setAttribute(String, Object, Context) or List<String> overload
        tracer.setAttribute("gen_ai.response.finish_reasons", completions.getChoices().stream().map(c -> c.getFinishReason().toString())
            .collect(Collectors.joining(", ")), span);

        tracer.setAttribute("gen_ai.usage.completion_tokens", completions.getUsage().getCompletionTokens(), span);
        tracer.setAttribute("gen_ai.usage.prompt_tokens", completions.getUsage().getPromptTokens(), span);

        recordCompletion(completions, span);
    }

    public void endSpan(AutoCloseable scope, Throwable throwable, Context span) {
        if (scope != null) {
            try {
                scope.close();
            } catch (Exception e) {
                LOGGER.verbose("Failed to close scope", e);
            }
        }
        tracer.end(null, throwable, span);
    }

    public AutoCloseable makeSpanCurrent(Context span) {
        return tracer.makeSpanCurrent(span);
    }

    private void recordPrompt(ChatCompletionsOptions options, Context span) {
        if (!isEventCollectionEnabled) {
            return;
        }

        try {
            // TODO optimize serialization
            // TODO otel: we should eventually be able to pass it as a structured event payload
            String eventBody = serializer.serialize(options.getMessages(), SerializerEncoding.JSON);
            tracer.addEvent("gen_ai.prompt", Collections.singletonMap("event.body", eventBody), OffsetDateTime.now(), span);
        } catch (IOException e) {
            LOGGER.verbose("Failed to serialize prompt", e);
        }
    }

    private void recordCompletion(ChatCompletions completions, Context span) {
        if (!isEventCollectionEnabled) {
            return;
        }

        try {
            // TODO optimize serialization
            // TODO spec: there's more than role:content in the response message
            // TODO otel: we should eventually be able to pass it as a structured event payload
            String eventBody = serializer.serialize(completions.getChoices().stream().map(c -> c.getMessage()).collect(Collectors.toList()), SerializerEncoding.JSON);
            tracer.addEvent("gen_ai.completion", Collections.singletonMap("event.body", eventBody), OffsetDateTime.now(), span);
        } catch (IOException e) {
            LOGGER.verbose("Failed to serialize completions", e);
        }
    }
}
