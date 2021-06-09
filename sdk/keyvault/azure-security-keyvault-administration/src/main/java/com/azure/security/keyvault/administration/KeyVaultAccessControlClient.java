// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * The {@link KeyVaultAccessControlClient} provides synchronous methods to view and manage Role Based Access for the
 * Azure Key Vault. The client supports creating, listing, updating, and deleting {@link KeyVaultRoleAssignment role
 * assignments}. Additionally, the client supports listing {@link KeyVaultRoleDefinition role definitions}.
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class)
public final class KeyVaultAccessControlClient {
    private final KeyVaultAccessControlAsyncClient asyncClient;

    /**
     * Creates an {@link KeyVaultAccessControlClient} that uses a {@link com.azure.core.http.HttpPipeline pipeline}
     * to service requests.
     *
     * @param asyncClient The {@link KeyVaultAccessControlAsyncClient} that this client routes its request through.
     */
    KeyVaultAccessControlClient(KeyVaultAccessControlAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Gets the URL for the Key Vault this client is associated with.
     *
     * @return The Key Vault URL.
     */
    public String getVaultUrl() {
        return asyncClient.getVaultUrl();
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleDefinition role definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, context));
    }

    /**
     * Creates a {@link KeyVaultRoleDefinition} with a randomly generated {@link String name}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM only
     * supports '/'.
     *
     * @return The created {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope) {
        return asyncClient.setRoleDefinition(roleScope).block();
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM
     * only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}. It can be any valid UUID.
     *
     * @return The created or updated {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition setRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return asyncClient.setRoleDefinition(roleScope, roleDefinitionName).block();
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition}.
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the created or updated
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If any parameter in {@code options} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * in the {@link SetRoleDefinitionOptions options} object are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options,
                                                                          Context context) {
        return asyncClient.setRoleDefinitionWithResponse(options, context).block();
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name used of the {@link KeyVaultRoleDefinition}.
     *
     * @return The retrieved {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition getRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return asyncClient.getRoleDefinition(roleScope, roleDefinitionName).block();
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the
     * retrieved {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                          String roleDefinitionName, Context context) {
        return asyncClient.getRoleDefinitionWithResponse(roleScope, roleDefinitionName, context).block();
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM
     * only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     *
     * @return The deleted {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleDefinition deleteRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return asyncClient.deleteRoleDefinition(roleScope, roleDefinitionName).block();
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Response} whose {@link Response#getValue() value} contains the deleted
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleDefinition> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                             String roleDefinitionName,
                                                                             Context context) {
        return asyncClient.deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName, context).block();
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope scope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, context));
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated {@link String name}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId}
     * are invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope}, {@link String roleDefinitionId} or
     * {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
                                                       String principalId) {
        return createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, UUID.randomUUID().toString(),
            Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return The created {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name already
     * exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
                                                       String principalId, String roleAssignmentName) {
        return createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, roleAssignmentName,
            Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given
     * name already exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are
     * invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                             String roleDefinitionId,
                                                                             String principalId,
                                                                             String roleAssignmentName,
                                                                             Context context) {
        return asyncClient.createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId,
            roleAssignmentName, context).block();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     *
     * @return The {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment getRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return getRoleAssignmentWithResponse(roleScope, roleAssignmentName, Context.NONE).getValue();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                          String roleAssignmentName, Context context) {
        return asyncClient.getRoleAssignmentWithResponse(roleScope, roleAssignmentName, context).block();
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     *
     * @return The {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment deleteRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, Context.NONE).getValue();
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return The {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                             String roleAssignmentName,
                                                                             Context context) {
        return asyncClient.deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, context).block();
    }
}
