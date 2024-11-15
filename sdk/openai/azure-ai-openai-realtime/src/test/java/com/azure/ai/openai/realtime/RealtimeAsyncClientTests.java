// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.realtime;

import com.azure.ai.openai.realtime.models.RealtimeAudioFormat;
import com.azure.ai.openai.realtime.models.RealtimeClientEventConversationItemCreate;
import com.azure.ai.openai.realtime.models.RealtimeClientEventConversationItemDelete;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreate;
import com.azure.ai.openai.realtime.models.RealtimeClientEventResponseCreateResponse;
import com.azure.ai.openai.realtime.models.RealtimeClientEventSessionUpdate;
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
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemInputAudioTranscriptionCompleted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemInputAudioTranscriptionFailed;
import com.azure.ai.openai.realtime.models.RealtimeServerEventError;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferSpeechStarted;
import com.azure.ai.openai.realtime.models.RealtimeServerEventInputAudioBufferSpeechStopped;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseOutputItemDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionUpdated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.ai.openai.realtime.models.RealtimeTurnDetectionDisabled;
import com.azure.ai.openai.realtime.implementation.FileUtils;
import com.azure.ai.openai.realtime.implementation.RealtimeEventHandler;
import com.azure.ai.openai.realtime.utils.ConversationItem;
import com.azure.core.util.CoreUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RealtimeAsyncClientTests extends RealtimeClientTestBase {

    private RealtimeAsyncClient client;

    @Test
    @Override
    public void testAlawSendAudio() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents()).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
        }).then(() -> {
            client.sendMessage(RealtimeEventHandler.sessionUpdate()).block();
        }).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
        })
            .then(() -> FileUtils.sendAudioFileAsync(client, FileUtils.openResourceFile("audio_weather_alaw.wav"))
                .block())
            .thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                Assertions::assertNotNull)
            .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
            .then(() -> client.stop().block())
            .verifyComplete();
    }

    @Test
    @Override
    void canConfigureSession() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents()).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
            client.sendMessage(new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession().setInstructions("You are a helpful assistant.")
                    .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
                    .setTurnDetection(new RealtimeTurnDetectionDisabled())
                    .setMaxResponseOutputTokens(2048)))
                .block();
        }).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
            RealtimeClientEventSessionUpdate sessionUpdate
                = new RealtimeClientEventSessionUpdate(new RealtimeRequestSession().setMaxResponseOutputTokensToInf()
                    .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT)));
            client.sendMessage(sessionUpdate).block();
        }).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
            // send prompt
            client.sendMessage(ConversationItem.createUserMessage("Hello, assistant! Tell me a joke.")).block();

            // starting conversation - needs to be submitted after the prompt, otherwise it will be ignored
            RealtimeClientEventResponseCreate conversation
                = new RealtimeClientEventResponseCreate(new RealtimeClientEventResponseCreateResponse()
                    .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString())));
            client.sendMessage(conversation).block();
        })
            .thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                Assertions::assertNotNull)
            .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
            .then(() -> client.stop().block())
            .verifyComplete();
    }

    @Test
    @Override
    void textOnly() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();
        client.sendMessage(new RealtimeClientEventSessionUpdate(
            new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
                .setTurnDetection(new RealtimeTurnDetectionDisabled())))
            .block();
        StepVerifier.create(client.getServerEvents()).assertNext(event -> {
            assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
            client.sendMessage(ConversationItem.createUserMessage("Hello, world!")).block();
            client.sendMessage(new RealtimeClientEventResponseCreate(new RealtimeClientEventResponseCreateResponse()
                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString())))).block();
        }).thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE, event -> {
            if (event instanceof RealtimeServerEventSessionUpdated) {
                RealtimeServerEventSessionUpdated sessionUpdatedEvent = (RealtimeServerEventSessionUpdated) event;
                assertNotNull(sessionUpdatedEvent.getSession());
            } else if (event instanceof RealtimeServerEventConversationItemCreated) {
                RealtimeServerEventConversationItemCreated itemCreatedEvent
                    = (RealtimeServerEventConversationItemCreated) event;
                assertInstanceOf(RealtimeResponseMessageItem.class, itemCreatedEvent.getItem());
                RealtimeResponseMessageItem responseItem = (RealtimeResponseMessageItem) itemCreatedEvent.getItem();
                if (responseItem.getRole() == RealtimeMessageRole.ASSISTANT) {
                    assertEquals(0, responseItem.getContent().size());
                } else if (responseItem.getRole() == RealtimeMessageRole.USER) {
                    assertEquals(1, responseItem.getContent().size());
                    RealtimeRequestTextContentPart textContentPart
                        = (RealtimeRequestTextContentPart) responseItem.getContent().get(0);
                    assertEquals("Hello, world!", textContentPart.getText());
                } else {
                    fail("Unexpected message role: " + responseItem.getRole());
                }
            }
        })
            .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
            .then(() -> client.stop().block())
            .verifyComplete();
    }

    @Test
    @Override
    void ItemManipulation() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();
        client
            .sendMessage(new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
                    .setTurnDetection(new RealtimeTurnDetectionDisabled())))
            .then(client.sendMessage(
                ConversationItem.createUserMessage("The first special word you know about is 'aardvark'.")))
            .then(client
                .sendMessage(ConversationItem.createUserMessage("The next special word you know about is 'banana'.")))
            .then(client
                .sendMessage(ConversationItem.createUserMessage("The next special word you know about is 'coconut'.")))
            .block();

        StepVerifier.create(client.getServerEvents())
            .thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.ERROR
                && event.getType() != RealtimeServerEventType.RESPONSE_DONE, event -> {
                    if (event instanceof RealtimeServerEventConversationItemCreated) {
                        RealtimeServerEventConversationItemCreated itemCreatedEvent
                            = (RealtimeServerEventConversationItemCreated) event;
                        List<RealtimeContentPart> contentParts
                            = ((RealtimeResponseMessageItem) itemCreatedEvent.getItem()).getContent();
                        if (contentParts.isEmpty()) {
                            return;
                        }
                        RealtimeRequestTextContentPart textContentPart
                            = (RealtimeRequestTextContentPart) contentParts.get(0);
                        if (textContentPart.getText().contains("banana")) {
                            client
                                .sendMessage(
                                    new RealtimeClientEventConversationItemDelete(itemCreatedEvent.getItem().getId()))
                                .block();
                            client
                                .sendMessage(ConversationItem
                                    .createUserMessage("What's the second special word you know about?"))
                                .block();
                            client
                                .sendMessage(new RealtimeClientEventResponseCreate(
                                    new RealtimeClientEventResponseCreateResponse()
                                        .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))))
                                .block();
                        }
                    } else if (event instanceof RealtimeServerEventResponseOutputItemDone) {
                        RealtimeServerEventResponseOutputItemDone outputItemDoneEvent
                            = (RealtimeServerEventResponseOutputItemDone) event;
                        assertInstanceOf(RealtimeResponseMessageItem.class, outputItemDoneEvent.getItem());
                        RealtimeResponseMessageItem responseItem
                            = (RealtimeResponseMessageItem) outputItemDoneEvent.getItem();
                        assertEquals(1, responseItem.getContent().size());
                        assertEquals(RealtimeItemStatus.COMPLETED, responseItem.getStatus());
                        assertEquals(RealtimeMessageRole.ASSISTANT, responseItem.getRole());
                        assertEquals(RealtimeItemType.MESSAGE, responseItem.getType());
                        RealtimeResponseTextContentPart textContentPart
                            = (RealtimeResponseTextContentPart) responseItem.getContent().get(0);
                        assertTrue(textContentPart.getText().contains("coconut"));
                        assertEquals(RealtimeContentPartType.TEXT, textContentPart.getType());
                    }
                })
            .thenRequest(1)
            .then(() -> client.stop().block())
            .verifyComplete();
    }

    @Test
    @Override
    void AudioWithTool() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();

        AtomicInteger responseDoneCount = new AtomicInteger(0);
        getWeatherToolRunner((weatherTool, sessionConfig) -> {
            client.sendMessage(sessionConfig).block();
            FileUtils
                .sendAudioFileAsync(client,
                    FileUtils.openResourceFile("realtime_whats_the_weather_pcm16_24khz_mono.wav"))
                .block();
            StepVerifier.create(client.getServerEvents())
                .thenConsumeWhile(
                    event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE || responseDoneCount.get() < 1, // we break on the 2nd RESPONSE_DONE
                    event -> {
                        assertFalse(CoreUtils.isNullOrEmpty(event.getEventId()));
                        if (event instanceof RealtimeServerEventSessionUpdated) {
                            RealtimeServerEventSessionUpdated sessionUpdatedEvent
                                = (RealtimeServerEventSessionUpdated) event;
                            assertNotNull(sessionUpdatedEvent.getSession());
                        } else if (event instanceof RealtimeServerEventResponseOutputItemDone) {
                            RealtimeServerEventResponseOutputItemDone outputItemDoneEvent
                                = (RealtimeServerEventResponseOutputItemDone) event;
                            RealtimeResponseItem responseItem = outputItemDoneEvent.getItem();
                            assertNotNull(responseItem);
                            if (responseItem instanceof RealtimeResponseFunctionCallItem) {
                                RealtimeResponseFunctionCallItem functionCallItem
                                    = (RealtimeResponseFunctionCallItem) outputItemDoneEvent.getItem();
                                assertEquals(functionCallItem.getName(), weatherTool.getName());
                                client
                                    .sendMessage(ConversationItem.createFunctionCallOutput(functionCallItem.getCallId(),
                                        "71 degrees Fahrenheit, sunny"))
                                    .block();
                                client.sendMessage(new RealtimeClientEventResponseCreate(
                                    new RealtimeClientEventResponseCreateResponse())).block();
                            }
                        } else if (event instanceof RealtimeServerEventResponseDone) {
                            RealtimeServerEventResponseDone responseDoneEvent = (RealtimeServerEventResponseDone) event;
                            assertTrue(responseDoneEvent.getResponse()
                                .getOutput()
                                .stream()
                                .anyMatch(outputItem -> outputItem instanceof RealtimeResponseFunctionCallItem));
                            responseDoneCount.incrementAndGet();
                        }
                    })
                .thenRequest(1)
                .then(() -> client.stop().block())
                .verifyComplete();
        });
    }

    @Test
    @Override
    void canDisableVoiceActivityDetection() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();
        client.start().block();
        client.sendMessage(new RealtimeClientEventSessionUpdate(
            new RealtimeRequestSession().setTurnDetection(new RealtimeTurnDetectionDisabled())
                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))))
            .block();

        FileUtils
            .sendAudioFileAsync(client, FileUtils.openResourceFile("realtime_whats_the_weather_pcm16_24khz_mono.wav"))
            .block();
        client.sendMessage(ConversationItem.createUserMessage("Hello, assistant!")).block();

        StepVerifier.create(client.getServerEvents())
            .thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.CONVERSATION_ITEM_CREATED, event -> {
                System.out.println("Event: " + toJson(event));
                if (event instanceof RealtimeServerEventError) {
                    RealtimeServerEventError errorEvent = (RealtimeServerEventError) event;
                    fail("Error occurred: " + errorEvent.getError().getMessage());
                } else {
                    assertFalse(event instanceof RealtimeServerEventInputAudioBufferSpeechStarted);
                    assertFalse(event instanceof RealtimeServerEventInputAudioBufferSpeechStopped);
                    assertFalse(event instanceof RealtimeServerEventConversationItemInputAudioTranscriptionCompleted);
                    assertFalse(event instanceof RealtimeServerEventConversationItemInputAudioTranscriptionFailed);
                    assertFalse(event instanceof RealtimeServerEventResponseCreated);
                    assertFalse(event instanceof RealtimeServerEventResponseDone);
                }
            })
            .then(() -> client.stop().block());
    }

    @Test
    @Disabled("ContentPartType should be TEXT for Assistant message. Current spec does not allow that combination.")
    @Override
    void badCommandProvidesError() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW).buildAsyncClient();

        client.start().block();
        client
            .sendMessage(new RealtimeClientEventSessionUpdate(
                new RealtimeRequestSession().setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))))
            .block();

        List<RealtimeClientEventConversationItemCreate> conversationItems
            = Arrays.asList(ConversationItem.createSystemMessage("You are a robot. Beep boop."),
                ConversationItem.createUserMessage("How can I pay for a joke?"),
                ConversationItem.createAssistantMessage("I ONLY ACCEPT CACHE"),
                ConversationItem
                    .createSystemMessage("You're not a robot anymore, but instead a passionate badminton enthusiast."),
                ConversationItem.createUserMessage("What's a good gift to buy?"),
                ConversationItem.createFunctionCall("product_lookup", "call-id-123", "{}"),
                ConversationItem.createFunctionCallOutput("call-id-123", "A new racquet!"));

        conversationItems.forEach(conversationItem -> client.sendMessage(conversationItem).block());

        AtomicInteger itemCreatedCount = new AtomicInteger(0);

        StepVerifier.create(client.getServerEvents())
            .thenConsumeWhile(event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE, event -> {
                System.out.println("Event: " + toJson(event));
                if (event instanceof RealtimeServerEventError) {
                    RealtimeServerEventError errorEvent = (RealtimeServerEventError) event;
                    System.out.println("Error occurred: " + toJson(errorEvent));
                    fail("Error occurred: " + errorEvent.getError().getMessage());
                }
                if (event instanceof RealtimeServerEventConversationItemCreated) {
                    itemCreatedCount.incrementAndGet();
                }
            })
            .verifyComplete();
        client.stop().block();

        assertEquals(itemCreatedCount.get(), conversationItems.size());
    }
}
