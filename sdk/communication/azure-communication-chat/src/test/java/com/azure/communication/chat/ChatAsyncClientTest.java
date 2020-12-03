// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.http.rest.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.UUID;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatAsyncClientTest extends ChatClientTestBase {

    private ClientLogger logger = new ClientLogger(ChatClientTest.class);

    private CommunicationIdentityClient communicationClient;
    private ChatAsyncClient client;

    private CommunicationUser firstThreadMember;
    private CommunicationUser secondThreadMember;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient) {
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        assertNotNull(communicationClient);

        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstThreadMember, scopes);

        client = getChatClientBuilder(response.getToken(), httpClient).buildAsyncClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThread(threadRequest))
            .assertNext(result -> {
                assertNotNull(result);
                assertNotNull(result.getThread());
                assertNotNull(result.getThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest))
            .assertNext(chatThreadClientResponse -> {
                CreateChatThreadResult result = chatThreadClientResponse.getValue();
                assertNotNull(result);
                assertNotNull(result.getThread());
                assertNotNull(result.getThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canRepeatCreateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        UUID uuid = UUID.randomUUID();
        CreateChatThreadOptions threadRequest = ChatOptionsProvider
            .createThreadOptions(
                firstThreadMember.getId(),
                secondThreadMember.getId()
            )
            .setRepeatabilityRequestId(uuid.toString());

        Response<CreateChatThreadResult> response1 = client.createChatThreadWithResponse(threadRequest).block();
        assertNotNull(response1.getValue());
        assertNotNull(response1.getValue().getThread());
        assertNotNull(response1.getValue().getThread().getId());

        String expectedThreadId = response1.getValue().getThread().getId();

        // Act & Assert
        StepVerifier.create(client.createChatThreadWithResponse(threadRequest))
            .assertNext(response2 -> {
                CreateChatThreadResult result = response2.getValue();
                assertNotNull(result);
                assertNotNull(result.getThread());
                assertNotNull(result.getThread().getId());
                assertEquals(expectedThreadId, result.getThread().getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetChatThreadClient(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
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
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return client.getChatThread(chatThreadClient.getChatThreadId());
                }))
            .assertNext(chatThread -> {
                assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThread.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
            .flatMap(createChatThreadResult -> {
                ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getThread().getId());
                chatThreadClientRef.set(chatThreadClient);
                return client.getChatThreadWithResponse(chatThreadClient.getChatThreadId());
            }))
            .assertNext(chatThreadResponse -> {
                ChatThread chatThread = chatThreadResponse.getValue();
                assertEquals(chatThreadClientRef.get().getChatThreadId(), chatThread.getId());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getForbiddenOnNonExistingChatThread(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient);
        StepVerifier.create(client.getChatThread("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2"))
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 403
            )
            .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getForbiddenOnNonExistingChatThreadWithResponse(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient);
        StepVerifier.create(client.getChatThreadWithResponse("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2"))
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 403
            )
            .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getThread().getId());
                    chatThreadClientRef.set(chatThreadClient);
                    return client.deleteChatThread(chatThreadClient.getChatThreadId());
                })
            )
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(createChatThreadResult -> {
                    ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getThread().getId());
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
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        StepVerifier.create(
                client.createChatThread(threadRequest)
                    .concatWith(client.createChatThread(threadRequest)))
            .assertNext(chatThreadClient -> {
                // Act & Assert
                PagedIterable<ChatThreadInfo> threadsResponse = new PagedIterable<>(client.listChatThreads());

                // process the iterableByPage
                List<ChatThreadInfo> returnedThreads = new ArrayList<ChatThreadInfo>();
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
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        ListChatThreadsOptions options = new ListChatThreadsOptions();
        options.setMaxPageSize(10);

        StepVerifier.create(
            client.createChatThread(threadRequest)
                .concatWith(client.createChatThread(threadRequest)))
            .assertNext(chatThreadClient -> {
                // Act & Assert
                PagedIterable<ChatThreadInfo> threadsResponse = new PagedIterable<>(client.listChatThreads(options));

                // process the iterableByPage
                List<ChatThreadInfo> returnedThreads = new ArrayList<ChatThreadInfo>();
                threadsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedThreads.add(item));
                });

                assertTrue(returnedThreads.size() == 2);
            });
    }
}
