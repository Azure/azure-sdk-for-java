// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.http.models.HttpResponse;
import com.generic.core.models.BinaryData;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpResponse extends OkHttpResponseBase {
    private static final BinaryData EMPTY_BODY = BinaryData.fromBytes(new byte[0]);

    public OkHttpResponse(Response response, HttpRequest request, boolean eagerlyConvertHeaders) {
        super(response, request, eagerlyConvertHeaders, toBinaryData(response.body()));
    }

    private static BinaryData toBinaryData(ResponseBody responseBody) {
        return responseBody == null ? EMPTY_BODY : new OkHttpResponseBinaryData(responseBody);
    }

    @Override
    public HttpResponse buffer() {
        return new OkHttpBufferedResponse(getRequest(), getHeaders(), getStatusCode(),
            getBody().toReplayableBinaryData());
    }
}
