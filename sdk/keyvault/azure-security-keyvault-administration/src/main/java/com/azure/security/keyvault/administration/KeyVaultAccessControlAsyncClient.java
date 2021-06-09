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
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.DataAction;
import com.azure.security.keyvault.administration.implementation.models.Permission;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignment;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentProperties;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentPropertiesWithScope;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinition;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionProperties;
import com.azure.security.keyvault.administration.implementation.models.RoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignmentProperties;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinitionType;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleType;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * The {@link KeyVaultAccessControlAsyncClient} provides asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting
 * {@link KeyVaultRoleDefinition role definitions} and {@link KeyVaultRoleAssignment role assignments}.
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
     * The Key Vault URL this client is associated to.
     */
    private final String vaultUrl;

    /**
     * The Key Vault Administration Service version to use with this client.
     */
    private final String serviceVersion;

    /**
     * The {@link HttpPipeline} powering this client.
     */
    private final HttpPipeline pipeline;

    /**
     * Package private constructor to be used by {@link KeyVaultAccessControlClientBuilder}.
     */
    KeyVaultAccessControlAsyncClient(URL vaultUrl, HttpPipeline httpPipeline,
                                     KeyVaultAdministrationServiceVersion serviceVersion) {
        Objects.requireNonNull(vaultUrl,
            KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED));

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();
        this.pipeline = httpPipeline;

        clientImpl = new KeyVaultAccessControlClientImplBuilder()
            .pipeline(httpPipeline)
            .apiVersion(this.serviceVersion)
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
     * Gets the {@link HttpPipeline} powering this client.
     *
     * @return The pipeline.
     */
    HttpPipeline getHttpPipeline() {
        return this.pipeline;
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definitions}.
     *
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope role scope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope) {
        return new PagedFlux<>(
            () -> withContext(context -> listRoleDefinitionsFirstPage(vaultUrl, roleScope, context)),
            continuationToken -> withContext(context -> listRoleDefinitionsNextPage(continuationToken, context)));
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definitions}.
     * @param context Additional {@link Context} that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleDefinition role definitions} for the given
     * {@link KeyVaultRoleScope role scope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    PagedFlux<KeyVaultRoleDefinition> listRoleDefinitions(KeyVaultRoleScope roleScope, Context context) {
        return new PagedFlux<>(
            () -> listRoleDefinitionsFirstPage(vaultUrl, roleScope, context),
            continuationToken -> listRoleDefinitionsNextPage(continuationToken, context));
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} in the first page that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleDefinition role definitions}
     * for the given {@link KeyVaultRoleScope role scope} from the first page of results.
     *
     * @throws KeyVaultAdministrationException If the given {@code vaultUrl} or {@code roleScope} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    Mono<PagedResponse<KeyVaultRoleDefinition>> listRoleDefinitionsFirstPage(String vaultUrl,
                                                                             KeyVaultRoleScope roleScope,
                                                                             Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));

            return clientImpl.getRoleDefinitions()
                .listSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing role definitions for roleScope - {}", roleScope))
                .doOnSuccess(response -> logger.verbose("Listed role definitions for roleScope - {}", roleScope))
                .doOnError(error ->
                    logger.warning(String.format("Failed to list role definitions for roleScope - %s", roleScope),
                        error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleDefinition role definitions} given by the {@code nextPageLink} that was retrieved
     * from a call to
     * {@link KeyVaultAccessControlAsyncClient#listRoleDefinitionsFirstPage(String, KeyVaultRoleScope, Context)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     * successful call to one of the {@code listKeyVaultRoleDefinitions} operations.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleDefinition role definitions}
     * for the given {@link KeyVaultRoleScope role scope} from the next page of results.
     *
     * @throws KeyVaultAdministrationException If the given {@code continuationToken} is invalid.
     */
    Mono<PagedResponse<KeyVaultRoleDefinition>> listRoleDefinitionsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleDefinitions()
                .listNextSinglePageAsync(continuationToken, vaultUrl, context.addData(AZ_TRACING_NAMESPACE_KEY,
                    KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Listing next role definitions page - Page {}", continuationToken))
                .doOnSuccess(response ->
                    logger.verbose("Listed next role definitions page - Page {}", continuationToken))
                .doOnError(error ->
                    logger.warning("Failed to list next role definitions page - Page {}", continuationToken, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition} with a randomly generated {@link String name}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM
     * only supports '/'.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> setRoleDefinition(KeyVaultRoleScope roleScope) {
        return setRoleDefinition(roleScope, UUID.randomUUID().toString());
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM only
     * supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}. It can be any valid UUID.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> setRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return setRoleDefinitionWithResponse(new SetRoleDefinitionOptions(roleScope, roleDefinitionName))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition}.
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * created or updated {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If any parameter in {@code options} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * in the {@link SetRoleDefinitionOptions options} object are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleDefinition>> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options) {
        return withContext(context -> setRoleDefinitionWithResponse(options, context));
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition}.
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * created or updated {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If any parameter in {@code options} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * in the {@link SetRoleDefinitionOptions options} object are {@code null}.
     */
    Mono<Response<KeyVaultRoleDefinition>> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options,
                                                                         Context context) {
        try {
            Objects.requireNonNull(options,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'options'"));
            Objects.requireNonNull(options.getRoleScope(),
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'options.getRoleScope()'"));
            Objects.requireNonNull(options.getRoleDefinitionName(),
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'options.getRoleDefinitionName()'"));

            List<RoleScope> assignableScopes = null;

            if (options.getAssignableScopes() != null) {
                assignableScopes = options.getAssignableScopes().stream()
                    .map(keyVaultRoleScope -> RoleScope.fromString(keyVaultRoleScope.toString()))
                    .collect(Collectors.toList());
            }

            List<Permission> permissions = null;

            if (options.getPermissions() != null) {
                permissions = options.getPermissions().stream()
                    .map(keyVaultPermission -> new Permission()
                        .setActions(keyVaultPermission.getActions())
                        .setNotActions(keyVaultPermission.getNotActions())
                        .setDataActions(keyVaultPermission.getDataActions().stream()
                            .map(allowedDataAction -> DataAction.fromString(allowedDataAction.toString()))
                            .collect(Collectors.toList()))
                        .setNotDataActions(keyVaultPermission.getNotDataActions().stream()
                            .map(notDataAction -> DataAction.fromString(notDataAction.toString()))
                            .collect(Collectors.toList())))
                    .collect(Collectors.toList());
            }

            RoleDefinitionProperties roleDefinitionProperties =
                new RoleDefinitionProperties()
                    .setRoleName(options.getRoleDefinitionName())
                    .setAssignableScopes(assignableScopes)
                    .setDescription(options.getDescription())
                    .setPermissions(permissions);
            RoleDefinitionCreateParameters parameters =
                new RoleDefinitionCreateParameters()
                    .setProperties(roleDefinitionProperties);

            return clientImpl.getRoleDefinitions()
                .createOrUpdateWithResponseAsync(vaultUrl, options.getRoleScope().toString(),
                    options.getRoleDefinitionName(), parameters,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Creating role assignment - {}", options.getRoleDefinitionName()))
                .doOnSuccess(response -> logger.verbose("Created role assignment - {}", response.getValue().getName()))
                .doOnError(error ->
                    logger.warning("Failed to create role assignment - {}", options.getRoleDefinitionName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name used of the {@link KeyVaultRoleDefinition}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> getRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return getRoleDefinitionWithResponse(roleScope, roleDefinitionName).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleDefinition>> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                                String roleDefinitionName) {
        return withContext(context -> getRoleDefinitionWithResponse(roleScope, roleDefinitionName, context));
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleDefinition>> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                         String roleDefinitionName, Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));
            Objects.requireNonNull(roleDefinitionName,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleDefinitionName'"));

            return clientImpl.getRoleDefinitions()
                .getWithResponseAsync(vaultUrl, roleScope.toString(), roleDefinitionName,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Retrieving role assignment - {}", roleDefinitionName))
                .doOnSuccess(response ->
                    logger.verbose("Retrieved role assignment - {}", response.getValue().getName()))
                .doOnError(error ->
                    logger.warning("Failed to retrieved role assignment - {}", roleDefinitionName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}. Managed HSM
     * only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     *
     * @return A {@link Mono} containing the deleted {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> deleteRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the deleted
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleDefinition>> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                                   String roleDefinitionName) {
        return withContext(context -> deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName, context));
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the deleted
     * {@link KeyVaultRoleDefinition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleDefinition>> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                            String roleDefinitionName,
                                                                            Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));
            Objects.requireNonNull(roleDefinitionName,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleDefinitionName'"));

            return clientImpl.getRoleDefinitions()
                .deleteWithResponseAsync(vaultUrl, roleScope.toString(), roleDefinitionName,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Deleting role assignment - {}", roleDefinitionName))
                .doOnSuccess(response -> logger.verbose("Deleted role assignment - {}", response.getValue().getName()))
                .doOnError(error -> logger.warning("Failed to delete role assignment - {}", roleDefinitionName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleDefinitionResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope role scope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope) {
        return new PagedFlux<>(
            () -> withContext(context -> listRoleAssignmentsFirstPage(vaultUrl, roleScope, context)),
            continuationToken -> withContext(context -> listRoleAssignmentsNextPage(continuationToken, context)));
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link PagedFlux} containing the {@link KeyVaultRoleAssignment role assignments} for the given
     * {@link KeyVaultRoleScope role scope}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    PagedFlux<KeyVaultRoleAssignment> listRoleAssignments(KeyVaultRoleScope roleScope, Context context) {
        return new PagedFlux<>(
            () -> listRoleAssignmentsFirstPage(vaultUrl, roleScope, context),
            continuationToken -> listRoleAssignmentsNextPage(continuationToken, context));
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} in the first page that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param vaultUrl The URL for the Key Vault this client is associated with.
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleAssignment role assignments}
     * in the given {@link KeyVaultRoleScope role scope} from the first page of results.
     *
     * @throws KeyVaultAdministrationException If the given {@code vaultUrl} or {@code roleScope} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    Mono<PagedResponse<KeyVaultRoleAssignment>> listRoleAssignmentsFirstPage(String vaultUrl,
                                                                             KeyVaultRoleScope roleScope,
                                                                             Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));

            return clientImpl.getRoleAssignments()
                .listForScopeSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Listing role assignments for roleScope - {}", roleScope))
                .doOnSuccess(response -> logger.verbose("Listed role assignments for roleScope - {}", roleScope))
                .doOnError(error ->
                    logger.warning(String.format("Failed to list role assignments for roleScope - %s", roleScope),
                        error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} given by the {@code nextPageLink} that was
     * retrieved from a call to {@link KeyVaultAccessControlAsyncClient#listRoleAssignments(KeyVaultRoleScope)}.
     *
     * @param continuationToken The {@link PagedResponse#getContinuationToken() continuationToken} from a previous,
     * successful call to one of the {@code listKeyVaultRoleAssignments} operations.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link PagedResponse} of {@link KeyVaultRoleAssignment role assignments}
     * for the given {@link KeyVaultRoleScope role scope} from the first page of results.
     *
     * @throws KeyVaultAdministrationException If the given {@code continuationToken} is invalid.
     */
    Mono<PagedResponse<KeyVaultRoleAssignment>> listRoleAssignmentsNextPage(String continuationToken, Context context) {
        try {
            return clientImpl.getRoleAssignments()
                .listForScopeNextSinglePageAsync(continuationToken, vaultUrl,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored ->
                    logger.verbose("Listing next role assignments page - Page {}", continuationToken))
                .doOnSuccess(response ->
                    logger.verbose("Listed next role assignments page - Page {}", continuationToken))
                .doOnError(error -> logger.warning("Failed to list next role assignments page - Page {}",
                    continuationToken, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment} with a randomly generated name.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId}
     * are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
                                                             String principalId) {
        return createRoleAssignment(roleScope, roleDefinitionId, principalId, UUID.randomUUID().toString());
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name already
     * exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
                                                             String principalId, String roleAssignmentName) {
        return createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, roleAssignmentName)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name already
     * exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                                   String roleDefinitionId,
                                                                                   String principalId,
                                                                                   String roleAssignmentName) {
        return withContext(context ->
            createRoleAssignmentWithResponse(roleScope, roleDefinitionId, principalId, roleAssignmentName, context));
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment} to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment}. It can be any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name already
     * exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                            String roleDefinitionId, String principalId,
                                                                            String roleAssignmentName,
                                                                            Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));
            Objects.requireNonNull(roleAssignmentName,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleAssignmentName'"));
            Objects.requireNonNull(principalId,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'principalId'"));
            Objects.requireNonNull(roleDefinitionId,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleDefinitionId'"));

            RoleAssignmentProperties roleAssignmentProperties =
                new RoleAssignmentProperties()
                    .setRoleDefinitionId(roleDefinitionId)
                    .setPrincipalId(principalId);
            RoleAssignmentCreateParameters parameters =
                new RoleAssignmentCreateParameters()
                    .setProperties(roleAssignmentProperties);

            return clientImpl.getRoleAssignments()
                .createWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName, parameters,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Creating role assignment - {}", roleAssignmentName))
                .doOnSuccess(response -> logger.verbose("Created role assignment - {}", response.getValue().getName()))
                .doOnError(error -> logger.warning("Failed to create role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name used of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> getRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return getRoleAssignmentWithResponse(roleScope, roleAssignmentName).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                                String roleAssignmentName) {
        return withContext(context -> getRoleAssignmentWithResponse(roleScope, roleAssignmentName, context));
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                         String roleAssignmentName, Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));
            Objects.requireNonNull(roleAssignmentName,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleAssignmentName'"));

            return clientImpl.getRoleAssignments()
                .getWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Retrieving role assignment - {}", roleAssignmentName))
                .doOnSuccess(response ->
                    logger.verbose("Retrieved role assignment - {}", response.getValue().getName()))
                .doOnError(error ->
                    logger.warning("Failed to retrieved role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> deleteRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                                   String roleAssignmentName) {
        return withContext(context -> deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, context));
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name cannot
     * be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                            String roleAssignmentName,
                                                                            Context context) {
        try {
            Objects.requireNonNull(roleScope,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleScope'"));
            Objects.requireNonNull(roleAssignmentName,
                String.format(KeyVaultErrorCodeStrings.getErrorString(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED),
                    "'roleAssignmentName'"));

            return clientImpl.getRoleAssignments()
                .deleteWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName,
                    context.addData(AZ_TRACING_NAMESPACE_KEY, KEYVAULT_TRACING_NAMESPACE_VALUE))
                .doOnRequest(ignored -> logger.verbose("Deleting role assignment - {}", roleAssignmentName))
                .doOnSuccess(response -> logger.verbose("Deleted role assignment - {}", response.getValue().getName()))
                .doOnError(error -> logger.warning("Failed to delete role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAccessControlAsyncClient::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    @SuppressWarnings("BoundedWildcard")
    private static PagedResponse<KeyVaultRoleDefinition> transformRoleDefinitionsPagedResponse(
        PagedResponse<RoleDefinition> pagedResponse) {

        List<KeyVaultRoleDefinition> keyVaultRoleDefinitions = new ArrayList<>();

        for (RoleDefinition roleDefinition : pagedResponse.getValue()) {
            keyVaultRoleDefinitions.add(roleDefinitionToKeyVaultRoleDefinition(roleDefinition));
        }

        return new TransformedPagedResponse<>(keyVaultRoleDefinitions, pagedResponse);
    }

    private static Response<KeyVaultRoleDefinition> transformRoleDefinitionResponse(Response<RoleDefinition> response) {
        KeyVaultRoleDefinition keyVaultRoleDefinition = roleDefinitionToKeyVaultRoleDefinition(response.getValue());

        return new TransformedResponse<>(keyVaultRoleDefinition, response);
    }

    private static KeyVaultRoleDefinition roleDefinitionToKeyVaultRoleDefinition(RoleDefinition roleDefinition) {
        List<KeyVaultPermission> keyVaultPermissions = new ArrayList<>();

        for (Permission permission : roleDefinition.getPermissions()) {
            keyVaultPermissions.add(
                new KeyVaultPermission(permission.getActions(), permission.getNotActions(),
                    permission.getDataActions().stream()
                        .map(dataAction -> KeyVaultDataAction.fromString(dataAction.toString()))
                        .collect(Collectors.toList()),
                    permission.getNotDataActions().stream()
                        .map(notDataAction -> KeyVaultDataAction.fromString(notDataAction.toString()))
                        .collect(Collectors.toList())));
        }

        return new KeyVaultRoleDefinition(roleDefinition.getId(), roleDefinition.getName(),
            KeyVaultRoleDefinitionType.fromString(roleDefinition.getType().toString()), roleDefinition.getRoleName(),
            roleDefinition.getDescription(), KeyVaultRoleType.fromString(roleDefinition.getRoleType().toString()),
            keyVaultPermissions, roleDefinition.getAssignableScopes().stream()
                .map(roleScope -> KeyVaultRoleScope.fromString(roleScope.toString()))
                .collect(Collectors.toList()));
    }

    private static PagedResponse<KeyVaultRoleAssignment> transformRoleAssignmentsPagedResponse(
        PagedResponse<RoleAssignment> pagedResponse) {

        List<KeyVaultRoleAssignment> keyVaultRoleAssignments = new ArrayList<>();

        for (RoleAssignment roleAssignment : pagedResponse.getValue()) {
            keyVaultRoleAssignments.add(roleAssignmentToKeyVaultRoleAssignment(roleAssignment));
        }

        return new TransformedPagedResponse<>(keyVaultRoleAssignments, pagedResponse);
    }

    private static Response<KeyVaultRoleAssignment> transformRoleAssignmentResponse(Response<RoleAssignment> response) {
        KeyVaultRoleAssignment keyVaultRoleAssignment = roleAssignmentToKeyVaultRoleAssignment(response.getValue());

        return new TransformedResponse<>(keyVaultRoleAssignment, response);
    }

    private static KeyVaultRoleAssignment roleAssignmentToKeyVaultRoleAssignment(RoleAssignment roleAssignment) {
        RoleAssignmentPropertiesWithScope propertiesWithScope = roleAssignment.getProperties();

        return new KeyVaultRoleAssignment(roleAssignment.getId(), roleAssignment.getName(), roleAssignment.getType(),
            new KeyVaultRoleAssignmentProperties(propertiesWithScope.getRoleDefinitionId(),
                propertiesWithScope.getPrincipalId(),
                KeyVaultRoleScope.fromString(propertiesWithScope.getScope().toString())));
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
