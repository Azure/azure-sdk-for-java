// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
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

        final String body = serializeObject(result);
        return generateMockResponse(body, request, 201);
    }

    public static HttpResponse addParticipantsInvalidParticipantResponse(HttpRequest request, CommunicationUserIdentifier invalidUser) {
        List<MockCommunicationError> invalidParticipants = new ArrayList<>();
        invalidParticipants.add(new MockCommunicationError()
            .setTarget(invalidUser.getId()));

        MockAddChatParticipantsResult result = new MockAddChatParticipantsResult()
            .setInvalidParticipants(invalidParticipants);

        final String body = serializeObject(result);
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

    static class MockCreateChatThreadResult implements JsonSerializable<MockCreateChatThreadResult> {

        private ChatThreadProperties chatThread;

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

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeJsonField("chatThread", chatThread);
            jsonWriter.writeArrayField("invalidParticipants", invalidParticipants, (writer, error) -> error.toJson(writer));
            return jsonWriter.writeEndObject();
        }

        /**
         * Reads an instance of MockCreateChatThreadResult from the JsonReader.
         *
         * @param jsonReader The JsonReader being read.
         * @return An instance of MockCreateChatThreadResult if the JsonReader was pointing to an instance of it, or null
         * if it was pointing to JSON null.
         * @throws IOException If an error occurs while reading the MockCreateChatThreadResult.
         */
        public static MockCreateChatThreadResult fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                final MockCreateChatThreadResult result = new MockCreateChatThreadResult();
                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("chatThread".equals(fieldName)) {
                        result.setChatThread(ChatThreadProperties.fromJson(jsonReader));
                    } else if ("invalidParticipants".equals(fieldName)) {
                        result.setInvalidParticipants(reader.readArray(MockCommunicationError::fromJson));
                    } else {
                        reader.skipChildren();
                    }
                }
                return result;
            });
        }
    }

    static class MockAddChatParticipantsResult implements JsonSerializable<MockCommunicationError> {

        private List<MockCommunicationError> invalidParticipants;

        public List<MockCommunicationError> getInvalidParticipants() {
            return this.invalidParticipants;
        }

        public MockAddChatParticipantsResult setInvalidParticipants(List<MockCommunicationError> invalidParticipants) {
            this.invalidParticipants = invalidParticipants;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeArrayField("invalidParticipants", invalidParticipants, (writer, error) -> error.toJson(writer));
            return jsonWriter.writeEndObject();
        }

        /**
         * Reads an instance of MockAddChatParticipantsResult from the JsonReader.
         *
         * @param jsonReader The JsonReader being read.
         * @return An instance of MockAddChatParticipantsResult if the JsonReader was pointing to an instance of it, or null
         * if it was pointing to JSON null.
         * @throws IOException If an error occurs while reading the MockAddChatParticipantsResult.
         */
        public static MockAddChatParticipantsResult fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                final MockAddChatParticipantsResult result = new MockAddChatParticipantsResult();
                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("invalidParticipants".equals(fieldName)) {
                        result.setInvalidParticipants(reader.readArray(MockCommunicationError::fromJson));
                    } else {
                        reader.skipChildren();
                    }
                }
                return result;
            });
        }
    }

    static class MockCommunicationError implements JsonSerializable<MockCommunicationError> {

        private String code;

        private String message;

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

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("code", code);
            jsonWriter.writeStringField("message", message);
            jsonWriter.writeStringField("target", target);
            return jsonWriter.writeEndObject();
        }

        public static MockCommunicationError fromJson(JsonReader jsonReader) throws IOException {
            return jsonReader.readObject(reader -> {
                final MockCommunicationError error = new MockCommunicationError();
                while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    if ("code".equals(fieldName)) {
                        error.setCode(reader.getString());
                    } else if ("message".equals(fieldName)) {
                        error.setMessage(reader.getString());
                    } else if ("target".equals(fieldName)) {
                        error.setTarget(reader.getString());
                    } else {
                        reader.skipChildren();
                    }
                }
                return error;
            });
        }
    }

    private static String serializeObject(JsonSerializable<?> o) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             JsonWriter writer = JsonProviders.createWriter(outputStream)) {
            o.toJson(writer);
            writer.flush();
            return outputStream.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
