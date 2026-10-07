// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CosmosHttp2InitializationTest {
    @DataProvider(name = "invalidHeaders")
    public Object[][] invalidHeaders() {
        return new Object[][] {
            { new String[] { ":status", "200", "X-Upper", "value" } },
            { new String[] { ":status", "200", "", "value" } },
            { new String[] { ":status", "200", "bad name", "value" } },
            { new String[] { ":unknown", "value" } },
            { new String[] { ":status", "200", ":method", "GET" } },
            { new String[] { ":status", "" } },
            { new String[] { ":status", "200", ":status", "201" } },
            { new String[] { ":status", "200", "connection", "close" } },
            { new String[] { ":status", "200", "transfer-encoding", "chunked" } },
            { new String[] { ":status", "200", "te", "gzip" } },
            { new String[] { "x-header", "value", ":status", "200" } }
        };
    }

    @Test(groups = "unit", dataProvider = "invalidHeaders")
    public void customDecoderRetainsNettyNameAndProtocolValidation(String[] fields) {
        ByteBuf block = headerBlock(fields);
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.StreamException.class)
                .satisfies(error -> assertThat(((Http2Exception) error).error()).isEqualTo(Http2Error.PROTOCOL_ERROR));
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void customDecoderRetainsCompressionValidation() {
        ByteBuf block = Unpooled.buffer(1).writeByte(0x80);
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.class)
                .satisfies(error -> assertThat(((Http2Exception) error).error()).isEqualTo(Http2Error.COMPRESSION_ERROR));
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void normalizationDoesNotMergeDuplicateFieldsOrCopyArrays() throws Exception {
        ByteBuf block = headerBlock("x-ms-serviceversion", " version ", "x-ms-serviceversion", "\tother\t");
        try {
            Http2Headers headers = new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block);
            assertThat(headers.getAll("x-ms-serviceversion")).hasSize(2);
            assertThat(headers.getAll("x-ms-serviceversion").get(0).toString()).isEqualTo("version");
            assertThat(headers.getAll("x-ms-serviceversion").get(1).toString()).isEqualTo("other");
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void codecRetainsConfiguredHeaderAndStreamLimits() {
        CosmosHttp2FrameCodecBuilder builder = new CosmosHttp2FrameCodecBuilder();
        Http2FrameCodec codec = builder.initialSettings(new Http2Settings().maxHeaderListSize(16384)
            .initialWindowSize(1024 * 1024).maxFrameSize(65536).maxConcurrentStreams(7)).build();
        EmbeddedChannel channel = new EmbeddedChannel(codec);
        try {
            channel.writeInbound(Unpooled.buffer(9).writeMedium(0).writeByte(4).writeByte(0).writeInt(0));
            channel.writeInbound(Unpooled.buffer(9).writeMedium(0).writeByte(4).writeByte(1).writeInt(0));
            assertThat(codec.connection().isServer()).isFalse();
            assertThat(codec.decoder().localSettings().maxHeaderListSize()).isEqualTo(16384L);
            assertThat(codec.decoder().localSettings().initialWindowSize()).isEqualTo(1024 * 1024);
            assertThat(codec.decoder().localSettings().maxFrameSize()).isEqualTo(65536);
            assertThat(codec.connection().remote().maxActiveStreams()).isEqualTo(7);
            assertThat(codec.gracefulShutdownTimeoutMillis()).isZero();
            assertThat(codec.encoder().getClass().getSimpleName()).isEqualTo("Http2ControlFrameLimitEncoder");
            assertThat(codec.decoder().getClass().getSimpleName()).isEqualTo("Http2EmptyDataFrameConnectionDecoder");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void codecRetainsConfiguredRstFrameProtection() {
        CosmosHttp2FrameCodecBuilder builder = new CosmosHttp2FrameCodecBuilder();
        builder.decoderEnforceMaxRstFramesPerWindow(10, 30);
        Http2FrameCodec codec = builder.build();
        EmbeddedChannel channel = new EmbeddedChannel(codec);
        try {
            assertThat(codec.decoder().getClass().getSimpleName()).isEqualTo("Http2MaxRstFrameDecoder");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void settingsAckIsFilteredWithoutScanningHeaders() {
        EmbeddedChannel channel = new EmbeddedChannel(Http2SettingsHandler.INSTANCE);
        try {
            assertThat(channel.writeInbound(Http2SettingsAckFrame.INSTANCE)).isFalse();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static ByteBuf headerBlock(String... fields) {
        ByteBuf block = Unpooled.buffer();
        for (int i = 0; i < fields.length; i += 2) {
            block.writeByte(0).writeByte(fields[i].length());
            block.writeCharSequence(fields[i], StandardCharsets.US_ASCII);
            block.writeByte(fields[i + 1].length());
            block.writeCharSequence(fields[i + 1], StandardCharsets.US_ASCII);
        }
        return block;
    }
}
