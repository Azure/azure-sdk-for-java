// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation.simple;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import io.netty.buffer.ByteBuf;
import reactor.core.publisher.Sinks;

import java.nio.ByteBuffer;

// TODO (kasobol-msft) this should probably be publisher to control backpressure better and stop channel reads
// when necessary. I.e. https://stackoverflow.com/questions/22060454/how-to-pause-and-resume-reading-with-netty-4
public class SinkBodyCollector implements SimpleBodyCollector {

    private static final ClientLogger LOGGER = new ClientLogger(SinkBodyCollector.class);

    // This has unbounded queue. Should be better solved by implementing publisher.
    private final Sinks.Many<ByteBuffer> sink = Sinks.many().unicast().onBackpressureBuffer();

    @Override
    public void collect(ByteBuf buffer, boolean isLast) {
        ByteBuffer nioBuffer = buffer.nioBuffer();
        Sinks.EmitResult emitResult = sink.tryEmitNext(nioBuffer);
        if (emitResult.isFailure()) {
            throw LOGGER.logExceptionAsError(new RuntimeException("can't emit buffers"));
        }
        if (isLast) {
            sink.tryEmitComplete();
        }
    }

    @Override
    public BinaryData toBinaryData() {
        return BinaryData.fromFluxLazy(sink.asFlux());
    }
}
