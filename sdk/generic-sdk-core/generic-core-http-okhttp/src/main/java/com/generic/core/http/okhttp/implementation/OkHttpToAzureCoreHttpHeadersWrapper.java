// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.okhttp.implementation;

import com.generic.core.models.HeaderName;
import com.generic.core.models.Header;
import okhttp3.Headers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Wraps an OkHttp HttpHeaders instance and provides an azure-core HttpHeaders view onto it. This avoids the need to
 * copy the OkHttp HttpHeaders into an azure-core HttpHeaders instance. Whilst it's not necessary to support mutability
 * (as these headers are the result of an OkHttp response), we do so in any case, given the additional implementation
 * cost is minimal.
 */
public final class OkHttpToAzureCoreHttpHeadersWrapper extends com.generic.core.models.Headers {
    private final Headers okhttpHeaders;

    private com.generic.core.models.Headers coreHeaders;
    private boolean converted = false;

    public OkHttpToAzureCoreHttpHeadersWrapper(Headers okhttpHeaders) {
        this.okhttpHeaders = okhttpHeaders;
        this.coreHeaders = new com.generic.core.models.Headers(okhttpHeaders.size() * 2);
    }

    @Override
    public int getSize() {
        return converted ? coreHeaders.getSize() : okhttpHeaders.size();
    }

    @Override
    public com.generic.core.models.Headers add(HeaderName name, String value) {
        if (name == null || value == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.add(name, value);

        return this;
    }


    @Override
    public com.generic.core.models.Headers set(HeaderName name, String value) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, value);

        return this;
    }

    @Override
    public com.generic.core.models.Headers set(HeaderName name, List<String> values) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        coreHeaders.set(name, values);

        return this;
    }

    @Override
    public com.generic.core.models.Headers setAll(Map<String, List<String>> headers) {
        convertIfNeeded();

        coreHeaders.setAll(headers);

        return this;
    }

    @Override
    public com.generic.core.models.Headers setAllHeaders(com.generic.core.models.Headers headers) {
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
    public String[] getValues(HeaderName name) {
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

        coreHeaders = OkHttpResponseBase.fromOkHttpHeaders(okhttpHeaders);
        converted = true;
    }
}
