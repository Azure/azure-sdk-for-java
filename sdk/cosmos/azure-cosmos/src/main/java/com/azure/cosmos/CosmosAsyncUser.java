// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.models.CosmosPermissionResponse;
import com.azure.cosmos.models.CosmosUserResponse;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosPermissionRequestOptions;
import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.UtilBridgeInternal;
import reactor.core.publisher.Mono;

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
    public Mono<CosmosUserResponse> read() {
        return this.database.getDocClientWrapper()
                            .readUser(getLink(), null)
                            .map(response -> ModelBridgeInternal.createCosmosUserResponse(response)).single();
    }

    /**
     * Replace a cosmos user
     *
     * @param userProperties the user properties to use
     * @return a {@link Mono} containing the single resource response with the replaced user or an error.
     */
    public Mono<CosmosUserResponse> replace(CosmosUserProperties userProperties) {
        return this.database.getDocClientWrapper()
                            .replaceUser(ModelBridgeInternal.getV2User(userProperties), null)
                            .map(response -> ModelBridgeInternal.createCosmosUserResponse(response)).single();
    }

    /**
     * Delete a cosmos user
     *
     * @return a {@link Mono} containing the single resource response with the deleted user or an error.
     */
    public Mono<CosmosUserResponse> delete() {
        return this.database.getDocClientWrapper()
                            .deleteUser(getLink(), null)
                            .map(response -> ModelBridgeInternal.createCosmosUserResponse(response)).single();
    }

    /**
     * Creates a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionProperties the permission properties to create.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the created permission or an error.
     */
    public Mono<CosmosPermissionResponse> createPermission(
        CosmosPermissionProperties permissionProperties,
        CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }
        Permission permission = ModelBridgeInternal.getPermission(permissionProperties, database.getId());
        return database.getDocClientWrapper()
                   .createPermission(getLink(), permission, ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosPermissionResponse(response))
                   .single();
    }

    /**
     * Upserts a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the upserted permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionProperties the permission properties to upsert.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the upserted permission or an error.
     */
    public Mono<CosmosPermissionResponse> upsertPermission(
        CosmosPermissionProperties permissionProperties,
        CosmosPermissionRequestOptions options) {
        Permission permission = ModelBridgeInternal.getPermission(permissionProperties, database.getId());
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }
        return database.getDocClientWrapper()
                   .upsertPermission(getLink(), permission, ModelBridgeInternal.toRequestOptions(options))
                   .map(response -> ModelBridgeInternal.createCosmosPermissionResponse(response))
                   .single();
    }


    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read permissions or an
     * error.
     */
    public CosmosPagedFlux<CosmosPermissionProperties> readAllPermissions() {
        return readAllPermissions(new CosmosQueryRequestOptions());
    }

    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the read permissions or an
     * error.
     */
    CosmosPagedFlux<CosmosPermissionProperties> readAllPermissions(CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                       .readPermissions(getLink(), options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosPermissionPropertiesFromResults(response.getResults()),
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
        return queryPermissions(query, new CosmosQueryRequestOptions());
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosPagedFlux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link CosmosPagedFlux} will error.
     *
     * @param query the query.
     * @param options the query request options.
     * @return a {@link CosmosPagedFlux} containing one or several feed response pages of the obtained permissions or
     * an error.
     */
    public CosmosPagedFlux<CosmosPermissionProperties> queryPermissions(String query, CosmosQueryRequestOptions options) {
        return UtilBridgeInternal.createCosmosPagedFlux(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                       .queryPermissions(getLink(), query, options)
                       .map(response -> BridgeInternal.createFeedResponse(
                           ModelBridgeInternal.getCosmosPermissionPropertiesFromResults(response.getResults()),
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
    CosmosAsyncDatabase getDatabase() {
        return database;
    }
}
