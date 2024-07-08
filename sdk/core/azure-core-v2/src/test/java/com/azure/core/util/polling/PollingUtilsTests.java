// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import io.clientcore.core.util.ClientLogger;
import org.junit.jupiter.api.Test;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollingUtilsTests {
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtilsTests.class);
    private static final String FORWARD_SLASH = "/";
    private static final String LOCAL_HOST = "http://localhost";

    @Test
    public void invalidPathTest() {
        String invalidPath = "`file";
        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> getAbsolutePath(invalidPath, LOCAL_HOST, LOGGER));
        assertTrue("'path' must be a valid URI.".equals(exception.getMessage()));
        assertThrows(NullPointerException.class, () -> getAbsolutePath(null, LOCAL_HOST, LOGGER));
    }

    @Test
    public void relativePathTest() {
        String relativePath = "/file";
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, null));
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, LOGGER));

        IllegalArgumentException exception
            = assertThrows(IllegalArgumentException.class, () -> getAbsolutePath(relativePath, null, LOGGER));
        assertTrue("Relative path requires endpoint to be non-null and non-empty to create an absolute path."
            .equals(exception.getMessage()));

        assertThrows(NullPointerException.class, () -> getAbsolutePath(relativePath, null, null));
        assertThrows(NullPointerException.class, () -> getAbsolutePath(relativePath, "", null));
    }

    @Test
    public void absolutePathTest() {
        String absolutePath = "http://localhost";
        assertEquals(absolutePath, getAbsolutePath(absolutePath, null, null));
        assertEquals(absolutePath, getAbsolutePath(absolutePath, LOCAL_HOST, null));
        assertEquals(absolutePath, getAbsolutePath(absolutePath, LOCAL_HOST, LOGGER));
    }

    @Test
    public void missingOrDuplicateSlashInPath() {
        final String endpoint = "http://localhost";
        final String relativePath = "path";
        final String expectedPath = endpoint + FORWARD_SLASH + relativePath;
        // Case 1: without slash in between
        assertEquals(expectedPath, getAbsolutePath(relativePath, endpoint, null));
        // Case 2: endpoint with slash only
        final String endpointWithSlash = endpoint + FORWARD_SLASH;
        assertEquals(expectedPath, getAbsolutePath(relativePath, endpointWithSlash, null));
        // Case 3: relative path with slash only
        final String relativePathWithSlash = FORWARD_SLASH + relativePath;
        assertEquals(expectedPath, getAbsolutePath(relativePathWithSlash, endpoint, null));
        // Case 4: both of endpoint and relative path have slash
        assertEquals(expectedPath, getAbsolutePath(relativePathWithSlash, endpointWithSlash, null));
    }
}
