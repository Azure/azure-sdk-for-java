// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import com.azure.v2.core.credentials.TokenRequestContext;
import io.clientcore.core.credentials.oauth.AccessToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

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
        AccessToken token = new GitHubActionsCredentialBuilder().build()
            .getToken(new TokenRequestContext().addScopes("https://management.azure.com/.default"));
        assertNotNull(token);
        assertFalse(token.getToken().isEmpty());
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()));
    }
}
