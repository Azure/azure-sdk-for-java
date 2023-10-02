// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation;

import com.typespec.core.http.HttpHeaderName;
import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.policy.ExponentialBackoff;
import com.typespec.core.http.policy.FixedDelay;
import com.typespec.core.http.policy.RetryOptions;
import com.typespec.core.http.policy.RetryStrategy;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.DateTimeRfc1123;
import com.typespec.core.util.FluxUtil;
import com.typespec.core.util.UrlBuilder;
import com.typespec.core.util.logging.ClientLogger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing implementation specific methods.
 */
public final class ImplUtils {
    private static final HttpHeaderName RETRY_AFTER_MS_HEADER = HttpHeaderName.fromString("retry-after-ms");
    private static final HttpHeaderName X_MS_RETRY_AFTER_MS_HEADER = HttpHeaderName.fromString("x-ms-retry-after-ms");

    // future improvement - make this configurable
    public static final int MAX_CACHE_SIZE = 10000;

    private static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    private static final ClientLogger LOGGER = new ClientLogger(ImplUtils.class);
    private static final byte ZERO = (byte) 0x00;
    private static final byte BB = (byte) 0xBB;
    private static final byte BF = (byte) 0xBF;
    private static final byte EF = (byte) 0xEF;
    private static final byte FE = (byte) 0xFE;
    private static final byte FF = (byte) 0xFF;
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(\\S+)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Attempts to extract a retry after duration from a given set of {@link HttpHeaders}.
     * <p>
     * This searches for the well-known retry after headers {@code Retry-After}, {@code retry-after-ms}, and
     * {@code x-ms-retry-after-ms}.
     * <p>
     * If no well-known headers are found null will be returned.
     *
     * @param headers The set of headers to search for a well-known retry after header.
     * @param nowSupplier A supplier for the current time used when {@code Retry-After} is using relative retry after
     * time.
     * @return The retry after duration if a well-known retry after header was found, otherwise null.
     */
    public static Duration getRetryAfterFromHeaders(HttpHeaders headers, Supplier<OffsetDateTime> nowSupplier) {
        // Found 'x-ms-retry-after-ms' header, use a Duration of milliseconds based on the value.
        Duration retryDelay = tryGetRetryDelay(headers, X_MS_RETRY_AFTER_MS_HEADER, ImplUtils::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'retry-after-ms' header, use a Duration of milliseconds based on the value.
        retryDelay = tryGetRetryDelay(headers, RETRY_AFTER_MS_HEADER, ImplUtils::tryGetDelayMillis);
        if (retryDelay != null) {
            return retryDelay;
        }

        // Found 'Retry-After' header. First, attempt to resolve it as a Duration of seconds. If that fails, then
        // attempt to resolve it as an HTTP date (RFC1123).
        retryDelay = tryGetRetryDelay(headers, HttpHeaderName.RETRY_AFTER,
            headerValue -> tryParseLongOrDateTime(headerValue, nowSupplier));

        // Either the retry delay will have been found or it'll be null, null indicates no retry after.
        return retryDelay;
    }

    private static Duration tryGetRetryDelay(HttpHeaders headers, HttpHeaderName headerName,
        Function<String, Duration> delayParser) {
        String headerValue = headers.getValue(headerName);

        return CoreUtils.isNullOrEmpty(headerValue) ? null : delayParser.apply(headerValue);
    }

    private static Duration tryGetDelayMillis(String value) {
        long delayMillis = tryParseLong(value);
        return (delayMillis >= 0) ? Duration.ofMillis(delayMillis) : null;
    }

    private static Duration tryParseLongOrDateTime(String value, Supplier<OffsetDateTime> nowSupplier) {
        long delaySeconds;
        try {
            OffsetDateTime retryAfter = new DateTimeRfc1123(value).getDateTime();

            delaySeconds = nowSupplier.get().until(retryAfter, ChronoUnit.SECONDS);
        } catch (DateTimeException ex) {
            delaySeconds = tryParseLong(value);
        }

        return (delaySeconds >= 0) ? Duration.ofSeconds(delaySeconds) : null;
    }

    private static long tryParseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    /**
     * Writes a {@link ByteBuffer} into an {@link OutputStream}.
     * <p>
     * This method provides writing optimization based on the type of {@link ByteBuffer} and {@link OutputStream}
     * passed. For example, if the {@link ByteBuffer} has a backing {@code byte[]} this method will access that directly
     * to write to the {@code stream} instead of buffering the contents of the {@link ByteBuffer} into a temporary
     * buffer.
     *
     * @param buffer The {@link ByteBuffer} to write into the {@code stream}.
     * @param stream The {@link OutputStream} where the {@code buffer} will be written.
     * @throws IOException If an I/O occurs while writing the {@code buffer} into the {@code stream}.
     */
    public static void writeByteBufferToStream(ByteBuffer buffer, OutputStream stream) throws IOException {
        // First check if the buffer has a backing byte[]. The backing byte[] can be accessed directly and written
        // without an additional buffering byte[].
        if (buffer.hasArray()) {
            // Write the byte[] from the current view position to the length remaining in the view.
            stream.write(buffer.array(), buffer.position(), buffer.remaining());

            // Update the position of the ByteBuffer to treat this the same as getting from the buffer.
            buffer.position(buffer.position() + buffer.remaining());
            return;
        }

        // Next begin checking for specific instances of OutputStream that may provide better writing options for
        // direct ByteBuffers.
        if (stream instanceof FileOutputStream) {
            FileOutputStream fileOutputStream = (FileOutputStream) stream;

            // Writing to the FileChannel directly may provide native optimizations for moving the OS managed memory
            // into the file.
            // Write will move both the OutputStream's and ByteBuffer's position so there is no need to perform
            // additional updates that are required when using the backing array.
            fileOutputStream.getChannel().write(buffer);
            return;
        }

        // All optimizations have been exhausted, fallback to buffering write.
        stream.write(FluxUtil.byteBufferToArray(buffer));
    }

    /**
     * Utility method for parsing a {@link URL} into a {@link UrlBuilder}.
     *
     * @param url The URL being parsed.
     * @param includeQuery Whether the query string should be excluded.
     * @return The UrlBuilder that represents the parsed URL.
     */
    public static UrlBuilder parseUrl(URL url, boolean includeQuery) {
        final UrlBuilder result = new UrlBuilder();

        if (url != null) {
            final String protocol = url.getProtocol();
            if (protocol != null && !protocol.isEmpty()) {
                result.setScheme(protocol);
            }

            final String host = url.getHost();
            if (host != null && !host.isEmpty()) {
                result.setHost(host);
            }

            final int port = url.getPort();
            if (port != -1) {
                result.setPort(port);
            }

            final String path = url.getPath();
            if (path != null && !path.isEmpty()) {
                result.setPath(path);
            }

            final String query = url.getQuery();
            if (query != null && !query.isEmpty() && includeQuery) {
                result.setQuery(query);
            }
        }

        return result;
    }

    public static final class QueryParameterIterator implements Iterator<Map.Entry<String, String>> {
        private final String queryParameters;
        private final int queryParametersLength;

        private boolean done = false;
        private int position;

        public QueryParameterIterator(String queryParameters) {
            this.queryParameters = queryParameters;
            this.queryParametersLength = queryParameters.length();

            // If the URL query begins with '?' the first possible start of a query parameter key is the
            // second character in the query.
            position = (queryParameters.startsWith("?")) ? 1 : 0;
        }

        @Override
        public boolean hasNext() {
            return !done;
        }

        @Override
        public Map.Entry<String, String> next() {
            if (done) {
                throw new NoSuchElementException();
            }

            int nextPosition = position;
            char c;
            while (nextPosition < queryParametersLength) {
                // Next position can either be '=' or '&' as a query parameter may not have a '=', ex 'key&key2=value'.
                c = queryParameters.charAt(nextPosition);
                if (c == '=') {
                    break;
                } else if (c == '&') {
                    String key = queryParameters.substring(position, nextPosition);

                    // Position is set to nextPosition + 1 to skip over the '&'
                    position = nextPosition + 1;

                    return new AbstractMap.SimpleImmutableEntry<>(key, "");
                }

                nextPosition++;
            }

            if (nextPosition == queryParametersLength) {
                // Query parameters completed.
                done = true;
                return new AbstractMap.SimpleImmutableEntry<>(queryParameters.substring(position), "");
            }

            String key = queryParameters.substring(position, nextPosition);

            // Position is set to nextPosition + 1 to skip over the '='
            position = nextPosition + 1;

            nextPosition = queryParameters.indexOf('&', position);

            String value = null;
            if (nextPosition == -1) {
                // This was the last key-value pair in the query parameters 'https://example.com?param=done'
                done = true;
                value = queryParameters.substring(position);
            } else {
                value = queryParameters.substring(position, nextPosition);
                // Position is set to nextPosition + 1 to skip over the '&'
                position = nextPosition + 1;
            }

            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }
    }

    /**
     * Attempts to convert a byte stream into the properly encoded String.
     * <p>
     * This utility method will attempt to find the encoding for the String in this order.
     * <ol>
     *     <li>Find the byte order mark in the byte array.</li>
     *     <li>Find the charset in the {@code contentType} header.</li>
     *     <li>Default to {@code UTF-8}.</li>
     * </ol>
     *
     * @param bytes The byte array.
     * @param offset The starting offset in the byte array.
     * @param count The number of bytes to process in the byte array.
     * @param contentType The {@code Content-Type} header value.
     * @return A string representation of the byte encoded to the found encoding, or null if {@code bytes} is null.
     */
    public static String bomAwareToString(byte[] bytes, int offset, int count, String contentType) {
        if (bytes == null) {
            return null;
        }

        if (count >= 3 && bytes[offset] == EF && bytes[offset + 1] == BB && bytes[offset + 2] == BF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        } else if (count >= 4 && bytes[offset] == ZERO && bytes[offset + 1] == ZERO
            && bytes[offset + 2] == FE && bytes[offset + 3] == FF) {
            return new String(bytes, 4, bytes.length - 4, UTF_32BE);
        } else if (count >= 4 && bytes[offset] == FF && bytes[offset + 1] == FE
            && bytes[offset + 2] == ZERO && bytes[offset + 3] == ZERO) {
            return new String(bytes, 4, bytes.length - 4, UTF_32LE);
        } else if (count >= 2 && bytes[offset] == FE && bytes[offset + 1] == FF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        } else if (count >= 2 && bytes[offset] == FF && bytes[offset + 1] == FE) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16LE);
        } else {
            /*
             * Attempt to retrieve the default charset from the 'Content-Encoding' header, if the value isn't
             * present or invalid fallback to 'UTF-8' for the default charset.
             */
            if (!CoreUtils.isNullOrEmpty(contentType)) {
                try {
                    Matcher charsetMatcher = CHARSET_PATTERN.matcher(contentType);
                    if (charsetMatcher.find()) {
                        return new String(bytes, offset, count, Charset.forName(charsetMatcher.group(1)));
                    } else {
                        return new String(bytes, offset, count, StandardCharsets.UTF_8);
                    }
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                    return new String(bytes, offset, count, StandardCharsets.UTF_8);
                }
            } else {
                return new String(bytes, offset, count, StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Creates a new {@link URL} from the given {@code urlString}.
     * <p>
     * This is a temporary method that will be removed once all usages of {@link URL#URL(String)} are migrated to
     * {@link URI}-based methods given the deprecation of the URL methods in Java 20.
     *
     * @param urlString The string to convert to a {@link URL}.
     * @return The {@link URL} representing the {@code urlString}.
     * @throws MalformedURLException If the {@code urlString} isn't a valid {@link URL}.
     */
    @SuppressWarnings("deprecation")
    public static URL createUrl(String urlString) throws MalformedURLException {
        return new URL(urlString);
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> getClassByName(String className) {
        Objects.requireNonNull("'className' cannot be null");
        try {
            return (Class<? extends T>) Class.forName(className, false, ImplUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            String message = String.format("Class `%s` is not found on the classpath.", className);
            throw LOGGER.logExceptionAsError(new RuntimeException(message, e));
        }
    }

    /**
     * Converts the {@link RetryOptions} into a {@link RetryStrategy} so it can be more easily consumed.
     *
     * @param retryOptions The retry options.
     * @return The retry strategy based on the retry options.
     */
    public static RetryStrategy getRetryStrategyFromOptions(RetryOptions retryOptions) {
        Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");

        if (retryOptions.getExponentialBackoffOptions() != null) {
            return new ExponentialBackoff(retryOptions.getExponentialBackoffOptions());
        } else if (retryOptions.getFixedDelayOptions() != null) {
            return new FixedDelay(retryOptions.getFixedDelayOptions());
        } else {
            // This should never happen.
            throw new IllegalArgumentException("'retryOptions' didn't define any retry strategy options");
        }
    }

    private ImplUtils() {
    }
}
