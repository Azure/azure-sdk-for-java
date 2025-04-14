// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.testng.annotations.Test;

import java.util.Base64;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class RntbdTokenTests {
    private static final Random rnd = new Random();
    @Test(groups = { "unit" })
    public void getValueIsIdempotent() {
        RntbdToken token = RntbdToken.create(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey);
        byte[] blob = new byte[10];
        rnd.nextBytes(blob);
        token.setValue(blob);

        String expectedJson = "{\"id\":90,\"name\":\"EffectivePartitionKey\",\"present\":true,"
        + "\"required\":false,\"value\":\""
        + Base64.getEncoder().encodeToString(blob)
        + "\",\"tokenType\":\"Bytes\"}";
        assertThat(RntbdObjectMapper.toJson(token)).isEqualTo(expectedJson);

        Object value1 = token.getValue();
        Object value2 = token.getValue();
        assertThat(value1).isSameAs(value2);
        assertThat(value1).isSameAs(blob);

        assertThat(RntbdObjectMapper.toJson(token)).isEqualTo(expectedJson);

        ByteBuf buffer = Unpooled.buffer(1024);
        token.encode(buffer);

        RntbdToken decodedToken = RntbdToken.create(RntbdConstants.RntbdRequestHeader.EffectivePartitionKey);
        // skipping 3 bytes (2 bytes for header id + 1 byte for token type)
        buffer.readerIndex(3);
        // when decoding the RntbdToken.value is a ByteBuffer - not a byte[] - testing this path for idempotency as well
        decodedToken.decode(buffer);
        assertThat(RntbdObjectMapper.toJson(decodedToken)).isEqualTo(expectedJson);

        value1 = decodedToken.getValue();
        assertThat(value1).isInstanceOf(ByteBuf.class);
        ByteBuf byteBufValue1 = (ByteBuf)value1;
        assertThat(byteBufValue1.readableBytes()).isEqualTo(10);
        byte[] byteArray1 = new byte[10];
        byteBufValue1.getBytes(byteBufValue1.readerIndex(), byteArray1);
        assertThat(byteArray1).isEqualTo(blob);

        value2 = decodedToken.getValue();
        assertThat(value1).isSameAs(value2);
        ByteBuf byteBufValue2 = (ByteBuf)value2;
        assertThat(byteBufValue2.readableBytes()).isEqualTo(10);
        byte[] byteArray2 = new byte[10];
        byteBufValue2.getBytes(byteBufValue2.readerIndex(), byteArray2);
        assertThat(byteArray2).isEqualTo(blob);

        assertThat(RntbdObjectMapper.toJson(decodedToken)).isEqualTo(expectedJson);
    }
}
