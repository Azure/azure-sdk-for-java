// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;
import reactor.core.publisher.Mono;

import static com.azure.data.cosmos.internal.Paths.CONFLICTS_PATH_SEGMENT;

/**
 * Read and delete conflicts
 */
public class CosmosConflict {

    private CosmosContainer container;
    private String id;

    /**
     * Constructor
     * 
     * @param id        the conflict id
     * @param container the container
     */
    CosmosConflict(String id, CosmosContainer container) {
        this.id = id;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosConflict}
     * 
     * @return the id of the {@link CosmosConflict}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosConflict}
     * 
     * @param id the id of the {@link CosmosConflict}
     * @return the same {@link CosmosConflict} that had the id set
     */
    CosmosConflict id(String id) {
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
     *         conflict or an error.
     */
    public Mono<CosmosConflictResponse> read(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return this.container.getDatabase().getDocClientWrapper().readConflict(getLink(), requestOptions)
                .map(response -> new CosmosConflictResponse(response, container)).single();

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
     *         read conflicts or an error.
     */
    public Mono<CosmosConflictResponse> delete(CosmosConflictRequestOptions options) {
        if (options == null) {
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return this.container.getDatabase().getDocClientWrapper().deleteConflict(getLink(), requestOptions)
                .map(response -> new CosmosConflictResponse(response, container)).single();
    }

    String URIPathSegment() {
        return CONFLICTS_PATH_SEGMENT;
    }

    String parentLink() {
        return this.container.getLink();
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
