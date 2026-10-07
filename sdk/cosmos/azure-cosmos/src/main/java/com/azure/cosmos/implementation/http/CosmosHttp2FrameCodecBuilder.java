// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http2.DefaultHttp2ConnectionDecoder;
import io.netty.handler.codec.http2.DefaultHttp2FrameReader;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2FrameReader;
import io.netty.handler.codec.http2.Http2InboundFrameLogger;
import io.netty.handler.codec.http2.Http2Settings;

final class CosmosHttp2FrameCodecBuilder extends Http2FrameCodecBuilder {
    private int maxDecodedRstFrames;
    private int rstWindowSeconds = 30;

    CosmosHttp2FrameCodecBuilder() {
        server(false);
        gracefulShutdownTimeoutMillis(0);
    }

    @Override
    public CosmosHttp2FrameCodecBuilder decoderEnforceMaxRstFramesPerWindow(int maxFrames, int windowSeconds) {
        super.decoderEnforceMaxRstFramesPerWindow(maxFrames, windowSeconds);
        maxDecodedRstFrames = maxFrames;
        rstWindowSeconds = windowSeconds;
        return this;
    }

    @Override
    protected Http2FrameCodec build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                   Http2Settings settings) {
        Long maxHeaderListSize = settings.maxHeaderListSize();
        Http2FrameReader reader = new DefaultHttp2FrameReader(new CosmosHttp2HeadersDecoder(
            maxHeaderListSize == null ? Http2CodecUtil.DEFAULT_HEADER_LIST_SIZE : maxHeaderListSize),
            decoderEnforceMaxSmallContinuationFrames());
        if (frameLogger() != null) {
            reader = new Http2InboundFrameLogger(reader, frameLogger());
        }
        Http2ConnectionDecoder replacement = new DefaultHttp2ConnectionDecoder(
            decoder.connection(), encoder, reader, promisedRequestVerifier(), isAutoAckSettingsFrame(),
            isAutoAckPingFrame(), true, isValidateRequiredPseudoHeaders());

        // Retain Netty's constructed encoder/control-frame limits. The second builder reapplies
        // decoder protections to the custom reader before a codec is installed or sends its preface.
        decoder.close();
        return new DecoderBuilder(replacement, encoder, settings, this).build();
    }

    private static final class DecoderBuilder extends Http2FrameCodecBuilder {
        private DecoderBuilder(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                               Http2Settings settings, CosmosHttp2FrameCodecBuilder source) {
            initialSettings(settings);
            gracefulShutdownTimeoutMillis(source.gracefulShutdownTimeoutMillis());
            decoderEnforceMaxConsecutiveEmptyDataFrames(source.decoderEnforceMaxConsecutiveEmptyDataFrames());
            decoderEnforceMaxRstFramesPerWindow(source.maxDecodedRstFrames, source.rstWindowSeconds);
            decoupleCloseAndGoAway(source.decoupleCloseAndGoAway());
            flushPreface(source.flushPreface());
            codec(decoder, encoder);
        }

        @Override
        public boolean isServer() {
            return false;
        }
    }
}
