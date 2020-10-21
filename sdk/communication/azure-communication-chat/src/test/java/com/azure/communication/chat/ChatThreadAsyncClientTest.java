// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to determine if tests are playback or
 * live. By default, tests are run in playback mode.
 */
public class ChatThreadAsyncClientTest extends ChatClientTestBase {

    private ClientLogger logger = new ClientLogger(ChatThreadAsyncClientTest.class);

    private CommunicationIdentityClient communicationClient;
    private ChatAsyncClient client;
    private ChatThreadAsyncClient chatThreadClient;
    private String threadId;

    private CommunicationUser firstThreadMember;
    private CommunicationUser secondThreadMember;
    private CommunicationUser firstAddedThreadMember;
    private CommunicationUser secondAddedThreadMember;

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
        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstThreadMember, scopes);

        client = getChatClientBuilder(response.getToken(), httpClient).buildAsyncClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest).block();
        threadId = chatThreadClient.getChatThreadId();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        setupTest(httpClient);
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        chatThreadClient.updateChatThread(threadRequest).block();

        ChatThread chatThread = client.getChatThread(threadId).block();
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        chatThreadClient.updateChatThreadWithResponse(threadRequest).block().getValue();

        ChatThread chatThread = client.getChatThread(threadId).block();
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        chatThreadClient.addMembers(options).block();

        PagedIterable<ChatThreadMember> membersResponse = new PagedIterable<>(chatThreadClient.listMembers());

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
            chatThreadClient.removeMember(member.getUser()).block();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        chatThreadClient.addMembersWithResponse(options).block().getValue();

        PagedIterable<ChatThreadMember> membersResponse = new PagedIterable<>(chatThreadClient.listMembers());

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
            chatThreadClient.removeMemberWithResponse(member.getUser()).block().getValue();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessage(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessageWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessageWithResponse(messageRequest).block().getValue();

        ChatMessage message = chatThreadClient.getMessageWithResponse(response.getId()).block().getValue();
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.deleteMessage(response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.deleteMessageWithResponse(response.getId()).block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.updateMessage(response.getId(), updateMessageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.updateMessageWithResponse(response.getId(), updateMessageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessages(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        chatThreadClient.sendMessage(messageRequest).block();
        chatThreadClient.sendMessage(messageRequest).block();

        PagedIterable<ChatMessage> messagesResponse = new PagedIterable<ChatMessage>(chatThreadClient.listMessages());

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

        assertTrue(returnedMessages.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessagesWithOptions(HttpClient httpClient) {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        chatThreadClient.sendMessage(messageRequest).block();
        chatThreadClient.sendMessage(messageRequest).block();

        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        PagedIterable<ChatMessage> messagesResponse = new PagedIterable<ChatMessage>(chatThreadClient.listMessages(options));

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

        assertTrue(returnedMessages.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) {
        setupTest(httpClient);
        chatThreadClient.sendTypingNotification().block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) {
        setupTest(httpClient);
        chatThreadClient.sendTypingNotificationWithResponse().block();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceipts(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.sendReadReceipt(response.getId()).block();

        PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        if (interceptorManager.isPlaybackMode()) {
            assertTrue(returnedReadReceipts.size() > 0);
            checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.sendReadReceiptWithResponse(response.getId()).block();

        PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        if (interceptorManager.isPlaybackMode()) {
            assertTrue(returnedReadReceipts.size() > 0);
            checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
        }
    }
}
