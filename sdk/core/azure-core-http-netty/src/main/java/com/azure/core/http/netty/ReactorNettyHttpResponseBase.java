// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.IterableStream;
import reactor.netty.http.client.HttpClientResponse;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

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

        @Override
        public int getSize() {
            return nettyHeaders.size();
        }

        @Override
        public HttpHeaders put(String name, String value) {
            nettyHeaders.add(name, value);
            return this;
        }

        @Override
        public HttpHeader get(String name) {
            if (nettyHeaders.contains(name)) {
                return new NettyHttpHeader(this, name, nettyHeaders.get(name));
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
            return new IterableStream<>(nettyHeaders).stream().map(e -> new NettyHttpHeader(this, e));
        }
    }

    static class NettyHttpHeader extends HttpHeader {
        private final HttpHeaders allHeaders;

        NettyHttpHeader(HttpHeaders allHeaders, String name, String value) {
            super(name, value);
            this.allHeaders = allHeaders;
        }

        NettyHttpHeader(HttpHeaders allHeaders, Map.Entry<String, String> entry) {
            this(allHeaders, entry.getKey(), entry.getValue());
        }

        @Override
        public void addValue(String value) {
            allHeaders.put(getName(), value);
        }
    }
}
