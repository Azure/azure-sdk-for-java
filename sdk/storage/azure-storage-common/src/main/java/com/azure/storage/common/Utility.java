// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

/**
 * Utility methods for storage client libraries.
 */
public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);

    /**
     * Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
     * for more information on Azure resource provider namespaces.
      */
    public static final String STORAGE_TRACING_NAMESPACE_VALUE = "Microsoft.Storage";

    /**
     * Creates a new instance of {@link Utility}.
     */
    public Utility() {
    }

    /**
     * Performs a safe decoding of the passed string, taking care to preserve each {@code +} character rather than
     * replacing it with a space character.
     *
     * @param stringToDecode String value to decode
     * @return the decoded string value
     * @throws RuntimeException If the UTF-8 charset isn't supported
     */
    public static String urlDecode(final String stringToDecode) {
        if (CoreUtils.isNullOrEmpty(stringToDecode)) {
            return "";
        }

        int lastIndexOfPlus = 0;
        int indexOfPlus = stringToDecode.indexOf('+');

        if (indexOfPlus == -1) {
            // No '+' characters to preserve.
            return decode(stringToDecode);
        }

        // Create a StringBuilder large enough to contain the decoded string.
        // This will create a StringBuilder larger than the final string as decoding shrinks in size ('%20' -> ' ').
        StringBuilder outBuilder = new StringBuilder(stringToDecode.length());

        do {
            // Decode the range of characters between the last two '+'s found.
            outBuilder.append(decode(stringToDecode.substring(lastIndexOfPlus, indexOfPlus)));

            // Append the preserved '+'/
            outBuilder.append('+');

            // Set the last found plus index to the index after the '+' just found.
            lastIndexOfPlus = indexOfPlus + 1;

            // Continue until no further '+' characters are found.
        } while ((indexOfPlus = stringToDecode.indexOf('+', lastIndexOfPlus)) != -1);

        // If the last found plus wasn't the last character decode the remaining string.
        if (lastIndexOfPlus != stringToDecode.length()) {
            outBuilder.append(decode(stringToDecode.substring(lastIndexOfPlus)));
        }

        return outBuilder.toString();
    }

    /*
     * Helper method to reduce duplicate calls of URLDecoder.decode
     */
    private static String decode(final String stringToDecode) {
        try {
            return URLDecoder.decode(stringToDecode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Performs a safe encoding of the specified string, taking care to insert %20 for each space character instead of
     * inserting the {@code +} character.
     *
     * @param stringToEncode String value to encode
     * @return the encoded string value
     * @throws RuntimeException If the UTF-8 charset ins't supported
     */
    public static String urlEncode(final String stringToEncode) {
        if (stringToEncode == null) {
            return null;
        }

        if (stringToEncode.isEmpty()) {
            return "";
        }

        int lastIndexOfSpace = 0;
        int indexOfSpace = stringToEncode.indexOf(' ');

        if (indexOfSpace == -1) {
            // No ' ' characters to escape.
            return encode(stringToEncode);
        }

        // Create a StringBuilder with an estimated size large enough to contain the encoded string.
        // It's unknown how many characters will need encoding so this is a best effort as encoding increases size
        // (' ' -> '%20').
        // Use 2x the string length, this means every third character will need to be encoded to three characters.
        // 90 characters / 3 = 30 encodings of 3 characters = 90, 2 * 90 = 180.
        StringBuilder outBuilder = new StringBuilder(stringToEncode.length() * 2);

        do {
            // Encode the range of characters between the last two ' 's found.
            outBuilder.append(encode(stringToEncode.substring(lastIndexOfSpace, indexOfSpace)));

            // Append the preserved ' '.
            outBuilder.append("%20");

            // Set the last found space index to the index after the ' ' just found.
            lastIndexOfSpace = indexOfSpace + 1;

            // Continue until no further ' ' characters are found.
        } while ((indexOfSpace = stringToEncode.indexOf(' ', lastIndexOfSpace)) != -1);

        // If the last found space wasn't the last character encode the remaining string.
        if (lastIndexOfSpace != stringToEncode.length()) {
            outBuilder.append(encode(stringToEncode.substring(lastIndexOfSpace)));
        }

        return outBuilder.toString();
    }

    /*
     * Helper method to reduce duplicate calls of URLEncoder.encode
     */
    private static String encode(final String stringToEncode) {
        try {
            return URLEncoder.encode(stringToEncode, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Performs a safe encoding of a url string, only encoding the path.
     *
     * @param url The url to encode.
     * @return The encoded url.
     */
    public static String encodeUrlPath(String url) {
        /* Deconstruct the URL and reconstruct it making sure the path is encoded. */
        UrlBuilder builder = UrlBuilder.parse(url);
        String path = builder.getPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        path = Utility.urlEncode(Utility.urlDecode(path));
        builder.setPath(path);
        return builder.toString();
    }

    /**
     * Given a String representing a date in a form of the ISO8601 pattern, generates a Date representing it with up to
     * millisecond precision.
     *
     * @param dateString the {@code String} to be interpreted as a <code>Date</code>
     * @return the corresponding <code>Date</code> object
     * @throws IllegalArgumentException If {@code dateString} doesn't match an ISO8601 pattern
     * @deprecated Use {@link StorageImplUtils#parseDateAndFormat(String)}
     */
    @Deprecated
    public static OffsetDateTime parseDate(String dateString) {
        // Call into the internal method that replaces this deprecated method and extract the OffsetDateTime value that
        // would have been returned before. This allows for the same implementation to be shared, reducing a duplication
        // of effort if an update needs to be made to logic.
        return StorageImplUtils.parseDateAndFormat(dateString).getDateTime();
    }

    /**
     * A utility method for converting the input stream to Flux of ByteBuffer. Will check the equality of entity length
     * and the input length.
     *
     * @param data The input data which needs to convert to ByteBuffer.
     * @param length The expected input data length.
     * @param blockSize The size of each ByteBuffer.
     * @return {@link ByteBuffer} which contains the input data.
     * @throws UnexpectedLengthException when input data length mismatch input length.
     * @throws RuntimeException When I/O error occurs.
     */
    public static Flux<ByteBuffer> convertStreamToByteBuffer(InputStream data, long length, int blockSize) {
        return convertStreamToByteBuffer(data, length, blockSize, true);
    }

    /**
     * A utility method for converting the input stream to Flux of ByteBuffer. Will check the equality of entity length
     * and the input length.
     * <p>
     * Using markAndReset=true to force a seekable stream implies a buffering strategy is not being used, in which case
     * length is still needed for whatever underlying REST call is being streamed to. If markAndReset=false and data is
     * being buffered, consider using {@link com.azure.core.util.FluxUtil#toFluxByteBuffer(InputStream, int)} which
     * does not require a data length.
     *
     * @param data The input data which needs to convert to ByteBuffer.
     * @param length The expected input data length.
     * @param blockSize The size of each ByteBuffer.
     * @param markAndReset Whether the stream needs to be marked and reset. This should generally always be true to
     * support retries. It is false in the case of buffered upload to support non markable streams because buffered
     * upload uses its own mechanisms to support retries.
     * @return {@link ByteBuffer} which contains the input data.
     * @throws UnexpectedLengthException when input data length mismatch input length.
     * @throws RuntimeException When I/O error occurs.
     */
    public static Flux<ByteBuffer> convertStreamToByteBuffer(InputStream data, long length, int blockSize,
        boolean markAndReset) {
        if (markAndReset) {
            data.mark(Integer.MAX_VALUE);
        }

        if (length == 0) {
            try {
                if (data.read() != -1) {
                    long totalLength = 1 + data.available();
                    return FluxUtil.fluxError(LOGGER, new UnexpectedLengthException(String.format(
                        "Request body emitted %d bytes, more than the expected %d bytes.", totalLength, length),
                        totalLength, length));
                }
            } catch (IOException e) {
                return FluxUtil.fluxError(LOGGER, new UncheckedIOException(e));
            }
        }

        return Flux.<ByteBuffer>defer(() -> {
            /*
             * If the request needs to be retried, the flux will be resubscribed to. The stream and counter must be
             * reset in order to correctly return the same data again.
             */
            if (markAndReset) {
                try {
                    data.reset();
                } catch (IOException e) {
                    return FluxUtil.fluxError(LOGGER, new UncheckedIOException(e));
                }
            }

            final long[] currentTotalLength = new long[1];
            return Flux.generate(() -> data, (is, sink) -> {
                long pos = currentTotalLength[0];

                long count = (pos + blockSize) > length ? (length - pos) : blockSize;
                byte[] cache = new byte[(int) count];

                int numOfBytes = 0;
                int offset = 0;
                // Revise the casting if the max allowed network data transmission is over 2G.
                int len = (int) count;

                while (numOfBytes != -1 && offset < count) {
                    try {
                        numOfBytes = data.read(cache, offset, len);
                        if (numOfBytes != -1) {
                            offset += numOfBytes;
                            len -= numOfBytes;
                            currentTotalLength[0] += numOfBytes;
                        }
                    } catch (IOException e) {
                        sink.error(e);
                        return is;
                    }
                }

                if (numOfBytes == -1 && currentTotalLength[0] < length) {
                    sink.error(LOGGER.logExceptionAsError(new UnexpectedLengthException(String.format(
                        "Request body emitted %d bytes, less than the expected %d bytes.",
                        currentTotalLength[0], length), currentTotalLength[0], length)));
                    return is;
                }

                // Validate that stream isn't longer.
                if (currentTotalLength[0] >= length) {
                    try {
                        if (data.read() != -1) {
                            long totalLength = 1 + currentTotalLength[0] + data.available();
                            sink.error(LOGGER.logExceptionAsError(new UnexpectedLengthException(
                                String.format("Request body emitted %d bytes, more than the expected %d bytes.",
                                    totalLength, length), totalLength, length)));
                            return is;
                        } else if (currentTotalLength[0] > length) {
                            sink.error(LOGGER.logExceptionAsError(new IllegalStateException(
                                String.format("Read more data than was requested. Size of data read: %d. Size of data"
                                    + " requested: %d", currentTotalLength[0], length))));
                            return is;
                        }
                    } catch (IOException e) {
                        sink.error(LOGGER.logExceptionAsError(new RuntimeException("I/O errors occurred", e)));
                        return is;
                    }
                }

                sink.next(ByteBuffer.wrap(cache, 0, offset));
                if (currentTotalLength[0] == length) {
                    sink.complete();
                }
                return is;
            });
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Appends a query parameter to a url.
     *
     * @param url The url.
     * @param key The query key.
     * @param value The query value.
     * @return The updated url.
     */
    public static String appendQueryParameter(String url, String key, String value) {
        return (url.indexOf('?') != -1)
            ? url + "&" + key + "=" + value
            : url + "?" + key + "=" + value;
    }
}
