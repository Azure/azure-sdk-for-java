// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

/**
 * Preserves HTTP polling failures before the operation status is deserialized.
 */
public final class FileIngestionPollingHttpClient implements HttpClient {
    private static final ClientLogger LOGGER = new ClientLogger(FileIngestionPollingHttpClient.class);

    private final HttpPipeline pipeline;

    private FileIngestionPollingHttpClient(HttpPipeline pipeline) {
        this.pipeline = pipeline;
    }

    /**
     * Creates a polling pipeline that validates responses after the original pipeline has applied all its policies.
     *
     * @param pipeline the original client pipeline.
     * @return the polling pipeline.
     */
    public static HttpPipeline createPipeline(HttpPipeline pipeline) {
        return new HttpPipelineBuilder().httpClient(new FileIngestionPollingHttpClient(pipeline))
            .tracer(pipeline.getTracer())
            .build();
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        return send(request, Context.NONE);
    }

    @Override
    public Mono<HttpResponse> send(HttpRequest request, Context context) {
        return pipeline.send(request, context).flatMap(response -> {
            if (isSuccessful(response)) {
                return Mono.just(response);
            }
            return Mono.using(() -> response, errorResponse -> {
                HttpResponse bufferedResponse = errorResponse.buffer();
                return bufferedResponse.getBodyAsByteArray()
                    .then(Mono.error(LOGGER.logExceptionAsError(pollingError(bufferedResponse))));
            }, HttpResponse::close);
        });
    }

    @Override
    public HttpResponse sendSync(HttpRequest request, Context context) {
        HttpResponse response = pipeline.sendSync(request, context);
        if (isSuccessful(response)) {
            return response;
        }
        try (HttpResponse errorResponse = response) {
            HttpResponse bufferedResponse = errorResponse.buffer();
            bufferedResponse.getBodyAsBinaryData().toBytes();
            throw LOGGER.logExceptionAsError(pollingError(bufferedResponse));
        }
    }

    private static boolean isSuccessful(HttpResponse response) {
        return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
    }

    private static HttpResponseException pollingError(HttpResponse response) {
        return new HttpResponseException(
            "File ingestion polling failed with HTTP status " + response.getStatusCode() + ".", response);
    }
}
