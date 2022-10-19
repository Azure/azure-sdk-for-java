// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.perf.test.core.TestDataCreationHelper;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.function.Function;

public class ByteBufferReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public ByteBufferReceiveTest(CorePerfStressOptions options) {
        super(options, createMockResponseSupplier(options));
    }

    private static Function<HttpRequest, HttpResponse> createMockResponseSupplier(CorePerfStressOptions options) {
        byte[] bodyBytes = new byte[(int) options.getSize()];
        new Random(0).nextBytes(bodyBytes);
        return httpRequest -> createMockResponse(httpRequest,
            "application/octet-stream", bodyBytes);
    }

    @Override
    public Mono<Void> setupAsync() {
        return service.setRawData(
            endpoint, id, TestDataCreationHelper.createRandomByteBufferFlux(options.getSize()), options.getSize());
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getRawDataAsync(endpoint, id)
           .flatMapMany(response -> response.getValue())
           .map(byteBuffer -> {
               for (int i = 0; i < byteBuffer.remaining(); i++) {
                   byteBuffer.get();
               }
               return 1;
           }).then();
    }
}
