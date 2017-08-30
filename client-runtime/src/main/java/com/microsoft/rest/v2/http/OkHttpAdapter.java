/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Single;

import java.io.IOException;

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
    public Single<? extends HttpResponse> sendRequestAsync(HttpRequest request) {
        RequestBody requestBody = null;
        final String requestBodyString = request.body();
        if (requestBodyString != null && !requestBodyString.isEmpty()) {
            final MediaType mediaType = MediaType.parse(request.mimeType());
            requestBody = RequestBody.create(mediaType, requestBodyString);
        }

        final Request.Builder requestBuilder = new Request.Builder()
                .method(request.httpMethod(), requestBody)
                .url(request.url());

        for (HttpHeader header : request.headers()) {
            requestBuilder.addHeader(header.name(), header.value());
        }

        final Request okhttpRequest = requestBuilder.build();
        final Call call = client.newCall(okhttpRequest);

        Single<? extends HttpResponse> result;
        try {
            final Response response = call.execute();
            result = Single.just(new OkHttpResponse(response));
        }
        catch (IOException e) {
            result = Single.error(e);
        }

        return result;
    }
}
