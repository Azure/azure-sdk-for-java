// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.netty.handler.codec.DateFormatter;
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

import static io.clientcore.http.netty4.implementation.Netty4Utility.fromPossibleAsciiString;

/**
 * Implementation of Netty's {@link HttpHeaders} wrapping an instance of ClientCore's
 * {@link io.clientcore.core.http.models.HttpHeader}, eliminating the need to convert between the two HTTP header
 * holders.
 * <p>
 * This only implements the {@code HTTP/1.1} headers in Netty. If {@code HTTP/2} is used, these headers will be
 * converted to Netty's {@link io.netty.handler.codec.http2.Http2Headers}, a future optimization if we begin seeing
 * more usage of {@code HTTP/2} would be having a similar wrapper for the {@code HTTP/2} headers.
 */
public final class WrappedHttp11Headers extends HttpHeaders {
    private static final ClientLogger LOGGER = new ClientLogger(WrappedHttp11Headers.class);
    private io.clientcore.core.http.models.HttpHeaders coreHeaders;

    /**
     * Creates a new instance of {@link WrappedHttp11Headers} wrapping the provided {@code coreHeaders}.
     *
     * @param coreHeaders The ClientCore {@link io.clientcore.core.http.models.HttpHeaders} to wrap providing
     * integration with Netty {@link HttpHeaders}.
     * @throws NullPointerException If {@code coreHeaders} is null.
     */
    public WrappedHttp11Headers(io.clientcore.core.http.models.HttpHeaders coreHeaders) {
        this.coreHeaders = Objects.requireNonNull(coreHeaders, "'coreHeaders' cannot be null.");
    }

    /**
     * Get the underlying {@link io.clientcore.core.http.models.HttpHeaders} instance.
     *
     * @return The underlying {@link io.clientcore.core.http.models.HttpHeaders} instance.
     */
    public io.clientcore.core.http.models.HttpHeaders getCoreHeaders() {
        return coreHeaders;
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
            throw LOGGER.throwableAtError()
                .addKeyValue("headerValue", value)
                .log("header can't be parsed into a Date.", IllegalStateException::new);
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
            .flatMap(header -> header.getValues()
                .stream()
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

    @SuppressWarnings("deprecation")
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
        return coreHeaders.stream().map(header -> header.getName().getCaseSensitiveName()).collect(Collectors.toSet());
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
        if (headers instanceof WrappedHttp11Headers) {
            coreHeaders.addAll(((WrappedHttp11Headers) headers).coreHeaders);
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
        if (headers instanceof WrappedHttp11Headers) {
            coreHeaders = new io.clientcore.core.http.models.HttpHeaders(((WrappedHttp11Headers) headers).coreHeaders);
        } else {
            super.set(headers);
        }

        return this;
    }

    @Override
    public HttpHeaders setAll(HttpHeaders headers) {
        if (headers instanceof WrappedHttp11Headers) {
            coreHeaders.setAll(((WrappedHttp11Headers) headers).coreHeaders);
        } else {
            super.setAll(headers);
        }

        return this;
    }

    @Override
    public HttpHeaders copy() {
        return new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders(coreHeaders));
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
}
