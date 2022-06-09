// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.exception.UnexpectedLengthException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.implementation.StorageImplUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
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

        if (stringToDecode.contains("+")) {
            StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;
            for (int m = 0; m < stringToDecode.length(); m++) {
                if (stringToDecode.charAt(m) == '+') {
                    if (m > startDex) {
                        outBuilder.append(decode(stringToDecode.substring(startDex, m)));
                    }

                    outBuilder.append("+");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToDecode.length()) {
                outBuilder.append(decode(stringToDecode.substring(startDex)));
            }

            return outBuilder.toString();
        } else {
            return decode(stringToDecode);
        }
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

        if (stringToEncode.length() == 0) {
            return "";
        }

        if (stringToEncode.contains(" ")) {
            StringBuilder outBuilder = new StringBuilder();

            int startDex = 0;
            for (int m = 0; m < stringToEncode.length(); m++) {
                if (stringToEncode.charAt(m) == ' ') {
                    if (m > startDex) {
                        outBuilder.append(encode(stringToEncode.substring(startDex, m)));
                    }

                    outBuilder.append("%20");
                    startDex = m + 1;
                }
            }

            if (startDex != stringToEncode.length()) {
                outBuilder.append(encode(stringToEncode.substring(startDex)));
            }

            return outBuilder.toString();
        } else {
            return encode(stringToEncode);
        }
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
     *
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
                    throw LOGGER.logExceptionAsError(new UnexpectedLengthException(
                        String.format("Request body emitted %d bytes, more than the expected %d bytes.",
                            totalLength, length), totalLength, length));
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException("I/O errors occurred", e));
            }
        }
        return Flux.defer(() -> {
            /*
            If the request needs to be retried, the flux will be resubscribed to. The stream and counter must be
            reset in order to correctly return the same data again.
             */
            final long[] currentTotalLength = new long[1];
            if (markAndReset) {
                try {
                    data.reset();
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(e));
                }
            }
            return Flux.range(0, (int) Math.ceil((double) length / (double) blockSize))
                .map(i -> i * blockSize)
                .concatMap(pos -> Mono.fromCallable(() -> {
                    long count = pos + blockSize > length ? length - pos : blockSize;
                    byte[] cache = new byte[(int) count];
                    int numOfBytes = 0;
                    int offset = 0;
                    // Revise the casting if the max allowed network data transmission is over 2G.
                    int len = (int) count;
                    while (numOfBytes != -1 && offset < count) {
                        numOfBytes = data.read(cache, offset, len);
                        if (numOfBytes != -1) {
                            offset += numOfBytes;
                            len -= numOfBytes;
                            currentTotalLength[0] += numOfBytes;
                        }
                    }
                    if (numOfBytes == -1 && currentTotalLength[0] < length) {
                        throw LOGGER.logExceptionAsError(new UnexpectedLengthException(
                            String.format("Request body emitted %d bytes, less than the expected %d bytes.",
                                currentTotalLength[0], length), currentTotalLength[0], length));
                    }

                    // Validate that stream isn't longer.
                    if (currentTotalLength[0] >= length) {
                        try {
                            if (data.read() != -1) {
                                long totalLength = 1 + currentTotalLength[0] + data.available();
                                throw LOGGER.logExceptionAsError(new UnexpectedLengthException(
                                    String.format("Request body emitted %d bytes, more than the expected %d bytes.",
                                        totalLength, length), totalLength, length));
                            } else if (currentTotalLength[0] > length) {
                                throw LOGGER.logExceptionAsError(new IllegalStateException(
                                    String.format("Read more data than was requested. Size of data read: %d. Size of data"
                                        + " requested: %d", currentTotalLength[0], length)));
                            }
                        } catch (IOException e) {
                            throw LOGGER.logExceptionAsError(new RuntimeException("I/O errors occurred", e));
                        }
                    }

                    return ByteBuffer.wrap(cache, 0, offset);
                }));
        });
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
        if (url.contains("?")) {
            url = String.format("%s&%s=%s", url, key, value);
        } else {
            url = String.format("%s?%s=%s", url, key, value);
        }
        return url;
    }
}
