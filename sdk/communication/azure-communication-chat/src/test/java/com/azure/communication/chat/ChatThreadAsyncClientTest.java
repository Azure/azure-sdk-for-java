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
import reactor.test.StepVerifier;

import com.azure.communication.administration.CommunicationIdentityClient;
import com.azure.communication.administration.CommunicationUserToken;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.util.Context;
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
    private ChatClientBuilder chatBuilder;
    private ChatAsyncClient client;
    private ChatThreadAsyncClient chatThreadClient;
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

        List<String> scopes = new ArrayList<String>(Arrays.asList("chat"));
        CommunicationUserToken response = communicationClient.issueToken(firstThreadMember, scopes);

        ChatClientBuilder chatBuilder = getChatClientBuilder(response.getToken(), httpClient);
        client = addLoggingPolicyForIdentityClientBuilder(chatBuilder, testName).buildAsyncClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(firstThreadMember.getId(),
                secondThreadMember.getId());
        chatThreadClient = client.createChatThread(threadRequest).block();
        threadId = chatThreadClient.getChatThreadId();
    }

    private void setupUnitTest(HttpClient mockHttpClient) {
        String threadId = "19:4b72178530934b7790135dd9359205e0@thread.v2";
        String mockToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMl9pbnQiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoic3Bvb2w6NTdiOWJhYzktZGY2Yy00ZDM5LWE3M2ItMjZlOTQ0YWRmNmVhXzNmMDExNi03YzAwOTQ5MGRjIiwic2NwIjoxNzkyLCJjc2kiOiIxNTk3ODcyMDgyIiwiaWF0IjoxNTk3ODcyMDgyLCJleHAiOjE1OTc5NTg0ODIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiI1N2I5YmFjOS1kZjZjLTRkMzktYTczYi0yNmU5NDRhZGY2ZWEifQ.l2UXI0KH2LXZQoz7FPsfLZS0CX8cYsnW3CMECfqwuncV8WqrTD7RbqZDfAaYXn0t5sHrGM4CRbpx4LwIZhXOlmsmOdTdHSsPUCIqJscwNjQmltvOrIt11DOmObQ63w0kYq9QrlB-lyZNzTEAED2FhMwBAbhZOokRtFajYD7KvJb1w9oUXousQ_z6zZqjbt1Cy4Ll3zO1GR4G7yRV8vK3bLnN2IWPaEkoqx8PHeHLa9Cb4joowseRfQxFHv28xcCF3r9SBCauUeJcmbwBmnOAOLS-EAJTLiGhil7m3BNyLN5RnYbsK5ComtL2-02TbkPilpy21OhW0MJkicSFlCbYvg";
        client = getChatClientBuilder(mockToken, mockHttpClient).buildAsyncClient();
        chatThreadClient = client.getChatThreadClient(threadId);
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateThread(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateThread");
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
        setupTest(httpClient, "canUpdateThreadWithResponse");
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
    public void cannotUpdateThreadWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateThreadWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.updateChatThread(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotUpdateThreadWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateThreadWithResponseWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.updateChatThreadWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersAsync");
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
    public void canAddListWithContextAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListWithContextAndRemoveMembersAsync");

        // Act & Assert
        PagedFlux<ChatThreadMember> membersResponse = chatThreadClient.listMembers(Context.NONE);

        List<ChatThreadMember> returnedMembers = new ArrayList<ChatThreadMember>();
        membersResponse.toIterable().forEach(item -> {
            returnedMembers.add(item);
        });
        assertEquals(returnedMembers.size(), 2);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListWithContextMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListWithContextMembersAsync");
        firstAddedThreadMember = communicationClient.createUser();
        secondAddedThreadMember = communicationClient.createUser();

        AddChatThreadMembersOptions options = ChatOptionsProvider.addThreadMembersOptions(
            firstAddedThreadMember.getId(), secondAddedThreadMember.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addMembers(options))
            .assertNext(noResp -> {
                PagedIterable<ChatThreadMember> membersResponse = new PagedIterable<>(chatThreadClient.listMembers(Context.NONE));

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
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersWithResponseAsync");
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
            });

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
    public void cannotAddMembersWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotAddMembersWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.addMembers(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotAddMembersWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotAddMembersWithResponseWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.addMembersWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotRemoveMembersWithNullUser(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotRemoveMembersWithNullUser");

        // Act & Assert
        StepVerifier.create(chatThreadClient.removeMember(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotRemoveMembersWithNullUserWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotRemoveMembersWithNullUserWithResponse");

        // Act & Assert
        StepVerifier.create(chatThreadClient.removeMemberWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendThenGetMessage");
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
        setupTest(httpClient, "canSendThenGetMessageWithResponse");
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
    public void cannotSendMessageWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotSendMessageWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.sendMessage(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotSendMessageWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotSendMessageWithResponseWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.sendMessageWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotGetMessageNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotGetMessageNullId");

        // Act & Assert
        StepVerifier.create(chatThreadClient.getMessage(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotGetMessageWithReponseNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotGetMessageWithReponseNullId");

        // Act & Assert
        StepVerifier.create(chatThreadClient.getMessageWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteExistingMessage");
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
    public void cannotDeleteMessageNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotDeleteMessageNullId");

        // Act & Assert
        StepVerifier.create(chatThreadClient.deleteMessage(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotDeleteMessageWithRepsonseNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotDeleteMessageWithRepsonseNullId");

        // Act & Assert
        StepVerifier.create(chatThreadClient.deleteMessageWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canDeleteExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canDeleteExistingMessageWithResponse");
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
        setupTest(httpClient, "canUpdateExistingMessage");
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
    public void cannotUpdateMessageNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateMessageNullId");
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        // Action & Assert
        StepVerifier.create(chatThreadClient.updateMessage(null, updateMessageRequest))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotUpdateMessageWithResponseNullId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateMessageWithResponseNullId");
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        // Action & Assert
        StepVerifier.create(chatThreadClient.updateMessageWithResponse(null, updateMessageRequest))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canUpdateExistingMessageWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canUpdateExistingMessageWithResponse");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        UpdateChatMessageOptions updateMessageRequest = ChatOptionsProvider.updateMessageOptions();

        // Action & Assert
        AtomicReference<SendChatMessageResult> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(chatThreadClient.sendMessage(messageRequest)
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
        setupTest(httpClient, "canListMessages");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        // Action & Assert
        StepVerifier
            .create(
                chatThreadClient.sendMessage(messageRequest)
                    .flatMap(message -> {
                        return chatThreadClient.listMessages().next();
                    }))
            .assertNext((ChatMessage message) -> {
                assertNotNull(message);
                assertEquals(message.getType(), "Text");
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canListMessagesWithOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canListMessagesWithOptions");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        ListChatMessagesOptions options = new ListChatMessagesOptions();
        options.setMaxPageSize(10);
        options.setStartTime(OffsetDateTime.parse("2020-09-08T01:02:14.387Z"));

        // Action & Assert
        StepVerifier
            .create(
                chatThreadClient.sendMessage(messageRequest)
                    .flatMap(message -> {
                        return chatThreadClient.listMessages(options).next();
                    }))
            .assertNext((ChatMessage message) -> {
                assertNotNull(message);
                assertEquals(message.getType(), "Text");
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotification(HttpClient httpClient) {
        // Action & Assert
        setupTest(httpClient, "canSendTypingNotification");
        StepVerifier.create(chatThreadClient.sendTypingNotification())
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponse(HttpClient httpClient) {
        // Action & Assert
        setupTest(httpClient, "canSendTypingNotificationWithResponse");
        StepVerifier.create(chatThreadClient.sendTypingNotificationWithResponse())
            .assertNext(response -> {
                assertEquals(response.getStatusCode(), 200);
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
        setupTest(httpClient, "canSendThenListReadReceipts");
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
                assertTrue(returnedReadReceipts.size() > 0);
                checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get().getId());
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @DisabledIfEnvironmentVariable(
        named = "SKIP_LIVE_TEST",
        matches = "(?i)(true)")
    public void canSendThenListReadReceiptsWithResponse(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendThenListReadReceiptsWithResponse");
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

                assertTrue(returnedReadReceipts.size() > 0);
                checkReadReceiptListContainsMessageId(returnedReadReceipts, messageResponseRef.get().getId());
            });
            
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
        PagedFlux<ReadReceipt> readReceipts = chatThreadClient.listReadReceipts();

        // // process the iterableByPage
        List<ReadReceipt> readReceiptList = new ArrayList<ReadReceipt>();
        readReceipts.toIterable().forEach(receipt -> {
            readReceiptList.add(receipt);
        });
        
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
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
        PagedFlux<ReadReceipt> readReceipts = chatThreadClient.listReadReceipts(Context.NONE);

        // // process the iterableByPage
        List<ReadReceipt> readReceiptList = new ArrayList<ReadReceipt>();
        readReceipts.toIterable().forEach(receipt -> {
            readReceiptList.add(receipt);
        });
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }
    
    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendReadReceipt(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendReadReceipt");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        StepVerifier
            .create(chatThreadClient.sendMessage(messageRequest)
                .flatMap(message -> {
                    return chatThreadClient.sendReadReceipt(message.getId());
                }))
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendReadReceiptWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendReadReceiptWithResponse");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();

        // Action & Assert
        StepVerifier
           .create(chatThreadClient.sendMessage(messageRequest)
               .flatMap((SendChatMessageResult message) -> {
                   return chatThreadClient.sendReadReceiptWithResponse(message.getId());
               }))
            .assertNext((Response<Void> response) -> {
                assertEquals(201, response.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotSendReadReceiptWithNullMessageId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotSendReadReceiptWithNullMessageId");

        // Action & Assert
        StepVerifier.create(chatThreadClient.sendReadReceipt(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotSendReadReceiptWithResponseWithNullMessageId(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotSendReadReceiptWithResponseWithNullMessageId");

        // Action & Assert
        StepVerifier.create(chatThreadClient.sendReadReceiptWithResponse(null))
            .verifyError(NullPointerException.class);
    }
}
