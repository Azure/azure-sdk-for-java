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
import java.util.Locale;
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

    static class NettyHttpHeaders extends HttpHeaders {
        private final io.netty.handler.codec.http.HttpHeaders nettyHeaders;
        private Map<String, String> map;

        NettyHttpHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
            this.nettyHeaders = nettyHeaders;
        }

        private String formatKey(final String key) {
            return key == null ? null : key.toLowerCase(Locale.ROOT);
        }

        @Override
        public int getSize() {
            return nettyHeaders.size();
        }

        /* This replaces any current value with the given name */
        @Override
        public HttpHeaders put(String name, String value) {
            if (name == null) {
                return this;
            }

            name = formatKey(name);
            if (value == null) {
                remove(name);
            } else {
                nettyHeaders.set(name, value);
            }
            return this;
        }

        HttpHeaders add(String name, String value) {
            if (name == null) {
                return this;
            }

            name = formatKey(name);
            if (value == null) {
                remove(name);
            } else {
                nettyHeaders.add(formatKey(name), value);
            }
            return this;
        }

        @Override
        public HttpHeader get(String name) {
            name = formatKey(name);
            if (nettyHeaders.contains(name)) {
                return new NettyHttpHeader(this, name, nettyHeaders.get(name));
            }
            return null;
        }

        @Override
        public HttpHeader remove(String name) {
            name = formatKey(name);
            HttpHeader header = get(name);
            nettyHeaders.remove(name);
            return header;
        }

        @Override
        public String getValue(String name) {
            return nettyHeaders.get(formatKey(name));
        }

        @Override
        public String[] getValues(String name) {
            return nettyHeaders.getAll(formatKey(name)).toArray(new String[] { });
        }

        @Override
        public Map<String, String> toMap() {
            if (map == null) {
                map = new AbstractMap<String, String>() {
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
            return map;
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

        @Override
        public void addValue(String value) {
            allHeaders.add(getName(), value);
        }
    }
}
