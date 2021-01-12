// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
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

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstParticipant, scopes);

        client = getChatClientBuilder(response.getToken(), httpClient).buildAsyncClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(firstParticipant.getId(),
                secondParticipant.getId());

        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest).block();
        chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
        threadId = chatThreadClient.getChatThreadId();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        String newTopic = "Update Test";

        // Act & Assert
        StepVerifier.create(
                chatThreadClient.updateTopic(newTopic)
                    .flatMap(noResp -> {
                        return client.getChatThread(threadId);
                    })
            )
            .assertNext(chatThread -> {
                assertEquals(chatThread.getTopic(), newTopic);
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThreadWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        String newTopic = "Update Test";

        // Act & Assert
        StepVerifier.create(
                chatThreadClient.updateTopicWithResponse(newTopic)
                    .flatMap(updateThreadResponse -> {
                        assertEquals(204, updateThreadResponse.getStatusCode());
                        return client.getChatThread(threadId);
                    })

            )
            .assertNext(chatThread -> {
                assertEquals(chatThread.getTopic(), newTopic);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipantsAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        AddChatParticipantsOptions options = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipants(options))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());

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
            });

        for (ChatParticipant participant: options.getParticipants()) {
            StepVerifier.create(chatThreadClient.removeParticipant(participant.getUser()))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveParticipantsWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        AddChatParticipantsOptions options = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipantsWithResponse(options))
            .assertNext(addParticipantsResponse -> {
                assertEquals(207, addParticipantsResponse.getStatusCode());
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());

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
            });

        for (ChatParticipant participant: options.getParticipants()) {
            StepVerifier.create(chatThreadClient.removeParticipantWithResponse(participant.getUser()))
                .assertNext(resp -> {
                    assertEquals(204, resp.getStatusCode());
                })
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        CommunicationUser participant = communicationClient.createUser();

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipant(new ChatParticipant().setUser(participant)))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());
                assertTrue(participantsResponse.stream().anyMatch(p -> p.getUser().getId().equals(participant.getId())));
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient);
        CommunicationUser participant = communicationClient.createUser();

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipantWithResponse(new ChatParticipant().setUser(participant)))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());
                assertTrue(participantsResponse.stream().anyMatch(p -> p.getUser().getId().equals(participant.getId())));
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetHtmlMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient);
        SendChatMessageOptions messageRequest = new SendChatMessageOptions()
            .setType(ChatMessageType.HTML)
            .setPriority(ChatMessagePriority.NORMAL)
            .setSenderDisplayName("John")
            .setContent("<div>test</div>");

        // Action & Assert
        StepVerifier
            .create(chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    return chatThreadClient.getMessage(response);
                })
            )
            .assertNext(message -> {
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
                assertEquals(message.getType(), messageRequest.getType());
                assertEquals(message.getPriority(), messageRequest.getPriority());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
            })
            .verifyComplete();
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
                    return chatThreadClient.getMessage(response);
                })
            )
            .assertNext(message -> {
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
                assertEquals(message.getPriority(), messageRequest.getPriority());
                assertEquals(message.getType(), messageRequest.getType());
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
                    assertEquals(201, sendResponse.getStatusCode());
                    return chatThreadClient.getMessageWithResponse(sendResponse.getValue());
                })
            )
            .assertNext(getResponse -> {
                ChatMessage message = getResponse.getValue();
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
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
                    return chatThreadClient.deleteMessage(response);
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
                    return chatThreadClient.deleteMessageWithResponse(response);
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.updateMessage(response, updateMessageRequest);
                })
                .flatMap((Void resp) -> {
                    return chatThreadClient.getMessage(messageResponseRef.get());
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(
                chatThreadClient.sendMessage(messageRequest)
                    .flatMap((String response) -> {
                        messageResponseRef.set(response);
                        return chatThreadClient.updateMessageWithResponse(response, updateMessageRequest);
                    })
                    .flatMap((Response<Void> updateResponse) -> {
                        assertEquals(204, updateResponse.getStatusCode());
                        return chatThreadClient.getMessage(messageResponseRef.get());
                    })
                )
            .assertNext(message -> {
                assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());
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
                    assertEquals(200, resp.getStatusCode());
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
                    assertEquals(200, resp.getStatusCode());
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
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.sendReadReceipt(response);
                })
            )
            .assertNext(noResp -> {
                PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = new PagedIterable<ChatMessageReadReceipt>(chatThreadClient.listReadReceipts());

                // process the iterableByPage
                List<ChatMessageReadReceipt> returnedReadReceipts = new ArrayList<>();
                readReceiptsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getItems().forEach(item -> returnedReadReceipts.add(item));
                });
                assertTrue(returnedReadReceipts.size() > 0);
                checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get());
            });
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response);
                    return chatThreadClient.sendReadReceiptWithResponse(response);
                })
            )
            .assertNext(receiptResponse -> {
                assertEquals(201, receiptResponse.getStatusCode());
                PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = new PagedIterable<>(chatThreadClient.listReadReceipts());

                // process the iterableByPage
                List<ChatMessageReadReceipt> returnedReadReceipts = new ArrayList<>();
                readReceiptsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getItems().forEach(item -> returnedReadReceipts.add(item));
                });

                assertTrue(returnedReadReceipts.size() > 0);
                checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get());
            });

    }
}
