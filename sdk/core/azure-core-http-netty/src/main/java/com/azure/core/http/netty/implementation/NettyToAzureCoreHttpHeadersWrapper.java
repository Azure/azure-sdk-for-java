// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// This class wraps a Netty HttpHeaders instance and provides an azure-core HttpHeaders view onto it.
// This avoids the need to copy the Netty HttpHeaders into an azure-core HttpHeaders instance.
// Whilst it is not necessary to support mutability (as these headers are the result of a Netty response), we do so in
// any case, given the additional implementation cost is minimal.
public class NettyToAzureCoreHttpHeadersWrapper extends HttpHeaders {
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    private HttpHeaders azureCoreHeaders;
    private boolean converted = false;

    public NettyToAzureCoreHttpHeadersWrapper(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        this.nettyHeaders = nettyHeaders;
    }

    @Override
    public int getSize() {
        convertIfNeeded();

        return azureCoreHeaders.getSize();
    }

    @Override
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

        azureCoreHeaders = new HttpHeaders((int) (nettyHeaders.size() / 0.75F));
        // Use iteratorCharSequence as iterator has overhead converting to Map.Entry<String, String> in the common
        // case and this can be handled here instead.
        Iterator<Map.Entry<CharSequence, CharSequence>> nettyHeadersIterator = nettyHeaders.iteratorCharSequence();
        while (nettyHeadersIterator.hasNext()) {
            Map.Entry<CharSequence, CharSequence> next = nettyHeadersIterator.next();
            // Value may be null and that needs to be guarded but key should never be null.
            CharSequence value = next.getValue();
            azureCoreHeaders.add(next.getKey().toString(), (value == null) ? null : value.toString());
        }

        converted = true;
    }
}
