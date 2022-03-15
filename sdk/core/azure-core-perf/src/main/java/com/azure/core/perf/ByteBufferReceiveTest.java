// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.perf.core.MockHttpClient;
import com.azure.core.perf.core.MyRestProxyService;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.perf.test.core.PerfStressOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Random;

public class ByteBufferReceiveTest extends RestProxyTestBase<PerfStressOptions> {
    private final MockHttpClient mockHTTPClient;
    private final MyRestProxyService service;
    private final byte[] bodyBytes;

    public ByteBufferReceiveTest(PerfStressOptions options) throws IOException, URISyntaxException {
        super(options);
        bodyBytes = new byte[(int) options.getSize()];
        new Random(0).nextBytes(bodyBytes);
        mockHTTPClient = new MockHttpClient(httpRequest -> createMockResponse(httpRequest,
            "application/octet-stream", bodyBytes));
        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(mockHTTPClient)
            .build();

        service = RestProxy.create(MyRestProxyService.class, pipeline);
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getRawDataAsync()
           .flatMapMany(response -> response.getValue())
           .map(byteBuffer -> {
               for (int i = 0; i < byteBuffer.remaining(); i++) {
                   byteBuffer.get();
               }
               return 1;
           }).then();
    }
}
