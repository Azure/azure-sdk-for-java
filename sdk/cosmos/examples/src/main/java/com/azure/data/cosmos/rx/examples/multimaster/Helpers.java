// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.rx.examples.multimaster;

import com.azure.data.cosmos.internal.AsyncDocumentClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.internal.Database;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.ResourceResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Helpers {

    static public String createDocumentCollectionUri(String databaseName, String collectionName) {
        return String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    }

    static public String createDatabaseUri(String databaseName) {
        return String.format("/dbs/%s", databaseName);
    }

    static public Mono<Database> createDatabaseIfNotExists(AsyncDocumentClient client, String databaseName) {

        return client.readDatabase("/dbs/" + databaseName, null)
                .onErrorResume(
                        e -> {
                            if (e instanceof CosmosClientException) {
                                CosmosClientException dce = (CosmosClientException) e;
                                if (dce.statusCode() ==  404) {
                                    // if doesn't exist create it

                                    Database d = new Database();
                                    d.id(databaseName);

                                    return client.createDatabase(d, null);
                                }
                            }

                            return Flux.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }

    static public Mono<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, String collectionName) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collectionName), null)
                .onErrorResume(
                        e -> {
                            if (e instanceof CosmosClientException) {
                                CosmosClientException dce = (CosmosClientException) e;
                                if (dce.statusCode() ==  404) {
                                    // if doesn't exist create it

                                    DocumentCollection collection = new DocumentCollection();
                                    collection.id(collectionName);

                                    return client.createCollection(createDatabaseUri(databaseName), collection, null);
                                }
                            }

                            return Flux.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }

    static public Mono<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, DocumentCollection collection) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collection.id()), null)
                .onErrorResume(
                        e -> {
                            if (e instanceof CosmosClientException) {
                                CosmosClientException dce = (CosmosClientException) e;
                                if (dce.statusCode() ==  404) {
                                    // if doesn't exist create it

                                    return client.createCollection(createDatabaseUri(databaseName), collection, null);
                                }
                            }

                            return Flux.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }
}
