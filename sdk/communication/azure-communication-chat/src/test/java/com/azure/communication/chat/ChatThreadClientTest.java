// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
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

    private CommunicationUser firstParticipant;
    private CommunicationUser secondParticipant;
    private CommunicationUser firstAddedParticipant;
    private CommunicationUser secondAddedParticipant;

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
        firstParticipant = communicationClient.createUser();
        secondParticipant = communicationClient.createUser();
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstParticipant, scopes);

        client = getChatClientBuilder(response.getToken(), httpClient).buildClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(
            firstParticipant.getId(), secondParticipant.getId());

        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest);
        chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());

        threadId = chatThreadClient.getChatThreadId();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        String newTopic = "Update Test";

        // Action & Assert
        chatThreadClient.updateTopic(newTopic);

        ChatThread chatThread = client.getChatThread(threadId);
        assertEquals(chatThread.getTopic(), newTopic);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        String newTopic = "Update Test";

         // Action & Assert
        chatThreadClient.updateTopicWithResponse(newTopic, Context.NONE);

        ChatThread chatThread = client.getChatThread(threadId);
        assertEquals(chatThread.getTopic(), newTopic);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipants(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        AddChatParticipantsOptions options = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Action & Assert
        chatThreadClient.addParticipants(options);

        PagedIterable<ChatParticipant> participantsResponse = chatThreadClient.listParticipants();

        // process the iterableByPage
        List<ChatParticipant> returnedparticipants = new ArrayList<ChatParticipant>();
        participantsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedparticipants.add(item));
        });

        for (ChatParticipant participant: options.getParticipants()) {
            assertTrue(checkParticipantsListContainsParticipantId(returnedparticipants, participant.getUser().getId()));
        }

        assertTrue(returnedparticipants.size() == 4);

        for (ChatParticipant participant: options.getParticipants()) {
            chatThreadClient.removeParticipant(participant.getUser());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipantsWithOptions(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        AddChatParticipantsOptions options = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Action & Assert
        chatThreadClient.addParticipants(options);

        PagedIterable<ChatParticipant> participantsResponse = chatThreadClient.listParticipants(
            new ListParticipantsOptions().setMaxPageSize(2));

        // process the iterableByPage
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedParticipants.add(item));
        });

        for (ChatParticipant participant: options.getParticipants()) {
            assertTrue(checkParticipantsListContainsParticipantId(returnedParticipants, participant.getUser().getId()));
        }

        assertTrue(returnedParticipants.size() == 4);

        for (ChatParticipant participant: options.getParticipants()) {
            chatThreadClient.removeParticipant(participant.getUser());
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipantsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        AddChatParticipantsOptions options = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Action & Assert
        chatThreadClient.addParticipantsWithResponse(options, Context.NONE);

        PagedIterable<ChatParticipant> participantsResponse = chatThreadClient.listParticipants();

        // process the iterableByPage
        List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
        participantsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedParticipants.add(item));
        });

        for (ChatParticipant participant: options.getParticipants()) {
            assertTrue(checkParticipantsListContainsParticipantId(returnedParticipants, participant.getUser().getId()));
        }

        assertTrue(returnedParticipants.size() == 4);

        for (ChatParticipant participant: options.getParticipants()) {
            chatThreadClient.removeParticipantWithResponse(participant.getUser(), Context.NONE);
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipant(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        CommunicationUser participant = communicationClient.createUser();

        // Action & Assert
        chatThreadClient.addParticipant(new ChatParticipant().setUser(participant));

        PagedIterable<ChatParticipant> participantsResponse = chatThreadClient.listParticipants();
        assertTrue(participantsResponse.stream().anyMatch(p -> p.getUser().getId().equals(participant.getId())));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        CommunicationUser participant = communicationClient.createUser();

        // Action & Assert
        chatThreadClient.addParticipantWithResponse(new ChatParticipant().setUser(participant), Context.NONE);

        PagedIterable<ChatParticipant> participantsResponse = chatThreadClient.listParticipants();
        assertTrue(participantsResponse.stream().anyMatch(p -> p.getUser().getId().equals(participant.getId())));
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        String response = chatThreadClient.sendMessage(messageRequest);

        ChatMessage message = chatThreadClient.getMessage(response);
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
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
        String response = chatThreadClient.sendMessageWithResponse(messageRequest, Context.NONE).getValue();

        ChatMessage message = chatThreadClient.getMessageWithResponse(response, Context.NONE).getValue();
        assertEquals(message.getContent().getMessage(), messageRequest.getContent());
        assertEquals(message.getPriority(), messageRequest.getPriority());
        assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.deleteMessage(response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.deleteMessageWithResponse(response, Context.NONE);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.updateMessage(response, updateMessageRequest);

        ChatMessage message = chatThreadClient.getMessage(response);
        assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.updateMessageWithResponse(response, updateMessageRequest, Context.NONE);

        ChatMessage message = chatThreadClient.getMessage(response);
        assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
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
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> {
                if (item.getType().equals(ChatMessageType.TEXT)) {
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
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> {
                if (item.getType().equals(ChatMessageType.TEXT)) {
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
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceipts(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceipt(response);

        PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();

        // process the iterableByPage
        List<ChatMessageReadReceipt> returnedReadReceipts = new ArrayList<ChatMessageReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceiptsWithOptions(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceipt(response);

        PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts(
            new ListReadReceiptOptions().setMaxPageSize(1));

        // process the iterableByPage
        List<ChatMessageReadReceipt> returnedReadReceipts = new ArrayList<ChatMessageReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        String response = chatThreadClient.sendMessage(messageRequest);

        // Action & Assert
        chatThreadClient.sendReadReceiptWithResponse(response, Context.NONE);

        PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = chatThreadClient.listReadReceipts();

        // process the iterableByPage
        List<ChatMessageReadReceipt> returnedReadReceipts = new ArrayList<ChatMessageReadReceipt>();
        readReceiptsResponse.iterableByPage().forEach(resp -> {
            assertEquals(200, resp.getStatusCode());
            resp.getItems().forEach(item -> returnedReadReceipts.add(item));
        });

        assertTrue(returnedReadReceipts.size() > 0);
        checkReadReceiptListContainsMessageId(returnedReadReceipts, response);
    }
}
