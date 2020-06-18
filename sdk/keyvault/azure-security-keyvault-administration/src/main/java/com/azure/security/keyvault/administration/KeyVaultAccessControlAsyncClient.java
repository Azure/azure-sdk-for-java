package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImplBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionListResult;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultAccessControlClient} provides synchronous and asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting
 * {@link com.azure.security.keyvault.administration.implementation.models.RoleAssignment}. Additionally, the
 * client supports listing {@link com.azure.security.keyvault.administration.implementation.models.RoleDefinition}.
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class, isAsync = true)
public class KeyVaultAccessControlAsyncClient {
    // Please see <a href=https://docs.microsoft.com/en-us/azure/azure-resource-manager/management/azure-services-resource-providers>here</a>
    // for more information on Azure resource provider namespaces.
    private static final String KEYVAULT_TRACING_NAMESPACE_VALUE = "Microsoft.KeyVault";

    /**
     * The logger to be used.
     */
    private final ClientLogger logger = new ClientLogger(KeyVaultAccessControlAsyncClient.class);

    /**
     * The underlying AutoRest client used to interact with the Key Vault service.
     */
    private final KeyVaultAccessControlClientImpl clientImpl;

    /**
     * The Kay Vault URL this client is associated to.
     */
    private final String vaultUrl;

    /**
     * Package private constructor to be used by {@link KeyVaultAccessControlClientBuilder}.
     */
    KeyVaultAccessControlAsyncClient(URL vaultUrl, HttpPipeline httpPipeline) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();

        clientImpl = new KeyVaultAccessControlClientImplBuilder()
            .pipeline(httpPipeline)
            .buildClient();
    }

    /**
     * Gets the URL for the Key Vault this client is associated with.
     *
     * @return The Key Vault URL.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
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
    public PagedFlux<RoleDefinition> listRoleDefinitions(String scope, String filter) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listRoleDefinitionsFirstPage(vaultUrl, scope, filter, context)),
                continuationToken -> withContext(context -> listRoleDefinitionsNextPage(continuationToken, context)));
        } catch (RuntimeException e) {
            return new PagedFlux<>(() -> monoError(logger, e));
        }
    }

    /**
     * Get all role definitions that are applicable at scope and above.
     *
     * @param scope  The scope of the role definition.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A Mono containing a {@link SimpleResponse response} whose {@link Response#getValue() value} contains the
     * {@link RoleDefinitionListResult list of role definitions}.
     */
    PagedFlux<RoleDefinition> listRoleDefinitions(String scope, String filter, Context context) {
        return new PagedFlux<>(
            () -> listRoleDefinitionsFirstPage(vaultUrl, scope, filter, context),
            continuationToken -> listRoleDefinitionsNextPage(continuationToken, context));
    }

    /**
     * Gets role definitions in the first page.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param scope    The scope of the role definition.
     * @param filter   The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                 scope as well.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} of {@link PagedResponse<RoleDefinition>} from the first page of results.
     */
    Mono<PagedResponse<RoleDefinition>> listRoleDefinitionsFirstPage(String vaultUrl, String scope, String filter,
                                                                     Context context) {
        try {
            return clientImpl.getRoleDefinitions().listSinglePageAsync(vaultUrl, scope, filter,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.info("Listing role definitions for scope - {}", scope))
                .doOnSuccess(response ->
                    logger.info("Listed role definitions for scope - {}", scope))
                .doOnError(error ->
                    logger.warning(String.format("Failed to list role definitions for scope - %s", scope), error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets all the role definitions given by the {@code nextPageLink} that was retrieved from a call to
     * {@link KeyVaultAccessControlAsyncClient#listRoleDefinitions(String, String)} ()}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     *                          successful call to one of the listRoleDefinitions operations.
     * @param context           Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} of {@link PagedResponse<RoleDefinition>} from the next page of results.
     */
    Mono<PagedResponse<RoleDefinition>> listRoleDefinitionsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleDefinitions().listNextSinglePageAsync(continuationToken,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next role definitions page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next role definitions page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next role definitions page - Page {}",
                    continuationToken, error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }
}
