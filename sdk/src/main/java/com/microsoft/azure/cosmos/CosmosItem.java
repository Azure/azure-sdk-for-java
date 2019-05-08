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
package com.microsoft.azure.cosmos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.Resource;
import com.microsoft.azure.cosmosdb.internal.Paths;
import com.microsoft.azure.cosmosdb.internal.Utils;
import hu.akarnokd.rxjava.interop.RxJavaInterop;
import reactor.adapter.rxjava.RxJava2Adapter;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class CosmosItem extends Resource {

    private CosmosContainer container;
    final static ObjectMapper mapper = Utils.getSimpleObjectMapper();

    CosmosItem(String json, CosmosContainer container) {
        super(json);
        this.container = container;
    }

    /**
     * Initialize a item object.
     */
    public CosmosItem() {
        super();
    }

    /**
     * Initialize a CosmosItem object from json string.
     *
     * @param jsonString the json string that represents the document object.
     */
    public CosmosItem(String jsonString) {
        super(jsonString);
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
    public Mono<CosmosItemResponse> read(Object partitionKey) {
        return read(new CosmosItemRequestOptions(partitionKey));
    }

    /**
     * Reads an item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a cosmos item response with the read item
     * In case of failure the {@link Mono} will error.
     *
     * @param requestOptions the request comosItemRequestOptions
     * @return an {@link Mono} containing the cosmos item response with the read item or an error
     */
    public Mono<CosmosItemResponse> read(CosmosItemRequestOptions requestOptions) {
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(container.getDatabase().getDocClientWrapper()
                .readDocument(getLink(), requestOptions.toRequestOptions())
                .map(response -> new CosmosItemResponse(response, container)).toSingle()));
    }

    /**
     * Replaces an item with the passed in item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     *
     * @param item the item to replace (containing the document id).
     * @param partitionKey the partition key
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosItemResponse> replace(Object item, Object partitionKey){
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
     * @param requestOptions the request comosItemRequestOptions
     * @return an {@link Mono} containing the  cosmos item resource response with the replaced item or an error.
     */
    public Mono<CosmosItemResponse> replace(Object item, CosmosItemRequestOptions requestOptions){
        Document doc = CosmosItem.fromObject(item);
        return RxJava2Adapter.singleToMono(RxJavaInterop.toV2Single(container.getDatabase()
                .getDocClientWrapper()
                .replaceDocument(doc, requestOptions.toRequestOptions())
                .map(response -> new CosmosItemResponse(response, container)).toSingle()));
    }
    
    /**
     * Deletes the item.
     *
     * After subscription the operation will be performed. 
     * The {@link Mono} upon successful completion will contain a single cosmos item response with the replaced item.
     * In case of failure the {@link Mono} will error.
     * @param partitionKey
     * @return an {@link Mono} containing the  cosmos item resource response.
     */
    public Mono<CosmosItemResponse> delete(Object partitionKey) {
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
        return RxJava2Adapter.singleToMono(
                RxJavaInterop.toV2Single(container.getDatabase()
                                                 .getDocClientWrapper()
                                                 .deleteDocument(getLink(),options.toRequestOptions())
                                                 .map(response -> new CosmosItemResponse(response, container))
                                                 .toSingle()));
    }

    /**
     * Initialize an CosmosItem object from json string.
     *
     * @param jsonString the json string that represents the item object.
     * @param objectMapper the custom object mapper
     */
    public CosmosItem(String jsonString, ObjectMapper objectMapper) {
        super(jsonString, objectMapper);
    }
    
    void setContainer(CosmosContainer container) {
        this.container = container;
    }

    /**
     * fromObject retuns Document for compatibility with V2 sdk
     * @param cosmosItem
     * @return
     */
    static Document fromObject(Object cosmosItem) {
        Document typedItem;
        if (cosmosItem instanceof CosmosItem) {
            typedItem = new Document(((CosmosItem)cosmosItem).toJson());
        } else {
            try {
                return new Document(CosmosItem.mapper.writeValueAsString(cosmosItem));
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't serialize the object into the json string", e);
            }
        }
        return typedItem;
    }

    static List<CosmosItem> getFromV2Results(List<Document> results, CosmosContainer container) {
        return results.stream().map(document -> new CosmosItem(document.toJson(), container)).collect(Collectors.toList());
    }
    
    public <T> T getObject(Class<?> klass) throws IOException {
        return (T) mapper.readValue(this.toJson(), klass);
    }
    
    private String getLink(){
            StringBuilder builder = new StringBuilder();
            builder.append(container.getLink());
            builder.append("/");
            builder.append(Paths.DOCUMENTS_PATH_SEGMENT);
            builder.append("/");
            builder.append(getId());
            return builder.toString();
    }
}
