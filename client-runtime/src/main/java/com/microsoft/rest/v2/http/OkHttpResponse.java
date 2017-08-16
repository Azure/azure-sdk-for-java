/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;

/**
 * A HttpResponse that is implemented using OkHttp.
 */
public class OkHttpResponse implements HttpResponse {
    private final Response response;

    /**
     * Create a new OkHttpResponse using the provided OkHttp Response object.
     * @param response The OkHttp Response object that came from a real OkHttpClient object.
     */
    public OkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public boolean hasBody() {
        return response.body() != null;
    }

    @Override
    public InputStream bodyAsInputStream() {
        return response.body().byteStream();
    }

    @Override
    public byte[] bodyAsByteArray() throws IOException {
        return response.body().bytes();
    }

    @Override
    public String bodyAsString() throws IOException {
        return response.body().string();
    }
}
