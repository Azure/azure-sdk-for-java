// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.core.MockHttpReceiveClient;
import com.azure.core.perf.core.MyRestProxyService;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class ByteBufferSendTest extends RestProxyTestBase<PerfStressOptions> {
    private final MockHttpReceiveClient mockHttpReceiveClient;
    private final MyRestProxyService service;
    private final Flux<ByteBuffer> dataToSend;

    public ByteBufferSendTest(PerfStressOptions options) {
        super(options);
        //Creates Flux of ByteBuffer with blockSize of 1MB by default.
        //TODO: parametrize the block size
        dataToSend = TestDataCreationHelper.createRandomByteBufferFlux(options.getSize());
        mockHttpReceiveClient = new MockHttpReceiveClient();
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(mockHttpReceiveClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, pipeline);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        //Send data in blocks of 1MB or less.
        return dataToSend
            .map(byteBuffer -> {
                service.setRawData(Flux.just(byteBuffer), byteBuffer.remaining());
                return 1;
            }).then();
    }
}
