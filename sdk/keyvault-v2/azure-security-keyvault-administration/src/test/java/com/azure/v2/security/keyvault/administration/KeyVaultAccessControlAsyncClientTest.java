// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration;

import com.azure.v2.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleType;
import io.clientcore.core.http.client.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlAsyncClientTest extends KeyVaultAccessControlClientTestBase {
    private KeyVaultAccessControlAsyncClient asyncClient;

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient
            = getClientBuilder(
                buildAsyncAssertingClient(
                    interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient),
                forCleanup).buildAsyncClient();

        if (!interceptorManager.isLiveMode()) {
            // Remove `id` and `name` sanitizers from the list of common sanitizers.
            interceptorManager.removeSanitizers("AZSDK3430", "AZSDK3493");
        }
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new TestUtils.AssertingHttpClientBuilder(httpClient).assertAsync().build();
    }

    /**
     * Tests that existing {@link KeyVaultRoleDefinition role definitions} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleDefinitions(HttpClient httpClient) {
        getClient(httpClient, false);

        // Test that we can iterate through role definitions
        asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).forEach(roleDefinition -> {
            assertNotNull(roleDefinition.getId());
            assertNotNull(roleDefinition.getName());
            assertNotNull(roleDefinition.getType());
            assertNotNull(roleDefinition.getRoleName());
            assertNotNull(roleDefinition.getDescription());
            assertNotNull(roleDefinition.getRoleType());
            assertNotNull(roleDefinition.getAssignableScopes());
            assertNotNull(roleDefinition.getPermissions());
        });
    }

    /**
     * Tests that a {@link KeyVaultRoleDefinition role definition} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void setRoleDefinition(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            CompletableFuture<KeyVaultRoleDefinition> future = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition roleDefinition = future.join();

            assertNotNull(roleDefinition);
            assertNotNull(roleDefinition.getId());
            assertEquals(roleDefinitionName, roleDefinition.getName());
            assertEquals(KeyVaultRoleDefinitionType.MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS, roleDefinition.getType());
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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleDefinition(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition to retrieve.
            CompletableFuture<KeyVaultRoleDefinition> createFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition createdRoleDefinition = createFuture.join();

            assertNotNull(createdRoleDefinition);

            // Introduce delay for async processing if not in playback mode
            if (!interceptorManager.isPlaybackMode()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Get the role definition.
            CompletableFuture<KeyVaultRoleDefinition> getFuture = asyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition retrievedRoleDefinition = getFuture.join();

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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleDefinition(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        // Create a role definition to delete.
        CompletableFuture<KeyVaultRoleDefinition> createFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
        KeyVaultRoleDefinition createdRoleDefinition = createFuture.join();

        assertNotNull(createdRoleDefinition);

        // Delete the role definition.
        CompletableFuture<Void> deleteFuture = asyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
        deleteFuture.join(); // Verify deletion completes without exception
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleDefinition role definition} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleDefinitionDoesNotThrow(HttpClient httpClient) {
        getClient(httpClient, false);

        CompletableFuture<Void> future = asyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, "non-existing");
        assertThrows(Exception.class, future::join);
    }

    /**
     * Tests that existing {@link KeyVaultRoleAssignment role assignments} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleAssignments(HttpClient httpClient) {
        getClient(httpClient, false);

        // Test that we can iterate through role assignments
        asyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL).forEach(roleAssignment -> {
            assertNotNull(roleAssignment.getId());
            assertNotNull(roleAssignment.getName());
            assertNotNull(roleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

            assertNotNull(properties);
            assertNotNull(properties.getRoleDefinitionId());
            assertNotNull(properties.getPrincipalId());
            assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
        });
    }

    /**
     * Tests that a {@link KeyVaultRoleAssignment role assignment} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void createRoleAssignment(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            CompletableFuture<KeyVaultRoleDefinition> roleDefFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition roleDefinition = roleDefFuture.join();

            assertNotNull(roleDefinition);

            // Introduce delay for async processing if not in playback mode
            if (!interceptorManager.isPlaybackMode()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Create a role assignment.
            CompletableFuture<KeyVaultRoleAssignment> roleAssignFuture = asyncClient.createRoleAssignment(
                KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId, roleAssignmentName);
            KeyVaultRoleAssignment roleAssignment = roleAssignFuture.join();

            assertNotNull(roleAssignment);
            assertNotNull(roleAssignment.getId());
            assertEquals(roleAssignmentName, roleAssignment.getName());
            assertNotNull(roleAssignment.getType());

            KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

            assertNotNull(properties);
            assertNotNull(properties.getPrincipalId());
            assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());
            assertEquals(roleDefinition.getId(), properties.getRoleDefinitionId());
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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void createExistingRoleAssignmentThrows(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            CompletableFuture<KeyVaultRoleDefinition> roleDefFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition roleDefinition = roleDefFuture.join();

            assertNotNull(roleDefinition);

            // Introduce delay for async processing if not in playback mode
            if (!interceptorManager.isPlaybackMode()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Create a role assignment.
            CompletableFuture<KeyVaultRoleAssignment> roleAssignFuture1 = asyncClient.createRoleAssignment(
                KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId, roleAssignmentName);
            roleAssignFuture1.join();

            // Attempt to create a role assignment with the same name.
            CompletableFuture<KeyVaultRoleAssignment> roleAssignFuture2 = asyncClient.createRoleAssignment(
                KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId, roleAssignmentName);

            assertThrows(Exception.class, roleAssignFuture2::join);
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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleAssignment(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            CompletableFuture<KeyVaultRoleDefinition> roleDefFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition roleDefinition = roleDefFuture.join();

            assertNotNull(roleDefinition);

            // Introduce delay for async processing if not in playback mode
            if (!interceptorManager.isPlaybackMode()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Create a role assignment to retrieve.
            CompletableFuture<KeyVaultRoleAssignment> createAssignFuture = asyncClient.createRoleAssignment(
                KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId, roleAssignmentName);
            KeyVaultRoleAssignment createdRoleAssignment = createAssignFuture.join();

            assertNotNull(createdRoleAssignment);

            // Get the role assignment.
            CompletableFuture<KeyVaultRoleAssignment> getAssignFuture = asyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            KeyVaultRoleAssignment retrievedRoleAssignment = getAssignFuture.join();

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
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteRoleAssignment(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            CompletableFuture<KeyVaultRoleDefinition> roleDefFuture = asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);
            KeyVaultRoleDefinition roleDefinition = roleDefFuture.join();

            assertNotNull(roleDefinition);

            // Introduce delay for async processing if not in playback mode
            if (!interceptorManager.isPlaybackMode()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // Create a role assignment to delete.
            CompletableFuture<KeyVaultRoleAssignment> createAssignFuture = asyncClient.createRoleAssignment(
                KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId, roleAssignmentName);
            KeyVaultRoleAssignment createdRoleAssignment = createAssignFuture.join();

            assertNotNull(createdRoleAssignment);

            // Delete the role assignment.
            CompletableFuture<Void> deleteFuture = asyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);
            deleteFuture.join(); // Verify deletion completes without exception
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                cleanUpResources(getClientBuilder(httpClient, true).buildClient(), roleDefinitionName, null);
            }
        }
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleAssignment role assignment} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.v2.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleAssignmentDoesNotThrow(HttpClient httpClient) {
        getClient(httpClient, false);

        CompletableFuture<Void> future = asyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, "non-existing");
        assertThrows(Exception.class, future::join);
    }
}
