// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.internal.http.HttpHeaders;
import com.azure.data.cosmos.internal.http.HttpRequest;
import com.azure.data.cosmos.internal.http.HttpResponse;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class ResponseUtils {
    private final static int INITIAL_RESPONSE_BUFFER_SIZE = 1024;

    public static Mono<String> toString(Flux<ByteBuf> contentObservable) {
        return contentObservable
                .reduce(
                        new ByteArrayOutputStream(INITIAL_RESPONSE_BUFFER_SIZE),
                        (out, bb) -> {
                            try {
                                bb.readBytes(out, bb.readableBytes());
                                return out;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .map(out -> new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    static Mono<StoreResponse> toStoreResponse(HttpResponse httpClientResponse, HttpRequest httpRequest) {

        HttpHeaders httpResponseHeaders = httpClientResponse.headers();

        Mono<String> contentObservable;

        if (httpRequest.httpMethod() == HttpMethod.DELETE) {
            // for delete we don't expect any body
            contentObservable = Mono.just(StringUtils.EMPTY);
        } else {
            contentObservable = toString(httpClientResponse.body());
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
