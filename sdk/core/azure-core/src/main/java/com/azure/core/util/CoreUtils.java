// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class contains utility methods useful for building client libraries.
 */
public final class CoreUtils {
    private static final String COMMA = ",";
    private static final Charset UTF_32BE = Charset.forName("UTF-32BE");
    private static final Charset UTF_32LE = Charset.forName("UTF-32LE");
    private static final byte ZERO = (byte) 0x00;
    private static final byte BB = (byte) 0xBB;
    private static final byte BF = (byte) 0xBF;
    private static final byte EF = (byte) 0xEF;
    private static final byte FE = (byte) 0xFE;
    private static final byte FF = (byte) 0xFF;
    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=([\\S]+)\\b", Pattern.CASE_INSENSITIVE);

    private CoreUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Creates a copy of the source byte array.
     *
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] clone(byte[] source) {
        if (source == null) {
            return null;
        }
        byte[] copy = new byte[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Creates a copy of the source int array.
     *
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static int[] clone(int[] source) {
        if (source == null) {
            return null;
        }
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }

    /**
     * Creates a copy of the source array.
     *
     * @param source Array being copied.
     * @param <T> Generic representing the type of the source array.
     * @return A copy of the array or null if source was null.
     */
    public static <T> T[] clone(T[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }

    /**
     * Checks if the array is null or empty.
     *
     * @param array Array being checked for nullness or emptiness.
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the collection is null or empty.
     *
     * @param collection Collection being checked for nullness or emptiness.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the map is null or empty.
     *
     * @param map Map being checked for nullness or emptiness.
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the character sequence is null or empty.
     *
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Turns an array into a string mapping each element to a string and delimits them using a coma.
     *
     * @param array Array being formatted to a string.
     * @param mapper Function that maps each element to a string.
     * @param <T> Generic representing the type of the array.
     * @return Array with each element mapped and delimited, otherwise null if the array is empty or null.
     */
    public static <T> String arrayToString(T[] array, Function<T, String> mapper) {
        if (isNullOrEmpty(array)) {
            return null;
        }

        return Arrays.stream(array).map(mapper).collect(Collectors.joining(COMMA));
    }

    /**
     * Returns the first instance of the given class from an array of Objects.
     *
     * @param args Array of objects to search through to find the first instance of the given `clazz` type.
     * @param clazz The type trying to be found.
     * @param <T> Generic type
     * @return The first object of the desired type, otherwise null.
     */
    public static <T> T findFirstOfType(Object[] args, Class<T> clazz) {
        if (isNullOrEmpty(args)) {
            return null;
        }

        for (Object arg : args) {
            if (clazz.isInstance(arg)) {
                return clazz.cast(arg);
            }
        }

        return null;
    }

    /**
     * Extracts and combines the generic items from all the pages linked together.
     *
     * @param page The paged response from server holding generic items.
     * @param context Metadata that is passed into the function that fetches the items from the next page.
     * @param content The function which fetches items from the next page.
     * @param <T> The type of the item being returned in the paged response.
     * @return The publisher holding all the generic items combined.
     */
    public static <T> Publisher<T> extractAndFetch(PagedResponse<T> page, Context context,
        BiFunction<String, Context, Publisher<T>> content) {
        String nextPageLink = page.getContinuationToken();
        if (nextPageLink == null) {
            return Flux.fromIterable(page.getElements());
        }
        return Flux.fromIterable(page.getElements()).concatWith(content.apply(nextPageLink, context));
    }


    /**
     * Helper method that returns an immutable {@link Map} of properties defined in {@code propertiesFileName}.
     *
     * @param propertiesFileName The file name defining the properties.
     * @return an immutable {@link Map}.
     */
    public static Map<String, String> getProperties(String propertiesFileName) {
        ClientLogger logger = new ClientLogger(CoreUtils.class);
        try (InputStream inputStream = CoreUtils.class.getClassLoader()
            .getResourceAsStream(propertiesFileName)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return Collections.unmodifiableMap(properties.entrySet().stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                        entry -> (String) entry.getValue())));
            }
        } catch (IOException ex) {
            logger.warning("Failed to get properties from " + propertiesFileName, ex);
        }
        return Collections.emptyMap();
    }

    /**
     * Attempts to convert a byte stream into the properly encoded String.
     * <p>
     * This utility method will attempt to find the encoding for the String in this order.
     * <ol>
     *     <li>Find the byte order mark in the byte array.</li>
     *     <li>Find the {@code charset} in the {@code Content-Type} header.</li>
     *     <li>Default to {@code UTF-8}.</li>
     * </ol>
     *
     * @param bytes Byte array.
     * @param contentType {@code Content-Type} header value.
     * @return A string representation of the byte array encoded to the found encoding.
     */
    public static String bomAwareToString(byte[] bytes, String contentType) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length >= 3 && bytes[0] == EF && bytes[1] == BB && bytes[2] == BF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        } else if (bytes.length >= 4 && bytes[0] == ZERO && bytes[1] == ZERO && bytes[2] == FE && bytes[3] == FF) {
            return new String(bytes, 4, bytes.length - 4, UTF_32BE);
        } else if (bytes.length >= 4 && bytes[0] == FF && bytes[1] == FE && bytes[2] == ZERO && bytes[3] == ZERO) {
            return new String(bytes, 4, bytes.length - 4, UTF_32LE);
        } else if (bytes.length >= 2 && bytes[0] == FE && bytes[1] == FF) {
            return new String(bytes, 2, bytes.length - 2, StandardCharsets.UTF_16BE);
        } else if (bytes.length >= 2 && bytes[0] == FF && bytes[1] == FE) {
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
                        return new String(bytes, Charset.forName(charsetMatcher.group(1)));
                    } else {
                        return new String(bytes, StandardCharsets.UTF_8);
                    }
                } catch (IllegalCharsetNameException | UnsupportedCharsetException ex) {
                    return new String(bytes, StandardCharsets.UTF_8);
                }
            } else {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Retrieves the application ID from either a {@link ClientOptions} or {@link HttpLogOptions}.
     * <p>
     * This method first checks {@code clientOptions} for having an application ID then {@code logOptions}, finally
     * returning null if neither are set.
     * <p>
     * {@code clientOptions} is checked first as {@code logOptions} application ID is deprecated.
     *
     * @param clientOptions The {@link ClientOptions}.
     * @param logOptions The {@link HttpLogOptions}.
     * @return The application ID from either {@code clientOptions} or {@code logOptions}, if neither are set null.
     */
    @SuppressWarnings("deprecation")
    public static String getApplicationId(ClientOptions clientOptions, HttpLogOptions logOptions) {
        if (clientOptions != null && !CoreUtils.isNullOrEmpty(clientOptions.getApplicationId())) {
            return clientOptions.getApplicationId();
        } else if (logOptions != null && !CoreUtils.isNullOrEmpty(logOptions.getApplicationId())) {
            return logOptions.getApplicationId();
        } else {
            return null;
        }
    }

    /**
     * Creates {@link HttpHeaders} from the provided {@link ClientOptions}.
     * <p>
     * If {@code clientOptions} is null or {@link ClientOptions#getHeaders()} doesn't return any {@link Header} values
     * null will be returned.
     *
     * @param clientOptions The {@link ClientOptions} used to create the {@link HttpHeaders}.
     * @return {@link HttpHeaders} containing the {@link Header} values from {@link ClientOptions#getHeaders()} if
     * {@code clientOptions} isn't null and contains {@link Header} values, otherwise null.
     */
    public static HttpHeaders createHttpHeadersFromClientOptions(ClientOptions clientOptions) {
        if (clientOptions == null) {
            return null;
        }

        List<HttpHeader> httpHeaderList = new ArrayList<>();
        clientOptions.getHeaders().forEach(
            header -> httpHeaderList.add(new HttpHeader(header.getName(), header.getValue())));

        return httpHeaderList.isEmpty() ? null : new HttpHeaders(httpHeaderList);
    }
}
