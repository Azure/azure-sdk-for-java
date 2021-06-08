// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.test.TestMode;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        PagedIterable<KeyVaultRoleDefinition> roleDefinitions = client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        assertTrue(roleDefinitions.iterator().hasNext());

        for (KeyVaultRoleDefinition roleDefinition : roleDefinitions) {
            assertNotNull(roleDefinition.getId());
            assertNotNull(roleDefinition.getName());
            assertNotNull(roleDefinition.getType());
            assertNotNull(roleDefinition.getRoleName());
            assertNotNull(roleDefinition.getDescription());
            assertNotNull(roleDefinition.getRoleType());
            assertFalse(roleDefinition.getAssignableScopes().isEmpty());
            assertFalse(roleDefinition.getPermissions().isEmpty());
        }
    }

    /**
     * Tests that a {@link KeyVaultRoleDefinition role definition} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void setRoleDefinition(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        String roleDefinitionName = "91d62511-feb2-456f-80a0-5b17bbaa50ec";
        KeyVaultRoleDefinition roleDefinition = null;

        try {
            // Create a role definition.
            roleDefinition = client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(roleDefinition);
            assertNotNull(roleDefinition.getId());
            assertEquals(roleDefinitionName, roleDefinition.getName());
            assertEquals(KeyVaultRoleDefinitionType.MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS,
                roleDefinition.getType());
            assertTrue(roleDefinition.getAssignableScopes().contains(KeyVaultRoleScope.GLOBAL));
            assertEquals(KeyVaultRoleType.CUSTOM_ROLE, roleDefinition.getRoleType());
            assertEquals(roleDefinitionName, roleDefinition.getRoleName());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && roleDefinition != null) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleDefinition role definition} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleDefinition(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        String roleDefinitionName = "69dd1d15-b9c3-4252-be2e-e5ce7cbed1d5";
        KeyVaultRoleDefinition createdRoleDefinition = null;

        try {
            // Create a role definition to retrieve.
            createdRoleDefinition = client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Get the role assignment.
            KeyVaultRoleDefinition retrievedRoleDefinition =
                client.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(retrievedRoleDefinition);
            assertRoleDefinitionEquals(createdRoleDefinition, retrievedRoleDefinition);
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && createdRoleDefinition != null) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleDefinition role definition} can be deleted from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleDefinition(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        String roleDefinitionName = "6adc4e1b-ff4f-43a7-92ad-6e4ca58d354f";

        // Create a role definition to delete.
        KeyVaultRoleDefinition createdRoleDefinition =
            client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        assertNotNull(createdRoleDefinition);

        // Delete the role definition.
        KeyVaultRoleDefinition deletedRoleDefinition =
            client.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        assertNotNull(deletedRoleDefinition);
        assertRoleDefinitionEquals(createdRoleDefinition, deletedRoleDefinition);
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleDefinition role definition} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleDefinitionThrows(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        String roleDefinitionName = "475ed505-5835-48ce-b257-cdb8fa153e67";

        // Try to delete a non-existent role definition.
        assertThrows(KeyVaultAdministrationException.class,
            () -> client.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName));
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

        PagedIterable<KeyVaultRoleAssignment> roleAssignments = client.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

        assertTrue(roleAssignments.iterator().hasNext());

        for (KeyVaultRoleAssignment roleAssignment : roleAssignments) {
            assertNotNull(roleAssignment.getId());
            assertNotNull(roleAssignment.getName());
            assertNotNull(roleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

            assertNotNull(properties);
            assertNotNull(properties.getRoleDefinitionId());
            assertNotNull(properties.getPrincipalId());
            assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
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

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions = client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "d0bedeb4-7431-407d-81cd-278929c98218";

        try {
            // Create a role assignment.
            KeyVaultRoleAssignment createdRoleAssignment =
                client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                    roleAssignmentName);

            assertNotNull(createdRoleAssignment);
            assertNotNull(createdRoleAssignment.getId());
            assertEquals(roleAssignmentName, createdRoleAssignment.getName());
            assertNotNull(createdRoleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = createdRoleAssignment.getProperties();

            assertNotNull(properties);
            assertEquals(servicePrincipalId, properties.getPrincipalId());
            assertEquals(roleDefinition.getId(), properties.getRoleDefinitionId());
            assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            }
        }
    }

    /**
     * Tests that a {@link KeyVaultRoleAssignment role assignment} that already exists in the Key Vault cannot be
     * created again.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void createExistingRoleAssignmentThrows(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions = client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "9412ec53-56f1-4cd8-ab3e-cbbd38253f08";
        KeyVaultRoleAssignment roleAssignment = null;

        try {
            // Create a role assignment.
            roleAssignment =
                client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                    roleAssignmentName);

            KeyVaultRoleDefinition finalRoleDefinition = roleDefinition;

            // Attempt to create a role assignment with the same name and scope.
            assertThrows(KeyVaultAdministrationException.class,
                () -> client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, finalRoleDefinition.getId(),
                    servicePrincipalId, roleAssignmentName));
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && roleAssignment != null) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "658d6c14-98c2-4a53-a523-be8609eb7f8b";

        try {
            // Create a role assignment to retrieve.
            KeyVaultRoleAssignment createdRoleAssignment =
                client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                    roleAssignmentName);

            assertNotNull(createdRoleAssignment);

            // Get the role assignment.
            KeyVaultRoleAssignment retrievedRoleAssignment =
                client.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

            assertNotNull(retrievedRoleAssignment);
            assertRoleAssignmentEquals(createdRoleAssignment, retrievedRoleAssignment);
        } finally {
            if (getTestMode() != TestMode.PLAYBACK) {
                // Clean up the role assignment.
                KeyVaultAccessControlClient cleanupClient = getClientBuilder(httpClient, true).buildClient();

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

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "33785c35-4196-46b5-9d99-d5bcb2b9ca1d";

        // Create a role assignment to delete.
        KeyVaultRoleAssignment createdRoleAssignment =
            client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                roleAssignmentName);

        assertNotNull(createdRoleAssignment);

        // Delete the role assignment.
        KeyVaultRoleAssignment deletedRoleAssignment =
            client.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        assertNotNull(deletedRoleAssignment);
        assertRoleAssignmentEquals(createdRoleAssignment, deletedRoleAssignment);
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleAssignment role assignment} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleAssignmentThrows(HttpClient httpClient) {
        if (getTestMode() != TestMode.PLAYBACK) {
            // Currently there is no test cloud environment for Managed HSM.
            // TODO: Remove once there is a proper cloud environment available.
            return;
        }

        client = getClientBuilder(httpClient, false).buildClient();

        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            client.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "ee830d79-e3dc-4ac5-8581-b6f650aa7831";

        // Try to delete a non-existent role assignment.
        assertThrows(KeyVaultAdministrationException.class,
            () -> client.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName));
    }
}
