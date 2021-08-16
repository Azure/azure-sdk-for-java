// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatClientTest extends ChatClientTestBase {

    private ClientLogger logger = new ClientLogger(ChatClientTest.class);

    private CommunicationIdentityClient communicationClient;
    private ChatClient client;

    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient, String testName) {
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        AccessToken response = communicationClient.getToken(firstThreadMember, scopes);

        ChatClientBuilder chatBuilder = getChatClientBuilder(response.getToken(), httpClient);
        client = addLoggingPolicyForIdentityClientBuilder(chatBuilder, testName).buildClient();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadSync");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Action & Assert
        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest);
        ChatThreadClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
        assertNotNull(chatThreadClient);
        assertNotNull(chatThreadClient.getChatThreadId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canCreateThreadWithResponseSync");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        // Action & Assert
        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest);
        ChatThreadClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
        assertNotNull(chatThreadClient);
        assertNotNull(chatThreadClient.getChatThreadId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetChatThreadClient(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canGetChatThreadClientSync");
        String threadId = "19:fe0a2f65a7834185b29164a7de57699c@thread.v2";

        // Action & Assert
        ChatThreadClient chatThreadClient = client.getChatThreadClient(threadId);
        assertNotNull(chatThreadClient);
        assertEquals(chatThreadClient.getChatThreadId(), threadId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThreadSync");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest);
        ChatThreadClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());

        // Action & Assert
        client.deleteChatThread(chatThreadClient.getChatThreadId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteChatThreadWithResponseSync");
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest);
        ChatThreadClient chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());

        // Action & Assert
        client.deleteChatThreadWithResponse(chatThreadClient.getChatThreadId(), Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListChatThreadsSync");
        CreateChatThreadOptions threadRequest1 = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        CreateChatThreadOptions threadRequest2 = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        client.createChatThread(threadRequest1);
        client.createChatThread(threadRequest2);

        Thread.sleep(500);

        // Action & Assert
        PagedIterable<ChatThreadItem> threadsResponse = client.listChatThreads();

        // process the iterableByPage
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
        threadsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedThreads.add(item));
        });

        assertTrue(returnedThreads.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreadsWithMaxPageSize(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListChatThreadsWithMaxPageSizeSync");
        CreateChatThreadOptions threadRequest1 = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        CreateChatThreadOptions threadRequest2 = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        client.createChatThread(threadRequest1);
        client.createChatThread(threadRequest2);

        Thread.sleep(500);

        ListChatThreadsOptions options = new ListChatThreadsOptions();
        options.setMaxPageSize(10);

        // Action & Assert
        PagedIterable<ChatThreadItem> threadsResponse = client.listChatThreads(options, Context.NONE);

        // process the iterableByPage
        List<ChatThreadItem> returnedThreads = new ArrayList<ChatThreadItem>();
        threadsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedThreads.add(item));
        });

        assertTrue(returnedThreads.size() == 2);
    }
}
