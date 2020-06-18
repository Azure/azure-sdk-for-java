package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.AccessControlRestClientBuilder;
import com.azure.security.keyvault.administration.implementation.AccessControlRestClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionListResult;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link AccessControlClient} provides synchronous and asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting
 * {@link com.azure.security.keyvault.administration.implementation.models.RoleAssignment}. Additionally, the
 * client supports listing {@link com.azure.security.keyvault.administration.implementation.models.RoleDefinition}.
 */
@ServiceClient(builder = AccessControlClientBuilder.class, isAsync = true)
public class AccessControlAsyncClient {
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    /**
     * The logger to be used.
     */
    private final ClientLogger logger = new ClientLogger(AccessControlAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Key Vault service.
     */
    private final AccessControlRestClientImpl restClient;

    /**
     * The Kay Vault URI this client is associated to.
     */
    private final String vaultUrl;

    /**
     * Role-based Access Control REST API version.
     */
    private final String serviceVersion;

    /**
     * Package private constructor to be used by {@link AccessControlClientBuilder}.
     */
    AccessControlAsyncClient(URL vaultUrl, HttpPipeline httpPipeline, AccessControlServiceVersion serviceVersion) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();

        restClient = new AccessControlRestClientBuilder()
            .apiVersion(serviceVersion.getVersion())
            .pipeline(httpPipeline)
            .build();
    }

    /**
     * Gets the URI for the Key Vault this client is associated to.
     *
     * @return The Key Vault URI.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }


    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope The scope of the role definition.
     * @return A Mono containing the {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleDefinitionListResult> listRoleDefinitions(String scope) {
        return listRoleDefinitions(scope, null);
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope The scope of the role definition.
     * @return A Mono containing the {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleDefinitionListResult> listRoleDefinitions(String scope, String filter) {
        try {
            return listRoleDefinitionsWithResponse(scope, filter).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope The scope of the role definition.
     * @return A Mono containing a {@link SimpleResponse response} whose {@link Response#getValue() value} contains the
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<RoleDefinitionListResult>> listRoleDefinitionsWithResponse(String scope) {
        return listRoleDefinitionsWithResponse(scope, null);
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope  The scope of the role definition.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @return A Mono containing a {@link SimpleResponse response} whose {@link Response#getValue() value} contains the
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws com.azure.security.keyvault.administration.implementation.models.KeyVaultErrorException if the
     * operation is unsuccessful.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SimpleResponse<RoleDefinitionListResult>> listRoleDefinitionsWithResponse(String scope, String filter) {
        try {
            return withContext(context -> listRoleDefinitionsWithResponse(vaultUrl, scope, filter, context));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    Mono<SimpleResponse<RoleDefinitionListResult>> listRoleDefinitionsWithResponse(String vaultUrl, String scope,
                                                                                   String filter, Context context) {
        return restClient.roleDefinitions().listWithRestResponseAsync(vaultUrl, scope, filter,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Listing role definitions for scope - {}", scope))
            .doOnSuccess(response -> logger.info("Retrieved role definitions for scope - {}", scope))
            .doOnError(error -> logger.warning("Failed to list role definitions for scope - {}", scope));
    }
}
