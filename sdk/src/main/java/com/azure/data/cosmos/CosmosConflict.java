/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.azure.data.cosmos;

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
     * @return an {@link Mono} containing one or several feed response pages of the
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
