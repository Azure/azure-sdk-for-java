// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.implementation.exception.UnexpectedLengthException;
import com.azure.core.implementation.http.UrlBuilder;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.credentials.SharedKeyCredential;
import com.azure.storage.common.policy.SharedKeyCredentialPolicy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public final class Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Utility.class);
    private static final String DESERIALIZED_HEADERS = "deserializedHeaders";
    private static final String ETAG = "eTag";

    public static final DateTimeFormatter ISO_8601_UTC_DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ROOT).withZone(ZoneId.of("UTC"));
    /**
     * Stores a reference to the date/time pattern with the greatest precision Java.util.Date is capable of expressing.
     */
    private static final String MAX_PRECISION_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /**
     * Stores a reference to the ISO8601 date/time pattern.
     */
    private static final String ISO8601_PATTERN_NO_SECONDS = "yyyy-MM-dd'T'HH:mm'Z'";
    /**
     * The length of a datestring that matches the MAX_PRECISION_PATTERN.
     */
    private static final int MAX_PRECISION_DATESTRING_LENGTH = MAX_PRECISION_PATTERN.replaceAll("'", "").length();

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order. The value is
     * stored as a string (ex. key=val1,val2,val3 instead of key=[val1, val2, val3]).
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    public static TreeMap<String, String> parseQueryString(final String queryString) {
        return parseQueryStringHelper(queryString, Utility::urlDecode);
    }

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order. The value is
     * stored as a parsed array (ex. key=[val1, val2, val3] instead of key=val1,val2,val3).
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    public static TreeMap<String, String[]> parseQueryStringSplitValues(final String queryString) {
        return parseQueryStringHelper(queryString, (value) -> urlDecode(value).split(","));
    }

    private static <T> TreeMap<String, T> parseQueryStringHelper(final String queryString, Function<String, T> valueParser) {
        TreeMap<String, T> pieces = new TreeMap<>();

        if (ImplUtils.isNullOrEmpty(queryString)) {
            return pieces;
        }

        for (String kvp : queryString.split("&")) {
            int equalIndex = kvp.indexOf("=");
            String key = urlDecode(kvp.substring(0, equalIndex).toLowerCase(Locale.ROOT));
            T value = valueParser.apply(kvp.substring(equalIndex + 1));

            pieces.putIfAbsent(key, value);
        }

        return pieces;
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
        if (ImplUtils.isNullOrEmpty(stringToDecode)) {
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
            return URLDecoder.decode(stringToDecode, Constants.UTF8_CHARSET);
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
            return Constants.EMPTY_STRING;
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
            return URLEncoder.encode(stringToEncode, Constants.UTF8_CHARSET);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Parses the connection string into key-value pair map.
     *
     * @param connectionString Connection string to parse
     * @return a mapping of connection string pieces as key-value pairs.
     */
    public static Map<String, String> parseConnectionString(final String connectionString) {
        Map<String, String> parts = new HashMap<>();

        for (String part : connectionString.split(";")) {
            String[] kvp = part.split("=", 2);
            parts.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        return parts;
    }

    /**
     * Blocks an asynchronous response with an optional timeout.
     *
     * @param response Asynchronous response to block
     * @param timeout Optional timeout
     * @param <T> Return type of the asynchronous response
     * @return the value of the asynchronous response
     * @throws RuntimeException If the asynchronous response doesn't complete before the timeout expires.
     */
    public static <T> T blockWithOptionalTimeout(Mono<T> response, Duration timeout) {
        if (timeout == null) {
            return response.block();
        } else {
            return response.block(timeout);
        }
    }

    /**
     * Applies a timeout to a publisher if the given timeout is not null.
     *
     * @param publisher Mono to apply optional timeout to.
     * @param timeout Optional timeout.
     * @param <T> Return type of the Mono.
     * @return Mono with an applied timeout, if any.
     */
    public static <T> Mono<T> applyOptionalTimeout(Mono<T> publisher, Duration timeout) {
        return timeout == null
            ? publisher
            : publisher.timeout(timeout);
    }

    /**
     * Applies a timeout to a publisher if the given timeout is not null.
     *
     * @param publisher Flux to apply optional timeout to.
     * @param timeout Optional timeout.
     * @param <T> Return type of the Flux.
     * @return Flux with an applied timeout, if any.
     */
    public static <T> Flux<T> applyOptionalTimeout(Flux<T> publisher, Duration timeout) {
        return timeout == null
            ? publisher
            : publisher.timeout(timeout);
    }

    /**
     * Asserts that a value is not {@code null}.
     *
     * @param param Name of the parameter
     * @param value Value of the parameter
     * @throws IllegalArgumentException If {@code value} is {@code null}
     */
    public static void assertNotNull(final String param, final Object value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, Constants.MessageConstants.ARGUMENT_NULL_OR_EMPTY, param));
        }
    }

    /**
     * Asserts that the specified number is in the valid range. The range is inclusive.
     *
     * @param param Name of the parameter
     * @param value Value of the parameter
     * @param min The minimum allowed value
     * @param max The maximum allowed value
     * @throws IllegalArgumentException If {@code value} is less than {@code min} or {@code value} is greater than
     * {@code max}.
     */
    public static void assertInBounds(final String param, final long value, final long min, final long max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, Constants.MessageConstants.PARAMETER_NOT_IN_RANGE, param, min, max));
        }
    }

    /**
     * Given a String representing a date in a form of the ISO8601 pattern, generates a Date representing it with up to
     * millisecond precision.
     *
     * @param dateString the {@code String} to be interpreted as a <code>Date</code>
     * @return the corresponding <code>Date</code> object
     * @throws IllegalArgumentException If {@code dateString} doesn't match an ISO8601 pattern
     */
    public static OffsetDateTime parseDate(String dateString) {
        String pattern = MAX_PRECISION_PATTERN;
        switch (dateString.length()) {
            case 28: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'"-> [2012-01-04T23:21:59.1234567Z] length = 28
            case 27: // "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"-> [2012-01-04T23:21:59.123456Z] length = 27
            case 26: // "yyyy-MM-dd'T'HH:mm:ss.SSSSS'Z'"-> [2012-01-04T23:21:59.12345Z] length = 26
            case 25: // "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'"-> [2012-01-04T23:21:59.1234Z] length = 25
            case 24: // "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"-> [2012-01-04T23:21:59.123Z] length = 24
                dateString = dateString.substring(0, MAX_PRECISION_DATESTRING_LENGTH);
                break;
            case 23: // "yyyy-MM-dd'T'HH:mm:ss.SS'Z'"-> [2012-01-04T23:21:59.12Z] length = 23
                // SS is assumed to be milliseconds, so a trailing 0 is necessary
                dateString = dateString.replace("Z", "0");
                break;
            case 22: // "yyyy-MM-dd'T'HH:mm:ss.S'Z'"-> [2012-01-04T23:21:59.1Z] length = 22
                // S is assumed to be milliseconds, so trailing 0's are necessary
                dateString = dateString.replace("Z", "00");
                break;
            case 20: // "yyyy-MM-dd'T'HH:mm:ss'Z'"-> [2012-01-04T23:21:59Z] length = 20
                pattern = Utility.ISO8601_PATTERN;
                break;
            case 17: // "yyyy-MM-dd'T'HH:mm'Z'"-> [2012-01-04T23:21Z] length = 17
                pattern = Utility.ISO8601_PATTERN_NO_SECONDS;
                break;
            default:
                throw new IllegalArgumentException(String.format(Locale.ROOT, Constants.MessageConstants.INVALID_DATE_STRING, dateString));
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        return LocalDateTime.parse(dateString, formatter).atZone(ZoneOffset.UTC).toOffsetDateTime();
    }

    /**
     * Wraps any potential error responses from the service and applies post processing of the response's eTag header
     * to standardize the value.
     *
     * @param response Response from a service call
     * @param errorWrapper Error wrapping function that is applied to the response
     * @param <T> Value type of the response
     * @return an updated response with post processing steps applied.
     */
    public static <T> Mono<T> postProcessResponse(Mono<T> response, Function<Mono<T>, Mono<T>> errorWrapper) {
        return scrubETagHeader(errorWrapper.apply(response));
    }

    /*
    The service is inconsistent in whether or not the etag header value has quotes. This method will check if the
    response returns an etag value, and if it does, remove any quotes that may be present to give the user a more
    predictable format to work with.
     */
    private static <T> Mono<T> scrubETagHeader(Mono<T> unprocessedResponse) {
        return unprocessedResponse.map(response -> {
            String eTag = null;

            try {
                Object headers = response.getClass().getMethod(DESERIALIZED_HEADERS).invoke(response);
                Method eTagGetterMethod = headers.getClass().getMethod(ETAG);
                eTag = (String) eTagGetterMethod.invoke(headers);

                if (eTag == null) {
                    return response;
                }

                eTag = eTag.replace("\"", "");
                headers.getClass().getMethod(ETAG, String.class).invoke(headers, eTag);
            } catch (NoSuchMethodException ex) {
                // Response did not return an eTag value.
            } catch (IllegalAccessException | InvocationTargetException ex) {
                // Unable to access the method or the invoked method threw an exception.
            }

            try {
                HttpHeaders rawHeaders = (HttpHeaders) response.getClass().getMethod("headers").invoke(response);
                //
                if (eTag != null) {
                    rawHeaders.put(ETAG, eTag);
                } else {
                    HttpHeader eTagHeader = rawHeaders.get(ETAG);
                    if (eTagHeader != null && eTagHeader.value() != null) {
                        eTag = eTagHeader.value().replace("\"", "");
                        rawHeaders.put(ETAG, eTag);
                    }
                }
            } catch (NoSuchMethodException e) {
                // Response did not return an eTag value. No change necessary.
            } catch (IllegalAccessException | InvocationTargetException e) {
                // Unable to access the method or the invoked method threw an exception.
            }

            return response;
        });
    }

    /**
     * Computes a signature for the specified string using the HMAC-SHA256 algorithm.
     *
     * @param base64Key Base64 encoded key used to sign the string
     * @param stringToSign UTF-8 encoded string to sign
     * @return the HMAC-SHA256 encoded signature
     * @throws RuntimeException If the HMAC-SHA256 algorithm isn't support, if the key isn't a valid Base64 encoded
     * string, or the UTF-8 charset isn't supported.
     */
    public static String computeHMac256(final String base64Key, final String stringToSign) {
        try {
            byte[] key = Base64.getDecoder().decode(base64Key);
            Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
            hmacSHA256.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] utf8Bytes = stringToSign.getBytes(StandardCharsets.UTF_8);
            return Base64.getEncoder().encodeToString(hmacSHA256.doFinal(utf8Bytes));
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Appends a string to the end of the passed URL's path.
     *
     * @param baseURL URL having a path appended
     * @param name Name of the path
     * @return a URL with the path appended.
     * @throws IllegalArgumentException If {@code name} causes the URL to become malformed.
     */
    public static URL appendToURLPath(URL baseURL, String name) {
        UrlBuilder builder = UrlBuilder.parse(baseURL);

        if (builder.path() == null) {
            builder.path("/");
        } else if (!builder.path().endsWith("/")) {
            builder.path(builder.path() + "/");
        }

        builder.path(builder.path() + name);

        try {
            return builder.toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Strips the last path segment from the passed URL.
     *
     * @param baseURL URL having its last path segment stripped
     * @return a URL with the path segment stripped.
     * @throws IllegalArgumentException If stripping the last path segment causes the URL to become malformed or it
     * doesn't contain any path segments.
     */
    public static URL stripLastPathSegment(URL baseURL) {
        UrlBuilder builder = UrlBuilder.parse(baseURL);

        if (builder.path() == null || !builder.path().contains("/")) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, Constants.MessageConstants.NO_PATH_SEGMENTS, baseURL));
        }

        builder.path(builder.path().substring(0, builder.path().lastIndexOf("/")));
        try {
            return builder.toURL();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Searches for a {@link SharedKeyCredential} in the passed {@link HttpPipeline}.
     *
     * @param httpPipeline Pipeline being searched
     * @return a SharedKeyCredential if the pipeline contains one, otherwise null.
     */
    public static SharedKeyCredential getSharedKeyCredential(HttpPipeline httpPipeline) {
        for (int i = 0; i < httpPipeline.getPolicyCount(); i++) {
            HttpPipelinePolicy httpPipelinePolicy = httpPipeline.getPolicy(i);
            if (httpPipelinePolicy instanceof SharedKeyCredentialPolicy) {
                SharedKeyCredentialPolicy sharedKeyCredentialPolicy = (SharedKeyCredentialPolicy) httpPipelinePolicy;
                return sharedKeyCredentialPolicy.sharedKeyCredential();
            }
        }
        return null;
    }

    /**
     * A utility method for converting the input stream to Flux of ByteBuffer. Will check the equality of
     * entity length and the input length.
     *
     * @param data The input data which needs to convert to ByteBuffer.
     * @param length The expected input data length.
     * @param blockSize The size of each ByteBuffer.
     * @return {@link ByteBuffer} which contains the input data.
     * @throws UnexpectedLengthException when input data length mismatch input length.
     * @throws RuntimeException When I/O error occurs.
     */
    public static Flux<ByteBuffer> convertStreamToByteBuffer(InputStream data, long length, int blockSize) {
        final AtomicLong lengthInChunk = new AtomicLong();
        return Flux.range(0, (int) Math.ceil((double) length / (double) blockSize))
            .map(i -> i * blockSize)
            .concatMap(pos -> Mono.fromCallable(() -> {
                long count = pos + blockSize > length ? length - pos : blockSize;
                byte[] cache = new byte[(int) count];
                int lastIndex = data.read(cache);
                lengthInChunk.getAndAdd(lastIndex);
                if (lengthInChunk.get() < count) {
                    throw new UnexpectedLengthException(
                        String.format("Request body emitted %d bytes less than the expected %d bytes.",
                            lengthInChunk.get(), length), lengthInChunk.get(), length);
                }
                return ByteBuffer.wrap(cache);
            }))
            .doOnComplete(() -> {
                try {
                    if (data.available() > 0) {
                        Long totalLength = lengthInChunk.get() + data.available();
                        throw new UnexpectedLengthException(
                            String.format("Request body emitted %d bytes more than the expected %d bytes.",
                                totalLength, length), totalLength, length);
                    }
                } catch (IOException e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException("I/O errors occurs. Error deatils: "
                        + e.getMessage()));
                }
            });
    }
}
