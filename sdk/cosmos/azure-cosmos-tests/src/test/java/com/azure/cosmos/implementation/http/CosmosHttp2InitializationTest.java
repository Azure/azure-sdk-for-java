// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.WebExceptionRetryPolicy;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import com.azure.cosmos.implementation.routing.RegionalRoutingContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http2.DefaultHttp2SettingsFrame;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameAdapter;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2SettingsAckFrame;
import io.netty.util.AttributeKey;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.netty.ConnectionObserver;
import reactor.netty.NettyPipeline;
import reactor.netty.http.client.PrematureCloseException;

import java.net.URI;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static com.azure.cosmos.implementation.TestUtils.mockDiagnosticsClientContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class CosmosHttp2InitializationTest {
    @DataProvider(name = "validationFlags")
    public Object[][] validationFlags() {
        return new Object[][] { { true }, { false } };
    }

    @Test(groups = "unit", dataProvider = "validationFlags")
    public void codecBuilderRetainsExistingValidationSpec(boolean validateHeaders) throws Exception {
        CosmosHttp2FrameCodecBuilder builder = new CosmosHttp2FrameCodecBuilder();
        builder.validateHeaders(validateHeaders);
        Http2FrameCodec codec = builder.build();
        AtomicReference<Http2Headers> received = new AtomicReference<>();
        codec.decoder().frameListener(new Http2FrameAdapter() {
            @Override
            public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int padding,
                                      boolean endStream) {
                received.set(headers);
            }

            @Override
            public void onHeadersRead(ChannelHandlerContext ctx, int streamId, Http2Headers headers, int streamDependency,
                                      short weight, boolean exclusive, int padding, boolean endStream) {
                received.set(headers);
            }
        });
        EmbeddedChannel channel = new EmbeddedChannel(codec);
        ByteBuf block = headerBlock(":status", "200", "X-UPPER", "a\0b");
        ByteBuf frame = Unpooled.buffer(9 + block.readableBytes());
        ByteBuf settings = Unpooled.buffer(9).writeMedium(0).writeByte(4).writeByte(0).writeInt(0);
        try {
            ChannelHandlerContext context = channel.pipeline().context(codec);
            codec.decoder().decodeFrame(context, settings, Collections.emptyList());
            codec.connection().local().createStream(1, false);
            frame.writeMedium(block.readableBytes()).writeByte(1).writeByte(5).writeInt(1).writeBytes(block);
            if (validateHeaders) {
                assertThatThrownBy(() -> codec.decoder().decodeFrame(context, frame, Collections.emptyList()))
                    .isInstanceOf(Http2Exception.StreamException.class);
                assertThat(received.get()).isNull();
            } else {
                codec.decoder().decodeFrame(context, frame, Collections.emptyList());
                assertThat(received.get().get("X-UPPER").toString()).isEqualTo("a\0b");
            }
        } finally {
            settings.release();
            frame.release();
            block.release();
            channel.finishAndReleaseAll();
        }
    }

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
    public void headerCustomizationDelegatesToNettyValidation() {
        Http2Headers headers = new CosmosHttp2Headers(true, true, 8);
        headers.add("x-ms-serviceversion", "\t version \t").add("x-other", "unchanged");
        assertThat(headers.get("x-ms-serviceversion").toString()).isEqualTo("version");
        assertThat(headers.get("x-other").toString()).isEqualTo("unchanged");
        assertThatThrownBy(() -> headers.add("x-other", " padded "))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("x-other");
        assertThatThrownBy(() -> headers.add("x-ms-serviceversion", " v\0 "))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("x-ms-serviceversion");
        assertThat(headers.get("x-other").toString()).isEqualTo("unchanged");
        assertThat(headers.getAll("x-ms-serviceversion")).hasSize(1);
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

    @Test(groups = "unit")
    public void copiedSettingsMappingReachesTheInstalledCodec() {
        reactor.netty.http.client.HttpClientConfig config = reactor.netty.http.client.HttpClient.create()
            .http2Settings(spec -> spec.headerTableSize(1024).initialWindowSize(2048).maxConcurrentStreams(7)
                .maxFrameSize(32768).maxHeaderListSize(16384)
                .maxDecodedRstFramesPerWindow(10, 30).maxEncodedRstFramesPerWindow(12, 30))
            .configuration();
        Http2Settings expected = new Http2Settings().headerTableSize(1024).initialWindowSize(2048)
            .maxConcurrentStreams(7).maxFrameSize(32768).maxHeaderListSize(16384);
        Http2Settings settings = ReactorNettyHttp2Config.http2Settings(config.http2SettingsSpec());
        assertThat(settings).isEqualTo(expected);
        assertThat(ReactorNettyHttp2Config.http2Settings(null)).isEqualTo(Http2Settings.defaultSettings());
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast(NettyPipeline.ReactiveBridge, new ChannelInboundHandlerAdapter());
        try {
            ReactorNettyHttp2Config.configureHttp2Pipeline(channel.pipeline(), config.decoder(), settings,
                config.http2SettingsSpec(), (connection, state) -> { });
            channel.writeInbound(Unpooled.buffer(9).writeMedium(0).writeByte(4).writeByte(0).writeInt(0));
            channel.writeInbound(Unpooled.buffer(9).writeMedium(0).writeByte(4).writeByte(1).writeInt(0));
            Http2FrameCodec codec = channel.pipeline().get(Http2FrameCodec.class);
            assertThat(codec.decoder().localSettings()).containsAllEntriesOf(expected);
            assertThat(codec.decoder().getClass().getSimpleName()).isEqualTo("Http2MaxRstFrameDecoder");
            assertThat(codec.encoder().getClass().getSimpleName()).isEqualTo("Http2MaxRstFrameLimitEncoder");
            assertThat(channel.pipeline().get(NettyPipeline.ReactiveBridge)).isNull();
            assertThat(channel.pipeline().get(NettyPipeline.HttpTrafficHandler)).isNull();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void copiedTrafficHandlerPreservesReadinessOrdering() {
        List<ConnectionObserver.State> states = new ArrayList<>();
        EmbeddedChannel channel = new EmbeddedChannel();
        AttributeKey<Long> connectProtocol = AttributeKey.valueOf("$ENABLE_CONNECT_PROTOCOL");
        ConnectionObserver observer = (connection, state) -> {
            states.add(state);
            if (state == ConnectionObserver.State.CONFIGURED) {
                assertThat(connection.channel().attr(connectProtocol).get()).isEqualTo(1L);
                assertThat(connection.channel().pipeline().get(NettyPipeline.ReactiveBridge)).isNotNull();
            }
        };
        channel.pipeline().addLast(NettyPipeline.SslHandler, new ChannelInboundHandlerAdapter())
            .addLast(NettyPipeline.H2MultiplexHandler, new ChannelInboundHandlerAdapter())
            .addLast(NettyPipeline.HttpTrafficHandler, new ReactorNettyHttpTrafficHandler(observer))
            .addLast(NettyPipeline.ReactiveBridge, new ChannelInboundHandlerAdapter());
        try {
            channel.pipeline().fireChannelActive();
            assertThat(states).containsExactly(ConnectionObserver.State.CONNECTED);
            Http2Settings settings = new Http2Settings();
            settings.put(Http2CodecUtil.SETTINGS_ENABLE_CONNECT_PROTOCOL, Long.valueOf(1));
            assertThat(channel.writeInbound(new DefaultHttp2SettingsFrame(settings))).isFalse();
            assertThat(states).containsExactly(ConnectionObserver.State.CONNECTED, ConnectionObserver.State.CONFIGURED);
            assertThat(channel.pipeline().get(NettyPipeline.ReactiveBridge)).isNull();
            assertThat(channel.pipeline().get(NettyPipeline.HttpTrafficHandler)).isNull();
            DefaultHttp2SettingsFrame subsequent = new DefaultHttp2SettingsFrame(new Http2Settings());
            assertThat(channel.writeInbound(subsequent)).isTrue();
            assertThat((Object) channel.readInbound()).isSameAs(subsequent);
            assertThat(states).hasSize(2);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void copiedTrafficHandlerDoesNotReportAnInactiveChannelAsConnected() {
        ChannelHandlerContext context = mock(ChannelHandlerContext.class);
        Channel channel = mock(Channel.class);
        when(context.channel()).thenReturn(channel);
        when(channel.isActive()).thenReturn(false);
        ConnectionObserver observer = mock(ConnectionObserver.class);
        new ReactorNettyHttpTrafficHandler(observer).channelActive(context);
        verifyNoInteractions(observer);
    }

    @DataProvider(name = "earlyCloseOperations")
    public Object[][] earlyCloseOperations() {
        return new Object[][] { { OperationType.Read, true }, { OperationType.Replace, false } };
    }

    @Test(groups = "unit", dataProvider = "earlyCloseOperations")
    public void copiedTrafficHandlerPreservesCosmosRetryClassification(OperationType operation, boolean retry) {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        EmbeddedChannel channel = new EmbeddedChannel(
            new ReactorNettyHttpTrafficHandler((connection, state) -> { }),
            new ChannelInboundHandlerAdapter() {
                @Override
                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                    failure.set(cause);
                }
            });
        try {
            channel.close().syncUninterruptibly();
            assertThat(failure.get()).isInstanceOf(Http2PrematureCloseException.class);
            Exception exception = (Exception) failure.get();
            assertThat(WebExceptionUtility.isNetworkFailure(exception))
                .isEqualTo(WebExceptionUtility.isNetworkFailure(PrematureCloseException.TEST_EXCEPTION)).isTrue();
            assertThat(WebExceptionUtility.isWebExceptionRetriable(exception))
                .isEqualTo(WebExceptionUtility.isWebExceptionRetriable(PrematureCloseException.TEST_EXCEPTION)).isFalse();
            CosmosException wrapped = BridgeInternal.createCosmosException(null,
                HttpConstants.StatusCodes.SERVICE_UNAVAILABLE, exception);
            assertThat(WebExceptionUtility.isNetworkFailure(wrapped)).isTrue();
            RxDocumentServiceRequest request = RxDocumentServiceRequest.createFromName(mockDiagnosticsClientContext(),
                operation, "/dbs/db/colls/col/docs/doc", ResourceType.Document);
            request.requestContext.regionalRoutingContextToRoute = new RegionalRoutingContext(URI.create("https://localhost"));
            WebExceptionRetryPolicy policy = new WebExceptionRetryPolicy();
            policy.onBeforeSendRequest(request);
            assertThat(policy.shouldRetry(wrapped).block(Duration.ofSeconds(5)).shouldRetry).isEqualTo(retry);
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test(groups = "unit")
    public void unexpectedPipelineStillFailsFast() {
        EmbeddedChannel channel = new EmbeddedChannel();
        try {
            assertThatThrownBy(() -> CosmosHttp2ChannelInitializer.install(channel, (connection, state) -> { },
                reactor.netty.http.client.HttpClient.create().configuration()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Expected Reactor's deferred H2/HTTP1.1 initializer");
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
