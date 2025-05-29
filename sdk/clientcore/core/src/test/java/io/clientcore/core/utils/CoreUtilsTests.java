// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.utils;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.Person;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.SerializationFormat;
import io.clientcore.core.serialization.json.JsonSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static io.clientcore.core.utils.CoreUtils.serializationFormatFromContentType;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CoreUtils}.
 */
public class CoreUtilsTests {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final JsonSerializer SERIALIZER = new JsonSerializer();
    private static final ParameterizedType PLAIN_OBJECT = CoreUtils.createParameterizedType(Person.class);
    private static final ParameterizedType PLAIN_LIST_OBJECT
        = CoreUtils.createParameterizedType(List.class, Person.class);

    @ParameterizedTest
    @MethodSource("arrayIsNullOrEmptySupplier")
    public void arrayIsNullOrEmpty(Object[] array, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(array));
    }

    private static Stream<Arguments> arrayIsNullOrEmptySupplier() {
        return Stream.of(Arguments.of(null, true), Arguments.of(new Object[0], true),
            Arguments.of(new Object[] { 1 }, false));
    }

    @ParameterizedTest
    @MethodSource("collectionIsNullOrEmptySupplier")
    public void collectionIsNullOrEmpty(Collection<?> collection, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(collection));
    }

    private static Stream<Arguments> collectionIsNullOrEmptySupplier() {
        return Stream.of(Arguments.of(null, true), Arguments.of(new ArrayList<>(), true),
            Arguments.of(singletonList(1), false));
    }

    @ParameterizedTest
    @MethodSource("mapIsNullOrEmptySupplier")
    public void mapIsNullOrEmpty(Map<?, ?> map, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(map));
    }

    private static Stream<Arguments> mapIsNullOrEmptySupplier() {
        return Stream.of(Arguments.of(null, true), Arguments.of(new HashMap<>(), true),
            Arguments.of(singletonMap("key", "value"), false));
    }

    @ParameterizedTest
    @MethodSource("stringIsNullOrEmptySupplier")
    public void stringIsNullOrEmpty(String string, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(string));
    }

    private static Stream<Arguments> stringIsNullOrEmptySupplier() {
        return Stream.of(Arguments.of(null, true), Arguments.of("", true), Arguments.of(" ", false),
            Arguments.of("foo", false));
    }

    @ParameterizedTest
    @MethodSource("byteArrayArrayCopySupplier")
    public void byteArrayArrayCopy(byte[] byteArray) {
        assertArrayEquals(byteArray, CoreUtils.arrayCopy(byteArray));
    }

    private static Stream<byte[]> byteArrayArrayCopySupplier() {
        return Stream.of(null, new byte[0], "1234567890".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testProperties() {
        assertNotNull(CoreUtils.getProperties("core.properties").get("version"));
        assertNotNull(CoreUtils.getProperties("core.properties").get("name"));
        assertTrue(
            CoreUtils.getProperties("core.properties").get("version").matches("\\d+\\.\\d+\\.\\d+(-beta\\.\\d+)?"));
    }

    @Test
    public void testMissingProperties() {
        assertNotNull(CoreUtils.getProperties("foo.properties"));
        assertTrue(CoreUtils.getProperties("foo.properties").isEmpty());
        assertNull(CoreUtils.getProperties("azure-core.properties").get("foo"));
    }

    @ParameterizedTest
    @MethodSource("arrayCopyIntArraySupplier")
    public void arrayCopyIntArray(int[] intArray, int[] expected) {
        assertArrayEquals(expected, CoreUtils.arrayCopy(intArray));
    }

    private static Stream<Arguments> arrayCopyIntArraySupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(new int[0], new int[0]),
            Arguments.of(new int[] { 1, 2, 3 }, new int[] { 1, 2, 3 }));
    }

    @ParameterizedTest
    @MethodSource("arrayCopyGenericArraySupplier")
    public <T> void arrayCopyGenericArray(T[] genericArray, T[] expected) {
        assertArrayEquals(expected, CoreUtils.arrayCopy(genericArray));
    }

    private static Stream<Arguments> arrayCopyGenericArraySupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(new String[0], new String[0]),
            Arguments.of(new String[] { "1", "2", "3" }, new String[] { "1", "2", "3" }));
    }

    @ParameterizedTest
    @MethodSource("isNullOrEmptyCollectionSupplier")
    public void isNullOrEmptyCollection(Collection<?> collection, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(collection));
    }

    private static Stream<Arguments> isNullOrEmptyCollectionSupplier() {
        return Stream.of(Arguments.of(null, true), Arguments.of(new ArrayList<>(), true),
            Arguments.of(singletonList(1), false));
    }

    @ParameterizedTest
    @MethodSource("bytesToHexSupplier")
    public void bytesToHex(byte[] bytes, String expectedHex) {
        assertEquals(expectedHex, CoreUtils.bytesToHexString(bytes));
    }

    private static Stream<Arguments> bytesToHexSupplier() {
        return Stream.of(Arguments.of(null, null), Arguments.of(new byte[0], ""),
            Arguments.of("1234567890".getBytes(StandardCharsets.UTF_8), "31323334353637383930"));
    }

    @ParameterizedTest
    @MethodSource("contentRangeSizeExtractionSupplier")
    public void contentRangeSizeExtraction(String contentRange, long expectedSize) {
        assertEquals(expectedSize, CoreUtils.extractSizeFromContentRange(contentRange));
    }

    private static Stream<Arguments> contentRangeSizeExtractionSupplier() {
        return Stream.of(Arguments.of("0-1023/1024", 1024), Arguments.of("0-1023/*", -1));
    }

    @ParameterizedTest
    @MethodSource("invalidContentRangeSizeExtractionSupplier")
    public void invalidContentRangeSizeExtraction(String contentRange, Class<Throwable> expectedException) {
        assertThrows(expectedException, () -> CoreUtils.extractSizeFromContentRange(contentRange));
    }

    private static Stream<Arguments> invalidContentRangeSizeExtractionSupplier() {
        return Stream.of(Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class),
            Arguments.of("0-1023/notanumber", NumberFormatException.class));
    }

    @Test
    public void randomUuidIsCorrectlyType4() {
        long msb = RANDOM.nextLong();
        long lsb = RANDOM.nextLong();

        byte[] bytes = new byte[16];

        long msbToBytes = msb;
        long lsbToBytes = lsb;
        for (int i = 15; i >= 8; i--) {
            bytes[i] = (byte) (lsbToBytes & 0xff);
            lsbToBytes >>= 8;
        }
        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (msbToBytes & 0xff);
            msbToBytes >>= 8;
        }

        // Generate type 4 UUID using Java's built-in handling.
        bytes[6] &= 0x0f; /* clear version */
        bytes[6] |= 0x40; /* set to version 4 */
        bytes[8] &= 0x3f; /* clear variant */
        bytes[8] |= (byte) 0x80; /* set to IETF variant */
        long msbForJava = 0;
        long lsbForJava = 0;
        for (int i = 0; i < 8; i++) {
            msbForJava = (msbForJava << 8) | (bytes[i] & 0xff);
        }
        for (int i = 8; i < 16; i++) {
            lsbForJava = (lsbForJava << 8) | (bytes[i] & 0xff);
        }

        assertEquals(new UUID(msbForJava, lsbForJava), CoreUtils.randomUuid(msb, lsb));
    }

    @Test
    public void futureCompletesBeforeTimeout() {
        try {
            AtomicBoolean completed = new AtomicBoolean(false);
            Future<?> future = SharedExecutorService.getInstance().submit(() -> {
                Thread.sleep(10);
                completed.set(true);
                return null;
            });

            future.get(5000, TimeUnit.MILLISECONDS);

            assertTrue(completed.get());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest
    @MethodSource("parseBestOffsetDateTimeSupplier")
    public void parseBestOffsetDateTime(String dateTime, OffsetDateTime expected) {
        assertEquals(expected, CoreUtils.parseBestOffsetDateTime(dateTime));
    }

    private static Stream<Arguments> parseBestOffsetDateTimeSupplier() {
        return Stream.of(Arguments.of(null, null),
            Arguments.of("2023-09-26T18:32:05Z", OffsetDateTime.of(2023, 9, 26, 18, 32, 5, 0, ZoneOffset.UTC)),
            Arguments.of("2023-09-26T18:32:05+00:00", OffsetDateTime.of(2023, 9, 26, 18, 32, 5, 0, ZoneOffset.UTC)),
            Arguments.of("2023-09-26T18:32:05+0000", OffsetDateTime.of(2023, 9, 26, 18, 32, 5, 0, ZoneOffset.UTC)),
            Arguments.of("2023-09-26T18:32:05", OffsetDateTime.of(2023, 9, 26, 18, 32, 5, 0, ZoneOffset.UTC)));
    }

    @Test
    public void parseBestNoColonInTimezoneOffset() {
        OffsetDateTime parsed = CoreUtils.parseBestOffsetDateTime("2023-09-26T18:32:05+0000");
        assertEquals(2023, parsed.getYear());
        assertEquals(9, parsed.getMonthValue());
        assertEquals(26, parsed.getDayOfMonth());
        assertEquals(18, parsed.getHour());
        assertEquals(32, parsed.getMinute());
        assertEquals(5, parsed.getSecond());
        assertEquals(0, parsed.getOffset().getTotalSeconds());
    }

    @ParameterizedTest
    @MethodSource("stringJoinSupplier")
    public void stringJoin(String delimiter, List<String> strings, String expected) {
        assertEquals(expected, CoreUtils.stringJoin(delimiter, strings));
    }

    @ParameterizedTest
    @CsvSource({
        "null, JSON",
        "application/json, JSON",
        "application/xml, XML",
        "text/plain, TEXT",
        "text/html, TEXT",
        "application/json; charset=utf-8, JSON",
        "application/unknown, JSON",
        "application/vnd.example+xml, XML",
        "application/vnd.example+json, JSON" })
    public void testSerializationFormatFromContentType(String contentType, SerializationFormat expected) {
        HttpHeaders headers
            = (contentType == null) ? null : new HttpHeaders().set(HttpHeaderName.CONTENT_TYPE, contentType);
        assertEquals(expected, serializationFormatFromContentType(headers));
    }

    @Test
    void decodeNetworkResponseReturnsNullIfDataIsNull() {
        assertNull(CoreUtils.decodeNetworkResponse(null, SERIALIZER, PLAIN_OBJECT));
    }

    @Test
    void decodeNetworkResponseListType() {
        String json = "[{\"name\":\"A\",\"age\":0},{\"name\":\"B\",\"age\":0}]";
        BinaryData data = BinaryData.fromString(json);
        Object result = CoreUtils.decodeNetworkResponse(data, SERIALIZER, PLAIN_LIST_OBJECT);
        assertEquals(Arrays.asList(new Person().setName("A"), new Person().setName("B")), result);
    }

    @Test
    void decodeNetworkResponsePlainType() {
        String json = "{\"name\":\"A\",\"age\":0}";
        BinaryData data = BinaryData.fromString(json);
        Object result = CoreUtils.decodeNetworkResponse(data, SERIALIZER, PLAIN_OBJECT);
        assertEquals(new Person().setName("A"), result);
    }

    @SuppressWarnings("unchecked")
    @Test
    void decodeNetworkResponseThrowsCoreException() {
        // Custom serializer that always throws IOException
        JsonSerializer serializer = new ThrowingJsonSerializer();
        BinaryData data = BinaryData.fromBytes(new byte[] { 99 }); // Data is irrelevant here
        CoreException ex
            = assertThrows(CoreException.class, () -> CoreUtils.decodeNetworkResponse(data, serializer, PLAIN_OBJECT));
        assertTrue(ex.getCause() instanceof IOException);
        assertEquals("Unknown data", ex.getCause().getMessage());
    }

    private static class ThrowingJsonSerializer extends JsonSerializer {
        @SuppressWarnings("unchecked")
        @Override
        public <T> T deserializeFromBytes(byte[] data, Type type) throws IOException {
            throw new IOException("Unknown data");
        }
    }

    private static Stream<Arguments> stringJoinSupplier() {
        return Stream.of(Arguments.of(",", new ArrayList<>(), ""), Arguments.of(",", singletonList("red"), "red"),
            Arguments.of(",", Arrays.asList("red", "blue"), "red,blue"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow"), "red,blue,yellow"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green"), "red,blue,yellow,green"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green", "orange"),
                "red,blue,yellow,green,orange"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green", "orange", "purple"),
                "red,blue,yellow,green,orange,purple"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green", "orange", "purple", "brown"),
                "red,blue,yellow,green,orange,purple,brown"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green", "orange", "purple", "brown", "indigo"),
                "red,blue,yellow,green,orange,purple,brown,indigo"),
            Arguments.of(",",
                Arrays.asList("red", "blue", "yellow", "green", "orange", "purple", "brown", "indigo", "violet"),
                "red,blue,yellow,green,orange,purple,brown,indigo,violet"),
            Arguments.of(",",
                Arrays.asList("red", "blue", "yellow", "green", "orange", "purple", "brown", "indigo", "violet",
                    "black"),
                "red,blue,yellow,green,orange,purple,brown,indigo,violet,black"),
            Arguments.of(",", Arrays.asList("red", "blue", "yellow", "green", "orange", "purple", "brown", "indigo",
                "violet", "black", "white"), "red,blue,yellow,green,orange,purple,brown,indigo,violet,black,white"));
    }
}
