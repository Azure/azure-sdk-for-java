// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.DefaultHttp2SettingsFrame;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.util.AsciiString;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2ResponseHeaderCleanerHandlerTest {
    private static final String HEADER = "x-ms-serviceversion";

    @Test(groups = "unit")
    public void trimsTheServiceVersionValueWithoutChangingOtherHeaders() {
        Http2Headers headers = new DefaultHttp2Headers(false).status("200")
            .add(HEADER, " version ")
            .add("x-other", " untouched ");
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        try {
            DefaultHttp2HeadersFrame frame = new DefaultHttp2HeadersFrame(headers, true);
            assertThat(channel.writeInbound(frame)).isTrue();
            assertThat((Object) channel.readInbound()).isSameAs(frame);
            assertThat(headers.getAll(HEADER)).extracting(CharSequence::toString)
                .containsExactly("version");
            assertThat(headers.get("x-other").toString()).isEqualTo(" untouched ");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @DataProvider(name = "mainTrimmingBehavior")
    public Object[][] mainTrimmingBehavior() {
        return new Object[][] {
            { "", "" }, { " version", "version" }, { "version ", "version" },
            { " \tversion\t ", "version" }, { "\tversion\t", "\tversion\t" },
            { " v\0 ", "v" }, { " v\0x ", "v\0x" }
        };
    }

    @Test(groups = "unit", dataProvider = "mainTrimmingBehavior")
    public void retainsMainSpaceGuardAndStringTrimSemantics(String value, String expected) {
        Http2Headers headers = new DefaultHttp2Headers(false).status("200").add(HEADER, value);
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        try {
            channel.writeInbound(new DefaultHttp2HeadersFrame(headers, true));
            assertThat(headers.get(HEADER).toString()).isEqualTo(expected);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void retainsMainSingleValueReplacementForDuplicates() {
        Http2Headers headers = new DefaultHttp2Headers(false).status("200")
            .add(HEADER, " first ").add(HEADER, "second");
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        try {
            channel.writeInbound(new DefaultHttp2HeadersFrame(headers, true));
            assertThat(headers.getAll(HEADER)).extracting(CharSequence::toString).containsExactly("first");
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void leavesUnpaddedAndAbsentServiceVersionHeadersAlone() {
        AsciiString version = AsciiString.cached("version");
        Http2Headers headers = new DefaultHttp2Headers(false).status("200").add(HEADER, version);
        Http2Headers absent = new DefaultHttp2Headers(false).status("204");
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        try {
            channel.writeInbound(new DefaultHttp2HeadersFrame(headers, true), new DefaultHttp2HeadersFrame(absent, true));
            assertThat(headers.get(HEADER)).isSameAs(version);
            assertThat(absent.contains(HEADER)).isFalse();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void filtersOnlySettingsAckAndPreservesDataOwnership() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        DefaultHttp2DataFrame data = new DefaultHttp2DataFrame(Unpooled.buffer().writeByte(1), true);
        try {
            assertThat(channel.writeInbound(Http2SettingsAckFrame.INSTANCE)).isFalse();
            DefaultHttp2SettingsFrame settings = new DefaultHttp2SettingsFrame(new Http2Settings());
            assertThat(channel.writeInbound(settings)).isTrue();
            assertThat((Object) channel.readInbound()).isSameAs(settings);
            assertThat(channel.writeInbound(data)).isTrue();
            assertThat((Object) channel.readInbound()).isSameAs(data);
            assertThat(data.refCnt()).isEqualTo(1);
        } finally {
            data.release();
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void trimmingDoesNotMutateHpackDynamicTableValues() throws Exception {
        DefaultHttp2HeadersDecoder decoder = new DefaultHttp2HeadersDecoder(false);
        ByteBuf first = Unpooled.buffer().writeByte(0x40).writeByte(HEADER.length());
        first.writeCharSequence(HEADER, StandardCharsets.US_ASCII);
        first.writeByte(9).writeCharSequence(" version ", StandardCharsets.US_ASCII);
        ByteBuf indexed = Unpooled.buffer().writeByte(0xbe);
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ResponseHeaderCleanerHandler());
        try {
            Http2Headers decoded = decoder.decodeHeaders(1, first);
            channel.writeInbound(new DefaultHttp2HeadersFrame(decoded, true));
            assertThat(decoded.get(HEADER).toString()).isEqualTo("version");
            Http2Headers reused = decoder.decodeHeaders(3, indexed);
            assertThat(reused.get(HEADER).toString()).isEqualTo(" version ");
            channel.writeInbound(new DefaultHttp2HeadersFrame(reused, true));
            assertThat(reused.get(HEADER).toString()).isEqualTo("version");
        } finally {
            first.release();
            indexed.release();
            channel.finishAndReleaseAll();
        }
    }
}
