// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlClientTest extends KeyVaultAccessControlClientTestBase {
    private KeyVaultAccessControlClient client;

    /**
     * Tests that existing {@link KeyVaultRoleDefinition role definitions} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleDefinitions(HttpClient httpClient) {
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
            assertNotNull(roleDefinition.getAssignableScopes());
            assertNotNull(roleDefinition.getPermissions());
        }
    }

    /**
     * Tests that a {@link KeyVaultRoleDefinition role definition} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void setRoleDefinition(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            KeyVaultRoleDefinition roleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(roleDefinition);
            assertNotNull(roleDefinition.getId());
            assertEquals(roleDefinitionName, roleDefinition.getName());
            assertEquals(KeyVaultRoleDefinitionType.MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS,
                roleDefinition.getType());
            assertTrue(roleDefinition.getAssignableScopes().contains(KeyVaultRoleScope.GLOBAL));
            assertEquals(KeyVaultRoleType.CUSTOM_ROLE, roleDefinition.getRoleType());
            assertEquals(roleDefinitionName, roleDefinition.getRoleName());
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName, null);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleDefinition role definition} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleDefinition(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition to retrieve.
            KeyVaultRoleDefinition createdRoleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Get the role assignment.
            KeyVaultRoleDefinition retrievedRoleDefinition =
                client.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(retrievedRoleDefinition);
            assertRoleDefinitionEquals(createdRoleDefinition, retrievedRoleDefinition);
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName, null);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleDefinition role definition} can be deleted from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleDefinition(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();

        // Create a role definition to delete.
        KeyVaultRoleDefinition createdRoleDefinition =
            client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        assertNotNull(createdRoleDefinition);

        // Delete the role definition.
        Response<Void> deleteResponse =
            client.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, roleDefinitionName, Context.NONE);

        assertNotNull(deleteResponse);
        assertEquals(200, deleteResponse.getStatusCode());
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleDefinition role definition} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleDefinitionDoesNotThrow(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();
        // Try to delete a non-existent role definition.
        Response<Void> deleteResponse =
            client.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, roleDefinitionName, Context.NONE);

        assertNotNull(deleteResponse);
        assertEquals(404, deleteResponse.getStatusCode());
    }

    /**
     * Tests that existing {@link KeyVaultRoleAssignment role assignments} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleAssignments(HttpClient httpClient) {
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
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            KeyVaultRoleDefinition createdRoleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Create a role assignment.
            KeyVaultRoleAssignment createdRoleAssignment = client.createRoleAssignment(KeyVaultRoleScope.GLOBAL,
                createdRoleDefinition.getId(), servicePrincipalId, roleAssignmentName);

            assertNotNull(createdRoleAssignment);
            assertNotNull(createdRoleAssignment.getId());
            assertEquals(roleAssignmentName, createdRoleAssignment.getName());
            assertNotNull(createdRoleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = createdRoleAssignment.getProperties();

            assertNotNull(properties);
            assertEquals(servicePrincipalId, properties.getPrincipalId());
            assertEquals(createdRoleDefinition.getId(), properties.getRoleDefinitionId());
            assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName,
                    roleAssignmentName);
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
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            KeyVaultRoleDefinition createdRoleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Create a role assignment.
            client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, createdRoleDefinition.getId(), servicePrincipalId,
                roleAssignmentName);

            // Attempt to create a role assignment with the same name and scope.
            assertThrows(KeyVaultAdministrationException.class,
                () -> client.createRoleAssignment(KeyVaultRoleScope.GLOBAL, createdRoleDefinition.getId(),
                    servicePrincipalId, roleAssignmentName));
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName,
                    roleAssignmentName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleAssignment role assignment} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleAssignment(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            KeyVaultRoleDefinition createdRoleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Create a role assignment to retrieve.
            KeyVaultRoleAssignment createdRoleAssignment = client.createRoleAssignment(KeyVaultRoleScope.GLOBAL,
                createdRoleDefinition.getId(), servicePrincipalId, roleAssignmentName);

            assertNotNull(createdRoleAssignment);

            // Get the role assignment.
            KeyVaultRoleAssignment retrievedRoleAssignment =
                client.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

            assertNotNull(retrievedRoleAssignment);
            assertRoleAssignmentEquals(createdRoleAssignment, retrievedRoleAssignment);
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName,
                    roleAssignmentName);
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleAssignment role assignment} can be deleted from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleAssignment(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            KeyVaultRoleDefinition createdRoleDefinition =
                client.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

            assertNotNull(createdRoleDefinition);

            // Create a role assignment to delete.
            KeyVaultRoleAssignment createdRoleAssignment = client.createRoleAssignment(KeyVaultRoleScope.GLOBAL,
                createdRoleDefinition.getId(), servicePrincipalId, roleAssignmentName);

            assertNotNull(createdRoleAssignment);

            // Delete the role assignment.
            Response<Void> deleteResponse =
                client.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignmentName, Context.NONE);

            assertNotNull(deleteResponse);
            assertEquals(200, deleteResponse.getStatusCode());
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName,
                    roleAssignmentName);
            }
        }
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleAssignment role assignment} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleAssignmentDoesNotThrow(HttpClient httpClient) {
        client = getClientBuilder(httpClient, false).buildClient();
        String roleAssignmentName = testResourceNamer.randomUuid();
        // Try to delete a non-existent role assignment.
        Response<Void> deleteResponse =
            client.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignmentName, Context.NONE);

        assertNotNull(deleteResponse);
        assertEquals(404, deleteResponse.getStatusCode());
    }
}
