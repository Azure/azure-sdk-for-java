// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpClient;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.MockHttpClient;
import com.azure.core.perf.core.RestProxyTestBase;
import reactor.core.publisher.Mono;

import java.util.Random;

public class ByteBufferReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public ByteBufferReceiveTest(CorePerfStressOptions options) {
        super(options, createMockHttpClient(options));
    }

    private static HttpClient createMockHttpClient(CorePerfStressOptions options) {
        byte[] bodyBytes = new byte[(int) options.getSize()];
        new Random(0).nextBytes(bodyBytes);
        return new MockHttpClient(httpRequest -> createMockResponse(httpRequest,
            "application/octet-stream", bodyBytes));
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return new ByteBufferSendTest(options).runAsync();
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
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
