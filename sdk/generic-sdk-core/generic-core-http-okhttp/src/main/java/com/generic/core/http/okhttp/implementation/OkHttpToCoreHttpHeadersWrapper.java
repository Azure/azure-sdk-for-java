// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.http.models.Header;
import com.generic.core.http.models.HeaderName;
import com.generic.core.http.models.Headers;
import com.generic.core.http.models.Response;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Wraps an {@link okhttp3.Headers OkHttp Headers} instance and provides a {@link Headers generic-core Headers} view
 * onto it. This avoids the need to copy the {@link okhttp3.Headers OkHttp Headers} into a
 * {@link Headers generic-core Headers} instance. Whilst it's not necessary to support mutability (as these headers
 * are the result of an {@link Response OkHttp Response}), we do so in any case, given the additional implementation
 * cost is minimal.
 */
public final class OkHttpToCoreHttpHeadersWrapper extends Headers {
    private final okhttp3.Headers okhttpHeaders;

    private Headers coreHeaders;
    private boolean converted = false;

    public OkHttpToCoreHttpHeadersWrapper(okhttp3.Headers okhttpHeaders) {
        this.okhttpHeaders = okhttpHeaders;
        this.coreHeaders = new Headers(okhttpHeaders.size() * 2);
    }

    @Override
    public int getSize() {
        return converted ? coreHeaders.getSize() : okhttpHeaders.size();
    }

    @Override
    public com.generic.core.http.models.Headers add(HeaderName name, String value) {
        if (name == null || value == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.add(name, value);

        return this;
    }


    @Override
    public com.generic.core.http.models.Headers set(HeaderName name, String value) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, value);

        return this;
    }

    @Override
    public com.generic.core.http.models.Headers set(HeaderName name, List<String> values) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, values);

        return this;
    }

    @Override
    public com.generic.core.http.models.Headers setAll(Map<String, List<String>> headers) {
        convertIfNeeded();

        coreHeaders.setAll(headers);

        return this;
    }

    @Override
    public com.generic.core.http.models.Headers setAllHeaders(com.generic.core.http.models.Headers headers) {
        convertIfNeeded();

        coreHeaders.setAllHeaders(headers);

        return this;
    }

    @Override
    public Header get(HeaderName name) {
        convertIfNeeded();

        return coreHeaders.get(name);
    }

    @Override
    public Header remove(HeaderName name) {
        convertIfNeeded();

        return coreHeaders.remove(name);
    }

    @Override
    public String getValue(HeaderName name) {
        convertIfNeeded();

        return coreHeaders.getValue(name);
    }

    @Override
    public List<String> getValues(HeaderName name) {
        convertIfNeeded();

        return coreHeaders.getValues(name);
    }

    @Override
    public Map<String, String> toMap() {
        convertIfNeeded();

        return coreHeaders.toMap();
    }

    @Override
    public Iterator<Header> iterator() {
        convertIfNeeded();

        return coreHeaders.iterator();
    }

    @Override
    public Stream<Header> stream() {
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
