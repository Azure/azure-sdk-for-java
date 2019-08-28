// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
//
// https://github.com/xafero/java-keyring

package com.azure.identity.implementation.msal_extensions.cacheProtector;

import com.azure.identity.implementation.msal_extensions.cacheProtector.windows.WindowsDPAPIBackend;
import com.sun.jna.Platform;

import java.io.IOException;

public class CacheProtector {

    private ICacheProtectorBackend backend;

    public CacheProtector(String cacheLocation) throws PlatformNotSupportedException {
        createBackend(cacheLocation);
    }

    private void createBackend(String cacheLocation) throws PlatformNotSupportedException {
        if (Platform.isWindows()) {
            backend = new WindowsDPAPIBackend();
        } else {
            throw new PlatformNotSupportedException("Platform is not supported");
        }
        backend.setup(cacheLocation);
    }

    /**
     * Gets password from key store
     * @param service Service name
     * @param account Account name
     *
     * @return unprotected data from cache
     * @throws IOException Thrown when an error happened while un-protecting the data
     */
    public String unprotect(String service, String account)
            throws IOException {

        return backend.unprotect(service, account);
    }

    /**
     * Sets password to key store
     * @param service Service name
     * @param account Account name
     * @param data    Data to protect
     *
     * @throws IOException Thrown when an error happened while protecting the data
     */
    public void protect(String service, String account, String data)
            throws IOException {

        backend.protect(service, account, data);
    }

}
