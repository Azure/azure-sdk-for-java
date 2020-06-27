package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentProperties;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.models.RoleScope;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * The {@link KeyVaultAccessControlClient} provides synchronous methods to view and manage Role Based Access for the
 * Azure Key Vault. The client supports creating, listing, updating, and deleting {@link RoleAssignment role
 * assignments}. Additionally, the client supports listing {@link RoleDefinition role definitions}.
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class)
public class KeyVaultAccessControlClient {
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
     * Get all {@link RoleDefinition role definitions} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleDefinition role definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link RoleDefinition role definitions} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    public PagedIterable<RoleDefinition> listRoleDefinitions(RoleScope scope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(scope, context));
    }

    /**
     * Get all {@link RoleAssignment role assignments} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} containing the {@link RoleAssignment role assignments} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    public PagedIterable<RoleAssignment> listRoleAssignments(RoleScope scope, Context context) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(scope, context));
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment createRoleAssignment(RoleScope scope, UUID name,
                                               RoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(scope, name, properties, Context.NONE).getValue();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link RoleAssignment}.
     * @param context    Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    public Response<RoleAssignment> createRoleAssignmentWithResponse(RoleScope scope, UUID name,
                                                                     RoleAssignmentProperties properties,
                                                                     Context context) {
        return asyncClient.createRoleAssignmentWithResponse(scope, name, properties, context).block();
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return The {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment getRoleAssignment(RoleScope scope, String name) {
        return getRoleAssignmentWithResponse(scope, name, Context.NONE).getValue();
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return The {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    public Response<RoleAssignment> getRoleAssignmentWithResponse(RoleScope scope, String name, Context context) {
        return asyncClient.getRoleAssignmentWithResponse(scope, name, context).block();
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return The {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment deleteRoleAssignment(RoleScope scope, String name) {
        return deleteRoleAssignmentWithResponse(scope, name, Context.NONE).getValue();
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return The {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    public Response<RoleAssignment> deleteRoleAssignmentWithResponse(RoleScope scope, String name, Context context) {
        return asyncClient.deleteRoleAssignmentWithResponse(scope, name, context).block();
    }
}
