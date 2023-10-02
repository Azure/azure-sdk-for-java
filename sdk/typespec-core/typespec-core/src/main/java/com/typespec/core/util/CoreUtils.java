// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.policy.HttpLogOptions;
import com.typespec.core.http.rest.PagedResponse;
import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.util.logging.ClientLogger;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class contains utility methods useful for building client libraries.
 */
public final class CoreUtils {
    // CoreUtils is a commonly used utility, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(CoreUtils.class);

    private static final char[] LOWERCASE_HEX_CHARACTERS = "0123456789abcdef".toCharArray();

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

        return Arrays.stream(array).map(mapper).collect(Collectors.joining(","));
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
     * @param <T> The type of the item being returned by the paged response.
     * @return The publisher holding all the generic items combined.
     * @deprecated Use localized implementation.
     */
    @Deprecated
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
        try (InputStream inputStream = CoreUtils.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
            if (inputStream != null) {
                Properties properties = new Properties();
                properties.load(inputStream);
                return Collections.unmodifiableMap(properties.entrySet().stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(),
                        entry -> (String) entry.getValue())));
            }
        } catch (IOException ex) {
            LOGGER.warning("Failed to get properties from " + propertiesFileName, ex);
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

        return ImplUtils.bomAwareToString(bytes, 0, bytes.length, contentType);
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
    @SuppressWarnings("deprecation")
    public static HttpHeaders createHttpHeadersFromClientOptions(ClientOptions clientOptions) {
        if (clientOptions == null) {
            return null;
        }

        Iterator<Header> headerIterator = clientOptions.getHeaders().iterator();
        if (!headerIterator.hasNext()) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        do {
            Header header = headerIterator.next();
            headers.set(header.getName(), header.getValue());
        } while (headerIterator.hasNext());

        return headers;
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
        if (CoreUtils.isNullOrEmpty(environmentTimeout)) {
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
            logger.atWarning()
                .addKeyValue(timeoutPropertyName, environmentTimeout)
                .addKeyValue("defaultTimeout", defaultTimeout)
                .log("Timeout is not valid number. Using default value.", ex);

            return defaultTimeout;
        }
    }

    /**
     * Merges two {@link Context Contexts} into a new {@link Context}.
     *
     * @param into Context being merged into.
     * @param from Context being merged.
     * @return A new Context that is the merged Contexts.
     * @throws NullPointerException If either {@code into} or {@code from} is null.
     */
    public static Context mergeContexts(Context into, Context from) {
        Objects.requireNonNull(into, "'into' cannot be null.");
        Objects.requireNonNull(from, "'from' cannot be null.");

        // If the 'into' Context is the NONE Context just return the 'from' Context.
        // This is safe as Context is immutable and prevents needing to create any new Contexts and temporary arrays.
        if (into == Context.NONE) {
            return from;
        }

        // Same goes the other way, where if the 'from' Context is the NONE Context just return the 'into' Context.
        if (from == Context.NONE) {
            return into;
        }

        Context[] contextChain = from.getContextChain();

        Context returnContext = into;
        for (Context toAdd : contextChain) {
            if (toAdd != null) {
                returnContext = returnContext.addData(toAdd.getKey(), toAdd.getValue());
            }
        }

        return returnContext;
    }

    /**
     * Optimized version of {@link String#join(CharSequence, Iterable)} when the {@code values} has a small set of
     * object.
     *
     * @param delimiter Delimiter between the values.
     * @param values The values to join.
     * @return The {@code values} joined delimited by the {@code delimiter}.
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
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4);
            case 6:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5);
            case 7:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6);
            case 8:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7);
            case 9:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7) + delimiter + values.get(8);
            case 10:
                return values.get(0) + delimiter + values.get(1) + delimiter + values.get(2) + delimiter
                    + values.get(3) + delimiter + values.get(4) + delimiter + values.get(5) + delimiter + values.get(6)
                    + delimiter + values.get(7) + delimiter + values.get(8) + delimiter + values.get(9);
            default:
                return String.join(delimiter, values);
        }
    }

    /**
     * Converts a byte array into a hex string.
     *
     * <p>The hex string returned uses characters {@code 0123456789abcdef}, if uppercase {@code ABCDEF} is required the
     * returned string will need to be {@link String#toUpperCase() uppercased}.</p>
     *
     * <p>If {@code bytes} is null, null will be returned. If {@code bytes} was an empty array an empty string is
     * returned.</p>
     *
     * @param bytes The byte array to convert into a hex string.
     * @return A hex string representing the {@code bytes} that were passed, or null if {@code bytes} were null.
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return "";
        }

        // Hex uses 4 bits, converting a byte to hex will double its size.
        char[] hexString = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            // Convert the byte into an integer, masking all but the last 8 bits (the byte).
            int b = bytes[i] & 0xFF;

            // Shift 4 times to the right to get the leading 4 bits and get the corresponding hex character.
            hexString[i * 2] = LOWERCASE_HEX_CHARACTERS[b >>> 4];

            // Mask all but the last 4 bits and get the corresponding hex character.
            hexString[i * 2 + 1] = LOWERCASE_HEX_CHARACTERS[b & 0x0F];
        }

        return new String(hexString);
    }

    /**
     * Extracts the size from a {@code Content-Range} header.
     * <p>
     * The {@code Content-Range} header can take the following forms:
     *
     * <ul>
     * <li>{@code <unit> <start>-<end>/<size>}</li>
     * <li>{@code <unit> <start>-<end>/}&#42;</li>
     * <li>{@code <unit> }&#42;{@code /<size>}</li>
     * </ul>
     *
     * If the {@code <size>} is represented by &#42; this method will return -1.
     * <p>
     * If {@code contentRange} is null a {@link NullPointerException} will be thrown, if it doesn't contain a size
     * segment ({@code /<size>} or /&#42;) an {@link IllegalArgumentException} will be thrown.
     *
     * @param contentRange The {@code Content-Range} header to extract the size from.
     * @return The size contained in the {@code Content-Range}, or -1 if the size was &#42;.
     * @throws NullPointerException If {@code contentRange} is null.
     * @throws IllegalArgumentException If {@code contentRange} doesn't contain a {@code <size>} segment.
     * @throws NumberFormatException If the {@code <size>} segment of the {@code contentRange} isn't a valid number.
     */
    public static long extractSizeFromContentRange(String contentRange) {
        Objects.requireNonNull(contentRange, "Cannot extract length from null 'contentRange'.");
        int index = contentRange.indexOf('/');

        if (index == -1) {
            // No size segment.
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The Content-Range header wasn't properly "
                + "formatted and didn't contain a '/size' segment. The 'contentRange' was: " + contentRange));
        }

        String sizeString = contentRange.substring(index + 1).trim();
        if ("*".equals(sizeString)) {
            // Size unknown to the Content-Range header.
            return -1;
        }

        return Long.parseLong(sizeString);
    }

    /**
     * Utility method for parsing query parameters one-by-one without the use of string splitting.
     * <p>
     * This method provides an optimization over parsing query parameters with {@link String#split(String)} or a
     * {@link java.util.regex.Pattern} as it doesn't allocate any arrays to maintain values, instead it parses the query
     * parameters linearly.
     * <p>
     * Query parameter parsing works the following way, {@code key=value} will turn into an immutable {@link Map.Entry}
     * where the {@link Map.Entry#getKey()} is {@code key} and the {@link Map.Entry#getValue()} is {@code value}. For
     * query parameters without a value, {@code key=} or just {@code key}, the value will be an empty string.
     *
     * @param queryParameters The query parameter string.
     * @return An {@link Iterator} over the query parameter key-value pairs.
     */
    public static Iterator<Map.Entry<String, String>> parseQueryParameters(String queryParameters) {
        return (CoreUtils.isNullOrEmpty(queryParameters))
            ? Collections.emptyIterator()
            : new ImplUtils.QueryParameterIterator(queryParameters);
    }

    /**
     * Creates a type 4 (pseudo randomly generated) UUID.
     * <p>
     * The {@link UUID} is generated using a non-cryptographically strong pseudo random number generator.
     *
     * @return A randomly generated {@link UUID}.
     */
    public static UUID randomUuid() {
        return randomUuid(ThreadLocalRandom.current().nextLong(), ThreadLocalRandom.current().nextLong());
    }

    static UUID randomUuid(long msb, long lsb) {
        msb &= 0xffffffffffff0fffL; // Clear the UUID version.
        msb |= 0x0000000000004000L; // Set the UUID version to 4.
        lsb &= 0x3fffffffffffffffL; // Clear the variant.
        lsb |= 0x8000000000000000L; // Set the variant to IETF.

        // Use new UUID(long, long) instead of UUID.randomUUID as UUID.randomUUID may be blocking.
        // For environments using Reactor's BlockHound this will raise an exception if called in non-blocking threads.
        return new UUID(msb, lsb);
    }
}
