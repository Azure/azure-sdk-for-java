// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
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

        communicationClient = getCommunicationIdentityClientBuilder().buildClient();
        firstThreadMember = communicationClient.createUser();
        secondThreadMember = communicationClient.createUser();

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstThreadMember, scopes);

        client = getChatClientBuilder(response.getToken()).buildAsyncClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstThreadMember.getId(), secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest).block();
        threadId = chatThreadClient.getChatThreadId();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    @Test
    public void canUpdateThread() {
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        chatThreadClient.updateChatThread(threadRequest).block();

        ChatThread chatThread = client.getChatThread(threadId).block();
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @Test
    public void canUpdateThreadWithResponse() {
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        chatThreadClient.updateChatThreadWithResponse(threadRequest).block().getValue();

        ChatThread chatThread = client.getChatThread(threadId).block();
        assertEquals(chatThread.getTopic(), threadRequest.getTopic());
    }

    @Test
    public void canAddListAndRemoveMembersAsync() throws InterruptedException {
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

    @Test
    public void canAddListAndRemoveMembersWithResponseAsync() throws InterruptedException {
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

    @Test
    public void canSendThenGetMessage() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @Test
    public void canSendThenGetMessageWithResponse() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessageWithResponse(messageRequest).block().getValue();

        ChatMessage message = chatThreadClient.getMessageWithResponse(response.getId()).block().getValue();
        assertEquals(message.getContent(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @Test
    public void canDeleteExistingMessage() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.deleteMessage(response.getId());
    }

    @Test
    public void canDeleteExistingMessageWithResponse() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.deleteMessageWithResponse(response.getId()).block();
    }

    @Test
    public void canUpdateExistingMessage() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.updateMessage(response.getId(), updateMessageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @Test
    public void canUpdateExistingMessageWithResponse() {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.updateMessageWithResponse(response.getId(), updateMessageRequest).block();

        ChatMessage message = chatThreadClient.getMessage(response.getId()).block();
        assertEquals(message.getContent(), updateMessageRequest.getContent());
    }

    @Test
    public void canListMessages() {
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

    @Test
    public void canListMessagesWithOptions() {
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

    @Test
    public void canSendTypingNotification() {
        chatThreadClient.sendTypingNotification().block();
    }

    @Test
    public void canSendTypingNotificationWithResponse() {
        chatThreadClient.sendTypingNotificationWithResponse().block();
    }

    @Test
    public void canSendThenListReadReceipts() throws InterruptedException {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.sendReadReceipt(response.getId()).block();

        Thread.sleep(500);

        PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
    }

    @Test
    public void canSendThenListReadReceiptsWithResponse() throws InterruptedException {
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        SendChatMessageResult response = chatThreadClient.sendMessage(messageRequest).block();

        chatThreadClient.sendReadReceiptWithResponse(response.getId()).block();

        Thread.sleep(500);

        PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

        // process the iterableByPage
        List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response.getId());
    }
}
