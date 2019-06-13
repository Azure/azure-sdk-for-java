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

package com.azure.data.cosmos.rx.examples.multimaster;

import com.azure.data.cosmos.AsyncDocumentClient;
import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.Database;
import com.azure.data.cosmos.DocumentCollection;
import com.azure.data.cosmos.ResourceResponse;
import rx.Observable;
import rx.Single;

public class Helpers {

    static public String createDocumentCollectionUri(String databaseName, String collectionName) {
        return String.format("/dbs/%s/colls/%s", databaseName, collectionName);
    }

    static public String createDatabaseUri(String databaseName) {
        return String.format("/dbs/%s", databaseName);
    }

    static public Single<Database> createDatabaseIfNotExists(AsyncDocumentClient client, String databaseName) {

        return client.readDatabase("/dbs/" + databaseName, null)
                .onErrorResumeNext(
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

                            return Observable.error(e);
                        }
                ).map(ResourceResponse::getResource).toSingle();
    }

    static public Single<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, String collectionName) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collectionName), null)
                .onErrorResumeNext(
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

                            return Observable.error(e);
                        }
                ).map(ResourceResponse::getResource).toSingle();
    }

    static public Single<DocumentCollection> createCollectionIfNotExists(AsyncDocumentClient client, String databaseName, DocumentCollection collection) {
        return client.readCollection(createDocumentCollectionUri(databaseName, collection.id()), null)
                .onErrorResumeNext(
                        e -> {
                            if (e instanceof CosmosClientException) {
                                CosmosClientException dce = (CosmosClientException) e;
                                if (dce.statusCode() ==  404) {
                                    // if doesn't exist create it

                                    return client.createCollection(createDatabaseUri(databaseName), collection, null);
                                }
                            }

                            return Observable.error(e);
                        }
                ).map(ResourceResponse::getResource).toSingle();
    }
}
