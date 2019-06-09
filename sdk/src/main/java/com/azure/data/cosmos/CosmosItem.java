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

import com.azure.data.cosmos.internal.Paths;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

public class CosmosItem extends CosmosResource{
    private Object partitionKey;
    private CosmosContainer container;

     CosmosItem(String id, Object partitionKey, CosmosContainer container) {
        super(id);
        this.partitionKey = partitionKey;
        this.container = container;
    }

    /**
     * Reads an item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos item response with the read item
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the cosmos item response with the read item or an error
     */
    public Mono<CosmosItemResponse> read() {
        return read(new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Reads an item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos item response with the read item
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request comosItemRequestOptions
     * @return an {@link Mono} containing the cosmos item response with the read item or an error
     */
    public Mono<CosmosItemResponse> read(CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(container.getDatabase().getDocClientWrapper()
                .readDocument(getLink(), requestOptions)
                .map(response -> new CosmosItemResponse(response, requestOptions.getPartitionKey(), container))
                .toSingle()));
    }

    /**
     * Replaces an item with the passed in item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param item the item to replace (containing the document id).
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosItemResponse> replace(Object item){
        return replace(item, new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Replaces an item with the passed in item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param item the item to replace (containing the document id).
     * @param options the request comosItemRequestOptions
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosItemResponse> replace(Object item, CosmosItemRequestOptions options){
        Document doc = CosmosItemProperties.fromObject(item);
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(container.getDatabase()
                .getDocClientWrapper()
                .replaceDocument(getLink(), doc, requestOptions)
                .map(response -> new CosmosItemResponse(response, requestOptions.getPartitionKey(), container))
                .toSingle()));
    }

    /**
     * Deletes the item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosItemResponse> delete() {
        return delete(new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Deletes the item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosItemResponse> delete(CosmosItemRequestOptions options){
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(container.getDatabase()
                        .getDocClientWrapper()
                        .deleteDocument(getLink(), requestOptions)
                        .map(response -> new CosmosItemResponse(response, requestOptions.getPartitionKey(), container))
                        .toSingle()));
    }
    
    void setContainer(CosmosContainer container) {
        this.container = container;
    }

    @Override
    protected String URIPathSegment() {
        return Paths.DOCUMENTS_PATH_SEGMENT;
    }

    @Override
    protected String parentLink() {
        return this.container.getLink();
    }
}
