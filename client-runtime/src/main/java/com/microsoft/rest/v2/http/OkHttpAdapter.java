/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.io.ByteStreams;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Single;

import java.io.IOException;
import java.io.InputStream;

/**
 * A HttpClient that is implemented using OkHttp.
 */
public class OkHttpAdapter extends HttpClient {
    private final okhttp3.OkHttpClient client;

    /**
     * Create a new OkHttpAdapter.
     * @param client The inner OkHttpAdapter implementation.
     */
    public OkHttpAdapter(okhttp3.OkHttpClient client) {
        this.client = client;
    }

    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     */
    @Override
    public Single<HttpResponse> sendRequestInternalAsync(HttpRequest request) {
        RequestBody requestBody = null;

        Single<HttpResponse> result;

        final HttpRequestBody body = request.body();
        try {
            if (body != null) {
                final MediaType mediaType = MediaType.parse(request.mimeType());
                try (final InputStream bodyStream = body.createInputStream()) {
                    requestBody = RequestBody.create(mediaType, ByteStreams.toByteArray(bodyStream));
                }
            }

            final Request.Builder requestBuilder = new Request.Builder()
                    .method(request.httpMethod(), requestBody)
                    .url(request.url());

            for (HttpHeader header : request.headers()) {
                requestBuilder.addHeader(header.name(), header.value());
            }

            final Request okhttpRequest = requestBuilder.build();
            final Call call = client.newCall(okhttpRequest);

            final Response response = call.execute();
            result = Single.<HttpResponse>just(new OkHttpResponse(response));
        }
        catch (IOException e) {
            result = Single.error(e);
        }

        return result;
    }
}
