// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msalExtensionsTests;

import com.azure.identity.implementation.msal_extensions.cachePersister.CachePersister;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MsalCacheStorageTest {

    private CachePersister cachePersister;
    private String cacheLocation;

    @Before
    public void setup() throws Exception {
        cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString();
        cachePersister = new CachePersister.Builder()
                .cacheLocation(cacheLocation)
                .lockfileLocation(cacheLocation + ".lockfile")
                .build();
    }

    @After
    public void cleanup() {
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
