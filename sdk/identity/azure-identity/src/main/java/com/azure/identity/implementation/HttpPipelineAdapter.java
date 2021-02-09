// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpClient;
import com.microsoft.aad.msal4j.IHttpResponse;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * Adapts an HttpPipeline to an instance of IHttpClient in the MSAL4j pipeline.
 */
class HttpPipelineAdapter implements IHttpClient {
    private final HttpPipeline httpPipeline;

    HttpPipelineAdapter(HttpPipeline httpPipeline) {
        this.httpPipeline = httpPipeline;
    }

    @Override
    public IHttpResponse send(HttpRequest httpRequest) throws Exception {
        // convert request
        com.azure.core.http.HttpRequest request = new com.azure.core.http.HttpRequest(
            HttpMethod.valueOf(httpRequest.httpMethod().name()),
            httpRequest.url());
        if (httpRequest.headers() != null) {
            request.setHeaders(new HttpHeaders(httpRequest.headers()));
        }
        if (httpRequest.body() != null) {
            request.setBody(httpRequest.body());
        }

        return httpPipeline.send(request)
            .flatMap(response -> response.getBodyAsString()
                .map(body -> {
                    com.microsoft.aad.msal4j.HttpResponse httpResponse = new com.microsoft.aad.msal4j.HttpResponse()
                        .body(body)
                        .statusCode(response.getStatusCode());
                    httpResponse.addHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpHeader::getName,
                        HttpHeader::getValuesList)));
                    return httpResponse;
                })
                // if no body
                .switchIfEmpty(Mono.defer(() -> {
                    com.microsoft.aad.msal4j.HttpResponse httpResponse = new com.microsoft.aad.msal4j.HttpResponse()
                        .statusCode(response.getStatusCode());
                    httpResponse.addHeaders(response.getHeaders().stream().collect(Collectors.toMap(HttpHeader::getName,
                        HttpHeader::getValuesList)));
                    return Mono.just(httpResponse);
                })))
            .block();
    }
}
