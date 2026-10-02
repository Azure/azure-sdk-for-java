// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GitHubActionsCredentialBuilderTest {

    @Test
    public void testThrowsWhenTenantIdMissing() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-token"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration).build());

        assertTrue(e.getMessage().contains("Set the AZURE_TENANT_ID"));
    }

    @Test
    public void testThrowsWhenClientIdMissing() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-token"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration).build());

        assertTrue(e.getMessage().contains("Set the AZURE_CLIENT_ID"));
    }

    @Test
    public void testThrowsWhenOidcUrlMissing() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-token"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration).build());

        assertTrue(e.getMessage().contains("ACTIONS_ID_TOKEN_REQUEST_URL"));
    }

    @Test
    public void testThrowsWhenOidcTokenMissing() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration).build());

        assertTrue(e.getMessage().contains("ACTIONS_ID_TOKEN_REQUEST_TOKEN"));
    }

    @Test
    public void testReportsBothMissingGitHubEnvVars() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration).build());

        assertTrue(e.getMessage().contains("ACTIONS_ID_TOKEN_REQUEST_URL"));
        assertTrue(e.getMessage().contains("ACTIONS_ID_TOKEN_REQUEST_TOKEN"));
    }

    @Test
    public void testThrowsForUnsupportedAuthorityHost() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-token"));

        CredentialUnavailableException e = assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(configuration)
                .authorityHost("https://custom.authority.example.com")
                .build());

        assertTrue(
            e.getMessage().contains("The authority host \"https://custom.authority.example.com\" is not supported."));
    }

    @Test
    public void testConstructsSuccessfullyWhenAllEnvVarsSet() {
        Configuration configuration
            = TestUtils.createTestConfiguration(new TestConfigurationSource().put("AZURE_TENANT_ID", "test-tenant-id")
                .put("AZURE_CLIENT_ID", "test-client-id")
                .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request?foo=bar")
                .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-request-token"));

        GitHubActionsCredential credential = new GitHubActionsCredentialBuilder().configuration(configuration).build();

        assertNotNull(credential);
    }

    @Test
    public void testExplicitClientIdAndTenantIdOverrideConfiguration() {
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://token.actions.githubusercontent.com/request")
            .put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", "test-request-token"));

        GitHubActionsCredential credential = new GitHubActionsCredentialBuilder().configuration(configuration)
            .tenantId("explicit-tenant-id")
            .clientId("explicit-client-id")
            .build();

        assertNotNull(credential);
    }
}
