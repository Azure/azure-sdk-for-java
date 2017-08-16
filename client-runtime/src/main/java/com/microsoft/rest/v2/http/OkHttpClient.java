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

import java.io.IOException;

/**
 * A HttpClient that is implemented using OkHttp.
 */
public class OkHttpClient implements HttpClient {
    private final okhttp3.OkHttpClient client;

    /**
     * Create a new OkHttpClient.
     * @param client The inner OkHttpClient implementation.
     */
    public OkHttpClient(okhttp3.OkHttpClient client) {
        this.client = client;
    }

    /**
     * Send the provided request and block until the response is received.
     * @param request The HTTP request to send.
     * @return The HTTP response received.
     */
    @Override
    public HttpResponse sendRequest(HttpRequest request) throws IOException {
        RequestBody requestBody = null;
        final String requestBodyString = request.body();
        if (requestBodyString != null && !requestBodyString.isEmpty()) {
            final MediaType mediaType = MediaType.parse(request.mimeType());
            requestBody = RequestBody.create(mediaType, requestBodyString);
        }

        final Request.Builder requestBuilder = new Request.Builder()
                .method(request.method(), requestBody)
                .url(request.url());

        final Request okhttpRequest = requestBuilder.build();
        final Call call = client.newCall(okhttpRequest);

        final Response response = call.execute();

        return new OkHttpResponse(response);
    }
}
