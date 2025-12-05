// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RequestImageContentPartDetail}.
 */
class RequestImageContentPartDetailTest {

    @Test
    void testAutoConstant() {
        // Assert
        assertNotNull(RequestImageContentPartDetail.AUTO);
        assertEquals("auto", RequestImageContentPartDetail.AUTO.toString());
    }

    @Test
    void testLowConstant() {
        // Assert
        assertNotNull(RequestImageContentPartDetail.LOW);
        assertEquals("low", RequestImageContentPartDetail.LOW.toString());
    }

    @Test
    void testHighConstant() {
        // Assert
        assertNotNull(RequestImageContentPartDetail.HIGH);
        assertEquals("high", RequestImageContentPartDetail.HIGH.toString());
    }

    @Test
    void testFromString() {
        // Act
        RequestImageContentPartDetail auto = RequestImageContentPartDetail.fromString("auto");
        RequestImageContentPartDetail low = RequestImageContentPartDetail.fromString("low");
        RequestImageContentPartDetail high = RequestImageContentPartDetail.fromString("high");

        // Assert
        assertEquals(RequestImageContentPartDetail.AUTO, auto);
        assertEquals(RequestImageContentPartDetail.LOW, low);
        assertEquals(RequestImageContentPartDetail.HIGH, high);
    }

    @Test
    void testValues() {
        // Act
        Collection<RequestImageContentPartDetail> values = RequestImageContentPartDetail.values();

        // Assert
        assertNotNull(values);
        assertTrue(values.size() >= 3);
        assertTrue(values.contains(RequestImageContentPartDetail.AUTO));
        assertTrue(values.contains(RequestImageContentPartDetail.LOW));
        assertTrue(values.contains(RequestImageContentPartDetail.HIGH));
    }

    @Test
    void testEquality() {
        // Act
        RequestImageContentPartDetail detail1 = RequestImageContentPartDetail.fromString("high");
        RequestImageContentPartDetail detail2 = RequestImageContentPartDetail.HIGH;

        // Assert
        assertEquals(detail1, detail2);
        assertEquals(detail1.hashCode(), detail2.hashCode());
    }

    @Test
    void testCustomValue() {
        // Act
        RequestImageContentPartDetail custom = RequestImageContentPartDetail.fromString("custom-detail");

        // Assert
        assertNotNull(custom);
        assertEquals("custom-detail", custom.toString());
    }

    @Test
    void testCaseSensitivity() {
        // Act
        RequestImageContentPartDetail lowercase = RequestImageContentPartDetail.fromString("high");
        RequestImageContentPartDetail uppercase = RequestImageContentPartDetail.fromString("HIGH");

        // Assert
        // ExpandableStringEnum is case-sensitive
        assertEquals("high", lowercase.toString());
        assertEquals("HIGH", uppercase.toString());
    }
}
