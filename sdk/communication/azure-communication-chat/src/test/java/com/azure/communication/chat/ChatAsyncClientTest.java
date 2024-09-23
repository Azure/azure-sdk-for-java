// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.ChatThreadItem;
import com.azure.communication.chat.models.ChatThreadProperties;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.CreateChatThreadResult;
import com.azure.communication.chat.models.ListChatThreadsOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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
        CommunicationIdentityClient communicationClient =
            getCommunicationIdentityClientBuilder(httpClient).buildClient();
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
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThread(threadRequest))
            .assertNext(result -> {
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
                assertEquals(0, result.getInvalidParticipants().size());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithResponse");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest))
            .assertNext(chatThreadClientResponse -> {
                CreateChatThreadResult result = chatThreadClientResponse.getValue();
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canRepeatCreateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canRepeatCreateThread");
        UUID uuid = UUID.randomUUID();
        CreateChatThreadOptions threadRequest = ChatOptionsProvider
            .createThreadOptions(
                firstThreadMember.getId(),
                secondThreadMember.getId()
            )
            .setIdempotencyToken(uuid.toString());

        Response<CreateChatThreadResult> response1 = client.createChatThreadWithResponse(threadRequest).block();
        assertNotNull(response1.getValue());
        assertNotNull(response1.getValue().getChatThread());
        assertNotNull(response1.getValue().getChatThread().getId());

        String expectedThreadId = response1.getValue().getChatThread().getId();

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest))
            .assertNext(response2 -> {
                CreateChatThreadResult result = response2.getValue();
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
                assertEquals(expectedThreadId, result.getChatThread().getId());
            })
            .verifyComplete();

        threadRequest.setIdempotencyToken(UUID.randomUUID().toString());
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest))
            .assertNext(response3 -> {
                CreateChatThreadResult result = response3.getValue();
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
                assertNotEquals(expectedThreadId, result.getChatThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canCreateNewThreadWithoutSettingRepeatabilityID(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateNewThreadWithoutSettingRepeatabilityID");

        CreateChatThreadOptions threadRequest1 = ChatOptionsProvider
            .createThreadOptions(
                firstThreadMember.getId(),
                secondThreadMember.getId()
            );

        CreateChatThreadOptions threadRequest2 = ChatOptionsProvider
            .createThreadOptions(
                firstThreadMember.getId(),
                secondThreadMember.getId()
            );

        Response<CreateChatThreadResult> response1 = client.createChatThreadWithResponse(threadRequest1).block();
        assertNotNull(response1.getValue());
        assertNotNull(response1.getValue().getChatThread());
        assertNotNull(response1.getValue().getChatThread().getId());

        String firstThreadId = response1.getValue().getChatThread().getId();

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest2))
            .assertNext(response2 -> {
                CreateChatThreadResult result = response2.getValue();
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
                assertNotEquals(firstThreadId, result.getChatThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithNullOptions");
        CreateChatThreadOptions options = null;

        // Act & Assert
        StepVerifier.create(client.createChatThread(options))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithResponseWithNullOptions");
        CreateChatThreadOptions options = null;

        // Act & Assert
        StepVerifier.create(client.createChatThread(options))
            .verifyError(NullPointerException.class);
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
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canGetExistingChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetExistingChatThread");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return chatThreadClient.getProperties();
                }))
            .assertNext(chatThread -> {
                assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThread.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetExistingChatThreadWithResponse");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return chatThreadClient.getPropertiesWithResponse();
                }))
            .assertNext(chatThreadResponse -> {
                ChatThreadProperties chatThreadProperties = chatThreadResponse.getValue();
                assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThreadProperties.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canDeleteChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThread");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return client.deleteChatThread(chatThreadClient.getChatThreadId());
                })
        )
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThreadWithResponse");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return client.deleteChatThreadWithResponse(chatThreadClient.getChatThreadId());
                })
        )
            .assertNext(deleteResponse -> {
                assertEquals(deleteResponse.getStatusCode(), 204);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotDeleteChatThreadWithNullId");
        StepVerifier.create(client.deleteChatThread(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotDeleteChatThreadWithResponseWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotDeleteChatThreadWithResponseWithNullId");
        StepVerifier.create(client.deleteChatThreadWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListChatThreads");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        StepVerifier.create(
            client.createChatThread(threadRequest)
                .concatWith(client.createChatThread(threadRequest)))
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
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        ListChatThreadsOptions options = new ListChatThreadsOptions();
        options.setMaxPageSize(10);

        StepVerifier.create(
            client.createChatThread(threadRequest)
                .concatWith(client.createChatThread(threadRequest)))
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
