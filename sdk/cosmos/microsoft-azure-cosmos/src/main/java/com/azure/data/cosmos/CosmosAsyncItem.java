// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Document;
import com.azure.data.cosmos.internal.Paths;
import com.azure.data.cosmos.internal.RequestOptions;
import reactor.core.publisher.Mono;

public class CosmosAsyncItem {
    private Object partitionKey;
    private CosmosAsyncContainer container;
    private String id;

    CosmosAsyncItem(String id, Object partitionKey, CosmosAsyncContainer container) {
        this.id = id;
        this.partitionKey = partitionKey;
        this.container = container;
    }

    /**
     * Get the id of the {@link CosmosAsyncItem}
     * @return the id of the {@link CosmosAsyncItem}
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the {@link CosmosAsyncItem}
     * @param id the id of the {@link CosmosAsyncItem}
     * @return the same {@link CosmosAsyncItem} that had the id set
     */
    CosmosAsyncItem id(String id) {
        this.id = id;
        return this;
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
    public Mono<CosmosAsyncItemResponse> read() {
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
    public Mono<CosmosAsyncItemResponse> read(CosmosItemRequestOptions options) {
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return container.getDatabase().getDocClientWrapper()
                .readDocument(getLink(), requestOptions)
                .map(response -> new CosmosAsyncItemResponse(response, requestOptions.getPartitionKey(), container))
                .single();
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
    public Mono<CosmosAsyncItemResponse> replace(Object item){
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
    public Mono<CosmosAsyncItemResponse> replace(Object item, CosmosItemRequestOptions options){
        Document doc = CosmosItemProperties.fromObject(item);
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return container.getDatabase()
                .getDocClientWrapper()
                .replaceDocument(getLink(), doc, requestOptions)
                .map(response -> new CosmosAsyncItemResponse(response, requestOptions.getPartitionKey(), container))
                .single();
    }

    /**
     * Deletes the item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosAsyncItemResponse> delete() {
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
    public Mono<CosmosAsyncItemResponse> delete(CosmosItemRequestOptions options){
        if (options == null) {
            options = new CosmosItemRequestOptions();
        }
        RequestOptions requestOptions = options.toRequestOptions();
        return container.getDatabase()
                        .getDocClientWrapper()
                        .deleteDocument(getLink(), requestOptions)
                        .map(response -> new CosmosAsyncItemResponse(response, requestOptions.getPartitionKey(), container))
                        .single();
    }
    
    void setContainer(CosmosAsyncContainer container) {
        this.container = container;
    }

    String URIPathSegment() {
        return Paths.DOCUMENTS_PATH_SEGMENT;
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
