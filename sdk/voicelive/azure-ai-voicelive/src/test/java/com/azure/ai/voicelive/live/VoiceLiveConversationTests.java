// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.live;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.ClientEventConversationItemCreate;
import com.azure.ai.voicelive.models.ClientEventConversationItemDelete;
import com.azure.ai.voicelive.models.ClientEventConversationItemRetrieve;
import com.azure.ai.voicelive.models.ClientEventSessionUpdate;
import com.azure.ai.voicelive.models.InputTextContentPart;
import com.azure.ai.voicelive.models.InteractionModality;
import com.azure.ai.voicelive.models.ItemType;
import com.azure.ai.voicelive.models.MessageContentPart;
import com.azure.ai.voicelive.models.OpenAIVoice;
import com.azure.ai.voicelive.models.OpenAIVoiceName;
import com.azure.ai.voicelive.models.ServerEventType;
import com.azure.ai.voicelive.models.SessionResponseItem;
import com.azure.ai.voicelive.models.SessionResponseMessageItem;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemCreated;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemDeleted;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemRetrieved;
import com.azure.ai.voicelive.models.SessionUpdateConversationItemTruncated;
import com.azure.ai.voicelive.models.SessionUpdateResponseOutputItemDone;
import com.azure.ai.voicelive.models.UserMessageItem;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import reactor.core.Disposable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Live tests for VoiceLive conversation item operations (retrieve, truncate, delete).
 */
public class VoiceLiveConversationTests extends VoiceLiveTestBase {

    static Stream<Arguments> apiVersionParams() {
        return Arrays.stream(API_VERSIONS).map(Arguments::of);
    }

    static Stream<Arguments> retrieveItemParams() {
        return crossProduct(new String[] { "gpt-realtime" }, API_VERSIONS);
    }

    @ParameterizedTest
    @MethodSource("retrieveItemParams")
    @LiveOnly
    public void testRealtimeServiceRetrieveItem(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicReference<String> outputItemId = new AtomicReference<>();
        AtomicReference<SessionResponseItem> retrievedItem = new AtomicReference<>();
        CountDownLatch outputItemLatch = new CountDownLatch(1);
        CountDownLatch retrieveLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant.")
                    .setVoice(BinaryData.fromObject(new OpenAIVoice(OpenAIVoiceName.ALLOY)));

            session = client.startSession(model, null).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_OUTPUT_ITEM_DONE) {
                    if (event instanceof SessionUpdateResponseOutputItemDone) {
                        SessionUpdateResponseOutputItemDone outputDone = (SessionUpdateResponseOutputItemDone) event;
                        if (outputDone.getItem() != null && outputDone.getItem().getType() == ItemType.MESSAGE) {
                            outputItemId.set(outputDone.getItem().getId());
                            outputItemLatch.countDown();
                        }
                    }
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_RETRIEVED) {
                    if (event instanceof SessionUpdateConversationItemRetrieved) {
                        SessionUpdateConversationItemRetrieved retrieved
                            = (SessionUpdateConversationItemRetrieved) event;
                        retrievedItem.set(retrieved.getItem());
                    }
                    retrieveLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    outputItemLatch.countDown();
                    retrieveLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                outputItemLatch.countDown();
                retrieveLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);
            session.sendInputAudio(BinaryData.fromBytes(getTrailingSilenceBytes())).block(SEND_TIMEOUT);

            boolean outputReceived = outputItemLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(outputReceived, "Should receive output item done event");
            Assertions.assertNotNull(outputItemId.get(), "Output item ID should not be null");

            // Retrieve the conversation item
            session.sendEvent(new ClientEventConversationItemRetrieve(outputItemId.get())).block(SEND_TIMEOUT);

            boolean retrieved = retrieveLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(retrieved, "Should receive conversation item retrieved event");
            Assertions.assertNotNull(retrievedItem.get(), "Retrieved item should not be null");
            Assertions.assertTrue(retrievedItem.get() instanceof SessionResponseMessageItem,
                "Retrieved item should be a message item");

            SessionResponseMessageItem messageItem = (SessionResponseMessageItem) retrievedItem.get();
            Assertions.assertNotNull(messageItem.getRole(), "Message item should have a role");
            Assertions.assertEquals("assistant", messageItem.getRole().toString(),
                "Message role should be 'assistant'");
            Assertions.assertNotNull(messageItem.getContent(), "Message item should have content");
            Assertions.assertFalse(messageItem.getContent().isEmpty(), "Message content should not be empty");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    static Stream<Arguments> truncateItemParams() {
        return crossProduct(new String[] { "gpt-realtime" }, API_VERSIONS);
    }

    @ParameterizedTest
    @MethodSource("truncateItemParams")
    @LiveOnly
    public void testRealtimeServiceTruncateItem(String model, String apiVersion)
        throws InterruptedException, IOException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        byte[] audioData = loadAudioFile("largest_lake.wav");

        AtomicReference<String> outputItemId = new AtomicReference<>();
        AtomicReference<SessionUpdateConversationItemTruncated> truncatedEvent = new AtomicReference<>();
        CountDownLatch outputItemLatch = new CountDownLatch(1);
        CountDownLatch truncateLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant.");

            session = client.startSession(model, null).block(SESSION_TIMEOUT);

            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();

                if (eventType == ServerEventType.RESPONSE_OUTPUT_ITEM_DONE) {
                    if (event instanceof SessionUpdateResponseOutputItemDone) {
                        SessionUpdateResponseOutputItemDone outputDone = (SessionUpdateResponseOutputItemDone) event;
                        if (outputDone.getItem() != null && outputDone.getItem().getType() == ItemType.MESSAGE) {
                            outputItemId.set(outputDone.getItem().getId());
                            outputItemLatch.countDown();
                        }
                    }
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_TRUNCATED) {
                    if (event instanceof SessionUpdateConversationItemTruncated) {
                        truncatedEvent.set((SessionUpdateConversationItemTruncated) event);
                    }
                    truncateLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    outputItemLatch.countDown();
                    truncateLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                outputItemLatch.countDown();
                truncateLatch.countDown();
            });

            waitForSetup();

            ClientEventSessionUpdate updateEvent = new ClientEventSessionUpdate(sessionOptions);
            session.sendEvent(updateEvent).block(SEND_TIMEOUT);

            waitForSetup();

            session.sendInputAudio(BinaryData.fromBytes(audioData)).block(SEND_TIMEOUT);
            session.sendInputAudio(BinaryData.fromBytes(getTrailingSilenceBytes())).block(SEND_TIMEOUT);

            boolean outputReceived = outputItemLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(outputReceived, "Should receive output item done event");
            Assertions.assertNotNull(outputItemId.get(), "Output item ID should not be null");

            // Truncate the conversation item at 1000ms
            session.truncateConversation(outputItemId.get(), 0, Duration.ofMillis(1000)).block(SEND_TIMEOUT);

            boolean truncated = truncateLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(truncated, "Should receive conversation item truncated event");
            Assertions.assertNotNull(truncatedEvent.get(), "Truncated event should not be null");
            Assertions.assertEquals(outputItemId.get(), truncatedEvent.get().getItemId(),
                "Truncated item ID should match the output item ID");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }

    // Create a user text item, then delete it and assert the server echoes a
    // conversation.item.deleted event with the matching item id.
    @ParameterizedTest
    @MethodSource("apiVersionParams")
    @LiveOnly
    public void testConversationItemDelete(String apiVersion) throws InterruptedException {
        VoiceLiveAsyncClient client = createClient(apiVersion);

        AtomicReference<String> createdItemId = new AtomicReference<>();
        AtomicReference<SessionUpdateConversationItemDeleted> deletedEvent = new AtomicReference<>();
        CountDownLatch createdLatch = new CountDownLatch(1);
        CountDownLatch deletedLatch = new CountDownLatch(1);

        VoiceLiveSessionAsyncClient session = null;
        Disposable subscription = null;
        try {
            VoiceLiveSessionOptions sessionOptions
                = new VoiceLiveSessionOptions().setInstructions("You are a helpful assistant.")
                    .setModalities(Collections.singletonList(InteractionModality.TEXT));

            session = client.startSession(TEST_MODEL, null).block(SESSION_TIMEOUT);
            Assertions.assertNotNull(session, "Session should be created successfully");

            subscription = session.receiveEvents().subscribe(event -> {
                ServerEventType eventType = event.getType();
                if (eventType == ServerEventType.CONVERSATION_ITEM_CREATED) {
                    if (event instanceof SessionUpdateConversationItemCreated) {
                        SessionUpdateConversationItemCreated created = (SessionUpdateConversationItemCreated) event;
                        if (created.getItem() != null) {
                            createdItemId.set(created.getItem().getId());
                        }
                    }
                    createdLatch.countDown();
                } else if (eventType == ServerEventType.CONVERSATION_ITEM_DELETED) {
                    if (event instanceof SessionUpdateConversationItemDeleted) {
                        deletedEvent.set((SessionUpdateConversationItemDeleted) event);
                    }
                    deletedLatch.countDown();
                } else if (eventType == ServerEventType.ERROR) {
                    handleError(event);
                    createdLatch.countDown();
                    deletedLatch.countDown();
                }
            }, error -> {
                System.err.println("Error receiving events: " + error.getMessage());
                createdLatch.countDown();
                deletedLatch.countDown();
            });

            waitForSetup();

            session.sendEvent(new ClientEventSessionUpdate(sessionOptions)).block(SEND_TIMEOUT);
            waitForSetup();

            MessageContentPart textPart = new InputTextContentPart("Remember this message.");
            UserMessageItem userMessage = new UserMessageItem(Collections.singletonList(textPart));
            session.sendEvent(new ClientEventConversationItemCreate().setItem(userMessage)).block(SEND_TIMEOUT);

            boolean created = createdLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(created, "Should receive conversation item created event");
            Assertions.assertNotNull(createdItemId.get(), "Created item ID should not be null");

            session.sendEvent(new ClientEventConversationItemDelete(createdItemId.get())).block(SEND_TIMEOUT);

            boolean deleted = deletedLatch.await(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            Assertions.assertTrue(deleted, "Should receive conversation item deleted event");
            Assertions.assertNotNull(deletedEvent.get(), "Deleted event should not be null");
            Assertions.assertEquals(createdItemId.get(), deletedEvent.get().getItemId(),
                "Deleted item ID should match the created item ID");
        } finally {
            if (subscription != null) {
                subscription.dispose();
            }
            closeSession(session);
        }
    }
}
