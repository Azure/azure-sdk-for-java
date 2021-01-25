// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.netty.http.client.HttpClientResponse;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Base response class for Reactor Netty with implementations for response metadata.
 */
abstract class ReactorNettyHttpResponseBase extends HttpResponse {
    private final HttpClientResponse reactorNettyResponse;
    private NettyHttpHeaders headers;

    ReactorNettyHttpResponseBase(HttpClientResponse reactorNettyResponse, HttpRequest httpRequest) {
        super(httpRequest);
        this.reactorNettyResponse = reactorNettyResponse;
    }

    @Override
    public final int getStatusCode() {
        return reactorNettyResponse.status().code();
    }

    @Override
    public final String getHeaderValue(String name) {
        return reactorNettyResponse.responseHeaders().get(name);
    }

    @Override
    public final HttpHeaders getHeaders() {
        if (headers == null) {
            headers = new NettyHttpHeaders(reactorNettyResponse.responseHeaders());
        }
        return headers;
    }

    // This class wraps a Netty HttpHeaders instance and provides an azure-core HttpHeaders view onto it.
    // This avoids the need to copy the Netty HttpHeaders into an azure-core HttpHeaders instance.
    static class NettyHttpHeaders extends HttpHeaders {
        // The Netty HttpHeaders we are wrapping
        private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;

        // this is an AbstractMap that we create to virtualize a view onto the Netty HttpHeaders type.
        // We lazily instantiate it when toMap is called, and then use that for all future calls.
        private Map<String, String> abstractMap;

        NettyHttpHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
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
                remove(name);
            } else {
                nettyHeaders.set(name, values);
            }
            return this;
        }

        HttpHeaders add(String name, String value) {
            if (name == null) {
                return this;
            }

            if (value == null) {
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
            return nettyHeaders.get(name);
        }

        @Override
        public String[] getValues(String name) {
            return nettyHeaders.getAll(name).toArray(new String[] { });
        }

        @Override
        public Map<String, String> toMap() {
            if (abstractMap == null) {
                abstractMap = new AbstractMap<String, String>() {
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
                        return nettyHeaders.contains((String)key);
                    }

                    @Override
                    public boolean containsValue(Object value) {
                        return false;
                    }

                    @Override
                    public String get(final Object key) {
                        return nettyHeaders.get((String)key);
                    }

                    @Override
                    public String put(String key, String value) {
                        throw new UnsupportedOperationException("HttpHeaders are unmodifiable");
                    }

                    @Override
                    public String remove(Object key) {
                        throw new UnsupportedOperationException("HttpHeaders are unmodifiable");
                    }

                    @Override
                    public void putAll(Map<? extends String, ? extends String> m) {
                        throw new UnsupportedOperationException("HttpHeaders are unmodifiable");
                    }

                    @Override
                    public void clear() {
                        throw new UnsupportedOperationException("HttpHeaders are unmodifiable");
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
            return StreamSupport.stream(nettyHeaders.spliterator(), false)
                       .map(e -> new NettyHttpHeader(this, e));
        }
    }

    static class NettyHttpHeader extends HttpHeader {
        private final NettyHttpHeaders allHeaders;

        NettyHttpHeader(NettyHttpHeaders allHeaders, String name, String value) {
            super(name, value);
            this.allHeaders = allHeaders;
        }

        NettyHttpHeader(NettyHttpHeaders allHeaders, Map.Entry<String, String> entry) {
            this(allHeaders, entry.getKey(), entry.getValue());
        }

        NettyHttpHeader(NettyHttpHeaders allHeaders, String name, List<String> values) {
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
