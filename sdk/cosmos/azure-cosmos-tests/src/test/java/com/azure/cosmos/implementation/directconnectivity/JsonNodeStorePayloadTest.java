// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.json.CosmosBinaryJacksonCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public final class JsonNodeStorePayloadTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test(groups = "unit")
    public void parsesCosmosBinaryPayload() throws Exception {
        JsonNode source = MAPPER.readTree("{\"id\":\"one\",\"value\":42}");
        byte[] content = CosmosBinaryJacksonCodec.encode(source);

        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(Unpooled.wrappedBuffer(content)),
            content.length,
            Collections.emptyMap());

        assertThat(payload.getPayload().path("id").textValue()).isEqualTo("one");
        assertThat(payload.getPayload().path("value").longValue()).isEqualTo(42L);
        assertThat(payload.getResponsePayloadSize()).isEqualTo(content.length);
    }

    @Test(groups = "unit")
    public void mapsCorruptCosmosBinaryToStructuredParseFailure() {
        byte[] content = new byte[] { (byte) 0x80, (byte) 0x82, (byte) 0xC3, 0x28 };

        assertThatThrownBy(() -> new JsonNodeStorePayload(
            new ByteBufInputStream(Unpooled.wrappedBuffer(content)),
            content.length,
            Collections.emptyMap()))
            .isInstanceOf(CosmosException.class)
            .satisfies(error -> assertThat(((CosmosException) error).getSubStatusCode())
                .isEqualTo(HttpConstants.SubStatusCodes.FAILED_TO_PARSE_SERVER_RESPONSE));
    }

    @Test(groups = "unit")
    public void preservesRawRecordIoWithoutJsonParsing() {
        byte[] content = new byte[] { (byte) 0x81, 1, 2, 3 };

        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(Unpooled.wrappedBuffer(content)),
            content.length,
            Collections.emptyMap());

        assertThat(payload.getPayload()).isNull();
        assertThat(payload.getRawPayload()).isEqualTo(content).isNotSameAs(content);
    }

    @Test(groups = "unit")
    public void preservesJsonTextPayloadPath() {
        byte[] content = "{\"id\":\"text\"}".getBytes(StandardCharsets.UTF_8);

        JsonNodeStorePayload payload = new JsonNodeStorePayload(
            new ByteBufInputStream(Unpooled.wrappedBuffer(content)),
            content.length,
            Collections.emptyMap());

        assertThat(payload.getPayload().path("id").textValue()).isEqualTo("text");
        assertThat(payload.getRawPayload()).isNull();
    }
}
