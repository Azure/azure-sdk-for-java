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
import com.azure.security.keyvault.administration.models.RoleScope;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultAccessControlAsyncClient} provides asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting {@link RoleAssignment role
 * assignments}. Additionally, the client supports listing {@link RoleDefinition role definitions}.
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
        return vaultUrl;
    }

    /**
     * Get all {@link RoleDefinition role definitions} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleDefinition role definitions}.
     * @return A {@link PagedFlux} containing the {@link RoleDefinition role definitions} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<RoleDefinition> listRoleDefinitions(RoleScope scope) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listRoleDefinitionsFirstPage(vaultUrl, scope, context)),
                continuationToken -> withContext(context -> listRoleDefinitionsNextPage(continuationToken, context)));
        } catch (RuntimeException e) {
            return new PagedFlux<>(() -> monoError(logger, e));
        }
    }

    /**
     * Get all {@link RoleDefinition role definitions} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleDefinition role definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link RoleDefinition role definitions} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    PagedFlux<RoleDefinition> listRoleDefinitions(RoleScope scope, Context context) {
        return new PagedFlux<>(
            () -> listRoleDefinitionsFirstPage(vaultUrl, scope, context),
            continuationToken -> listRoleDefinitionsNextPage(continuationToken, context));
    }

    /**
     * Get all {@link RoleDefinition role definitions} in the first page that are applicable at the given
     * {@link RoleScope scope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param scope    The {@link RoleScope scope} of the {@link RoleDefinition}.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleDefinition role definitions} for the
     * given {@link RoleScope scope} from the first page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    Mono<PagedResponse<RoleDefinition>> listRoleDefinitionsFirstPage(String vaultUrl, RoleScope scope,
                                                                     Context context) {
        try {
            return clientImpl.getRoleDefinitions().listSinglePageAsync(vaultUrl, scope.toString(), null,
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
     * a call to
     * {@link KeyVaultAccessControlAsyncClient#listRoleDefinitionsFirstPage(String, RoleScope, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     *                          successful call to one of the {@code listRoleDefinitions} operations.
     * @param context           Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleDefinition role definitions} for the
     * given {@link RoleScope scope} from the next page of results.
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
     * Get all {@link RoleAssignment role assignments} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @return A {@link PagedFlux} containing the {@link RoleAssignment role assignments} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedFlux<RoleAssignment> listRoleAssignments(RoleScope scope) {
        try {
            return new PagedFlux<>(
                () -> withContext(context -> listRoleAssignmentsFirstPage(vaultUrl, scope, context)),
                continuationToken -> withContext(context -> listRoleAssignmentsNextPage(continuationToken, context)));
        } catch (RuntimeException e) {
            return new PagedFlux<>(() -> monoError(logger, e));
        }
    }

    /**
     * Get all {@link RoleAssignment role assignments} that are applicable at the given {@link RoleScope
     * scope} and above.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link RoleAssignment role assignments} for the given
     * {@link RoleScope scope}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    PagedFlux<RoleAssignment> listRoleAssignments(RoleScope scope, Context context) {
        return new PagedFlux<>(
            () -> listRoleAssignmentsFirstPage(vaultUrl, scope, context),
            continuationToken -> listRoleAssignmentsNextPage(continuationToken, context));
    }

    /**
     * Get all {@link RoleAssignment role assignments} in the first page that are applicable at
     * the given {@link RoleScope scope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param scope    The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param context  Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleAssignment role assignments} in the
     * given {@link RoleScope scope} from the first page of results.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} is {@code null}.
     */
    Mono<PagedResponse<RoleAssignment>> listRoleAssignmentsFirstPage(String vaultUrl, RoleScope scope,
                                                                     Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");

        try {
            return clientImpl.getRoleAssignments().listForScopeSinglePageAsync(vaultUrl, scope.toString(), null,
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
     * a call to {@link KeyVaultAccessControlAsyncClient#listRoleAssignments(RoleScope)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     *                          successful call to one of the {@code listRoleAssignments} operations.
     * @param context           Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link RoleAssignment role assignments} for the
     * given {@link RoleScope scope} from the first page of results.
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
     * Creates a {@link RoleAssignment} with a randomly generated {@link UUID name}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing the created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> createRoleAssignment(RoleScope scope, RoleAssignmentProperties properties) {
        return createRoleAssignment(scope, UUID.randomUUID(), properties);
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing the created {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> createRoleAssignment(RoleScope scope, UUID name, RoleAssignmentProperties properties) {
        try {
            return createRoleAssignmentWithResponse(scope, name, properties).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Creates a {@link RoleAssignment} with a randomly generated {@link UUID name}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> createRoleAssignmentWithResponse(RoleScope scope,
                                                                           RoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(scope, UUID.randomUUID(), properties);
    }

    /**
     * Creates a {@link RoleAssignment}.
     *
     * @param scope      The {@link RoleScope scope} of the {@link RoleAssignment} to create.
     * @param name       The name used to create the {@link RoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope}, {@link UUID name} or
     * {@link RoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> createRoleAssignmentWithResponse(RoleScope scope, UUID name,
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
    Mono<Response<RoleAssignment>> createRoleAssignmentWithResponse(RoleScope scope, UUID name,
                                                                    RoleAssignmentProperties properties,
                                                                    Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(properties, "'properties' cannot be null.");

        RoleAssignmentCreateParameters parameters = new RoleAssignmentCreateParameters().setProperties(properties);
        return clientImpl.getRoleAssignments().createWithResponseAsync(vaultUrl, scope.toString(), name.toString(), parameters,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name used of the {@link RoleAssignment}.
     * @return A {@link Mono} containing the {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> getRoleAssignment(RoleScope scope, String name) {
        try {
            return getRoleAssignmentWithResponse(scope, name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> getRoleAssignmentWithResponse(RoleScope scope, String name) {
        try {
            return withContext(context -> getRoleAssignmentWithResponse(scope, name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Gets a {@link RoleAssignment}.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name    The name of the {@link RoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    Mono<Response<RoleAssignment>> getRoleAssignmentWithResponse(RoleScope scope, String name, Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");

        return clientImpl.getRoleAssignments().getWithResponseAsync(vaultUrl, scope.toString(), name,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing the {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the operation is unsuccessful.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RoleAssignment> deleteRoleAssignment(RoleScope scope, String name) {
        try {
            return deleteRoleAssignmentWithResponse(scope, name).flatMap(FluxUtil::toMono);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name  The name of the {@link RoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RoleAssignment>> deleteRoleAssignmentWithResponse(RoleScope scope, String name) {
        try {
            return withContext(context -> deleteRoleAssignmentWithResponse(scope, name, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Deletes a {@link RoleAssignment}.
     *
     * @param scope   The {@link RoleScope scope} of the {@link RoleAssignment}.
     * @param name    The name of the {@link RoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link RoleAssignment}.
     * @throws KeyVaultErrorException if the request is rejected by the server.
     * @throws NullPointerException   if the {@link RoleScope scope} or {@link UUID name} are {@code null}.
     */
    Mono<Response<RoleAssignment>> deleteRoleAssignmentWithResponse(RoleScope scope, String name, Context context) {
        Objects.requireNonNull(scope, "'scope' cannot be null.");
        Objects.requireNonNull(name, "'name' cannot be null.");

        return clientImpl.getRoleAssignments().deleteWithResponseAsync(vaultUrl, scope.toString(), name,
            context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error));
    }
}
