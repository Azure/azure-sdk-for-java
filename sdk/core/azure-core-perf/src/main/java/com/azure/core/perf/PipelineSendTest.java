// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpRequestMetadata;
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
        return Mono.fromSupplier(binaryDataSupplier)
            .flatMap(data -> {
                HttpHeaders headers = new HttpHeaders();
                headers.set("Content-Length", contentLengthHeaderValue);
                HttpRequest httpRequest = new HttpRequest(HttpMethod.PUT, targetURL, headers, data)
                    .setMetadata(new HttpRequestMetadata(null, null, true, false));
                // HttpRequestMetadata with 'eagerlyReadResponse' makes sure that the response is disposed to prevent
                // connection leaks. There's no response body in this scenario anyway.
                return httpPipeline.send(httpRequest, Context.NONE)
                    .flatMap(httpResponse -> (httpResponse.getStatusCode() / 100 != 2)
                        ? Mono.error(new IllegalStateException("Endpoint didn't return 2xx http status code."))
                        : Mono.just(httpResponse))
                    .then();
            });
    }
}
