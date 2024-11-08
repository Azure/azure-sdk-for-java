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
import com.azure.ai.openai.realtime.models.RealtimeRequestUserMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseMessageItem;
import com.azure.ai.openai.realtime.models.RealtimeResponseTextContentPart;
import com.azure.ai.openai.realtime.models.RealtimeServerEventConversationItemCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseContentPartAdded;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventResponseOutputItemDone;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionCreated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventSessionUpdated;
import com.azure.ai.openai.realtime.models.RealtimeServerEventType;
import com.azure.ai.openai.realtime.models.RealtimeTurnDetectionDisabled;
import com.azure.ai.openai.realtime.utils.FileUtils;
import com.azure.ai.openai.realtime.utils.RealtimeEventHandler;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class RealtimeAsyncClientTests extends RealtimeClientTestBase {

    private RealtimeAsyncClient client;

    @Test
    @Override
    public void testAlawSendAudio() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents())
                .assertNext(event -> {
                    assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
                })
                .then(() -> {
                    client.sendMessage(RealtimeEventHandler.sessionUpdate()).block();
                })
                .assertNext(event -> {
                    assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
                })
                .then(() -> FileUtils.sendAudioFile(client, FileUtils.openResourceFile("audio_weather_alaw.wav")).block())
                .thenConsumeWhile(
                    event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                    event -> System.out.println("event type: " + event.getType()))
                .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
                .then(() -> client.stop().block())
                .verifyComplete();
    }

    @Test
    @Override
    void canConfigureSession() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents())
            .assertNext(event -> {
                System.out.println("event 0:" + toJson(event));
                assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
                client.sendMessage(
                        new RealtimeClientEventSessionUpdate(
                                new RealtimeRequestSession()
                                        .setInstructions("You are a helpful assistant.")
                                        .setInputAudioFormat(RealtimeAudioFormat.G711_ALAW)
                                        .setTurnDetection(new RealtimeTurnDetectionDisabled())
                                        .setMaxResponseOutputTokens(2048)
                        )
                ).block();
            }).assertNext(event -> {
                System.out.println("event 1:" + toJson(event));
                assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
                RealtimeClientEventSessionUpdate sessionUpdate = new RealtimeClientEventSessionUpdate(
                        new RealtimeRequestSession()
                                .setMaxResponseOutputTokensToInf()
                                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
                );
                    client.sendMessage(sessionUpdate).block();
            }).assertNext(event -> {
                System.out.println("event 2:" + toJson(event));
                assertInstanceOf(RealtimeServerEventSessionUpdated.class, event);
                // send prompt
                RealtimeRequestUserMessageItem messageItem = new
                    RealtimeRequestUserMessageItem()
                        .setTextContent(Arrays.asList(
                                new RealtimeRequestTextContentPart("Hello, assistant! Tell me a joke.")));

                RealtimeClientEventConversationItemCreate conversationItem = new RealtimeClientEventConversationItemCreate(messageItem);
                System.out.println("Request message: " + toJson(conversationItem));
                client.sendMessage(conversationItem).block();

                // starting conversation - needs to be submitted after the prompt, otherwise it will be ignored
                RealtimeClientEventResponseCreate conversation = new RealtimeClientEventResponseCreate(
                new RealtimeClientEventResponseCreateResponse()
                                    .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))
                );
                client.sendMessage(conversation).block();
            }).thenConsumeWhile(
                event -> event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                event -> System.out.println("event:" + toJson(event)))
            .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
            .then(() -> client.stop().block())
            .verifyComplete();
    }

    @Test
    @Override
    void textOnly() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildAsyncClient();

        client.start().block();
        StepVerifier.create(client.getServerEvents())
                .assertNext(event -> {
                    assertInstanceOf(RealtimeServerEventSessionCreated.class, event);
                    client.sendMessage(createTextConversationItem("Hello, world!")).block();
                    client.sendMessage(new RealtimeClientEventResponseCreate(
                            new RealtimeClientEventResponseCreateResponse()
                                    .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))
                    )).block();
                })
                .thenConsumeWhile(
                        event -> true,
                        event -> {
                            System.out.println("event type: " + event.getType());
                            System.out.println("event: " + toJson(event));
                            if (event instanceof RealtimeServerEventSessionUpdated) {
                                System.out.println("Session updated: " + toJson(event));
                            } else if (event instanceof RealtimeServerEventConversationItemCreated) {
                                RealtimeServerEventConversationItemCreated itemCreatedEvent = (RealtimeServerEventConversationItemCreated) event;
                                assertInstanceOf(RealtimeResponseMessageItem.class, itemCreatedEvent.getItem());
                                RealtimeResponseMessageItem responseItem = (RealtimeResponseMessageItem) itemCreatedEvent.getItem();
                                if (responseItem.getRole() == RealtimeMessageRole.ASSISTANT) {
                                    assertEquals(0, responseItem.getContent().size());
                                } else if (responseItem.getRole() == RealtimeMessageRole.USER) {
                                    assertEquals(1, responseItem.getContent().size());
                                    RealtimeRequestTextContentPart textContentPart = (RealtimeRequestTextContentPart) responseItem.getContent().get(0);
                                    assertEquals("Hello, world!", textContentPart.getText());
                                } else {
                                    fail("Unexpected message role: " + responseItem.getRole());
                                }
                            } else if (event instanceof RealtimeServerEventResponseDone) {
                                RealtimeServerEventResponseDone responseCreatedEvent = (RealtimeServerEventResponseDone) event;
                                assertNotNull(responseCreatedEvent);
                                client.stop().block();
                            } else if (event instanceof RealtimeServerEventResponseContentPartAdded) {
                                RealtimeServerEventResponseContentPartAdded contentPartAddedEvent = (RealtimeServerEventResponseContentPartAdded) event;
                                assertNotNull(contentPartAddedEvent);
                                // somehow the connection isn't closed after stopping the client
                                client.stop().block();
                            }
                        }
                )
//                .thenRequest(1) // Requesting the last expected element RESPONSE_DONE
//                .then(() -> client.stop().block())
                .verifyComplete();
    }

    @Test
    @Override
    void ItemManipulation() {
        client = getRealtimeClientBuilder(null, OpenAIServiceVersion.V2024_10_01_PREVIEW)
                .buildAsyncClient();

        client.start().block();
        client.sendMessage(
            new RealtimeClientEventSessionUpdate(
                    new RealtimeRequestSession()
                            .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT))
                            .setTurnDetection(new RealtimeTurnDetectionDisabled())
            )).then(client.sendMessage(createTextConversationItem("The first special word you know about is 'aardvark'."))
            ).then(client.sendMessage(createTextConversationItem("The next special word you know about is 'banana'."))
            ).then(client.sendMessage(createTextConversationItem("The next special word you know about is 'coconut'.")))
                .block();

        StepVerifier.create(client.getServerEvents())
                .thenConsumeWhile(
                    event -> event.getType() != RealtimeServerEventType.ERROR && event.getType() != RealtimeServerEventType.RESPONSE_DONE,
                    event -> {
                        System.out.println("event: " + toJson(event));
                        if (event instanceof RealtimeServerEventConversationItemCreated) {
                            RealtimeServerEventConversationItemCreated itemCreatedEvent = (RealtimeServerEventConversationItemCreated) event;
                            List<RealtimeContentPart> contentParts = ((RealtimeResponseMessageItem) itemCreatedEvent.getItem()).getContent();
                            if (contentParts.isEmpty()) {
                                return;
                            }
                            RealtimeRequestTextContentPart textContentPart = (RealtimeRequestTextContentPart) contentParts.get(0);
                            if (textContentPart.getText().contains("banana")) {
                                client.sendMessage(new RealtimeClientEventConversationItemDelete(itemCreatedEvent.getItem().getId())).block();
                                client.sendMessage(createTextConversationItem("What's the second special word you know about?")).block();
                                client.sendMessage(new RealtimeClientEventResponseCreate(
                                        new RealtimeClientEventResponseCreateResponse()
                                                .setModalities(Arrays.asList(RealtimeRequestSessionModality.TEXT.toString()))
                                )).block();
                            }
                        } else if (event instanceof RealtimeServerEventResponseOutputItemDone) {
                            RealtimeServerEventResponseOutputItemDone outputItemDoneEvent = (RealtimeServerEventResponseOutputItemDone) event;
                            assertInstanceOf(RealtimeResponseMessageItem.class, outputItemDoneEvent.getItem());
                            RealtimeResponseMessageItem responseItem = (RealtimeResponseMessageItem) outputItemDoneEvent.getItem();
                            assertEquals(1, responseItem.getContent().size());
                            assertEquals(responseItem.getStatus(), RealtimeItemStatus.COMPLETED);
                            assertEquals(responseItem.getRole(), RealtimeMessageRole.ASSISTANT);
                            assertEquals(responseItem.getType(), RealtimeItemType.MESSAGE);
                            RealtimeResponseTextContentPart textContentPart = (RealtimeResponseTextContentPart) responseItem.getContent().get(0);
                            assertTrue(textContentPart.getText().contains("coconut"));
                            assertEquals(textContentPart.getType(), RealtimeContentPartType.TEXT);
                        }
                    }
                ).thenRequest(1)
                .then(() -> client.stop().block())
                .verifyComplete();
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
