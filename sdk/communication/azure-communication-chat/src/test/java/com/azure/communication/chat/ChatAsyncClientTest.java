// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.ChatRetentionPolicy;
import com.azure.communication.chat.models.ChatThreadItem;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.ListChatThreadsOptions;
import com.azure.communication.chat.models.NoneRetentionPolicy;
import com.azure.communication.chat.models.ThreadCreationDateRetentionPolicy;

import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatAsyncClientTest extends ChatClientTestBase {
    private ChatAsyncClient client;

    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient, String testName) {
        CommunicationIdentityClient communicationClient
            = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        assertNotNull(communicationClient);

        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        AccessToken response = communicationClient.getToken(firstThreadMember, scopes);

        ChatClientBuilder chatBuilder = getChatClientBuilder(response.getToken(), httpClient);
        client = addLoggingPolicyForIdentityClientBuilder(chatBuilder, testName).buildAsyncClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThread");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThread(threadRequest)).assertNext(result -> {
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());
            assertEquals(0, result.getInvalidParticipants().size());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithParticipantsHavingMetadata(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithParticipantsHavingMetadata");

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "val1");
        metadata.put("key2", "val2");

        CreateChatThreadOptions threadRequest = ChatOptionsProvider
            .createThreadOptionsWithMemberMetadata(firstThreadMember.getId(), secondThreadMember.getId(), metadata);

        // Act & Assert
        StepVerifier.create(client.createChatThread(threadRequest)).assertNext(result -> {
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            String threadId = result.getChatThread().getId();
            assertNotNull(threadId);
            assertEquals(0, result.getInvalidParticipants().size());

            // Verify the participants and their metadata
            PagedIterable<ChatParticipant> participants
                = new PagedIterable<>(client.getChatThreadClient(threadId).listParticipants());

            // Expect exactly two participants, each with metadata
            List<ChatParticipant> list = new ArrayList<>();
            participants.forEach(list::add);
            assertEquals(2, list.size(), "Should have exactly two participants");

            for (ChatParticipant p : list) {
                Map<String, String> resMeta = p.getMetadata();
                assertNotNull(resMeta, "Participant metadata should not be null");
                assertEquals(2, resMeta.size(), "Metadata size should match");
                assertEquals("val1", resMeta.get("key1"), "key1 should round-trip");
                assertEquals("val2", resMeta.get("key2"), "key2 should round-trip");
            }
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithResponse");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest)).assertNext(chatThreadClientResponse -> {
            CreateChatThreadResult result = chatThreadClientResponse.getValue();
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithMetadata(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithMetadata");
        Map<String, String> metadata = new HashMap<>();
        metadata.put("key1", "val1");
        metadata.put("key2", "val2");

        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());
        threadRequest.setMetadata(metadata);

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest)).assertNext(chatThreadClientResponse -> {
            CreateChatThreadResult result = chatThreadClientResponse.getValue();
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());

            // Verify metadata round-trip
            Map<String, String> resMetadata = result.getChatThread().getMetadata();
            assertNotNull(resMetadata, "Metadata should not be null");
            assertEquals(2, resMetadata.size(), "Metadata size should match");
            assertEquals("val1", resMetadata.get("key1"), "key1 should round-trip correctly");
            assertEquals("val2", resMetadata.get("key2"), "key2 should round-trip correctly");
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithNoneRetentionPolicy(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithNoneRetentionPolicy");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());
        threadRequest.setRetentionPolicy(new NoneRetentionPolicy());

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest)).assertNext(chatThreadClientResponse -> {
            CreateChatThreadResult result = chatThreadClientResponse.getValue();
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());
            assertNotNull(result.getChatThread().getRetentionPolicy());
            assertEquals("none", result.getChatThread().getRetentionPolicy().getKind().getValue());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithThreadCreationDateRetentionPolicy(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithThreadCreationDateRetentionPolicy");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());
        threadRequest.setRetentionPolicy(new ThreadCreationDateRetentionPolicy(45));

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest)).assertNext(chatThreadClientResponse -> {
            CreateChatThreadResult result = chatThreadClientResponse.getValue();
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());
            ChatRetentionPolicy chatRetentionPolicy = result.getChatThread().getRetentionPolicy();
            assertNotNull(chatRetentionPolicy);
            assertEquals("threadCreationDate", chatRetentionPolicy.getKind().getValue());
            ThreadCreationDateRetentionPolicy datePolicy = (ThreadCreationDateRetentionPolicy) chatRetentionPolicy;
            assertEquals(45, datePolicy.getDeleteThreadAfterDays());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateNewThreadWithoutSettingRepeatabilityID(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateNewThreadWithoutSettingRepeatabilityID");

        CreateChatThreadOptions threadRequest1
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        CreateChatThreadOptions threadRequest2
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        Response<CreateChatThreadResult> response1 = client.createChatThreadWithResponse(threadRequest1).block();
        assertNotNull(response1.getValue());
        assertNotNull(response1.getValue().getChatThread());
        assertNotNull(response1.getValue().getChatThread().getId());

        String firstThreadId = response1.getValue().getChatThread().getId();

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest2)).assertNext(response2 -> {
            CreateChatThreadResult result = response2.getValue();
            assertNotNull(result);
            assertNotNull(result.getChatThread());
            assertNotNull(result.getChatThread().getId());
            assertNotEquals(firstThreadId, result.getChatThread().getId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithNullOptions");
        CreateChatThreadOptions options = null;

        // Act & Assert
        StepVerifier.create(client.createChatThread(options)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithResponseWithNullOptions");
        CreateChatThreadOptions options = null;

        // Act & Assert
        StepVerifier.create(client.createChatThread(options)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetChatThreadClient(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetChatThreadClient");
        String threadId = "19:fe0a2f65a7834185b29164a7de57699c@thread.v2";

        // Act
        ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(threadId);

        // Assert
        assertNotNull(chatThreadClient);
        assertEquals(chatThreadClient.getChatThreadId(), threadId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetExistingChatThread");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(client.createChatThread(threadRequest).flatMap(createChatThreadResult -> {
            ChatThreadAsyncClient chatThreadClient
                = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
            chatThreadClientRef.set(chatThreadClient);
            return chatThreadClient.getProperties();
        })).assertNext(chatThread -> {
            assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThread.getId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetExistingChatThreadWithResponse");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(client.createChatThread(threadRequest).flatMap(createChatThreadResult -> {
            ChatThreadAsyncClient chatThreadClient
                = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
            chatThreadClientRef.set(chatThreadClient);
            return chatThreadClient.getPropertiesWithResponse();
        })).assertNext(chatThreadResponse -> {
            ChatThreadProperties chatThreadProperties = chatThreadResponse.getValue();
            assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThreadProperties.getId());
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThread");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(client.createChatThread(threadRequest).flatMap(createChatThreadResult -> {
            ChatThreadAsyncClient chatThreadClient
                = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
            chatThreadClientRef.set(chatThreadClient);
            return client.deleteChatThread(chatThreadClient.getChatThreadId());
        })).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThreadWithResponse");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(client.createChatThread(threadRequest).flatMap(createChatThreadResult -> {
            ChatThreadAsyncClient chatThreadClient
                = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
            chatThreadClientRef.set(chatThreadClient);
            return client.deleteChatThreadWithResponse(chatThreadClient.getChatThreadId());
        })).assertNext(deleteResponse -> {
            assertEquals(deleteResponse.getStatusCode(), 204);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotDeleteChatThreadWithNullId");
        StepVerifier.create(client.deleteChatThread(null)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithResponseWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotDeleteChatThreadWithResponseWithNullId");
        StepVerifier.create(client.deleteChatThreadWithResponse(null)).verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListChatThreads");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        StepVerifier.create(client.createChatThread(threadRequest).concatWith(client.createChatThread(threadRequest)))
            .assertNext(chatThreadClient -> {
                // Act & Assert
                PagedIterable<ChatThreadItem> threadsResponse = new PagedIterable<>(client.listChatThreads());

                // process the iterableByPage
                List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
                threadsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedThreads.add(item));
                });

                assertTrue(returnedThreads.size() == 2);
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreadsWithMaxPageSize(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListChatThreadsWithMaxPageSize");
        CreateChatThreadOptions threadRequest
            = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(), secondThreadMember.getId());

        ListChatThreadsOptions options = new ListChatThreadsOptions();
        options.setMaxPageSize(10);

        StepVerifier.create(client.createChatThread(threadRequest).concatWith(client.createChatThread(threadRequest)))
            .assertNext(chatThreadClient -> {
                // Act & Assert
                PagedIterable<ChatThreadItem> threadsResponse = new PagedIterable<>(client.listChatThreads(options));

                // process the iterableByPage
                List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
                threadsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedThreads.add(item));
                });

                assertTrue(returnedThreads.size() == 2);
            });
    }
}
