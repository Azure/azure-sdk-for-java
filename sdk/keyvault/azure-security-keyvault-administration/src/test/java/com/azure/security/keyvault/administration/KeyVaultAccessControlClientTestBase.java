// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestMode;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class KeyVaultAccessControlClientTestBase extends KeyVaultAdministrationClientTestBase {
    protected static final String ROLE_NAME = "Managed HSM Crypto Officer";
    String servicePrincipalId = "49acc88b-8f9e-4619-9856-16691db66767";

    protected KeyVaultAccessControlClientBuilder getClientBuilder(HttpClient httpClient, boolean forCleanup) {
        List<HttpPipelinePolicy> policies = getPolicies();

        if (getTestMode() == TestMode.RECORD && !forCleanup) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        HttpPipeline httpPipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return new KeyVaultAccessControlClientBuilder()
            .vaultUrl(getEndpoint())
            .pipeline(httpPipeline);
    }

    @Test
    public abstract void listRoleDefinitions(HttpClient httpClient);

    @Test
    public abstract void setRoleDefinition(HttpClient httpClient);

    @Test
    public abstract void getRoleDefinition(HttpClient httpClient);

    @Test
    public abstract void deleteRoleDefinition(HttpClient httpClient);

    @Test
    public abstract void deleteNonExistingRoleDefinitionThrows(HttpClient httpClient);

    @Test
    public abstract void listRoleAssignments(HttpClient httpClient);

    @Test
    public abstract void createRoleAssignment(HttpClient httpClient);

    @Test
    public abstract void createExistingRoleAssignmentThrows(HttpClient httpClient);

    @Test
    public abstract void getRoleAssignment(HttpClient httpClient);

    @Test
    public abstract void deleteRoleAssignment(HttpClient httpClient);

    @Test
    public abstract void deleteNonExistingRoleAssignmentThrows(HttpClient httpClient);

    static void assertRoleAssignmentEquals(KeyVaultRoleAssignment roleAssignment1,
                                           KeyVaultRoleAssignment roleAssignment2) {
        assertEquals(roleAssignment1.getId(), roleAssignment2.getId());
        assertEquals(roleAssignment1.getName(), roleAssignment2.getName());
        assertEquals(roleAssignment1.getType(), roleAssignment2.getType());
        assertEquals(roleAssignment1.getRoleScope(), roleAssignment2.getRoleScope());

        KeyVaultRoleAssignmentProperties properties1 = roleAssignment1.getProperties();
        KeyVaultRoleAssignmentProperties properties2 = roleAssignment2.getProperties();

        if (properties1 == null && properties2 == null) {
            return;
        }

        assertNotNull(properties1);
        assertNotNull(properties2);
        assertEquals(properties1.getPrincipalId(), properties2.getPrincipalId());
        assertEquals(properties1.getRoleDefinitionId(), properties2.getRoleDefinitionId());
    }

    static void assertRoleDefinitionEquals(KeyVaultRoleDefinition roleDefinition1,
                                           KeyVaultRoleDefinition roleDefinition2) {
        assertEquals(roleDefinition1.getId(), roleDefinition2.getId());
        assertEquals(roleDefinition1.getName(), roleDefinition2.getName());
        assertEquals(roleDefinition1.getType(), roleDefinition2.getType());

        KeyVaultRoleDefinitionProperties properties1 = roleDefinition1.getProperties();
        KeyVaultRoleDefinitionProperties properties2 = roleDefinition2.getProperties();

        if (properties1 == null && properties2 == null) {
            return;
        }

        assertNotNull(properties1);
        assertNotNull(properties2);
        assertEquals(properties1.getRoleName(), properties2.getRoleName());
        assertEquals(properties1.getRoleType(), properties2.getRoleType());
        assertEquals(properties1.getDescription(), properties2.getDescription());

        List<KeyVaultRoleScope> assignableScopes1 = properties1.getAssignableScopes();
        List<KeyVaultRoleScope> assignableScopes2 = properties2.getAssignableScopes();

        if (assignableScopes1 == null && assignableScopes2 == null) {
            return;
        }

        assertNotNull(assignableScopes1);
        assertNotNull(assignableScopes2);

        assertEquals(assignableScopes1.size(), assignableScopes2.size());
        assertTrue(assignableScopes1.containsAll(assignableScopes2));

        List<KeyVaultPermission> permissions1 = properties1.getPermissions();
        List<KeyVaultPermission> permissions2 = properties2.getPermissions();

        if (permissions1 == null && permissions2 == null) {
            return;
        }

        assertNotNull(permissions1);
        assertNotNull(permissions2);

        assertEquals(permissions1.size(), permissions2.size());
    }
}
