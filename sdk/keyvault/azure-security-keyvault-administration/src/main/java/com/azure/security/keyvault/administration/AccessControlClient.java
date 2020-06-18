package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionListResult;

public class AccessControlClient {
    private final AccessControlAsyncClient asyncClient;

    /**
     * Creates an {@link AccessControlClient} that uses a {@code pipeline} to service requests
     *
     * @param asyncClient The {@link AccessControlAsyncClient} that the client routes its request through.
     */
    AccessControlClient(AccessControlAsyncClient asyncClient) {
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
     * @param scope The scope of the role definition.
     * @return The {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleDefinitionListResult listRoleDefinitions(String scope) {
        return listRoleDefinitions(scope, null);
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope  The scope of the role definition.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @return The {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RoleDefinitionListResult listRoleDefinitions(String scope, String filter) {
        return listRoleDefinitionsWithResponse(scope, filter, Context.NONE).getValue();
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope The scope of the role definition.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SimpleResponse response} whose {@link Response#getValue() value} contains the
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SimpleResponse<RoleDefinitionListResult> listRoleDefinitionsWithResponse(String scope, Context context) {
        return listRoleDefinitionsWithResponse(scope, null, context);
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope  The scope of the role definition.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @param context Additional context that is passed through the Http pipeline during the service call.
     * @return A {@link SimpleResponse response} whose {@link Response#getValue() value} contains the
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SimpleResponse<RoleDefinitionListResult> listRoleDefinitionsWithResponse(String scope, String filter,
                                                                                    Context context) {
        return asyncClient.listRoleDefinitionsWithResponse(asyncClient.getVaultUrl(), scope, filter, context).block();
    }
}
