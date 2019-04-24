// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.auth.credentials;

import com.azure.common.AzureEnvironment;
import com.microsoft.aad.adal4j.AuthenticationResult;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;

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
        Assert.assertEquals("token1", credentials.getToken(null).block());
        Thread.sleep(1500);
        Assert.assertEquals("token2", credentials.getToken(null).block());
    }

    public static class MockUserTokenCredentials extends UserTokenCredentials {
        private AuthenticationResult authenticationResult;

        public MockUserTokenCredentials(String clientId, String domain, String username, String password, AzureEnvironment environment) {
            super(clientId, domain, username, password, environment);
        }

        @Override
        public Mono<String> getToken(String resource) {
            if (authenticationResult != null
                && authenticationResult.getExpiresOnDate().before(new Date())) {
                acquireAccessTokenFromRefreshToken();
            } else {
                acquireAccessToken();
            }
            return Mono.just(authenticationResult.getAccessToken());
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
