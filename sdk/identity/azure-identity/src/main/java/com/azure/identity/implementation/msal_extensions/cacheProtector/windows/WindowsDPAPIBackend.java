// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cacheProtector.windows;

import com.azure.identity.implementation.msal_extensions.cacheProtector.ICacheProtectorBackend;
import com.sun.jna.platform.win32.Crypt32Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WindowsDPAPIBackend implements ICacheProtectorBackend {

    private String CACHE_FILENAME;
    private File cache_file;

    @Override
    public void setup(String cacheLocation) {
        CACHE_FILENAME = cacheLocation;
        cache_file = new File(CACHE_FILENAME);
    }

    /*
     * Reads encrypted cache from cache file, decrypts it and returns as a String
     * */
    @Override
    public String unprotect(String service, String account) throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = new byte[(int) cache_file.length()];

        FileInputStream stream = new FileInputStream(cache_file);
        stream.read(encrypted_bytes);
        stream.close();

        byte[] decrypted_bytes = Crypt32Util.cryptUnprotectData(encrypted_bytes);

        return new String(decrypted_bytes, "UTF-8");
    }

    /*
     * Encrypts cache from in memory cache and writes to cache file
     * */
    @Override
    public void protect(String service, String account, String data) throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = Crypt32Util.cryptProtectData(data.getBytes("UTF-8"));

        FileOutputStream stream = new FileOutputStream(cache_file);
        stream.write(encrypted_bytes);
        stream.close();
    }

    private void makeSureFileExists() throws IOException {
        if (!cache_file.exists()) {
            cache_file.createNewFile();
            protect("", "", " ");
        }
    }
}
