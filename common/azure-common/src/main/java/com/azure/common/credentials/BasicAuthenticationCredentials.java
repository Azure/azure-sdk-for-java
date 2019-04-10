/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.credentials;

import com.azure.common.implementation.util.Base64Util;

import java.io.UnsupportedEncodingException;

/**
 * Basic Auth credentials for use with a REST Service Client.
 */
public class BasicAuthenticationCredentials implements ServiceClientCredentials {
    /**
     * Basic auth user name.
     */
    private String userName;

    /**
     * Basic auth password.
     */
    private String password;

    /**
     * Creates a basic authentication credential.
     *
     * @param userName basic auth user name
     * @param password basic auth password
     */
    public BasicAuthenticationCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    @Override
    public String authorizationHeaderValue(String uri) {
        String credential = userName + ":" + password;
        String encodedCredential;
        try {
            encodedCredential = Base64Util.encodeToString(credential.getBytes("UTF8"));
        } catch (UnsupportedEncodingException e) {
            // The encoding is hard-coded, so if it's unsupported, it needs to be fixed right here.
            throw new RuntimeException(e);
        }

        return "Basic " + encodedCredential;
    }
}
