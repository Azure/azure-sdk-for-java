// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlAsyncClientTest extends KeyVaultAccessControlClientTestBase {
    private KeyVaultAccessControlAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        beforeTestSetup();
    }

    /**
     * Tests that existing {@link KeyVaultRoleDefinition role definitions} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleDefinitions(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);

        assertTrue(roleDefinitions.iterator().hasNext());

        for (KeyVaultRoleDefinition roleDefinition : roleDefinitions) {
            assertNotNull(roleDefinition.getId());
            assertNotNull(roleDefinition.getName());
            assertNotNull(roleDefinition.getType());

            KeyVaultRoleDefinitionProperties properties = roleDefinition.getProperties();

            assertNotNull(properties);
            assertNotNull(properties.getRoleName());
            assertNotNull(properties.getDescription());
            assertNotNull(properties.getRoleType());
            assertNotEquals(0, properties.getAssignableScopes().size());
            assertNotEquals(0, properties.getPermissions().size());
        }
    }

    /**
     * Tests that existing {@link KeyVaultRoleAssignment role assignments} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleAssignments(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleAssignment> roleAssignments = new ArrayList<>();

        asyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL).subscribe(roleAssignments::add);
        sleepInLiveOrRecordMode(5000);

        for (KeyVaultRoleAssignment roleAssignment : roleAssignments) {
            assertNotNull(roleAssignment.getId());
            assertNotNull(roleAssignment.getName());
            assertNotNull(roleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

            assertNotNull(properties);
            assertNotNull(properties.getRoleDefinitionId());
            assertNotNull(properties.getPrincipalId());
        }
    }

    /**
     * Tests that a {@link KeyVaultRoleAssignment role assignment} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void createRoleAssignment(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);
        sleepInLiveOrRecordMode(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "d0bedeb4-7431-407d-81cd-278929c98218";

        try {
            // Create a role assignment.
            KeyVaultRoleAssignment roleAssignment =
                asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName, roleDefinition.getId(),
                    clientId).block();

            assertNotNull(roleAssignment);
            assertNotNull(roleAssignment.getId());
            assertEquals(roleAssignment.getName(), roleAssignmentName);
            assertNotNull(roleAssignment.getType());
            assertNotNull(roleAssignment.getScope());

            KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

            assertNotNull(properties);
            assertEquals(clientId, properties.getPrincipalId());
            assertEquals(roleDefinition.getId(), properties.getRoleDefinitionId());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleAssignment role assignment} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleAssignment(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);
        sleepInLiveOrRecordMode(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "658d6c14-98c2-4a53-a523-be8609eb7f8b";

        try {
            // Create a role assignment to retrieve.
            KeyVaultRoleAssignment createdRoleAssignment =
                asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName, roleDefinition.getId(),
                    clientId).block();

            assertNotNull(createdRoleAssignment);

            // Get the role assignment.
            KeyVaultRoleAssignment retrievedRoleAssignment =
                asyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
                    .block();

            assertNotNull(retrievedRoleAssignment);
            assertEquals(createdRoleAssignment.getId(), retrievedRoleAssignment.getId());
            assertEquals(createdRoleAssignment.getName(), retrievedRoleAssignment.getName());
            assertEquals(createdRoleAssignment.getType(), retrievedRoleAssignment.getType());
            assertEquals(createdRoleAssignment.getScope(), retrievedRoleAssignment.getScope());

            KeyVaultRoleAssignmentProperties retrievedProperties = retrievedRoleAssignment.getProperties();

            assertNotNull(retrievedProperties);
            assertEquals(clientId, retrievedProperties.getPrincipalId());
            assertEquals(roleDefinition.getId(), retrievedProperties.getRoleDefinitionId());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleAssignment role assignment} can be deleted from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleAssignment(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);
        sleepInLiveOrRecordMode(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "33785c35-4196-46b5-9d99-d5bcb2b9ca1d";

        // Create a role assignment to delete.
        KeyVaultRoleAssignment createdRoleAssignment =
            asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName, roleDefinition.getId(),
                clientId).block();

        assertNotNull(createdRoleAssignment);

        // Delete the role assignment.
        KeyVaultRoleAssignment deletedRoleAssignment =
            asyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();

        assertNotNull(deletedRoleAssignment);
        assertEquals(createdRoleAssignment.getId(), deletedRoleAssignment.getId());
        assertEquals(createdRoleAssignment.getName(), deletedRoleAssignment.getName());
        assertEquals(createdRoleAssignment.getType(), deletedRoleAssignment.getType());
        assertEquals(createdRoleAssignment.getScope(), deletedRoleAssignment.getScope());

        KeyVaultRoleAssignmentProperties retrievedProperties = deletedRoleAssignment.getProperties();

        assertNotNull(retrievedProperties);
        assertEquals(clientId, retrievedProperties.getPrincipalId());
        assertEquals(roleDefinition.getId(), retrievedProperties.getRoleDefinitionId());
    }
}
