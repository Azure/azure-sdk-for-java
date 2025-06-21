// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.reactor.netty.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class wraps a Netty HttpHeaders instance and provides an azure-core HttpHeaders view onto it.
 * <p>
 * This avoids the need to copy the Netty HttpHeaders into an azure-core HttpHeaders instance.
 * <p>
 * Whilst it is not necessary to support mutability (as these headers are the result of a Netty response), we do so any
 * case, given the additional implementation cost is minimal.
 */
public class ReactorNettyToClientCoreHttpHeadersWrapper extends HttpHeaders {
    // This wrapper is frequently created, so we are OK with creating a single shared logger instance here,
    // to lessen the cost of this type.
    private static final ClientLogger LOGGER = new ClientLogger(ReactorNettyToClientCoreHttpHeadersWrapper.class);

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
    public ReactorNettyToClientCoreHttpHeadersWrapper(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        this.nettyHeaders = nettyHeaders;
    }

    @Override
    public int getSize() {
        return nettyHeaders.size();
    }

    @Deprecated
    public HttpHeaders add(String name, String value) {
        nettyHeaders.add(name, value);
        return this;
    }

    @Override
    public HttpHeaders add(HttpHeaderName name, String value) {
        return add(name.getCaseSensitiveName(), value);
    }

    @Deprecated
    public HttpHeaders set(String name, String value) {
        nettyHeaders.set(name, value);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, String value) {
        return set(name.getCaseSensitiveName(), value);
    }

    @Deprecated
    public HttpHeaders set(String name, List<String> values) {
        nettyHeaders.set(name, values);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaderName name, List<String> values) {
        return set(name.getCaseSensitiveName(), values);
    }

    public HttpHeaders setAll(Map<String, List<String>> headers) {
        headers.forEach(this::set);
        return this;
    }

    public HttpHeaders setAllHttpHeaders(HttpHeaders headers) {
        headers.stream().collect(Collectors.toList()).forEach(header -> set(header.getName(), header.getValues()));
        return this;
    }

    @Deprecated
    public HttpHeader get(String name) {
        // Be careful here: Netty's HttpHeaders 'get' method will return only the first value, which is obviously not
        // what we want to call! We call 'getAll' instead, but unfortunately there is a representation mismatch:
        // Netty HttpHeaders uses List<String>, whereas azure-core HttpHeaders joins it all into a comma-separated
        // String. Additionally, 'getAll' will return an empty list if there is no value(s) for the header.
        List<String> values = nettyHeaders.getAll(name);
        return (values == null || values.size() == 0) ? null : new NettyHttpHeader(this, name, values);
    }

    @Override
    public HttpHeader get(HttpHeaderName name) {
        return get(name.getCaseSensitiveName());
    }

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

    @Deprecated
    public String getValue(String name) {
        List<String> values = nettyHeaders.getAll(name);
        return (values == null || values.size() == 0) ? null : String.join(",", values);
    }

    @Override
    public String getValue(HttpHeaderName name) {
        return getValue(name.getCaseSensitiveName());
    }

    @Deprecated
    public List<String> getValues(String name) {
        return nettyHeaders.getAll(name);
    }

    public static String stringJoin(String delimiter, List<String> values) {
        Objects.requireNonNull(delimiter, "'delimiter' cannot be null.");
        Objects.requireNonNull(values, "'values' cannot be null.");

        int count = values.size();
        switch (count) {
            case 0:
                return "";

            case 1:
                return values.get(0);

            case 2:
                return values.get(0) + delimiter + values.get(1);

            case 3:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2);

            case 4:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3);

            case 5:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4);

            case 6:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4) + delimiter + values.get(5);

            case 7:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6);

            case 8:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6) + delimiter
                    + values.get(7);

            case 9:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6) + delimiter
                    + values.get(7) + delimiter + values.get(8);

            case 10:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter + values.get(3)
                    + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6) + delimiter
                    + values.get(7) + delimiter + values.get(8) + delimiter + values.get(9);

            default:
                return String.join(delimiter, values);
        }
    }

    @Override
    public List<String> getValues(HttpHeaderName name) {
        return getValues(name.getCaseSensitiveName());
    }


    @Override
    public Stream<HttpHeader> stream() {
        return nettyHeaders.names().stream().map(name -> new NettyHttpHeader(this, name, nettyHeaders.getAll(name)));
    }

    static class NettyHttpHeader extends HttpHeader {
        private final ReactorNettyToClientCoreHttpHeadersWrapper allHeaders;

        NettyHttpHeader(ReactorNettyToClientCoreHttpHeadersWrapper allHeaders, String name, String value) {
            super(HttpHeaderName.fromString(name), value);
            this.allHeaders = allHeaders;
        }

        NettyHttpHeader(ReactorNettyToClientCoreHttpHeadersWrapper allHeaders, String name, List<String> values) {
            super(HttpHeaderName.fromString(name), values);
            this.allHeaders = allHeaders;
        }

        @Override
        public void addValue(String value) {
            super.addValue(value);
            allHeaders.add(getName(), value);
        }
    }

    static final class NettyHeadersIterator implements Iterator<HttpHeader> {
        private final ReactorNettyToClientCoreHttpHeadersWrapper allHeaders;
        private final Iterator<String> headerNames;

        NettyHeadersIterator(ReactorNettyToClientCoreHttpHeadersWrapper allHeaders) {
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
