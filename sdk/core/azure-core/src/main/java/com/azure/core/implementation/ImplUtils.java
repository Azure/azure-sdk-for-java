// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.FluxUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Utility class containing implementation specific methods.
 */
public final class ImplUtils {
    private static final String RETRY_AFTER_HEADER = "Retry-After";
    private static final String RETRY_AFTER_MS_HEADER = "retry-after-ms";
    private static final String X_MS_RETRY_AFTER_MS_HEADER = "x-ms-retry-after-ms";

    private static final String[] JSON_STRING_ESCAPES;

    static {
        // JSON string escapes are only needed in ASCII.
        JSON_STRING_ESCAPES = new String[128];

        // Control characters need to be converted to their unicode equivalent.
        for (int i = 0; i < 32; i++) {
            // Convert the ASCII character into hex.
            JSON_STRING_ESCAPES[i] = String.format("\\u%04x", i);
        }

        // Special characters that need to be escaped.
        JSON_STRING_ESCAPES['"'] = "\\\"";
        JSON_STRING_ESCAPES['\\'] = "\\\\";
        JSON_STRING_ESCAPES['\t'] = "\\t";
        JSON_STRING_ESCAPES['\b'] = "\\b";
        JSON_STRING_ESCAPES['\n'] = "\\n";
        JSON_STRING_ESCAPES['\r'] = "\\r";
        JSON_STRING_ESCAPES['\f'] = "\\f";
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
        Duration retryDelay = tryGetRetryDelay(headers, X_MS_RETRY_AFTER_MS_HEADER,
            ImplUtils::tryGetDelayMillis);
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
        retryDelay = tryGetRetryDelay(headers, RETRY_AFTER_HEADER,
            headerValue -> tryParseLongOrDateTime(headerValue, nowSupplier));
        if (retryDelay != null) {
            return retryDelay;
        }

        // None of the well-known headers have been found, return null to indicate no retry after.
        return null;
    }

    private static Duration tryGetRetryDelay(HttpHeaders headers, String headerName,
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
     * Writes a String into a JSON output escaping the required characters.
     *
     * @param input The String input.
     * @param output The JSON output.
     */
    public static void safeWriteStringIntoJson(String input, StringBuilder output) {
        int inputLength = input.length();
        int lastWrite = 0;

        for (int i = 0; i < inputLength; i++) {
            char c = input.charAt(i);

            String replacement;
            if (c < 128) {
                replacement = JSON_STRING_ESCAPES[c];
                if (replacement == null) {
                    // Character doesn't need to replaced, skip it.
                    continue;
                }
            } else if (c == '\u2028') {
                replacement = "\\u2028";
            } else if (c == '\u2029') {
                replacement = "\\u2029";
            } else {
                // Character doesn't need to replaced, skip it.
                continue;
            }

            // If a range of characters that don't need to be escaped were skipped write them now.
            if (lastWrite < i) {
                output.append(input, lastWrite, i);
            }

            // Write the replacement character.
            output.append(replacement);
            lastWrite = i + 1;
        }

        if (lastWrite == 0) {
            // No characters were escaped, write the whole input.
            output.append(input);
        } else if (lastWrite < inputLength) {
            // If the last characters in the input didn't need escaping write them now.
            output.append(input, lastWrite, inputLength);
        }
    }

    private ImplUtils() {
    }
}
