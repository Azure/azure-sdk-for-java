// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msalExtensionsTests;

import com.azure.identity.implementation.msal_extensions.MsalCacheStorage;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MsalCacheStorageTest {

    MsalCacheStorage storage;
    String cacheLocation;

    @Before
    public void setup() throws Exception {
        cacheLocation = java.nio.file.Paths.get(System.getProperty("user.home"), "test.cache").toString();
        storage = new MsalCacheStorage.Builder()
                .cacheLocation(cacheLocation)
                .lockfileLocation(cacheLocation + ".lockfile")
                .build();
    }

    @After
    public void cleanup() {
        storage.deleteCache();
    }

    @Test
    public void createsNewCacheTest() throws IOException {
        storage.createCache();
        File f = new File(cacheLocation);

        Assert.assertTrue(f.exists());

        storage.deleteCache();
    }

    @Test
    public void writesReadsCacheData() throws IOException {
        storage.createCache();
        File f = new File(cacheLocation);

        String testString = "hello world";

        storage.writeCache(testString.getBytes());
        String receivedString = new String(storage.readCache());

        Assert.assertEquals(receivedString, testString);

        storage.deleteCache();
    }

}
