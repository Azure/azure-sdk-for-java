// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
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
     * {@link KeyVaultRoleScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, context));
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, context));
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated {@link String name}.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return The created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, UUID.randomUUID().toString(), properties, Context.NONE)
            .getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param roleAssignmentName The roleAssignmentName used to create the {@link KeyVaultRoleAssignment}. It can be any
     * valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return The created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope}, {@link String roleAssignmentName} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, roleAssignmentName, properties, Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param roleAssignmentName The roleAssignmentName used to create the {@link KeyVaultRoleAssignment}. It can be any
     * valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope}, {@link String roleAssignmentName} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName, KeyVaultRoleAssignmentProperties properties, Context context) {
        return asyncClient.createRoleAssignmentWithResponse(roleScope, roleAssignmentName, properties, context).block();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The roleAssignmentName of the {@link KeyVaultRoleAssignment}.
     * @return The {@link KeyVaultRoleAssignment}.
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
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The roleAssignmentName of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName, Context context) {
        return asyncClient.getRoleAssignmentWithResponse(roleScope, roleAssignmentName, context).block();
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The roleAssignmentName of the {@link KeyVaultRoleAssignment}.
     * @return The {@link KeyVaultRoleAssignment}.
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
     * @param roleScope The {@link KeyVaultRoleScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The roleAssignmentName of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleScope roleScope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName, Context context) {
        return asyncClient.deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, context).block();
    }
}
