// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

public class PipelineSendTest extends RestProxyTestBase<CorePerfStressOptions> {

    private final Supplier<BinaryData> binaryDataSupplier;
    private final URL targetURL;

    public PipelineSendTest(CorePerfStressOptions options) {
        super(options);
        binaryDataSupplier = createBinaryDataSupplier(options);
        try {
            targetURL = new URL(new URL(endpoint), "BinaryData");
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        HttpRequest httpRequest = new HttpRequest(
            HttpMethod.PUT, targetURL, new HttpHeaders(), binaryDataSupplier.get().toFluxByteBuffer());
        return httpPipeline.send(httpRequest)
            .then();
    }
}
