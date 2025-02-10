// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * This class wraps a Netty HttpHeaders instance and provides an azure-core HttpHeaders view onto it.
 * <p>
 * This avoids the need to copy the Netty HttpHeaders into an azure-core HttpHeaders instance.
 * <p>
 * Whilst it is not necessary to support mutability (as these headers are the result of a Netty response), we do so any
 * case, given the additional implementation cost is minimal.
 */
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

    /**
     * Creates a new instance of NettyToAzureCoreHttpHeadersWrapper.
     *
     * @param nettyHeaders The Netty HttpHeaders to wrap.
     */
    public NettyToAzureCoreHttpHeadersWrapper(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        this.nettyHeaders = nettyHeaders;
    }

    @Override
    public int getSize() {
        return nettyHeaders.size();
    }

    @Override
    @Deprecated
    public HttpHeaders add(String name, String value) {
        nettyHeaders.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(HttpHeaderName name, String value) {
        return add(name.getCaseSensitiveName(), value);
    }

    @Override
    @Deprecated
    public HttpHeaders set(String name, String value) {
        nettyHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, String value) {
        return set(name.getCaseSensitiveName(), value);
    }

    @Override
    @Deprecated
    public HttpHeaders set(String name, List<String> values) {
        nettyHeaders.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, List<String> values) {
        return set(name.getCaseSensitiveName(), values);
    }

    @Override
    public HttpHeaders setAll(Map<String, List<String>> headers) {
        headers.forEach(this::set);
        return this;
    }

    @Override
    public HttpHeaders setAllHttpHeaders(HttpHeaders headers) {
        headers.forEach(header -> set(header.getName(), header.getValuesList()));
        return this;
    }

    @Override
    @Deprecated
    public HttpHeader get(String name) {
        // Be careful here: Netty's HttpHeaders 'get' method will return only the first value, which is obviously not
        // what we want to call! We call 'getAll' instead, but unfortunately there is a representation mismatch:
        // Netty HttpHeaders uses List<String>, whereas azure-core HttpHeaders joins it all into a comma-separated
        // String. Additionally, 'getAll' will return an empty list if there is no value(s) for the header.
        List<String> values = nettyHeaders.getAll(name);
        return (CoreUtils.isNullOrEmpty(values)) ? null : new NettyHttpHeader(this, name, values);
    }

    @Override
    public HttpHeader get(HttpHeaderName name) {
        return get(name.getCaseSensitiveName());
    }

    @Override
    @Deprecated
    public HttpHeader remove(String name) {
        HttpHeader header = get(name);
        if (header != null) {
            nettyHeaders.remove(name);
        }

        return header;
    }

    @Override
    public HttpHeader remove(HttpHeaderName name) {
        return remove(name.getCaseSensitiveName());
    }

    @Override
    @Deprecated
    public String getValue(String name) {
        List<String> values = nettyHeaders.getAll(name);
        return CoreUtils.isNullOrEmpty(values) ? null : CoreUtils.stringJoin(",", values);
    }

    @Override
    public String getValue(HttpHeaderName name) {
        return getValue(name.getCaseSensitiveName());
    }

    @Override
    @Deprecated
    public String[] getValues(String name) {
        List<String> values = nettyHeaders.getAll(name);
        return CoreUtils.isNullOrEmpty(values) ? null : values.toArray(new String[0]);
    }

    @Override
    public String[] getValues(HttpHeaderName name) {
        return getValues(name.getCaseSensitiveName());
    }

    @Override
    public Map<String, String> toMap() {
        if (abstractMap == null) {
            abstractMap = new DeferredCacheImmutableMap<>(LOGGER, new HashMap<>(), nettyHeaders,
                getAll -> CoreUtils.stringJoin(",", getAll));
        }
        return abstractMap;
    }

    Map<String, String[]> toMultiMap() {
        if (abstractMultiMap == null) {
            abstractMultiMap = new DeferredCacheImmutableMap<>(LOGGER, new HashMap<>(), nettyHeaders,
                getAll -> getAll.toArray(new String[0]));
        }
        return abstractMultiMap;
    }

    @Override
    public Iterator<HttpHeader> iterator() {
        return new NettyHeadersIterator(this);
    }

    @Override
    public Stream<HttpHeader> stream() {
        return nettyHeaders.names().stream().map(name -> new NettyHttpHeader(this, name, nettyHeaders.getAll(name)));
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

    static final class NettyHeadersIterator implements Iterator<HttpHeader> {
        private final NettyToAzureCoreHttpHeadersWrapper allHeaders;
        private final Iterator<String> headerNames;

        NettyHeadersIterator(NettyToAzureCoreHttpHeadersWrapper allHeaders) {
            this.allHeaders = allHeaders;
            this.headerNames = allHeaders.nettyHeaders.names().iterator();
        }

        @Override
        public boolean hasNext() {
            return headerNames.hasNext();
        }

        @Override
        public NettyHttpHeader next() {
            String headerName = headerNames.next();
            return new NettyHttpHeader(allHeaders, headerName, allHeaders.nettyHeaders.getAll(headerName));
        }
    }
}
