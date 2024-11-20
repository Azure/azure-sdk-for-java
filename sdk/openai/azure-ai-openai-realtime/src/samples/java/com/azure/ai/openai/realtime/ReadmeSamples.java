// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionModel;
import com.azure.ai.openai.realtime.models.RealtimeAudioInputTranscriptionSettings;
import com.azure.ai.openai.realtime.models.RealtimeClientEventInputAudioBufferAppend;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDelta;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseAudioTranscriptDone;
import com.azure.ai.openai.realtime.models.RealtimeServerVadTurnDetection;
import com.azure.ai.openai.realtime.models.RealtimeVoice;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import reactor.core.Disposable;
import reactor.core.Disposables;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {

    private RealtimeAsyncClient client;

    public void createSyncClientKeyCredential() {
        // BEGIN: readme-sample-createSyncAzureClientKeyCredential
        RealtimeClient client = new RealtimeClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();
        // END: readme-sample-createSyncAzureClientKeyCredential
    }

    public void createAsyncAzureClientKeyCredential() {
        // BEGIN: readme-sample-createAsyncAzureClientKeyCredential
        RealtimeAsyncClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildAsyncClient();
        // END: readme-sample-createAsyncAzureClientKeyCredential
    }

    public void createSyncNonAzureClientKeyCredential() {
        // BEGIN: readme-sample-createSyncNonAzureClientKeyCredential
        RealtimeClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential("{key}"))
            .buildClient();
        // END: readme-sample-createSyncNonAzureClientKeyCredential
    }

    public void createAsyncNonAzureClientKeyCredential() {
        // BEGIN: readme-sample-createAsyncNonAzureClientKeyCredential
        RealtimeAsyncClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential("{key}"))
            .buildAsyncClient();
        // END: readme-sample-createAsyncNonAzureClientKeyCredential
    }

    public void uploadAudioFile() throws IOException {
        // BEGIN: readme-sample-uploadAudioFile
        RealtimeClient client = new RealtimeClientBuilder()
            .credential(new AzureKeyCredential("{key}"))
            .endpoint("{endpoint}")
            .buildClient();

        String audioFilePath = "{path to audio file}";
        byte[] audioBytes = Files.readAllBytes(Paths.get(audioFilePath));

        client.addOnResponseDoneEventHandler(event -> {
            System.out.println("Response done");
        });

        client.start();
        client.sendMessage(new RealtimeClientEventInputAudioBufferAppend(audioBytes));
        // END: readme-sample-uploadAudioFile
    }

    public void createTextAndAudioSession() {
        // BEGIN: readme-sample-sessionUpdate
        client.sendMessage(new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession()
                        .setVoice(RealtimeVoice.ALLOY)
                        .setTurnDetection(
                                new RealtimeServerVadTurnDetection()
                                        .setThreshold(0.5)
                                        .setPrefixPaddingMs(300)
                                        .setSilenceDurationMs(200)
                        ).setInputAudioTranscription(new RealtimeAudioInputTranscriptionSettings(
                                RealtimeAudioInputTranscriptionModel.WHISPER_1)
                        ).setModalities(Arrays.asList(RealtimeRequestSessionModality.AUDIO, RealtimeRequestSessionModality.TEXT))
        ));
        // END: readme-sample-sessionUpdate
    }

    public void consumeSpecificEventsAsync() {
        // BEGIN: readme-sample-consumeSpecificEventsAsync
        RealtimeAsyncClient client = new RealtimeClientBuilder()
            .credential(new KeyCredential("{key}"))
            .buildAsyncClient();

        Disposable.Composite disposables = Disposables.composite();

        disposables.addAll(Arrays.asList(
                client.getServerEvents()
                        .takeUntil(serverEvent -> serverEvent instanceof RealtimeServerEventResponseAudioDone)
                        .ofType(RealtimeServerEventResponseAudioDelta.class)
                        .subscribe(this::consumeAudioDelta, this::consumeError, this::onAudioResponseCompleted),
                client.getServerEvents()
                        .takeUntil(serverEvent -> serverEvent instanceof RealtimeServerEventResponseAudioTranscriptDone)
                        .ofType(RealtimeServerEventResponseAudioTranscriptDelta.class)
                        .subscribe(this::consumeAudioTranscriptDelta, this::consumeError, this::onAudioResponseTranscriptCompleted)
        ));
        // END: readme-sample-consumeSpecificEventsAsync
    }

    private void onAudioResponseTranscriptCompleted() {
        // no-op
    }

    private void consumeAudioTranscriptDelta(RealtimeServerEventResponseAudioTranscriptDelta realtimeServerEventResponseAudioTranscriptDelta) {
        // no-op
    }

    private void onAudioResponseCompleted() {
        // no-op
    }

    private void consumeError(Throwable throwable) {
        // no-op
    }

    private void consumeAudioDelta(RealtimeServerEventResponseAudioDelta realtimeServerEventResponseAudioDelta) {
        // no-op
    }
}
