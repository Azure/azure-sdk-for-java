// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SemanticVersionTests {
    @Test
    public void simpleVersion() {
        SemanticVersion version = SemanticVersion.parse("1.23.45");
        SemanticVersion expectedVersion = new SemanticVersion(1, 23, 45, null, "1.23.45");
        assertEquals(expectedVersion, version);

        assertEquals(1, version.getMajorVersion());
        assertEquals("1.23.45", version.getVersionString());
        assertEquals(0, version.compareTo(expectedVersion));
        assertEquals(0, version.compareTo(SemanticVersion.parse("1.23.45+build")));

        assertEquals(1, version.compareTo(SemanticVersion.parse("1.23.45-beta")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("1.23.45-beta+build")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("0.23.45")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("1.2.45")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("1.23.4")));

        assertEquals(-1, version.compareTo(SemanticVersion.parse("1.23.46")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("1.24.0")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("1.24.0-beta")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("1.24.0+build")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("1.24.0-beta+build")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("2.0.0")));
        assertTrue(version.isValid());
    }

    @Test
    public void betaVersion() {
        SemanticVersion version = SemanticVersion.parse("10.2.3-beta.1");
        SemanticVersion expectedVersion = new SemanticVersion(10, 2, 3, "beta.1", "10.2.3-beta.1");
        assertEquals(expectedVersion, version);

        assertEquals(10, version.getMajorVersion());
        assertEquals("10.2.3-beta.1", version.getVersionString());
        assertEquals(0, version.compareTo(expectedVersion));
        assertEquals(0, version.compareTo(SemanticVersion.parse("10.2.3-beta.1+build")));

        assertEquals(1, version.compareTo(SemanticVersion.parse("10.2.3-beta.0")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("10.2.2")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("10.1.99")));
        assertEquals(1, version.compareTo(SemanticVersion.parse("09.2.3")));

        assertEquals(-1, version.compareTo(SemanticVersion.parse("10.2.3-beta.2")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("10.2.4")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("10.2.4-beta")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("10.2.5")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("10.3.0")));
        assertEquals(-1, version.compareTo(SemanticVersion.parse("11.0.0")));

        assertTrue(version.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonsense", "a.b.c", "1.2", "1.2-c", "c.1.2.3", "1.2.3?beta", ""})
    public void malformedVersion(String versionStr) {
        SemanticVersion malformed = SemanticVersion.parse(versionStr);
        assertFalse(malformed.isValid());
        assertEquals(versionStr, malformed.getVersionString());
        assertEquals(-1, malformed.getMajorVersion());
    }

    @Test
    public void reasonablyBrokenSuppoertedVersion() {
        SemanticVersion brokenButSupported1 = SemanticVersion.parse("1.2.3.45");
        SemanticVersion brokenButSupported2 = SemanticVersion.parse("1.2.3.45+build");
        SemanticVersion brokenButSupported3 = SemanticVersion.parse("1.2.3.rc1");
        SemanticVersion brokenButSupported4 = SemanticVersion.parse("1.2.3.rc1+build");

        SemanticVersion expectedVersion1 = new SemanticVersion(1, 2, 3, "45", "1.2.3.45");
        SemanticVersion expectedVersion2 = new SemanticVersion(1, 2, 3, "45", "1.2.3.45+build");
        SemanticVersion expectedVersion3 = new SemanticVersion(1, 2, 3, "rc1", "1.2.3.rc1");
        SemanticVersion expectedVersion4 = new SemanticVersion(1, 2, 3, "rc1", "1.2.3.rc1+build");

        assertTrue(brokenButSupported1.isValid());
        assertTrue(brokenButSupported2.isValid());
        assertTrue(brokenButSupported3.isValid());
        assertTrue(brokenButSupported4.isValid());

        assertEquals(expectedVersion1, brokenButSupported1);
        assertEquals(expectedVersion2, brokenButSupported2);
        assertEquals(expectedVersion3, brokenButSupported3);
        assertEquals(expectedVersion4, brokenButSupported4);

        assertEquals(0, expectedVersion1.compareTo(brokenButSupported1));
        assertEquals(0, expectedVersion1.compareTo(brokenButSupported2));
        assertEquals(0, expectedVersion2.compareTo(brokenButSupported2));
        assertEquals(0, expectedVersion3.compareTo(brokenButSupported3));
        assertEquals(0, expectedVersion3.compareTo(brokenButSupported4));
        assertEquals(0, expectedVersion4.compareTo(brokenButSupported4));

    }

    @Test
    public void classVersion() throws ClassNotFoundException {
        SemanticVersion version = SemanticVersion
                .getPackageVersionForClass("com.fasterxml.jackson.databind.ObjectMapper");
        assertTrue(version.isValid());

        version = SemanticVersion.getPackageVersionForClass("org.reactivestreams.Processor");
        assertTrue(version.isValid());
    }

    @ParameterizedTest
    @ValueSource(strings = {"nonsense", ""})
    public void malformedClassVersion(String className) {
        assertThrows(ClassNotFoundException.class, () -> SemanticVersion.getPackageVersionForClass(className));
    }
}
