// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister.windows;

import com.azure.identity.implementation.msal_extensions.cachePersister.CacheProtectorBase;
import com.sun.jna.platform.win32.Crypt32Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Cache Protector for Windows which uses Windows DPAPI to encrypt the cache
 * */
public class WindowsDPAPICacheProtector extends CacheProtectorBase {

    private String CACHE_FILENAME;
    private File cache_file;

    /**
     * Constructor to initialize WindowsDPAPICacheProtector
     * Calls super constructor to initialize lock
     *
     * @param cacheLocation
     * @param lockfileLocation
     *
     * @throws IOException if cache_file File isn't created
     * */
    public WindowsDPAPICacheProtector(String cacheLocation, String lockfileLocation) throws IOException {
        super(lockfileLocation);
        CACHE_FILENAME = cacheLocation;
        cache_file = new File(CACHE_FILENAME);

        makeSureFileExists();
    }

    /**
     * Uses DPAPI to read and decrypt cache contents
     *
     * @return  byte[] cache contents
     * */
    protected byte[] unprotect() throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = new byte[(int) cache_file.length()];

        FileInputStream stream = new FileInputStream(cache_file);
        stream.read(encrypted_bytes);
        stream.close();

        byte[] decrypted_bytes = Crypt32Util.cryptUnprotectData(encrypted_bytes);
        return decrypted_bytes;
    }

    /**
     * Uses DPAPI to write and protect cache contents
     *
     * @param data contents to write to cache
     * */
    protected void protect(byte[] data) throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = Crypt32Util.cryptProtectData(data);

        FileOutputStream stream = new FileOutputStream(cache_file);
        stream.write(encrypted_bytes);
        stream.close();
    }

    /**
     * Make sure file exists - and write " " if it was just created
     * Just a backup in case the cache was deleted
     * */
    private void makeSureFileExists() throws IOException {
        if (!cache_file.exists()) {
            cache_file.createNewFile();
            protect(" ".getBytes());
        }
    }

    /**
     * Deletes the cache file
     * */
    public void deleteCacheHelper() {
        cache_file.delete();
    }

}
