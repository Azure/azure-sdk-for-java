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

/**
 * The {@link KeyVaultAccessControlClient} provides synchronous methods to view and manage Role Based Access for the
 * Azure Key Vault. The client supports creating, listing, updating, and deleting {@link RoleAssignment}.
 * Additionally, the client supports listing {@link RoleDefinition}.
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class)
public class KeyVaultAccessControlClient {
    private final KeyVaultAccessControlAsyncClient asyncClient;

    /**
     * Creates an {@link KeyVaultAccessControlClient} that uses a {@code pipeline} to service requests
     *
     * @param asyncClient The {@link KeyVaultAccessControlAsyncClient} that the client routes its request through.
     */
    KeyVaultAccessControlClient(KeyVaultAccessControlAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Get the vault endpoint URL.
     *
     * @return The vault endpoint URL.
     */
    public String getVaultUrl() {
        return asyncClient.getVaultUrl();
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope   The scope of the role definition.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of {@link RoleDefinition role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    public PagedIterable<RoleDefinition> listRoleDefinitions(String scope, Context context) {
        return listRoleDefinitions(scope, null, context);
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope   The scope of the role definition.
     * @param filter  The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                scope as well.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of {@link RoleDefinition role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    public PagedIterable<RoleDefinition> listRoleDefinitions(String scope, String filter, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(scope, filter, context));
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope   The scope of the role definition.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of {@link RoleDefinition role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    public PagedIterable<RoleAssignment> listRoleAssignments(String scope, Context context) {
        return listRoleAssignments(scope, null, context);
    }

    /**
     * Get all role assignments that are applicable at scope and above.
     *
     * @param scope   The scope of the role assignment.
     * @param filter  The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                scope as well.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedIterable} of {@link RoleAssignment role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    public PagedIterable<RoleAssignment> listRoleAssignments(String scope, String filter, Context context) {
        return new PagedIterable<>(asyncClient.listRoleAssignments(scope, filter, context));
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The scope of the role assignment to create.
     * @param name       The name used to create the role assignment.
     * @param properties Properties for the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope}, {@code name} or {@code properties} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment createRoleAssignment(String scope, String name, RoleAssignmentProperties properties) {
        return createKeyWithResponse(scope, name, properties, Context.NONE).getValue();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The scope of the role assignment to create.
     * @param name       The name used to create the role assignment.
     * @param properties Properties for the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope}, {@code name} or {@code properties} are null.
     */
    public Response<RoleAssignment> createKeyWithResponse(String scope, String name,
                                                          RoleAssignmentProperties properties, Context context) {
        return asyncClient.createRoleAssignmentWithResponse(scope, name, properties, context).block();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the role assignment to create.
     * @param name  The name used to create the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment getRoleAssignment(String scope, String name) {
        return getKeyWithResponse(scope, name, Context.NONE).getValue();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the role assignment to create.
     * @param name  The name used to create the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    public Response<RoleAssignment> getKeyWithResponse(String scope, String name, Context context) {
        return asyncClient.getRoleAssignmentWithResponse(scope, name, context).block();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the role assignment to create.
     * @param name  The name used to create the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleAssignment deleteRoleAssignment(String scope, String name) {
        return deleteKeyWithResponse(scope, name, Context.NONE).getValue();
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the role assignment to create.
     * @param name  The name used to create the role assignment.
     * @return The created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    public Response<RoleAssignment> deleteKeyWithResponse(String scope, String name, Context context) {
        return asyncClient.deleteRoleAssignmentWithResponse(scope, name, context).block();
    }
}
