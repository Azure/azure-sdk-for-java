// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.utils;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the {@link CodeGenUtils} class.
 */
public class CodeGenUtilsTest {

    @Test
    void testEncodeQueryParamValue() {
        assertEquals("UriEscapers.QUERY_ESCAPER.escape(foo)", CodeGenUtils.encodeQueryParamValue("foo"));
    }

    @Test
    void testToJavaArrayInitializerWithQuotes() {
        assertEquals("\"a\", \"b\", \"c\"", CodeGenUtils.toJavaArrayInitializer(Arrays.asList("a", "b", "c"), true));
    }

    @Test
    void testToJavaArrayInitializerWithoutQuotes() {
        assertEquals("a, b, c", CodeGenUtils.toJavaArrayInitializer(Arrays.asList("a", "b", "c"), false));
    }

    @Test
    void testQuoteHeaderValueAlreadyQuoted() {
        assertEquals("\"foo\"", CodeGenUtils.quoteHeaderValue("\"foo\""));
    }

    @Test
    void testQuoteHeaderValueStartsWithQuote() {
        assertEquals("\"foo\"", CodeGenUtils.quoteHeaderValue("\"foo"));
    }

    @Test
    void testQuoteHeaderValueEndsWithQuote() {
        assertEquals("\"foo\"", CodeGenUtils.quoteHeaderValue("foo\""));
    }

    @Test
    void testQuoteHeaderValueNoQuotes() {
        assertEquals("\"foo\"", CodeGenUtils.quoteHeaderValue("foo"));
    }

    @Test
    void testQuoteHeaderValueEmptyString() {
        assertEquals("\"\"", CodeGenUtils.quoteHeaderValue(""));
    }

    @Test
    void testQuoteHeaderValueSingleQuote() {
        assertEquals("\"\"", CodeGenUtils.quoteHeaderValue("\""));
    }

    @Test
    void testToJavaArrayInitializerMixedQuotes() {
        assertEquals("\"foo\", \"bar\", \"baz\"",
            CodeGenUtils.toJavaArrayInitializer(Arrays.asList("foo", "bar", "baz"), true));
        assertEquals("\"foo\", \"bar\", \"baz\"",
            CodeGenUtils.toJavaArrayInitializer(Arrays.asList("\"foo\"", "bar", "baz"), true));
        assertEquals("\"foo\", \"bar\", \"baz\"",
            CodeGenUtils.toJavaArrayInitializer(Arrays.asList("foo", "\"bar\"", "baz"), true));
        assertEquals("\"foo\", \"bar\", \"baz\"",
            CodeGenUtils.toJavaArrayInitializer(Arrays.asList("foo", "bar", "\"baz\""), true));
    }

    @Test
    void testToJavaArrayInitializerEmptyList() {
        assertEquals("", CodeGenUtils.toJavaArrayInitializer(Arrays.asList(), true));
        assertEquals("", CodeGenUtils.toJavaArrayInitializer(Arrays.asList(), false));
    }
}
