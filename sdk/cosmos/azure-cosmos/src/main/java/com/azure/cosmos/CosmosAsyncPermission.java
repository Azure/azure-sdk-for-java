// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.internal.Paths;
import com.azure.cosmos.internal.RequestOptions;
import com.azure.cosmos.internal.Paths;
import com.azure.cosmos.internal.RequestOptions;
import reactor.core.publisher.Mono;

public class CosmosAsyncPermission {

    private final CosmosAsyncUser cosmosUser;
    private String id;

    CosmosAsyncPermission(String id, CosmosAsyncUser user){
        this.id = id;
        this.cosmosUser = user; 
    }

    /**
     * Get the id of the {@link CosmosAsyncPermission}
     * @return the id of the {@link CosmosAsyncPermission}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncPermission}
     * @param id the id of the {@link CosmosAsyncPermission}
     * @return the same {@link CosmosAsyncPermission} that had the id set
     */
    CosmosAsyncPermission id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the read permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param options        the request options.
     * @return an {@link Mono} containing the single resource response with the read permission or an error.
     */
    public Mono<CosmosAsyncPermissionResponse> read(RequestOptions options) {

        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .readPermission(getLink(),options)
                .map(response -> new CosmosAsyncPermissionResponse(response, cosmosUser))
                .single();
    }

    /**
     * Replaces a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionSettings the permission properties to use.
     * @param options    the request options.
     * @return an {@link Mono} containing the single resource response with the replaced permission or an error.
     */
    public Mono<CosmosAsyncPermissionResponse> replace(CosmosPermissionProperties permissionSettings, RequestOptions options) {
        
        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .replacePermission(permissionSettings.getV2Permissions(), options)
                .map(response -> new CosmosAsyncPermissionResponse(response, cosmosUser))
                .single();
    }

    /**
     * Deletes a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param options        the request options.
     * @return an {@link Mono} containing the single resource response for the deleted permission or an error.
     */
    public Mono<CosmosAsyncPermissionResponse> delete(CosmosPermissionRequestOptions options) {
        if(options == null){
            options = new CosmosPermissionRequestOptions();
        }
        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .deletePermission(getLink(), options.toRequestOptions())
                .map(response -> new CosmosAsyncPermissionResponse(response, cosmosUser))
                .single();
    }

    String URIPathSegment() {
        return Paths.PERMISSIONS_PATH_SEGMENT;
    }

    String parentLink() {
        return cosmosUser.getLink();
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
}
