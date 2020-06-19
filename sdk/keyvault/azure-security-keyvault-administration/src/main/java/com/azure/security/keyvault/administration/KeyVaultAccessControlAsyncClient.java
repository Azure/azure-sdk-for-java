package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImplBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.*;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultAccessControlAsyncClient} provides asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting {@link RoleAssignment}.
 * Additionally, the client supports listing {@link RoleDefinition}.
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
     * Get all {@link RoleDefinition role definitions} that are applicable at scope and above.
     *
     * @param scope  The scope of the {@link RoleDefinition}.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @return A {@link PagedFlux} containing the {@link RoleDefinition role definitions} the given scope.
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
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
     * Get all {@link RoleDefinition role definitions} that are applicable at scope and above.
     *
     * @param scope   The scope of the {@link RoleDefinition}.
     * @param filter  The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                scope as well.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link RoleDefinition role definitions} the given scope.
     * {@link RoleDefinitionListResult list of role definitions}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    PagedFlux<RoleDefinition> listRoleDefinitions(String scope, String filter, Context context) {
        return new PagedFlux<>(
            () -> listRoleDefinitionsFirstPage(vaultUrl, scope, filter, context),
            continuationToken -> listRoleDefinitionsNextPage(continuationToken, context));
    }

    /**
     * Get all {@link RoleDefinition role definitions} in the first page that are applicable at scope and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param scope    The scope of the {@link RoleDefinition}.
     * @param filter   The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                 scope as well.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleDefinition role definitions} for the
     * given scope from the first page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    Mono<PagedResponse<RoleDefinition>> listRoleDefinitionsFirstPage(String vaultUrl, String scope, String filter,
                                                                     Context context) {
        try {
            return clientImpl.getRoleDefinitions().listSinglePageAsync(vaultUrl, scope, filter,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing role definitions for scope - {}", scope))
                .doOnSuccess(response -> logger.info("Listed role definitions for scope - {}", scope))
                .doOnError(error -> logger.warning(String.format("Failed to list role definitions for scope - %s",
                    scope), error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets all the {@link RoleDefinition role definitions} given by the {@code nextPageLink} that was retrieved from
     * a call to {@link KeyVaultAccessControlAsyncClient#listRoleDefinitionsFirstPage(String, String, String, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     *                          successful call to one of the {@code listRoleDefinitions} operations.
     * @param context           Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleDefinition role definitions} for the
     * given scope from the next page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
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

    /**
     * Get all {@link RoleAssignment role assignments} that are applicable at scope and above.
     *
     * @param scope  The scope of the {@link RoleAssignment}.
     * @param filter The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *               scope as well.
     * @return A {@link PagedFlux} containing the {@link RoleAssignment role assignments} the given scope.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<RoleAssignment> listRoleAssignments(String scope, String filter) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listRoleAssignmentsFirstPage(vaultUrl, scope, filter, context)),
                continuationToken -> withContext(context -> listRoleAssignmentsNextPage(continuationToken, context)));
        } catch (RuntimeException e) {
            return new PagedFlux<>(() -> monoError(logger, e));
        }
    }

    /**
     * Get all {@link RoleAssignment role assignments} that are applicable at scope and above.
     *
     * @param scope   The scope of the {@link RoleAssignment}.
     * @param filter  The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                scope as well.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link RoleAssignment role assignments} the given scope.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    PagedFlux<RoleAssignment> listRoleAssignments(String scope, String filter, Context context) {
        return new PagedFlux<>(
            () -> listRoleAssignmentsFirstPage(vaultUrl, scope, filter, context),
            continuationToken -> listRoleAssignmentsNextPage(continuationToken, context));
    }

    /**
     * Get all {@link RoleAssignment role assignments} in the first page that are applicable at scope and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param scope    The scope of the {@link RoleAssignment}.
     * @param filter   The filter to apply on the operation. Use a "atScopeAndBelow" filter to search below the given
     *                 scope as well.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleAssignment role assignments} for the
     * given scope from the first page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} is null.
     */
    Mono<PagedResponse<RoleAssignment>> listRoleAssignmentsFirstPage(String vaultUrl, String scope, String filter,
                                                                     Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");

        try {
            return clientImpl.getRoleAssignments().listForScopeSinglePageAsync(vaultUrl, scope, filter,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing role assignments for scope - {}", scope))
                .doOnSuccess(response -> logger.info("Listed role assignments for scope - {}", scope))
                .doOnError(error -> logger.warning(String.format("Failed to list role assignments for scope - %s",
                    scope), error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets all the {@link RoleAssignment role assignments} given by the {@code nextPageLink} that was retrieved from
     * a call to {@link KeyVaultAccessControlAsyncClient#listRoleAssignments(String, String)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     *                          successful call to one of the {@code listRoleAssignments} operations.
     * @param context           Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleAssignment role assignments} for the
     * given scope from the first page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     */
    Mono<PagedResponse<RoleAssignment>> listRoleAssignmentsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleAssignments().listForScopeNextSinglePageAsync(continuationToken,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next role assignments page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next role assignments page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next role assignments page - Page {}",
                    continuationToken, error));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The scope of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing the created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope}, {@code name} or {@code properties} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> createRoleAssignment(String scope, String name, RoleAssignmentProperties properties) {
        try {
            return createRoleAssignmentWithResponse(scope, name, properties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The scope of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope}, {@code name} or {@code properties} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> createRoleAssignmentWithResponse(String scope, String name,
                                                                           RoleAssignmentProperties properties) {
        try {
            return withContext(context -> createRoleAssignmentWithResponse(scope, name, properties, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The scope of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}.
     * @param properties Properties for the {@link RoleAssignment}.
     * @param context    Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope}, {@code name} or {@code properties} are null.
     */
    Mono<Response<RoleAssignment>> createRoleAssignmentWithResponse(String scope, String name,
                                                                    RoleAssignmentProperties properties,
                                                                    Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        RoleAssignmentCreateParameters parameters = new RoleAssignmentCreateParameters().setProperties(properties);
        return clientImpl.getRoleAssignments().createWithResponseAsync(vaultUrl, scope, name, parameters,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name used of the {@link RoleAssignment}.
     * @return A {@link Mono} containing the {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> getRoleAssignment(String scope, String name) {
        try {
            return getRoleAssignmentWithResponse(scope, name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> getRoleAssignmentWithResponse(String scope, String name) {
        try {
            return withContext(context -> getRoleAssignmentWithResponse(scope, name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @param context    Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    Mono<Response<RoleAssignment>> getRoleAssignmentWithResponse(String scope, String name,
                                                                 Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");

        return clientImpl.getRoleAssignments().getWithResponseAsync(vaultUrl, scope, name,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing the {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> deleteRoleAssignment(String scope, String name) {
        try {
            return deleteRoleAssignmentWithResponse(scope, name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> deleteRoleAssignmentWithResponse(String scope, String name) {
        try {
            return withContext(context -> deleteRoleAssignmentWithResponse(scope, name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope The scope of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @param context    Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@code scope} or {@code name} are null.
     */
    Mono<Response<RoleAssignment>> deleteRoleAssignmentWithResponse(String scope, String name,
                                                                    Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");

        return clientImpl.getRoleAssignments().deleteWithResponseAsync(vaultUrl, scope, name,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }
}
