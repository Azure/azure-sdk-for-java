// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.HttpHeader;
import com.generic.core.http.models.HttpHeaderName;
import com.generic.core.http.models.HttpHeaders;
import com.generic.core.http.models.Response;
import okhttp3.Headers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Wraps an {@link Headers OkHttp Headers} instance and provides a {@link HttpHeaders generic-core Headers} view
 * onto it. This avoids the need to copy the {@link Headers OkHttp Headers} into a
 * {@link HttpHeaders generic-core Headers} instance. Whilst it's not necessary to support mutability (as these headers
 * are the result of an {@link Response OkHttp Response}), we do so in any case, given the additional implementation
 * cost is minimal.
 */
public final class OkHttpToCoreHttpHeadersWrapper extends HttpHeaders {
    private final Headers okhttpHeaders;

    private HttpHeaders coreHeaders;
    private boolean converted = false;

    public OkHttpToCoreHttpHeadersWrapper(Headers okhttpHeaders) {
        this.okhttpHeaders = okhttpHeaders;
        this.coreHeaders = new HttpHeaders(okhttpHeaders.size() * 2);
    }

    @Override
    public int getSize() {
        return converted ? coreHeaders.getSize() : okhttpHeaders.size();
    }

    @Override
    public HttpHeaders add(HttpHeaderName name, String value) {
        if (name == null || value == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.add(name, value);

        return this;
    }


    @Override
    public HttpHeaders set(HttpHeaderName name, String value) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, value);

        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, List<String> values) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, values);

        return this;
    }

    @Override
    public HttpHeaders setAll(Map<String, List<String>> headers) {
        convertIfNeeded();

        coreHeaders.setAll(headers);

        return this;
    }

    @Override
    public HttpHeaders setAllHeaders(HttpHeaders headers) {
        convertIfNeeded();

        coreHeaders.setAllHeaders(headers);

        return this;
    }

    @Override
    public HttpHeader get(HttpHeaderName name) {
        convertIfNeeded();

        return coreHeaders.get(name);
    }

    @Override
    public HttpHeader remove(HttpHeaderName name) {
        convertIfNeeded();

        return coreHeaders.remove(name);
    }

    @Override
    public String getValue(HttpHeaderName name) {
        convertIfNeeded();

        return coreHeaders.getValue(name);
    }

    @Override
    public List<String> getValues(HttpHeaderName name) {
        convertIfNeeded();

        return coreHeaders.getValues(name);
    }

    @Override
    public Map<String, String> toMap() {
        convertIfNeeded();

        return coreHeaders.toMap();
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        convertIfNeeded();

        return coreHeaders.iterator();
    }

    @Override
    public Stream<HttpHeader> stream() {
        convertIfNeeded();

        return coreHeaders.stream();
    }

    @Override
    public String toString() {
        convertIfNeeded();

        return coreHeaders.toString();
    }

    private void convertIfNeeded() {
        if (converted) {
            return;
        }

        coreHeaders = OkHttpResponse.fromOkHttpHeaders(okhttpHeaders);
        converted = true;
    }
}
