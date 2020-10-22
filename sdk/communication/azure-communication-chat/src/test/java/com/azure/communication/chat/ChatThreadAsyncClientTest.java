// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.test.StepVerifier;

import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUser;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Set the AZURE_TEST_MODE environment variable to either PLAYBACK or RECORD to
 * determine if tests are playback or live. By default, tests are run in
 * playback mode.
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

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(),
                secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest).block();
        threadId = chatThreadClient.getChatThreadId();
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        // Act & Assert
        StepVerifier.create(
                chatThreadClient.updateChatThread(threadRequest)
                    .flatMap(noResp -> {
                        return client.getChatThread(threadId);
                    })
            )
            .assertNext(chatThread -> {
                assertEquals(chatThread.getTopic(), threadRequest.getTopic());
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        UpdateChatThreadOptions threadRequest = ChatOptionsProvider.updateThreadOptions();

        // Act & Assert
        StepVerifier.create(
                chatThreadClient.updateChatThreadWithResponse(threadRequest)
                    .flatMap(updateThreadResponse -> {
                        assertEquals(updateThreadResponse.getStatusCode(), 200);
                        return client.getChatThread(threadId);
                    })
                
            )       
            .assertNext(chatThread -> {
                assertEquals(chatThread.getTopic(), threadRequest.getTopic());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addMembers(options))
            .assertNext(noResp -> {
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
            });

        for (ChatThreadMember member: options.getMembers()) {
            StepVerifier.create(chatThreadClient.removeMember(member.getUser()))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        // Action & Assert
        StepVerifier.create(chatThreadClient.addMembersWithResponse(options))
            .assertNext(addMembersResponse -> {
                assertEquals(addMembersResponse.getStatusCode(), 207);
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
            })
            .verifyComplete();

        for (ChatThreadMember member: options.getMembers()) {
            StepVerifier.create(chatThreadClient.removeMemberWithResponse(member.getUser()))
                .assertNext(resp -> {
                    assertEquals(resp.getStatusCode(), 204);
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        
        // Action & Assert
        StepVerifier
            .create(chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    return chatThreadClient.getMessage(response.getId());
                })
            )
            .assertNext(message -> {
                assertEquals(message.getContent(), messageRequest.getContent());
                assertEquals(message.getPriority(), messageRequest.getPriority());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        StepVerifier
            .create(chatThreadClient.sendMessageWithResponse(messageRequest)
                .flatMap(sendResponse -> {
                    assertEquals(sendResponse.getStatusCode(), 201);
                    return chatThreadClient.getMessageWithResponse(sendResponse.getValue().getId());
                })
            )
            .assertNext(getResponse -> {
                ChatMessage message = getResponse.getValue();
                assertEquals(message.getContent(), messageRequest.getContent());
                assertEquals(message.getPriority(), messageRequest.getPriority());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    return chatThreadClient.deleteMessage(response.getId());
                })
            )
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    return chatThreadClient.deleteMessageWithResponse(response.getId());
                })
            )
            .assertNext(deleteResponse -> {
                assertEquals(deleteResponse.getStatusCode(), 204);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        // Action & Assert
        AtomicReference<SendChatMessageResult> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.updateMessage(response.getId(), updateMessageRequest);
                })
                .flatMap((Void resp) -> {
                    return chatThreadClient.getMessage(messageResponseRef.get().getId());
                })
            )
            .assertNext(message -> {
                assertEquals(message.getContent(), updateMessageRequest.getContent());
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        // Action & Assert
        AtomicReference<SendChatMessageResult> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(
                chatThreadClient.sendMessage(messageRequest)
                    .flatMap((SendChatMessageResult response) -> {
                        messageResponseRef.set(response);
                        return chatThreadClient.updateMessageWithResponse(response.getId(), updateMessageRequest);
                    })
                    .flatMap((Response<Void> updateResponse) -> {
                        assertEquals(updateResponse.getStatusCode(), 200);
                        return chatThreadClient.getMessage(messageResponseRef.get().getId());
                    })
                )
            .assertNext(message -> {
                assertEquals(message.getContent(), updateMessageRequest.getContent());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessages(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        StepVerifier.create(
                chatThreadClient.sendMessage(messageRequest)
                    .concatWith(chatThreadClient.sendMessage(messageRequest))
            )
            .assertNext((message) -> {
                // Action & Assert
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
            });    
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessagesWithOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .concatWith(chatThreadClient.sendMessage(messageRequest)))
            .assertNext((message) -> {
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
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) {
        // Action & Assert
        setupTest(httpClient);
        StepVerifier.create(chatThreadClient.sendTypingNotification())
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) {
        // Action & Assert
        setupTest(httpClient);
        StepVerifier.create(chatThreadClient.sendTypingNotificationWithResponse())
            .assertNext(response -> {
                assertEquals(response.getStatusCode(), 200);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceipts(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        AtomicReference<SendChatMessageResult> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.sendReadReceipt(response.getId());
                })
            )
            .assertNext(noResp -> {
                PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

                // process the iterableByPage
                List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
                readReceiptsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedReadReceipts.add(item));
                });
        
                if (interceptorManager.isPlaybackMode()) {
                    assertTrue(returnedReadReceipts.size() > 0);
                    checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get().getId());
                }
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        AtomicReference<SendChatMessageResult> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.sendReadReceiptWithResponse(response.getId());
                })
            )
            .assertNext(receiptResponse -> {
                assertEquals(receiptResponse.getStatusCode(), 201);
                PagedIterable<ReadReceipt> readReceiptsResponse = new PagedIterable<ReadReceipt>(chatThreadClient.listReadReceipts());

                // process the iterableByPage
                List<ReadReceipt> returnedReadReceipts = new ArrayList<ReadReceipt>();
                readReceiptsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedReadReceipts.add(item));
                });

                if (interceptorManager.isPlaybackMode()) {
                    assertTrue(returnedReadReceipts.size() > 0);
                    checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get().getId());
                }
            })
            .verifyComplete();
            
    }
}
