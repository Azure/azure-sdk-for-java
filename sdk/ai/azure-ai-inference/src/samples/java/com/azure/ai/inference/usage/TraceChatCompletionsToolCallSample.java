// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inference.usage;

import com.azure.ai.inference.ChatCompletionsClient;
import com.azure.ai.inference.ChatCompletionsClientBuilder;
import com.azure.ai.inference.models.ChatChoice;
import com.azure.ai.inference.models.ChatCompletions;
import com.azure.ai.inference.models.ChatCompletionsOptions;
import com.azure.ai.inference.models.ChatCompletionsToolCall;
import com.azure.ai.inference.models.ChatCompletionsToolDefinition;
import com.azure.ai.inference.models.ChatRequestAssistantMessage;
import com.azure.ai.inference.models.ChatRequestMessage;
import com.azure.ai.inference.models.ChatRequestSystemMessage;
import com.azure.ai.inference.models.ChatRequestToolMessage;
import com.azure.ai.inference.models.ChatRequestUserMessage;
import com.azure.ai.inference.models.CompletionsFinishReason;
import com.azure.ai.inference.models.FunctionCall;
import com.azure.ai.inference.models.FunctionDefinition;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TraceChatCompletionsToolCallSample {
    /**
     * @param args Unused. Arguments to the program.
     */
    @SuppressWarnings("try")
    public static void main(final String[] args) {
        final OpenTelemetrySdk telemetry = configureOpenTelemetry();
        final Tracer tracer = telemetry.getTracer(TraceChatCompletionsToolCallSample.class.getName());

        final Span span = tracer.spanBuilder("main").startSpan();
        try (AutoCloseable scope = span.makeCurrent()) {

            final ChatCompletionsClient client = createChatCompletionClient();

            final List<ChatRequestMessage> messages = new ArrayList<>();
            messages.add(new ChatRequestSystemMessage("You are a helpful assistant."));
            messages.add(new ChatRequestUserMessage("What is the weather and temperature in Seattle?"));
            final GetWeatherTemperatureFunctions functions = new GetWeatherTemperatureFunctions();

            ChatCompletions response = client.complete(new ChatCompletionsOptions(messages).setTools(functions.toolDefinitions()));
            ChatChoice choice = response.getChoice();

            while (isToolCalls(choice)) {
                final List<ChatCompletionsToolCall> toolCalls = assertNonEmpty(choice.getMessage().getToolCalls());
                messages.add(toAssistantMessage(toolCalls));
                for (final ChatCompletionsToolCall toolCall : toolCalls) {
                    final ChatRequestToolMessage toolMessage = functions.invoke(toolCall);
                    messages.add(toolMessage);
                }
                response = client.complete(new ChatCompletionsOptions(messages).setTools(functions.toolDefinitions()));
                choice = response.getChoice();
            }

            System.out.println("Model response: " + modelResponseContent(response));
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            throw new RuntimeException(e);
        } finally {
            span.end();
        }
    }

    private static OpenTelemetrySdk configureOpenTelemetry() {
        // With the below configuration, the runtime sends OpenTelemetry data to the local OTLP/gRPC endpoint.
        //
        // For debugging purposes, Aspire Dashboard can be run locally that listens for telemetry data and offer a UI
        // for viewing the collected data. To run Aspire Dashboard, run the following docker command:
        //
        // docker run --rm -p 18888:18888 -p 4317:18889 -p 4318:18890 --name aspire-dashboard mcr.microsoft.com/dotnet/nightly/aspire-dashboard:latest
        //
        // The output of the docker command includes a link to the dashboard. For more information on Aspire Dashboard,
        // see https://learn.microsoft.com/dotnet/aspire/fundamentals/dashboard/overview
        //
        // See https://learn.microsoft.com/azure/developer/java/sdk/tracing for more information on tracing with Azure SDK.
        //
        final AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        return sdkBuilder
            .addPropertiesSupplier(() -> {
                final Map<String, String> properties = new HashMap<>();
                properties.put("otel.service.name", "get-weather-temperature-sample");
                // change to your endpoint address, "http://localhost:4317" is used by default
                // properties.put("otel.exporter.otlp.endpoint", "http://localhost:4317");
                return properties;
            })
            .setResultAsGlobal()
            .build()
            .getOpenTelemetrySdk();
    }

    private static ChatCompletionsClient createChatCompletionClient() {
        return new ChatCompletionsClientBuilder()
            .endpoint(System.getenv("MODEL_ENDPOINT"))
            .credential(new AzureKeyCredential(System.getenv("AZURE_API_KEY")))
            // uncomment to capture message content in the telemetry (or you can set AZURE_TRACING_GEN_AI_CONTENT_RECORDING_ENABLED environment variable to `true`)
            // .configuration(new ConfigurationBuilder().putProperty("azure.tracing.gen_ai.content_recording_enabled", "true").build())
            .buildClient();
    }

    private static boolean isToolCalls(ChatChoice choice) {
        return choice.getFinishReason() == CompletionsFinishReason.TOOL_CALLS;
    }

    private static List<ChatCompletionsToolCall> assertNonEmpty(List<ChatCompletionsToolCall> toolCalls) {
        if (toolCalls == null || toolCalls.isEmpty()) {
            throw new RuntimeException("Service requested tool-calls, but without information about function(s) to invoke.");
        }
        return toolCalls;
    }

    private static ChatRequestAssistantMessage toAssistantMessage(List<ChatCompletionsToolCall> toolCalls) {
        return new ChatRequestAssistantMessage("").setToolCalls(toolCalls);
    }

    private static String modelResponseContent(ChatCompletions response) {
        return response.getChoices().get(0).getMessage().getContent();
    }

    /**
     * represents function tool ('get_weather', 'get_temperature') definitions and react to model evaluation of function tools.
     */
    private static final class GetWeatherTemperatureFunctions {
        private final WeatherFunc weatherFunc;
        private final TemperatureFunc temperatureFunc;
        private final List<ChatCompletionsToolDefinition> toolDefinitions = new ArrayList<>(2);

        GetWeatherTemperatureFunctions() {
            this.weatherFunc = new WeatherFunc();
            this.temperatureFunc = new TemperatureFunc();
            this.toolDefinitions.add(new ChatCompletionsToolDefinition(weatherFunc.getDefinition()));
            this.toolDefinitions.add(new ChatCompletionsToolDefinition(temperatureFunc.getDefinition()));
        }

        List<ChatCompletionsToolDefinition> toolDefinitions() {
            return this.toolDefinitions;
        }

        ChatRequestToolMessage invoke(ChatCompletionsToolCall toolCall) {
            final Optional<ChatRequestToolMessage> wResponse = weatherFunc.tryInvoke(toolCall);
            if (wResponse.isPresent()) {
                return wResponse.get();
            }
            final Optional<ChatRequestToolMessage> rwResponse = temperatureFunc.tryInvoke(toolCall);
            if (rwResponse.isPresent()) {
                return rwResponse.get();
            }
            throw new RuntimeException("Service requested tool-call has no matching function information.");
        }

        private static final class WeatherFunc {
            private FunctionDefinition getDefinition() {
                return new FunctionDefinition("get_weather")
                    .setDescription("Returns description of the weather in the specified city")
                    .setParameters(BinaryData.fromBytes(parameters()));
            }

            @SuppressWarnings("try")
            private Optional<ChatRequestToolMessage> tryInvoke(ChatCompletionsToolCall toolCall) {
                final FunctionCall function = toolCall.getFunction();
                final String functionName = function.getName();
                if (functionName.equalsIgnoreCase("get_weather")) {
                    final FunctionArguments functionArguments = BinaryData.fromString(function.getArguments()).toObject(FunctionArguments.class);
                    final String functionResponse;
                    if ("Seattle".equalsIgnoreCase(functionArguments.getCity())) {
                        functionResponse = "Nice weather";
                    } else if ("New York City".equalsIgnoreCase(functionArguments.getCity())) {
                        functionResponse = "Good weather";
                    } else {
                        functionResponse = "Unavailable";
                    }
                    return Optional.of(new ChatRequestToolMessage(toolCall.getId()).setContent(functionResponse));
                }
                return Optional.empty();
            }

            private static byte[] parameters() {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     JsonWriter jsonWriter = JsonProviders.createWriter(byteArrayOutputStream)) {
                    jsonWriter.writeStartObject();
                    jsonWriter.writeStringField("type", "object");
                    jsonWriter.writeStartObject("properties");
                    jsonWriter.writeStartObject("city");
                    jsonWriter.writeStringField("type", "string");
                    jsonWriter.writeStringField("description", "The name of the city for which weather info is requested");
                    jsonWriter.writeEndObject();
                    jsonWriter.writeEndObject();
                    jsonWriter.writeStartArray("required");
                    jsonWriter.writeString("city");
                    jsonWriter.writeEndArray();
                    jsonWriter.writeEndObject();
                    jsonWriter.flush();
                    return byteArrayOutputStream.toByteArray();
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            }

            private static void sleep() {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // ignored
                }
            }
        }

        private static final class TemperatureFunc {
            private FunctionDefinition getDefinition() {
                return new FunctionDefinition("get_temperature")
                    .setDescription("Returns the current temperature for the specified city")
                    .setParameters(BinaryData.fromBytes(parameters()));
            }

            @SuppressWarnings("try")
            private Optional<ChatRequestToolMessage> tryInvoke(ChatCompletionsToolCall toolCall) {
                final FunctionCall function = toolCall.getFunction();
                final String functionName = function.getName();
                if (functionName.equalsIgnoreCase("get_temperature")) {
                    final FunctionArguments functionArguments = BinaryData.fromString(function.getArguments()).toObject(FunctionArguments.class);
                    final String functionResponse;
                    if ("Seattle".equalsIgnoreCase(functionArguments.getCity())) {
                        functionResponse = "75";
                    } else if ("New York City".equalsIgnoreCase(functionArguments.getCity())) {
                        functionResponse = "80";
                    } else {
                        functionResponse = "Unavailable";
                    }
                    return Optional.of(new ChatRequestToolMessage(toolCall.getId()).setContent(functionResponse));
                }
                return Optional.empty();
            }

            private static byte[] parameters() {
                try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                     JsonWriter jsonWriter = JsonProviders.createWriter(byteArrayOutputStream)) {
                    jsonWriter.writeStartObject();
                    jsonWriter.writeStringField("type", "object");
                    jsonWriter.writeStartObject("properties");
                    jsonWriter.writeStartObject("city");
                    jsonWriter.writeStringField("type", "string");
                    jsonWriter.writeStringField("description", "The name of the city for which temperature info is requested");
                    jsonWriter.writeEndObject();
                    jsonWriter.writeEndObject();
                    jsonWriter.writeStartArray("required");
                    jsonWriter.writeString("city");
                    jsonWriter.writeEndArray();
                    jsonWriter.writeEndObject();
                    jsonWriter.flush();
                    return byteArrayOutputStream.toByteArray();
                } catch (IOException ioe) {
                    throw new UncheckedIOException(ioe);
                }
            }
        }

        private static final class FunctionArguments implements JsonSerializable<FunctionArguments> {
            private final String city;

            private FunctionArguments(String city) {
                this.city = city;
            }

            public String getCity() {
                return this.city;
            }

            @Override
            public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
                jsonWriter.writeStartObject();
                jsonWriter.writeStringField("city", this.city);
                return jsonWriter.writeEndObject();
            }

            public static FunctionArguments fromJson(JsonReader jsonReader) throws IOException {
                return jsonReader.readObject(reader -> {
                    String city = null;
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("city".equals(fieldName)) {
                            city = reader.getString();
                        } else {
                            reader.skipChildren();
                        }
                    }
                    return new FunctionArguments(city);
                });
            }
        }
    }
}
