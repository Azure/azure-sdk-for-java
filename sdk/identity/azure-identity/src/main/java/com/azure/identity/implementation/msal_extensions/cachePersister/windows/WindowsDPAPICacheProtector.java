// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.

package com.azure.identity.implementation.msal_extensions.cachePersister.windows;

import com.azure.identity.implementation.msal_extensions.cachePersister.CacheProtectorBase;
import com.sun.jna.platform.win32.Crypt32Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class WindowsDPAPICacheProtector extends CacheProtectorBase {

    private String CACHE_FILENAME;
    private File cache_file;

    public WindowsDPAPICacheProtector(String cacheLocation, String lockfileLocation) throws IOException {
        super(lockfileLocation);
        CACHE_FILENAME = cacheLocation;
        cache_file = new File(CACHE_FILENAME);

        makeSureFileExists();
    }

    protected String unprotect() throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = new byte[(int) cache_file.length()];

        FileInputStream stream = new FileInputStream(cache_file);
        stream.read(encrypted_bytes);
        stream.close();

        byte[] decrypted_bytes = Crypt32Util.cryptUnprotectData(encrypted_bytes);

        return new String(decrypted_bytes, "UTF-8");
    }

    protected void protect(String data) throws IOException {
        makeSureFileExists();

        byte[] encrypted_bytes = Crypt32Util.cryptProtectData(data.getBytes("UTF-8"));

        FileOutputStream stream = new FileOutputStream(cache_file);
        stream.write(encrypted_bytes);
        stream.close();
    }

    private void makeSureFileExists() throws IOException {
        // make sure file exists - and write empty string
        if (!cache_file.exists()) {
            cache_file.createNewFile();
            protect(" ");
        }
    }

    public void deleteCacheHelper() {
        cache_file.delete();
    }

}
