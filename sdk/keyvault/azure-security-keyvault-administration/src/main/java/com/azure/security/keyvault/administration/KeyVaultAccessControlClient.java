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
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentScope;
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
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleAssignmentScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleAssignmentScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(roleScope, context));
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleAssignmentScope roleScope) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, Context.NONE));
    }

    /**
     * Get all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedIterable<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleAssignmentScope roleScope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(roleScope, context));
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated {@link UUID name}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return The created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleAssignmentScope roleScope, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, UUID.randomUUID(), properties, Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param name The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return The created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope}, {@link UUID name} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment createRoleAssignment(KeyVaultRoleAssignmentScope roleScope, UUID name, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, name, properties, Context.NONE).getValue();
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param name The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope}, {@link UUID name} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> createRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, UUID name, KeyVaultRoleAssignmentProperties properties, Context context) {
        return asyncClient.createRoleAssignmentWithResponse(roleScope, name, properties, context).block();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link UUID name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment getRoleAssignment(KeyVaultRoleAssignmentScope roleScope, String name) {
        return getRoleAssignmentWithResponse(roleScope, name, Context.NONE).getValue();
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link UUID name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> getRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name, Context context) {
        return asyncClient.getRoleAssignmentWithResponse(roleScope, name, context).block();
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link UUID name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public KeyVaultRoleAssignment deleteRoleAssignment(KeyVaultRoleAssignmentScope roleScope, String name) {
        return deleteRoleAssignmentWithResponse(roleScope, name, Context.NONE).getValue();
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return The {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link UUID name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<KeyVaultRoleAssignment> deleteRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name, Context context) {
        return asyncClient.deleteRoleAssignmentWithResponse(roleScope, name, context).block();
    }
}
