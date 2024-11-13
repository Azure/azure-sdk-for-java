package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RealtimeClientTests extends RealtimeClientTestBase {

    private RealtimeClient client;

    @Test
    @Override
    void testAlawSendAudio() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildClient();

        AtomicBoolean sessionCreatedEventFired = new AtomicBoolean(false);
        AtomicBoolean sessionUpdatedEventFired = new AtomicBoolean(false);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);

        client.start();
        client.addOnSessionCreatedEventHandler(sessionCreated -> {
            assertNotNull(sessionCreated);
            sessionCreatedEventFired.set(true);
            client.sendMessage(RealtimeEventHandler.sessionUpdate());
        });

        client.addOnSessionUpdatedEventHandler(sessionUpdated -> {
            assertNotNull(sessionUpdated);
            sessionUpdatedEventFired.set(true);
            FileUtils.sendAudioFile(client, FileUtils.openResourceFile("audio_weather_alaw.wav"));
        });

        client.addOnResponseDoneEventHandler(responseDone -> {
            assertNotNull(responseDone);
            responseDoneEventFired.set(true);
        });

        pause(2000);
        client.stop();

        assertTrue(sessionCreatedEventFired.get());
        assertTrue(sessionUpdatedEventFired.get());
        assertTrue(responseDoneEventFired.get());

    }

    @Override
    void canConfigureSession() {

    }

    @Override
    void textOnly() {

    }

    @Override
    void ItemManipulation() {

    }

    @Override
    void AudioWithTool() {

    }

    @Override
    void canDisableVoiceActivityDetection() {

    }

    @Override
    void badCommandProvidesError() {

    }
}
