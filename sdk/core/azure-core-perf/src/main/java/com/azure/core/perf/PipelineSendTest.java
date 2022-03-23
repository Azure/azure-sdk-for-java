// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.perf.core.CorePerfStressOptions;
import com.azure.core.perf.core.RestProxyTestBase;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.UrlBuilder;
import reactor.core.publisher.Mono;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Supplier;

public class PipelineSendTest extends RestProxyTestBase<CorePerfStressOptions> {

    private final Supplier<BinaryData> binaryDataSupplier;
    private final URL targetURL;
    private final String contentLengthHeaderValue;
    private final Context context = Context.NONE.addData("azure-eagerly-read-response" , true);

    public PipelineSendTest(CorePerfStressOptions options) {
        super(options);
        binaryDataSupplier = createBinaryDataSupplier(options);
        try {
            UrlBuilder urlBuilder = UrlBuilder.parse(endpoint);
            String path = urlBuilder.getPath();
            path = path == null ? "" : path;
            targetURL = urlBuilder
                .setPath(path + "/BinaryData/" + id)
                .toUrl();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
        contentLengthHeaderValue = Long.toString(options.getSize());
    }

    @Override
    public void run() {
        runAsync().block();
    }

    @Override
    public Mono<Void> runAsync() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Length", contentLengthHeaderValue);
        HttpRequest httpRequest = new HttpRequest(
            HttpMethod.PUT, targetURL, headers, binaryDataSupplier.get().toFluxByteBuffer());
        // Context with azure-eagerly-read-response=true makes sure
        // that response is disposed to prevent connection leak.
        // There's no response body in this scenario anyway.
        return httpPipeline.send(httpRequest, context)
            .map(httpResponse -> {
                if (httpResponse.getStatusCode() / 100 != 2) {
                    throw new IllegalStateException("Endpoint didn't return 2xx http status code.");
                }
                return httpResponse;
            })
            .then();
    }
}
