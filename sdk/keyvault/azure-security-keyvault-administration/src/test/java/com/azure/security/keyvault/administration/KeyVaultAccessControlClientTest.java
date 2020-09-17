// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionProperties;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class KeyVaultAccessControlClientTest extends KeyVaultAccessControlClientTestBase {
    private KeyVaultAccessControlClient client;

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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleAssignmentScope.GLOBAL);

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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleAssignment> roleAssignments =
            client.listRoleAssignments(KeyVaultRoleAssignmentScope.GLOBAL);

        assertTrue(roleAssignments.iterator().hasNext());

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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleAssignmentScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
                break;
            }
        }

        assertNotNull(roleDefinition);

        UUID roleAssignmentName = UUID.fromString("d0bedeb4-7431-407d-81cd-278929c98218");
        KeyVaultRoleAssignmentProperties creationProperties =
            new KeyVaultRoleAssignmentProperties(roleDefinition.getId(), clientId);

        try {
            // Create a role assignment.
            KeyVaultRoleAssignment createdRoleAssignment =
                client.createRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName, creationProperties);

            assertNotNull(createdRoleAssignment);
            assertNotNull(createdRoleAssignment.getId());
            assertEquals(createdRoleAssignment.getName(), roleAssignmentName.toString());
            assertNotNull(createdRoleAssignment.getType());
            assertNotNull(createdRoleAssignment.getScope());

            KeyVaultRoleAssignmentProperties properties = createdRoleAssignment.getProperties();

            assertNotNull(properties);
            assertEquals(creationProperties.getPrincipalId(), properties.getPrincipalId());
            assertEquals(creationProperties.getRoleDefinitionId(), properties.getRoleDefinitionId());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName.toString());
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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleAssignmentScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        UUID roleAssignmentName = UUID.fromString("658d6c14-98c2-4a53-a523-be8609eb7f8b");
        KeyVaultRoleAssignmentProperties creationProperties =
            new KeyVaultRoleAssignmentProperties(roleDefinition.getId(), clientId);

        try {
            // Create a role assignment to retrieve.
            KeyVaultRoleAssignment createdRoleAssignment =
                client.createRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName, creationProperties);

            // Get the role assignment.
            KeyVaultRoleAssignment retrievedRoleAssignment =
                client.getRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName.toString());

            assertNotNull(retrievedRoleAssignment);
            assertEquals(createdRoleAssignment.getId(), retrievedRoleAssignment.getId());
            assertEquals(createdRoleAssignment.getName(), retrievedRoleAssignment.getName());
            assertEquals(createdRoleAssignment.getType(), retrievedRoleAssignment.getType());
            assertEquals(createdRoleAssignment.getScope(), retrievedRoleAssignment.getScope());

            KeyVaultRoleAssignmentProperties retrievedProperties = retrievedRoleAssignment.getProperties();

            assertNotNull(retrievedProperties);
            assertEquals(creationProperties.getPrincipalId(), retrievedProperties.getPrincipalId());
            assertEquals(creationProperties.getRoleDefinitionId(), retrievedProperties.getRoleDefinitionId());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName.toString());
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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleAssignmentScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getProperties().getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        UUID roleAssignmentName = UUID.fromString("33785c35-4196-46b5-9d99-d5bcb2b9ca1d");
        KeyVaultRoleAssignmentProperties creationProperties =
            new KeyVaultRoleAssignmentProperties(roleDefinition.getId(), clientId);

        // Create a role assignment to delete.
        KeyVaultRoleAssignment createdRoleAssignment =
            client.createRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName, creationProperties);

        // Delete the role assignment.
        KeyVaultRoleAssignment deletedRoleAssignment =
            client.deleteRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, roleAssignmentName.toString());

        assertNotNull(deletedRoleAssignment);
        assertEquals(createdRoleAssignment.getId(), deletedRoleAssignment.getId());
        assertEquals(createdRoleAssignment.getName(), deletedRoleAssignment.getName());
        assertEquals(createdRoleAssignment.getType(), deletedRoleAssignment.getType());
        assertEquals(createdRoleAssignment.getScope(), deletedRoleAssignment.getScope());

        KeyVaultRoleAssignmentProperties retrievedProperties = deletedRoleAssignment.getProperties();

        assertNotNull(retrievedProperties);
        assertEquals(creationProperties.getPrincipalId(), retrievedProperties.getPrincipalId());
        assertEquals(creationProperties.getRoleDefinitionId(), retrievedProperties.getRoleDefinitionId());
    }
}
