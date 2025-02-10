// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.util.Random;
import java.util.function.Function;

public class BinaryDataReceiveTest extends RestProxyTestBase<CorePerfStressOptions> {

    public BinaryDataReceiveTest(CorePerfStressOptions options) {
        super(options, createMockResponseSupplier(options));
    }

    private static Function<HttpRequest, HttpResponse> createMockResponseSupplier(CorePerfStressOptions options) {
        byte[] bodyBytes = new byte[(int) options.getSize()];
        new Random(0).nextBytes(bodyBytes);
        return httpRequest -> createMockResponse(httpRequest, "application/octet-stream", bodyBytes);
    }

    @Override
    public Mono<Void> setupAsync() {
        byte[] bodyBytes = new byte[(int) options.getSize()];
        new Random(0).nextBytes(bodyBytes);
        return service.setBinaryData(endpoint, id, BinaryData.fromBytes(bodyBytes), options.getSize());
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        return service.getBinaryDataAsync(endpoint, id).map(Response::getValue).map(BinaryData::toBytes).then();
    }
}
