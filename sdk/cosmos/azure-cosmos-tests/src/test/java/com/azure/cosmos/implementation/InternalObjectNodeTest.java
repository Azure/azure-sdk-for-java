// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.TestPojo;
import com.azure.cosmos.implementation.json.CosmosBinaryJacksonCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class InternalObjectNodeTest {
    private final static ObjectMapper MAPPER = Utils.getSimpleObjectMapper();

    @Test(groups = {"unit"})
    public void PojoWithTrackingId() throws IOException {
        String expectedTrackingId = UUID.randomUUID().toString();
        TestPojo pojo = new TestPojo();
        ByteBuffer buffer = InternalObjectNode.serializeJsonToByteBuffer(
            pojo, CosmosItemSerializer.DEFAULT_SERIALIZER, expectedTrackingId, false);
        byte[] blob = new byte[buffer.remaining()];
        buffer.get(blob);
        validateTrackingId(blob, expectedTrackingId);
    }

    @Test(groups = {"unit"})
    public void ByteArrayWithTrackingId() throws IOException {
        String expectedTrackingId = UUID.randomUUID().toString();
        ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("id", "myId");
        ByteBuffer buffer = InternalObjectNode.serializeJsonToByteBuffer(
            objectNode, CosmosItemSerializer.DEFAULT_SERIALIZER, expectedTrackingId, false);
        byte[] blob =  blob = new byte[buffer.remaining()];
        buffer.get(blob);
        validateTrackingId(blob, expectedTrackingId);
    }

    @Test(groups = {"unit"})
    public void preSerializedJsonIsConvertedWhenBinaryEncodingIsEnabled() throws IOException {
        byte[] json = "{\"id\":\"binary\",\"payload\":\"value\"}"
            .getBytes(java.nio.charset.StandardCharsets.UTF_8);

        ByteBuffer buffer = InternalObjectNode.serializeJsonToByteBuffer(
            json, CosmosItemSerializer.DEFAULT_SERIALIZER, null, false, true);
        byte[] encoded = new byte[buffer.remaining()];
        buffer.get(encoded);

        assertThat(CosmosBinaryJacksonCodec.isBinaryFormat(encoded)).isTrue();
        assertThat(CosmosBinaryJacksonCodec.decode(encoded).path("id").textValue()).isEqualTo("binary");
    }

    @Test(groups = {"unit"})
    public void internalObjectNodeWithTrackingId() throws IOException {
        String expectedTrackingId = UUID.randomUUID().toString();
        InternalObjectNode intenalObjectNode = new InternalObjectNode();
        intenalObjectNode.set("id", "myId");
        ByteBuffer buffer = InternalObjectNode.serializeJsonToByteBuffer(
            intenalObjectNode, CosmosItemSerializer.DEFAULT_SERIALIZER, expectedTrackingId, false);
        byte[] blob = new byte[buffer.remaining()];
        buffer.get(blob);
        validateTrackingId(blob, expectedTrackingId);
    }

    @Test(groups = {"unit"})
    public void objectNodeWithTrackingId() throws IOException {
        String expectedTrackingId = UUID.randomUUID().toString();
        ObjectNode objectNode = MAPPER.createObjectNode();
        objectNode.put("id", "myId");
        ByteBuffer buffer = InternalObjectNode.serializeJsonToByteBuffer(
            objectNode, CosmosItemSerializer.DEFAULT_SERIALIZER, expectedTrackingId, false);
        byte[] blob = new byte[buffer.remaining()];
        buffer.get(blob);
        validateTrackingId(blob, expectedTrackingId);
    }

    private void validateTrackingId(byte[] blob, String expectedTrackingId) throws IOException {
        ObjectNode node = (ObjectNode)MAPPER.readTree(blob);
        assertThat(node).isNotNull();
        assertThat(node.get("_trackingId")).isNotNull();
        assertThat(node.get("_trackingId").textValue()).isEqualTo(expectedTrackingId);
    }
}
