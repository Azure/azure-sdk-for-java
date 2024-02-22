// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class ByteBufferSendTest extends RestProxyTestBase<CorePerfStressOptions> {
    private final Flux<ByteBuffer> dataToSend;
    private final long length;

    public ByteBufferSendTest(CorePerfStressOptions options) {
        super(options);
        // Creates Flux of ByteBuffer with blockSize of 1MB by default.
        // TODO: parametrize the block size
        length = options.getSize();
        dataToSend = TestDataCreationHelper.createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.setRawData(endpoint, id, dataToSend, length).then();
    }
}
