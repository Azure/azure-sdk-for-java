// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration;

import com.azure.core.http.HttpClient;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyVaultAccessControlAsyncClientTest extends KeyVaultAccessControlClientTestBase {
    private KeyVaultAccessControlAsyncClient asyncClient;

    private void getClient(HttpClient httpClient, boolean forCleanup) {
        asyncClient = getClientBuilder(buildAsyncAssertingClient(
            interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient), forCleanup)
            .buildAsyncClient();
    }

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    /**
     * Tests that existing {@link KeyVaultRoleDefinition role definitions} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleDefinitions(HttpClient httpClient) {
        getClient(httpClient, false);

        StepVerifier.create(asyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL))
            .thenConsumeWhile(roleDefinition -> {
                assertNotNull(roleDefinition.getId());
                assertNotNull(roleDefinition.getName());
                assertNotNull(roleDefinition.getType());
                assertNotNull(roleDefinition.getRoleName());
                assertNotNull(roleDefinition.getDescription());
                assertNotNull(roleDefinition.getRoleType());
                assertNotNull(roleDefinition.getAssignableScopes());
                assertNotNull(roleDefinition.getPermissions());

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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO))
                .assertNext(roleDefinition -> {
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        try {
            // Create a role definition to retrieve, then get the role assignment.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        // Create a role definition to delete, then delete the role definition.
        StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                .flatMap(createdRoleDefinition ->
                    asyncClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL,
                        createdRoleDefinition.getName())))
            .assertNext(deleteResponse -> assertEquals(200, deleteResponse.getStatusCode()))
            .expectComplete()
            .verify();
    }

    /**
     * Tests that an exception is thrown when trying to delete a non-existent
     * {@link KeyVaultRoleDefinition role definition} from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void deleteNonExistingRoleDefinitionDoesNotThrow(HttpClient httpClient) {
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();

        // Try to delete a non-existent role definition.
        StepVerifier.create(asyncClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, roleDefinitionName))
            .assertNext(deleteResponse -> assertEquals(404, deleteResponse.getStatusCode()))
            .expectComplete()
            .verify();
    }

    /**
     * Tests that existing {@link KeyVaultRoleAssignment role assignments} can be retrieved from the Key Vault.
     */
    @ParameterizedTest(name = DISPLAY_NAME)
    @MethodSource("com.azure.security.keyvault.administration.KeyVaultAdministrationClientTestBase#createHttpClients")
    public void listRoleAssignments(HttpClient httpClient) {
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleDefinition -> Mono.zip(Mono.just(roleDefinition),
                        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                            servicePrincipalId, roleAssignmentName)))
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO))
                .assertNext(tuple -> {
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
                })
                .expectComplete()
                .verify();
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleDefinition ->
                        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                            servicePrincipalId, roleAssignmentName))
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleAssignment ->
                        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL,
                            roleAssignment.getProperties().getRoleDefinitionId(), servicePrincipalId, roleAssignmentName)))
                .expectError(KeyVaultAdministrationException.class)
                .verify();
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleDefinition ->
                        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                            servicePrincipalId, roleAssignmentName))
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleAssignment -> Mono.zip(Mono.just(roleAssignment),
                        asyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignment.getName()))))
                .assertNext(tuple -> {
                    KeyVaultRoleAssignment createdRoleAssignment = tuple.getT1();
                    KeyVaultRoleAssignment retrievedRoleAssignment = tuple.getT2();

                    assertNotNull(retrievedRoleAssignment);
                    assertRoleAssignmentEquals(createdRoleAssignment, retrievedRoleAssignment);
                })
                .expectComplete()
                .verify();
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
        getClient(httpClient, false);
        String roleDefinitionName = testResourceNamer.randomUuid();
        String roleAssignmentName = testResourceNamer.randomUuid();

        try {
            // Create a role assignment to delete.
            StepVerifier.create(asyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleDefinition ->
                        asyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(), servicePrincipalId,
                            roleAssignmentName))
                    .delayElement(!interceptorManager.isPlaybackMode() ? Duration.ofSeconds(5) : Duration.ZERO)
                    .flatMap(roleAssignment ->
                        asyncClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignment.getName())))
                .assertNext(deleteResponse -> assertEquals(200, deleteResponse.getStatusCode()))
                .expectComplete()
                .verify();
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
        getClient(httpClient, false);
        String roleAssignmentName = testResourceNamer.randomUuid();

        // Try to delete a non-existent role assignment.
        StepVerifier.create(asyncClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, roleAssignmentName))
            .assertNext(deleteResponse -> assertEquals(404, deleteResponse.getStatusCode()))
            .expectComplete()
            .verify();
    }
}
