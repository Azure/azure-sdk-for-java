// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition -> {
                assertNotNull(roleDefinition.getId());
                assertNotNull(roleDefinition.getName());
                assertNotNull(roleDefinition.getType());
                assertNotNull(roleDefinition.getRoleName());
                assertNotNull(roleDefinition.getDescription());
                assertNotNull(roleDefinition.getRoleType());
                assertFalse(roleDefinition.getAssignableScopes().isEmpty());
                assertFalse(roleDefinition.getPermissions().isEmpty());
            });

        sleepIfRunningAgainstService(5000);
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        String roleDefinitionName = "91d62511-feb2-456f-80a0-5b17bbaa50ec";

        // Create a role definition.
        asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(roleDefinition -> {
                assertNotNull(roleDefinition);
                assertNotNull(roleDefinition.getId());
                assertEquals(roleDefinitionName, roleDefinition.getName());
                assertEquals(KeyVaultRoleDefinitionType.MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS,
                    roleDefinition.getType());
                assertTrue(roleDefinition.getAssignableScopes().contains(KeyVaultRoleScope.GLOBAL));
                assertEquals(KeyVaultRoleType.CUSTOM_ROLE, roleDefinition.getRoleType());
                assertEquals(roleDefinitionName, roleDefinition.getRoleName());

                // Clean up the role definition.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            });

        sleepIfRunningAgainstService(2000);
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        String roleDefinitionName = "69dd1d15-b9c3-4252-be2e-e5ce7cbed1d5";
        KeyVaultRoleDefinition createdRoleDefinition = null;

        try {
            List<KeyVaultRoleScope> assignableScopes = new ArrayList<>();
            assignableScopes.add(KeyVaultRoleScope.GLOBAL);
            assignableScopes.add(KeyVaultRoleScope.KEYS);

            // Create a role definition to retrieve.
            createdRoleDefinition = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();

            assertNotNull(createdRoleDefinition);

            // Get the role assignment.
            KeyVaultRoleDefinition retrievedRoleDefinition =
                asyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .block();

            assertNotNull(retrievedRoleDefinition);
            assertRoleDefinitionEquals(createdRoleDefinition, retrievedRoleDefinition);
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && createdRoleDefinition != null) {
                // Clean up the role definition.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        String roleDefinitionName = "6adc4e1b-ff4f-43a7-92ad-6e4ca58d354f";

        // Create a role definition to delete.
        KeyVaultRoleDefinition createdRoleDefinition =
            asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();

        assertNotNull(createdRoleDefinition);

        // Delete the role definition.
        KeyVaultRoleDefinition deletedRoleDefinition =
            asyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();

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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        String roleDefinitionName = "475ed505-5835-48ce-b257-cdb8fa153e67";

        // Try to delete a non-existent role definition.
        assertThrows(KeyVaultAdministrationException.class,
            () -> asyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block());
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

        asyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleAssignment -> {
                assertNotNull(roleAssignment.getId());
                assertNotNull(roleAssignment.getName());
                assertNotNull(roleAssignment.getType());

                KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

                assertNotNull(properties);
                assertNotNull(properties.getRoleDefinitionId());
                assertNotNull(properties.getPrincipalId());
                assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
            });

        sleepIfRunningAgainstService(5000);
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
        sleepIfRunningAgainstService(5000);

        assertFalse(roleDefinitions.isEmpty());

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "d0bedeb4-7431-407d-81cd-278929c98218";

        // Create a role assignment.
        KeyVaultRoleDefinition finalRoleDefinition = roleDefinition;

        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
            roleAssignmentName).subscribe(roleAssignment -> {
                assertNotNull(roleAssignment);
                assertNotNull(roleAssignment.getId());
                assertEquals(roleAssignmentName, roleAssignment.getName());
                assertNotNull(roleAssignment.getType());

                KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

                assertNotNull(properties);
                assertEquals(servicePrincipalId, properties.getPrincipalId());
                assertEquals(finalRoleDefinition.getId(), properties.getRoleDefinitionId());
                assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());

                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();
            });
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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);
        sleepIfRunningAgainstService(5000);

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
                asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                    roleAssignmentName).block();

            KeyVaultRoleDefinition finalRoleDefinition = roleDefinition;

            // Attempt to create a role assignment with the same roe scope, name, role definition ID and principal ID.
            assertThrows(KeyVaultAdministrationException.class,
                () -> asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, finalRoleDefinition.getId(),
                    servicePrincipalId, roleAssignmentName).block());
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && roleAssignment != null) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();
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
        sleepIfRunningAgainstService(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "658d6c14-98c2-4a53-a523-be8609eb7f8b";
        KeyVaultRoleAssignment createdRoleAssignment = null;

        try {
            // Create a role assignment to retrieve.
            createdRoleAssignment =
                asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                    roleAssignmentName).block();

            assertNotNull(createdRoleAssignment);

            // Get the role assignment.
            KeyVaultRoleAssignment retrievedRoleAssignment =
                asyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();

            assertNotNull(retrievedRoleAssignment);
            assertRoleAssignmentEquals(createdRoleAssignment, retrievedRoleAssignment);
        } finally {
            if (getTestMode() != TestMode.PLAYBACK && createdRoleAssignment != null) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient = getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();
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
        sleepIfRunningAgainstService(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "33785c35-4196-46b5-9d99-d5bcb2b9ca1d";

        // Create a role assignment to delete.
        KeyVaultRoleAssignment createdRoleAssignment =
            asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                roleAssignmentName).block();

        assertNotNull(createdRoleAssignment);

        // Delete the role assignment.
        KeyVaultRoleAssignment deletedRoleAssignment =
            asyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();

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

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).subscribe(roleDefinitions::add);
        sleepIfRunningAgainstService(5000);

        KeyVaultRoleDefinition roleDefinition = null;

        for (KeyVaultRoleDefinition currentRoleDefinition : roleDefinitions) {
            if (currentRoleDefinition.getRoleName().equals(ROLE_NAME)) {
                roleDefinition = currentRoleDefinition;

                break;
            }
        }

        assertNotNull(roleDefinition);

        String roleAssignmentName = "ee830d79-e3dc-4ac5-8581-b6f650aa7831";

        // Try to delete a non-existent role assignment.
        assertThrows(KeyVaultAdministrationException.class,
            () -> asyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block());
    }
}
