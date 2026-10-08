// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import io.netty.handler.codec.http2.Http2ConnectionDecoder;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;

final class CosmosHttp2DecoderCodecBuilder extends Http2FrameCodecBuilder {
    Http2FrameCodec build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder) {
        super.codec(decoder, encoder);
        return super.build();
    }

    @Override
    public boolean isServer() {
        return decoder().connection().isServer();
    }
}
