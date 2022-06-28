// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.polling;

import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;

import static com.azure.core.util.polling.implementation.PollingUtils.getAbsolutePath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PollingUtilsTests {
    private static final String LOCAL_HOST = "http://localhost";
    private static final ClientLogger LOGGER = new ClientLogger(PollingUtilsTests.class);

    @Test
    public void invalidPathTest() {
        String invalidPath = "`file";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> getAbsolutePath(invalidPath, LOCAL_HOST, LOGGER));
        assertTrue("'path' must be a valid URI.".equals(exception.getMessage()));
        assertThrows(NullPointerException.class, () -> getAbsolutePath(null, LOCAL_HOST, LOGGER));
    }

    @Test
    public void relativePathTest() {
        String relativePath = "/file";
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, null));
        assertEquals(LOCAL_HOST + relativePath, getAbsolutePath(relativePath, LOCAL_HOST, LOGGER));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                                                      getAbsolutePath(relativePath, null, LOGGER));
        assertTrue("Relative path requires endpoint to be non-null or non-empty to create an absolute path."
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
}
