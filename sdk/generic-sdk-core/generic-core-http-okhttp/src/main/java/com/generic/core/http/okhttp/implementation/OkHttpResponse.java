// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Default HTTP response for OkHttp.
 */
public final class OkHttpResponse extends OkHttpResponseBase {
    private final ResponseBody responseBody;

    private BinaryData value;

    public OkHttpResponse(Response response, HttpRequest request, boolean eagerlyConvertHeaders) {
        super(response, request, eagerlyConvertHeaders);

        // innerResponse.body() getter will not return null for server returned responses.
        // It can be null:
        // [a]. if response is built manually with null body (e.g for mocking)
        // [b]. for the cases described here
        // [ref](https://square.github.io/okhttp/4.x/okhttp/okhttp3/-response/body/).
        this.responseBody = response.body();
    }

    @Override
    public BinaryData getValue() {
        if (this.value == null) {
            if (this.responseBody == null) {
                this.value = BinaryData.fromBytes(new byte[0]);
            } else {
                this.value = BinaryData.fromStream(this.responseBody.byteStream());
            }
        }

        return this.value;
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
