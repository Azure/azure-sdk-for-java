// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.chat;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.chat.implementation.JsonMergePatchHelper;
import com.azure.messaging.webpubsub.chat.models.ChatMessage;
import com.azure.messaging.webpubsub.chat.models.MessageContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageContentTests {
    @Test
    public void factoriesRequireSelectedValue() {
        assertThrows(NullPointerException.class, () -> MessageContent.fromText(null));
        assertThrows(NullPointerException.class, () -> MessageContent.fromBinary(null));
    }

    @Test
    public void binaryIsDefensivelyCopied() {
        byte[] bytes = { 0, 1, (byte) 255 };
        MessageContent content = MessageContent.fromBinary(bytes);
        bytes[0] = 42;
        byte[] returned = content.getBinary();
        returned[1] = 42;
        assertArrayEquals(new byte[] { 0, 1, (byte) 255 }, content.getBinary());
        assertNull(content.getText());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "hello", "line\n\"quoted\"" })
    public void textRoundTrips(String text) throws IOException {
        MessageContent content = deserialize(serialize(MessageContent.fromText(text)));
        assertEquals(text, content.getText());
        assertNull(content.getBinary());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void binaryRoundTrips(boolean empty) throws IOException {
        byte[] bytes = empty ? new byte[0] : new byte[] { 0, 1, (byte) 254, (byte) 255 };
        MessageContent content = deserialize(serialize(MessageContent.fromBinary(bytes)));
        assertArrayEquals(bytes, content.getBinary());
        assertNull(content.getText());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"text\":\"hello\"}",
            "{\"binary\":null,\"text\":\"hello\",\"unknown\":{\"nested\":[1,2]}}",
            "{\"text\":\"hello\",\"binary\":null}" })
    public void deserializesTextWithUnusedNullAndUnknownFields(String json) throws IOException {
        MessageContent content = deserialize(json);
        assertEquals("hello", content.getText());
        assertNull(content.getBinary());
        assertEquals("{\"text\":\"hello\"}", serialize(content));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{\"binary\":\"AQID\"}",
            "{\"text\":null,\"binary\":\"AQID\"}",
            "{\"binary\":\"AQID\",\"text\":null,\"unknown\":[{\"value\":1}]}" })
    public void deserializesBinaryWithUnusedNullAndUnknownFields(String json) throws IOException {
        MessageContent content = deserialize(json);
        assertArrayEquals(new byte[] { 1, 2, 3 }, content.getBinary());
        assertNull(content.getText());
        assertEquals("{\"binary\":\"AQID\"}", serialize(content));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}",
            "{\"text\":null}",
            "{\"binary\":null}",
            "{\"text\":null,\"binary\":null}",
            "{\"text\":\"hello\",\"binary\":\"AQID\"}",
            "{\"binary\":\"AQID\",\"text\":\"hello\"}",
            "{\"unknown\":true}" })
    public void rejectsContentWithoutExactlyOneValue(String json) {
        assertThrows(IllegalStateException.class, () -> deserialize(json));
    }

    @Test
    public void jsonNullRemainsNull() throws IOException {
        assertNull(deserialize("null"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{\"text\":\"hello\"}", "{\"binary\":\"AQID\"}" })
    public void factoryAndDeserializedContentHaveIdenticalPatchBodies(String json) throws IOException {
        MessageContent deserialized = deserialize(json);
        MessageContent created = deserialized.getText() != null
            ? MessageContent.fromText(deserialized.getText())
            : MessageContent.fromBinary(deserialized.getBinary());
        assertEquals("{\"content\":" + json + "}", serializePatch(new ChatMessage().setContent(created)));
        assertEquals("{\"content\":" + json + "}", serializePatch(new ChatMessage().setContent(deserialized)));
        assertFalse(JsonMergePatchHelper.getMessageContentAccessor().isJsonMergePatch(deserialized));
        assertEquals(json, serialize(deserialized));
    }

    @Test
    public void patchPreservesOmittedAndExplicitNullContent() throws IOException {
        assertEquals("{}", serializePatch(new ChatMessage()));
        assertEquals("{\"content\":null}", serializePatch(new ChatMessage().setContent(null)));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void syncAndAsyncUpdatesSendOnlySelectedContent(boolean async) throws IOException {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        String response = "{\"content\":{\"binary\":\"AQID\"}}";
        WebPubSubChatServiceClientBuilder builder
            = new WebPubSubChatServiceClientBuilder().endpoint("https://example.webpubsub.azure.com")
                .hub("chat")
                .credential(new MockTokenCredential())
                .httpClient(request -> {
                    sentRequest.set(request);
                    return Mono.just(new MockHttpResponse(request, 200,
                        new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                        response.getBytes(StandardCharsets.UTF_8)));
                });
        ChatMessage update = new ChatMessage().setContent(deserialize("{\"binary\":\"AQID\"}"));
        ChatMessage result = async
            ? builder.buildAsyncClient().updateMessage("c.room.default", "1", update).block()
            : builder.buildClient().updateMessage("c.room.default", "1", update);
        assertArrayEquals(new byte[] { 1, 2, 3 }, result.getContent().getBinary());
        assertEquals("{\"content\":{\"binary\":\"AQID\"}}", sentRequest.get().getBodyAsBinaryData().toString());
        assertEquals("application/merge-patch+json",
            sentRequest.get().getHeaders().getValue(HttpHeaderName.CONTENT_TYPE));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void updatesUsePathMessageIdNotReadOnlyModelId(boolean async) {
        AtomicReference<HttpRequest> sentRequest = new AtomicReference<>();
        String response = "{\"id\":\"2\",\"content\":{\"text\":\"updated\"}}";
        WebPubSubChatServiceClientBuilder builder
            = new WebPubSubChatServiceClientBuilder().endpoint("https://example.webpubsub.azure.com")
                .hub("chat")
                .credential(new MockTokenCredential())
                .httpClient(request -> {
                    sentRequest.set(request);
                    return Mono.just(new MockHttpResponse(request, 200,
                        new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, "application/json"),
                        response.getBytes(StandardCharsets.UTF_8)));
                });
        ChatMessage message = BinaryData.fromString("{\"id\":\"1\",\"content\":{\"text\":\"original\"}}")
            .toObject(ChatMessage.class)
            .setContent(MessageContent.fromText("updated"));
        ChatMessage result = async
            ? builder.buildAsyncClient().updateMessage("c.room.default", "2", message).block()
            : builder.buildClient().updateMessage("c.room.default", "2", message);

        assertEquals("2", result.getId());
        assertEquals("/api/hubs/chat/chat/conversations/c.room.default/messages/2",
            sentRequest.get().getUrl().getPath());
        assertEquals("{\"content\":{\"text\":\"updated\"}}", sentRequest.get().getBodyAsBinaryData().toString());
    }

    private static MessageContent deserialize(String json) throws IOException {
        try (JsonReader reader = JsonProviders.createReader(json)) {
            return MessageContent.fromJson(reader);
        }
    }

    private static String serializePatch(ChatMessage message) throws IOException {
        JsonMergePatchHelper.getChatMessageAccessor().prepareModelForJsonMergePatch(message, true);
        return serialize(message);
    }

    private static String serialize(JsonSerializable<?> value) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (JsonWriter writer = JsonProviders.createWriter(output)) {
            value.toJson(writer);
            writer.flush();
        }
        return new String(output.toByteArray(), StandardCharsets.UTF_8);
    }
}
