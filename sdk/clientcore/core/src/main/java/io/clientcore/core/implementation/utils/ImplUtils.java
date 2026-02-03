// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.utils;

import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.CoreUtils;
import io.clientcore.core.utils.configuration.Configuration;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class containing implementation specific methods.
 */
public final class ImplUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ImplUtils.class);

    private static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    private static final byte ZERO = (byte) 0x00;
    private static final byte BB = (byte) 0xBB;
    private static final byte BF = (byte) 0xBF;
    private static final byte EF = (byte) 0xEF;
    private static final byte FE = (byte) 0xFE;
    private static final byte FF = (byte) 0xFF;
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(\\S+)\\b", Pattern.CASE_INSENSITIVE);

    private static final Duration DEFAULT_HTTP_CONNECT_TIMEOUT;
    private static final Duration DEFAULT_HTTP_WRITE_TIMEOUT;
    private static final Duration DEFAULT_HTTP_RESPONSE_TIMEOUT;
    private static final Duration DEFAULT_HTTP_READ_TIMEOUT;

    static {
        Configuration configuration = Configuration.getGlobalConfiguration();
        DEFAULT_HTTP_CONNECT_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            Configuration.REQUEST_CONNECT_TIMEOUT_IN_MS, Duration.ofSeconds(10), LOGGER);
        DEFAULT_HTTP_WRITE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            Configuration.REQUEST_WRITE_TIMEOUT_IN_MS, Duration.ofSeconds(60), LOGGER);
        DEFAULT_HTTP_RESPONSE_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            Configuration.REQUEST_RESPONSE_TIMEOUT_IN_MS, Duration.ofSeconds(60), LOGGER);
        DEFAULT_HTTP_READ_TIMEOUT = getDefaultTimeoutFromEnvironment(configuration,
            Configuration.REQUEST_READ_TIMEOUT_IN_MS, Duration.ofSeconds(60), LOGGER);
    }

    private ImplUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Attempts to load an environment configured default timeout.
     * <p>
     * If the environment default timeout isn't configured, {@code defaultTimeout} will be returned. If the environment
     * default timeout is a string that isn't parseable by {@link Long#parseLong(String)}, {@code defaultTimeout} will
     * be returned. If the environment default timeout is less than 0, {@link Duration#ZERO} will be returned indicated
     * that there is no timeout period.
     *
     * @param configuration The environment configurations.
     * @param timeoutPropertyName The default timeout property name.
     * @param defaultTimeout The fallback timeout to be used.
     * @param logger A {@link ClientLogger} to log exceptions.
     * @return Either the environment configured default timeout, {@code defaultTimeoutMillis}, or 0.
     */
    public static Duration getDefaultTimeoutFromEnvironment(Configuration configuration, String timeoutPropertyName,
        Duration defaultTimeout, ClientLogger logger) {
        String environmentTimeout = configuration.get(timeoutPropertyName);

        // Environment wasn't configured with the timeout property.
        if (environmentTimeout == null || environmentTimeout.isEmpty()) {
            return defaultTimeout;
        }

        try {
            long timeoutMillis = Long.parseLong(environmentTimeout);
            if (timeoutMillis < 0) {
                logger.atVerbose()
                    .addKeyValue(timeoutPropertyName, timeoutMillis)
                    .log("Negative timeout values are not allowed. Using 'Duration.ZERO' to indicate no timeout.");
                return Duration.ZERO;
            }

            return Duration.ofMillis(timeoutMillis);
        } catch (NumberFormatException ex) {
            logger.atInfo()
                .addKeyValue(timeoutPropertyName, environmentTimeout)
                .addKeyValue("defaultTimeout", defaultTimeout)
                .setThrowable(ex)
                .log("Timeout is not valid number. Using default value.");

            return defaultTimeout;
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

        private boolean done;
        private int position;

        public QueryParameterIterator(String queryParameters) {
            this.queryParameters = queryParameters;
            this.done = CoreUtils.isNullOrEmpty(queryParameters);

            if (done) {
                this.position = 0;
                this.queryParametersLength = 0;
            } else {
                this.queryParametersLength = queryParameters.length();

                // If the URI query begins with '?' the first possible start of a query parameter key is the
                // second character in the query.
                position = (queryParameters.startsWith("?")) ? 1 : 0;
            }
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

    /**
     * Gets the default connect timeout.
     *
     * @return The default connect timeout.
     */
    public static Duration getDefaultHttpConnectTimeout() {
        return DEFAULT_HTTP_CONNECT_TIMEOUT;
    }

    /**
     * Gets the default write timeout.
     *
     * @return The default write timeout.
     */
    public static Duration getDefaultHttpWriteTimeout() {
        return DEFAULT_HTTP_WRITE_TIMEOUT;
    }

    /**
     * Gets the default response timeout.
     *
     * @return The default response timeout.
     */
    public static Duration getDefaultHttpResponseTimeout() {
        return DEFAULT_HTTP_RESPONSE_TIMEOUT;
    }

    /**
     * Gets the default read timeout.
     *
     * @return The default read timeout.
     */
    public static Duration getDefaultHttpReadTimeout() {
        return DEFAULT_HTTP_READ_TIMEOUT;
    }

}
