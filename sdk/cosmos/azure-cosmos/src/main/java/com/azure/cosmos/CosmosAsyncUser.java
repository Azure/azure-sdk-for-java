// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Permission;
import reactor.core.publisher.Mono;

import static com.azure.cosmos.implementation.Utils.setContinuationTokenAndMaxItemCount;

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
        return this.database.getDocClientWrapper()
                   .readUser(getLink(), null)
                   .map(response -> new CosmosAsyncUserResponse(response, database)).single();
    }

    /**
     * REPLACE a cosmos user
     *
     * @param userSettings the user properties to use
     * @return a {@link Mono} containing the single resource response with the replaced user or an error.
     */
    public Mono<CosmosAsyncUserResponse> replace(CosmosUserProperties userSettings) {
        return this.database.getDocClientWrapper()
                   .replaceUser(userSettings.getV2User(), null)
                   .map(response -> new CosmosAsyncUserResponse(response, database)).single();
    }

    /**
     * Delete a cosmos user
     *
     * @return a {@link Mono} containing the single resource response with the deleted user or an error.
     */
    public Mono<CosmosAsyncUserResponse> delete() {
        return this.database.getDocClientWrapper()
                   .deleteUser(getLink(), null)
                   .map(response -> new CosmosAsyncUserResponse(response, database)).single();
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
    public Mono<CosmosAsyncPermissionResponse> createPermission(CosmosPermissionProperties permissionSettings,
                                                                CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }
        Permission permission = permissionSettings.getV2Permissions();
        return database.getDocClientWrapper()
                   .createPermission(getLink(), permission, options.toRequestOptions())
                   .map(response -> new CosmosAsyncPermissionResponse(response, this))
                   .single();
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
    public Mono<CosmosAsyncPermissionResponse> upsertPermission(CosmosPermissionProperties permissionSettings,
                                                                CosmosPermissionRequestOptions options) {
        Permission permission = permissionSettings.getV2Permissions();
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }
        return database.getDocClientWrapper()
                   .upsertPermission(getLink(), permission, options.toRequestOptions())
                   .map(response -> new CosmosAsyncPermissionResponse(response, this))
                   .single();
    }


    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosContinuablePagedFlux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link CosmosContinuablePagedFlux} will error.
     *
     * @param options the feed options.
     * @return a {@link CosmosContinuablePagedFlux} containing one or several feed response pages of the read permissions or an error.
     */
    public CosmosContinuablePagedFlux<CosmosPermissionProperties> readAllPermissions(FeedOptions options) {
        return new CosmosContinuablePagedFlux<>(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                                .readPermissions(getLink(), options)
                                .map(response -> BridgeInternal.createFeedResponse(
                                    CosmosPermissionProperties.getFromV2Results(response.getResults()),
                                    response.getResponseHeaders()));
        });
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosContinuablePagedFlux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link CosmosContinuablePagedFlux} will error.
     *
     * @param query the query.
     * @return a {@link CosmosContinuablePagedFlux} containing one or several feed response pages of the obtained permissions or an error.
     */
    public CosmosContinuablePagedFlux<CosmosPermissionProperties> queryPermissions(String query) {
        return queryPermissions(query, new FeedOptions());
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link CosmosContinuablePagedFlux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link CosmosContinuablePagedFlux} will error.
     *
     * @param query the query.
     * @param options the feed options.
     * @return a {@link CosmosContinuablePagedFlux} containing one or several feed response pages of the obtained permissions or an error.
     */
    public CosmosContinuablePagedFlux<CosmosPermissionProperties> queryPermissions(String query, FeedOptions options) {
        return new CosmosContinuablePagedFlux<>(pagedFluxOptions -> {
            setContinuationTokenAndMaxItemCount(pagedFluxOptions, options);
            return getDatabase().getDocClientWrapper()
                                .queryPermissions(getLink(), query, options)
                                .map(response -> BridgeInternal.createFeedResponse(
                                    CosmosPermissionProperties.getFromV2Results(response.getResults()),
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
}
