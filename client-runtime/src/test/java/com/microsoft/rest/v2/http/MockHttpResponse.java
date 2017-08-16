/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.microsoft.rest.protocol.SerializerAdapter;
import com.microsoft.rest.serializer.JacksonAdapter;
import rx.Single;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MockHttpResponse extends HttpResponse {
    private final static SerializerAdapter<?> serializer = new JacksonAdapter();

    private final boolean hasBody;
    private byte[] byteArray;
    private String string;

    public MockHttpResponse() {
        hasBody = false;
    }

    public MockHttpResponse(byte[] byteArray) {
        hasBody = true;
        this.byteArray = byteArray;
    }

    public MockHttpResponse(String string) {
        hasBody = true;
        this.string = string;
    }

    public MockHttpResponse(Object serializable) {
        hasBody = true;
        try {
            this.string = serializer.serialize(serializable);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Single<? extends InputStream> getBodyAsInputStreamAsync() {
        return Single.just(new ByteArrayInputStream(byteArray));
    }

    @Override
    public Single<byte[]> getBodyAsByteArrayAsync() {
        return Single.just(byteArray);
    }

    @Override
    public Single<String> getBodyAsStringAsync() {
        return Single.just(string);
    }
}
