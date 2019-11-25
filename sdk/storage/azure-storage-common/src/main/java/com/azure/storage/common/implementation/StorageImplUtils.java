// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.util.UrlBuilder;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.Utility;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static com.azure.storage.common.Utility.urlDecode;

/**
 * Utility class which is used internally.
 */
public class StorageImplUtils {
    private static final ClientLogger LOGGER = new ClientLogger(StorageImplUtils.class);

    private static final String ARGUMENT_NULL_OR_EMPTY =
        "The argument must not be null or an empty string. Argument name: %s.";

    private static final String PARAMETER_NOT_IN_RANGE = "The value of the parameter '%s' should be between %s and %s.";

    private static final String NO_PATH_SEGMENTS = "URL %s does not contain path segments.";

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order. The value is
     * stored as a string (ex. key=val1,val2,val3 instead of key=[val1, val2, val3]).
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    public static Map<String, String> parseQueryString(final String queryString) {
        return parseQueryStringHelper(queryString, Utility::urlDecode);
    }

    /**
     * Parses the query string into a key-value pair map that maintains key, query parameter key, order. The value is
     * stored as a parsed array (ex. key=[val1, val2, val3] instead of key=val1,val2,val3).
     *
     * @param queryString Query string to parse
     * @return a mapping of query string pieces as key-value pairs.
     */
    public static Map<String, String[]> parseQueryStringSplitValues(final String queryString) {
        return parseQueryStringHelper(queryString, (value) -> urlDecode(value).split(","));
    }

    private static <T> Map<String, T> parseQueryStringHelper(final String queryString,
                                                             Function<String, T> valueParser) {
        TreeMap<String, T> pieces = new TreeMap<>();

        if (CoreUtils.isNullOrEmpty(queryString)) {
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
     * @throws NullPointerException If {@code value} is {@code null}
     */
    public static void assertNotNull(final String param, final Object value) {
        if (value == null) {
            throw new NullPointerException(String.format(Locale.ROOT, ARGUMENT_NULL_OR_EMPTY, param));
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
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(Locale.ROOT,
                PARAMETER_NOT_IN_RANGE, param, min, max)));
        }
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
    public static URL appendToUrlPath(String baseURL, String name) {
        UrlBuilder builder = UrlBuilder.parse(baseURL);

        if (builder.getPath() == null) {
            builder.setPath("/");
        } else if (!builder.getPath().endsWith("/")) {
            builder.setPath(builder.getPath() + "/");
        }

        builder.setPath(builder.getPath() + name);

        try {
            return builder.toUrl();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }


    /**
     * Strips the last path segment from the passed URL.
     *
     * @param baseUrl URL having its last path segment stripped
     * @return a URL with the path segment stripped.
     * @throws IllegalArgumentException If stripping the last path segment causes the URL to become malformed or it
     * doesn't contain any path segments.
     */
    public static URL stripLastPathSegment(URL baseUrl) {
        UrlBuilder builder = UrlBuilder.parse(baseUrl);

        if (builder.getPath() == null || !builder.getPath().contains("/")) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, NO_PATH_SEGMENTS, baseUrl));
        }

        builder.setPath(builder.getPath().substring(0, builder.getPath().lastIndexOf("/")));
        try {
            return builder.toUrl();
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Strips the account name from host part of the URL object.
     *
     * @param url URL having its  hostanme
     * @return account name.
     */
    public static String getAccountName(URL url) {
        UrlBuilder builder = UrlBuilder.parse(url);
        String accountName =  null;
        String host = builder.getHost();
        //Parse host to get account name
        // host will look like this : <accountname>.blob.core.windows.net
        if (!CoreUtils.isNullOrEmpty(host)) {
            int accountNameIndex = host.indexOf('.');
            if (accountNameIndex == -1) {
                // host only contains account name
                accountName = host;
            } else {
                // if host is separated by .
                accountName = host.substring(0, accountNameIndex);
            }
        }
        return accountName;
    }
}
