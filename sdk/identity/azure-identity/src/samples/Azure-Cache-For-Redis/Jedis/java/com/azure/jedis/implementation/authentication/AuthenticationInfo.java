// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.implementation.authentication;

public class AuthenticationInfo {
    private String authToken;
    private boolean shouldAuthenticate;
    private boolean refreshedToken;

    public AuthenticationInfo(String authToken, boolean shouldAuthenticate) {
        this.authToken = authToken;
        this.shouldAuthenticate = shouldAuthenticate;
    }

    public AuthenticationInfo(String authToken, boolean shouldAuthenticate, boolean refreshedToken) {
        this.authToken = authToken;
        this.shouldAuthenticate = shouldAuthenticate;
        this.refreshedToken = refreshedToken;
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isShouldAuthenticate() {
        return shouldAuthenticate;
    }

    public boolean isRefreshedToken() {
        return refreshedToken;
    }
}
