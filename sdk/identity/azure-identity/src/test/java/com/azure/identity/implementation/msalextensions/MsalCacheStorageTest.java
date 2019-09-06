// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions;

import com.azure.identity.implementation.msalextensions.cachepersister.CachePersister;
import com.sun.jna.Platform;
import org.junit.*;

import java.io.File;
import java.io.IOException;

public class MsalCacheStorageTest {

    private CachePersister cachePersister;
    private String cacheLocation;

    @Before
    public void setup() throws Exception {
        Assume.assumeTrue(Platform.isWindows());
        cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString();
        cachePersister = new CachePersister.Builder()
                .cacheLocation(cacheLocation)
                .lockfileLocation(cacheLocation + ".lockfile")
                .build();
    }

    @After
    public void cleanup() {
        Assume.assumeTrue(Platform.isWindows());
        cachePersister.deleteCache();
    }

    @Test
    public void writesReadsCacheData() throws IOException {
        File f = new File(cacheLocation);

        String testString = "hello world";

        cachePersister.writeCache(testString.getBytes());
        String receivedString = new String(cachePersister.readCache());

        Assert.assertEquals(receivedString, testString);

        cachePersister.deleteCache();
    }

}
