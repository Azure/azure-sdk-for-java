// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.OperationType;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.implementation.ResourceType;
import com.azure.cosmos.models.CosmosPermissionProperties;
import com.azure.cosmos.models.CosmosPermissionRequestOptions;
import com.azure.cosmos.models.CosmosPermissionResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

/**
 *  Has methods to operate on a per-User Permission to access a specific resource
 */
public class CosmosAsyncPermission {

    private final CosmosAsyncUser cosmosUser;

    @SuppressWarnings("EnforceFinalFields")
    private String id;

    CosmosAsyncPermission(String id, CosmosAsyncUser user) {
        this.id = id;
        this.cosmosUser = user;
    }

    /**
     * Get the id of the {@link CosmosAsyncPermission}
     *
     * @return the id of the {@link CosmosAsyncPermission}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncPermission}
     *
     * @param id the id of the {@link CosmosAsyncPermission}
     * @return the same {@link CosmosAsyncPermission} that had the id set
     */
    CosmosAsyncPermission setId(String id) {
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
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the read permission or an error.
     */
    public Mono<CosmosPermissionResponse> read(CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }

        final CosmosPermissionRequestOptions requestOptions = options;
        return withContext(context -> readInternal(requestOptions, context));
    }

    /**
     * Replaces a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the replaced permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param permissionProperties the permission properties to use.
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response with the replaced permission or an error.
     */
    public Mono<CosmosPermissionResponse> replace(CosmosPermissionProperties permissionProperties,
                                                  CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }

        final CosmosPermissionRequestOptions requestOptions = options;
        return withContext(context -> replaceInternal(permissionProperties, requestOptions, context));
    }

    /**
     * Deletes a permission.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response for the deleted permission.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return an {@link Mono} containing the single resource response for the deleted permission or an error.
     */
    public Mono<CosmosPermissionResponse> delete(CosmosPermissionRequestOptions options) {
        if (options == null) {
            options = new CosmosPermissionRequestOptions();
        }

        final CosmosPermissionRequestOptions requestOptions = options;
        return withContext(context -> deleteInternal(requestOptions, context));
    }

    String getURIPathSegment() {
        return Paths.PERMISSIONS_PATH_SEGMENT;
    }

    String getParentLink() {
        return cosmosUser.getLink();
    }

    String getLink() {
        return getParentLink()
            + "/"
            + getURIPathSegment()
            + "/"
            + getId();
    }

    private Mono<CosmosPermissionResponse> readInternal(CosmosPermissionRequestOptions options, Context context) {

        String spanName = "readPermission." + cosmosUser.getId();
        RequestOptions nonNullRequestOptions = options != null
            ? ModelBridgeInternal.toRequestOptions(options)
            : new RequestOptions();
        Mono<CosmosPermissionResponse> responseMono = cosmosUser.getDatabase()
            .getDocClientWrapper()
            .readPermission(getLink(), nonNullRequestOptions)
            .map(ModelBridgeInternal::createCosmosPermissionResponse)
            .single();
        CosmosAsyncClient client = cosmosUser.getDatabase().getClient();

        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            cosmosUser.getDatabase().getId(),
            null,
            client,
            null,
            OperationType.Read,
            ResourceType.Permission,
            nonNullRequestOptions);
    }

    private Mono<CosmosPermissionResponse> replaceInternal(CosmosPermissionProperties permissionProperties,
                                                                CosmosPermissionRequestOptions options,
                                                                Context context) {

        String spanName = "replacePermission." + cosmosUser.getId();
        RequestOptions nonNullRequestOptions = ensureAndConvertRequestOptions(options);
        CosmosAsyncDatabase databaseContext = cosmosUser.getDatabase();
        Mono<CosmosPermissionResponse> responseMono = cosmosUser.getDatabase()
            .getDocClientWrapper()
            .replacePermission(ModelBridgeInternal.getPermission(permissionProperties, databaseContext.getId()),
                nonNullRequestOptions)
            .map(ModelBridgeInternal::createCosmosPermissionResponse)
            .single();
        CosmosAsyncClient client = cosmosUser.getDatabase().getClient();

        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            cosmosUser.getDatabase().getId(),
            null,
            client,
            null,
            OperationType.Replace,
            ResourceType.Permission,
            nonNullRequestOptions);
    }

    private Mono<CosmosPermissionResponse> deleteInternal(CosmosPermissionRequestOptions options,
                                                               Context context) {

        String spanName = "deletePermission." + cosmosUser.getId();
        RequestOptions nonNullRequestOptions = ensureAndConvertRequestOptions(options);

        Mono<CosmosPermissionResponse> responseMono = cosmosUser.getDatabase()
            .getDocClientWrapper()
            .deletePermission(getLink(), nonNullRequestOptions)
            .map(ModelBridgeInternal::createCosmosPermissionResponse)
            .single();
        CosmosAsyncClient client = cosmosUser.getDatabase().getClient();

        return client.getDiagnosticsProvider().traceEnabledCosmosResponsePublisher(
            responseMono,
            context,
            spanName,
            cosmosUser.getDatabase().getId(),
            null,
            client,
            null,
            OperationType.Delete,
            ResourceType.Permission,
            nonNullRequestOptions);
    }

    private static RequestOptions ensureAndConvertRequestOptions(CosmosPermissionRequestOptions options) {
        if (options != null) {
            return ModelBridgeInternal.toRequestOptions(options);
        }
        return new RequestOptions();
    }
}
