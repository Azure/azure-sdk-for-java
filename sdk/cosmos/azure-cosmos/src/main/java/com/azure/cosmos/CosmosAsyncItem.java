// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.Document;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.RequestOptions;
import reactor.core.publisher.Mono;

public class CosmosAsyncItem {
    private final Object partitionKey;
    private final String id;
    private final String link;
    private CosmosAsyncContainer container;

    CosmosAsyncItem(String id, Object partitionKey, CosmosAsyncContainer container) {
        this.id = id;
        this.partitionKey = partitionKey;
        this.container = container;
        this.link = getParentLink() + "/" + getURIPathSegment() + "/" + getId();
    }

    /**
     * Get the id of the {@link CosmosAsyncItem}
     *
     * @return the id of the {@link CosmosAsyncItem}
     */
    public String getId() {
        return id;
    }

    /**
     * Reads an item.
     * <p>
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
     * <p>
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param item the item to replace (containing the document id).
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosAsyncItemResponse> replace(Object item) {
        return replace(item, new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Replaces an item with the passed in item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param item the item to replace (containing the document id).
     * @param options the request comosItemRequestOptions
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosAsyncItemResponse> replace(Object item, CosmosItemRequestOptions options) {
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
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosAsyncItemResponse> delete() {
        return delete(new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Deletes the item.
     * <p>
     * After subscription the operation will be performed.
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param options the request options
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosAsyncItemResponse> delete(CosmosItemRequestOptions options) {
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

    String getURIPathSegment() {
        return Paths.DOCUMENTS_PATH_SEGMENT;
    }

    String getParentLink() {
        return this.container.getLink();
    }

    String getLink() {
        return this.link;
    }
}
