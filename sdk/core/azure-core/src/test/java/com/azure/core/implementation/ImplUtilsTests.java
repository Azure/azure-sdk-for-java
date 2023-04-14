// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ImplUtilsTests {
    @Test
    public void parseNullQueryParameters() {
        assertFalse(ImplUtils.parseQueryParameters(null).hasNext());
    }

    @Test
    public void parseEmptyQueryParameters() {
        assertFalse(ImplUtils.parseQueryParameters("").hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=value", "?key=value" })
    public void parseSimpleQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=", "?key=" })
    public void parseSimpleEmptyValueQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key", "?key" })
    public void parseSimpleKeyOnlyQueryParameter(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=value&key2=", "key=value&key2", "?key=value&key2=", "?key=value&key2" })
    public void parseQueryParameterLastParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertFalse(iterator.hasNext());
    }

    @ParameterizedTest
    @ValueSource(strings = { "key=&key2=value2", "key&key2=value2", "?key=&key2=value2", "?key&key2=value2" })
    public void parseQueryParameterFirstParameterEmpty(String queryParameters) {
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

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
        Iterator<Map.Entry<String, String>> iterator = ImplUtils.parseQueryParameters(queryParameters);

        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key", "value"), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key2", ""), iterator.next());
        assertEquals(new AbstractMap.SimpleImmutableEntry<>("key3", "value3"), iterator.next());
        assertFalse(iterator.hasNext());
    }
}
