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
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL))
            .thenConsumeWhile(roleDefinition -> {
                assertNotNull(roleDefinition.getId());
                assertNotNull(roleDefinition.getName());
                assertNotNull(roleDefinition.getType());
                assertNotNull(roleDefinition.getRoleName());
                assertNotNull(roleDefinition.getDescription());
                assertNotNull(roleDefinition.getRoleType());
                assertFalse(roleDefinition.getAssignableScopes().isEmpty());
                assertFalse(roleDefinition.getPermissions().isEmpty());

                return true;
            })
            .expectComplete()
            .verify();
    }

    /**
     * Tests that a {@link KeyVaultRoleDefinition role definition} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void setRoleDefinition(HttpClient httpClient) {
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleDefinitionName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "41424788-d6f0-4218-95b9-b597a3697fe0"
            : UUID.randomUUID().toString();

        try {
            // Create a role definition.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName))
                .assertNext(roleDefinition -> {
                    assertNotNull(roleDefinition);
                    assertNotNull(roleDefinition.getId());
                    assertEquals(roleDefinitionName, roleDefinition.getName());
                    assertEquals(KeyVaultRoleDefinitionType.MICROSOFT_AUTHORIZATION_ROLE_DEFINITIONS,
                        roleDefinition.getType());
                    assertTrue(roleDefinition.getAssignableScopes().contains(KeyVaultRoleScope.GLOBAL));
                    assertEquals(KeyVaultRoleType.CUSTOM_ROLE, roleDefinition.getRoleType());
                    assertEquals(roleDefinitionName, roleDefinition.getRoleName());
                })
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();
            }
        }
    }

    /**
     * Tests that an existing {@link KeyVaultRoleDefinition role definition} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void getRoleDefinition(HttpClient httpClient) {
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleDefinitionName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "be9864ed-e256-488a-b98b-d119f9317afb"
            : UUID.randomUUID().toString();

        try {
            // Create a role definition to retrieve, then get the role assignment.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                .flatMap(createdRoleDefinition -> Mono.zip(Mono.just(createdRoleDefinition),
                    asyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, createdRoleDefinition.getName()))))
                .assertNext(tuple -> {
                    KeyVaultRoleDefinition createdRoleDefinition = tuple.getT1();
                    KeyVaultRoleDefinition retrievedRoleDefinition = tuple.getT2();

                    assertNotNull(retrievedRoleDefinition);
                    assertRoleDefinitionEquals(createdRoleDefinition, retrievedRoleDefinition);
                })
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleDefinitionName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "b6bea7ea-e108-45ed-8f02-8aa69ea2623f"
            : UUID.randomUUID().toString();

        try {
            // Create a role definition to delete, then delete the role definition.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                .flatMap(createdRoleDefinition ->
                    asyncClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL,
                        createdRoleDefinition.getName())))
                .assertNext(deleteResponse -> {
                    assertNotNull(deleteResponse);
                    assertEquals(200, deleteResponse.getStatusCode());
                })
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionName).block();
            }
        }
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleDefinition role definition} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleDefinitionDoesNotThrow(HttpClient httpClient) {
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        String roleDefinitionName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "4c4161e6-8e3e-474d-97a0-cb93cf58a46d"
            : UUID.randomUUID().toString();

        // Try to delete a non-existent role definition.
        StepVerifier.create(asyncClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, roleDefinitionName))
            .assertNext(deleteResponse -> {
                assertNotNull(deleteResponse);
                assertEquals(404, deleteResponse.getStatusCode());
            })
            .expectComplete()
            .verify();
    }

    /**
     * Tests that existing {@link KeyVaultRoleAssignment role assignments} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleAssignments(HttpClient httpClient) {
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();

        StepVerifier.create(asyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL))
            .thenConsumeWhile(roleAssignment -> {
                assertNotNull(roleAssignment.getId());
                assertNotNull(roleAssignment.getName());
                assertNotNull(roleAssignment.getType());

                KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

                assertNotNull(properties);
                assertNotNull(properties.getRoleDefinitionId());
                assertNotNull(properties.getPrincipalId());
                assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());

                return true;
            })
            .expectComplete()
            .verify();
    }

    /**
     * Tests that a {@link KeyVaultRoleAssignment role assignment} can be created in the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void createRoleAssignment(HttpClient httpClient) {
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleAssignmentName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "50b01e0e-a5af-4ab7-9965-08ec3145e6b1"
            : UUID.randomUUID().toString();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
                .filter(roleDefinition -> roleDefinition.getRoleName().equals(ROLE_NAME))
                .take(1)
                .flatMap(roleDefinition -> Mono.zip(Mono.just(roleDefinition),
                    asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                        roleAssignmentName)))
                .map(tuple -> {
                    KeyVaultRoleAssignment roleAssignment = tuple.getT2();

                    assertNotNull(roleAssignment);
                    assertNotNull(roleAssignment.getId());
                    assertEquals(roleAssignmentName, roleAssignment.getName());
                    assertNotNull(roleAssignment.getType());

                    KeyVaultRoleAssignmentProperties properties = roleAssignment.getProperties();

                    assertNotNull(properties);
                    assertEquals(servicePrincipalId, properties.getPrincipalId());
                    assertEquals(KeyVaultRoleScope.GLOBAL, properties.getScope());

                    KeyVaultRoleDefinition roleDefinition = tuple.getT1();

                    assertEquals(roleDefinition.getId(), properties.getRoleDefinitionId());

                    return tuple;
                })
                .then())
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();
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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleAssignmentName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "4553f6a9-6796-47cd-b4c9-16158db89f8a"
            : UUID.randomUUID().toString();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
                .filter(roleDefinition -> roleDefinition.getRoleName().equals(ROLE_NAME))
                .take(1)
                .flatMap(roleDefinition ->
                    asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                        servicePrincipalId, roleAssignmentName))
                .flatMap(roleAssignment ->
                    asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL,
                        roleAssignment.getProperties().getRoleDefinitionId(), servicePrincipalId, roleAssignmentName)))
                .expectError(KeyVaultAdministrationException.class)
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleAssignmentName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "16d210a8-8b3a-45fd-88de-95abb0b63e41"
            : UUID.randomUUID().toString();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
                .filter(roleDefinition -> roleDefinition.getRoleName().equals(ROLE_NAME))
                .take(1)
                .flatMap(roleDefinition ->
                    asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                        servicePrincipalId, roleAssignmentName))
                .flatMap(roleAssignment -> Mono.zip(Mono.just(roleAssignment),
                    asyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignment.getName())))
                .map(tuple -> {
                    KeyVaultRoleAssignment createdRoleAssignment = tuple.getT1();
                    KeyVaultRoleAssignment retrievedRoleAssignment = tuple.getT2();

                    assertNotNull(retrievedRoleAssignment);
                    assertRoleAssignmentEquals(createdRoleAssignment, retrievedRoleAssignment);

                    return tuple;
                })
                .then())
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleAssignmentName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "8c0bac04-c964-4c64-85ad-92ed0f199ecf"
            : UUID.randomUUID().toString();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
                .filter(roleDefinition -> roleDefinition.getRoleName().equals(ROLE_NAME))
                .take(1)
                .flatMap(roleDefinition ->
                    asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                        roleAssignmentName))
                .flatMap(roleAssignment ->
                    asyncClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignment.getName()))
                .map(deleteResponse -> {
                    assertNotNull(deleteResponse);
                    assertEquals(200, deleteResponse.getStatusCode());

                    return deleteResponse;
                })
                .then())
                .expectComplete()
                .verify();
        } finally {
            if (!interceptorManager.isPlaybackMode()) {
                // Clean up the role assignment.
                KeyVaultAccessControlAsyncClient cleanupClient =
                    getClientBuilder(httpClient, true).buildAsyncClient();

                cleanupClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName).block();
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
        Assumptions.assumeTrue(isHsmDeployed);

        asyncClient = getClientBuilder(httpClient, false).buildAsyncClient();
        String roleAssignmentName = (getTestMode() == TestMode.PLAYBACK || getTestMode() == TestMode.RECORD)
            ? "ef2f0b3c-66d6-4ce5-bee0-38d7b0b65c65"
            : UUID.randomUUID().toString();

        // Try to delete a non-existent role assignment.
        StepVerifier.create(asyncClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignmentName))
            .assertNext(deleteResponse -> {
                assertNotNull(deleteResponse);
                assertEquals(404, deleteResponse.getStatusCode());
            })
            .expectComplete()
            .verify();
    }
}
