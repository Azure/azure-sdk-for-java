// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import okhttp3.Headers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.azure.core.http.okhttp.implementation.OkHttpAsyncResponseBase.fromOkHttpHeaders;

/**
 * Wraps an OkHttp HttpHeaders instance and provides an azure-core HttpHeaders view onto it. This avoids the need to
 * copy the OkHttp HttpHeaders into an azure-core HttpHeaders instance. Whilst it's not necessary to support mutability
 * (as these headers are the result of an OkHttp response), we do so in any case, given the additional implementation
 * cost is minimal.
 */
public final class OkHttpToAzureCoreHttpHeadersWrapper extends HttpHeaders {
    private final Headers okhttpHeaders;

    private HttpHeaders azureCoreHeaders;
    private boolean converted = false;

    public OkHttpToAzureCoreHttpHeadersWrapper(Headers okhttpHeaders) {
        this.okhttpHeaders = okhttpHeaders;
        this.azureCoreHeaders = new HttpHeaders(okhttpHeaders.size() * 2);
    }

    @Override
    public int getSize() {
        return converted ? azureCoreHeaders.getSize() : okhttpHeaders.size();
    }

    @Override
    @Deprecated
    public HttpHeaders add(String name, String value) {
        if (name == null || value == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(HttpHeaderName name, String value) {
        if (name == null || value == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.add(name, value);
        return this;
    }

    @Override
    @Deprecated
    public HttpHeaders set(String name, String value) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, String value) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.set(name, value);
        return this;
    }

    @Override
    @Deprecated
    public HttpHeaders set(String name, List<String> values) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, List<String> values) {
        if (name == null) {
            return this;
        }

        convertIfNeeded();

        azureCoreHeaders.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders setAll(Map<String, List<String>> headers) {
        convertIfNeeded();

        azureCoreHeaders.setAll(headers);
        return this;
    }

    @Override
    public HttpHeaders setAllHttpHeaders(HttpHeaders headers) {
        convertIfNeeded();

        azureCoreHeaders.setAllHttpHeaders(headers);
        return this;
    }

    @Override
    @Deprecated
    public HttpHeader get(String name) {
        convertIfNeeded();

        return azureCoreHeaders.get(name);
    }

    @Override
    public HttpHeader get(HttpHeaderName name) {
        convertIfNeeded();

        return azureCoreHeaders.get(name);
    }

    @Override
    @Deprecated
    public HttpHeader remove(String name) {
        convertIfNeeded();

        return azureCoreHeaders.remove(name);
    }

    @Override
    public HttpHeader remove(HttpHeaderName name) {
        convertIfNeeded();

        return azureCoreHeaders.remove(name);
    }

    @Override
    @Deprecated
    public String getValue(String name) {
        convertIfNeeded();

        return azureCoreHeaders.getValue(name);
    }

    @Override
    public String getValue(HttpHeaderName name) {
        convertIfNeeded();

        return azureCoreHeaders.getValue(name);
    }

    @Override
    @Deprecated
    public String[] getValues(String name) {
        convertIfNeeded();

        return azureCoreHeaders.getValues(name);
    }

    @Override
    public String[] getValues(HttpHeaderName name) {
        convertIfNeeded();

        return azureCoreHeaders.getValues(name);
    }

    @Override
    public Map<String, String> toMap() {
        convertIfNeeded();

        return azureCoreHeaders.toMap();
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        convertIfNeeded();

        return azureCoreHeaders.iterator();
    }

    @Override
    public Stream<HttpHeader> stream() {
        convertIfNeeded();

        return azureCoreHeaders.stream();
    }

    @Override
    public String toString() {
        convertIfNeeded();

        return azureCoreHeaders.toString();
    }

    private void convertIfNeeded() {
        if (converted) {
            return;
        }

        azureCoreHeaders = fromOkHttpHeaders(okhttpHeaders);
        converted = true;
    }
}
