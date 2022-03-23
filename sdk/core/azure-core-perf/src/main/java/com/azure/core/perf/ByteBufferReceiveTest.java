// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
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
    public Mono<Void> globalSetupAsync() {
        ByteBufferSendTest sendTest = new ByteBufferSendTest(options);
        return super.globalSetupAsync()
            .then(Mono.defer(sendTest::globalSetupAsync))
            .then(Mono.defer(sendTest::setupAsync))
            .then(Mono.defer(sendTest::runAsync))
            .then(Mono.defer(sendTest::cleanupAsync))
            .then(Mono.defer(sendTest::globalCleanupAsync));
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getRawDataAsync(endpoint)
           .flatMapMany(response -> response.getValue())
           .map(byteBuffer -> {
               for (int i = 0; i < byteBuffer.remaining(); i++) {
                   byteBuffer.get();
               }
               return 1;
           }).then();
    }
}
