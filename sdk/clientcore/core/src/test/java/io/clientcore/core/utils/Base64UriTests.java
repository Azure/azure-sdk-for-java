// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import org.junit.jupiter.api.Test;

import static io.clientcore.core.utils.TestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Base64UriTests {
    @Test
    public void constructorWithNullBytes() {
        final Base64Uri base64Uri = new Base64Uri((byte[]) null);
        assertNull(base64Uri.encodedBytes());
        assertNull(base64Uri.decodedBytes());
        assertEmptyString(base64Uri.toString());
    }

    @Test
    public void constructorWithEmptyBytes() {
        final Base64Uri base64Uri = new Base64Uri(new byte[0]);
        assertArraysEqual(new byte[0], base64Uri.encodedBytes());
        assertArraysEqual(new byte[0], base64Uri.decodedBytes());
        assertEquals("", base64Uri.toString());
    }

    @Test
    public void constructorWithNonEmptyBytes() {
        final Base64Uri base64Uri
            = new Base64Uri(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 });
        assertArraysEqual(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 },
            base64Uri.encodedBytes());
        assertArraysEqual(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, base64Uri.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Uri.toString());
    }

    @Test
    public void constructorWithNullString() {
        final Base64Uri base64Uri = new Base64Uri((String) null);
        assertNull(base64Uri.encodedBytes());
        assertNull(base64Uri.decodedBytes());
        assertEmptyString(base64Uri.toString());
    }

    @Test
    public void constructorWithEmptyString() {
        final Base64Uri base64Uri = new Base64Uri("");
        assertArraysEqual(new byte[0], base64Uri.encodedBytes());
        assertArraysEqual(new byte[0], base64Uri.decodedBytes());
        assertEquals("", base64Uri.toString());
    }

    @Test
    public void constructorWithEmptyDoubleQuotedString() {
        final Base64Uri base64Uri = new Base64Uri("\"\"");
        assertArraysEqual(new byte[0], base64Uri.encodedBytes());
        assertArraysEqual(new byte[0], base64Uri.decodedBytes());
        assertEquals("", base64Uri.toString());
    }

    @Test
    public void constructorWithEmptySingleQuotedString() {
        final Base64Uri base64Uri = new Base64Uri("\'\'");
        assertArraysEqual(new byte[0], base64Uri.encodedBytes());
        assertArraysEqual(new byte[0], base64Uri.decodedBytes());
        assertEquals("", base64Uri.toString());
    }

    @Test
    public void constructorWithNonEmptyString() {
        final Base64Uri base64Uri = new Base64Uri("AAECAwQFBgcICQ");
        assertArraysEqual(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 },
            base64Uri.encodedBytes());
        assertArraysEqual(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, base64Uri.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Uri.toString());
    }

    @Test
    public void constructorWithNonEmptyDoubleQuotedString() {
        final Base64Uri base64Uri = new Base64Uri("\"AAECAwQFBgcICQ\"");
        assertArraysEqual(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 },
            base64Uri.encodedBytes());
        assertArraysEqual(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, base64Uri.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Uri.toString());
    }

    @Test
    public void constructorWithNonEmptySingleQuotedString() {
        final Base64Uri base64Uri = new Base64Uri("\'AAECAwQFBgcICQ\'");
        assertArraysEqual(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 },
            base64Uri.encodedBytes());
        assertArraysEqual(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, base64Uri.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Uri.toString());
    }

    @Test
    public void encodeWithNullBytes() {
        final Base64Uri base64Uri = Base64Uri.encode(null);
        assertNull(base64Uri.encodedBytes());
        assertNull(base64Uri.decodedBytes());
        assertEmptyString(base64Uri.toString());
    }

    @Test
    public void encodeWithEmptyBytes() {
        final Base64Uri base64Uri = Base64Uri.encode(new byte[0]);
        assertArraysEqual(new byte[0], base64Uri.encodedBytes());
        assertArraysEqual(new byte[0], base64Uri.decodedBytes());
        assertEquals("", base64Uri.toString());
    }

    @Test
    public void encodeWithNonEmptyBytes() {
        final Base64Uri base64Uri = Base64Uri.encode(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
        assertArraysEqual(new byte[] { 65, 65, 69, 67, 65, 119, 81, 70, 66, 103, 99, 73, 67, 81 },
            base64Uri.encodedBytes());
        assertArraysEqual(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 }, base64Uri.decodedBytes());
        assertEquals("AAECAwQFBgcICQ", base64Uri.toString());
    }

    private static void assertEmptyString(String input) {
        assertEquals("", input);
    }

}
