/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class StringExtensionsTests {
    private static final String customDelim = ";";

    private static ArrayList<Triple<String, Integer, Integer>> TestDataUTF8 = new ArrayList<>();

    private static ArrayList<Triple<String, Integer, Integer>>  TestDataUTF8CustomDelim =  new ArrayList<>();

    private static ArrayList<Triple<String, Integer, Integer>> TestDataUTF16 = new ArrayList<>();

    private static ArrayList<Triple<String, Integer, Integer>> TestDataUTF16CustomDelim = new ArrayList<>();

    private static ArrayList<Triple<String, Integer, Integer>> TestDataUTF32 = new ArrayList<>();

    private static ArrayList<Triple<String, Integer, Integer>> TestDataUTF32CustomDelim = new ArrayList<>();

    @BeforeClass
    public static void setup() throws Exception {

        TestDataUTF8.add(new ImmutableTriple<>("", -1, -1));
        TestDataUTF8.add(new ImmutableTriple<>("a", -1, -1));
        TestDataUTF8.add(new ImmutableTriple<>("a b", -1, -1));
        TestDataUTF8.add(new ImmutableTriple<>("\r", 0, 0));
        TestDataUTF8.add(new ImmutableTriple<>("\n", 0, 0));
        TestDataUTF8.add(new ImmutableTriple<>("\r\n", 1, 1));
        TestDataUTF8.add(new ImmutableTriple<>("\n\r", 1, 1));
        TestDataUTF8.add(new ImmutableTriple<>("\r\nabcde", 1, 1));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\r", 5, 5));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\n", 5, 5));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\r\n", 6, 6));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\rabcde", 5, 5));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\nabcde", 5, 5));
        TestDataUTF8.add(new ImmutableTriple<>("abcde\r\nabcde", 6, 6));
        TestDataUTF8.add(new ImmutableTriple<>("a\rb\na\r\n", 1, 6));
        TestDataUTF8.add(new ImmutableTriple<>("\rb\na\r\n", 0, 5));

        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("", -1, -1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("a", -1, -1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("a b", -1, -1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>(";", 0, 0));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("a;", 1, 1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("b;", 1, 1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("a;abcde", 1, 1));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("abcde;", 5, 5));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("abcde\r;", 6, 6));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("abcde;abcde", 5, 5));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("abcde;abcde", 5, 5));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("abcde\r;abcde", 6, 6));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>("a;b\na\r;", 1, 6));
        TestDataUTF8CustomDelim.add(new ImmutableTriple<>(";b\na\r;", 0, 5));

        TestDataUTF16.add(new ImmutableTriple("", -1, -1));
        TestDataUTF16.add(new ImmutableTriple("a", -1, -1));
        TestDataUTF16.add(new ImmutableTriple("a b", -1, -1));
        TestDataUTF16.add(new ImmutableTriple("\r", 1, 1));
        TestDataUTF16.add(new ImmutableTriple("\n", 1, 1));
        TestDataUTF16.add(new ImmutableTriple("\r\n", 3, 3));
        TestDataUTF16.add(new ImmutableTriple("\n\r", 3, 3));
        TestDataUTF16.add(new ImmutableTriple("\r\nabcde", 3, 3));
        TestDataUTF16.add(new ImmutableTriple("abcde\r", 11, 11));
        TestDataUTF16.add(new ImmutableTriple("abcde\n", 11, 11));
        TestDataUTF16.add(new ImmutableTriple("abcde\r\n", 13, 13));
        TestDataUTF16.add(new ImmutableTriple("abcde\rabcde", 11, 11));
        TestDataUTF16.add(new ImmutableTriple("abcde\nabcde", 11, 11));
        TestDataUTF16.add(new ImmutableTriple("abcde\r\nabcde", 13, 13));
        TestDataUTF16.add(new ImmutableTriple("a\rb\na\r\n", 3, 13));
        TestDataUTF16.add(new ImmutableTriple("\rb\na\r\n", 1, 11));

        TestDataUTF16CustomDelim.add(new ImmutableTriple("", -1, -1));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("a", -1, -1));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("a b", -1, -1));
        TestDataUTF16CustomDelim.add(new ImmutableTriple(";", 1, 1));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("a;", 3, 3));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("b;", 3, 3));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("a;abcde", 3, 3));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("abcde;", 11, 11));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("abcde\r;", 13, 13));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("abcde;abcde", 11, 11));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("abcde;abcde", 11, 11));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("abcde\r;abcde", 13, 13));
        TestDataUTF16CustomDelim.add(new ImmutableTriple("a;b\na\r;", 3, 13));
        TestDataUTF16CustomDelim.add(new ImmutableTriple(";b\na\r;", 1, 11));

        TestDataUTF32.add(new ImmutableTriple("", -1, -1));
        TestDataUTF32.add(new ImmutableTriple("a", -1, -1));
        TestDataUTF32.add(new ImmutableTriple("a b", -1, -1));
        TestDataUTF32.add(new ImmutableTriple("\r", 3, 3));
        TestDataUTF32.add(new ImmutableTriple("\n", 3, 3));
        TestDataUTF32.add(new ImmutableTriple("\r\n", 7, 7));
        TestDataUTF32.add(new ImmutableTriple("\n\r", 7, 7));
        TestDataUTF32.add(new ImmutableTriple("\r\nabcde", 7, 7));
        TestDataUTF32.add(new ImmutableTriple("abcde\r", 23, 23));
        TestDataUTF32.add(new ImmutableTriple("abcde\n", 23, 23));
        TestDataUTF32.add(new ImmutableTriple("abcde\r\n", 27, 27));
        TestDataUTF32.add(new ImmutableTriple("abcde\rabcde", 23, 23));
        TestDataUTF32.add(new ImmutableTriple("abcde\nabcde", 23, 23));
        TestDataUTF32.add(new ImmutableTriple("abcde\r\nabcde", 27, 27));
        TestDataUTF32.add(new ImmutableTriple("a\rb\na\r\n", 7, 27));
        TestDataUTF32.add(new ImmutableTriple("\rb\na\r\n", 3, 23));

        TestDataUTF32CustomDelim.add(new ImmutableTriple("", -1, -1));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("a", -1, -1));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("a b", -1, -1));
        TestDataUTF32CustomDelim.add(new ImmutableTriple(";", 3, 3));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("a;", 7, 7));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("b;", 7, 7));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("a;abcde", 7, 7));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("abcde;", 23, 23));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("abcde\r;", 27, 27));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("abcde;abcde", 23, 23));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("abcde;abcde", 23, 23));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("abcde\r;abcde", 27, 27));
        TestDataUTF32CustomDelim.add(new ImmutableTriple("a;b\na\r;", 7, 27));
        TestDataUTF32CustomDelim.add(new ImmutableTriple(";b\na\r;", 3, 23));
    }

    @Test
    public void StringExtensions_FindNewLine_UTF8()
    {
        for (Triple<String, Integer, Integer> t: TestDataUTF8)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_8);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer =  StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_8, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_8, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_8, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_8, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }

        for (Triple<String, Integer, Integer> t: TestDataUTF8CustomDelim)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_8);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_8, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_8, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_8, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_8, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }
    }

    @Test
    public void StringExtensions_FindNewLine_UTF16()
    {
        for (Triple<String, Integer, Integer> t: TestDataUTF16)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_16LE);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16LE, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16LE, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16LE, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16LE, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }

        for (Triple<String, Integer, Integer> t: TestDataUTF16CustomDelim)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_16LE);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16LE, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16LE, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16LE, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16LE, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }
    }

    @Test
    public void StringExtensions_FindNewLine_UTF16BigEndian()
    {
        for (Triple<String, Integer, Integer> t: TestDataUTF16)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_16BE);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16BE, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16BE, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16BE, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16BE, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }

        for (Triple<String, Integer, Integer> t: TestDataUTF16CustomDelim)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.UTF_16BE);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16BE, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.UTF_16BE, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16BE, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.UTF_16BE, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }
    }

    @Test
    public void StringExtensions_FindNewLine_ASCII()
    {
        for (Triple<String, Integer, Integer> t: TestDataUTF8)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.US_ASCII);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.US_ASCII, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.US_ASCII, null);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.US_ASCII, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.US_ASCII, null);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }

        for (Triple<String, Integer, Integer> t: TestDataUTF8CustomDelim)
        {
            byte[] exactBuffer = t.getLeft().getBytes(StandardCharsets.US_ASCII);
            byte[] largerBuffer = new byte[exactBuffer.length + 100];
            System.arraycopy(exactBuffer, 0, largerBuffer, 0, exactBuffer.length);

            int forwardInExactBuffer = StringExtensions.FindNewline(exactBuffer, 0, exactBuffer.length, false, StandardCharsets.US_ASCII, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInExactBuffer);

            int forwardInLargeBuffer = StringExtensions.FindNewline(largerBuffer, 0, exactBuffer.length, false, StandardCharsets.US_ASCII, customDelim);
            Assert.assertEquals(t.getMiddle().intValue(), forwardInLargeBuffer);

            int reverseInExactBuffer = StringExtensions.FindNewline(exactBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.US_ASCII, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInExactBuffer);

            int reverseInLargeBuffer = StringExtensions.FindNewline(largerBuffer, Math.max(0, exactBuffer.length - 1), exactBuffer.length, true, StandardCharsets.US_ASCII, customDelim);
            Assert.assertEquals(t.getRight().intValue(), reverseInLargeBuffer);
        }
    }
}
