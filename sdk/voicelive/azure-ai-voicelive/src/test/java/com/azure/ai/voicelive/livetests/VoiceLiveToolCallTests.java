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
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioTranscriptDone;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDelta;
import com.azure.ai.voicelive.models.SessionUpdateResponseFunctionCallArgumentsDone;
import com.azure.ai.voicelive.models.ToolChoiceFunctionSelection;
import com.azure.ai.voicelive.models.VoiceLiveFunctionDefinition;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive tool/function call operations.
 * Translated from Python voicelive tool call tests.
 */
public class VoiceLiveToolCallTests extends VoiceLiveTestBase {

    // API version for the older preview used by session update test
    private static final String API_VERSION_2025_05_01_PREVIEW = "2025-05-01-preview";

    // ===== test_realtime_service_tool_call =====
    // Python: models=[gpt-4o-realtime, gpt-4o], api_versions=[2025-10-01, 2026-01-01-preview]
    // Uses _get_speech_recognition_setting(model), audio=4-1.wav, tool=assess_pronunciation
    // Voice: AzureStandardVoice("en-US-AriaNeural")

    static Stream<Arguments> toolCallParams() {
        return crossProduct(new String[] { MODEL_GPT_4O_REALTIME, MODEL_GPT_4O },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("toolCallParams")
    @LiveOnly
    public void testRealtimeServiceToolCall(String model, String apiVersion) throws InterruptedException, IOException {
        // Python uses @pytest.mark.flaky(reruns=3, reruns_delay=2) because the model
        // sometimes responds with audio instead of calling the tool.
        int maxAttempts = 3;
        AssertionError lastFailure = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                doTestRealtimeServiceToolCall(model, apiVersion);
                return; // passed
            } catch (AssertionError e) {
                lastFailure = e;
                System.out.println("testRealtimeServiceToolCall attempt " + attempt + "/" + maxAttempts + " failed: "
                    + e.getMessage());
                if (attempt < maxAttempts) {
                    Thread.sleep(2000); // reruns_delay=2
                }
            }
        }
        throw lastFailure;
    }

    private void doTestRealtimeServiceToolCall(String model, String apiVersion)
        throws InterruptedException, IOException {

        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("4-1.wav");

        // Matching Python: collect RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA events within timeout
        List<SessionUpdateResponseFunctionCallArgumentsDelta> functionCallResults = new ArrayList<>();
        CountDownLatch firstDeltaLatch = new CountDownLatch(1);
        // Track response completions so we can re-issue response.create() if VAD
        // triggered a non-tool-call response first (gpt-4o-realtime race condition).
        CountDownLatch responseDoneLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        try {
            // Build tool: assess_pronunciation (no parameters, matching Python)
            VoiceLiveFunctionDefinition assessTool = new VoiceLiveFunctionDefinition("assess_pronunciation");
            assessTool.setDescription("Assess pronunciation of the last user input speech");

            // Session options matching Python exactly
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions(
                    "You are a teacher to a student who is learning English. You are talking with student with speech. "
                        + "For each user input speech, you need to call the assess_pronunciation function to assess "
                        + "the pronunciation of the last user input speech, and then give feedback to the student.")
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AriaNeural")))
                .setInputAudioTranscription(getSpeechRecognitionSetting(model))
                .setTools(Arrays.asList(assessTool))
                .setToolChoice(BinaryData.fromObject("auto"));

            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DELTA) {
                    functionCallResults.add((SessionUpdateResponseFunctionCallArgumentsDelta) event);
                    firstDeltaLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    responseDoneLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                firstDeltaLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);

            // Send audio and response.create() in tight succession to beat server VAD.
            // With gpt-4o-realtime, the default server VAD detects speech, auto-commits the
            // buffer and triggers its own response before a delayed response.create() arrives.
            session.sendInputAudio(audioData)
                .then(session.sendEvent(new ClientEventResponseCreate()))
                .block(SEND_TIMEOUT);

            // Python uses a 10s polling loop; wait for at least one delta event
            boolean gotDelta = firstDeltaLatch.await(10, TimeUnit.SECONDS);

            // If no function call delta yet, the server VAD may have created a non-tool-call
            // response first (audio). Wait for that response to finish, then try again.
            if (!gotDelta && functionCallResults.isEmpty()) {
                responseDoneLatch.await(5, TimeUnit.SECONDS);
                // Re-issue response.create() now that the VAD response has completed
                session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);
                firstDeltaLatch.await(10, TimeUnit.SECONDS);
            }

            Assertions.assertFalse(functionCallResults.isEmpty(), "Should have at least one function call result");
        } finally {
            closeSession(session);
        }
    }

    // ===== test_realtime_service_tool_choice =====
    // Python: models=[gpt-realtime, gpt-4o, gpt-5-chat], skip if "realtime" in model
    //   -> effective models: [gpt-4o, gpt-5-chat]
    // api_versions=[2025-10-01, 2026-01-01-preview]
    // Uses azure-speech + ServerVad, audio=ask_weather.wav
    // Tools: get_weather, get_time. ToolChoice: get_time
    // Assert: function_done.name == "get_time", arguments contains Beijing

    static Stream<Arguments> toolChoiceParams() {
        return crossProduct(new String[] { MODEL_GPT_4O, MODEL_GPT_5_CHAT },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("toolChoiceParams")
    @LiveOnly
    public void testRealtimeServiceToolChoice(String model, String apiVersion)
        throws InterruptedException, IOException {

        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicReference<SessionUpdateResponseFunctionCallArgumentsDone> functionDone = new AtomicReference<>();
        CountDownLatch responseDoneLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        try {
            // Build tools: get_weather, get_time (matching Python)
            VoiceLiveFunctionDefinition weatherTool = new VoiceLiveFunctionDefinition("get_weather");
            weatherTool.setDescription("Get the weather for a given location.");
            weatherTool.setParameters(createFunctionParameters("location"));

            VoiceLiveFunctionDefinition timeTool = new VoiceLiveFunctionDefinition("get_time");
            timeTool.setDescription("Get the current time in a given location.");
            timeTool.setParameters(createFunctionParameters("location"));

            // Session options: azure-speech + ServerVad, tool_choice=get_time (matching Python)
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant with tools.")
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.AZURE_SPEECH))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200))
                .setTools(Arrays.asList(weatherTool, timeTool))
                .setToolChoice(BinaryData.fromObject(new ToolChoiceFunctionSelection("get_time")));

            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    functionDone.set((SessionUpdateResponseFunctionCallArgumentsDone) event);
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    responseDoneLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    responseDoneLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                responseDoneLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean done = responseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(done, "Should receive response done event");
            Assertions.assertNotNull(functionDone.get(), "Should have received function call arguments done");
            Assertions.assertEquals("get_time", functionDone.get().getName(), "Function name should be get_time");

            // Normalized argument check matching Python:
            // function_done.arguments.replace(" ", "").replace("\n", "")
            //   in ['{"location":"北京"}', '{"location":"Beijing"}']
            String normalized = functionDone.get().getArguments().replace(" ", "").replace("\n", "");
            boolean matchesBeijing
                = "{\"location\":\"北京\"}".equals(normalized) || "{\"location\":\"Beijing\"}".equals(normalized);
            Assertions.assertTrue(matchesBeijing,
                "Arguments should contain Beijing location, got: " + functionDone.get().getArguments());
        } finally {
            closeSession(session);
        }
    }

    // ===== test_realtime_service_tool_call_parameter =====
    // Python: models=[gpt-realtime, gpt-4.1, gpt-5, gpt-5.1, gpt-5.2, phi4-mm-realtime],
    //   skip if "realtime" in model -> effective models: [gpt-4.1, gpt-5]
    // api_versions=[2025-10-01, 2026-01-01-preview]
    // Uses azure-speech + ServerVad, audio=ask_weather.wav
    // Tool: get_weather. Full tool call flow: get function call -> send tool output -> get transcript
    // Assert: transcript contains "sunny" or chinese equivalent, and "25"

    static Stream<Arguments> toolCallParameterParams() {
        return crossProduct(new String[] { MODEL_GPT_41, MODEL_GPT_5 },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("toolCallParameterParams")
    @LiveOnly
    public void testRealtimeServiceToolCallParameter(String model, String apiVersion)
        throws InterruptedException, IOException {

        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("ask_weather.wav");

        AtomicReference<SessionUpdateResponseFunctionCallArgumentsDone> functionDone = new AtomicReference<>();
        // Phase 1: wait for function call response to FULLY complete (response.done)
        // This avoids a race where audio_transcript.done and function_call_arguments.done
        // are interleaved within the same response.
        AtomicBoolean functionCallSeen = new AtomicBoolean(false);
        CountDownLatch functionCallResponseDoneLatch = new CountDownLatch(1);
        // Phase 2: collect transcripts from post-tool-output response
        AtomicBoolean collectingPostToolTranscripts = new AtomicBoolean(false);
        List<String> postToolTranscripts = new ArrayList<>();
        CountDownLatch postToolResponseDoneLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        try {
            // Build tool: get_weather
            VoiceLiveFunctionDefinition weatherTool = new VoiceLiveFunctionDefinition("get_weather");
            weatherTool.setDescription("Retrieve the weather of given location.");
            weatherTool.setParameters(createFunctionParameters("location"));

            // Session options: azure-speech + ServerVad (matching Python)
            VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant with tools. "
                    + "If you are asked about the weather, please respond with "
                    + "`I will get the weather for you. Please wait a moment.` "
                    + "and then call the get_weather function with the location parameter.")
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.AZURE_SPEECH))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200))
                .setTools(Arrays.asList(weatherTool))
                .setToolChoice(BinaryData.fromObject("auto"));

            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE) {
                    functionDone.set((SessionUpdateResponseFunctionCallArgumentsDone) event);
                    functionCallSeen.set(true);
                } else if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE) {
                    if (collectingPostToolTranscripts.get()) {
                        String transcript = ((SessionUpdateResponseAudioTranscriptDone) event).getTranscript();
                        if (transcript != null && !transcript.isEmpty()) {
                            postToolTranscripts.add(transcript);
                        }
                    }
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    if (functionCallSeen.get() && !collectingPostToolTranscripts.get()) {
                        // Function call response fully complete (all output items done)
                        functionCallResponseDoneLatch.countDown();
                    } else if (collectingPostToolTranscripts.get()) {
                        // Post-tool-output response complete
                        postToolResponseDoneLatch.countDown();
                    }
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    functionCallResponseDoneLatch.countDown();
                    postToolResponseDoneLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                functionCallResponseDoneLatch.countDown();
                postToolResponseDoneLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            // Send audio + trailing silence
            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            // Wait for RESPONSE_DONE that contains the function call.
            // This ensures all interleaved audio_transcript events from the same response
            // have been fully processed before we start collecting post-tool transcripts.
            boolean responseDone = functionCallResponseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(responseDone, "Should receive response done with function call");
            Assertions.assertNotNull(functionDone.get(), "Function call should not be null");
            Assertions.assertEquals("get_weather", functionDone.get().getName(), "Function name should be get_weather");

            // Verify arguments contain Beijing
            String normalized = functionDone.get().getArguments().replace(" ", "").replace("\n", "");
            boolean matchesBeijing
                = "{\"location\":\"北京\"}".equals(normalized) || "{\"location\":\"Beijing\"}".equals(normalized);
            Assertions.assertTrue(matchesBeijing,
                "Arguments should contain Beijing location, got: " + functionDone.get().getArguments());

            // Now safe to flip the flag — RESPONSE_DONE guarantees all pre-tool events are processed
            collectingPostToolTranscripts.set(true);

            // Send tool output and trigger new response
            String toolOutput = "{\"location\": \"Beijing\", \"weather\": \"sunny\", \"temp_c\": 25}";
            FunctionCallOutputItem outputItem = new FunctionCallOutputItem(functionDone.get().getCallId(), toolOutput);
            ClientEventConversationItemCreate createItem = new ClientEventConversationItemCreate();
            createItem.setItem(outputItem);
            session.sendEvent(createItem).block(SEND_TIMEOUT);
            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);

            // Wait for the post-tool response to complete
            boolean gotPostToolResponse = postToolResponseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(gotPostToolResponse, "Should receive post-tool response done");

            // Verify transcript contains weather info
            String fullTranscript = String.join(" ", postToolTranscripts).toLowerCase();
            boolean hasSunny = fullTranscript.contains("\u6674") || fullTranscript.contains("sunny");
            boolean has25 = fullTranscript.contains("25");
            Assertions.assertTrue(hasSunny, "Transcript should mention sunny, got: " + fullTranscript);
            Assertions.assertTrue(has25, "Transcript should mention 25, got: " + fullTranscript);
        } finally {
            closeSession(session);
        }
    }

    // ===== test_realtime_service_live_session_update =====
    // Python: model=[gpt-realtime], api_versions=[2025-05-01-preview, 2026-01-01-preview]
    // Two-phase test:
    //   Phase 1: Session without tools -> send audio -> expect no function call in response
    //   Phase 2: New session with tools -> send audio -> expect function call
    // Uses azure-speech + ServerVad, audio=ask_weather.wav

    static Stream<Arguments> liveSessionUpdateParams() {
        return crossProduct(new String[] { MODEL_GPT_4O_REALTIME },
            new String[] { API_VERSION_2025_05_01_PREVIEW, API_VERSION_PREVIEW });
    }

    @ParameterizedTest
    @MethodSource("liveSessionUpdateParams")
    @LiveOnly
    public void testRealtimeServiceLiveSessionUpdate(String model, String apiVersion)
        throws InterruptedException, IOException {

        VoiceLiveAsyncClient client = createClient(apiVersion);
        byte[] audioData = loadAudioFile("ask_weather.wav");

        // Build tool: get_weather (used in phase 2)
        VoiceLiveFunctionDefinition weatherTool = new VoiceLiveFunctionDefinition("get_weather");
        weatherTool.setDescription("Get the weather for a given location.");
        weatherTool.setParameters(createFunctionParameters("location"));

        // Single session for both phases (matching Python: session.update on same connection)
        VoiceLiveSessionAsyncClient session = null;
        try {
            session = client.startSession(model).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            // Phase tracking: 1 = no tools, 2 = with tools, 3 = post-response.create
            AtomicInteger phase = new AtomicInteger(1);
            AtomicInteger phase1TranscriptCount = new AtomicInteger(0);
            CountDownLatch phase1Latch = new CountDownLatch(1);
            CountDownLatch phase1ResponseDoneLatch = new CountDownLatch(1);
            AtomicReference<SessionUpdateResponseFunctionCallArgumentsDone> phase2FunctionDone
                = new AtomicReference<>();
            CountDownLatch phase2Latch = new CountDownLatch(1);
            // Wait for phase 2 response to fully complete before transitioning to phase 3
            AtomicBoolean phase2FunctionCallSeen = new AtomicBoolean(false);
            CountDownLatch phase2ResponseDoneLatch = new CountDownLatch(1);
            AtomicInteger phase3TranscriptCount = new AtomicInteger(0);
            CountDownLatch phase3Latch = new CountDownLatch(1);

            session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                int currentPhase = phase.get();

                if (eventType == ServerEventType.RESPONSE_AUDIO_TRANSCRIPT_DONE) {
                    if (currentPhase == 1) {
                        phase1TranscriptCount.incrementAndGet();
                        phase1Latch.countDown();
                    } else if (currentPhase == 3) {
                        phase3TranscriptCount.incrementAndGet();
                    }
                } else if (eventType == ServerEventType.RESPONSE_DONE && currentPhase == 1) {
                    phase1ResponseDoneLatch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_FUNCTION_CALL_ARGUMENTS_DONE && currentPhase == 2) {
                    phase2FunctionDone.set((SessionUpdateResponseFunctionCallArgumentsDone) event);
                    phase2FunctionCallSeen.set(true);
                    phase2Latch.countDown();
                } else if (eventType == ServerEventType.RESPONSE_DONE) {
                    if (currentPhase == 2 && phase2FunctionCallSeen.get()) {
                        phase2ResponseDoneLatch.countDown();
                    } else if (currentPhase == 3) {
                        phase3Latch.countDown();
                    }
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    phase1Latch.countDown();
                    phase1ResponseDoneLatch.countDown();
                    phase2Latch.countDown();
                    phase2ResponseDoneLatch.countDown();
                    phase3Latch.countDown();
                }
            }, error -> {
                System.err.println("Session error: " + error.getMessage());
                phase1Latch.countDown();
                phase1ResponseDoneLatch.countDown();
                phase2Latch.countDown();
                phase2ResponseDoneLatch.countDown();
                phase3Latch.countDown();
            });

            waitForSetup();

            // ---- Phase 1: Session WITHOUT tools ----
            VoiceLiveSessionOptions sessionOptionsNoTools = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant that can answer questions.")
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AvaMultilingualNeural")))
                .setInputAudioTranscription(
                    new AudioInputTranscriptionOptions(AudioInputTranscriptionOptionsModel.AZURE_SPEECH))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            session.sendEvent(new ClientEventSessionUpdate(sessionOptionsNoTools)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean phase1Done = phase1Latch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(phase1Done, "Phase 1: Should receive audio transcript done event");

            // Wait for Phase 1 response to FULLY complete before transitioning.
            // AUDIO_TRANSCRIPT_DONE fires before RESPONSE_DONE; sending new audio before
            // RESPONSE_DONE causes the server to reject the next VAD-triggered response
            // with conversation_already_has_active_response.
            phase1ResponseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertEquals(1, phase1TranscriptCount.get(),
                "Phase 1: Should have exactly 1 transcript (speech response, no function call)");

            // ---- Phase 2: Session update WITH tools (same session) ----
            phase.set(2);

            VoiceLiveSessionOptions sessionOptionsWithTools = new VoiceLiveSessionOptions()
                .setInstructions("You are a helpful assistant with tools.")
                .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-AvaMultilingualNeural")))
                .setTools(Arrays.asList(weatherTool))
                .setToolChoice(BinaryData.fromObject("auto"))
                .setTurnDetection(
                    new ServerVadTurnDetection().setThreshold(0.5).setPrefixPaddingMs(300).setSilenceDurationMs(200));

            session.sendEvent(new ClientEventSessionUpdate(sessionOptionsWithTools)).block(SEND_TIMEOUT);
            waitForSetup();

            session.sendInputAudio(audioData).block(SEND_TIMEOUT);
            session.sendInputAudio(getTrailingSilenceBytes()).block(SEND_TIMEOUT);

            boolean phase2Done = phase2Latch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(phase2Done, "Phase 2: Should receive function call after adding tools");
            Assertions.assertNotNull(phase2FunctionDone.get(),
                "Phase 2: Should have function call result after adding tools");
            Assertions.assertEquals("get_weather", phase2FunctionDone.get().getName(),
                "Phase 2: Function name should be get_weather");

            // Verify arguments contain Beijing
            String normalized = phase2FunctionDone.get().getArguments().replace(" ", "").replace("\n", "");
            boolean matchesBeijing
                = "{\"location\":\"北京\"}".equals(normalized) || "{\"location\":\"Beijing\"}".equals(normalized);
            Assertions.assertTrue(matchesBeijing,
                "Phase 2: Arguments should contain Beijing, got: " + phase2FunctionDone.get().getArguments());

            // Wait for Phase 2 response to fully complete before transitioning
            boolean phase2ResponseCompleted = phase2ResponseDoneLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(phase2ResponseCompleted,
                "Phase 2: Response did not complete before timeout; cannot safely proceed to Phase 3");

            // Phase 3: Create response after function call (matching Python)
            phase.set(3);
            session.sendEvent(new ClientEventResponseCreate()).block(SEND_TIMEOUT);

            boolean phase3Done = phase3Latch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(phase3Done, "Phase 3: Should receive response done after response.create");
            Assertions.assertTrue(phase3TranscriptCount.get() >= 1,
                "Phase 3: Should have at least 1 transcript, got: " + phase3TranscriptCount.get());
        } finally {
            closeSession(session);
        }
    }

    // ===== test_realtime_service_tool_call_no_audio_overlap =====
    // Python: @pytest.mark.skip() - skipped in Python tests

    static Stream<Arguments> toolCallNoAudioOverlapParams() {
        return crossProduct(new String[] { MODEL_GPT_4O_REALTIME },
            new String[] { API_VERSION_GA, API_VERSION_PREVIEW });
    }
}
