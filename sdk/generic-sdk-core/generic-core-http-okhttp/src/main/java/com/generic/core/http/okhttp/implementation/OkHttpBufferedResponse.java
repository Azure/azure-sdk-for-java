// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import okhttp3.Response;

/**
 * An OkHttp response where the response body has been buffered into memory.
 */
public final class OkHttpBufferedResponse extends OkHttpResponseBase {
    private final byte[] value;

    public OkHttpBufferedResponse(Response response, HttpRequest request, byte[] value,
                                  boolean eagerlyConvertHeaders) {
        super(response, request, eagerlyConvertHeaders);

        this.value = value;
    }

    @Override
    public BinaryData getBody() {
        return BinaryData.fromBytes(value);
    }

    @Override
    public byte[] getValue() {
        return value;
    }
}
