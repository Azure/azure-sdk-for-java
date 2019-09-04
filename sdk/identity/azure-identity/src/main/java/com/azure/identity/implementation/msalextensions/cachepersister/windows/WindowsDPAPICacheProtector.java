// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.msalextensions.cachepersister.windows;

import com.azure.identity.implementation.msalextensions.cachepersister.CacheProtectorBase;
import com.sun.jna.platform.win32.Crypt32Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Cache Protector for Windows which uses Windows DPAPI to encrypt the cache
 * */
public class WindowsDPAPICacheProtector extends CacheProtectorBase {

    private final String cacheFilename;
    private File cacheFile;

    /**
     * Constructor to initialize WindowsDPAPICacheProtector
     * Calls super constructor to initialize lock
     *
     * @param cacheLocation
     * @param lockfileLocation
     *
     * @throws IOException if cacheFile File isn't created
     * */
    public WindowsDPAPICacheProtector(String cacheLocation, String lockfileLocation) throws IOException {
        super(lockfileLocation);
        cacheFilename = cacheLocation;
        cacheFile = new File(cacheFilename);

        makeSureFileExists();
    }

    /**
     * Uses DPAPI to read and decrypt cache contents
     *
     * @return  byte[] cache contents
     * */
    protected byte[] unprotect() throws IOException {
        makeSureFileExists();

        byte[] encryptedBytes = new byte[(int) cacheFile.length()];

        try (FileInputStream stream = new FileInputStream(cacheFile)) {
            int read = 0;
            while (read != encryptedBytes.length) {
                read += stream.read(encryptedBytes);
            }
        }

        byte[] decryptedBytes = Crypt32Util.cryptUnprotectData(encryptedBytes);
        return decryptedBytes;
    }

    /**
     * Uses DPAPI to write and protect cache contents
     *
     * @param data contents to write to cache
     * */
    protected void protect(byte[] data) throws IOException {
        makeSureFileExists();

        byte[] encryptedBytes = Crypt32Util.cryptProtectData(data);

        try (FileOutputStream stream = new FileOutputStream(cacheFile)) {
            stream.write(encryptedBytes);
        }
    }

    /**
     * Make sure file exists - and write " " if it was just created
     * Just a backup in case the cache was deleted
     * */
    private void makeSureFileExists() throws IOException {
        if (!cacheFile.exists()) {
            cacheFile.createNewFile();
            protect(" ".getBytes("UTF-8"));
        }
    }

    /**
     * Deletes the cache file if it exists
     * */
    public void deleteCacheHelper() {
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }

}
