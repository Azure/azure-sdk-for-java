/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import okhttp3.Response;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;

/**
 * A HttpResponse that is implemented using OkHttp.
 */
class OkHttpResponse extends HttpResponse {
    private final Response response;

    /**
     * Create a new OkHttpResponse using the provided OkHttp Response object.
     * @param response The OkHttp Response object that came from a real OkHttpClient object.
     */
    OkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public int statusCode() {
        return response.code();
    }

    @Override
    public String headerValue(String headerName) {
        return response.header(headerName);
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return Single.just(response.body().byteStream());
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        Single<byte[]> result;
        try {
            result = Single.just(response.body().bytes());
        }
        catch (IOException e) {
            result = Single.error(e);
        }
        return result;
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        Single<String> result;
        try {
            result = Single.just(response.body().string());
        }
        catch (IOException e) {
            result = Single.error(e);
        }
        return result;
    }
}
