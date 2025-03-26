// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.netty.handler.codec.DateFormatter;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Implementation of Netty's {@link HttpHeaders} wrapping an instance of ClientCore's
 * {@link io.clientcore.core.http.models.HttpHeader}, eliminating the need to convert between the two HTTP header
 * holders.
 * <p>
 * This only implements the {@code HTTP/1.1} headers in Netty. If {@code HTTP/2} is used, these headers will be
 * converted to Netty's {@link io.netty.handler.codec.http2.Http2Headers}, a future optimization if we begin seeing
 * more usage of {@code HTTP/2} would be having a similar wrapper for the {@code HTTP/2} headers.
 */
public final class WrappedHttpHeaders extends HttpHeaders {
    private io.clientcore.core.http.models.HttpHeaders coreHeaders;

    /**
     * Creates a new instance of {@link WrappedHttpHeaders} wrapping the provided {@code coreHeaders}.
     *
     * @param coreHeaders The ClientCore {@link io.clientcore.core.http.models.HttpHeaders} to wrap providing
     * integration with Netty {@link HttpHeaders}.
     * @throws NullPointerException If {@code coreHeaders} is null.
     */
    public WrappedHttpHeaders(io.clientcore.core.http.models.HttpHeaders coreHeaders) {
        this.coreHeaders = Objects.requireNonNull(coreHeaders, "'coreHeaders' cannot be null.");
    }

    @Override
    public String get(String name) {
        // Per Javadoc on super type, if there are multiple header values for the same name this will return the first
        // one.
        List<String> values = getAll(name);
        return values.isEmpty() ? null : values.get(0);
    }

    @Override
    public String get(CharSequence name) {
        List<String> values = getAll(name);
        return values.isEmpty() ? null : values.get(0);
    }

    @Override
    public Integer getInt(CharSequence name) {
        String value = get(name);
        try {
            return (value == null) ? null : Integer.parseInt(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Override
    public int getInt(CharSequence name, int defaultValue) {
        Integer value = getInt(name);
        return (value == null) ? defaultValue : value;
    }

    @Override
    public Short getShort(CharSequence name) {
        String value = get(name);
        try {
            return (value == null) ? null : Short.parseShort(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    @Override
    public short getShort(CharSequence name, short defaultValue) {
        Short value = getShort(name);
        return (value == null) ? defaultValue : value;
    }

    @Override
    public Long getTimeMillis(CharSequence name) {
        String value = get(name);
        try {
            return (value == null) ? null : convertToTimeMillis(value);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static Long convertToTimeMillis(String value) {
        Date date = DateFormatter.parseHttpDate(value);
        if (date == null) {
            throw new IllegalStateException("header can't be parsed into a Date: " + value);
        } else {
            return date.getTime();
        }
    }

    @Override
    public long getTimeMillis(CharSequence name, long defaultValue) {
        Long value = getTimeMillis(name);
        return (value == null) ? defaultValue : value;
    }

    @Override
    public List<String> getAll(String name) {
        List<String> values = coreHeaders.getValues(HttpHeaderName.fromString(name));
        return (values == null) ? Collections.emptyList() : values;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        List<String> values = coreHeaders.getValues(fromPossibleAsciiString(name));
        return (values == null) ? Collections.emptyList() : values;
    }

    @Override
    public List<Map.Entry<String, String>> entries() {
        return coreHeaders.stream()
            .flatMap(header -> header.getValues().stream()
                .map(value -> new AbstractMap.SimpleImmutableEntry<>(header.getName().getCaseSensitiveName(), value)))
            .collect(Collectors.toList());
    }

    @Override
    public boolean contains(String name) {
        return coreHeaders.get(HttpHeaderName.fromString(name)) != null;
    }

    @Override
    public boolean contains(CharSequence name) {
        return coreHeaders.get(fromPossibleAsciiString(name)) != null;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        return new HeadersIterator<>(coreHeaders.stream().iterator(), AbstractMap.SimpleImmutableEntry::new);
    }

    @Override
    public Iterator<Map.Entry<CharSequence, CharSequence>> iteratorCharSequence() {
        return new HeadersIterator<>(coreHeaders.stream().iterator(), AbstractMap.SimpleImmutableEntry::new);
    }

    private static final class HeadersIterator<T> implements Iterator<Map.Entry<T, T>> {
        private final Iterator<HttpHeader> headersIterator;
        private final BiFunction<String, String, Map.Entry<T, T>> mapperFunction;

        private boolean done = false;
        private String currentHeaderName;
        private Iterator<String> valuesIterator;

        private HeadersIterator(Iterator<HttpHeader> headersIterator,
            BiFunction<String, String, Map.Entry<T, T>> mapperFunction) {
            this.headersIterator = headersIterator;
            this.mapperFunction = mapperFunction;
        }

        @Override
        public boolean hasNext() {
            if (done) {
                // Once we're done we can never have additional values.
                return false;
            }

            // Iterator hasn't been initialized yet. Attempt to initialize.
            // currentHeader will only be null if hasNext has never been called.
            if (valuesIterator == null) {
                if (headersIterator.hasNext()) {
                    HttpHeader nextHeader = headersIterator.next();
                    currentHeaderName = nextHeader.getName().getCaseSensitiveName();
                    valuesIterator = nextHeader.getValues().iterator();
                } else {
                    // HttpHeader iterator had no values.
                    done = true;
                    return false;
                }
            }

            // Loop until values iterator hasNext or the HttpHeader iterator terminates.
            // This is done in a loop as HttpHeader.getValues() may return an empty list while there are more
            // HttpHeaders to iterator over. Meaning if this was only done once it would be possible to return false
            // while the real return is true. For example, a HttpHeader iterator as follows:
            //   header1 -> emptyList
            //   header2 -> emptyList
            //   header3 -> value1,value2
            // If we were on header1, valuesIterator.hasNext() would be false. If we only advanced one header we then
            // be on header2 and its valuesIterator.hasNext() would also be false, and we'd return false when in
            // actuality if we looped we'd reach header3 which would return true.
            while (!valuesIterator.hasNext()) {
                if (headersIterator.hasNext()) {
                    HttpHeader nextHeader = headersIterator.next();
                    currentHeaderName = nextHeader.getName().getCaseSensitiveName();
                    valuesIterator = nextHeader.getValues().iterator();
                } else {
                    // HttpHeader iterator had no additional values.
                    done = true;
                    return false;
                }
            }

            return true;
        }

        @Override
        public Map.Entry<T, T> next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return mapperFunction.apply(currentHeaderName, valuesIterator.next());
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return coreHeaders.getSize();
    }

    @Override
    public Set<String> names() {
        return coreHeaders.stream()
            .map(header -> header.getName().getCaseSensitiveName())
            .collect(Collectors.toSet());
    }

    @Override
    public HttpHeaders add(String name, Object value) {
        coreHeaders.add(HttpHeaderName.fromString(name), value.toString());
        return this;
    }

    @Override
    public HttpHeaders add(String name, Iterable<?> values) {
        List<String> stringValues = new ArrayList<>();
        for (Object value : values) {
            stringValues.add(value.toString());
        }
        coreHeaders.add(new HttpHeader(HttpHeaderName.fromString(name), stringValues));
        return this;
    }

    @Override
    public HttpHeaders addInt(CharSequence name, int value) {
        coreHeaders.add(fromPossibleAsciiString(name.toString()), String.valueOf(value));
        return this;
    }

    @Override
    public HttpHeaders addShort(CharSequence name, short value) {
        coreHeaders.add(fromPossibleAsciiString(name.toString()), String.valueOf(value));
        return this;
    }

    @Override
    public HttpHeaders add(CharSequence name, Object value) {
        coreHeaders.add(fromPossibleAsciiString(name), value.toString());
        return this;
    }

    @Override
    public HttpHeaders add(CharSequence name, Iterable<?> values) {
        List<String> stringValues = new ArrayList<>();
        for (Object value : values) {
            stringValues.add(value.toString());
        }
        coreHeaders.add(new HttpHeader(fromPossibleAsciiString(name), stringValues));
        return this;
    }

    @Override
    public HttpHeaders add(HttpHeaders headers) {
        if (headers instanceof WrappedHttpHeaders) {
            coreHeaders.addAll(((WrappedHttpHeaders) headers).coreHeaders);
            return this;
        } else {
            return super.add(headers);
        }
    }

    @Override
    public HttpHeaders set(String name, Object value) {
        coreHeaders.set(HttpHeaderName.fromString(name), value.toString());
        return this;
    }

    @Override
    public HttpHeaders set(String name, Iterable<?> values) {
        List<String> stringValues = new ArrayList<>();
        for (Object value : values) {
            stringValues.add(value.toString());
        }
        coreHeaders.set(HttpHeaderName.fromString(name), stringValues);
        return this;
    }

    @Override
    public HttpHeaders setInt(CharSequence name, int value) {
        coreHeaders.set(fromPossibleAsciiString(name.toString()), String.valueOf(value));
        return this;
    }

    @Override
    public HttpHeaders setShort(CharSequence name, short value) {
        coreHeaders.set(fromPossibleAsciiString(name.toString()), String.valueOf(value));
        return this;
    }

    @Override
    public HttpHeaders set(CharSequence name, Object value) {
        coreHeaders.set(fromPossibleAsciiString(name), value.toString());
        return this;
    }

    @Override
    public HttpHeaders set(CharSequence name, Iterable<?> values) {
        List<String> stringValues = new ArrayList<>();
        for (Object value : values) {
            stringValues.add(value.toString());
        }
        coreHeaders.set(fromPossibleAsciiString(name), stringValues);
        return this;
    }

    @Override
    public HttpHeaders set(HttpHeaders headers) {
        if (headers instanceof WrappedHttpHeaders) {
            coreHeaders = new io.clientcore.core.http.models.HttpHeaders(((WrappedHttpHeaders) headers).coreHeaders);
        } else {
            super.set(headers);
        }

        return this;
    }

    @Override
    public HttpHeaders setAll(HttpHeaders headers) {
        if (headers instanceof WrappedHttpHeaders) {
            coreHeaders.setAll(((WrappedHttpHeaders) headers).coreHeaders);
        } else {
            super.setAll(headers);
        }

        return this;
    }

    @Override
    public HttpHeaders copy() {
        return new WrappedHttpHeaders(new io.clientcore.core.http.models.HttpHeaders(coreHeaders));
    }

    @Override
    public HttpHeaders remove(String name) {
        coreHeaders.remove(HttpHeaderName.fromString(name));
        return this;
    }

    @Override
    public HttpHeaders remove(CharSequence name) {
       coreHeaders.remove(fromPossibleAsciiString(name));
       return this;
    }

    @Override
    public HttpHeaders clear() {
        coreHeaders = new io.clientcore.core.http.models.HttpHeaders();
        return this;
    }

    // Helper method that hot paths some well-known AsciiString HttpHeaderNames.
    @SuppressWarnings("deprecation")
    private static HttpHeaderName fromPossibleAsciiString(CharSequence asciiString) {
        if (HttpHeaderNames.ACCEPT == asciiString) {
            return HttpHeaderName.ACCEPT;
        } else if (HttpHeaderNames.ACCEPT_CHARSET == asciiString) {
            return HttpHeaderName.ACCEPT_CHARSET;
        } else if (HttpHeaderNames.ACCEPT_ENCODING == asciiString) {
            return HttpHeaderName.ACCEPT_ENCODING;
        } else if (HttpHeaderNames.ACCEPT_LANGUAGE == asciiString) {
            return HttpHeaderName.ACCEPT_LANGUAGE;
        } else if (HttpHeaderNames.ACCEPT_RANGES == asciiString) {
            return HttpHeaderName.ACCEPT_RANGES;
        } else if (HttpHeaderNames.ACCEPT_PATCH == asciiString) {
            return HttpHeaderName.ACCEPT_PATCH;
        } else if (HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS;
        } else if (HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_ALLOW_HEADERS;
        } else if (HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_ALLOW_METHODS;
        } else if (HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_ALLOW_ORIGIN;
        } else if (HttpHeaderNames.ACCESS_CONTROL_EXPOSE_HEADERS == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_EXPOSE_HEADERS;
        } else if (HttpHeaderNames.ACCESS_CONTROL_MAX_AGE == asciiString) {
            return HttpHeaderName.ACCESS_CONTROL_MAX_AGE;
        } else if (HttpHeaderNames.AGE == asciiString) {
            return HttpHeaderName.AGE;
        } else if (HttpHeaderNames.ALLOW == asciiString) {
            return HttpHeaderName.ALLOW;
        } else if (HttpHeaderNames.AUTHORIZATION == asciiString) {
            return HttpHeaderName.AUTHORIZATION;
        } else if (HttpHeaderNames.CACHE_CONTROL == asciiString) {
            return HttpHeaderName.CACHE_CONTROL;
        } else if (HttpHeaderNames.CONNECTION == asciiString) {
            return HttpHeaderName.CONNECTION;
        } else if (HttpHeaderNames.CONTENT_ENCODING == asciiString) {
            return HttpHeaderName.CONTENT_ENCODING;
        } else if (HttpHeaderNames.CONTENT_LANGUAGE == asciiString) {
            return HttpHeaderName.CONTENT_LANGUAGE;
        } else if (HttpHeaderNames.CONTENT_LENGTH == asciiString) {
            return HttpHeaderName.CONTENT_LENGTH;
        } else if (HttpHeaderNames.CONTENT_LOCATION == asciiString) {
            return HttpHeaderName.CONTENT_LOCATION;
        } else if (HttpHeaderNames.CONTENT_DISPOSITION == asciiString) {
            return HttpHeaderName.CONTENT_DISPOSITION;
        } else if (HttpHeaderNames.CONTENT_MD5 == asciiString) {
            return HttpHeaderName.CONTENT_MD5;
        } else if (HttpHeaderNames.CONTENT_RANGE == asciiString) {
            return HttpHeaderName.CONTENT_RANGE;
        } else if (HttpHeaderNames.CONTENT_TYPE == asciiString) {
            return HttpHeaderName.CONTENT_TYPE;
        } else if (HttpHeaderNames.COOKIE == asciiString) {
            return HttpHeaderName.COOKIE;
        } else if (HttpHeaderNames.DATE == asciiString) {
            return HttpHeaderName.DATE;
        } else if (HttpHeaderNames.ETAG == asciiString) {
            return HttpHeaderName.ETAG;
        } else if (HttpHeaderNames.EXPECT == asciiString) {
            return HttpHeaderName.EXPECT;
        } else if (HttpHeaderNames.EXPIRES == asciiString) {
            return HttpHeaderName.EXPIRES;
        } else if (HttpHeaderNames.FROM == asciiString) {
            return HttpHeaderName.FROM;
        } else if (HttpHeaderNames.HOST == asciiString) {
            return HttpHeaderName.HOST;
        } else if (HttpHeaderNames.IF_MATCH == asciiString) {
            return HttpHeaderName.IF_MATCH;
        } else if (HttpHeaderNames.IF_MODIFIED_SINCE == asciiString) {
            return HttpHeaderName.IF_MODIFIED_SINCE;
        } else if (HttpHeaderNames.IF_NONE_MATCH == asciiString) {
            return HttpHeaderName.IF_NONE_MATCH;
        } else if (HttpHeaderNames.IF_RANGE == asciiString) {
            return HttpHeaderName.IF_RANGE;
        } else if (HttpHeaderNames.IF_UNMODIFIED_SINCE == asciiString) {
            return HttpHeaderName.IF_UNMODIFIED_SINCE;
        } else if (HttpHeaderNames.KEEP_ALIVE == asciiString) {
            return HttpHeaderName.KEEP_ALIVE;
        } else if (HttpHeaderNames.LAST_MODIFIED == asciiString) {
            return HttpHeaderName.LAST_MODIFIED;
        } else if (HttpHeaderNames.LOCATION == asciiString) {
            return HttpHeaderName.LOCATION;
        } else if (HttpHeaderNames.MAX_FORWARDS == asciiString) {
            return HttpHeaderName.MAX_FORWARDS;
        } else if (HttpHeaderNames.ORIGIN == asciiString) {
            return HttpHeaderName.ORIGIN;
        } else if (HttpHeaderNames.PRAGMA == asciiString) {
            return HttpHeaderName.PRAGMA;
        } else if (HttpHeaderNames.PROXY_AUTHENTICATE == asciiString) {
            return HttpHeaderName.PROXY_AUTHENTICATE;
        } else if (HttpHeaderNames.PROXY_AUTHORIZATION == asciiString) {
            return HttpHeaderName.PROXY_AUTHORIZATION;
        } else if (HttpHeaderNames.RANGE == asciiString) {
            return HttpHeaderName.RANGE;
        } else if (HttpHeaderNames.REFERER == asciiString) {
            return HttpHeaderName.REFERER;
        } else if (HttpHeaderNames.RETRY_AFTER == asciiString) {
            return HttpHeaderName.RETRY_AFTER;
        } else if (HttpHeaderNames.SERVER == asciiString) {
            return HttpHeaderName.SERVER;
        } else if (HttpHeaderNames.SET_COOKIE == asciiString) {
            return HttpHeaderName.SET_COOKIE;
        } else if (HttpHeaderNames.TE == asciiString) {
            return HttpHeaderName.TE;
        } else if (HttpHeaderNames.TRAILER == asciiString) {
            return HttpHeaderName.TRAILER;
        } else if (HttpHeaderNames.TRANSFER_ENCODING == asciiString) {
            return HttpHeaderName.TRANSFER_ENCODING;
        } else if (HttpHeaderNames.UPGRADE == asciiString) {
            return HttpHeaderName.UPGRADE;
        } else if (HttpHeaderNames.USER_AGENT == asciiString) {
            return HttpHeaderName.USER_AGENT;
        } else if (HttpHeaderNames.VARY == asciiString) {
            return HttpHeaderName.VARY;
        } else if (HttpHeaderNames.VIA == asciiString) {
            return HttpHeaderName.VIA;
        } else if (HttpHeaderNames.WARNING == asciiString) {
            return HttpHeaderName.WARNING;
        } else if (HttpHeaderNames.WWW_AUTHENTICATE == asciiString) {
            return HttpHeaderName.WWW_AUTHENTICATE;
        } else {
            return HttpHeaderName.fromString(asciiString.toString());
        }
    }
}
