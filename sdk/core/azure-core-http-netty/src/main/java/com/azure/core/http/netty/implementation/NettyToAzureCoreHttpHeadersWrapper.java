// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.util.logging.ClientLogger;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    // PACKAGE-PRIVATE
    // This is only used internally by the NettyHttpHeader class to append a value to an existing value, and to support
    // this actually occurring in the underlying Netty HttpHeaders instance.
    HttpHeaders add(String name, String value) {
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
            abstractMap = new AbstractMap<String, String>() {
                private final Map<String, String> innerJoinMap = new HashMap<>();

                @Override
                public int size() {
                    return nettyHeaders.size();
                }

                @Override
                public boolean isEmpty() {
                    return nettyHeaders.isEmpty();
                }

                @Override
                public boolean containsKey(Object key) {
                    return nettyHeaders.contains((String) key);
                }

                @Override
                public boolean containsValue(Object value) {
                    throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
                }

                @Override
                public String get(final Object key) {
                    // Calling nettyHeaders.get(key) returns only the first value in the headers for the given key.
                    // If there are multiple values, the user will not get the result they expect.
                    // For now, this is resolved by joining the headers back into a String here, with the obvious
                    // performance implication, and therefore it is recommended that users steer away from calling
                    // httpHeaders.toMap().get(key), and instead be directed towards httpHeaders.get(key), as this
                    // avoids the need for unnecessary string.join operations.

                    // To alleviate some concerns with performance, we internally cache the joined string in the
                    // innerJoinMap, so multiple requests to this get method will not incur the cost of string
                    // concatenation.
                    return innerJoinMap.computeIfAbsent((String) key, _key ->
                        String.join(",", nettyHeaders.getAll(_key)));
                }

                @Override
                public String put(String key, String value) {
                    throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
                }

                @Override
                public String remove(Object key) {
                    throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
                }

                @Override
                public void putAll(Map<? extends String, ? extends String> m) {
                    throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
                }

                @Override
                public void clear() {
                    throw LOGGER.logExceptionAsWarning(new UnsupportedOperationException());
                }

                @Override
                public Set<Entry<String, String>> entrySet() {
                    return new AbstractSet<Entry<String, String>>() {
                        @Override
                        public Iterator<Entry<String, String>> iterator() {
                            return nettyHeaders.iteratorAsString();
                        }

                        @Override
                        public int size() {
                            return nettyHeaders.size();
                        }
                    };
                }
            };
        }
        return abstractMap;
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
