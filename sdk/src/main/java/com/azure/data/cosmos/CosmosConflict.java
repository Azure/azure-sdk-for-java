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

import com.azure.data.cosmos.changefeed.internal.ChangeFeedHelper;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

public class CosmosConflict extends CosmosResource {

    private CosmosContainer container;

    /**
     * Constructor
     * @param id the conflict id
     * @param container the container
     */
    CosmosConflict(String id, CosmosContainer container) {
        super(id);
        this.container = container;
    }

    /**
     * Reads a conflict.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single resource response with the read conflict.
     * In case of failure the {@link Mono} will error.
     *
     * @param options      the request options.
     * @return a {@link Mono} containing the single resource response with the read conflict or an error.
     */
    public Mono<CosmosConflictResponse> read(CosmosConflictRequestOptions options){
        if(options == null){
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(this.container.getDatabase()
                .getDocClientWrapper()
                .readConflict(getLink(), requestOptions)
                .map(response -> new CosmosConflictResponse(response, container))
                .toSingle()));
                
    }

    /**
     * Reads all conflicts in a document collection.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} will contain one or several feed response pages of the read conflicts.
     * In case of failure the {@link Mono} will error.
     *
     * @param options        the feed options.
     * @return an {@link Mono} containing one or several feed response pages of the read conflicts or an error.
     */
    public Mono<CosmosConflictResponse> delete(CosmosConflictRequestOptions options){
        if(options == null){
            options = new CosmosConflictRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(this.container.getDatabase()
                .getDocClientWrapper()
                .deleteConflict(getLink(), requestOptions)
                .map(response -> new CosmosConflictResponse(response, container))
                .toSingle()));
    }

    @Override
    protected String URIPathSegment() {
        return ChangeFeedHelper.Paths.CONFLICTS_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return this.container.getLink();
    }
}
