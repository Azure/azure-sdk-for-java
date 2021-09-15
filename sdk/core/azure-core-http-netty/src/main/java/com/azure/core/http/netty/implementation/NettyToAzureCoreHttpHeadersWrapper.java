// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

// This class wraps a Netty HttpHeaders instance and provides an azure-core HttpHeaders view onto it.
// This avoids the need to copy the Netty HttpHeaders into an azure-core HttpHeaders instance.
// Whilst it is not necessary to support mutability (as these headers are the result of a Netty response), we do so in
// any case, given the additional implementation cost is minimal.
public class NettyToAzureCoreHttpHeadersWrapper extends HttpHeaders {
    // This wrapper is frequently created, so we are OK with creating a single shared logger instance here,
    // to lessen the cost of this type.
    private static final ClientLogger LOGGER = new ClientLogger(NettyToAzureCoreHttpHeadersWrapper.class);

    // The Netty HttpHeaders we are wrapping
    private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

    // this is an AbstractMap that we create to virtualize a view onto the Netty HttpHeaders type, for use in the
    // toMap API. We lazily instantiate it when toMap is called, and then reuse that for all future calls.
    private Map<String, String> abstractMap;

    // this is an AbstractMap that we create to virtualize a view onto the Netty HttpHeaders type, for use in the
    // toMultiMap API. We lazily instantiate it when toMap is called, and then reuse that for all future calls.
    private Map<String, String[]> abstractMultiMap;

    public NettyToAzureCoreHttpHeadersWrapper(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        this.nettyHeaders = nettyHeaders;
    }

    @Override
    public int getSize() {
        return nettyHeaders.size();
    }

    @Override
    public HttpHeaders set(String name, String value) {
        if (name == null) {
            return this;
        }

        if (value == null) {
            // our general contract in HttpHeaders is that a null value will result in any key with this name
            // being removed.
            remove(name);
        } else {
            nettyHeaders.set(name, value);
        }
        return this;
    }

    @Override
    public HttpHeaders set(String name, List<String> values) {
        if (name == null) {
            return this;
        }

        if (values == null) {
            // our general contract in HttpHeaders is that a null value will result in any key with this name
            // being removed.
            remove(name);
        } else {
            nettyHeaders.set(name, values);
        }
        return this;
    }

    public HttpHeaders add(String name, String value) {
        if (name == null) {
            return this;
        }

        if (value == null) {
            // our general contract in HttpHeaders is that a null value will result in any key with this name
            // being removed.
            remove(name);
        } else {
            nettyHeaders.add(name, value);
        }
        return this;
    }

    @Override
    public HttpHeader get(String name) {
        if (nettyHeaders.contains(name)) {
            // Be careful here: Netty's HttpHeaders 'get' method will return only the first value,
            // which is obviously not what we want to call!
            // We call 'getAll' instead, but unfortunately there is a representation mismatch:
            // Netty HttpHeaders uses List<String>, whereas azure-core HttpHeaders joins it all into a
            // comma-separated String.
            return new NettyHttpHeader(this, name, nettyHeaders.getAll(name));
        }
        return null;
    }

    @Override
    public HttpHeader remove(String name) {
        HttpHeader header = get(name);
        nettyHeaders.remove(name);
        return header;
    }

    @Override
    public String getValue(String name) {
        final HttpHeader header = get(name);
        return (header == null) ? null : header.getValue();
    }

    @Override
    public String[] getValues(String name) {
        final HttpHeader header = get(name);
        return (header == null) ? null : header.getValues();
    }

    @Override
    public Map<String, String> toMap() {
        if (abstractMap == null) {
            abstractMap = new DeferredCacheImmutableMap<>(LOGGER, new HashMap<>(), nettyHeaders,
                getAll -> String.join(",", getAll));
        }
        return abstractMap;
    }

    @Override
    public Map<String, String[]> toMultiMap() {
        if (abstractMultiMap == null) {
            abstractMultiMap = new DeferredCacheImmutableMap<>(LOGGER, new HashMap<>(), nettyHeaders,
                getAll -> getAll.toArray(new String[0]));
        }
        return abstractMultiMap;
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return stream().iterator();
    }

    @Override
    public Stream<HttpHeader> stream() {
        return nettyHeaders.names().stream()
            .map(name -> new NettyHttpHeader(this, name, nettyHeaders.getAll(name)));
    }

    static class NettyHttpHeader extends HttpHeader {
        private final NettyToAzureCoreHttpHeadersWrapper allHeaders;

        NettyHttpHeader(NettyToAzureCoreHttpHeadersWrapper allHeaders, String name, String value) {
            super(name, value);
            this.allHeaders = allHeaders;
        }

        NettyHttpHeader(NettyToAzureCoreHttpHeadersWrapper allHeaders, String name, List<String> values) {
            super(name, values);
            this.allHeaders = allHeaders;
        }

        @Override
        public void addValue(String value) {
            super.addValue(value);
            allHeaders.add(getName(), value);
        }
    }
}
