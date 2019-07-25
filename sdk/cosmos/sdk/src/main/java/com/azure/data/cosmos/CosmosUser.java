// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.Permission;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CosmosUser {
    CosmosDatabase database;
    private String id;

    CosmosUser(String id, CosmosDatabase database) {
        this.id = id;
        this.database = database;
    }

    /**
     * Get the id of the {@link CosmosUser}
     * @return the id of the {@link CosmosUser}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosUser}
     * @param id the id of the {@link CosmosUser}
     * @return the same {@link CosmosUser} that had the id set
     */
    CosmosUser id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a cosmos user
     * @return a {@link Mono} containing the single resource response with the read user or an error.
     */
    public Mono<CosmosUserResponse> read() {
        return this.database.getDocClientWrapper()
                .readUser(getLink(), null)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * REPLACE a cosmos user
     *
     * @param userSettings the user properties to use
     * @return a {@link Mono} containing the single resource response with the replaced user or an error.
     */
    public Mono<CosmosUserResponse> replace(CosmosUserProperties userSettings) {
        return this.database.getDocClientWrapper()
                .replaceUser(userSettings.getV2User(), null)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * Delete a cosmos user
     *
     * @return a {@link Mono} containing the single resource response with the deleted user or an error.
     */
    public Mono<CosmosUserResponse> delete() {
        return this.database.getDocClientWrapper()
                .deleteUser(getLink(), null)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * Creates a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionSettings the permission properties to create.
     * @param options    the request options.
     * @return an {@link Mono} containing the single resource response with the created permission or an error.
     */
    public Mono<CosmosPermissionResponse> createPermission(CosmosPermissionProperties permissionSettings, CosmosPermissionRequestOptions options) {
        if(options == null){
            options = new CosmosPermissionRequestOptions();
        }
        Permission permission = permissionSettings.getV2Permissions();
        return database.getDocClientWrapper()
                .createPermission(getLink(), permission, options.toRequestOptions())
                .map(response -> new CosmosPermissionResponse(response, this))
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
     * @param options    the request options.
     * @return an {@link Mono} containing the single resource response with the upserted permission or an error.
     */
    public Mono<CosmosPermissionResponse> upsertPermission(CosmosPermissionProperties permissionSettings, CosmosPermissionRequestOptions options) {
        Permission permission = permissionSettings.getV2Permissions();
        if(options == null){
            options = new CosmosPermissionRequestOptions();
        }
        return database.getDocClientWrapper()
                .upsertPermission(getLink(), permission, options.toRequestOptions())
                .map(response -> new CosmosPermissionResponse(response, this))
                .single();
    }


    /**
     * Reads all permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the read permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the read permissions or an error.
     */
    public Flux<FeedResponse<CosmosPermissionProperties>> readAllPermissions(FeedOptions options) {
        return getDatabase().getDocClientWrapper()
                        .readPermissions(getLink(), options)
                        .map(response-> BridgeInternal.createFeedResponse(CosmosPermissionProperties.getFromV2Results(response.results()),
                                response.responseHeaders()));
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param query          the query.
     * @return an {@link Flux} containing one or several feed response pages of the obtained permissions or an error.
     */
    public Flux<FeedResponse<CosmosPermissionProperties>> queryPermissions(String query) {
        return queryPermissions(query);
    }

    /**
     * Query for permissions.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Flux} will contain one or several feed response pages of the obtained permissions.
     * In case of failure the {@link Flux} will error.
     *
     * @param query          the query.
     * @param options        the feed options.
     * @return an {@link Flux} containing one or several feed response pages of the obtained permissions or an error.
     */
    public Flux<FeedResponse<CosmosPermissionProperties>> queryPermissions(String query, FeedOptions options) {
        return getDatabase().getDocClientWrapper()
                        .queryPermissions(getLink(), query, options)
                        .map(response-> BridgeInternal.createFeedResponse(CosmosPermissionProperties.getFromV2Results(response.results()),
                                response.responseHeaders()));
    }

    /**
     * Get cosmos permission without making a call to backend
     * @param id the id
     * @return the cosmos permission
     */
    public CosmosPermission getPermission(String id){
        return new CosmosPermission(id, this);
    }

    String URIPathSegment() {
        return Paths.USERS_PATH_SEGMENT;
    }

    String parentLink() {
        return database.getLink()   ;
    }

    String getLink() {
        StringBuilder builder = new StringBuilder();
        builder.append(parentLink());
        builder.append("/");
        builder.append(URIPathSegment());
        builder.append("/");
        builder.append(id());
        return builder.toString();
    }

    /**
     * Gets the parent Database
     *
     * @return the (@link CosmosDatabase)
     */
    public CosmosDatabase getDatabase() {
        return database;
    }
}