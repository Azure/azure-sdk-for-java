package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class CosmosUser extends CosmosResource {
    CosmosDatabase database;
    CosmosUser(String id, CosmosDatabase database) {
        super(id);
        this.database = database;
    }

    /**
     * Reads a cosmos user
     * @return an {@link Mono} containing the single cosmos user response with the read user or an error.
     */
    public Mono<CosmosUserResponse> read() {
        return this.read(null);
    }

    /**
     * Reads a cosmos user
     * @param options the request options
     * @return a {@link Mono} containing the single resource response with the read user or an error.
     */
    public Mono<CosmosUserResponse> read(RequestOptions options) {
        return this.database.getDocClientWrapper()
                .readUser(getLink(), options)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * REPLACE a cosmos user
     *
     * @param userSettings the user settings to use
     * @param options      the request options
     * @return a {@link Mono} containing the single resource response with the replaced user or an error.
     */
    public Mono<CosmosUserResponse> replace(CosmosUserProperties userSettings, RequestOptions options) {
        return this.database.getDocClientWrapper()
                .replaceUser(userSettings.getV2User(), options)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * DELETE a cosmos user
     *
     * @param options the request options
     * @return a {@link Mono} containing the single resource response with the deleted user or an error.
     */
    public Mono<CosmosUserResponse> delete(RequestOptions options) {
        return this.database.getDocClientWrapper()
                .deleteUser(getLink(), options)
                .map(response -> new CosmosUserResponse(response, database)).single();
    }

    /**
     * Creates a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the created permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionSettings the permission settings to create.
     * @param options    the request options.
     * @return an {@link Mono} containing the single resource response with the created permission or an error.
     */
    public Mono<CosmosPermissionResponse> createPermission(CosmosPermissionProperties permissionSettings, CosmosPermissionsRequestOptions options) {
        if(options == null){
            options = new CosmosPermissionsRequestOptions();
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
     * @param permissionSettings the permission settings to upsert.
     * @param options    the request options.
     * @return an {@link Mono} containing the single resource response with the upserted permission or an error.
     */
    public Mono<CosmosPermissionResponse> upsertPermission(CosmosPermissionProperties permissionSettings, CosmosPermissionsRequestOptions options) {
        Permission permission = permissionSettings.getV2Permissions();
        if(options == null){
            options = new CosmosPermissionsRequestOptions();
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
    public Flux<FeedResponse<CosmosPermissionProperties>> listPermissions(FeedOptions options) {
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
    
    @Override
    protected String URIPathSegment() {
        return Paths.USERS_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return database.getLink()   ;
    }

    public CosmosDatabase getDatabase() {
        return database;
    }
}