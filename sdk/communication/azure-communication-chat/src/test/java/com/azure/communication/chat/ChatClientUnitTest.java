// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import com.azure.communication.chat.models.ChatMessageReadReceipt;
import com.azure.communication.chat.models.ChatParticipant;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.chat.models.InvalidParticipantException;
import com.azure.communication.chat.models.ListReadReceiptOptions;
import com.azure.communication.chat.models.SendChatMessageOptions;
import com.azure.communication.common.CommunicationTokenCredential;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.NoOpHttpClient;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static com.azure.communication.chat.ChatClientTestBase.ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ChatClientUnitTest {

    @Test
    public void canListReadReceipts() {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };

        ChatThreadAsyncClient chatThreadAsyncClient = getChatThreadAsyncClient(mockHttpClient);

        PagedFlux<ChatMessageReadReceipt> readReceipts = chatThreadAsyncClient.listReadReceipts();

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
        readReceipts.toIterable().forEach(readReceiptList::add);

        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }

    @Test
    public void canListReadReceiptsWithOptions() {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };

        ChatThreadAsyncClient chatThreadAsyncClient = getChatThreadAsyncClient(mockHttpClient);

        PagedFlux<ChatMessageReadReceipt> readReceipts = chatThreadAsyncClient.listReadReceipts(
            new ListReadReceiptOptions().setMaxPageSize(1));

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
        readReceipts.toIterable().forEach(readReceiptList::add);

        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }

    @Test
    public void canProcessInvalidParticipantsOnCreateChatThread() {

        CreateChatThreadOptions threadRequest = new CreateChatThreadOptions("topic");

        threadRequest.addParticipant(new ChatParticipant()
            .setCommunicationIdentifier(new CommunicationUserIdentifier("valid"))
        );

        CommunicationUserIdentifier invalidUser = new CommunicationUserIdentifier("invalid");
        threadRequest.addParticipant(new ChatParticipant()
            .setCommunicationIdentifier(invalidUser));

        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createChatThreadInvalidParticipantResponse(request, threadRequest, invalidUser));
            }
        };

        StepVerifier.create(getChatAsyncClient(mockHttpClient).createChatThread(threadRequest))
            .assertNext(result -> {
                assertNotNull(result);
                assertNotNull(result.getChatThread());
                assertNotNull(result.getChatThread().getId());
                assertEquals(1, result.getInvalidParticipants().size());
                assertEquals(invalidUser.getId(), result.getInvalidParticipants().stream().findFirst().get().getTarget());
            })
            .verifyComplete();
    }

    @SyncAsyncTest
    public void throwsExceptionOnBadRequest() {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createErrorResponse(request, 400));
            }
        };
        ChatThreadAsyncClient chatThreadAsyncClient = getChatThreadAsyncClient(mockHttpClient);
        ChatThreadClient chatThreadClient = getChatThreadClient(mockHttpClient);

        assertThrows(HttpResponseException.class, () -> SyncAsyncExtension.execute(
            () -> chatThreadClient.sendMessage(new SendChatMessageOptions()),
            () -> chatThreadAsyncClient.sendMessage(new SendChatMessageOptions())
        ));
    }

    @Test
    public void canListReadReceiptsWithContext() {
        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.createReadReceiptsResponse(request));
            }
        };
        PagedIterable<ChatMessageReadReceipt> readReceipts = getChatThreadClient(mockHttpClient).listReadReceipts();

        // // process the iterableByPage
        List<ChatMessageReadReceipt> readReceiptList = new ArrayList<ChatMessageReadReceipt>();
        readReceipts.iterableByPage().forEach(resp -> {
            assertEquals(resp.getStatusCode(), 200);
            resp.getItems().forEach(item -> readReceiptList.add(item));
        });
        assertEquals(readReceiptList.size(), 2);
        assertNotNull(readReceiptList.get(0).getChatMessageId());
        assertNotNull(readReceiptList.get(0).getReadOn());
        assertNotNull(readReceiptList.get(0).getSender());
    }

    @Test
    public void canAddSingleParticipantWithErrorAsync() {
        // Arrange
        CommunicationUserIdentifier participant = new CommunicationUserIdentifier("000");

        HttpClient mockHttpClient = new NoOpHttpClient() {
            @Override
            public Mono<HttpResponse> send(HttpRequest request) {
                return Mono.just(ChatResponseMocker.addParticipantsInvalidParticipantResponse(request, participant));
            }
        };
        // Action & Assert
        StepVerifier.create(getChatThreadAsyncClient(mockHttpClient).addParticipantWithResponse(new ChatParticipant().setCommunicationIdentifier(participant)))
            .expectErrorMatches(err -> err instanceof InvalidParticipantException);
    }

    private static ChatThreadAsyncClient getChatThreadAsyncClient(HttpClient mockHttpClient) {
        String threadId = "19:4b72178530934b7790135dd9359205e0@thread.v2";
        return getChatAsyncClient(mockHttpClient).getChatThreadClient(threadId);
    }

    private static ChatAsyncClient getChatAsyncClient(HttpClient mockHttpClient) {
        String mockToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMl9pbnQiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoic3Bvb2w6NTdiOWJhYzktZGY2Yy00ZDM5LWE3M2ItMjZlOTQ0YWRmNmVhXzNmMDExNi03YzAwOTQ5MGRjIiwic2NwIjoxNzkyLCJjc2kiOiIxNTk3ODcyMDgyIiwiaWF0IjoxNTk3ODcyMDgyLCJleHAiOjE1OTc5NTg0ODIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiI1N2I5YmFjOS1kZjZjLTRkMzktYTczYi0yNmU5NDRhZGY2ZWEifQ.l2UXI0KH2LXZQoz7FPsfLZS0CX8cYsnW3CMECfqwuncV8WqrTD7RbqZDfAaYXn0t5sHrGM4CRbpx4LwIZhXOlmsmOdTdHSsPUCIqJscwNjQmltvOrIt11DOmObQ63w0kYq9QrlB-lyZNzTEAED2FhMwBAbhZOokRtFajYD7KvJb1w9oUXousQ_z6zZqjbt1Cy4Ll3zO1GR4G7yRV8vK3bLnN2IWPaEkoqx8PHeHLa9Cb4joowseRfQxFHv28xcCF3r9SBCauUeJcmbwBmnOAOLS-EAJTLiGhil7m3BNyLN5RnYbsK5ComtL2-02TbkPilpy21OhW0MJkicSFlCbYvg";

        return new ChatClientBuilder()
            .endpoint(ENDPOINT)
            .httpClient(mockHttpClient)
            .credential(new CommunicationTokenCredential(mockToken))
            .buildAsyncClient();
    }

    private static ChatThreadClient getChatThreadClient(HttpClient mockHttpClient) {
        String threadId = "19:4b72178530934b7790135dd9359205e0@thread.v2";
        return getChatClient(mockHttpClient).getChatThreadClient(threadId);
    }

    private static ChatClient getChatClient(HttpClient mockHttpClient) {
        String mockToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEwMl9pbnQiLCJ0eXAiOiJKV1QifQ.eyJza3lwZWlkIjoic3Bvb2w6NTdiOWJhYzktZGY2Yy00ZDM5LWE3M2ItMjZlOTQ0YWRmNmVhXzNmMDExNi03YzAwOTQ5MGRjIiwic2NwIjoxNzkyLCJjc2kiOiIxNTk3ODcyMDgyIiwiaWF0IjoxNTk3ODcyMDgyLCJleHAiOjE1OTc5NTg0ODIsImFjc1Njb3BlIjoiY2hhdCIsInJlc291cmNlSWQiOiI1N2I5YmFjOS1kZjZjLTRkMzktYTczYi0yNmU5NDRhZGY2ZWEifQ.l2UXI0KH2LXZQoz7FPsfLZS0CX8cYsnW3CMECfqwuncV8WqrTD7RbqZDfAaYXn0t5sHrGM4CRbpx4LwIZhXOlmsmOdTdHSsPUCIqJscwNjQmltvOrIt11DOmObQ63w0kYq9QrlB-lyZNzTEAED2FhMwBAbhZOokRtFajYD7KvJb1w9oUXousQ_z6zZqjbt1Cy4Ll3zO1GR4G7yRV8vK3bLnN2IWPaEkoqx8PHeHLa9Cb4joowseRfQxFHv28xcCF3r9SBCauUeJcmbwBmnOAOLS-EAJTLiGhil7m3BNyLN5RnYbsK5ComtL2-02TbkPilpy21OhW0MJkicSFlCbYvg";

        return new ChatClientBuilder()
            .endpoint(ENDPOINT)
            .httpClient(mockHttpClient)
            .credential(new CommunicationTokenCredential(mockToken))
            .buildClient();
    }
}
