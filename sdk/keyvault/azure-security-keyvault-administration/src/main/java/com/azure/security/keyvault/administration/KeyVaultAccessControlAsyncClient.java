// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImplBuilder;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.Permission;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentProperties;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentPropertiesWithScope;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionProperties;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultAccessControlAsyncClient} provides asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting
 * {@link KeyVaultRoleAssignment role assignments}. Additionally, the client supports listing
 * {@link KeyVaultRoleDefinition role definitions}.
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class, isAsync = true)
public final class KeyVaultAccessControlAsyncClient {
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
     * Lists all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleAssignmentScope roleScope) {
        return new PagedFlux<>(
            () -> withContext(context -> listRoleDefinitionsFirstPage(vaultUrl, roleScope, context)),
            continuationToken -> withContext(context -> listRoleDefinitionsNextPage(continuationToken, context)));
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleDefinition role
     * definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    PagedFlux<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleAssignmentScope roleScope, Context context) {
        return new PagedFlux<>(
            () -> listRoleDefinitionsFirstPage(vaultUrl, roleScope, context),
            continuationToken -> listRoleDefinitionsNextPage(continuationToken, context));
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} in the first page that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleDefinition role definitions}
     * for the given {@link KeyVaultRoleAssignmentScope roleScope} from the first page of results.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    Mono<PagedResponse<KeyVaultRoleDefinition>> listRoleDefinitionsFirstPage(String vaultUrl, KeyVaultRoleAssignmentScope roleScope, Context context) {
        Objects.requireNonNull(roleScope,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleScope'"));

        try {
            return clientImpl.getRoleDefinitions()
                .listSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing role definitions for roleScope - {}", roleScope))
                .doOnSuccess(response -> logger.info("Listed role definitions for roleScope - {}", roleScope))
                .doOnError(error -> logger.warning(String.format("Failed to list role definitions for roleScope - %s",
                    roleScope), error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} given by the {@code nextPageLink} that was retrieved
     * from a call to
     * {@link KeyVaultAccessControlAsyncClient#listRoleDefinitionsFirstPage(String, KeyVaultRoleAssignmentScope, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     * successful call to one of the {@code listKeyVaultRoleDefinitions} operations.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleDefinition role definitions}
     * for the given {@link KeyVaultRoleAssignmentScope roleScope} from the next page of results.
     */
    Mono<PagedResponse<KeyVaultRoleDefinition>> listRoleDefinitionsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleDefinitions()
                .listNextSinglePageAsync(continuationToken, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next role definitions page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next role definitions page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next role definitions page - Page {}",
                    continuationToken, error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleAssignmentScope roleScope) {
        return new PagedFlux<>(
            () -> withContext(context -> listRoleAssignmentsFirstPage(vaultUrl, roleScope, context)),
            continuationToken -> withContext(context -> listRoleAssignmentsNextPage(continuationToken, context)));
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleAssignmentScope roleScope}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    PagedFlux<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleAssignmentScope roleScope, Context context) {
        return new PagedFlux<>(
            () -> listRoleAssignmentsFirstPage(vaultUrl, roleScope, context),
            continuationToken -> listRoleAssignmentsNextPage(continuationToken, context));
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} in the first page that are applicable at
     * the given {@link KeyVaultRoleAssignmentScope roleScope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleAssignment role assignments}
     * in the given {@link KeyVaultRoleAssignmentScope roleScope} from the first page of results.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} is {@code null}.
     */
    Mono<PagedResponse<KeyVaultRoleAssignment>> listRoleAssignmentsFirstPage(String vaultUrl, KeyVaultRoleAssignmentScope roleScope, Context context) {
        Objects.requireNonNull(roleScope,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleScope'"));

        try {
            return clientImpl.getRoleAssignments()
                .listForScopeSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing role assignments for roleScope - {}", roleScope))
                .doOnSuccess(response -> logger.info("Listed role assignments for roleScope - {}", roleScope))
                .doOnError(error -> logger.warning(String.format("Failed to list role assignments for roleScope - %s",
                    roleScope), error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} given by the {@code nextPageLink} that was
     * retrieved from a call to
     * {@link KeyVaultAccessControlAsyncClient#listRoleAssignments(KeyVaultRoleAssignmentScope)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     * successful call to one of the {@code listKeyVaultRoleAssignments} operations.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleAssignment role assignments}
     * for the given {@link KeyVaultRoleAssignmentScope roleScope} from the first page of results.
     */
    Mono<PagedResponse<KeyVaultRoleAssignment>> listRoleAssignmentsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleAssignments()
                .listForScopeNextSinglePageAsync(continuationToken, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Listing next role assignments page - Page {}", continuationToken))
                .doOnSuccess(response -> logger.info("Listed next role assignments page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next role assignments page - Page {}",
                    continuationToken, error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated {@link UUID name}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> createRoleAssignment(KeyVaultRoleAssignmentScope roleScope, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignment(roleScope, UUID.randomUUID(), properties);
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param name The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope}, {@link UUID name} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> createRoleAssignment(KeyVaultRoleAssignmentScope roleScope, UUID name, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, name, properties).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated {@link UUID name}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, KeyVaultRoleAssignmentProperties properties) {
        return createRoleAssignmentWithResponse(roleScope, UUID.randomUUID(), properties);
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment} to
     * create.
     * @param name The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param properties Properties for the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope}, {@link UUID name} or
     * {@link KeyVaultRoleAssignmentProperties properties} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, UUID name, KeyVaultRoleAssignmentProperties properties) {
        return withContext(context -> createRoleAssignmentWithResponse(roleScope, name, properties, context));
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
    Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, UUID name, KeyVaultRoleAssignmentProperties properties, Context context) {
        Objects.requireNonNull(roleScope,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleScope'"));
        Objects.requireNonNull(name,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'name'"));
        Objects.requireNonNull(properties,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'properties'"));

        RoleAssignmentProperties roleAssignmentProperties =
            new RoleAssignmentProperties()
                .setRoleDefinitionId(properties.getRoleDefinitionId())
                .setPrincipalId(properties.getPrincipalId());
        RoleAssignmentCreateParameters parameters =
            new RoleAssignmentCreateParameters()
                .setProperties(roleAssignmentProperties);

        return clientImpl.getRoleAssignments()
            .createWithResponseAsync(vaultUrl, roleScope.toString(), name.toString(), parameters,
                context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
            .doOnRequest(ignored -> logger.info("Creating role assignment - {}", name))
            .doOnSuccess(response -> logger.info("Created role assignment - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to create role assignment - {}", name, error))
            .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name used of the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing the {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> getRoleAssignment(KeyVaultRoleAssignmentScope roleScope, String name) {
        return getRoleAssignmentWithResponse(roleScope, name).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name) {
        return withContext(context -> getRoleAssignmentWithResponse(roleScope, name, context));
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name, Context context) {
        Objects.requireNonNull(roleScope,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleScope'"));
        Objects.requireNonNull(name,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'name'"));

        try {
            return clientImpl.getRoleAssignments()
                .getWithResponseAsync(vaultUrl, roleScope.toString(), name, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Retrieving role assignment - {}", name))
                .doOnSuccess(response -> logger.info("Retrieved role assignment - {}", response.getValue().getName()))
                .doOnError(error -> logger.warning("Failed to retrieved role assignment - {}", name, error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing the {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> deleteRoleAssignment(KeyVaultRoleAssignmentScope roleScope, String name) {
        return deleteRoleAssignmentWithResponse(roleScope, name).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> deleteRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name) {
        return withContext(context -> deleteRoleAssignmentWithResponse(roleScope, name, context));
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleAssignmentScope roleScope} of the {@link KeyVaultRoleAssignment}.
     * @param name The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     * @throws NullPointerException if the {@link KeyVaultRoleAssignmentScope roleScope} or {@link String name} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> deleteRoleAssignmentWithResponse(KeyVaultRoleAssignmentScope roleScope, String name, Context context) {
        Objects.requireNonNull(roleScope,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'roleScope'"));
        Objects.requireNonNull(name,
            String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                "'name'"));

        try {
            return clientImpl.getRoleAssignments()
                .deleteWithResponseAsync(vaultUrl, roleScope.toString(), name, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.info("Deleting role assignment - {}", name))
                .doOnSuccess(response -> logger.info("Deleted role assignment - {}", response.getValue().getName()))
                .doOnError(error -> logger.warning("Failed to delete role assignment - {}", name, error))
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    @SuppressWarnings("BoundedWildcard")
    private static PagedResponse<KeyVaultRoleDefinition> transformRoleDefinitionsPagedResponse(PagedResponse<RoleDefinition> pagedResponse) {
        List<KeyVaultRoleDefinition> keyVaultRoleDefinitions = new ArrayList<>();

        for (RoleDefinition roleDefinition : pagedResponse.getValue()) {
            keyVaultRoleDefinitions.add(roleDefinitionToKeyVaultRoleDefinition(roleDefinition));
        }

        return new TransformedPagedResponse<>(keyVaultRoleDefinitions, pagedResponse);
    }

    private static Response<KeyVaultRoleAssignment> transformRoleAssignmentResponse(Response<RoleAssignment> response) {
        KeyVaultRoleAssignment keyVaultRoleAssignment = roleAssignmentToKeyVaultRoleAssignment(response.getValue());

        return new TransformedResponse<>(keyVaultRoleAssignment, response);
    }

    private static KeyVaultRoleDefinition roleDefinitionToKeyVaultRoleDefinition(RoleDefinition roleDefinition) {
        List<KeyVaultPermission> keyVaultPermissions = new ArrayList<>();

        for (Permission permission : roleDefinition.getPermissions()) {
            keyVaultPermissions.add(
                new KeyVaultPermission(permission.getActions(), permission.getDataActions(),
                    permission.getDataActions(), permission.getNotDataActions()));
        }

        return new KeyVaultRoleDefinition(roleDefinition.getId(), roleDefinition.getName(), roleDefinition.getType(),
            new KeyVaultRoleDefinitionProperties(roleDefinition.getRoleName(),
                roleDefinition.getDescription(), roleDefinition.getRoleType(), keyVaultPermissions,
                roleDefinition.getAssignableScopes()));
    }

    private static PagedResponse<KeyVaultRoleAssignment> transformRoleAssignmentsPagedResponse(PagedResponse<RoleAssignment> pagedResponse) {
        List<KeyVaultRoleAssignment> keyVaultRoleAssignments = new ArrayList<>();

        for (RoleAssignment roleAssignment : pagedResponse.getValue()) {
            keyVaultRoleAssignments.add(roleAssignmentToKeyVaultRoleAssignment(roleAssignment));
        }

        return new TransformedPagedResponse<>(keyVaultRoleAssignments, pagedResponse);
    }

    private static KeyVaultRoleAssignment roleAssignmentToKeyVaultRoleAssignment(RoleAssignment roleAssignment) {
        RoleAssignmentPropertiesWithScope propertiesWithScope = roleAssignment.getProperties();

        return new KeyVaultRoleAssignment(roleAssignment.getId(), roleAssignment.getName(), roleAssignment.getType(),
            new KeyVaultRoleAssignmentProperties(propertiesWithScope.getRoleDefinitionId(),
                propertiesWithScope.getPrincipalId()), KeyVaultRoleAssignmentScope.fromString(propertiesWithScope.getScope()));
    }

    private static final class TransformedPagedResponse<L extends List<T>, T, U> implements PagedResponse<T> {
        private final L output;
        private final PagedResponse<U> pagedResponse;

        TransformedPagedResponse(L output, PagedResponse<U> inputPagedResponse) {
            this.output = output;
            this.pagedResponse = inputPagedResponse;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public IterableStream<T> getElements() {
            return new IterableStream<>(output);
        }

        @Override
        public String getContinuationToken() {
            return pagedResponse.getContinuationToken();
        }

        @Override
        public int getStatusCode() {
            return pagedResponse.getStatusCode();
        }

        @Override
        public HttpHeaders getHeaders() {
            return pagedResponse.getHeaders();
        }

        @Override
        public HttpRequest getRequest() {
            return pagedResponse.getRequest();
        }

        @Override
        public List<T> getValue() {
            return output;
        }
    }

    private static final class TransformedResponse<T, U> implements Response<T> {
        private final T output;
        private final Response<U> response;

        TransformedResponse(T output, Response<U> response) {
            this.output = output;
            this.response = response;
        }

        @Override
        public int getStatusCode() {
            return response.getStatusCode();
        }

        @Override
        public HttpHeaders getHeaders() {
            return response.getHeaders();
        }

        @Override
        public HttpRequest getRequest() {
            return response.getRequest();
        }

        @Override
        public T getValue() {
            return output;
        }
    }
}
