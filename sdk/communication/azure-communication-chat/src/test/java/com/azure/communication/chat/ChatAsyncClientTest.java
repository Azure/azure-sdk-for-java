// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatAsyncClientTest extends ChatClientTestBase {

    private ClientLogger logger = new ClientLogger(ChatClientTest.class);

    private CommunicationIdentityClient communicationClient;
    private ChatAsyncClient client;

    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient, String testName) {
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        assertNotNull(communicationClient);

        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        AccessToken response = communicationClient.issueToken(firstThreadMember, scopes);

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
            .assertNext(chatThreadClient -> {
                assertNotNull(chatThreadClient);
                assertNotNull(chatThreadClient.getChatThreadId());
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
                ChatThreadAsyncClient chatThreadClient = chatThreadClientResponse.getValue();
                assertNotNull(chatThreadClient);
                assertNotNull(chatThreadClient.getChatThreadId()); 
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithNullOptions");

        // Act & Assert
        StepVerifier.create(client.createChatThread(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotCreateThreadWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotCreateThreadWithResponseWithNullOptions");
        
        // Act & Assert
        StepVerifier.create(client.createChatThread(null))
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
    public void canGetExistingChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetExistingChatThread");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(chatThreadClient -> {
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
        setupTest(httpClient, "canGetExistingChatThreadWithResponse");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
            .flatMap(chatThreadClient -> {
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
    public void getNotFoundOnNonExistingChatThread(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "getNotFoundOnNonExistingChatThread");
        StepVerifier.create(client.getChatThread("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2"))
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 404
            )
            .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThreadWithResponse(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "getNotFoundOnNonExistingChatThreadWithResponse");
        StepVerifier.create(client.getChatThreadWithResponse("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2"))
            .expectErrorMatches(exception ->
                ((HttpResponseException) exception).getResponse().getStatusCode() == 404)
            .verify();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotGetChatThreadWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotGetChatThreadWithNullId");
        StepVerifier.create(client.getChatThread(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotGetChatThreadWithResponseWithNullId(HttpClient httpClient) {
        // Act & Assert
        setupTest(httpClient, "cannotGetChatThreadWithResponseWithNullId");
        StepVerifier.create(client.getChatThreadWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThread");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(chatThreadClient -> {
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
        setupTest(httpClient, "canDeleteChatThreadWithResponse");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        
        // Act & Assert
        AtomicReference<ChatThreadAsyncClient> chatThreadClientRef = new AtomicReference<>();
        StepVerifier.create(
            client.createChatThread(threadRequest)
                .flatMap(chatThreadClient -> {
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
