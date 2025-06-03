// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility class containing shared code for the SDK.
 */
public final class CoreUtils {
    private static final ClientLogger LOGGER = new ClientLogger(CoreUtils.class);

    private static final char[] LOWERCASE_HEX_CHARACTERS = "0123456789abcdef".toCharArray();

    // Used to check if the ISO8601 date time doesn't have a colon in the offset.
    private static final Pattern ISO8601_COLONLESS_OFFSET = Pattern.compile("([+-][0-9]{2})([0-9]{2})(?=\\[|$)");

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
     * Creates a copy of the source byte array.
     *
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static byte[] arrayCopy(byte[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }

    /**
     * Creates a copy of the source int array.
     *
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    public static int[] arrayCopy(int[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }

    /**
     * Creates a copy of the source array.
     *
     * @param source Array being copied.
     * @param <T> Generic representing the type of the source array.
     * @return A copy of the array or null if source was null.
     */
    public static <T> T[] arrayCopy(T[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
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
                return Collections.unmodifiableMap(properties.entrySet()
                    .stream()
                    .collect(Collectors.toMap(entry -> (String) entry.getKey(), entry -> (String) entry.getValue())));
            }
        } catch (IOException ex) {
            LOGGER.atWarning().setThrowable(ex).log("Failed to get properties from " + propertiesFileName);
        }

        return Collections.emptyMap();
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
            throw LOGGER.throwableAtError()
                .addKeyValue("Content-Range", contentRange)
                .log("The Content-Range header wasn't properly formatted and didn't contain a '/size' segment",
                    IllegalArgumentException::new);
        }

        String sizeString = contentRange.substring(index + 1).trim();
        if ("*".equals(sizeString)) {
            // Size unknown to the Content-Range header.
            return -1;
        }

        return Long.parseLong(sizeString);
    }

    /**
     * Parses a string into an {@link OffsetDateTime}.
     * <p>
     * If {@code dateString} is null, null will be returned.
     * <p>
     * This method attempts to parse the {@code dateString} using
     * {@link DateTimeFormatter#parseBest(CharSequence, TemporalQuery[])}. This will use
     * {@link OffsetDateTime#from(TemporalAccessor)} as the first attempt and will fall back to
     * {@link LocalDateTime#from(TemporalAccessor)} with setting the offset as {@link ZoneOffset#UTC}.
     *
     * @param dateString The string to parse into an {@link OffsetDateTime}.
     * @return The parsed {@link OffsetDateTime}, or null if {@code dateString} was null.
     * @throws DateTimeException If the {@code dateString} cannot be parsed by either
     * {@link OffsetDateTime#from(TemporalAccessor)} or {@link LocalDateTime#from(TemporalAccessor)}.
     */
    public static OffsetDateTime parseBestOffsetDateTime(String dateString) {
        if (dateString == null) {
            return null;
        }

        Matcher matcher = ISO8601_COLONLESS_OFFSET.matcher(dateString);
        if (matcher.find()) {
            dateString = dateString.substring(0, matcher.start()) + matcher.group(1) + ":" + matcher.group(2)
                + dateString.substring(matcher.start() + 5);
        }

        TemporalAccessor temporal
            = DateTimeFormatter.ISO_DATE_TIME.parseBest(dateString, OffsetDateTime::from, LocalDateTime::from);

        if (temporal.query(TemporalQueries.offset()) == null) {
            return LocalDateTime.from(temporal).atOffset(ZoneOffset.UTC);
        } else {
            return OffsetDateTime.from(temporal);
        }
    }

    /**
     * Helper method to create an instance of {@link ParameterizedType}.
     * @param rawType The raw type.
     * @param typeArguments The type arguments.
     * @return The instance of {@link ParameterizedType}.
     */
    public static ParameterizedType createParameterizedType(Type rawType, Type... typeArguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return typeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
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
     * Determines the serializer encoding to use based on the Content-Type header.
     *
     * @param headers the headers to get the Content-Type to check the encoding for.
     * @return the serializer encoding to use for the body. {@link SerializationFormat#JSON} if there is no Content-Type
     * header or an unrecognized Content-Type encoding is given.
     */
    public static SerializationFormat serializationFormatFromContentType(HttpHeaders headers) {
        if (headers == null) {
            return SerializationFormat.JSON;
        }

        String contentType = headers.getValue(HttpHeaderName.CONTENT_TYPE);
        if (CoreUtils.isNullOrEmpty(contentType)) {
            // When in doubt, JSON!
            return SerializationFormat.JSON;
        }

        int contentTypeEnd = contentType.indexOf(';');
        contentType = (contentTypeEnd == -1) ? contentType : contentType.substring(0, contentTypeEnd);
        SerializationFormat encoding = checkForKnownEncoding(contentType);
        if (encoding != null) {
            return encoding;
        }

        int contentTypeTypeSplit = contentType.indexOf('/');
        if (contentTypeTypeSplit == -1) {
            return SerializationFormat.JSON;
        }

        // Check the suffix if it does not match the full types.
        // Suffixes are defined by the Structured Syntax Suffix Registry
        // https://www.rfc-editor.org/rfc/rfc6839
        final String subtype = contentType.substring(contentTypeTypeSplit + 1);
        final int lastIndex = subtype.lastIndexOf('+');
        if (lastIndex == -1) {
            return SerializationFormat.JSON;
        }

        // Only XML and JSON are supported suffixes, there is no suffix for TEXT.
        final String mimeTypeSuffix = subtype.substring(lastIndex + 1);
        if ("xml".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.XML;
        } else if ("json".equalsIgnoreCase(mimeTypeSuffix)) {
            return SerializationFormat.JSON;
        }

        return SerializationFormat.JSON;
    }

    /*
     * There is a limited set of serialization encodings that are known ahead of time. Instead of using a TreeMap with
     * a case-insensitive comparator, use an optimized search specifically for the known encodings.
     */
    private static SerializationFormat checkForKnownEncoding(String contentType) {
        int length = contentType.length();

        // Check the length of the content type first as it is a quick check.
        if (length != 8 && length != 9 && length != 10 && length != 15 && length != 16) {
            return null;
        }

        if ("text/".regionMatches(true, 0, contentType, 0, 5)) {
            if (length == 8) {
                if ("xml".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.XML;
                } else if ("csv".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                } else if ("css".regionMatches(true, 0, contentType, 5, 3)) {
                    return SerializationFormat.TEXT;
                }
            } else if (length == 9 && "html".regionMatches(true, 0, contentType, 5, 4)) {
                return SerializationFormat.TEXT;
            } else if (length == 10 && "plain".regionMatches(true, 0, contentType, 5, 5)) {
                return SerializationFormat.TEXT;
            } else if (length == 15 && "javascript".regionMatches(true, 0, contentType, 5, 10)) {
                return SerializationFormat.TEXT;
            }
        } else if ("application/".regionMatches(true, 0, contentType, 0, 12)) {
            if (length == 16 && "json".regionMatches(true, 0, contentType, 12, 4)) {
                return SerializationFormat.JSON;
            } else if (length == 15 && "xml".regionMatches(true, 0, contentType, 12, 3)) {
                return SerializationFormat.XML;
            }
        }

        return null;
    }

    /**
     * Decodes the body of an {@link Response} into the type returned by the called API.
     * @param data The BinaryData to decode.
     * @param serializer The serializer to use.
     * @param returnType The type of the ParameterizedType return value.
     * @param <T> The decoded value type.
     * @return The decoded value.
     * @throws CoreException If the deserialization fails.
     */
    public static <T> T decodeNetworkResponse(BinaryData data, ObjectSerializer serializer,
        ParameterizedType returnType) {
        if (data == null) {
            return null;
        }
        try {
            if (List.class.isAssignableFrom((Class<?>) returnType.getRawType())) {
                return serializer.deserializeFromBytes(data.toBytes(), returnType);
            }
            Type token = returnType.getRawType();
            if (Response.class.isAssignableFrom((Class<?>) token)) {
                token = returnType.getActualTypeArguments()[0];
            }
            return serializer.deserializeFromBytes(data.toBytes(), token);
        } catch (IOException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    private CoreUtils() {
    }
}
