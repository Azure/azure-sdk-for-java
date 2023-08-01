// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.azure.communication.chat.implementation.models.ChatThreadProperties;
import com.azure.communication.chat.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.chat.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.chat.models.CreateChatThreadOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ChatResponseMocker {

    public static HttpResponse createReadReceiptsResponse(HttpRequest request) {
        String body = String.format("{\"value\":["
            + "{\"senderCommunicationIdentifier\":{\"communicationUser\":{ \"id\":\"8:acs:9b665d53-8164-4923-ad5d-5e983b07d2e7_00000005-334f-e4af-b274-5a3a0d0002f9\"} },\"chatMessageId\":\"1600201311647\",\"readOn\":\"2020-09-15T20:21:51Z\"},"
            + "{\"senderCommunicationIdentifier\":{\"communicationUser\":{ \"id\":\"8:acs:9b665d53-8164-4923-ad5d-5e983b07d2e7_00000005-334f-e4af-b274-5a3a0d0002f9\"} },\"chatMessageId\":\"1600201311648\",\"readOn\":\"2020-09-15T20:21:53Z\"}"
            + "]}");
        return generateMockResponse(body, request, 200);
    }

    public static HttpResponse createChatThreadInvalidParticipantResponse(HttpRequest request, CreateChatThreadOptions options, CommunicationUserIdentifier invalidUser) {
        List<MockCommunicationError> invalidParticipants = new ArrayList<>();
        invalidParticipants.add(new MockCommunicationError()
            .setTarget(invalidUser.getId()));

        MockCreateChatThreadResult result = new MockCreateChatThreadResult()
            .setChatThread(new ChatThreadProperties()
                .setTopic(options.getTopic())
                .setCreatedByCommunicationIdentifier(new CommunicationIdentifierModel()
                    .setCommunicationUser(new CommunicationUserIdentifierModel()
                        .setId("8:acs:000")))
                .setId("000"))
            .setInvalidParticipants(invalidParticipants);

        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return generateMockResponse(body, request, 201);
    }

    public static HttpResponse addParticipantsInvalidParticipantResponse(HttpRequest request, CommunicationUserIdentifier invalidUser) {
        List<MockCommunicationError> invalidParticipants = new ArrayList<>();
        invalidParticipants.add(new MockCommunicationError()
            .setTarget(invalidUser.getId()));

        MockAddChatParticipantsResult result = new MockAddChatParticipantsResult()
            .setInvalidParticipants(invalidParticipants);

        ObjectMapper mapper = new ObjectMapper();
        String body = null;
        try {
            body = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return generateMockResponse(body, request, 201);
    }

    public static HttpResponse createErrorResponse(HttpRequest request, int code) {
        return generateMockResponse("", request, code);
    }

    public static HttpResponse createSendReceiptsResponse(HttpRequest request) {
        return generateMockResponse("testBody", request, 200);
    }

    public static HttpResponse generateMockResponse(String body, HttpRequest request, int statusCode) {
        return new HttpResponse(request) {
            @Override
            public int getStatusCode() {
                return statusCode;
            }

            @Override
            public String getHeaderValue(String name) {
                return null;
            }

            @Override
            public HttpHeaders getHeaders() {
                return new HttpHeaders();
            }

            @Override
            public Flux<ByteBuffer> getBody() {
                return Flux.just(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public Mono<byte[]> getBodyAsByteArray() {
                return Mono.just(body.getBytes(StandardCharsets.UTF_8));
            }

            @Override
            public Mono<String> getBodyAsString() {
                return Mono.just(body);
            }

            @Override
            public Mono<String> getBodyAsString(Charset charset) {
                return Mono.just(body);
            }
        };
    }

    static class MockCreateChatThreadResult {

        @JsonProperty(value = "chatThread")
        private ChatThreadProperties chatThread;

        @JsonProperty(value = "invalidParticipants")
        private List<MockCommunicationError> invalidParticipants;

        public ChatThreadProperties getChatThread() {
            return this.chatThread;
        }

        public MockCreateChatThreadResult setChatThread(ChatThreadProperties chatThread) {
            this.chatThread = chatThread;
            return this;
        }

        public List<MockCommunicationError> getInvalidParticipants() {
            return this.invalidParticipants;
        }

        public MockCreateChatThreadResult setInvalidParticipants(List<MockCommunicationError> invalidParticipants) {
            this.invalidParticipants = invalidParticipants;
            return this;
        }
    }

    static class MockAddChatParticipantsResult {

        @JsonProperty(value = "invalidParticipants")
        private List<MockCommunicationError> invalidParticipants;

        public List<MockCommunicationError> getInvalidParticipants() {
            return this.invalidParticipants;
        }

        public MockAddChatParticipantsResult setInvalidParticipants(List<MockCommunicationError> invalidParticipants) {
            this.invalidParticipants = invalidParticipants;
            return this;
        }
    }

    static class MockCommunicationError {

        @JsonProperty(value = "code")
        private String code;

        @JsonProperty(value = "message")
        private String message;

        @JsonProperty(value = "target")
        private String target;

        public String getCode() {
            return this.code;
        }

        public MockCommunicationError setCode(String code) {
            this.code = code;
            return this;
        }

        public String getMessage() {
            return this.message;
        }

        public MockCommunicationError setMessage(String message) {
            this.message = message;
            return this;
        }

        public String getTarget() {
            return this.target;
        }

        public MockCommunicationError setTarget(String target) {
            this.target = target;
            return this;
        }
    }

}
