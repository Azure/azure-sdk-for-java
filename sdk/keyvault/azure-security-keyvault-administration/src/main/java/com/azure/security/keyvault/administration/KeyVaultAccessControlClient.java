package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;

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
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
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
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<RoleDefinition> listRoleDefinitions(String scope, String filter, Context context) {
        return new PagedIterable<>(asyncClient.listRoleDefinitions(scope, filter, context));
    }
}
