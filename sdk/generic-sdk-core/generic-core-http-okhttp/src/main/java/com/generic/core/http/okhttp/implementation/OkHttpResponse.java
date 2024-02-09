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
    // Previously, this was 4096, but it is being changed to 8192 as that more closely aligns to what Netty uses as a
    // default and will reduce the number of small allocations we'll need to make.
    private static final int BYTE_BUFFER_CHUNK_SIZE = 8192;

    private final ResponseBody responseBody;

    private BinaryData body;

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
    public BinaryData getBody() {
        if (this.body == null) {
            this.body = BinaryData.fromStream(this.responseBody.byteStream());
        }

        return body;
    }

    @Override
    public byte[] getValue() {
        return getBody().toBytes();
    }

    @Override
    public void close() {
        if (this.responseBody != null) {
            // It's safe to invoke close() multiple times, additional calls will be ignored.
            this.responseBody.close();
        }
    }
}
