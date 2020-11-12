// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

/**
 * Mock response transformer used to test {@link NettyAsyncHttpClient}.
 */
public final class ReactorNettyClientResponseTransformer extends ResponseTransformer {
    public static final String NAME = "reactor-netty-client-response-transformer";
    public static final String NULL_REPLACEMENT = "null";

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {
        String url = request.getUrl();

        if ("/httpHeaders".equalsIgnoreCase(url)) {
            return httpHeadersResponseHandler(request, response);
        }

        return response;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean applyGlobally() {
        return false;
    }

    private static Response httpHeadersResponseHandler(Request request, Response response) {
        String responseTestHeaderValue = request.containsHeader(ReactorNettyClientTests.TEST_HEADER)
            ? request.getHeaders().getHeader(ReactorNettyClientTests.TEST_HEADER).firstValue()
            : NULL_REPLACEMENT;

        return new Response.Builder()
            .status(response.getStatus())
            .headers(new HttpHeaders(new HttpHeader(ReactorNettyClientTests.TEST_HEADER, responseTestHeaderValue)))
            .build();
    }
}
