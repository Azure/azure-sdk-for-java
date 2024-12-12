// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.models.RealtimeResponseMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeServerEventErrorError;
import com.azure.ai.openai.realtime.models.ServerErrorReceivedException;
import com.azure.ai.openai.realtime.utils.AudioFile;
import com.azure.ai.openai.realtime.utils.FileUtils;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreateResponse;
import com.azure.ai.openai.realtime.models.RealtimeFunctionTool;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeResponseFunctionCallItem;
import com.azure.ai.openai.realtime.models.RealtimeVoice;
import com.azure.ai.openai.realtime.models.ResponseCreateEvent;
import com.azure.ai.openai.realtime.models.ResponseDoneEvent;
import com.azure.ai.openai.realtime.models.ResponseOutputItemDoneEvent;
import com.azure.ai.openai.realtime.models.SessionUpdateEvent;
import com.azure.ai.openai.realtime.utils.ConversationItem;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * This sample demonstrates the usage of tool calls in the Realtime client library.
 * In this sample, we send to the service a description of a tool that returns the weather for a given location.
 */
public class ToolCall {

    /**
     * This sample demonstrates the usage of tool calls in the Realtime client library.
     * In this sample, we send to the service a description of a tool that returns the weather for a given location.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        RealtimeAsyncClient client = buildClient(false);

        Disposable.Composite disposables = Disposables.composite();

        client.start().block();

        RealtimeFunctionTool weatherTool = new RealtimeFunctionTool("get_weather_for_location")
            .setDescription("Get the weather for a location")
            .setParameters(new WeatherToolDescriptor());

        SessionUpdateEvent sessionConfig = new SessionUpdateEvent(new RealtimeRequestSession()
                .setTools(Arrays.asList(weatherTool))
            .setInstructions("Call provided tools if appropriate for the user's input")
            .setVoice(RealtimeVoice.ALLOY)
            .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
            .setInputAudioTranscription(new RealtimeAudioInputTranscriptionSettings()
                .setModel(RealtimeAudioInputTranscriptionModel.WHISPER_1)));

        // We configure the session with the tool that we have defined in our classes below
        client.sendMessage(sessionConfig).block();

        // We send our prompt
        FileUtils.sendAudioFileAsync(client,
            new AudioFile(FileUtils.openResourceFile("realtime_whats_the_weather_pcm16_24khz_mono.wav")))
            .block();

        disposables.add(
            // We add a subscriber to the tool call request
            client.getServerEvents()
                .takeUntil(serverEvent -> serverEvent instanceof ResponseDoneEvent)
                .ofType(ResponseOutputItemDoneEvent.class)
                .subscribe(
                    event -> functionCallRequested(event, client),
                    ToolCall::onError,
                    ToolCall::onComplete)
        );

        // The interaction with the service will send the 1st ResponseDoneEvent when requesting the tool call,
        // and then another one when the response is computed for the prompt using the tool call.
        // We block on the 2nd and last of these events to prevent exiting the program before the interaction is finished.
        ResponseDoneEvent lastDoneEvent = client.getServerEvents()
                .ofType(ResponseDoneEvent.class)
                .take(2).blockLast();

        RealtimeResponseMessageItem messageItem = (RealtimeResponseMessageItem) lastDoneEvent.getResponse().getOutput().get(0);
        RealtimeResponseTextContentPart textPart = (RealtimeResponseTextContentPart) messageItem.getContent().get(0);
        System.out.println("Response from the service: " + textPart.getText());

        try {
            client.stop().block();
            client.close();
            disposables.dispose();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Completion handler for the subscriber observing any tool call requests from the server
     */
    private static void onComplete() {
        System.out.println("Function call requested by the server.");
    }

    /**
     * Error handler. We are particularly interested in the {@link ServerErrorReceivedException} which describes errors
     * sent by the server as an event, but mapped into an exception for ease of use.
     *
     * @param error The error that occurred while consuming the server sent events.
     */
    private static void onError(Throwable error) {
        if (error instanceof ServerErrorReceivedException) {
            ServerErrorReceivedException serverError = (ServerErrorReceivedException) error;
            RealtimeServerEventErrorError errorDetails = serverError.getErrorDetails();
            System.out.println("Error type: " + errorDetails.getType());
            System.out.println("Error code: " + errorDetails.getCode());
            System.out.println("Error parameter: " + errorDetails.getParam());
            System.out.println("Error message: " + errorDetails.getMessage());
        } else {
            System.out.println(error.getMessage());
        }
    }

    /**
     * Tool call request handler. This method is where we call the function we offered as a tool call to the service
     * In this example we ignore the arguments sent by the server. In a real world scenario we would have to deserialize
     * the arguments into {@link WeatherToolProperties}.
     * @param serverEvent The server event that contains the tool call request.
     * @param client The RealtimeAsyncClient instance.
     */
    private static void functionCallRequested(ResponseOutputItemDoneEvent serverEvent, RealtimeAsyncClient client) {
        RealtimeResponseFunctionCallItem functionCallItem
                = (RealtimeResponseFunctionCallItem) serverEvent.getItem();

        System.out.println("Tool call requested by the server.");
        System.out.println("Requested tool name: " + functionCallItem.getName());
        System.out.println("Requested tool call ID: " + functionCallItem.getCallId());

        WeatherToolServiceArguments arguments = BinaryData.fromString(functionCallItem.getArguments())
                .toObject(WeatherToolServiceArguments.class);

        client.sendMessage(ConversationItem.createFunctionCallOutput(functionCallItem.getCallId(),
                        fakeWeatherToolCallMethod(arguments.location, arguments.unit))).block();
        client.sendMessage(
            new ResponseCreateEvent(new RealtimeClientEventResponseCreateResponse()))
            .block();
    }

    /**
     * This is the tool call implemented on the client side. In this case for the sake of the example it ignores
     * the arguments and returns a hardcoded response.
     *
     * @param location requested by the service requesting a tool call.
     * @param unit requested by the service requesting a tool call.
     * @return A fake weather response to be returned to the service requesting the tool call.
     */
    private static String fakeWeatherToolCallMethod(String location, String unit) {
        System.out.println("Requested location by the service: " + location);
        System.out.println("Requested unit by the service: " + unit);
        return "71 degrees Fahrenheit, sunny";
    }

    /**
     * Builds a RealtimeAsyncClient based on the configuration settings defined in the environment variables.
     * @param isAzure is set to `true` will build the client assuming an Azure backend, whereas `false` builds the client
     *                for the OpenAI backend.
     * @return an instance of {@link RealtimeAsyncClient}.
     */
    private static RealtimeAsyncClient buildClient(boolean isAzure) {
        if (isAzure) {
            String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
            String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
            String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

            return new RealtimeClientBuilder()
                    .endpoint(endpoint)
                    .deploymentOrModelName(deploymentOrModelId)
                    .credential(new AzureKeyCredential(azureOpenaiKey))
                    .buildAsyncClient();
        } else {
            String openaiKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
            String modelName = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

            return new RealtimeClientBuilder()
                    .credential(new KeyCredential(openaiKey))
                    .deploymentOrModelName(modelName)
                    .buildAsyncClient();
        }
    }

    /**
     * Represents the weather tool offered to the service for calling when necessary.
     */
    private static final class WeatherToolDescriptor implements JsonSerializable<WeatherToolDescriptor> {
        String type = "object";
        List<String> required = Arrays.asList("location", "unit");
        WeatherToolProperties properties = new WeatherToolProperties();

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeArrayField("required", this.required, JsonWriter::writeString);
            jsonWriter.writeJsonField("properties", this.properties);
            return jsonWriter.writeEndObject();
        }
    }

    /**
     * Represents the types of the members of the weather tool descriptor class.
     */
    private static final class WeatherToolProperties implements JsonSerializable<WeatherToolProperties> {
        StringField location = new StringField("The city and state e.g. San Francisco, CA");
        StringField unit = new StringField(Arrays.asList("c", "f"));

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeJsonField("location", this.location);
            jsonWriter.writeJsonField("unit", this.unit);
            return jsonWriter.writeEndObject();
        }
    }

    /**
     * Represents the string field type.
     */
    private static final class StringField implements JsonSerializable<StringField> {
        String type = "string";
        String description;
        List<String> possibleValues;

        StringField(String description) {
            this.description = description;
        }

        StringField(List<String> possibleValues) {
            this.possibleValues = possibleValues;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            if (this.description != null) {
                jsonWriter.writeStringField("description", this.description);
            }
            if (this.possibleValues != null) {
                // we use the name `possibleValues` as `enum` is a reserved keyword in Java
                jsonWriter.writeArrayField("enum", this.possibleValues, JsonWriter::writeString);
            }
            return jsonWriter.writeEndObject();
        }
    }

    private static final class WeatherToolServiceArguments {
        String location;
        String unit;

        public static WeatherToolServiceArguments fromJson(JsonReader reader) throws IOException {
            String location = null;
            String unit = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("location".equals(fieldName)) {
                    location = reader.getString();
                } else if ("unit".equals(fieldName)) {
                    unit = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            WeatherToolServiceArguments deserializedArguments = new WeatherToolServiceArguments();
            deserializedArguments.location = location;
            deserializedArguments.unit = unit;

            return deserializedArguments;
        }
    }
}
