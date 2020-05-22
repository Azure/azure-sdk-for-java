// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx.examples.multimaster;

import com.azure.cosmos.implementation.AsyncDocumentClient;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.Database;
import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.ResourceResponse;
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
                            if (e instanceof CosmosException) {
                                CosmosException dce = (CosmosException) e;
                                if (dce.getStatusCode() ==  404) {
                                    // if doesn't exist create it

                                    Database d = new Database();
                                    d.setId(databaseName);

                                    return client.createDatabase(d, null);
                                }
                            }

                            return Mono.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }

    static public Mono<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, String collectionName) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collectionName), null)
                .onErrorResume(
                        e -> {
                            if (e instanceof CosmosException) {
                                CosmosException dce = (CosmosException) e;
                                if (dce.getStatusCode() ==  404) {
                                    // if doesn't exist create it

                                    DocumentCollection collection = new DocumentCollection();
                                    collection.setId(collectionName);

                                    return client.createCollection(createDatabaseUri(databaseName), collection, null);
                                }
                            }

                            return Mono.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }

    static public Mono<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, DocumentCollection collection) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collection.getId()), null)
                .onErrorResume(
                        e -> {
                            if (e instanceof CosmosException) {
                                CosmosException dce = (CosmosException) e;
                                if (dce.getStatusCode() ==  404) {
                                    // if doesn't exist create it

                                    return client.createCollection(createDatabaseUri(databaseName), collection, null);
                                }
                            }

                            return Mono.error(e);
                        }
                ).map(ResourceResponse::getResource).single();
    }
}
