// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jedis.implementation.authentication;

import com.azure.core.credential.AccessToken;

public class AccessTokenResult {
    private AccessToken accessToken;
    private boolean refreshedToken;

    public AccessTokenResult(AccessToken accessToken, boolean refreshedToken) {
        this.accessToken = accessToken;
        this.refreshedToken = refreshedToken;
    }

    public AccessToken getAccessToken() {
        return accessToken;
    }

    public boolean isRefreshedToken() {
        return refreshedToken;
    }
}
