// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfEnvironmentVariable(named = "RUN_GITHUB_ACTIONS_CREDENTIAL_LIVE_TESTS", matches = "true")
@EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
@EnabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
class GitHubActionsCredentialLiveTest {
    @Test
    void acquiresTokenFromGitHubActions() {
        GitHubActionsCredential credential = new GitHubActionsCredentialBuilder().build();
        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");
        assertValidToken(credential.getTokenSync(request));
        StepVerifier.create(credential.getToken(request))
            .assertNext(GitHubActionsCredentialLiveTest::assertValidToken)
            .expectComplete()
            .verify(Duration.ofMinutes(2));
    }

    private static void assertValidToken(AccessToken token) {
        assertNotNull(token);
        assertFalse(token.getToken().isEmpty());
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()));
    }
}
