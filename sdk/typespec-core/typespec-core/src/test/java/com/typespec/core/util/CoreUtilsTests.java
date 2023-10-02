// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.http.policy.HttpLogOptions;
import com.typespec.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoreUtilsTests {
    private static final byte[] BYTES = "Hello world!".getBytes(StandardCharsets.UTF_8);

    private static final byte[] UTF_8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final byte[] UTF_16BE_BOM = {(byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_16LE_BOM = {(byte) 0xFF, (byte) 0xFE};
    private static final byte[] UTF_32BE_BOM = {(byte) 0x00, (byte) 0x00, (byte) 0xFE, (byte) 0xFF};
    private static final byte[] UTF_32LE_BOM = {(byte) 0xFF, (byte) 0xFE, (byte) 0x00, (byte) 0x00};

    private static final String TIMEOUT_PROPERTY_NAME = "TIMEOUT_PROPERTY_NAME";
    private static final ConfigurationSource EMPTY_SOURCE = new TestConfigurationSource();

    @Test
    public void findFirstOfTypeEmptyArgs() {
        assertNull(CoreUtils.findFirstOfType(null, Integer.class));
    }

    @Test
    public void findFirstOfTypeWithOneOfType() {
        int expected = 1;
        Object[] args = {"string", expected};
        Integer actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithMultipleOfType() {
        int expected = 1;
        Object[] args = {"string", expected, 10};
        Integer actual = CoreUtils.findFirstOfType(args, Integer.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void findFirstOfTypeWithNoneOfType() {
        Object[] args = {"string", "anotherString"};
        assertNull(CoreUtils.findFirstOfType(args, Integer.class));
    }

    @Test
    public void testProperties() {
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("version"));
        assertNotNull(CoreUtils.getProperties("azure-core.properties").get("name"));
        assertTrue(CoreUtils.getProperties("azure-core.properties").get("version")
            .matches("\\d+\\.\\d+\\.\\d+(-beta\\.\\d+)?"));
    }

    @Test
    public void testMissingProperties() {
        assertNotNull(CoreUtils.getProperties("foo.properties"));
        assertTrue(CoreUtils.getProperties("foo.properties").isEmpty());
        assertNull(CoreUtils.getProperties("azure-core.properties").get("foo"));
    }

    @ParameterizedTest
    @MethodSource("cloneIntArraySupplier")
    public void cloneIntArray(int[] intArray, int[] expected) {
        assertArrayEquals(expected, CoreUtils.clone(intArray));
    }

    private static Stream<Arguments> cloneIntArraySupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(new int[0], new int[0]),
            Arguments.of(new int[]{1, 2, 3}, new int[]{1, 2, 3})
        );
    }

    @ParameterizedTest
    @MethodSource("cloneGenericArraySupplier")
    public <T> void cloneGenericArray(T[] genericArray, T[] expected) {
        assertArrayEquals(expected, CoreUtils.clone(genericArray));
    }

    private static Stream<Arguments> cloneGenericArraySupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(new String[0], new String[0]),
            Arguments.of(new String[]{"1", "2", "3"}, new String[]{"1", "2", "3"})
        );
    }

    @ParameterizedTest
    @MethodSource("isNullOrEmptyCollectionSupplier")
    public void isNullOrEmptyCollection(Collection<?> collection, boolean expected) {
        assertEquals(expected, CoreUtils.isNullOrEmpty(collection));
    }

    private static Stream<Arguments> isNullOrEmptyCollectionSupplier() {
        return Stream.of(
            Arguments.of(null, true),
            Arguments.of(new ArrayList<>(), true),
            Arguments.of(Collections.singletonList(1), false)
        );
    }

    @ParameterizedTest
    @MethodSource("arrayToStringSupplier")
    public <T> void arrayToString(T[] array, Function<T, String> mapper, String expected) {
        assertEquals(expected, CoreUtils.arrayToString(array, mapper));
    }

    private static Stream<Arguments> arrayToStringSupplier() {
        Function<?, String> toStringFunction = String::valueOf;

        return Stream.of(
            Arguments.of(null, null, null),
            Arguments.of(new String[0], toStringFunction, null),
            Arguments.of(new String[]{""}, toStringFunction, ""),
            Arguments.of(new String[]{"Hello world!"}, toStringFunction, "Hello world!"),
            Arguments.of(new String[]{"1", "2", "3"}, toStringFunction, "1,2,3")
        );
    }

    @ParameterizedTest
    @MethodSource("bomAwareToStringSupplier")
    public void bomAwareToString(byte[] bytes, String contentType, String expected) {
        assertEquals(expected, CoreUtils.bomAwareToString(bytes, contentType));
    }

    private static Stream<Arguments> bomAwareToStringSupplier() {
        return Stream.of(
            Arguments.arguments(null, null, null),
            Arguments.arguments(BYTES, null, new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(BYTES, "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_16BE)),
            Arguments.arguments(BYTES, "charset=invalid", new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_8_BOM), null, new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_16BE_BOM), null, new String(BYTES, StandardCharsets.UTF_16BE)),
            Arguments.arguments(addBom(UTF_16LE_BOM), null, new String(BYTES, StandardCharsets.UTF_16LE)),
            Arguments.arguments(addBom(UTF_32BE_BOM), null, new String(BYTES, Charset.forName("UTF-32BE"))),
            Arguments.arguments(addBom(UTF_32LE_BOM), null, new String(BYTES, Charset.forName("UTF-32LE"))),
            Arguments.arguments(addBom(UTF_8_BOM), "charset=UTF-8", new String(BYTES, StandardCharsets.UTF_8)),
            Arguments.arguments(addBom(UTF_8_BOM), "charset=UTF-16BE", new String(BYTES, StandardCharsets.UTF_8))
        );
    }

    private static byte[] addBom(byte[] arr1) {
        byte[] mergedArray = new byte[arr1.length + BYTES.length];

        System.arraycopy(arr1, 0, mergedArray, 0, arr1.length);
        System.arraycopy(BYTES, 0, mergedArray, arr1.length, BYTES.length);

        return mergedArray;
    }

    @ParameterizedTest
    @MethodSource("getApplicationIdSupplier")
    public void getApplicationId(ClientOptions clientOptions, HttpLogOptions logOptions, String expected) {
        assertEquals(expected, CoreUtils.getApplicationId(clientOptions, logOptions));
    }

    @SuppressWarnings("deprecation")
    private static Stream<Arguments> getApplicationIdSupplier() {
        String clientOptionApplicationId = "clientOptions";
        String logOptionsApplicationId = "logOptions";

        ClientOptions clientOptionsWithApplicationId = new ClientOptions().setApplicationId(clientOptionApplicationId);
        ClientOptions clientOptionsWithoutApplicationId = new ClientOptions();

        HttpLogOptions logOptionsWithApplicationId = new HttpLogOptions().setApplicationId(logOptionsApplicationId);
        HttpLogOptions logOptionsWithoutApplicationId = new HttpLogOptions();

        return Stream.of(
            Arguments.of(clientOptionsWithApplicationId, logOptionsWithApplicationId, clientOptionApplicationId),
            Arguments.of(clientOptionsWithApplicationId, logOptionsWithoutApplicationId, clientOptionApplicationId),
            Arguments.of(clientOptionsWithApplicationId, null, clientOptionApplicationId),
            Arguments.of(clientOptionsWithoutApplicationId, logOptionsWithApplicationId, logOptionsApplicationId),
            Arguments.of(clientOptionsWithoutApplicationId, logOptionsWithoutApplicationId, null),
            Arguments.of(clientOptionsWithoutApplicationId, null, null),
            Arguments.of(null, logOptionsWithApplicationId, logOptionsApplicationId),
            Arguments.of(null, logOptionsWithoutApplicationId, null),
            Arguments.of(null, null, null)
        );
    }

    @ParameterizedTest
    @MethodSource("createHttpHeadersFromClientOptionsSupplier")
    public void createHttpHeadersFromClientOptions(ClientOptions clientOptions, HttpHeaders expected) {
        HttpHeaders actual = CoreUtils.createHttpHeadersFromClientOptions(clientOptions);
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected.toMap(), actual.toMap());
        }
    }

    private static Stream<Arguments> createHttpHeadersFromClientOptionsSupplier() {
        List<Header> multipleHeadersList = new ArrayList<>();
        multipleHeadersList.add(new Header("a", "header"));
        multipleHeadersList.add(new Header("another", "headerValue"));

        Map<String, String> multipleHeadersMap = new HashMap<>();
        multipleHeadersMap.put("a", "header");
        multipleHeadersMap.put("another", "headerValue");

        return Stream.of(
            // ClientOptions is null, null is returned.
            Arguments.of(null, null),

            // ClientOptions doesn't contain Header values, null is returned.
            Arguments.of(new ClientOptions(), null),

            // ClientOptions contains a single header value, a single header HttpHeaders is returned.
            Arguments.of(new ClientOptions().setHeaders(Collections.singletonList(new Header("a", "header"))),
                new HttpHeaders(Collections.singletonMap("a", "header"))),

            // ClientOptions contains multiple header values, a multi-header HttpHeaders is returned.
            Arguments.of(new ClientOptions().setHeaders(multipleHeadersList), new HttpHeaders(multipleHeadersMap))
        );
    }

    @ParameterizedTest
    @MethodSource("getDefaultTimeoutFromEnvironmentSupplier")
    public void getDefaultTimeoutFromEnvironmentTests(Configuration configuration, Duration defaultTimeout,
        ClientLogger logger, Duration expectedTimeout) {
        assertEquals(expectedTimeout, CoreUtils.getDefaultTimeoutFromEnvironment(configuration, TIMEOUT_PROPERTY_NAME,
            defaultTimeout, logger));
    }

    private static Stream<Arguments> getDefaultTimeoutFromEnvironmentSupplier() {
        ClientLogger logger = new ClientLogger(CoreUtilsTests.class);

        return Stream.of(
            // Configuration doesn't have the timeout property configured.
            Arguments.of(Configuration.NONE, Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has an empty string timeout property configured.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
                    .put(TIMEOUT_PROPERTY_NAME, ""))
                    .build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a value that isn't a valid number.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
                    .put(TIMEOUT_PROPERTY_NAME, "ten"))
                    .build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(10000)),

            // Configuration has a negative value.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
                    .put(TIMEOUT_PROPERTY_NAME, "-10"))
                    .build(),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a zero value.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
                    .put(TIMEOUT_PROPERTY_NAME, "0"))
                    .build(),
                Duration.ofMillis(10000), logger, Duration.ZERO),

            // Configuration has a positive value.
            Arguments.of(new ConfigurationBuilder(EMPTY_SOURCE, EMPTY_SOURCE, new TestConfigurationSource()
                    .put(TIMEOUT_PROPERTY_NAME, "42"))
                    .build(),
                Duration.ofMillis(10000), logger, Duration.ofMillis(42))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContextMergeSupplier")
    public void invalidContextMerge(Context into, Context from) {
        assertThrows(NullPointerException.class, () -> CoreUtils.mergeContexts(into, from));
    }

    private static Stream<Arguments> invalidContextMergeSupplier() {
        return Stream.of(
            Arguments.of(null, Context.NONE),
            Arguments.of(Context.NONE, null)
        );
    }

    @Test
    public void mergingContextNoneReturnsIntoContext() {
        Context into = new Context("key", "value");

        Context merged = CoreUtils.mergeContexts(into, Context.NONE);
        assertEquals(into, merged);
    }

    @Test
    public void mergingReturnsTheExpectedResult() {
        List<Context> expectedMergedContextChain = new ArrayList<>();
        Context into = new Context("key1", "value1");
        expectedMergedContextChain.add(into);

        into = into.addData("key2", "value2");
        expectedMergedContextChain.add(into);

        into = into.addData("key3", "value3");
        expectedMergedContextChain.add(into);

        Context from = new Context("key4", "value4");
        expectedMergedContextChain.add(from);

        from = from.addData("key5", "value5");
        expectedMergedContextChain.add(from);

        from = from.addData("key6", "value6");
        expectedMergedContextChain.add(from);

        Context merged = CoreUtils.mergeContexts(into, from);
        Context[] mergedContextChain = merged.getContextChain();

        assertEquals(expectedMergedContextChain.size(), mergedContextChain.length);
        for (int i = 0; i < expectedMergedContextChain.size(); i++) {
            Context expected = expectedMergedContextChain.get(i);
            Context actual = mergedContextChain[i];

            assertEquals(expected.getKey(), actual.getKey());
            assertEquals(expected.getValue(), actual.getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("bytesToHexSupplier")
    public void bytesToHex(byte[] bytes, String expectedHex) {
        assertEquals(expectedHex, CoreUtils.bytesToHexString(bytes));
    }

    private static Stream<Arguments> bytesToHexSupplier() {
        return Stream.of(
            Arguments.of(null, null),
            Arguments.of(new byte[0], ""),
            Arguments.of("1234567890".getBytes(StandardCharsets.UTF_8), "31323334353637383930")
        );
    }

    @ParameterizedTest
    @MethodSource("contentRangeSizeExtractionSupplier")
    public void contentRangeSizeExtraction(String contentRange, long expectedSize) {
        assertEquals(expectedSize, CoreUtils.extractSizeFromContentRange(contentRange));
    }

    private static Stream<Arguments> contentRangeSizeExtractionSupplier() {
        return Stream.of(
            Arguments.of("0-1023/1024", 1024),
            Arguments.of("0-1023/*", -1)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidContentRangeSizeExtractionSupplier")
    public void invalidContentRangeSizeExtraction(String contentRange, Class<Throwable> expectedException) {
        assertThrows(expectedException, () -> CoreUtils.extractSizeFromContentRange(contentRange));
    }

    private static Stream<Arguments> invalidContentRangeSizeExtractionSupplier() {
        return Stream.of(
            Arguments.of(null, NullPointerException.class),
            Arguments.of("", IllegalArgumentException.class),
            Arguments.of("0-1023/notanumber", NumberFormatException.class)
        );
    }

    @Test
    public void parseNullQueryParameters() {
        assertFalse(CoreUtils.parseQueryParameters(null).hasNext());
    }

    @Test
    public void parseEmptyQueryParameters() {
        assertFalse(CoreUtils.parseQueryParameters("").hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=value", "?key=value"})
    public void parseSimpleQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=", "?key="})
    public void parseSimpleEmptyValueQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"key", "?key"})
    public void parseSimpleKeyOnlyQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=value&key2=", "key=value&key2", "?key=value&key2=", "?key=value&key2"})
    public void parseQueryParameterLastParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {"key=&key2=value2", "key&key2=value2", "?key=&key2=value2", "?key&key2=value2"})
    public void parseQueryParameterFirstParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", "value2"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "key=value&key2=&key3=value3", "?key=value&key2=&key3=value3",
        "key=value&key2&key3=value3", "?key=value&key2&key3=value3",
    })
    public void parseQueryParameterMiddleParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = CoreUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key3", "value3"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void randomUuidIsCorrectlyType4() {
        long msb = ThreadLocalRandom.current().nextLong();
        long lsb = ThreadLocalRandom.current().nextLong();

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
        bytes[6] &= 0x0f;  /* clear version        */
        bytes[6] |= 0x40;  /* set to version 4     */
        bytes[8] &= 0x3f;  /* clear variant        */
        bytes[8] |= 0x80;  /* set to IETF variant  */
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
}
