// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import reactor.core.publisher.Mono;

/**
 * Read and delete conflicts
 */
public class CosmosAsyncConflict {

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
    public Mono<CosmosAsyncConflictResponse> read(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return this.container.getDatabase().getDocClientWrapper().readConflict(getLink(), requestOptions)
                   .map(response -> new CosmosAsyncConflictResponse(response, container)).single();

    }

    /**
     * Reads all conflicts in a document collection.
     * <p>
     * After subscription the operation will be performed. The {@link Mono} will
     * contain one or several feed response pages of the read conflicts. In case of
     * failure the {@link Mono} will error.
     *
     * @param options the feed options.
     * @return a {@link Mono} containing one or several feed response pages of the
     * read conflicts or an error.
     */
    public Mono<CosmosAsyncConflictResponse> delete(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return this.container.getDatabase().getDocClientWrapper().deleteConflict(getLink(), requestOptions)
                   .map(response -> new CosmosAsyncConflictResponse(response, container)).single();
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
}
