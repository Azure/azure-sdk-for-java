package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.models.RealtimeAudioFormat;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreate;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreateResponse;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeTurnDetectionDisabled;
import com.azure.ai.openai.realtime.utils.ConversationItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    @Override
    void canConfigureSession() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildClient();

        AtomicBoolean sessionCreatedEventFired = new AtomicBoolean(false);
        AtomicInteger sessionUpdatedEventFired = new AtomicInteger(0);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);

        client.start();
        client.addOnSessionCreatedEventHandler(sessionCreated -> {
            assertNotNull(sessionCreated);
            sessionCreatedEventFired.set(true);
            client.sendMessage(
                    new RealtimeClientEventSessionUpdate(
                            new RealtimeRequestSession()
                                    .setInstructions("You are a helpful assistant.")
                                    .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
                                    .setTurnDetection(new RealtimeTurnDetectionDisabled())
                                    .setMaxResponseOutputTokens(2048)
                    )
            );
        });

        client.addOnSessionUpdatedEventHandler(sessionUpdated -> {
            assertNotNull(sessionUpdated);
            int count = sessionUpdatedEventFired.incrementAndGet();
            if(count < 2) {
                RealtimeClientEventSessionUpdate sessionUpdate = new RealtimeClientEventSessionUpdate(
                        new RealtimeRequestSession()
                                .setMaxResponseOutputTokensToInf()
                                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
                );
                client.sendMessage(sessionUpdate);
            } else if (count == 2) {
                client.sendMessage(ConversationItem.createUserMessage("Hello, assistant! Tell me a joke."));
                RealtimeClientEventResponseCreate conversation
                        = new RealtimeClientEventResponseCreate(new RealtimeClientEventResponseCreateResponse()
                        .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString())));
                client.sendMessage(conversation);
            }
        });

        client.addOnResponseDoneEventHandler(responseDone -> {
            assertNotNull(responseDone);
            responseDoneEventFired.set(true);
        });

        pause(2000);
        client.stop();

        assertTrue(sessionCreatedEventFired.get());
        assertEquals(2 , sessionUpdatedEventFired.get());
        assertTrue(responseDoneEventFired.get());
    }

    @Test
    @Override
    void textOnly() {

    }

    @Test
    @Override
    void ItemManipulation() {

    }

    @Test
    @Override
    void AudioWithTool() {

    }

    @Test
    @Override
    void canDisableVoiceActivityDetection() {

    }

    @Test
    @Override
    void badCommandProvidesError() {

    }
}
