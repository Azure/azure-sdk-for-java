// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTests {
    @Test
    void comparesOneBatchWithoutEchoingSources() throws IOException {
        StringWriter output = new StringWriter();
        Main.classify(new BufferedReader(new StringReader(
            pair(0, "/** Old. */ class Example {}", "/** New. */ class Example {}")
                + pair(1, "class Formatted {}", "class Formatted { }")
                + pair(2, "class Commented {/* Old. */}", "class Commented {/* New. */}"))), new PrintWriter(output));
        assertEquals("0\tjavadoc-only\n1\twhitespace-only\n2\tordinary-comment-only\n",
            output.toString().replace("\r\n", "\n"));
    }

    @Test
    void rejectsIncompleteInvalidOrUnorderedInput() {
        for (String input : new String[] { "", "0\tonly-two-fields", "0\t!\t!", pair(1, "", ""),
            pair(0, "class Example {}", "class Example { }") + pair(0, "", ""), "0\t/w==\t/w==\n" }) {
            assertThrows(IOException.class, () -> Main.classify(
                new BufferedReader(new StringReader(input)), new PrintWriter(new StringWriter())));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void acceptsSourcesAtExactlyTwoMiB(boolean multibyte) throws IOException {
        String before = sourceWithUtf8Size(2 * 1024 * 1024, multibyte);
        String after = before.substring(0, 4) + "y" + before.substring(5);
        assertEquals(2 * 1024 * 1024, before.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(2 * 1024 * 1024, after.getBytes(StandardCharsets.UTF_8).length);
        StringWriter output = new StringWriter();
        Main.classify(new BufferedReader(new StringReader(pair(0, before, after))), new PrintWriter(output));
        assertEquals("0\tjavadoc-only\n", output.toString().replace("\r\n", "\n"));
    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2 })
    void rejectsSourcesAboveTwoMiB(int excessBytes) {
        String oversized = sourceWithUtf8Size(2 * 1024 * 1024 + excessBytes, false);
        for (String input : new String[] {
            pair(0, oversized, "class Example {}"), pair(0, "class Example {}", oversized)
        }) {
            StringWriter output = new StringWriter();
            IOException error = assertThrows(IOException.class, () -> Main.classify(
                new BufferedReader(new StringReader(input)), new PrintWriter(output)));
            assertEquals("Source exceeds the 2 MiB limit.", error.getMessage());
            assertEquals("", output.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 101, 500 })
    void comparesEveryPairBeyondTheFormerFileLimit(int fileCount) throws IOException {
        StringBuilder input = new StringBuilder();
        for (int i = 0; i < fileCount; i++) {
            input.append(pair(i, "class Example {}", "class Example { }"));
        }
        StringWriter output = new StringWriter();
        Main.classify(new BufferedReader(new StringReader(input.toString())), new PrintWriter(output));
        String[] results = output.toString().replace("\r\n", "\n").split("\n");
        assertEquals(fileCount, results.length);
        for (int i = 0; i < fileCount; i++) {
            assertEquals(i + "\twhitespace-only", results[i]);
        }
    }

    @Test
    void stopsReadingAtTheFirstDisqualifyingPair() throws IOException {
        for (int rejectAt : new int[] { 0, 1, 2, 100, 499 }) {
            final int expectedReads = rejectAt + 1;
            StringBuilder input = new StringBuilder();
            for (int i = 0; i < rejectAt; i++) {
                input.append(pair(i, "class Example {}", "class Example { }"));
            }
            input.append(pair(rejectAt, "class Example { int x; }", "class Example { int y; }"));
            input.append("invalid unread record\n");
            StringWriter output = new StringWriter();
            BufferedReader reader = new BufferedReader(new StringReader(input.toString())) {
                private int reads;

                @Override
                public String readLine() throws IOException {
                    assertTrue(++reads <= expectedReads, "The parser must not read another source pair.");
                    return super.readLine();
                }
            };
            Main.classify(reader, new PrintWriter(output));
            String actual = output.toString().replace("\r\n", "\n");
            assertTrue(actual.endsWith(rejectAt + "\tnon-documentation-change\n"));
            assertEquals(expectedReads, actual.split("\n").length);
        }
    }

    @Test
    void flushesEachResultBeforeRequestingAnotherSourcePair() throws IOException {
        StringWriter output = new StringWriter();
        BufferedReader reader = new BufferedReader(new StringReader(
            pair(0, "class Example {}", "class Example { }") + pair(1, "class Other {}", "class Other { }"))) {
            private int reads;

            @Override
            public String readLine() throws IOException {
                if (reads++ > 0) {
                    assertTrue(output.toString().contains((reads - 2) + "\twhitespace-only"));
                }
                return super.readLine();
            }
        };
        Main.classify(reader, new PrintWriter(new BufferedWriter(output)));
    }

    @Test
    void stopsOnParseErrorsAndUnchangedSources() throws IOException {
        for (String after : new String[] { "class Example {}", "class Example {" }) {
            StringWriter output = new StringWriter();
            Main.classify(new BufferedReader(new StringReader(
                pair(0, "class Example {}", after) + "invalid unread record\n")), new PrintWriter(output));
            assertTrue(output.toString().contains(after.endsWith("}") ? "no-source-edit" : "parse-error"));
        }
    }

    @Test
    void interactiveModeAllowsTheCallerToContinueWithAnotherLibrary() throws IOException {
        StringWriter output = new StringWriter();
        Main.classify(new BufferedReader(new StringReader(
            pair(0, "class First { int x; }", "class First { int y; }")
                + pair(1, "/** Old. */ class Second {}", "/** New. */ class Second {}")
                + pair(2, "class Broken {}", "class Broken {")
                + pair(3, "class Last {}", "class Last { }"))), new PrintWriter(output), false);
        assertEquals("0\tnon-documentation-change\n1\tjavadoc-only\n2\tparse-error\n3\twhitespace-only\n",
            output.toString().replace("\r\n", "\n"));
    }

    private static String pair(int index, String before, String after) {
        return index + "\t" + Base64.getEncoder().encodeToString(before.getBytes(StandardCharsets.UTF_8))
            + "\t" + Base64.getEncoder().encodeToString(after.getBytes(StandardCharsets.UTF_8)) + "\n";
    }

    private static String sourceWithUtf8Size(int bytes, boolean multibyte) {
        String prefix = "/** x";
        String suffix = " */ class Example {}";
        int paddingBytes = bytes - prefix.length() - suffix.length();
        char[] padding = new char[multibyte ? paddingBytes / 2 : paddingBytes];
        Arrays.fill(padding, multibyte ? '\u00e9' : 'a');
        return prefix + new String(padding) + (multibyte && paddingBytes % 2 != 0 ? "a" : "") + suffix;
    }
}
