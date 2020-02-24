// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.http.HttpHeaders;
import com.azure.cosmos.implementation.http.HttpRequest;
import com.azure.cosmos.implementation.http.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

class ResponseUtils {

    static Mono<StoreResponse> toStoreResponse(HttpResponse httpClientResponse, HttpRequest httpRequest) {

        HttpHeaders httpResponseHeaders = httpClientResponse.headers();

        Mono<String> contentObservable;

        if (httpRequest.httpMethod() == HttpMethod.DELETE) {
            // for delete we don't expect any body
            contentObservable = Mono.just(StringUtils.EMPTY);
        } else {
            contentObservable = httpClientResponse.bodyAsString().switchIfEmpty(Mono.just(StringUtils.EMPTY));
        }

        return contentObservable.flatMap(content -> {
            try {
                // transforms to Mono<StoreResponse>
                StoreResponse rsp = new StoreResponse(httpClientResponse.statusCode(), HttpUtils.unescape(httpResponseHeaders.toMap().entrySet()), content);
                return Mono.just(rsp);
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }
}
