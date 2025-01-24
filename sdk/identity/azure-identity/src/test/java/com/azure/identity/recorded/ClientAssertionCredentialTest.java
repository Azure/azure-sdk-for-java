// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.recorded;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.identity.ClientAssertionCredential;
import com.azure.identity.ClientAssertionCredentialBuilder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ClientAssertionCredentialTest extends IdentityTestBase {

    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";
    private ClientAssertionCredential credential;

    private void initializeClient(HttpClient httpClient) {
        credential = new ClientAssertionCredentialBuilder().clientId(isPlaybackMode() ? "Dummy-Id" : getClientId())
            .tenantId(isPlaybackMode() ? "Dummy-Id" : getTenantId())
            .clientAssertion(() -> isPlaybackMode() ? INVALID_DUMMY_CLIENT_ASSERTION : getClientAssertion())
            .pipeline(super.getHttpPipeline(httpClient))
            .build();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "PLAYBACK")
    public void getToken(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);

        // act
        AccessToken actual
            = credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default"));

        // assert
        assertNotNull(actual);
        assertNotNull(actual.getToken());
        assertNotNull(actual.getExpiresAt());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getHttpClients")
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "PLAYBACK")
    public void getTokenAsync(HttpClient httpClient) {
        // arrange
        initializeClient(httpClient);
        StepVerifier
            .create(credential.getToken(new TokenRequestContext().addScopes("https://vault.azure.net/.default")))
            .expectNextMatches(accessToken -> accessToken.getToken() != null && accessToken.getExpiresAt() != null)
            .verifyComplete();
    }
}
