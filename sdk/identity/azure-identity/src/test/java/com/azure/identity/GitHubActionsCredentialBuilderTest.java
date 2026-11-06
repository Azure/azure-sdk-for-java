// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static com.azure.identity.GitHubActionsCredentialTest.SCOPE;
import static com.azure.identity.GitHubActionsCredentialTest.credentialBuilder;
import static com.azure.identity.GitHubActionsCredentialTest.configuration;
import static com.azure.identity.GitHubActionsCredentialTest.parseForm;
import static com.azure.identity.GitHubActionsCredentialTest.settings;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitHubActionsCredentialBuilderTest {
    @ParameterizedTest
    @ValueSource(
        strings = {
            "AZURE_TENANT_ID",
            "AZURE_CLIENT_ID",
            "ACTIONS_ID_TOKEN_REQUEST_URL",
            "ACTIONS_ID_TOKEN_REQUEST_TOKEN" })
    void validatesEveryRequiredSettingBeforeHttp(String setting) {
        for (String value : new String[] { null, "" }) {
            Map<String, String> values = settings();
            values.put(setting, value);
            GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
            CredentialUnavailableException error = assertThrows(CredentialUnavailableException.class,
                () -> credentialBuilder(http).configuration(configuration(values)).build());
            assertTrue(error.getMessage().contains(setting));
            assertTrue(error.getMessage().contains("id-token: write"));
            assertFalse(error.getMessage().contains("runner-token-sentinel"));
            if (setting.startsWith("ACTIONS_ID_TOKEN_REQUEST_")) {
                assertTrue(error.getMessage().contains("https://aka.ms/azsdk/java/identity/troubleshoot"));
                String configuredSetting
                    = setting.endsWith("_URL") ? "ACTIONS_ID_TOKEN_REQUEST_TOKEN" : "ACTIONS_ID_TOKEN_REQUEST_URL";
                assertFalse(error.getMessage().contains(configuredSetting));
            }
            assertEquals(0, http.oidcRequests.get());
        }
    }

    @ParameterizedTest
    @CsvSource(value = { "null,null", "null,''", "'',null", "'',''" }, nullValues = "null")
    void reportsBothMissingGitHubVariables(String requestUrl, String requestToken) {
        Map<String, String> values = settings();
        values.put("ACTIONS_ID_TOKEN_REQUEST_URL", requestUrl);
        values.put("ACTIONS_ID_TOKEN_REQUEST_TOKEN", requestToken);
        GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
        CredentialUnavailableException error = assertThrows(CredentialUnavailableException.class,
            () -> credentialBuilder(http).configuration(configuration(values)).build());
        assertTrue(error.getMessage().contains("ACTIONS_ID_TOKEN_REQUEST_URL, ACTIONS_ID_TOKEN_REQUEST_TOKEN"));
        assertTrue(error.getMessage().contains("permissions: id-token: write"));
        assertTrue(error.getMessage().contains("https://aka.ms/azsdk/java/identity/troubleshoot"));
        assertEquals(0, http.oidcRequests.get());
    }

    @Test
    void explicitIdsOverrideEnvironmentAndCredentialsSnapshotBuilderValues() {
        GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
        GitHubActionsCredentialBuilder builder
            = credentialBuilder(http).clientId("explicit-client").tenantId("explicit-tenant");
        GitHubActionsCredential first = builder.build();
        GitHubActionsCredential second = builder.clientId("second-client").tenantId("second-tenant").build();
        assertEquals(0, http.oidcRequests.get());
        first.getTokenSync(new TokenRequestContext().addScopes(SCOPE));
        assertEquals("explicit-client", http.tokenForms.get(0).get("client_id"));
        assertTrue(http.lastTokenPath.contains("explicit-tenant"));
        second.getTokenSync(new TokenRequestContext().addScopes(SCOPE));
        assertEquals("second-client", http.tokenForms.get(1).get("client_id"));
        assertTrue(http.lastTokenPath.contains("second-tenant"));
    }

    @Test
    void disabledConfigurationDoesNotFallBackToTheEnvironment() {
        assertThrows(CredentialUnavailableException.class,
            () -> new GitHubActionsCredentialBuilder().configuration(Configuration.NONE).build());
    }

    @ParameterizedTest
    @CsvSource({
        "login.microsoftonline.com,AzureADTokenExchange",
        "login.chinacloudapi.cn,AzureADTokenExchangeChina",
        "login.microsoftonline.us,AzureADTokenExchangeUSGov",
        "login.sovcloud-identity.fr,AzureADTokenExchangeFrance",
        "login.sovcloud-identity.de,AzureADTokenExchangeGermany",
        "login.sovcloud-identity.sg,AzureADTokenExchangeGovSG",
        "LOGIN.MICROSOFTONLINE.COM,AzureADTokenExchange" })
    void derivesTheAudienceFromTheConfiguredAuthority(String host, String audience) {
        GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
        Map<String, String> values = settings();
        values.put("AZURE_AUTHORITY_HOST", "https://" + host + "/");
        values.put("ACTIONS_ID_TOKEN_REQUEST_URL",
            "https://github-oidc.test/token?encoded=a%2Fb&audience=old&audience=duplicate");
        credentialBuilder(http).configuration(configuration(values))
            .build()
            .getTokenSync(new TokenRequestContext().addScopes(SCOPE));
        String query = http.oidcRequest.getUrl().getQuery();
        assertEquals("api://" + audience, parseForm(query).get("audience"));
        assertEquals("a/b", parseForm(query).get("encoded"));
        assertEquals(1, query.split("audience=", -1).length - 1);
        assertTrue(query.contains("api%3A%2F%2F"));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void explicitAuthorityWinsRegardlessOfConfigurationSetterOrder(boolean configurationFirst) {
        GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
        Map<String, String> values = settings();
        values.put("AZURE_AUTHORITY_HOST", AzureAuthorityHosts.AZURE_CHINA);
        GitHubActionsCredentialBuilder builder = credentialBuilder(http);
        if (configurationFirst) {
            builder.configuration(configuration(values)).authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT);
        } else {
            builder.authorityHost(AzureAuthorityHosts.AZURE_GOVERNMENT).configuration(configuration(values));
        }
        builder.build().getTokenSync(new TokenRequestContext().addScopes(SCOPE));
        assertEquals("api://AzureADTokenExchangeUSGov",
            parseForm(http.oidcRequest.getUrl().getQuery()).get("audience"));
        assertEquals("login.microsoftonline.us", http.tokenRequest.getUrl().getHost());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "https://unsupported.test",
            "https://login.microsoftonline.com.attacker.test",
            "https://login.microsoftonline.de",
            "https://user:secret@login.microsoftonline.com" })
    void rejectsUnsupportedAuthorityHosts(String authority) {
        assertThrows(CredentialUnavailableException.class,
            () -> credentialBuilder(new GitHubActionsCredentialTest.FakeHttpClient()).authorityHost(authority).build());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "not a URI",
            "/relative",
            "http://github-oidc.test/token",
            "https:///token",
            "https://user:secret@github-oidc.test/token",
            "https://github-oidc.test/token#fragment" })
    void rejectsInvalidRunnerUrls(String url) {
        Map<String, String> values = settings();
        values.put("ACTIONS_ID_TOKEN_REQUEST_URL", url);
        CredentialUnavailableException error = assertThrows(CredentialUnavailableException.class,
            () -> credentialBuilder(new GitHubActionsCredentialTest.FakeHttpClient())
                .configuration(configuration(values))
                .build());
        assertFalse(error.getMessage().contains("secret"));
    }

    @Test
    void validatesEnvironmentTenantAndSupportsUrlsWithoutQueries() {
        Map<String, String> values = settings();
        values.put("AZURE_TENANT_ID", "invalid/tenant");
        assertThrows(IllegalArgumentException.class,
            () -> credentialBuilder(new GitHubActionsCredentialTest.FakeHttpClient())
                .configuration(configuration(values))
                .build());
        values.put("AZURE_TENANT_ID", "valid-tenant");
        values.put("ACTIONS_ID_TOKEN_REQUEST_URL", "https://github-oidc.test/token");
        GitHubActionsCredentialTest.FakeHttpClient http = new GitHubActionsCredentialTest.FakeHttpClient();
        assertNotNull(credentialBuilder(http).configuration(configuration(values))
            .build()
            .getTokenSync(new TokenRequestContext().addScopes(SCOPE)));
        assertEquals("api://AzureADTokenExchange", parseForm(http.oidcRequest.getUrl().getQuery()).get("audience"));
    }
}
