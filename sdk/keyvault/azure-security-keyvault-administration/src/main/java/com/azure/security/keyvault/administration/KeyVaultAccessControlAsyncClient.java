// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.administration.implementation.KeyVaultAccessControlClientImpl;
import com.azure.security.keyvault.administration.implementation.KeyVaultAdministrationUtils;
import com.azure.security.keyvault.administration.implementation.KeyVaultErrorCodeStrings;
import com.azure.security.keyvault.administration.implementation.models.RoleAssignmentCreateParameters;
import com.azure.security.keyvault.administration.implementation.models.RoleDefinitionCreateParameters;
import com.azure.security.keyvault.administration.models.KeyVaultAdministrationException;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.swallowExceptionForStatusCodeAsync;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleAssignmentCreateParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateAndGetRoleDefinitionCreateParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleAssignmentParameters;
import static com.azure.security.keyvault.administration.KeyVaultAdministrationUtil.validateRoleDefinitionParameters;

/**
 * The {@link KeyVaultAccessControlAsyncClient} provides asynchronous methods to view and manage Role Based Access
 * for the Azure Key Vault. The client supports creating, listing, updating, and deleting
 * {@link KeyVaultRoleDefinition role definitions} and {@link KeyVaultRoleAssignment role assignments}.
 *
 * <p>Instances of this client are obtained by calling the {@link KeyVaultAccessControlClientBuilder#buildAsyncClient()}
 * method on a {@link KeyVaultAccessControlClientBuilder} object.</p>
 *
 * <p><strong>Samples to construct an async client</strong></p>
 * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.instantiation -->
 * <pre>
 * KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.instantiation -->
 *
 * @see KeyVaultAccessControlClientBuilder
 */
@ServiceClient(builder = KeyVaultAccessControlClientBuilder.class, isAsync = true)
public final class KeyVaultAccessControlAsyncClient {
    /**
     * The logger to be used.
     */
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultAccessControlAsyncClient.class);

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
        Objects.requireNonNull(vaultUrl, KeyVaultErrorCodeStrings.VAULT_END_POINT_REQUIRED);

        this.vaultUrl = vaultUrl.toString();
        this.serviceVersion = serviceVersion.getVersion();
        this.pipeline = httpPipeline;

        clientImpl = new KeyVaultAccessControlClientImpl(httpPipeline, this.serviceVersion);
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
     * <p><strong>Code Samples</strong></p>
     * <p>Lists all {@link KeyVaultRoleDefinition role definitions}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition role definitions}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleDefinitions#KeyVaultRoleScope -->
     * <pre>
     * keyVaultAccessControlAsyncClient.listRoleDefinitions&#40;KeyVaultRoleScope.GLOBAL&#41;
     *     .subscribe&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Retrieved role definition with name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleDefinitions#KeyVaultRoleScope -->
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
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
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
                String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'roleScope'"));

            return clientImpl.getRoleDefinitions()
                .listSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing role definitions for roleScope - {}", roleScope))
                .doOnSuccess(response -> LOGGER.verbose("Listed role definitions for roleScope - {}", roleScope))
                .doOnError(error -> LOGGER.warning("Failed to list role definitions for roleScope - {}", roleScope,
                    error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
                .listNextSinglePageAsync(continuationToken, vaultUrl, context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Listing next role definitions page - Page {}", continuationToken))
                .doOnSuccess(response ->
                    LOGGER.verbose("Listed next role definitions page - Page {}", continuationToken))
                .doOnError(error ->
                    LOGGER.warning("Failed to list next role definitions page - Page {}", continuationToken, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleDefinitionsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition role definition} with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleDefinition role definition} with a randomly generated name. Prints out the
     * details of the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope -->
     * <pre>
     * keyVaultAccessControlAsyncClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;
     *     .subscribe&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
     *             roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} is {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> setRoleDefinition(KeyVaultRoleScope roleScope) {
        return setRoleDefinition(roleScope, UUID.randomUUID().toString());
    }

    /**
     * Creates or updates a {@link KeyVaultRoleDefinition role definition}. If no name is provided, then a
     * {@link KeyVaultRoleDefinition role definition} will be created with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates or updates a {@link KeyVaultRoleDefinition role definition} with a given generated name. Prints out
     * the details of the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleDefinitionName = &quot;504a3d11-5a63-41a9-b603-41bdf88df03e&quot;;
     *
     * keyVaultAccessControlAsyncClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName&#41;
     *     .subscribe&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Set role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
     *             roleDefinition.getRoleName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}. It can be any valid\
     * UUID. If {@code null} is provided, a name will be randomly generated.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleDefinition role definition}.
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
     * Creates or updates a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates or updates a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response} and the created {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions -->
     * <pre>
     * String roleDefinitionName = &quot;9de303d3-6ea8-4b8f-a20b-18e67f77e42a&quot;;
     *
     * List&lt;KeyVaultRoleScope&gt; assignableScopes = new ArrayList&lt;&gt;&#40;&#41;;
     * assignableScopes.add&#40;KeyVaultRoleScope.GLOBAL&#41;;
     * assignableScopes.add&#40;KeyVaultRoleScope.KEYS&#41;;
     *
     * List&lt;KeyVaultDataAction&gt; dataActions = new ArrayList&lt;&gt;&#40;&#41;;
     * dataActions.add&#40;KeyVaultDataAction.START_HSM_RESTORE&#41;;
     * dataActions.add&#40;KeyVaultDataAction.START_HSM_BACKUP&#41;;
     * dataActions.add&#40;KeyVaultDataAction.READ_HSM_BACKUP_STATUS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.READ_HSM_RESTORE_STATUS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.BACKUP_HSM_KEYS&#41;;
     * dataActions.add&#40;KeyVaultDataAction.RESTORE_HSM_KEYS&#41;;
     *
     * List&lt;KeyVaultPermission&gt; permissions = new ArrayList&lt;&gt;&#40;&#41;;
     * permissions.add&#40;new KeyVaultPermission&#40;null, null, dataActions, null&#41;&#41;;
     *
     * SetRoleDefinitionOptions setRoleDefinitionOptions =
     *     new SetRoleDefinitionOptions&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;
     *         .setRoleName&#40;&quot;Backup and Restore Role Definition&quot;&#41;
     *         .setDescription&#40;&quot;Can backup and restore a whole Managed HSM, as well as individual keys.%n&quot;&#41;
     *         .setAssignableScopes&#40;assignableScopes&#41;
     *         .setPermissions&#40;permissions&#41;;
     *
     * keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse&#40;setRoleDefinitionOptions&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role&quot;
     *             + &quot; name '%s' was set.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;,
     *             response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions -->
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * created or updated {@link KeyVaultRoleDefinition role definition}.
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
     * Creates or updates a {@link KeyVaultRoleDefinition role definition}.
     *
     * @param options Object representing the configurable options to create or update a
     * {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * created or updated {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If any parameter in {@code options} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName}
     * in the {@link SetRoleDefinitionOptions options} object are {@code null}.
     */
    Mono<Response<KeyVaultRoleDefinition>> setRoleDefinitionWithResponse(SetRoleDefinitionOptions options,
                                                                         Context context) {
        try {
            RoleDefinitionCreateParameters parameters = validateAndGetRoleDefinitionCreateParameters(options);

            return clientImpl.getRoleDefinitions()
                .createOrUpdateWithResponseAsync(vaultUrl, options.getRoleScope().toString(),
                    options.getRoleDefinitionName(), parameters,
                    context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Creating role definition - {}", options.getRoleDefinitionName()))
                .doOnSuccess(response -> LOGGER.verbose("Created role definition - {}", response.getValue().getName()))
                .doOnError(error ->
                    LOGGER.warning("Failed to create role definition - {}", options.getRoleDefinitionName(), error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleDefinitionResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the retrieved
     * {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;8f90b099-7361-4db6-8321-719adaf6e4ca&quot;;
     *
     * keyVaultAccessControlAsyncClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;
     *     .subscribe&#40;roleDefinition -&gt;
     *         System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;,
     *             roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name used of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleDefinition> getRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return getRoleDefinitionWithResponse(roleScope, roleDefinitionName).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleDefinitionName = &quot;0877b4ee-6275-4559-89f1-c289060ef398&quot;;
     *
     * keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionName&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' and role&quot;
     *             + &quot; name '%s' was retrieved.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;,
     *             response.getValue&#40;&#41;.getRoleName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleDefinition>> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                                String roleDefinitionName) {
        return withContext(context -> getRoleDefinitionWithResponse(roleScope, roleDefinitionName, context));
    }

    /**
     * Gets a {@link KeyVaultRoleDefinition role definition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleDefinition role definition}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleDefinition role definition} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleDefinition>> getRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                         String roleDefinitionName, Context context) {
        try {
            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            return clientImpl.getRoleDefinitions()
                .getWithResponseAsync(vaultUrl, roleScope.toString(), roleDefinitionName,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Retrieving role definition - {}", roleDefinitionName))
                .doOnSuccess(response ->
                    LOGGER.verbose("Retrieved role definition - {}", response.getValue().getName()))
                .doOnError(error ->
                    LOGGER.warning("Failed to retrieved role definition - {}", roleDefinitionName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleDefinitionResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleDefinition role definition}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     * <pre>
     * String roleDefinitionName = &quot;e3c7c51a-8abd-4b1b-9201-48ded34d0358&quot;;
     *
     * keyVaultAccessControlAsyncClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;
     *     .subscribe&#40;unused -&gt; System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinition#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * Managed HSM only supports '/'.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} of a {@link Void}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRoleDefinition(KeyVaultRoleScope roleScope, String roleDefinitionName) {
        return deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition role definition}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleDefinition role definition}. Prints out the details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleDefinitionName = &quot;ccaafb00-31fb-40fe-9ccc-39a2ad2af082&quot;;
     *
     * keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse&#40;KeyVaultRoleScope.GLOBAL,
     *     myRoleDefinitionName&#41;.subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role definition with name '%s' was&quot;
     *             + &quot; deleted.%n&quot;, response.getStatusCode&#40;&#41;, myRoleDefinitionName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     *
     * @return A {@link Mono} containing a {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                 String roleDefinitionName) {
        return withContext(context -> deleteRoleDefinitionWithResponse(roleScope, roleDefinitionName, context));
    }

    /**
     * Deletes a {@link KeyVaultRoleDefinition role definition}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleDefinition role definition}.
     * @param roleDefinitionName The name of the {@link KeyVaultRoleDefinition role definition}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleDefinitionName} are
     * {@code null}.
     */
    Mono<Response<Void>> deleteRoleDefinitionWithResponse(KeyVaultRoleScope roleScope,
                                                                            String roleDefinitionName,
                                                                            Context context) {
        try {
            validateRoleDefinitionParameters(roleScope, roleDefinitionName);

            return clientImpl.getRoleDefinitions()
                .deleteWithResponseAsync(vaultUrl, roleScope.toString(), roleDefinitionName,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Deleting role definition - {}", roleDefinitionName))
                .doOnSuccess(response -> LOGGER.verbose("Deleted role definition - {}", response.getValue().getName()))
                .doOnError(error -> LOGGER.warning("Failed to delete role definition - {}", roleDefinitionName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(KeyVaultAdministrationException.class, e ->
                    swallowExceptionForStatusCodeAsync(404, e, LOGGER));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Lists all {@link KeyVaultRoleAssignment role assignments} that are applicable at the given
     * {@link KeyVaultRoleScope role scope} and above.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
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
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
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
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
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
                String.format(KeyVaultErrorCodeStrings.PARAMETER_REQUIRED, "'roleScope'"));

            return clientImpl.getRoleAssignments()
                .listForScopeSinglePageAsync(vaultUrl, roleScope.toString(), null,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Listing role assignments for roleScope - {}", roleScope))
                .doOnSuccess(response -> LOGGER.verbose("Listed role assignments for roleScope - {}", roleScope))
                .doOnError(error -> LOGGER.warning("Failed to list role assignments for roleScope - {}", roleScope,
                    error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
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
                    context)
                .doOnRequest(ignored ->
                    LOGGER.verbose("Listing next role assignments page - Page {}", continuationToken))
                .doOnSuccess(response ->
                    LOGGER.verbose("Listed next role assignments page - Page {}", continuationToken))
                .doOnError(error -> LOGGER.warning("Failed to list next role assignments page - Page {}",
                    continuationToken, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleAssignmentsPagedResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment role assignment} with a randomly generated name.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment} with a randomly generated name. Prints out the
     * details of the created {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     * <pre>
     * String roleDefinitionId = &quot;142e42c1-ab29-4dc7-9dfa-8fd7c0815128&quot;;
     * String servicePrincipalId = &quot;07dca82e-b625-4a60-977b-859d2a162ca7&quot;;
     *
     * keyVaultAccessControlAsyncClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionId,
     *     servicePrincipalId&#41;.subscribe&#40;roleAssignment -&gt;
     *         System.out.printf&#40;&quot;Created role assignment with randomly generated name '%s' for principal with id&quot;
     *             + &quot;'%s'.%n&quot;, roleAssignment.getName&#40;&#41;, roleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope}, {@code roleDefinitionId} or
     * {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> createRoleAssignment(KeyVaultRoleScope roleScope, String roleDefinitionId,
                                                             String principalId) {
        return createRoleAssignment(roleScope, roleDefinitionId, principalId, UUID.randomUUID().toString());
    }

    /**
     * Creates a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment}. Prints out the details of the created
     * {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
     * <pre>
     * String myRoleDefinitionId = &quot;e1ca67d0-4332-465c-b9cd-894b2834401b&quot;;
     * String myServicePrincipalId = &quot;31af81fe-6123-4838-92c0-7c2531ec13d7&quot;;
     * String myRoleAssignmentName = &quot;94d7827f-f8c9-4a5d-94fd-9fd2cd02d12f&quot;;
     *
     * keyVaultAccessControlAsyncClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, myRoleDefinitionId,
     *     myServicePrincipalId, myRoleAssignmentName&#41;.subscribe&#40;roleAssignment -&gt;
     *         System.out.printf&#40;&quot;Created role assignment with name '%s' for principal with id '%s'.%n&quot;,
     *             roleAssignment.getName&#40;&#41;, roleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment role assignment}. It can be
     * any valid UUID.
     *
     * @return A {@link Mono} containing the created {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * already or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
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
     * Creates a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Creates a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response} and the created {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String -->
     * <pre>
     * String someRoleDefinitionId = &quot;686b0f78-5012-4def-8a70-eba36aa54d3d&quot;;
     * String someServicePrincipalId = &quot;345ec980-904b-4238-aafc-1eaeed3e23cf&quot;;
     * String someRoleAssignmentName = &quot;1c79927c-6e08-4e5c-8a6c-f58c13c9bbb5&quot;;
     *
     * keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL,
     *     someRoleDefinitionId, someServicePrincipalId, someRoleAssignmentName&#41;.subscribe&#40;response -&gt; &#123;
     *         KeyVaultRoleAssignment createdRoleAssignment = response.getValue&#40;&#41;;
     *
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' for&quot;
     *             + &quot; principal with id '%s' was created.%n&quot;, response.getStatusCode&#40;&#41;,
     *             createdRoleAssignment.getName&#40;&#41;, createdRoleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment role assignment}. It can be
     * any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * already exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
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
     * Creates a {@link KeyVaultRoleAssignment role assignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}
     * to create.
     * @param roleAssignmentName The name used to create the {@link KeyVaultRoleAssignment role assignment}. It can be
     * any valid UUID.
     * @param roleDefinitionId The {@link KeyVaultRoleDefinition role definition} ID for the role assignment.
     * @param principalId The principal ID assigned to the role. This maps to the ID inside the Active Directory.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the created
     * {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * already exists or if the given {@code roleScope}, {@code roleDefinitionId} or {@code principalId} are invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope}, {@link String roleAssignmentName},
     * {@link String roleDefinitionId} or {@link String principalId} are {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> createRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                            String roleDefinitionId, String principalId,
                                                                            String roleAssignmentName,
                                                                            Context context) {
        try {
            RoleAssignmentCreateParameters parameters = validateAndGetRoleAssignmentCreateParameters(roleScope, roleDefinitionId, principalId, roleAssignmentName);

            return clientImpl.getRoleAssignments()
                .createWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName, parameters,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Creating role assignment - {}", roleAssignmentName))
                .doOnSuccess(response -> LOGGER.verbose("Created role assignment - {}", response.getValue().getName()))
                .doOnError(error -> LOGGER.warning("Failed to create role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the retrieved
     * {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;c5a305c0-e17a-40f5-af79-73801bdd8867&quot;;
     *
     * keyVaultAccessControlAsyncClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;
     *     .subscribe&#40;roleAssignment -&gt;
     *         System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name used of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return A {@link Mono} containing the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<KeyVaultRoleAssignment> getRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return getRoleAssignmentWithResponse(roleScope, roleAssignmentName).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Gets a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response} and the retrieved {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleAssignmentName = &quot;76ccbf52-4d49-4fcc-ad3f-044c254be114&quot;;
     *
     * keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL, myRoleAssignmentName&#41;
     *     .subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was&quot;
     *             + &quot; retrieved.%n&quot;, response.getStatusCode&#40;&#41;, response.getValue&#40;&#41;.getName&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                                String roleAssignmentName) {
        return withContext(context -> getRoleAssignmentWithResponse(roleScope, roleAssignmentName, context));
    }

    /**
     * Gets a {@link KeyVaultRoleAssignment role assignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} whose {@link Response#getValue() value} contains the
     * {@link KeyVaultRoleAssignment role assignment}.
     *
     * @throws KeyVaultAdministrationException If a {@link KeyVaultRoleAssignment role assignment} with the given name
     * cannot be found or if the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    Mono<Response<KeyVaultRoleAssignment>> getRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                         String roleAssignmentName, Context context) {
        try {
            validateRoleAssignmentParameters(roleScope, roleAssignmentName);
            return clientImpl.getRoleAssignments()
                .getWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Retrieving role assignment - {}", roleAssignmentName))
                .doOnSuccess(response ->
                    LOGGER.verbose("Retrieved role assignment - {}", response.getValue().getName()))
                .doOnError(error ->
                    LOGGER.warning("Failed to retrieve role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(KeyVaultAdministrationUtil::transformRoleAssignmentResponse);
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     * <pre>
     * String roleAssignmentName = &quot;f05d11ce-578a-4524-950c-fb4c53e5fb96&quot;;
     *
     * keyVaultAccessControlAsyncClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;
     *     .subscribe&#40;unused -&gt;
     *         System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignment#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return A {@link Mono} of a {@link Void}.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRoleAssignment(KeyVaultRoleScope roleScope, String roleAssignmentName) {
        return deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment role assignment}.
     *
     * <p><strong>Code Samples</strong></p>
     * <p>Deletes a {@link KeyVaultRoleAssignment role assignment}. Prints out details of the
     * {@link Response HTTP response}.</p>
     * <!-- src_embed com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String -->
     * <pre>
     * String myRoleAssignmentName = &quot;06aaea13-e4f3-4d3f-8a93-088dff6e90ed&quot;;
     *
     * keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse&#40;KeyVaultRoleScope.GLOBAL,
     *     myRoleAssignmentName&#41;.subscribe&#40;response -&gt;
     *         System.out.printf&#40;&quot;Response successful with status code: %d. Role assignment with name '%s' was&quot;
     *             + &quot; deleted.%n&quot;, response.getStatusCode&#40;&#41;, myRoleAssignmentName&#41;&#41;;
     * </pre>
     * <!-- end com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String -->
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     *
     * @return A {@link Mono} containing a {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope,
                                                                 String roleAssignmentName) {
        return withContext(context -> deleteRoleAssignmentWithResponse(roleScope, roleAssignmentName, context));
    }

    /**
     * Deletes a {@link KeyVaultRoleAssignment role assignment}.
     *
     * @param roleScope The {@link KeyVaultRoleScope role scope} of the {@link KeyVaultRoleAssignment role assignment}.
     * @param roleAssignmentName The name of the {@link KeyVaultRoleAssignment role assignment}.
     * @param context Additional context that is passed through the HTTP pipeline during the service call.
     *
     * @return A {@link Mono} containing a {@link Response} with a {@link Void} value.
     *
     * @throws KeyVaultAdministrationException If the given {@code roleScope} is invalid.
     * @throws NullPointerException If the {@link KeyVaultRoleScope role scope} or {@link String roleAssignmentName} are
     * {@code null}.
     */
    Mono<Response<Void>> deleteRoleAssignmentWithResponse(KeyVaultRoleScope roleScope, String roleAssignmentName,
                                                          Context context) {
        try {
            validateRoleAssignmentParameters(roleScope, roleAssignmentName);

            return clientImpl.getRoleAssignments()
                .deleteWithResponseAsync(vaultUrl, roleScope.toString(), roleAssignmentName,
                    context)
                .doOnRequest(ignored -> LOGGER.verbose("Deleting role assignment - {}", roleAssignmentName))
                .doOnSuccess(response -> LOGGER.verbose("Deleted role assignment - {}", response.getValue().getName()))
                .doOnError(error -> LOGGER.warning("Failed to delete role assignment - {}", roleAssignmentName, error))
                .onErrorMap(KeyVaultAdministrationUtils::mapThrowableToKeyVaultAdministrationException)
                .map(response -> (Response<Void>) new SimpleResponse<Void>(response, null))
                .onErrorResume(KeyVaultAdministrationException.class, e ->
                    swallowExceptionForStatusCodeAsync(404, e, LOGGER));
        } catch (RuntimeException e) {
            return monoError(LOGGER, e);
        }
    }
}
