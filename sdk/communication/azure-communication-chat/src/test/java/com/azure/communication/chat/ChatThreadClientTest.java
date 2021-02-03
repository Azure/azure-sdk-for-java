// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatThreadClientTest extends ChatClientTestBase {

    private ClientLogger logger = new ClientLogger(ChatThreadClientTest.class);

    private CommunicationIdentityClient communicationClient;
    private ChatClient client;
    private ChatThreadClient chatThreadClient;
    private String threadId;

    private CommunicationUserIdentifier firstThreadMember;
    private CommunicationUserIdentifier secondThreadMember;
    private CommunicationUserIdentifier firstAddedThreadMember;
    private CommunicationUserIdentifier secondAddedThreadMember;

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
        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        AccessToken response = communicationClient.issueToken(firstThreadMember, scopes);

        ChatClientBuilder chatBuilder = getChatClientBuilder(response.getToken(), httpClient);
        client = addLoggingPolicyForIdentityClientBuilder(chatBuilder, testName).buildClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest);
        threadId = chatThreadClient.getChatThreadId();
    }

    private void setupUnitTest(HttpClient mockHttpClient) {
        String threadId = "19:4b72178530934b7790135dd9359205e0@thread.v2";
        String mockToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMl9pbnQiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoic3Bvb2w6NTdiOWJhYzktZGY2Yy00ZDM5LWE3M2ItMjZlOTQ0YWRmNmVhXzNmMDExNi03YzAwOTQ5MGRjIiwic2NwIjoxNzkyLCJjc2kiOiIxNTk3ODcyMDgyIiwiaWF0IjoxNTk3ODcyMDgyLCJleHAiOjE1OTc5NTg0ODIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiI1N2I5YmFjOS1kZjZjLTRkMzktYTczYi0yNmU5NDRhZGY2ZWEifQ.l2UXI0KH2LXZQoz7FPsfLZS0CX8cYsnW3CMECfqwuncV8WqrTD7RbqZDfAaYXn0t5sHrGM4CRbpx4LwIZhXOlmsmOdTdHSsPUCIqJscwNjQmltvOrIt11DOmObQ63w0kYq9QrlB-lyZNzTEAED2FhMwBAbhZOokRtFajYD7KvJb1w9oUXousQ_z6zZqjbt1Cy4Ll3zO1GR4G7yRV8vK3bLnN2IWPaEkoqx8PHeHLa9Cb4joowseRfQxFHv28xcCF3r9SBCauUeJcmbwBmnOAOLS-EAJTLiGhil7m3BNyLN5RnYbsK5ComtL2-02TbkPilpy21OhW0MJkicSFlCbYvg";
        client = getChatClientBuilder(mockToken, mockHttpClient).buildClient();
        chatThreadClient = client.getChatThreadClient(threadId);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateThreadSync");
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        // Action & Assert
        chatThreadClient.updateChatThread(threadRequest);

        ChatThread chatThread = client.getChatThread(threadId);
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateThreadWithResponseSync");
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

         // Action & Assert
        chatThreadClient.updateChatThreadWithResponse(threadRequest, Context.NONE);

        ChatThread chatThread = client.getChatThread(threadId);
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembers(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersSync");
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        // Action & Assert
        chatThreadClient.addMembers(options);

        PagedIterable<ChatThreadMember> membersResponse = chatThreadClient.listMembers();

        // process the iterableByPage
        List<ChatThreadMember> returnedMembers = new ArrayList<ChatThreadMember>();
        membersResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedMembers.add(item));
        });

        for (ChatThreadMember member: options.getMembers()) {
            assertTrue(checkMembersListContainsMemberId(returnedMembers, member.getUser().getId()));
        }

        assertTrue(returnedMembers.size() == 4);

        for (ChatThreadMember member: options.getMembers()) {
            chatThreadClient.removeMember(member.getUser());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMembersWithContext(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canListMembersWithContextSync");

        // Action & Assert
        PagedIterable<ChatThreadMember> membersResponse = chatThreadClient.listMembers(Context.NONE);

        // process the iterableByPage
        List<ChatThreadMember> returnedMembers = new ArrayList<ChatThreadMember>();
        membersResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedMembers.add(item));
        });

        assertEquals(returnedMembers.size(), 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersWithResponseSync");
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        // Action & Assert
        chatThreadClient.addMembersWithResponse(options, Context.NONE);

        PagedIterable<ChatThreadMember> membersResponse = chatThreadClient.listMembers();

        // process the iterableByPage
        List<ChatThreadMember> returnedMembers = new ArrayList<ChatThreadMember>();
        membersResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedMembers.add(item));
        });

        for (ChatThreadMember member: options.getMembers()) {
            assertTrue(checkMembersListContainsMemberId(returnedMembers, member.getUser().getId()));
        }

        assertTrue(returnedMembers.size() == 4);

        for (ChatThreadMember member: options.getMembers()) {
            chatThreadClient.removeMemberWithResponse(member.getUser(), Context.NONE);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendThenGetMessageSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        ChatMessage message = chatThreadClient.getMessage(response.getId());
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendThenGetMessageWithResponseSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        SendChatMessageResult response = chatThreadClient.sendMessageWithResponse(messageRequest, Context.NONE).getValue();

        ChatMessage message = chatThreadClient.getMessageWithResponse(response.getId(), Context.NONE).getValue();
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteExistingMessageSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);
        
        // Action & Assert
        chatThreadClient.deleteMessage(response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteExistingMessageWithResponseSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.deleteMessageWithResponse(response.getId(), Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateExistingMessageSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.updateMessage(response.getId(), updateMessageRequest);

        ChatMessage message = chatThreadClient.getMessage(response.getId());
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateExistingMessageWithResponseSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.updateMessageWithResponse(response.getId(), updateMessageRequest, Context.NONE);

        ChatMessage message = chatThreadClient.getMessage(response.getId());
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessagesWithOptionsSync(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canListMessagesWithOptionsSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        chatThreadClient.sendMessage(messageRequest);
        chatThreadClient.sendMessage(messageRequest);

        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        // Action & Assert
        PagedIterable<ChatMessage> messagesResponse = chatThreadClient.listMessages(options, Context.NONE);

        // process the iterableByPage
        List<ChatMessage> returnedMessages = new ArrayList<ChatMessage>();
        messagesResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> {
                if (item.getType().equals("Text")) {
                    returnedMessages.add(item);
                }
            });
        });

        assertEquals(returnedMessages.size(), 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendTypingNotificationSync");

        // Action & Assert
        chatThreadClient.sendTypingNotification();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendTypingNotificationWithResponseSync");

        // Action & Assert
        chatThreadClient.sendTypingNotificationWithResponse(Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceipts(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendThenListReadReceiptsSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceipt(response.getId());

        PagedIterable<ReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendThenListReadReceiptsWithResponseSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceiptWithResponse(response.getId(), Context.NONE);

        PagedIterable<ReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListReadReceiptsWithContext(HttpClient httpClient) {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };
        setupUnitTest(mockHttpClient);
        PagedIterable<ReadReceipt> readReceipts = chatThreadClient.listReadReceipts(Context.NONE);

        // // process the iterableByPage
        List<ReadReceipt> readReceiptList = new ArrayList<ReadReceipt>();
        readReceipts.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> readReceiptList.add(item));
        });
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListReadReceipts(HttpClient httpClient) {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };
        setupUnitTest(mockHttpClient);
        PagedIterable<ReadReceipt> readReceipts = chatThreadClient.listReadReceipts();

        // // process the iterableByPage
        List<ReadReceipt> readReceiptList = new ArrayList<ReadReceipt>();
        readReceipts.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> readReceiptList.add(item));
        });
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendReadReceiptSync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendReadReceiptSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceipt(response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendReadReceiptWithResponseSync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendReadReceiptWithResponseSync");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        Response<Void> sendResponse = chatThreadClient.sendReadReceiptWithResponse(response.getId(), Context.NONE);
        assertEquals(201, sendResponse.getStatusCode());
    }
}
