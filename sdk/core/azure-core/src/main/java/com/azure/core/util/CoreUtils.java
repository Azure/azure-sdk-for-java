// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.ContentType;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import org.reactivestreams.Publisher;
import org.xml.sax.InputSource;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

/**
 * This class contains utility methods useful for building client libraries.
 */
public final class CoreUtils {
    private static final String COMMA = ",";

    private CoreUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Creates a copy of the source byte array.
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
     * @param array Array being checked for nullness or emptiness.
     * @return True if the array is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Object[] array) {
        return array == null || array.length == 0;
    }

    /**
     * Checks if the collection is null or empty.
     * @param collection Collection being checked for nullness or emptiness.
     * @return True if the collection is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Checks if the map is null or empty.
     * @param map Map being checked for nullness or emptiness.
     * @return True if the map is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /**
     * Checks if the character sequence is null or empty.
     * @param charSequence Character sequence being checked for nullness or emptiness.
     * @return True if the character sequence is null or empty, false otherwise.
     */
    public static boolean isNullOrEmpty(CharSequence charSequence) {
        return charSequence == null || charSequence.length() == 0;
    }

    /**
     * Turns an array into a string mapping each element to a string and delimits them using a coma.
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
            return Flux.fromIterable(page.getItems());
        }
        return Flux.fromIterable(page.getItems()).concatWith(content.apply(nextPageLink, context));
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
     * Pretty print the json or xml string content. If the content is not in a format of json or xml,
     * return original string.
     *
     * @param content The body content which need to parse.
     * @return Pretty json or xml format of the content. If it is not in a format of json or xml, returns original one.
     */
    public static String printPrettyFormatJsonOrXml(String content, String contentType) {
        if (content == null || contentType == null) {
            return content;
        }

        ClientLogger logger = new ClientLogger(CoreUtils.class);
        if (contentType.startsWith(ContentType.APPLICATION_JSON)) {
            try {
                ObjectMapper prettyPrinter = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
                final Object deserialized = prettyPrinter.readTree(content);
                return prettyPrinter.writeValueAsString(deserialized);
            } catch (Exception e) {
                logger.warning("Failed to pretty print JSON: {}", e.getMessage());
            }
        } else if (contentType.startsWith(ContentType.APPLICATION_XML)) {
            try {
                Transformer serializer = SAXTransformerFactory.newInstance().newTransformer();
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                Source xmlSource = new SAXSource(new InputSource(new ByteArrayInputStream(
                    content.getBytes(StandardCharsets.UTF_8))));
                StreamResult res =  new StreamResult(new ByteArrayOutputStream());
                serializer.transform(xmlSource, res);
                return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                logger.warning("Failed to pretty print XML: {}", e.getMessage());
            }
        }
        return content;
    }
}
