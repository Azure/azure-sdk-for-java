/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.http;

import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import com.microsoft.rest.v2.http.HttpHeaders;
import com.microsoft.rest.v2.http.HttpResponse;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockAzureHttpResponse extends HttpResponse {
    private final static SerializerAdapter<?> serializer = new JacksonAdapter();

    private final int statusCode;

    private final HttpHeaders headers;

    private byte[] byteArray;
    private String string;

    public MockAzureHttpResponse(int statusCode) {
        this.statusCode = statusCode;
        headers = new HttpHeaders();
    }

    public MockAzureHttpResponse(int statusCode, byte[] byteArray) {
        this(statusCode);

        this.byteArray = byteArray;
    }

    public MockAzureHttpResponse(int statusCode, String string) {
        this(statusCode);

        this.string = string;
    }

    public MockAzureHttpResponse(int statusCode, Object serializable) {
        this(statusCode);

        try {
            this.string = serializer.serialize(serializable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int statusCode() {
        return statusCode;
    }

    @Override
    public String headerValue(String headerName) {
        return headers.value(headerName);
    }

    public MockAzureHttpResponse withHeader(String headerName, String headerValue) {
        headers.add(headerName, headerValue);
        return this;
    }

    @Override
    public Single<? extends InputStream> bodyAsInputStreamAsync() {
        return Single.just(new ByteArrayInputStream(byteArray));
    }

    @Override
    public Single<byte[]> bodyAsByteArrayAsync() {
        return Single.just(byteArray);
    }

    @Override
    public Single<String> bodyAsStringAsync() {
        return Single.just(string);
    }
}
