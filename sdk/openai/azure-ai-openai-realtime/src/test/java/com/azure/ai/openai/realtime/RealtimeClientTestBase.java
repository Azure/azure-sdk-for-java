// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.WeatherToolDescriptor;
import com.azure.ai.openai.realtime.implementation.websocket.MessageEncoder;
import com.azure.ai.openai.realtime.implementation.websocket.WebSocketClient;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEvent;
import com.azure.ai.openai.realtime.models.RealtimeClientEventConversationItemCreate;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeFunctionTool;
import com.azure.ai.openai.realtime.models.RealtimeRequestFunctionCallOutputItem;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeRequestTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeRequestUserMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeServerEvent;
import com.azure.ai.openai.realtime.models.RealtimeVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.function.BiConsumer;

public abstract class RealtimeClientTestBase { //} extends TestProxyTestBase {

    RealtimeClientBuilder getRealtimeClientBuilder(WebSocketClient webSocketClient, OpenAIServiceVersion serviceVersion) {
        String azureOpenaiKey = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_KEY");
        String endpoint = Configuration.getGlobalConfiguration().get("AZURE_OPENAI_ENDPOINT");
        String deploymentOrModelId = Configuration.getGlobalConfiguration().get("MODEL_OR_DEPLOYMENT_NAME");

        return new RealtimeClientBuilder()
                .endpoint(endpoint)
                .deploymentOrModelName(deploymentOrModelId)
                .serviceVersion(serviceVersion)
                .credential(new AzureKeyCredential(azureOpenaiKey));
    }

    RealtimeClientBuilder getNonAzureRealtimeClientBuilder(WebSocketClient webSocketClient) {
        String openAIKey = Configuration.getGlobalConfiguration().get("OPENAI_KEY");
        String openAIModel = Configuration.getGlobalConfiguration().get("OPENAI_MODEL");

        return new RealtimeClientBuilder()
                .deploymentOrModelName(openAIModel)
                .credential(new KeyCredential(openAIKey));
    }

    @Test
    abstract void testAlawSendAudio();

    @Test
    abstract void canConfigureSession();

    @Test
    abstract void textOnly();

    @Test
    abstract void ItemManipulation();

    @Test
    abstract void AudioWithTool();

    @Test
    abstract void canDisableVoiceActivityDetection();

    @Test
    abstract void badCommandProvidesError();

    protected String toJson(RealtimeClientEvent event) {
        MessageEncoder decoder = new MessageEncoder();
        return decoder.encode(event);
    }

    protected String toJson(RealtimeServerEvent event) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            event.toJson(writer).flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected RealtimeClientEventConversationItemCreate createTextConversationItem(String itemText) {
        RealtimeRequestUserMessageItem messageItem = new RealtimeRequestUserMessageItem()
                .setTextContent(Arrays.asList(new RealtimeRequestTextContentPart(itemText)));
        return new RealtimeClientEventConversationItemCreate(messageItem);
    }

    protected RealtimeClientEventConversationItemCreate createFunctionOutputItem(String callId, String functionOutput) {
        return new RealtimeClientEventConversationItemCreate(new RealtimeRequestFunctionCallOutputItem(callId, functionOutput));
    }

    void getWeatherToolRunner(BiConsumer<RealtimeFunctionTool, RealtimeClientEventSessionUpdate> testRunner) {
        RealtimeFunctionTool weatherTool = new RealtimeFunctionTool("get_weather_for_location")
                .setDescription("Get the weather for a location")
                .setParameters(BinaryData.fromObject(new WeatherToolDescriptor()));
        RealtimeClientEventSessionUpdate sessionUpdate = new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession()
                        .setTools(Arrays.asList(weatherTool))
                        .setInstructions("Call provided tools if appropriate for the user's input")
                        .setVoice(RealtimeVoice.ALLOY)
                        .setModalities(Arrays.asList(RealtimeRequestSessionModality.AUDIO, RealtimeRequestSessionModality.TEXT))
                        .setInputAudioTranscription(
                                new RealtimeAudioInputTranscriptionSettings()
                                        .setModel(RealtimeAudioInputTranscriptionModel.WHISPER_1)
                        )
        );
        testRunner.accept(weatherTool, sessionUpdate);
    }
}
