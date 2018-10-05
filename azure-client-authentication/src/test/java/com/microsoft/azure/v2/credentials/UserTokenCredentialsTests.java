/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.v2.credentials;

import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.azure.v2.AzureEnvironment;
import io.reactivex.Single;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

public class UserTokenCredentialsTests {
    private static MockUserTokenCredentials credentials = new MockUserTokenCredentials(
            "clientId",
            "domain",
            "username",
            "password",
            AzureEnvironment.AZURE
    );

    @Test
    public void testAcquireToken() throws Exception {
        credentials.acquireAccessToken();
        Assert.assertEquals("token1", credentials.getToken((String)null).blockingGet());
        Thread.sleep(1500);
        Assert.assertEquals("token2", credentials.getToken((String)null).blockingGet());
    }

    public static class MockUserTokenCredentials extends UserTokenCredentials {
        private AuthenticationResult authenticationResult;

        public MockUserTokenCredentials(String clientId, String domain, String username, String password, AzureEnvironment environment) {
            super(clientId, domain, username, password, environment);
        }

        @Override
        public Single<String> getToken(String resource) {
            if (authenticationResult != null
                && authenticationResult.getExpiresOnDate().before(new Date())) {
                acquireAccessTokenFromRefreshToken();
            } else {
                acquireAccessToken();
            }
            return Single.just(authenticationResult.getAccessToken());
        }

        private void acquireAccessToken() {
            this.authenticationResult = new AuthenticationResult(
                    null,
                    "token1",
                    "refresh",
                    1,
                    null,
                    null,
                    false);
        }

        private void acquireAccessTokenFromRefreshToken() {
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
}
