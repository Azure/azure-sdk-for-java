// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpResponse;
import com.azure.cosmos.implementation.Configs;
import java.nio.charset.StandardCharsets;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2FrameStreamException;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.util.AsciiString;
import org.testng.annotations.Test;
import org.testng.annotations.DataProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Http2ResponseHeaderValidationHandlerTest {
    private static final String HEADER = "x-ms-serviceversion";

    @Test(groups = "unit")
    public void validFramesAndValuesArePassedWithoutCopying() {
        EmbeddedChannel channel = new EmbeddedChannel(Http2ResponseHeaderValidationHandler.INSTANCE);
        AsciiString value = AsciiString.cached("version");
        Http2HeadersFrame frame = frame(1, new DefaultHttp2Headers(true, false, 16).status("200").add(HEADER, value));
        try {
            assertThat(channel.writeInbound(frame)).isTrue();
            Http2HeadersFrame received = channel.readInbound();
            assertThat(received).isSameAs(frame);
            assertThat(received.headers().get(HEADER)).isSameAs(value);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void duplicateServiceVersionValuesAreIndividuallyNormalized() {
        EmbeddedChannel channel = new EmbeddedChannel(Http2ResponseHeaderValidationHandler.INSTANCE);
        AsciiString first = AsciiString.cached(" version ");
        AsciiString second = AsciiString.cached("\tother\t");
        Http2Headers headers = new DefaultHttp2Headers(true, false, 16).status("200")
            .add(HEADER, first).add(HEADER, second);
        try {
            channel.writeInbound(frame(1, headers));
            Http2HeadersFrame received = channel.readInbound();
            assertThat(received.headers().getAll(HEADER)).hasSize(2);
            AsciiString a = (AsciiString) received.headers().getAll(HEADER).get(0);
            AsciiString b = (AsciiString) received.headers().getAll(HEADER).get(1);
            assertThat(a.toString()).isEqualTo("version");
            assertThat(b.toString()).isEqualTo("other");
            assertThat(a.array()).isSameAs(first.array());
            assertThat(b.array()).isSameAs(second.array());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void invalidDuplicateValueFailsOnlyItsStreamAndDoesNotForwardHeaders() {
        EmbeddedChannel channel = new EmbeddedChannel(Http2ResponseHeaderValidationHandler.INSTANCE);
        Http2HeadersFrame invalid = frame(3, new DefaultHttp2Headers(true, false, 16).status("200")
            .add(HEADER, "version").add(HEADER, "a\r\nb"));
        try {
            assertThatThrownBy(() -> channel.writeInbound(invalid))
                .isInstanceOf(Http2FrameStreamException.class)
                .satisfies(error -> {
                    Http2FrameStreamException streamError = (Http2FrameStreamException) error;
                    assertThat(streamError.stream()).isSameAs(invalid.stream());
                    assertThat(streamError.error()).isEqualTo(Http2Error.PROTOCOL_ERROR);
                    assertThat(streamError.getCause()).hasMessageContaining(HEADER);
                });
            assertThat((Object) channel.readInbound()).isNull();
            Http2HeadersFrame valid = frame(5, new DefaultHttp2Headers(true, false, 16).status("200"));
            assertThat(channel.writeInbound(valid)).isTrue();
            assertThat((Object) channel.readInbound()).isSameAs(valid);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void trailersAreValidatedAndSettingsAcksRemainFiltered() {
        EmbeddedChannel channel = new EmbeddedChannel(Http2ResponseHeaderValidationHandler.INSTANCE);
        try {
            assertThat(channel.writeInbound(Http2SettingsAckFrame.INSTANCE)).isFalse();
            Http2HeadersFrame trailers = frame(1, new DefaultHttp2Headers(true, false, 16).add("x-trailer", "a\0b"));
            assertThatThrownBy(() -> channel.writeInbound(trailers))
                .isInstanceOf(Http2FrameStreamException.class)
                .hasCauseInstanceOf(IllegalArgumentException.class);
            assertThat((Object) channel.readInbound()).isNull();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @DataProvider(name = "invalidNamesAndPseudoHeaders")
    public Object[][] invalidNamesAndPseudoHeaders() {
        return new Object[][] {
            { "X-Upper", "value" }, { "", "value" }, { "bad name", "value" },
            { ":unknown", "value" }, { ":method", "GET" }, { ":status", "" },
            { "connection", "close" }, { "transfer-encoding", "chunked" }, { "te", "gzip" }
        };
    }

    @Test(groups = "unit", dataProvider = "invalidNamesAndPseudoHeaders")
    public void handlerChecksNamesAndResponseProtocolRules(String name, String value) {
        Http2Headers headers = new DefaultHttp2Headers(false, false, 16).add(name, value);
        assertThatThrownBy(() -> Http2ResponseHeaderValidationHandler.validateHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = "unit")
    public void duplicateStatusIsRejected() {
        Http2Headers headers = new DefaultHttp2Headers(false, false, 16)
            .add(":status", "200").add(":status", "201");
        assertThatThrownBy(() -> Http2ResponseHeaderValidationHandler.validateHeaders(headers))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test(groups = "unit")
    public void http11FallbackRestoresNativeValidation() {
        EmbeddedChannel channel = new EmbeddedChannel(new HttpClientCodec(4096, 8192, 8192, false, false));
        try {
            ReactorNettyClient.restoreHttp11HeaderValidation(channel, new HttpClientConfig(new Configs()));
            HttpClientCodec installed = channel.pipeline().get(HttpClientCodec.class);
            ReactorNettyClient.restoreHttp11HeaderValidation(channel, new HttpClientConfig(new Configs()));
            assertThat(channel.pipeline().get(HttpClientCodec.class)).isSameAs(installed);
            assertThat(channel.pipeline().get(Http2ResponseHeaderValidationHandler.HANDLER_NAME)).isNull();
            channel.writeInbound(Unpooled.copiedBuffer("HTTP/1.1 200 OK\r\nx-header: a\0b\r\n\r\n",
                StandardCharsets.US_ASCII));
            HttpResponse response = channel.readInbound();
            assertThat(response.decoderResult().isFailure()).isTrue();
            assertThat(response.decoderResult().cause()).isInstanceOf(IllegalArgumentException.class);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    private static Http2HeadersFrame frame(int id, Http2Headers headers) {
        return new DefaultHttp2HeadersFrame(headers, true).stream(new Http2FrameStream() {
            @Override
            public int id() {
                return id;
            }

            @Override
            public Http2Stream.State state() {
                return Http2Stream.State.OPEN;
            }
        });
    }
}
