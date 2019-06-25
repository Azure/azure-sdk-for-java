// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.test.resources;

import java.io.IOException;
import java.util.Date;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.UserTokenCredentials;
import com.microsoft.aad.adal4j.AuthenticationResult;

public class MockUserTokenCredentials extends UserTokenCredentials {

    private AuthenticationResult authenticationResult;

    public MockUserTokenCredentials(String clientId, String domain, String username, String password,
            AzureEnvironment environment) {
        super(clientId, domain, username, password, environment);
    }

    public MockUserTokenCredentials() {
        this("", "", "", "", AzureEnvironment.AZURE);
    }

    @Override
    public String getToken(String resource) throws IOException {
        if (authenticationResult != null
            && authenticationResult.getExpiresOnDate().before(new Date())) {
            acquireAccessTokenFromRefreshToken();
        } else {
            acquireAccessToken();
        }
        return authenticationResult.getAccessToken();
    }

    private void acquireAccessToken() throws IOException {
        this.authenticationResult = new AuthenticationResult(
                null,
                "token1",
                "refresh",
                1,
                null,
                null,
                false);
    }

    private void acquireAccessTokenFromRefreshToken() throws IOException {
        this.authenticationResult = new AuthenticationResult(
                null,
                "token2",
                "refresh",
                1,
                null,
                null,
                false);
    }
}

