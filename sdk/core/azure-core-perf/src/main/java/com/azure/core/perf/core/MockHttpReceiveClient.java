// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.perf.models.MockHttpResponse;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

public class MockHttpReceiveClient implements HttpClient {
    byte[] receivedBytes = null;

    @Override
    public Mono<HttpResponse> send(HttpRequest request) {
        String url = request.getUrl().toString();
        if (url.endsWith("SetRawData") || url.endsWith("SetUserDatabase")) {
            return FluxUtil.collectBytesInByteBufferStream(request.getBody())
                       .map(bytes -> {
                           receivedBytes = bytes;
                           return new MockHttpResponse(request, 200);
                       });
        } else {
            return Mono.<HttpResponse>just(new MockHttpResponse(request, 404));
        }
    }
}
