// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        ChatThreadAsyncClient chatThreadClient = client.createChatThread(threadRequest).block();
        assertNotNull(chatThreadClient);
        assertNotNull(chatThreadClient.getChatThreadId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canCreateThreadWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());

        ChatThreadAsyncClient chatThreadClient = client.createChatThreadWithResponse(threadRequest).block().getValue();
        assertNotNull(chatThreadClient);
        assertNotNull(chatThreadClient.getChatThreadId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetChatThreadClient(HttpClient httpClient) {
        setupTest(httpClient);
        String threadId = "19:fe0a2f65a7834185b29164a7de57699c@thread.v2";

        ChatThreadAsyncClient chatThreadClient = client.getChatThreadClient(threadId);
        assertNotNull(chatThreadClient);
        assertEquals(chatThreadClient.getChatThreadId(), threadId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThread(HttpClient httpClient) {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        ChatThreadAsyncClient chatThreadClient = client.createChatThread(threadRequest).block();

        ChatThread chatThread = client.getChatThread(chatThreadClient.getChatThreadId()).block();
        assertEquals(chatThreadClient.getChatThreadId(), chatThread.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canGetExistingChatThreadWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        ChatThreadAsyncClient chatThreadClient = client.createChatThread(threadRequest).block();

        ChatThread chatThread = client.getChatThreadWithResponse(chatThreadClient.getChatThreadId()).block().getValue();
        assertEquals(chatThreadClient.getChatThreadId(), chatThread.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThread(HttpClient httpClient) {
        setupTest(httpClient);
        assertRestException(
            () -> client.getChatThread("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2").block(), 404);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getNotFoundOnNonExistingChatThreadWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        assertRestException(
            () -> client.getChatThreadWithResponse("19:020082a8df7b44dd8c722bea8fe7167f@thread.v2").block(), 404);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThread(HttpClient httpClient) {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        ChatThreadAsyncClient chatThreadClient = client.createChatThread(threadRequest).block();

        client.deleteChatThread(chatThreadClient.getChatThreadId()).block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteChatThreadWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        ChatThreadAsyncClient chatThreadClient = client.createChatThread(threadRequest).block();

        client.deleteChatThreadWithResponse(chatThreadClient.getChatThreadId()).block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreads(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        client.createChatThread(threadRequest).block();
        client.createChatThread(threadRequest).block();

        Thread.sleep(500);

        PagedIterable<ChatThreadInfo> threadsResponse = new PagedIterable<>(client.listChatThreads());

        // process the iterableByPage
        List<ChatThreadInfo> returnedThreads = new ArrayList<ChatThreadInfo>();
        threadsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedThreads.add(item));
        });

        assertTrue(returnedThreads.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListChatThreadsWithMaxPageSize(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        client.createChatThread(threadRequest).block();
        client.createChatThread(threadRequest).block();

        Thread.sleep(500);

        ListChatThreadsOptions options = new ListChatThreadsOptions();
        options.setMaxPageSize(10);

        PagedIterable<ChatThreadInfo> threadsResponse = new PagedIterable<>(client.listChatThreads(options));

        // process the iterableByPage
        List<ChatThreadInfo> returnedThreads = new ArrayList<ChatThreadInfo>();
        threadsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedThreads.add(item));
        });

        assertTrue(returnedThreads.size() == 2);
    }
}
