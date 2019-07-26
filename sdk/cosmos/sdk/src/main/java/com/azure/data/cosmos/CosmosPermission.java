// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.RequestOptions;
import reactor.core.publisher.Mono;

public class CosmosPermission {

    private final CosmosUser cosmosUser;
    private String id;

    CosmosPermission(String id, CosmosUser user){
        this.id = id;
        this.cosmosUser = user; 
    }

    /**
     * Get the id of the {@link CosmosPermission}
     * @return the id of the {@link CosmosPermission}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosPermission}
     * @param id the id of the {@link CosmosPermission}
     * @return the same {@link CosmosPermission} that had the id set
     */
    CosmosPermission id(String id) {
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
    public Mono<CosmosPermissionResponse> read(RequestOptions options) {

        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .readPermission(getLink(),options)
                .map(response -> new CosmosPermissionResponse(response, cosmosUser))
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
    public Mono<CosmosPermissionResponse> replace(CosmosPermissionProperties permissionSettings, RequestOptions options) {
        
        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .replacePermission(permissionSettings.getV2Permissions(), options)
                .map(response -> new CosmosPermissionResponse(response, cosmosUser))
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
    public Mono<CosmosPermissionResponse> delete(CosmosPermissionRequestOptions options) {
        if(options == null){
            options = new CosmosPermissionRequestOptions();
        }
        return cosmosUser.getDatabase()
                .getDocClientWrapper()
                .deletePermission(getLink(), options.toRequestOptions())
                .map(response -> new CosmosPermissionResponse(response, cosmosUser))
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
