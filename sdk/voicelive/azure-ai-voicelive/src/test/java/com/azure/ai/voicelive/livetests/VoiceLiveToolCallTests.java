// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.livetests;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventResponseCreate;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.FunctionCallOutputItem;
import com.azure.ai.voicelive.models.ItemType;
import com.azure.ai.voicelive.models.ResponseFunctionCallItem;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioTranscriptDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDone;
import com.azure.ai.voicelive.models.ToolChoiceFunctionSelection;
import com.azure.ai.voicelive.models.VoiceLiveFunctionDefinition;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.ai.voicelive.models.VoiceLiveToolDefinition;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Live tests for VoiceLive tool/function call features.
 */
public class VoiceLiveToolCallTests extends VoiceLiveTestBase {

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o-realtime", "gpt-4o" })
    @LiveOnly
    public void testRealtimeServiceToolCall(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicBoolean functionCallReceived = new AtomicBoolean(false);
        List<String> functionCallArguments = new ArrayList<>();
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveFunctionDefinition weatherTool
                = new VoiceLiveFunctionDefinition("get_weather").setDescription("Get the weather for a given location.")
                    .setParameters(createFunctionParameters("location"));

            List<VoiceLiveToolDefinition> tools = Arrays.asList(weatherTool);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions(
                    "You are a helpful assistant with tools. If asked about weather, call get_weather function.")
                .setTools(tools)
                .setToolChoice(BinaryData.fromString("auto"))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.CONVERSATION_ITEM_CREATED) {
                    if (event instanceof SessionUpdateConversationItemCreated) {
                        SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) event;
                        if (itemCreated.getItem() != null
                            && itemCreated.getItem().getType() == ItemType.FUNCTION_CALL) {
                            functionCallReceived.set(true);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    if (event instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
                        SessionUpdateResponseFunctionCallArgumentsDone funcDone
                            = (SessionUpdateResponseFunctionCallArgumentsDone) event;
                        functionCallArguments.add(funcDone.getArguments());
                    }
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertTrue(functionCallReceived.get(), "Should receive function call item created event");
            Assertions.assertFalse(functionCallArguments.isEmpty(), "Should have function call arguments");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o", "gpt-5-chat" })
    @LiveOnly
    public void testRealtimeServiceToolChoice(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicReference<String> functionNameCalled = new AtomicReference<>();
        AtomicReference<String> functionArguments = new AtomicReference<>();
        CountDownLatch responseLatch = new CountDownLatch(1);

        try {
            VoiceLiveFunctionDefinition weatherTool
                = new VoiceLiveFunctionDefinition("get_weather").setDescription("Get the weather for a given location.")
                    .setParameters(createFunctionParameters("location"));

            VoiceLiveFunctionDefinition timeTool = new VoiceLiveFunctionDefinition("get_time")
                .setDescription("Get the current time in a given location.")
                .setParameters(createFunctionParameters("location"));

            List<VoiceLiveToolDefinition> tools = Arrays.asList(weatherTool, timeTool);

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant with tools.")
                .setTools(tools)
                .setToolChoice(BinaryData.fromObject(new ToolChoiceFunctionSelection("get_time")))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    if (event instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
                        SessionUpdateResponseFunctionCallArgumentsDone funcDone
                            = (SessionUpdateResponseFunctionCallArgumentsDone) event;
                        functionNameCalled.set(funcDone.getName());
                        functionArguments.set(funcDone.getArguments());
                    }
                    responseLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean received = responseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            Assertions.assertTrue(received, "Should receive response within timeout");
            Assertions.assertEquals("get_time", functionNameCalled.get(),
                "Should call get_time function as forced by tool_choice");
            Assertions.assertNotNull(functionArguments.get(), "Should have function arguments");
            Assertions.assertTrue(functionArguments.get().contains("北京") || functionArguments.get().contains("Beijing"),
                "Arguments should contain Beijing location, got: " + functionArguments.get());

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4o", "gpt-4o-realtime" })
    @LiveOnly
    public void testLiveSessionUpdateWithTools(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicInteger firstResponseBytes = new AtomicInteger(0);
        AtomicReference<String> functionCallName = new AtomicReference<>();
        CountDownLatch firstResponseLatch = new CountDownLatch(1);
        CountDownLatch functionCallLatch = new CountDownLatch(1);
        AtomicBoolean waitingForFunctionCall = new AtomicBoolean(false);

        try {
            VoiceLiveSessionOptions initialSession = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant that can answer questions.")
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AvaMultilingualNeural")))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE) {
                    if (!waitingForFunctionCall.get()) {
                        firstResponseLatch.countDown();
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_DELTA) {
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioDelta = (SessionUpdateResponseAudioDelta) event;
                        if (audioDelta.getDelta() != null && !waitingForFunctionCall.get()) {
                            firstResponseBytes.addAndGet(audioDelta.getDelta().length);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    if (event instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
                        SessionUpdateResponseFunctionCallArgumentsDone funcDone
                            = (SessionUpdateResponseFunctionCallArgumentsDone) event;
                        functionCallName.set(funcDone.getName());
                    }
                    functionCallLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    firstResponseLatch.countDown();
                    functionCallLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                firstResponseLatch.countDown();
                functionCallLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(initialSession)).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean firstReceived = firstResponseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(firstReceived, "Should receive first response");
            Assertions.assertTrue(firstResponseBytes.get() > 0, "First response should have audio");

            waitingForFunctionCall.set(true);

            VoiceLiveFunctionDefinition weatherTool
                = new VoiceLiveFunctionDefinition("get_weather").setDescription("Get the weather for a given location.")
                    .setParameters(createFunctionParameters("location"));

            VoiceLiveSessionOptions updatedSession = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant with tools.")
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AvaMultilingualNeural")))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setTools(Arrays.asList(weatherTool))
                .setToolChoice(BinaryData.fromString("auto"))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            session.sendEvent(new ClientEventSessionUpdate(updatedSession)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean functionCallReceived = functionCallLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(functionCallReceived, "Should receive function call");
            Assertions.assertEquals("get_weather", functionCallName.get(), "Should call get_weather function");

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "gpt-4.1", "gpt-5", "phi4-mm-realtime" })
    @LiveOnly
    public void testRealtimeServiceToolCallParameter(String model) throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient();

        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicReference<String> functionCallName = new AtomicReference<>();
        AtomicReference<String> functionArguments = new AtomicReference<>();
        AtomicReference<String> callId = new AtomicReference<>();
        AtomicReference<String> previousItemId = new AtomicReference<>();
        AtomicReference<String> finalTranscript = new AtomicReference<>();
        CountDownLatch functionCallLatch = new CountDownLatch(1);
        CountDownLatch responseDoneLatch = new CountDownLatch(1);
        CountDownLatch finalResponseLatch = new CountDownLatch(1);
        AtomicBoolean waitingForFinalResponse = new AtomicBoolean(false);

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("type", "object");
            Map<String, Object> properties = new HashMap<>();
            Map<String, Object> locationProp = new HashMap<>();
            locationProp.put("type", "string");
            locationProp.put("description", "The location to get the weather for.");
            properties.put("location", locationProp);
            params.put("properties", properties);
            params.put("required", Arrays.asList("location"));

            VoiceLiveFunctionDefinition weatherTool = new VoiceLiveFunctionDefinition("get_weather")
                .setDescription("Retrieve the weather of given location.")
                .setParameters(BinaryData.fromObject(params));

            List<VoiceLiveToolDefinition> tools = Arrays.asList(weatherTool);

            String instructions = "You are a helpful assistant with tools.";
            if (!"phi4-mm-realtime".equals(model)) {
                instructions
                    += " If you are asked about the weather, please respond with `I will get the weather for you. Please wait a moment.` and then call the get_weather function with the location parameter.";
            }

            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setInstructions(instructions)
                .setTools(tools)
                .setToolChoice(BinaryData.fromString("auto"))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.WHISPER_1))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            VoiceLiveSessionAsyncClient session = client.startSession(model).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.CONVERSATION_ITEM_CREATED) {
                    if (event instanceof SessionUpdateConversationItemCreated) {
                        SessionUpdateConversationItemCreated itemCreated = (SessionUpdateConversationItemCreated) event;
                        if (itemCreated.getItem() != null
                            && itemCreated.getItem().getType() == ItemType.FUNCTION_CALL) {
                            ResponseFunctionCallItem funcItem = (ResponseFunctionCallItem) itemCreated.getItem();
                            callId.set(funcItem.getCallId());
                            previousItemId.set(funcItem.getId());
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    if (event instanceof SessionUpdateResponseFunctionCallArgumentsDone) {
                        SessionUpdateResponseFunctionCallArgumentsDone funcDone
                            = (SessionUpdateResponseFunctionCallArgumentsDone) event;
                        functionCallName.set(funcDone.getName());
                        functionArguments.set(funcDone.getArguments());
                    }
                    functionCallLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    if (!waitingForFinalResponse.get()) {
                        responseDoneLatch.countDown();
                    } else {
                        finalResponseLatch.countDown();
                    }
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE) {
                    if (waitingForFinalResponse.get() && event instanceof SessionUpdateResponseAudioTranscriptDone) {
                        SessionUpdateResponseAudioTranscriptDone transcriptDone
                            = (SessionUpdateResponseAudioTranscriptDone) event;
                        finalTranscript.set(transcriptDone.getTranscript());
                    }
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    functionCallLatch.countDown();
                    responseDoneLatch.countDown();
                    finalResponseLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                functionCallLatch.countDown();
                responseDoneLatch.countDown();
                finalResponseLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean functionCallReceived = functionCallLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(functionCallReceived, "Should receive function call within timeout");
            Assertions.assertEquals("get_weather", functionCallName.get(), "Should call get_weather function");
            Assertions.assertNotNull(functionArguments.get(), "Should have function arguments");
            Assertions.assertTrue(functionArguments.get().contains("北京") || functionArguments.get().contains("Beijing"),
                "Arguments should contain Beijing location, got: " + functionArguments.get());

            boolean firstResponseDone = responseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(firstResponseDone, "Should receive first response done");

            waitingForFinalResponse.set(true);

            String toolOutput = "{\"location\": \"Beijing\", \"weather\": \"sunny\", \"temp_c\": 25}";

            FunctionCallOutputItem outputItem = new FunctionCallOutputItem(callId.get(), toolOutput);
            ClientEventConversationItemCreate createItemEvent
                = new ClientEventConversationItemCreate().setItem(outputItem).setPreviousItemId(previousItemId.get());
            session.sendEvent(createItemEvent).block(SEND_TIMEOUT);

            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);

            boolean finalResponseReceived = finalResponseLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(finalResponseReceived, "Should receive final response within timeout");

            Assertions.assertNotNull(finalTranscript.get(), "Should have final transcript");
            boolean hasSunny = finalTranscript.get().contains("晴") || finalTranscript.get().contains("sunny");
            boolean hasTemp = finalTranscript.get().contains("25");
            Assertions.assertTrue(hasSunny || hasTemp,
                "Transcript should contain weather info (sunny/晴 or 25), got: " + finalTranscript.get());

            session.close();
        } catch (Exception e) {
            Assertions.fail("Test failed with exception: " + e.getMessage());
        }
    }
}
