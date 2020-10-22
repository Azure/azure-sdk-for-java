// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstThreadMember, scopes);

        client = getChatClientBuilder(response.getToken(), httpClient).buildClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest);
        threadId = chatThreadClient.getChatThreadId();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
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
        setupTest(httpClient);
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
        setupTest(httpClient);
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
    public void canAddListAndRemoveMembersWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
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
        setupTest(httpClient);
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
        setupTest(httpClient);
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
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);
        
        // Action & Assert
        chatThreadClient.deleteMessage(response.getId());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.deleteMessageWithResponse(response.getId(), Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
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
        setupTest(httpClient);
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
    public void canListMessages(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        chatThreadClient.sendMessage(messageRequest);
        chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        PagedIterable<ChatMessage> messagesResponse = chatThreadClient.listMessages();

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
        // Arrange
        setupTest(httpClient);
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

        assertTrue(returnedMessages.size() == 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);

        // Action & Assert
        chatThreadClient.sendTypingNotification();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);

        // Action & Assert
        chatThreadClient.sendTypingNotificationWithResponse(Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceipts(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
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

        if (interceptorManager.isPlaybackMode()) {
            assertTrue(returnedReadReceipts.size() > 0);
            checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
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

        if (interceptorManager.isPlaybackMode()) {
            assertTrue(returnedReadReceipts.size() > 0);
            checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
        }
    }
}
