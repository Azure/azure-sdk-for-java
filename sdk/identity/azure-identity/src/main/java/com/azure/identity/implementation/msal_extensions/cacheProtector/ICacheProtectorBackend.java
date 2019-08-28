// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
//
// https://github.com/xafero/java-keyring

package com.azure.identity.implementation.msal_extensions.cacheProtector;

import java.io.IOException;

public interface ICacheProtectorBackend {

    void setup(String cacheLocation);

    /**
     * Gets password from key store
     * @param service Service name
     * @param account Account name
     *
     * @return unprotected data from cache
     * @throws IOException Thrown when an error happened while un-protecting the data
     */
    String unprotect(String service, String account)
            throws IOException;

    /**
     * Sets password to key store
     * @param service Service name
     * @param account Account name
     * @param data    Data to protect
     *
     * @throws IOException Thrown when an error happened while protecting the data
     */
    void protect(String service, String account, String data)
            throws IOException;
}
