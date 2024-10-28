// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.util;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.configuration.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
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
    private static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    private static final byte ZERO = (byte) 0x00;
    private static final byte BB = (byte) 0xBB;
    private static final byte BF = (byte) 0xBF;
    private static final byte EF = (byte) 0xEF;
    private static final byte FE = (byte) 0xFE;
    private static final byte FF = (byte) 0xFF;
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(\\S+)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Default sanitizer for a value, where it is simply replaced with "REDACTED".
     */
    public static final Function<String, String> DEFAULT_SANITIZER = value -> "REDACTED";

    private ImplUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Checks if the array is null or empty.
     *
     * @param array Array being checked for nullness or emptiness.
     *
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the collection is null or empty.
     *
     * @param collection Collection being checked for nullness or emptiness.
     *
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the map is null or empty.
     *
     * @param map Map being checked for nullness or emptiness.
     *
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the character sequence is null or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     *
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Optimized version of {@link String#join(CharSequence, Iterable)} when the {@code values} has a small set of
     * object.
     *
     * @param delimiter Delimiter between the values.
     * @param values The values to join.
     *
     * @return The {@code values} joined delimited by the {@code delimiter}.
     *
     * @throws NullPointerException If {@code delimiter} or {@code values} is null.
     */
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

        return isNullOrEmpty(headerValue) ? null : delayParser.apply(headerValue);
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
        stream.write(byteBufferToArray(buffer));
    }

    /**
     * Gets the content of the provided ByteBuffer as a byte array. This method will create a new byte array even if the
     * ByteBuffer can have optionally backing array.
     *
     * @param byteBuffer the byte buffer
     * @return the byte array
     */
    public static byte[] byteBufferToArray(ByteBuffer byteBuffer) {
        int length = byteBuffer.remaining();
        byte[] byteArray = new byte[length];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    /**
     * Utility method for parsing a {@link URI} into a {@link UriBuilder}.
     *
     * @param uri The URI being parsed.
     * @param includeQuery Whether the query string should be excluded.
     * @return The UriBuilder that represents the parsed URI.
     */
    public static UriBuilder parseUri(URI uri, boolean includeQuery) {
        final UriBuilder result = new UriBuilder();

        if (uri != null) {
            final String scheme = uri.getScheme();
            if (scheme != null && !scheme.isEmpty()) {
                result.setScheme(scheme);
            }

            final String host = uri.getHost();
            if (host != null && !host.isEmpty()) {
                result.setHost(host);
            }

            final int port = uri.getPort();
            if (port != -1) {
                result.setPort(port);
            }

            final String path = uri.getPath();
            if (path != null && !path.isEmpty()) {
                result.setPath(path);
            }

            final String query = uri.getQuery();
            if (query != null && !query.isEmpty() && includeQuery) {
                result.setQuery(query);
            }
        }

        return result;
    }

    public static final class QueryParameterIterable implements Iterable<Map.Entry<String, String>> {
        private final String queryParameters;

        public QueryParameterIterable(String queryParameters) {
            this.queryParameters = queryParameters;
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return new QueryParameterIterator(queryParameters);
        }
    }

    public static final class QueryParameterIterator implements Iterator<Map.Entry<String, String>> {
        private final String queryParameters;
        private final int queryParametersLength;

        private boolean done = false;
        private int position;

        public QueryParameterIterator(String queryParameters) {
            this.queryParameters = queryParameters;
            this.queryParametersLength = queryParameters.length();

            // If the URI query begins with '?' the first possible start of a query parameter key is the
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
        } else if (count >= 4
            && bytes[offset] == ZERO
            && bytes[offset + 1] == ZERO
            && bytes[offset + 2] == FE
            && bytes[offset + 3] == FF) {
            return new String(bytes, 4, bytes.length - 4, UTF_32BE);
        } else if (count >= 4
            && bytes[offset] == FF
            && bytes[offset + 1] == FE
            && bytes[offset + 2] == ZERO
            && bytes[offset + 3] == ZERO) {
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
            if (!isNullOrEmpty(contentType)) {
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
     * Gets either the system property or environment variable configuration for the passed environment.
     * <p>
     * The system property is checked first, then the environment variable. If neither are found, null is returned.
     *
     * @param environmentConfiguration The environment to search.
     * @param systemProperty The system property name.
     * @param envVar The environment variable name.
     * @param valueSanitizer The function to sanitize the value.
     * @param logger The logger to log the property retrieval.
     * @return The value of the property if found, otherwise null.
     */
    public static String getFromEnvironment(EnvironmentConfiguration environmentConfiguration, String systemProperty,
        String envVar, Function<String, String> valueSanitizer, ClientLogger logger) {
        if (systemProperty != null) {
            final String value = environmentConfiguration.getSystemProperty(systemProperty);
            if (value != null) {
                logger.atVerbose()
                    .addKeyValue("systemProperty", systemProperty)
                    .addKeyValue("value", () -> valueSanitizer.apply(value))
                    .log("Got property from system property.");
                return value;
            }
        }

        if (envVar != null) {
            final String value = environmentConfiguration.getEnvironmentVariable(envVar);
            if (value != null) {
                logger.atVerbose()
                    .addKeyValue("envVar", envVar)
                    .addKeyValue("value", () -> valueSanitizer.apply(value))
                    .log("Got property from environment variable.");
                return value;
            }
        }

        return null;
    }

    /**
     * Creates a {@link Thread} that will shut down the passed {@link ExecutorService} when ran.
     * <p>
     * There are two phases to shut down, each will use half of the shutdown timeout. The first phase uses
     * {@link ExecutorService#shutdown()} and awaits termination. If the executor does not terminate within half the
     * timeout, the second phase will use {@link ExecutorService#shutdownNow()} and await termination.
     *
     * @param executorService The {@link ExecutorService} to shut down.
     * @param shutdownTimeout The maximum time to wait for the executor to shut down.
     * @return The {@link Thread} that will shut down the executor when ran.
     */
    public static Thread createExecutorServiceShutdownThread(ExecutorService executorService,
        Duration shutdownTimeout) {
        long timeoutNanos = shutdownTimeout.toNanos();
        return new Thread(() -> {
            try {
                executorService.shutdown();
                if (!executorService.awaitTermination(timeoutNanos / 2, TimeUnit.NANOSECONDS)) {
                    executorService.shutdownNow();
                    executorService.awaitTermination(timeoutNanos / 2, TimeUnit.NANOSECONDS);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                executorService.shutdown();
            }
        });
    }

    /**
     * Helper method that safely adds a {@link Runtime#addShutdownHook(Thread)} to the JVM that will run when the JVM is
     * shutting down.
     * <p>
     * {@link Runtime#addShutdownHook(Thread)} checks for security privileges and will throw an exception if the proper
     * security isn't available. So, if running with a security manager, setting
     * {@code CORE_ENABLE_SHUTDOWN_HOOK_WITH_PRIVILEGE} to true will have this method use access controller to add
     * the shutdown hook with privileged permissions.
     * <p>
     * If {@code shutdownThread} is null, no shutdown hook will be added and this method will return null.
     *
     * @param shutdownThread The {@link Thread} that will be added as a
     * {@link Runtime#addShutdownHook(Thread) shutdown hook}.
     * @return The {@link Thread} that was passed in.
     */
    @SuppressWarnings({ "deprecation", "removal" })
    public static Thread addShutdownHookSafely(Thread shutdownThread) {
        if (shutdownThread == null) {
            return null;
        }

        if (ShutdownHookAccessHelperHolder.shutdownHookAccessHelper) {
            java.security.AccessController.doPrivileged((java.security.PrivilegedAction<Void>) () -> {
                Runtime.getRuntime().addShutdownHook(shutdownThread);
                return null;
            });
        } else {
            Runtime.getRuntime().addShutdownHook(shutdownThread);
        }

        return shutdownThread;
    }

    /**
     * Helper method that safely removes a {@link Runtime#removeShutdownHook(Thread)} from the JVM.
     * <p>
     * {@link Runtime#removeShutdownHook(Thread)} checks for security privileges and will throw an exception if the
     * proper security isn't available. So, if running with a security manager, setting
     * {@code CORE_ENABLE_SHUTDOWN_HOOK_WITH_PRIVILEGE} to true will have this method use access controller to remove
     * the shutdown hook with privileged permissions.
     * <p>
     * If {@code shutdownThread} is null, no shutdown hook will be removed.
     *
     * @param shutdownThread The {@link Thread} that will be added as a
     * {@link Runtime#addShutdownHook(Thread) shutdown hook}.
     */
    @SuppressWarnings({ "deprecation", "removal" })
    public static void removeShutdownHookSafely(Thread shutdownThread) {
        if (shutdownThread == null) {
            return;
        }

        if (ShutdownHookAccessHelperHolder.shutdownHookAccessHelper) {
            java.security.AccessController.doPrivileged((java.security.PrivilegedAction<Void>) () -> {
                Runtime.getRuntime().removeShutdownHook(shutdownThread);
                return null;
            });
        } else {
            Runtime.getRuntime().removeShutdownHook(shutdownThread);
        }
    }

    /*
     * This looks a bit strange but is needed as ImplUtils is used within Configuration code and if this was done in
     * the static constructor for ImplUtils it would cause a circular dependency, potentially causing a deadlock.
     * Since this is in a static holder class, it will only be loaded when ImplUtils accesses it, which won't happen
     * until ImplUtils is loaded.
     */
    private static final class ShutdownHookAccessHelperHolder {
        private static boolean shutdownHookAccessHelper;

        static {
            shutdownHookAccessHelper = Boolean
                .parseBoolean(Configuration.getGlobalConfiguration().get("CORE_ENABLE_SHUTDOWN_HOOK_WITH_PRIVILEGE"));
        }
    }

    static boolean isShutdownHookAccessHelper() {
        return ShutdownHookAccessHelperHolder.shutdownHookAccessHelper;
    }

    static void setShutdownHookAccessHelper(boolean shutdownHookAccessHelper) {
        ShutdownHookAccessHelperHolder.shutdownHookAccessHelper = shutdownHookAccessHelper;
    }
}
