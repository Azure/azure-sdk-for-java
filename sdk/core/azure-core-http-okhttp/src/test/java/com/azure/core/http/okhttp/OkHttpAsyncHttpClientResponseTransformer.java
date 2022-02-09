// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import static com.azure.core.http.okhttp.OkHttpAsyncHttpClientTests.RETURN_HEADERS_AS_IS_PATH;

/**
 * Mock response transformer used to test {@link OkHttpAsyncHttpClient}.
 */
public class OkHttpAsyncHttpClientResponseTransformer extends ResponseTransformer {
    public static final String NAME = "okhttp-async-http-client-response-transformer";

    @Override
    public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {
        String url = request.getUrl();

        if (RETURN_HEADERS_AS_IS_PATH.equalsIgnoreCase(url)) {
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
}
