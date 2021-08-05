// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.core.util.Context;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import com.azure.cosmos.models.CosmosConflictRequestOptions;
import com.azure.cosmos.models.CosmosConflictResponse;
import com.azure.cosmos.models.ModelBridgeInternal;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * Read and delete conflicts
 */
public final class CosmosAsyncConflict {

    private final CosmosAsyncContainer container;
    private String id;

    /**
     * Constructor
     *
     * @param id the conflict id
     * @param container the container
     */
    CosmosAsyncConflict(String id, CosmosAsyncContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncConflict}
     *
     * @return the id of the {@link CosmosAsyncConflict}
     */
    public String getId() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncConflict}
     *
     * @param id the id of the {@link CosmosAsyncConflict}
     * @return the same {@link CosmosAsyncConflict} that had the id set
     */
    CosmosAsyncConflict setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Reads a conflict.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response with the read
     * conflict. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return a {@link Mono} containing the single resource response with the read
     * conflict or an error.
     */
    public Mono<CosmosConflictResponse> read(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> readInternal(requestOptions, context));
    }

    /**
     * Deletes a conflict.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} upon
     * successful completion will contain a single resource response for the deleted
     * conflict. In case of failure the {@link Mono} will error.
     *
     * @param options the request options.
     * @return a {@link Mono} containing the single resource response for the deleted
     * conflict or an error.
     */
    public Mono<CosmosConflictResponse> delete(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = ModelBridgeInternal.toRequestOptions(options);
        return withContext(context -> deleteInternal(requestOptions, context));
    }

    String getURIPathSegment() {
        return Paths.CONFLICTS_PATH_SEGMENT;
    }

    String getParentLink() {
        return this.container.getLink();
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

    private Mono<CosmosConflictResponse> readInternal(RequestOptions options, Context context) {
        String spanName = "readConflict." + getId();
        Mono<CosmosConflictResponse> responseMono =
            this.container.getDatabase().getDocClientWrapper().readConflict(getLink(), options)
            .map(response -> ModelBridgeInternal.createCosmosConflictResponse(response)).single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            this.container.getDatabase().getId(),
            this.container.getDatabase().getClient().getServiceEndpoint());

    }

    private Mono<CosmosConflictResponse> deleteInternal(RequestOptions options, Context context) {
        String spanName = "deleteConflict." + getId();
        Mono<CosmosConflictResponse> responseMono =
            this.container.getDatabase().getDocClientWrapper().deleteConflict(getLink(), options)
            .map(response -> ModelBridgeInternal.createCosmosConflictResponse(response)).single();
        return this.container.getDatabase().getClient().getTracerProvider().traceEnabledCosmosResponsePublisher(responseMono, context,
            spanName,
            this.container.getDatabase().getId(),
            this.container.getDatabase().getClient().getServiceEndpoint());
    }
}
