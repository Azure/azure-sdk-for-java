// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.credentials.TokenCredential;
import com.azure.v2.core.test.models.CustomMatcher;
import com.azure.v2.core.test.models.TestProxyRequestMatcher;
import com.azure.v2.core.test.models.TestProxySanitizer;
import com.azure.v2.core.test.models.TestProxySanitizerType;
import com.azure.v2.identity.AzurePowerShellCredentialBuilder;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import io.clientcore.core.credentials.oauth.AccessToken;
import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class KeyVaultAccessControlClientTestBase extends KeyVaultAdministrationClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultAccessControlClientTestBase.class);

    protected final String servicePrincipalId
        = Configuration.getGlobalConfiguration().get("CLIENT_OBJECTID", "f84ae8f9-c979-4750-a2fe-b350a00bebff");

    KeyVaultAccessControlClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) throws IOException {
        TokenCredential credential;

        if (interceptorManager.isLiveMode()) {
            credential = new AzurePowerShellCredentialBuilder().additionallyAllowedTenants("*").build();
        } else if (interceptorManager.isRecordMode()) {
            credential = new DefaultAzureCredentialBuilder().additionallyAllowedTenants("*").build();
            List<TestProxySanitizer> customSanitizers = new ArrayList<>();

            customSanitizers.add(new TestProxySanitizer("token", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizers);
        } else {
            credential = request -> new AccessToken("mockToken", OffsetDateTime.now().plusHours(2));

            List<TestProxyRequestMatcher> customMatchers = new ArrayList<>();

            customMatchers.add(new CustomMatcher().setComparingBodies(false)
                .setHeadersKeyOnlyMatch(Collections.singletonList("Accept"))
                .setExcludedHeaders(Arrays.asList("Authorization", "Accept-Language")));
            interceptorManager.addMatchers(customMatchers);
        }

        KeyVaultAccessControlClientBuilder builder = new KeyVaultAccessControlClientBuilder()
            .endpoint(getEndpoint())
            .credential(credential)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient);

        if (interceptorManager.isRecordMode() && !forCleanup) {
            return builder.addHttpPipelinePolicy(interceptorManager.getRecordPolicy());
        }

        return builder;
    }

    @Test
    public abstract void listRoleDefinitions(HttpClient httpClient) throws IOException;

    @Test
    public abstract void setRoleDefinition(HttpClient httpClient) throws IOException;

    @Test
    public abstract void getRoleDefinition(HttpClient httpClient) throws IOException;

    @Test
    public abstract void deleteRoleDefinition(HttpClient httpClient) throws IOException;

    @Test
    public abstract void deleteNonExistingRoleDefinitionDoesNotThrow(HttpClient httpClient) throws IOException;

    @Test
    public abstract void listRoleAssignments(HttpClient httpClient) throws IOException;

    @Test
    public abstract void createRoleAssignment(HttpClient httpClient) throws IOException;

    @Test
    public abstract void createExistingRoleAssignmentThrows(HttpClient httpClient) throws IOException;

    @Test
    public abstract void getRoleAssignment(HttpClient httpClient) throws IOException;

    @Test
    public abstract void deleteRoleAssignment(HttpClient httpClient) throws IOException;

    @Test
    public abstract void deleteNonExistingRoleAssignmentDoesNotThrow(HttpClient httpClient) throws IOException;

    static void assertRoleAssignmentEquals(KeyVaultRoleAssignment roleAssignment1,
        KeyVaultRoleAssignment roleAssignment2) {

        assertEquals(roleAssignment1.getId(), roleAssignment2.getId());
        assertEquals(roleAssignment1.getName(), roleAssignment2.getName());
        assertEquals(roleAssignment1.getType(), roleAssignment2.getType());

        KeyVaultRoleAssignmentProperties properties1 = roleAssignment1.getProperties();
        KeyVaultRoleAssignmentProperties properties2 = roleAssignment2.getProperties();

        if (properties1 == null && properties2 == null) {
            return;
        }

        assertNotNull(properties1);
        assertNotNull(properties2);
        assertEquals(properties1.getPrincipalId(), properties2.getPrincipalId());
        assertEquals(properties1.getRoleDefinitionId(), properties2.getRoleDefinitionId());
        assertEquals(properties1.getScope(), properties2.getScope());
    }

    static void assertRoleDefinitionEquals(KeyVaultRoleDefinition roleDefinition1,
        KeyVaultRoleDefinition roleDefinition2) {

        assertEquals(roleDefinition1.getId(), roleDefinition2.getId());
        assertEquals(roleDefinition1.getName(), roleDefinition2.getName());
        assertEquals(roleDefinition1.getType(), roleDefinition2.getType());
        assertEquals(roleDefinition1.getRoleName(), roleDefinition2.getRoleName());
        assertEquals(roleDefinition1.getRoleType(), roleDefinition2.getRoleType());
        assertEquals(roleDefinition1.getDescription(), roleDefinition2.getDescription());

        List<KeyVaultRoleScope> assignableScopes1 = roleDefinition1.getAssignableScopes();
        List<KeyVaultRoleScope> assignableScopes2 = roleDefinition2.getAssignableScopes();

        if (assignableScopes1 == null && assignableScopes2 == null) {
            return;
        }

        assertNotNull(assignableScopes1);
        assertNotNull(assignableScopes2);

        assertEquals(assignableScopes1.size(), assignableScopes2.size());
        assertTrue(assignableScopes1.containsAll(assignableScopes2));

        List<KeyVaultPermission> permissions1 = roleDefinition1.getPermissions();
        List<KeyVaultPermission> permissions2 = roleDefinition2.getPermissions();

        if (permissions1 == null && permissions2 == null) {
            return;
        }

        assertNotNull(permissions1);
        assertNotNull(permissions2);

        assertEquals(permissions1.size(), permissions2.size());
    }

    static void cleanUpResources(KeyVaultAccessControlClient cleanupClient, String roleDefinitionName,
        String roleAssignmentName) {
        if (roleDefinitionName != null) {
            try {
                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == 404) {
                    LOGGER.atInfo().log("Ignored 404 produced when trying to delete role definition.");
                }
            }
        }

        if (roleAssignmentName != null) {
            try {
                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            } catch (HttpResponseException e) {
                if (e.getResponse().getStatusCode() == 404) {
                    LOGGER.atInfo().log("Ignored 404 produced when trying to delete role assignment.");
                }
            }
        }
    }
}
