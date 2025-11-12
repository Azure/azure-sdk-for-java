// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.AudioEchoCancellation;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptions;
import com.azure.ai.voicelive.models.AudioInputTranscriptionOptionsModel;
import com.azure.ai.voicelive.models.AudioNoiseReduction;
import com.azure.ai.voicelive.models.AudioNoiseReductionType;
import com.azure.ai.voicelive.models.AzureCustomVoice;
import com.azure.ai.voicelive.models.AzurePersonalVoice;
import com.azure.ai.voicelive.models.AzureStandardVoice;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.PersonalVoiceModels;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.ServerVadTurnDetection;
import com.azure.ai.voicelive.models.SessionUpdate;
import com.azure.ai.voicelive.models.SessionUpdateError;
import com.azure.ai.voicelive.models.SessionUpdateResponseAudioDelta;
import com.azure.ai.voicelive.models.SessionUpdateSessionUpdated;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Code samples for the README.md
 */
public final class ReadmeSamples {

    private String endpoint = "https://your-resource.openai.azure.com/";
    private String apiKey = "your-api-key";

    /**
     * Complete voice assistant sample from README
     */
    public void readmeSamples() {
        // BEGIN: com.azure.ai.voicelive.readme
        String endpoint = System.getenv("AZURE_VOICELIVE_ENDPOINT");
        String apiKey = System.getenv("AZURE_VOICELIVE_API_KEY");

        // Create the VoiceLive client
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        // Configure session options for voice conversation
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
            .setThreshold(0.5)
            .setPrefixPaddingMs(300)
            .setSilenceDurationMs(500)
            .setInterruptResponse(true)
            .setAutoTruncate(true)
            .setCreateResponse(true);

        AudioInputTranscriptionOptions transcriptionOptions = new AudioInputTranscriptionOptions(
            AudioInputTranscriptionOptionsModel.WHISPER_1);

        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI voice assistant.")
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(24000)
            .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setInputAudioTranscription(transcriptionOptions)
            .setTurnDetection(turnDetection);

        // Start session and handle events
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                // Subscribe to receive server events
                session.receiveEvents()
                    .subscribe(
                        event -> handleEvent(event),
                        error -> System.err.println("Error: " + error.getMessage())
                    );

                // Send session configuration
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
                return session.sendEvent(updateEvent).then(Mono.just(session));
            })
            .block();
        // END: com.azure.ai.voicelive.readme
    }

    /**
     * Sample for authenticating with API key
     */
    public void authenticationApiKey() {
        // BEGIN: com.azure.ai.voicelive.authentication.apikey
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint("https://your-resource.openai.azure.com/")
            .credential(new AzureKeyCredential("your-api-key"))
            .buildAsyncClient();
        // END: com.azure.ai.voicelive.authentication.apikey
    }

    /**
     * Sample for authenticating with DefaultAzureCredential
     */
    public void authenticationDefaultCredential() {
        // BEGIN: com.azure.ai.voicelive.authentication.defaultcredential
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint("https://your-resource.openai.azure.com/")
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.ai.voicelive.authentication.defaultcredential
    }

    /**
     * Sample for authenticating with Azure CLI
     */
    public void authenticationAzureCli() {
        // BEGIN: com.azure.ai.voicelive.authentication.azurecli
        TokenCredential credential = new AzureCliCredentialBuilder().build();
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint("https://your-resource.openai.azure.com/")
            .credential(credential)
            .buildAsyncClient();
        // END: com.azure.ai.voicelive.authentication.azurecli
    }

    /**
     * Sample for simple voice assistant session
     */
    public void simpleSession() {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        // BEGIN: com.azure.ai.voicelive.simple.session
        // Start session with default options
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                System.out.println("Session started");

                // Subscribe to receive events
                session.receiveEvents()
                    .subscribe(
                        event -> System.out.println("Event: " + event.getType()),
                        error -> System.err.println("Error: " + error.getMessage())
                    );

                return Mono.just(session);
            })
            .block();
        // END: com.azure.ai.voicelive.simple.session
    }

    /**
     * Sample for configuring session options
     */
    public void configureSessionOptions() {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        // BEGIN: com.azure.ai.voicelive.configure.sessionoptions
        // Configure server-side voice activity detection
        ServerVadTurnDetection turnDetection = new ServerVadTurnDetection()
            .setThreshold(0.5)                    // Sensitivity threshold (0.0-1.0)
            .setPrefixPaddingMs(300)              // Audio before speech detection
            .setSilenceDurationMs(500)            // Silence to end turn
            .setInterruptResponse(true)           // Allow user interruptions
            .setAutoTruncate(true)                // Auto-truncate on interruption
            .setCreateResponse(true);             // Auto-create response after turn

        // Configure input audio transcription
        AudioInputTranscriptionOptions transcription = new AudioInputTranscriptionOptions(
            AudioInputTranscriptionOptionsModel.WHISPER_1);

        // Create session options
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setInstructions("You are a helpful AI voice assistant. Respond naturally and conversationally.")
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)))
            .setModalities(Arrays.asList(InteractionModality.TEXT, InteractionModality.AUDIO))
            .setInputAudioFormat(InputAudioFormat.PCM16)
            .setOutputAudioFormat(OutputAudioFormat.PCM16)
            .setInputAudioSamplingRate(24000)
            .setInputAudioNoiseReduction(new AudioNoiseReduction(AudioNoiseReductionType.NEAR_FIELD))
            .setInputAudioEchoCancellation(new AudioEchoCancellation())
            .setInputAudioTranscription(transcription)
            .setTurnDetection(turnDetection);

        // Start session with options
        client.startSession("gpt-4o-realtime-preview")
            .flatMap(session -> {
                // Send session configuration
                ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(options);
                return session.sendEvent(updateEvent).then(Mono.just(session));
            })
            .subscribe(
                session -> System.out.println("Session configured"),
                error -> System.err.println("Error: " + error.getMessage())
            );
        // END: com.azure.ai.voicelive.configure.sessionoptions
    }

    /**
     * Sample for sending audio input
     */
    public void sendAudioInput() throws IOException {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        VoiceLiveSessionAsyncClient session = client.startSession("gpt-4o-realtime-preview").block();

        // BEGIN: com.azure.ai.voicelive.send.audioinput
        // Send audio chunk
        byte[] audioData = readAudioChunk(); // Your audio data in PCM16 format
        session.sendInputAudio(BinaryData.fromBytes(audioData))
            .subscribe();

        // Send audio from file
        try {
            Path audioFile = Paths.get("audio.wav");
            byte[] fileData = Files.readAllBytes(audioFile);
            session.sendInputAudio(BinaryData.fromBytes(fileData))
                .subscribe();
        } catch (IOException e) {
            System.err.println("Error reading audio file: " + e.getMessage());
        }
        // END: com.azure.ai.voicelive.send.audioinput
    }

    /**
     * Sample for handling event types
     */
    public void handleEventTypes() {
        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildAsyncClient();

        VoiceLiveSessionAsyncClient session = client.startSession("gpt-4o-realtime-preview").block();

        // BEGIN: com.azure.ai.voicelive.handle.eventtypes
        session.receiveEvents()
            .subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (ServerEventType.SESSION_CREATED.equals(eventType)) {
                    System.out.println("✓ Session created - ready to start");
                } else if (ServerEventType.SESSION_UPDATED.equals(eventType)) {
                    System.out.println("✓ Session configured - starting conversation");
                    if (event instanceof SessionUpdateSessionUpdated) {
                        SessionUpdateSessionUpdated updated = (SessionUpdateSessionUpdated) event;
                        // Access session configuration details
                        String json = BinaryData.fromObject(updated).toString();
                        System.out.println("Config: " + json);
                    }
                } else if (ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STARTED.equals(eventType)) {
                    System.out.println("🎤 User started speaking");
                } else if (ServerEventType.INPUT_AUDIO_BUFFER_SPEECH_STOPPED.equals(eventType)) {
                    System.out.println("🤔 User stopped speaking - processing...");
                } else if (ServerEventType.RESPONSE_AUDIO_DELTA.equals(eventType)) {
                    // Play audio response
                    if (event instanceof SessionUpdateResponseAudioDelta) {
                        SessionUpdateResponseAudioDelta audioEvent =
                            (SessionUpdateResponseAudioDelta) event;
                        playAudioChunk(audioEvent.getDelta());
                    }
                } else if (ServerEventType.RESPONSE_AUDIO_DONE.equals(eventType)) {
                    System.out.println("🔊 Assistant finished speaking");
                } else if (ServerEventType.RESPONSE_DONE.equals(eventType)) {
                    System.out.println("✅ Response complete - ready for next input");
                } else if (ServerEventType.ERROR.equals(eventType)) {
                    if (event instanceof SessionUpdateError) {
                        SessionUpdateError errorEvent = (SessionUpdateError) event;
                        System.err.println("❌ Error: "
                            + errorEvent.getError().getMessage());
                    }
                }
            });
        // END: com.azure.ai.voicelive.handle.eventtypes
    }

    /**
     * Sample for OpenAI voice configuration
     */
    public void voiceConfigurationOpenAI() {
        // BEGIN: com.azure.ai.voicelive.voice.openai
        // Use OpenAIVoiceName enum for available voices (ALLOY, ASH, BALLAD, CORAL, ECHO, SAGE, SHIMMER, VERSE)
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)));
        // END: com.azure.ai.voicelive.voice.openai
    }

    /**
     * Sample for Azure voice configuration
     */
    public void voiceConfigurationAzure() {
        // BEGIN: com.azure.ai.voicelive.voice.azure
        // Azure Standard Voice - use any Azure TTS voice name
        // See: https://learn.microsoft.com/azure/ai-services/speech-service/language-support?tabs=tts
        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions()
            .setVoice(BinaryData.fromObject(new AzureStandardVoice("en-US-JennyNeural")));

        // Azure Custom Voice - requires custom voice name and endpoint ID
        VoiceLiveSessionOptions options2 = new VoiceLiveSessionOptions()
            .setVoice(BinaryData.fromObject(new AzureCustomVoice("myCustomVoice", "myEndpointId")));

        // Azure Personal Voice - requires speaker profile ID and model
        // Models: DRAGON_LATEST_NEURAL, PHOENIX_LATEST_NEURAL, PHOENIX_V2NEURAL
        VoiceLiveSessionOptions options3 = new VoiceLiveSessionOptions()
            .setVoice(BinaryData.fromObject(
                new AzurePersonalVoice("speakerProfileId", PersonalVoiceModels.PHOENIX_LATEST_NEURAL)));
        // END: com.azure.ai.voicelive.voice.azure
    }

    // Helper methods
    private byte[] readAudioChunk() {
        return new byte[0];
    }

    private void playAudioChunk(byte[] delta) {
        // Implementation for playing audio
    }

    private void handleEvent(SessionUpdate event) {
        // Implementation for handling events
    }
}
