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
    private final BinaryData value;

    public OkHttpBufferedResponse(Response response, HttpRequest request, boolean eagerlyConvertHeaders,
                                  byte[] bodyBytes) {
        super(response, request, eagerlyConvertHeaders);

        this.value = bodyBytes == null ? BinaryData.fromBytes(new byte[0]) : BinaryData.fromBytes(bodyBytes);
    }

    @Override
    public BinaryData getValue() {
        return this.value;
    }
}
