// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;
import okhttp3.Response;

/**
 * An OkHttp response where the response body has been buffered into memory.
 */
public final class OkHttpBufferedResponse extends OkHttpResponseBase {
    public OkHttpBufferedResponse(Response response, HttpRequest request, byte[] body, boolean eagerlyConvertHeaders) {
        super(response, request, eagerlyConvertHeaders, BinaryData.fromBytes(body));
    }

    OkHttpBufferedResponse(HttpRequest request, Headers headers, int statusCode, BinaryData body) {
        super(request, headers, statusCode, body);
    }

    @Override
    public HttpResponse buffer() {
        return this;
    }
}
