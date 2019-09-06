// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.sun.jna.Platform;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class MsalCacheStorageTest {

    private CachePersister cachePersister;
    private String cacheLocation;

    @Before
    public void setup() throws Exception {
        org.junit.Assume.assumeTrue("Record".equalsIgnoreCase(System.getenv("AZURE_TEST_MODE")));
        org.junit.Assume.assumeTrue(!Platform.isWindows());
        cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString();
        cachePersister = new CachePersister.Builder()
                .cacheLocation(cacheLocation)
                .lockfileLocation(cacheLocation + ".lockfile")
                .build();
    }

    @Test
    public void writesReadsCacheData() {
        try {
            File f = new File(cacheLocation);

            String testString = "hello world";

            cachePersister.writeCache(testString.getBytes());
            String receivedString = new String(cachePersister.readCache());

            Assert.assertEquals(receivedString, testString);

            cachePersister.deleteCache();
        } finally {
            cachePersister.deleteCache();
        }
    }

}
