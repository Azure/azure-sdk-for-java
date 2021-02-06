// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import com.azure.core.http.HttpHeader;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link NettyToAzureCoreHttpHeadersWrapper}
 */
public class NettyToAzureCoreHttpHeadersWrapperTests {
    @ParameterizedTest
    @MethodSource("getValueSupplier")
    public void getValue(HttpHeaders nettyHeaders, String key, String expected) {
        assertEquals(expected, createHeaderWrapper(nettyHeaders).getValue(key));
    }

    @ParameterizedTest
    @MethodSource("getValuesSupplier")
    public void getValues(HttpHeaders nettyHeaders, String key, String[] expected) {
        assertArrayEquals(expected, createHeaderWrapper(nettyHeaders).getValues(key));
    }

    @ParameterizedTest
    @MethodSource("getHeaderValueSupplier")
    public void getHeaderValue(HttpHeaders nettyHeaders, String key, String expected) {
        NettyToAzureCoreHttpHeadersWrapper headerWrapper = createHeaderWrapper(nettyHeaders);
        if (expected == null) {
            assertNull(headerWrapper.get(key));
        } else {
            assertEquals(expected, headerWrapper.get(key).getValue());
        }
    }

    @ParameterizedTest
    @MethodSource("getHeaderValuesSupplier")
    public void getHeaderValues(HttpHeaders nettyHeaders, String key, String[] expected) {
        NettyToAzureCoreHttpHeadersWrapper headerWrapper = createHeaderWrapper(nettyHeaders);
        if (expected == null) {
            assertNull(headerWrapper.get(key));
        } else {
            assertArrayEquals(expected, headerWrapper.get(key).getValues());
        }
    }

    @ParameterizedTest
    @MethodSource("getHeaderValuesListSupplier")
    public void getHeaderValuesList(HttpHeaders nettyHeaders, String key, List<String> expected) {
        NettyToAzureCoreHttpHeadersWrapper headerWrapper = createHeaderWrapper(nettyHeaders);
        if (expected == null) {
            assertNull(headerWrapper.get(key));
        } else {
            assertLinesMatch(expected, headerWrapper.get(key).getValuesList());
        }
    }

    private NettyToAzureCoreHttpHeadersWrapper createHeaderWrapper(HttpHeaders nettyHeaders) {
        return new NettyToAzureCoreHttpHeadersWrapper(nettyHeaders);
    }


    private static Stream<Arguments> getValueSupplier() {
        return getSupplierBase(value -> value);
    }

    private static Stream<Arguments> getValuesSupplier() {
        return getSupplierBase(stringValue -> {
            if (stringValue == null) {
                return null;
            } else {
                return stringValue.split(",");
            }
        });
    }

    private static Stream<Arguments> getHeaderValueSupplier() {
        return getSupplierBase(value -> value);
    }

    private static Stream<Arguments> getHeaderValuesSupplier() {
        return getSupplierBase(stringValue -> {
            if (stringValue == null) {
                return null;
            } else {
                return stringValue.split(",");
            }
        });
    }

    private static Stream<Arguments> getHeaderValuesListSupplier() {
        return getSupplierBase(stringValue -> {
            if (stringValue == null) {
                return null;
            } else {
                return Arrays.asList(stringValue.split(","));
            }
        });
    }

    private static Stream<Arguments> getSupplierBase(Function<String, Object> expectedConverter) {
        // Null
        HttpHeaders nullValueHeader = new DefaultHttpHeaders()
            .remove("test");

        // Single value
        HttpHeaders singleValueHeader = new DefaultHttpHeaders()
            .set("test", "value");

        // Overwritten value
        HttpHeaders overwrittenValueHeader = new DefaultHttpHeaders()
            .set("test", "value")
            .set("test", "value2");

        // Multi-value
        HttpHeaders multiValueHeader = new DefaultHttpHeaders()
            .set("test", "value")
            .add("test", "value2");

        return Stream.of(
            Arguments.of(new DefaultHttpHeaders(), "notAKey", null),
            Arguments.of(nullValueHeader, "test", expectedConverter.apply(null)),
            Arguments.of(singleValueHeader, "test", expectedConverter.apply("value")),
            Arguments.of(overwrittenValueHeader, "test", expectedConverter.apply("value2")),
            Arguments.of(multiValueHeader, "test", expectedConverter.apply("value,value2"))
        );
    }

    @ParameterizedTest
    @MethodSource("httpHeaderIteratorSupplier")
    public void httpHeaderIterator(HttpHeaders nettyHeaders, List<HttpHeader> expected) {
        List<HttpHeader> actual = new ArrayList<>();
        createHeaderWrapper(nettyHeaders)
            .iterator()
            .forEachRemaining(actual::add);

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            HttpHeader expectedHeader = expected.get(i);
            HttpHeader actualHeader = actual.get(i);

            assertEquals(expectedHeader.getName(), actualHeader.getName());
            assertEquals(expectedHeader.getValue(), actualHeader.getValue());
            assertArrayEquals(expectedHeader.getValues(), actualHeader.getValues());
            assertLinesMatch(expectedHeader.getValuesList(), actualHeader.getValuesList());
        }
    }

    @ParameterizedTest
    @MethodSource("httpHeaderIteratorSupplier")
    public void httpHeaderStream(HttpHeaders nettyHeaders, List<HttpHeader> expected) {
        List<HttpHeader> actual = createHeaderWrapper(nettyHeaders)
            .stream()
            .collect(Collectors.toList());

        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            HttpHeader expectedHeader = expected.get(i);
            HttpHeader actualHeader = actual.get(i);

            assertEquals(expectedHeader.getName(), actualHeader.getName());
            assertEquals(expectedHeader.getValue(), actualHeader.getValue());
            assertArrayEquals(expectedHeader.getValues(), actualHeader.getValues());
            assertLinesMatch(expectedHeader.getValuesList(), actualHeader.getValuesList());
        }
    }

    private static Stream<Arguments> httpHeaderIteratorSupplier() {
        // No header
        HttpHeaders nullValueHeader = new DefaultHttpHeaders()
            .remove("test");

        // Single value header
        HttpHeaders singleValueHeader = new DefaultHttpHeaders()
            .set("test", "value");

        // Overwritten value header
        HttpHeaders overwrittenValueHeader = new DefaultHttpHeaders()
            .set("test", "value")
            .set("test", "value2");

        // Multi-value header
        HttpHeaders multiValueHeader = new DefaultHttpHeaders()
            .set("test", "value")
            .add("test", "value2");

        // Single value headers
        HttpHeaders singleValueHeaders = new DefaultHttpHeaders()
            .set("test", "value")
            .set("test2", "value");

        // Overwritten value headers
        HttpHeaders overwrittenValueHeaders = new DefaultHttpHeaders()
            .set("test", "value")
            .set("test", "value2")
            .set("test2", "value")
            .set("test2", "value2");

        // Multi-value headers
        HttpHeaders multiValueHeaders = new DefaultHttpHeaders()
            .set("test", "value")
            .add("test", "value2")
            .set("test2", "value")
            .add("test2", "value2");

        return Stream.of(
            Arguments.of(nullValueHeader, Collections.emptyList()),
            Arguments.of(singleValueHeader, Collections.singletonList(new HttpHeader("test", "value"))),
            Arguments.of(overwrittenValueHeader, Collections.singletonList(new HttpHeader("test", "value2"))),
            Arguments.of(multiValueHeader, Collections.singletonList(new HttpHeader("test",
                Arrays.asList("value", "value2")))),
            Arguments.of(singleValueHeaders, Arrays.asList(new HttpHeader("test", "value"),
                new HttpHeader("test2", "value"))),
            Arguments.of(overwrittenValueHeaders, Arrays.asList(new HttpHeader("test", "value2"),
                new HttpHeader("test2", "value2"))),
            Arguments.of(multiValueHeaders, Arrays.asList(new HttpHeader("test", Arrays.asList("value", "value2")),
                new HttpHeader("test2", Arrays.asList("value", "value2"))))
        );
    }
}
