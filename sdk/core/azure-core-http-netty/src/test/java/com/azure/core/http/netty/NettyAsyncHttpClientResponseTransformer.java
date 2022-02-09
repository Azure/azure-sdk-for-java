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

import static com.azure.core.http.netty.NettyAsyncHttpClientTests.EXPECTED_HEADER;
import static com.azure.core.http.netty.NettyAsyncHttpClientTests.HTTP_HEADERS_PATH;
import static com.azure.core.http.netty.NettyAsyncHttpClientTests.NO_DOUBLE_UA_PATH;
import static com.azure.core.http.netty.NettyAsyncHttpClientTests.RETURN_HEADERS_AS_IS_PATH;

/**
 * Mock response transformer used to test {@link NettyAsyncHttpClient}.
 */
public final class NettyAsyncHttpClientResponseTransformer extends ResponseTransformer {
    public static final String NAME = "reactor-netty-client-response-transformer";
    public static final String NULL_REPLACEMENT = "null";

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {
        String url = request.getUrl();

        if (HTTP_HEADERS_PATH.equalsIgnoreCase(url)) {
            return httpHeadersResponseHandler(request, response);
        } else if (NO_DOUBLE_UA_PATH.equalsIgnoreCase(url)) {
            if (EXPECTED_HEADER.equals(request.getHeader("User-Agent"))) {
                return response;
            } else {
                return Response.response()
                    .status(400)
                    .build();
            }
        } else if (RETURN_HEADERS_AS_IS_PATH.equalsIgnoreCase(url)) {
            return Response.response()
                .status(200)
                .headers(request.getHeaders())
                .build();
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
        String responseTestHeaderValue = request.containsHeader(NettyAsyncHttpClientTests.TEST_HEADER)
            ? request.getHeaders().getHeader(NettyAsyncHttpClientTests.TEST_HEADER).firstValue()
            : NULL_REPLACEMENT;

        return new Response.Builder()
            .status(response.getStatus())
            .headers(new HttpHeaders(new HttpHeader(NettyAsyncHttpClientTests.TEST_HEADER, responseTestHeaderValue)))
            .build();
    }
}
