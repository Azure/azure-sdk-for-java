// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.MockHttpReceiveClient;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class ByteBufferSendTest extends RestProxyTestBase<CorePerfStressOptions> {
    private final Flux<ByteBuffer> dataToSend;
    private final long length;

    public ByteBufferSendTest(CorePerfStressOptions options) {
        super(options, new MockHttpReceiveClient());
        //Creates Flux of ByteBuffer with blockSize of 1MB by default.
        //TODO: parametrize the block size
        length = options.getSize();
        dataToSend = TestDataCreationHelper.createRandomByteBufferFlux(options.getSize());
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        //Send data in blocks of 1MB or less.
        return service.setRawData(endpoint, dataToSend, length)
            .then();
    }
}
