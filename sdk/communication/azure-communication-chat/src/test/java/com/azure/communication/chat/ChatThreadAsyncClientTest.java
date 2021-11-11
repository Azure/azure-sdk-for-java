// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.chat.implementation.models.CommunicationErrorResponseException;
import com.azure.core.exception.HttpResponseException;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.identity.models.CommunicationTokenScope;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.communication.chat.implementation.ChatOptionsProvider;
import com.azure.communication.chat.models.*;
import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.test.http.NoOpHttpClient;
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

    private CommunicationUserIdentifier firstParticipant;
    private CommunicationUserIdentifier secondParticipant;
    private CommunicationUserIdentifier firstAddedParticipant;
    private CommunicationUserIdentifier secondAddedParticipant;

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    private void setupTest(HttpClient httpClient, String testName) {
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        firstParticipant = communicationClient.createUser();
        secondParticipant = communicationClient.createUser();

        List<CommunicationTokenScope> scopes = Arrays.asList(CommunicationTokenScope.CHAT);
        AccessToken response = communicationClient.getToken(firstParticipant, scopes);

        ChatClientBuilder chatBuilder = getChatClientBuilder(response.getToken(), httpClient);
        client = addLoggingPolicyForIdentityClientBuilder(chatBuilder, testName).buildAsyncClient();

        CreateChatThreadOptions threadRequest = ChatOptionsProvider.createThreadOptions(firstParticipant.getId(),
            secondParticipant.getId());

        CreateChatThreadResult createChatThreadResult = client.createChatThread(threadRequest).block();
        chatThreadClient = client.getChatThreadClient(createChatThreadResult.getChatThread().getId());
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
        String newTopic = "Update Test";

        // Act & Assert
        StepVerifier.create(
            chatThreadClient.updateTopic(newTopic)
                .flatMap(noResp -> {
                    return chatThreadClient.getProperties();
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
        setupTest(httpClient, "canUpdateThreadWithResponse");
        String newTopic = "Update Test";

        // Act & Assert
        StepVerifier.create(
            chatThreadClient.updateTopicWithResponse(newTopic)
                .flatMap(updateThreadResponse -> {
                    assertEquals(204, updateThreadResponse.getStatusCode());
                    return chatThreadClient.getProperties();
                })

        )
            .assertNext(chatThread -> {
                assertEquals(chatThread.getTopic(), newTopic);
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotUpdateThreadWithNullTopic(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateThreadWithNullTopic");

        // Act & Assert
        StepVerifier.create(chatThreadClient.updateTopic(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotUpdateThreadWithResponseWithNullTopic(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotUpdateThreadWithResponseWithNullTopic");

        // Act & Assert
        StepVerifier.create(chatThreadClient.updateTopicWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersAsync");
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        Iterable<ChatParticipant> participants = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipants(participants))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse =
                    new PagedIterable<>(chatThreadClient.listParticipants());

                // process the iterableByPage
                List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
                participantsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getItems().forEach(item -> returnedParticipants.add(item));
                });

                for (ChatParticipant participant : participants) {
                    assertTrue(checkParticipantsListContainsParticipantId(returnedParticipants,
                        ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
                }
                assertTrue(returnedParticipants.size() == 4);
            });

        for (ChatParticipant participant : participants) {
            StepVerifier.create(chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithOptionsAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersWithOptionsAsync");
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        Iterable<ChatParticipant> participants = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipants(participants))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse =
                    new PagedIterable<>(chatThreadClient.listParticipants(new ListParticipantsOptions().setMaxPageSize(2)));

                // process the iterableByPage
                List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
                participantsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getItems().forEach(item -> returnedParticipants.add(item));
                });

                for (ChatParticipant participant : participants) {
                    assertTrue(checkParticipantsListContainsParticipantId(returnedParticipants,
                        ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
                }
                assertTrue(returnedParticipants.size() == 4);
            });

        for (ChatParticipant participant : participants) {
            StepVerifier.create(chatThreadClient.removeParticipant(participant.getCommunicationIdentifier()))
                .verifyComplete();
        }
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListWithContextAndRemoveMembersAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListWithContextAndRemoveMembersAsync");

        // Act & Assert
        PagedFlux<ChatParticipant> membersResponse = chatThreadClient.listParticipants();

        List<ChatParticipant> returnedMembers = new ArrayList<ChatParticipant>();
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
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        Iterable<ChatParticipant> participants = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipants(participants))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> membersResponse = new PagedIterable<>(chatThreadClient.listParticipants());

                // process the iterableByPage
                List<ChatParticipant> returnedMembers = new ArrayList<ChatParticipant>();
                membersResponse.iterableByPage().forEach(resp -> {
                    assertEquals(resp.getStatusCode(), 200);
                    resp.getItems().forEach(item -> returnedMembers.add(item));
                });

                for (ChatParticipant member : participants) {
                    assertTrue(checkParticipantsListContainsParticipantId(returnedMembers,
                        ((CommunicationUserIdentifier) member.getCommunicationIdentifier()).getId()));
                }
                assertTrue(returnedMembers.size() == 4);
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddListAndRemoveMembersWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddListAndRemoveMembersWithResponseAsync");
        firstAddedParticipant = communicationClient.createUser();
        secondAddedParticipant = communicationClient.createUser();

        Iterable<ChatParticipant> participants = ChatOptionsProvider.addParticipantsOptions(
            firstAddedParticipant.getId(), secondAddedParticipant.getId());

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipantsWithResponse(participants))
            .assertNext(addParticipantsResponse -> {
                assertEquals(207, addParticipantsResponse.getStatusCode());
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());

                // process the iterableByPage
                List<ChatParticipant> returnedParticipants = new ArrayList<ChatParticipant>();
                participantsResponse.iterableByPage().forEach(resp -> {
                    assertEquals(200, resp.getStatusCode());
                    resp.getItems().forEach(item -> returnedParticipants.add(item));
                });

                for (ChatParticipant participant : participants) {
                    assertTrue(checkParticipantsListContainsParticipantId(returnedParticipants,
                        ((CommunicationUserIdentifier) participant.getCommunicationIdentifier()).getId()));
                }

                assertTrue(returnedParticipants.size() == 4);
            });

        for (ChatParticipant participant : participants) {
            StepVerifier.create(chatThreadClient.removeParticipantWithResponse(participant.getCommunicationIdentifier()))
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
        setupTest(httpClient, "canAddSingleParticipantAsync");
        CommunicationUserIdentifier participant = communicationClient.createUser();

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipant(new ChatParticipant().setCommunicationIdentifier(participant)))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());
                assertTrue(participantsResponse
                    .stream()
                    .anyMatch(p -> ((CommunicationUserIdentifier) p.getCommunicationIdentifier()).getId().equals(participant.getId())));
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithResponseAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canAddSingleParticipantWithResponseAsync");
        CommunicationUserIdentifier participant = communicationClient.createUser();

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipantWithResponse(new ChatParticipant().setCommunicationIdentifier(participant)))
            .assertNext(noResp -> {
                PagedIterable<ChatParticipant> participantsResponse = new PagedIterable<>(chatThreadClient.listParticipants());
                assertTrue(participantsResponse
                    .stream()
                    .anyMatch(p -> ((CommunicationUserIdentifier) p.getCommunicationIdentifier()).getId().equals(participant.getId())));
            });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canAddSingleParticipantWithErrorAsync(HttpClient httpClient) throws InterruptedException {
        // Arrange
        CommunicationUserIdentifier participant = new CommunicationUserIdentifier("000");

        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.addParticipantsInvalidParticipantResponse(request, participant));
            }
        };

        String mockToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMl9pbnQiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoic3Bvb2w6NTdiOWJhYzktZGY2Yy00ZDM5LWE3M2ItMjZlOTQ0YWRmNmVhXzNmMDExNi03YzAwOTQ5MGRjIiwic2NwIjoxNzkyLCJjc2kiOiIxNTk3ODcyMDgyIiwiaWF0IjoxNTk3ODcyMDgyLCJleHAiOjE1OTc5NTg0ODIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiI1N2I5YmFjOS1kZjZjLTRkMzktYTczYi0yNmU5NDRhZGY2ZWEifQ.l2UXI0KH2LXZQoz7FPsfLZS0CX8cYsnW3CMECfqwuncV8WqrTD7RbqZDfAaYXn0t5sHrGM4CRbpx4LwIZhXOlmsmOdTdHSsPUCIqJscwNjQmltvOrIt11DOmObQ63w0kYq9QrlB-lyZNzTEAED2FhMwBAbhZOokRtFajYD7KvJb1w9oUXousQ_z6zZqjbt1Cy4Ll3zO1GR4G7yRV8vK3bLnN2IWPaEkoqx8PHeHLa9Cb4joowseRfQxFHv28xcCF3r9SBCauUeJcmbwBmnOAOLS-EAJTLiGhil7m3BNyLN5RnYbsK5ComtL2-02TbkPilpy21OhW0MJkicSFlCbYvg";
        chatThreadClient = getChatThreadClientBuilder(mockToken, mockHttpClient)
            .chatThreadId("thread-id")
            .buildAsyncClient();

        // Action & Assert
        StepVerifier.create(chatThreadClient.addParticipantWithResponse(new ChatParticipant().setCommunicationIdentifier(participant)))
            .expectErrorMatches(err -> err instanceof InvalidParticipantException);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendThenGetHtmlMessage(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "canSendThenGetHtmlMessage");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions(ChatMessageType.HTML, "<div>test</div>");

        // Action & Assert
        StepVerifier
            .create(chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    return chatThreadClient.getMessage(response.getId());
                })
            )
            .assertNext(message -> {
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
                assertEquals(message.getType(), messageRequest.getType());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotAddParticipantsWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotAddParticipantsWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipants(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotAddParticipantsWithResponseWithNullOptions(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotAddParticipantsWithResponseWithNullOptions");

        // Act & Assert
        StepVerifier.create(chatThreadClient.addParticipantsWithResponse(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotRemoveParticipantWithNullUser(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotRemoveParticipantWithNullUser");

        // Act & Assert
        StepVerifier.create(chatThreadClient.removeParticipant(null))
            .verifyError(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void cannotRemoveParticipantWithNullUserWithResponse(HttpClient httpClient) {
        // Arrange
        setupTest(httpClient, "cannotRemoveParticipantWithNullUserWithResponse");

        // Act & Assert
        StepVerifier.create(chatThreadClient.removeParticipantWithResponse(null))
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
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
                assertEquals(message.getType(), messageRequest.getType());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
                assertTrue(message.getMetadata().equals(messageRequest.getMetadata()));
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
                    assertEquals(201, sendResponse.getStatusCode());
                    return chatThreadClient.getMessageWithResponse(sendResponse.getValue().getId());
                })
            )
            .assertNext(getResponse -> {
                ChatMessage message = getResponse.getValue();
                assertEquals(message.getContent().getMessage(), messageRequest.getContent());
                assertEquals(message.getSenderDisplayName(), messageRequest.getSenderDisplayName());
                assertTrue(message.getMetadata().equals(messageRequest.getMetadata()));
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response.getId());
                    return chatThreadClient.updateMessage(response.getId(), updateMessageRequest);
                })
                .flatMap((Void resp) -> {
                    return chatThreadClient.getMessage(messageResponseRef.get());
                })
        )
            .assertNext(message -> {
                assertEquals(message.getContent(), updateMessageRequest.getContent());

                assertFalse(message.getMetadata().containsKey("tags"));
                assertEquals(message.getMetadata().get("deliveryMode"), updateMessageRequest.getMetadata().get("deliveryMode"));
                assertEquals(message.getMetadata().get("onedriveReferences"), updateMessageRequest.getMetadata().get("onedriveReferences"));
                assertEquals(message.getMetadata().get("amsreferences"), messageRequest.getMetadata().get("amsreferences"));
                assertEquals(message.getMetadata().get("key"), messageRequest.getMetadata().get("key"));
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
        AtomicReference<String> messageResponseRef = new AtomicReference<>();
        StepVerifier.create(chatThreadClient.sendMessage(messageRequest)
            .flatMap((SendChatMessageResult response) -> {
                messageResponseRef.set(response.getId());
                return chatThreadClient.updateMessageWithResponse(response.getId(), updateMessageRequest);
            })
            .flatMap((Response<Void> updateResponse) -> {
                assertEquals(204, updateResponse.getStatusCode());
                return chatThreadClient.getMessage(messageResponseRef.get());
            })
        )
            .assertNext(message -> {
                assertEquals(message.getContent().getMessage(), updateMessageRequest.getContent());

                assertFalse(message.getMetadata().containsKey("tags"));
                assertEquals(message.getMetadata().get("deliveryMode"), updateMessageRequest.getMetadata().get("deliveryMode"));
                assertEquals(message.getMetadata().get("onedriveReferences"), updateMessageRequest.getMetadata().get("onedriveReferences"));
                assertEquals(message.getMetadata().get("amsreferences"), messageRequest.getMetadata().get("amsreferences"));
                assertEquals(message.getMetadata().get("key"), messageRequest.getMetadata().get("key"));
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
        setupTest(httpClient, "canListMessagesWithOptions");
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
                assertEquals(200, response.getStatusCode());
            })
            .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void canSendTypingNotificationWithResponseWithOptions(HttpClient httpClient) {
        // Action & Assert
        TypingNotificationOptions options = new TypingNotificationOptions();
        options.setSenderDisplayName("Sender");

        setupTest(httpClient, "canSendTypingNotificationWithResponseWithOptions");
        StepVerifier.create(chatThreadClient.sendTypingNotificationWithResponse(options))
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
        setupTest(httpClient, "canSendThenListReadReceipts");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        AtomicReference<String> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response.getId());
                    return chatThreadClient.sendReadReceipt(response.getId());
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
    public void canSendThenListReadReceiptsWithOptions(HttpClient httpClient) throws InterruptedException {
        // Arrange
        setupTest(httpClient, "canSendThenListReadReceiptsWithOptions");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        AtomicReference<String> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response.getId());
                    return chatThreadClient.sendReadReceipt(response.getId());
                })
        )
            .assertNext(noResp -> {
                PagedIterable<ChatMessageReadReceipt> readReceiptsResponse = new PagedIterable<ChatMessageReadReceipt>(
                    chatThreadClient.listReadReceipts(new ListReadReceiptOptions().setMaxPageSize(1)));

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
        setupTest(httpClient, "canSendThenListReadReceiptsWithResponse");
        SendChatMessageOptions messageRequest = ChatOptionsProvider.sendMessageOptions();
        AtomicReference<String> messageResponseRef = new AtomicReference<>();

        // Action & Assert
        StepVerifier.create(
            chatThreadClient.sendMessage(messageRequest)
                .flatMap(response -> {
                    messageResponseRef.set(response.getId());
                    return chatThreadClient.sendReadReceiptWithResponse(response.getId());
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
        PagedFlux<ChatMessageReadReceipt> readReceipts = chatThreadClient.listReadReceipts();

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
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
    public void canListReadReceiptsWithOptions(HttpClient httpClient) {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };
        setupUnitTest(mockHttpClient);
        PagedFlux<ChatMessageReadReceipt> readReceipts = chatThreadClient.listReadReceipts(
            new ListReadReceiptOptions().setMaxPageSize(1));

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
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
        PagedFlux<ChatMessageReadReceipt> readReceipts = chatThreadClient.listReadReceipts();

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
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
                .flatMap(response -> {
                    return chatThreadClient.sendReadReceipt(response.getId());
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
                .flatMap((SendChatMessageResult response) -> {
                    return chatThreadClient.sendReadReceiptWithResponse(response.getId());
                }))
            .assertNext((Response<Void> response) -> {
                assertEquals(200, response.getStatusCode());
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void throwsExceptionOnBadRequest(HttpClient httpClient) {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createErrorResponse(request, 400));
            }
        };
        setupUnitTest(mockHttpClient);

        StepVerifier.create(chatThreadClient.sendMessage(new SendChatMessageOptions()))
            .verifyErrorMatches(ex ->
                ex instanceof HttpResponseException && !(ex instanceof CommunicationErrorResponseException));
    }


}
