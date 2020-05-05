// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.TracerProvider;
import com.azure.cosmos.models.CosmosAsyncPermissionResponse;
import com.azure.cosmos.models.CosmosAsyncUserResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosPermissionRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.FeedOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

/**
 * The type Cosmos async user.
 */
public class CosmosAsyncUser {
    private final CosmosAsyncDatabase database;
    private String id;

    CosmosAsyncUser(String id, CosmosAsyncDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosAsyncUser}
     *
     * @return the id of the {@link CosmosAsyncUser}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncUser}
     *
     * @param id the id of the {@link CosmosAsyncUser}
     * @return the same {@link CosmosAsyncUser} that had the id set
     */
    CosmosAsyncUser setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a cosmos user
     *
     * @return a {@link Mono} containing the single resource response with the read user or an error.
     */
    public Mono<CosmosAsyncUserResponse> read() {
        return TracerProvider.cosmosWithContext(withContext(context -> readInternal(context)),
            database.getClient().getTracerProvider());
    }

    /**
     * REPLACE a cosmos user
     *
     * @param userSettings the user properties to use
     * @return a {@link Mono} containing the single resource response with the replaced user or an error.
     */
    public Mono<CosmosAsyncUserResponse> replace(CosmosUserProperties userSettings) {
        return TracerProvider.cosmosWithContext(withContext(context -> replaceInternal(userSettings, context)),
            database.getClient().getTracerProvider());
    }

    /**
     * Delete a cosmos user
     *
     * @return a {@link Mono} containing the single resource response with the deleted user or an error.
     */
    public Mono<CosmosAsyncUserResponse> delete() {
        return TracerProvider.cosmosWithContext(withContext(context -> deleteInternal(context)),
            database.getClient().getTracerProvider());
    }

    /**
     * Creates a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionSettings the permission properties to create.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the created permission or an error.
     */
    public Mono<CosmosAsyncPermissionResponse> createPermission(
        CosmosPermissionProperties permissionSettings,
        CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }

        Permission permission = ModelBridgeInternal.getV2Permissions(permissionSettings);
        final CosmosPermissionRequestOptions requesOptions = options;
        return TracerProvider.cosmosWithContext(withContext(context -> createPermissionInternal(permission, requesOptions, context)),
            database.getClient().getTracerProvider());
    }

    /**
     * Upserts a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the upserted permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionSettings the permission properties to upsert.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted permission or an error.
     */
    public Mono<CosmosAsyncPermissionResponse> upsertPermission(
        CosmosPermissionProperties permissionSettings,
        CosmosPermissionRequestOptions options) {
        Permission permission = ModelBridgeInternal.getV2Permissions(permissionSettings);
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }

        final CosmosPermissionRequestOptions requestOptions = options;
        return TracerProvider.cosmosWithContext(withContext(context -> upsertPermissionInternal(permission,
            requestOptions, context)),
            database.getClient().getTracerProvider());
    }


    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read permissions or an
     * error.
     */
    public CosmosPagedFlux<CosmosPermissionProperties> readAllPermissions(FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "readAllPermissions." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.getDatabase().getClient().getServiceEndpoint(),
                this.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                       .readPermissions(getLink(), options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosPermissionPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained permissions or
     * an error.
     */
    public CosmosPagedFlux<CosmosPermissionProperties> queryPermissions(String query) {
        return queryPermissions(query, new FeedOptions());
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the feed options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained permissions or
     * an error.
     */
    public CosmosPagedFlux<CosmosPermissionProperties> queryPermissions(String query, FeedOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            String spanName = "queryPermissions." + this.getId();
            pagedFluxOptions.setTracerInformation(this.getDatabase().getClient().getTracerProvider(),
                spanName,
                this.getDatabase().getClient().getServiceEndpoint(),
                this.getDatabase().getId());
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                       .queryPermissions(getLink(), query, options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosPermissionPropertiesFromV2Results(response.getResults()),
                           response.getResponseHeaders()));
        });
    }

    /**
     * Get cosmos permission without making a call to backend
     *
     * @param id the id
     * @return the cosmos permission
     */
    public CosmosAsyncPermission getPermission(String id) {
        return new CosmosAsyncPermission(id, this);
    }

    String getURIPathSegment() {
        return Paths.USERS_PATH_SEGMENT;
    }

    String getParentLink() {
        return database.getLink();
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(getParentLink());
        builder.append("/");
        builder.append(getURIPathSegment());
        builder.append("/");
        builder.append(getId());
        return builder.toString();
    }

    /**
     * Gets the parent Database
     *
     * @return the (@link CosmosAsyncDatabase)
     */
    public CosmosAsyncDatabase getDatabase() {
        return database;
    }

    private Mono<CosmosAsyncUserResponse> readInternal(Context context) {
        String spanName = "readUser." + getId();
        Mono<CosmosAsyncUserResponse> responseMono = this.database.getDocClientWrapper()
            .readUser(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserResponse(response, database)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, database.getId(), database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncUserResponse> replaceInternal(CosmosUserProperties userSettings, Context context) {
        String spanName = "replaceUser." + getId();
        Mono<CosmosAsyncUserResponse> responseMono = this.database.getDocClientWrapper()
            .replaceUser(ModelBridgeInternal.getV2User(userSettings), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserResponse(response, database)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, database.getId(), database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncUserResponse> deleteInternal(Context context) {
        String spanName = "deleteUser." + getId();
        Mono<CosmosAsyncUserResponse> responseMono = this.database.getDocClientWrapper()
            .deleteUser(getLink(), null)
            .map(response -> ModelBridgeInternal.createCosmosAsyncUserResponse(response, database)).single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, database.getId(), database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncPermissionResponse> createPermissionInternal(
        Permission permission,
        CosmosPermissionRequestOptions options,
        Context context) {
        String spanName = "createPermission." + getId();
        Mono<CosmosAsyncPermissionResponse> responseMono = database.getDocClientWrapper()
            .createPermission(getLink(), permission, ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncPermissionResponse(response, this))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, database.getId(), database.getClient().getServiceEndpoint());
    }

    private Mono<CosmosAsyncPermissionResponse> upsertPermissionInternal(
        Permission permission,
        CosmosPermissionRequestOptions options,
        Context context) {
        String spanName = "upsertPermission." + getId();
        Mono<CosmosAsyncPermissionResponse> responseMono = database.getDocClientWrapper()
            .upsertPermission(getLink(), permission, ModelBridgeInternal.toRequestOptions(options))
            .map(response -> ModelBridgeInternal.createCosmosAsyncPermissionResponse(response, this))
            .single();
        return database.getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName, database.getId(), database.getClient().getServiceEndpoint());
    }
}
