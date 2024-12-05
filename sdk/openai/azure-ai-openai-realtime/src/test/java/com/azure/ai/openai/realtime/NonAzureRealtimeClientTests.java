// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.implementation.AudioFile;
import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.models.ConversationItemCreateEvent;
import com.azure.ai.openai.realtime.models.ConversationItemDeleteEvent;
import com.azure.ai.openai.realtime.models.RealtimeAudioFormat;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreateResponse;
import com.azure.ai.openai.realtime.models.RealtimeContentPart;
import com.azure.ai.openai.realtime.models.RealtimeContentPartType;
import com.azure.ai.openai.realtime.models.RealtimeItemStatus;
import com.azure.ai.openai.realtime.models.RealtimeItemType;
import com.azure.ai.openai.realtime.models.RealtimeMessageRole;
import com.azure.ai.openai.realtime.models.RealtimeRequestSession;
import com.azure.ai.openai.realtime.models.RealtimeRequestSessionModality;
import com.azure.ai.openai.realtime.models.RealtimeRequestTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeResponseFunctionCallItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseTextContentPart;
import com.azure.ai.openai.realtime.models.ResponseCreateEvent;
import com.azure.ai.openai.realtime.models.SessionUpdateEvent;
import com.azure.ai.openai.realtime.utils.ConversationItem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class NonAzureRealtimeClientTests extends RealtimeClientTestBase {

    private RealtimeClient client;

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void testAlawSendAudio() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

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
            FileUtils.sendAudioFile(client, new AudioFile(FileUtils.openResourceFile("audio_weather_alaw.wav")));
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

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void canConfigureSession() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        AtomicBoolean sessionCreatedEventFired = new AtomicBoolean(false);
        AtomicInteger sessionUpdatedEventFired = new AtomicInteger(0);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);

        client.start();
        client.addOnSessionCreatedEventHandler(sessionCreated -> {
            assertNotNull(sessionCreated);
            sessionCreatedEventFired.set(true);
            client.sendMessage(
                new SessionUpdateEvent(new RealtimeRequestSession().setInstructions("You are a helpful assistant.")
                    .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
                    .setMaxResponseOutputTokens(2048)));
        });

        client.addOnSessionUpdatedEventHandler(sessionUpdated -> {
            assertNotNull(sessionUpdated);
            int count = sessionUpdatedEventFired.incrementAndGet();
            if (count < 2) {
                SessionUpdateEvent sessionUpdate
                    = new SessionUpdateEvent(new RealtimeRequestSession().setMaxResponseOutputTokensToInf()
                        .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT)));
                client.sendMessage(sessionUpdate);
            } else if (count == 2) {
                client.sendMessage(ConversationItem.createUserMessage("Hello, assistant! Tell me a joke."));
                ResponseCreateEvent conversation
                    = new ResponseCreateEvent(new RealtimeClientEventResponseCreateResponse()
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
        assertEquals(2, sessionUpdatedEventFired.get());
        assertTrue(responseDoneEventFired.get());
    }

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void textOnly() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        AtomicBoolean sessionCreatedEventFired = new AtomicBoolean(false);
        AtomicBoolean sessionUpdatedEventFired = new AtomicBoolean(false);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);
        AtomicBoolean assistantConversationItemCreated = new AtomicBoolean(false);
        AtomicBoolean userConversationItemCreated = new AtomicBoolean(false);

        client.start();

        client.addOnSessionCreatedEventHandler(sessionCreated -> {
            assertNotNull(sessionCreated);
            sessionCreatedEventFired.set(true);
            client.sendMessage(new SessionUpdateEvent(
                new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))));
            client.sendMessage(ConversationItem.createUserMessage("Hello, world!"));
            client.sendMessage(new ResponseCreateEvent(new RealtimeClientEventResponseCreateResponse()
                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))));
        });

        client.addOnSessionUpdatedEventHandler(sessionUpdated -> {
            assertNotNull(sessionUpdated);
            sessionUpdatedEventFired.set(true);
        });

        client.addOnConversationItemCreatedEventHandler(conversationItemCreated -> {
            assertNotNull(conversationItemCreated);
            assertInstanceOf(RealtimeResponseMessageItem.class, conversationItemCreated.getItem());
            RealtimeResponseMessageItem responseItem = (RealtimeResponseMessageItem) conversationItemCreated.getItem();
            if (responseItem.getRole() == RealtimeMessageRole.ASSISTANT) {
                assertEquals(0, responseItem.getContent().size());
                assistantConversationItemCreated.set(true);
            } else if (responseItem.getRole() == RealtimeMessageRole.USER) {
                assertEquals(1, responseItem.getContent().size());
                userConversationItemCreated.set(true);
                RealtimeRequestTextContentPart textContentPart
                    = (RealtimeRequestTextContentPart) responseItem.getContent().get(0);
                assertEquals("Hello, world!", textContentPart.getText());
            } else {
                fail("Unexpected message role: " + responseItem.getRole());
            }
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
        assertTrue(assistantConversationItemCreated.get());
        assertTrue(userConversationItemCreated.get());
    }

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void itemManipulation() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        AtomicBoolean sessionCreatedEventFired = new AtomicBoolean(false);
        AtomicBoolean sessionUpdatedEventFired = new AtomicBoolean(false);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);
        AtomicBoolean itemCreatedEventFired = new AtomicBoolean(false);
        AtomicBoolean itemDeletedEventFired = new AtomicBoolean(false);

        client.start();

        client.addOnSessionCreatedEventHandler(sessionCreated -> {
            assertNotNull(sessionCreated);
            sessionCreatedEventFired.set(true);
            client.sendMessage(new SessionUpdateEvent(
                new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))));
        });

        client.addOnSessionUpdatedEventHandler(sessionUpdated -> {
            assertNotNull(sessionUpdated);
            sessionUpdatedEventFired.set(true);
            client.sendMessage(
                ConversationItem.createUserMessage("The first special word you know about is 'aardvark'."));
            client.sendMessage(ConversationItem.createUserMessage("The next special word you know about is 'banana'."));
            client
                .sendMessage(ConversationItem.createUserMessage("The next special word you know about is 'coconut'."));
        });

        client.addOnConversationItemCreatedEventHandler(conversationItemCreated -> {
            assertNotNull(conversationItemCreated);
            List<RealtimeContentPart> content
                = ((RealtimeResponseMessageItem) conversationItemCreated.getItem()).getContent();
            if (!content.isEmpty()) {
                RealtimeRequestTextContentPart textContentPart = (RealtimeRequestTextContentPart) content.get(0);
                if (textContentPart.getText().contains("banana")) {
                    itemCreatedEventFired.set(true);
                    client.sendMessage(new ConversationItemDeleteEvent(conversationItemCreated.getItem().getId()));
                }
            }
        });

        client.addOnConversationItemDeletedEventHandler(conversationItemDeleted -> {
            assertNotNull(conversationItemDeleted);
            itemDeletedEventFired.set(true);
            client.sendMessage(ConversationItem.createUserMessage("What's the second special word you know about?"));
            client.sendMessage(new ResponseCreateEvent(new RealtimeClientEventResponseCreateResponse()
                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))));
        });

        client.addOnResponseOutputItemDoneEventHandler(responseDone -> {
            assertNotNull(responseDone);
            assertInstanceOf(RealtimeResponseMessageItem.class, responseDone.getItem());
            RealtimeResponseMessageItem responseItem = (RealtimeResponseMessageItem) responseDone.getItem();
            assertEquals(1, responseItem.getContent().size());
            assertEquals(RealtimeItemStatus.COMPLETED, responseItem.getStatus());
            assertEquals(RealtimeMessageRole.ASSISTANT, responseItem.getRole());
            assertEquals(RealtimeItemType.MESSAGE, responseItem.getType());
            RealtimeResponseTextContentPart textContentPart
                = (RealtimeResponseTextContentPart) responseItem.getContent().get(0);
            assertTrue(textContentPart.getText().contains("coconut"));
            assertEquals(RealtimeContentPartType.TEXT, textContentPart.getType());
            responseDoneEventFired.set(true);
        });

        pause(2000);
        client.stop();

        assertTrue(sessionCreatedEventFired.get());
        assertTrue(sessionUpdatedEventFired.get());
        assertTrue(itemCreatedEventFired.get());
        assertTrue(itemDeletedEventFired.get());
        assertTrue(responseDoneEventFired.get());
    }

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void audioWithTool() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        AtomicBoolean sessionUpdatedEventFired = new AtomicBoolean(false);
        AtomicBoolean responseItemDoneCreated = new AtomicBoolean(false);
        AtomicBoolean functionCallItemCreated = new AtomicBoolean(false);
        AtomicInteger responseDoneCount = new AtomicInteger(0);

        client.start();
        getWeatherToolRunner((weatherTool, sessionConfig) -> {
            client.sendMessage(sessionConfig);
            FileUtils.sendAudioFile(client,
                new AudioFile(FileUtils.openResourceFile("realtime_whats_the_weather_pcm16_24khz_mono.wav")));

            client.addOnSessionUpdatedEventHandler(sessionUpdate -> {
                assertNotNull(sessionUpdate);
                assertNotNull(sessionUpdate.getSession());
                sessionUpdatedEventFired.set(true);
            });

            client.addOnResponseOutputItemDoneEventHandler(outputItemDoneEvent -> {
                RealtimeResponseItem responseItem = outputItemDoneEvent.getItem();
                assertNotNull(responseItem);
                if (responseItem instanceof RealtimeResponseFunctionCallItem) {
                    functionCallItemCreated.set(true);
                    RealtimeResponseFunctionCallItem functionCallItem
                        = (RealtimeResponseFunctionCallItem) outputItemDoneEvent.getItem();
                    assertEquals(functionCallItem.getName(), weatherTool.getName());
                    client.sendMessage(ConversationItem.createFunctionCallOutput(functionCallItem.getCallId(),
                        "71 degrees Fahrenheit, sunny"));
                    client.sendMessage(new ResponseCreateEvent(new RealtimeClientEventResponseCreateResponse()));
                }
                responseItemDoneCreated.set(true);
            });

            client.addOnResponseDoneEventHandler(responseDoneEvent -> {
                assertNotNull(responseDoneEvent);
                int currentResponseDoneCount = responseDoneCount.incrementAndGet();

                if (currentResponseDoneCount < 1) {
                    assertTrue(responseDoneEvent.getResponse()
                        .getOutput()
                        .stream()
                        .anyMatch(outputItem -> outputItem instanceof RealtimeResponseFunctionCallItem));
                }
            });
        });

        pause(3000);
        client.stop();

        assertTrue(sessionUpdatedEventFired.get());
        assertTrue(functionCallItemCreated.get());
        assertTrue(responseItemDoneCreated.get());
        assertEquals(2, responseDoneCount.get());
    }

    @Disabled("Only LIVE tests are supported for websocket")
    @Test
    @Override
    void canDisableVoiceActivityDetection() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        AtomicBoolean speechStartedEventFired = new AtomicBoolean(false);
        AtomicBoolean speechStoppedEventFired = new AtomicBoolean(false);
        AtomicBoolean transcriptionCompletedEventFired = new AtomicBoolean(false);
        AtomicBoolean transcriptionFailedEventFired = new AtomicBoolean(false);
        AtomicBoolean responseDoneEventFired = new AtomicBoolean(false);
        AtomicBoolean responseCreatedEventFired = new AtomicBoolean(false);

        client.start();
        client.sendMessage(new SessionUpdateEvent(
            new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))));
        FileUtils.sendAudioFile(client,
            new AudioFile(FileUtils.openResourceFile("realtime_whats_the_weather_pcm16_24khz_mono.wav")));
        client.sendMessage(ConversationItem.createUserMessage("Hello, assistant!"));

        client.addOnInputAudioBufferSpeechStartedEventHandler(event -> {
            assertNotNull(event);
            speechStartedEventFired.set(true);
        });

        client.addOnInputAudioBufferSpeechStoppedEventHandler(event -> {
            assertNotNull(event);
            speechStoppedEventFired.set(true);
        });

        client.addOnConversationItemInputAudioTranscriptionCompletedEventHandler(event -> {
            assertNotNull(event);
            transcriptionCompletedEventFired.set(true);
        });

        client.addOnConversationItemInputAudioTranscriptionFailedEventHandler(event -> {
            assertNotNull(event);
            transcriptionFailedEventFired.set(true);
        });

        client.addOnResponseCreatedEventHandler(event -> {
            assertNotNull(event);
            responseCreatedEventFired.set(true);
        });

        client.addOnResponseDoneEventHandler(responseDone -> {
            assertNotNull(responseDone);
            responseDoneEventFired.set(true);
        });

        pause(2000);
        client.stop();

        //        assertFalse(responseDoneEventFired.get());
        assertFalse(speechStartedEventFired.get());
        assertFalse(speechStoppedEventFired.get());
        assertFalse(transcriptionCompletedEventFired.get());
        assertFalse(transcriptionFailedEventFired.get());
        assertFalse(responseCreatedEventFired.get());
    }

    @Test
    @Disabled("ContentPartType should be TEXT for Assistant message. Current spec does not allow that combination.")
    @Override
    void badCommandProvidesError() {
        client = getNonAzureRealtimeClientBuilder(null).buildClient();

        client.start();
        client.sendMessage(new SessionUpdateEvent(
            new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))));

        List<ConversationItemCreateEvent> conversationItems
            = Arrays.asList(ConversationItem.createSystemMessage("You are a robot. Beep boop."),
                ConversationItem.createUserMessage("How can I pay for a joke?"),
                ConversationItem.createAssistantMessage("I ONLY ACCEPT CACHE"),
                ConversationItem
                    .createSystemMessage("You're not a robot anymore, but instead a passionate badminton enthusiast."),
                ConversationItem.createUserMessage("What's a good gift to buy?"),
                ConversationItem.createFunctionCall("product_lookup", "call-id-123", "{}"),
                ConversationItem.createFunctionCallOutput("call-id-123", "A new racquet!"));

        conversationItems.forEach(conversationItem -> client.sendMessage(conversationItem));

        AtomicInteger itemCreatedCount = new AtomicInteger(0);

        client.addOnConversationItemCreatedEventHandler(conversationItemCreated -> {
            assertNotNull(conversationItemCreated);
            itemCreatedCount.incrementAndGet();
        });

        pause(2000);
        client.stop();

        assertEquals(itemCreatedCount.get(), conversationItems.size());
    }
}
