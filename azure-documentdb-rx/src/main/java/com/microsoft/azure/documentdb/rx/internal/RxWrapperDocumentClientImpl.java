/**
 * The MIT License (MIT)
 * Copyright (c) 2016 Microsoft Corporation
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
package com.microsoft.azure.documentdb.rx.internal;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.documentdb.Attachment;
import com.microsoft.azure.documentdb.ChangeFeedOptions;
import com.microsoft.azure.documentdb.Conflict;
import com.microsoft.azure.documentdb.ConnectionPolicy;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DatabaseAccount;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.FeedOptions;
import com.microsoft.azure.documentdb.FeedResponse;
import com.microsoft.azure.documentdb.FeedResponsePage;
import com.microsoft.azure.documentdb.MediaOptions;
import com.microsoft.azure.documentdb.MediaResponse;
import com.microsoft.azure.documentdb.Offer;
import com.microsoft.azure.documentdb.PartitionKeyRange;
import com.microsoft.azure.documentdb.Permission;
import com.microsoft.azure.documentdb.QueryIterable;
import com.microsoft.azure.documentdb.RequestOptions;
import com.microsoft.azure.documentdb.Resource;
import com.microsoft.azure.documentdb.ResourceResponse;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import com.microsoft.azure.documentdb.StoredProcedure;
import com.microsoft.azure.documentdb.StoredProcedureResponse;
import com.microsoft.azure.documentdb.Trigger;
import com.microsoft.azure.documentdb.User;
import com.microsoft.azure.documentdb.UserDefinedFunction;
import com.microsoft.azure.documentdb.internal.HttpConstants;
import com.microsoft.azure.documentdb.rx.AsyncDocumentClient;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func0;
import rx.internal.util.RxThreadFactory;
import rx.schedulers.Schedulers;

/**
 * This class provides a RX wrapper for existing blocking io operation, and is meant to be used internally.
 * For query APIs and a few other things we use this class, in the long term, after we implement all
 * APIs as non-blocking top to bottom, this will will be removed.
 * 
 */
public class RxWrapperDocumentClientImpl implements AsyncDocumentClient {

    private final Logger logger = LoggerFactory.getLogger(AsyncDocumentClient.class);
    private final DocumentClient client;
    private final Scheduler scheduler;
    private final ExecutorService executorService;

    public RxWrapperDocumentClientImpl(DocumentClient client) {
        this.client = client;

        int maxThreads = (int) (client.getConnectionPolicy().getMaxPoolSize() * 1.1);
        this.executorService =  new ThreadPoolExecutor(
                Math.min(8, maxThreads), // core thread pool size
                maxThreads, // maximum thread pool size
                30, // time to wait before killing idle threads
                TimeUnit.SECONDS, 
                new SynchronousQueue<>(),
                new RxThreadFactory("RxDocdb-io"),
                new ThreadPoolExecutor.CallerRunsPolicy());
        this.scheduler = Schedulers.from(executorService);
    }

    @Override
    public URI getServiceEndpoint() {
        return client.getServiceEndpoint();
    }

    @Override
    public URI getWriteEndpoint() {
        return client.getWriteEndpoint();
    }

    @Override
    public URI getReadEndpoint() {
        return client.getReadEndpoint();
    }

    @Override
    public ConnectionPolicy getConnectionPolicy() {
        return client.getConnectionPolicy();
    }
    
    private interface ImplFunc<T> {
        T invoke() throws Exception;
    }
    
    private <T> Observable<T> createDeferObservable(final ImplFunc<T> impl) {
        return Observable.defer(new Func0<Observable<T>>() {

            @Override
            public Observable<T> call() {

                try {
                    T rr = impl.invoke();
                    return Observable.just(rr);
                } catch (Exception e) {
                    return Observable.error(e);
                }
            }
        }).subscribeOn(scheduler);
    }
    
    private <T extends Resource> Observable<ResourceResponse<T>> createResourceResponseObservable(
            final ImplFunc<ResourceResponse<T>> impl) {
        logger.trace("Creating Observable<ResourceResponse<T>>");
        return Observable.defer(new Func0<Observable<ResourceResponse<T>>>() {

            @Override
            public Observable<ResourceResponse<T>> call() {

                try {
                    ResourceResponse<T> rr = impl.invoke();
                    return Observable.just(rr);
                } catch (Exception e) {
                    return Observable.error(flatten(e));
                }
            }
        }).subscribeOn(scheduler);
    }
    
    private int getHeaderItemCount(Map<String, String> header) {
        int pageSize = -1;
        try {
            String pageSizeHeaderValue = header!= null?header.get(HttpConstants.HttpHeaders.ITEM_COUNT):null;
            if (pageSizeHeaderValue != null) {
                pageSize = Integer.valueOf(pageSizeHeaderValue);
            } else {
                logger.debug("Page Item Count header is missing");
                pageSize = -1;
            }

        } catch (Exception e) {
            logger.debug("Page Item Count header is missing", e);
        }
        return pageSize;
    }

    private <T extends Resource> Observable<FeedResponsePage<T>> createFeedResponsePageObservable(final ImplFunc<FeedResponse<T>> impl) {
        return createFeedResponsePageObservable(impl, false);
    }
    
    private <T extends Resource> Observable<FeedResponsePage<T>> createFeedResponsePageObservable(final ImplFunc<FeedResponse<T>> impl, boolean isChangeFeed) {

        OnSubscribe<FeedResponsePage<T>> obs = new OnSubscribe<FeedResponsePage<T>>() {
            @Override
            public void call(Subscriber<? super FeedResponsePage<T>> subscriber) {
                
                try {
                    FeedResponse<T> feedResponse = impl.invoke();
                    
                    final QueryIterable<T> qi = feedResponse.getQueryIterable();

                    int numberOfPages = 0;
                    while (!subscriber.isUnsubscribed()) {
                        Map<String, String> header = feedResponse.getResponseHeaders();
                        logger.trace("Page Header key/value map {}", header);
                        int pageSize = getHeaderItemCount(header);

                        List<T> pageResults = qi.fetchNextBlock();
                        if (pageResults == null) {
                            pageResults = new ArrayList<>();
                        }

                        logger.trace("Found [{}] results in page with Header key/value map [{}]", pageResults.size(), header);

                        if (pageResults.size() != pageSize) {
                            logger.trace("Actual pageSize [{}] must match header page Size [{}] But it doesn't", pageResults.size(), pageSize);
                        }

                        if (pageResults.isEmpty() && (feedResponse.getResponseContinuation() == null || isChangeFeed)) {
                            // finished
                            break;
                        }

                        FeedResponsePage<T> frp = 
                                new FeedResponsePage<T>(pageResults, feedResponse.getResponseHeaders(), isChangeFeed);
                        subscriber.onNext(frp);
                        numberOfPages++;
                    }

                    if (!subscriber.isUnsubscribed() && numberOfPages == 0) {
                        // if no results, return one single feed response page containing the response headers
                        subscriber.onNext(new FeedResponsePage<>(new ArrayList<>(), feedResponse.getResponseHeaders(), isChangeFeed));
                    }
                } catch (Exception e) {
                    logger.debug("Query Failed due to [{}]", e.getMessage(), e);
                    if (!subscriber.isUnsubscribed()) {
                        subscriber.onError(flatten(e));
                    }
                    return;
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
                return;
            }
        };
        return Observable.defer(() -> Observable.create(obs)).subscribeOn(scheduler);
    }
    
    private Throwable flatten(Throwable e) {
        while (e instanceof RuntimeException && e.getCause() != null) {
            e = e.getCause();
        }
        return e;
    }

    @Override
    public Observable<ResourceResponse<Database>> createDatabase(final Database database, final RequestOptions options) {
        
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Database>>() {
            @Override
            public ResourceResponse<Database> invoke() throws Exception {
                return client.createDatabase(database, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Database>> deleteDatabase(final String databaseLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Database>>() {
            @Override
            public ResourceResponse<Database> invoke() throws Exception {
                return client.deleteDatabase(databaseLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Database>> readDatabase(final String databaseLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Database>>() {
            @Override
            public ResourceResponse<Database> invoke() throws Exception {
                return client.readDatabase(databaseLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Database>> readDatabases(final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Database>>() {
            @Override
            public FeedResponse<Database> invoke() throws Exception {
                return client.readDatabases(options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Database>> queryDatabases(final String query, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Database>>() {
            @Override
            public FeedResponse<Database> invoke() throws Exception {
                return client.queryDatabases(query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Database>> queryDatabases(final SqlQuerySpec querySpec, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Database>>() {
            @Override
            public FeedResponse<Database> invoke() throws Exception {
                return client.queryDatabases(querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> createCollection(final String databaseLink,
            final DocumentCollection collection, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<DocumentCollection>>() {
            @Override
            public ResourceResponse<DocumentCollection> invoke() throws Exception {
                return client.createCollection(databaseLink, collection, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> replaceCollection(final DocumentCollection collection,
            final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<DocumentCollection>>() {
            @Override
            public ResourceResponse<DocumentCollection> invoke() throws Exception {
                return client.replaceCollection(collection, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> deleteCollection(final String collectionLink,
            final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<DocumentCollection>>() {
            @Override
            public ResourceResponse<DocumentCollection> invoke() throws Exception {
                return client.deleteCollection(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<DocumentCollection>> readCollection(final String collectionLink,
            final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<DocumentCollection>>() {
            @Override
            public ResourceResponse<DocumentCollection> invoke() throws Exception {
                return client.readCollection(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<DocumentCollection>> readCollections(final String databaseLink,
            final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<DocumentCollection>>() {
            @Override
            public FeedResponse<DocumentCollection> invoke() throws Exception {
                return client.readCollections(databaseLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<DocumentCollection>> queryCollections(final String databaseLink, final String query,
            final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<DocumentCollection>>() {
            @Override
            public FeedResponse<DocumentCollection> invoke() throws Exception {
                return client.queryCollections(databaseLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<DocumentCollection>> queryCollections(final String databaseLink, final
            SqlQuerySpec querySpec, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<DocumentCollection>>() {
            @Override
            public FeedResponse<DocumentCollection> invoke() throws Exception {
                return client.queryCollections(databaseLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> createDocument(final String collectionLink, final Object document, final
            RequestOptions options, final boolean disableAutomaticIdGeneration) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.createDocument(collectionLink, document, options, disableAutomaticIdGeneration);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> upsertDocument(final String collectionLink, final Object document, final
            RequestOptions options, final boolean disableAutomaticIdGeneration) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.upsertDocument(collectionLink, document, options, disableAutomaticIdGeneration);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> replaceDocument(final String documentLink, final Object document, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.replaceDocument(documentLink, document, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> replaceDocument(final Document document, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.replaceDocument(document, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> deleteDocument(final String documentLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.deleteDocument(documentLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Document>> readDocument(final String documentLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Document>>() {
            @Override
            public ResourceResponse<Document> invoke() throws Exception {
                return client.readDocument(documentLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> readDocuments(final String collectionLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.readDocuments(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> queryDocuments(final String collectionLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.queryDocuments(collectionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> queryDocuments(final String collectionLink, final String query, final
            FeedOptions options, final Object partitionKey) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.queryDocuments(collectionLink, query, options, partitionKey);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> queryDocuments(final String collectionLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.queryDocuments(collectionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> queryDocuments(final String collectionLink, final SqlQuerySpec querySpec, final
            FeedOptions options, final Object partitionKey) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.queryDocuments(collectionLink, querySpec, options, partitionKey);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Document>> queryDocumentChangeFeed(final String collectionLink, final
            ChangeFeedOptions changeFeedOptions) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Document>>() {
            @Override
            public FeedResponse<Document> invoke() throws Exception {
                return client.queryDocumentChangeFeed(collectionLink, changeFeedOptions);
            }
        }, true);
    }
    
    @Override
    public Observable<FeedResponsePage<PartitionKeyRange>> readPartitionKeyRanges(final String collectionLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<PartitionKeyRange>>() {
            @Override
            public FeedResponse<PartitionKeyRange> invoke() throws Exception {
                return client.readPartitionKeyRanges(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> createStoredProcedure(final String collectionLink, final
            StoredProcedure storedProcedure, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<StoredProcedure>>() {
            @Override
            public ResourceResponse<StoredProcedure> invoke() throws Exception {
                return client.createStoredProcedure(collectionLink, storedProcedure, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> upsertStoredProcedure(final String collectionLink, final
            StoredProcedure storedProcedure, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<StoredProcedure>>() {
            @Override
            public ResourceResponse<StoredProcedure> invoke() throws Exception {
                return client.upsertStoredProcedure(collectionLink, storedProcedure, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> replaceStoredProcedure(final StoredProcedure storedProcedure, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<StoredProcedure>>() {
            @Override
            public ResourceResponse<StoredProcedure> invoke() throws Exception {
                return client.replaceStoredProcedure(storedProcedure, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> deleteStoredProcedure(final String storedProcedureLink, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<StoredProcedure>>() {
            @Override
            public ResourceResponse<StoredProcedure> invoke() throws Exception {
                return client.deleteStoredProcedure(storedProcedureLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<StoredProcedure>> readStoredProcedure(final String storedProcedureLink, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<StoredProcedure>>() {
            @Override
            public ResourceResponse<StoredProcedure> invoke() throws Exception {
                return client.readStoredProcedure(storedProcedureLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<StoredProcedure>> readStoredProcedures(final String collectionLink, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<StoredProcedure>>() {
            @Override
            public FeedResponse<StoredProcedure> invoke() throws Exception {
                return client.readStoredProcedures(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<StoredProcedure>> queryStoredProcedures(final String collectionLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<StoredProcedure>>() {
            @Override
            public FeedResponse<StoredProcedure> invoke() throws Exception {
                return client.queryStoredProcedures(collectionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<StoredProcedure>> queryStoredProcedures(final String collectionLink, final
            SqlQuerySpec querySpec, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<StoredProcedure>>() {
            @Override
            public FeedResponse<StoredProcedure> invoke() throws Exception {
                return client.queryStoredProcedures(collectionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<StoredProcedureResponse> executeStoredProcedure(final String storedProcedureLink, final Object[] procedureParams) {
        return this.createDeferObservable(new ImplFunc<StoredProcedureResponse>() {
            @Override
            public StoredProcedureResponse invoke() throws Exception {
                return client.executeStoredProcedure(storedProcedureLink, procedureParams);
            }
        });
    }

    @Override
    public Observable<StoredProcedureResponse> executeStoredProcedure(final String storedProcedureLink, final RequestOptions options, final
            Object[] procedureParams) {
        return this.createDeferObservable(new ImplFunc<StoredProcedureResponse>() {
            @Override
            public StoredProcedureResponse invoke() throws Exception {
                return client.executeStoredProcedure(storedProcedureLink, options, procedureParams);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Trigger>> createTrigger(final String collectionLink, final Trigger trigger, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Trigger>>() {
            @Override
            public ResourceResponse<Trigger> invoke() throws Exception {
                return client.createTrigger(collectionLink, trigger, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Trigger>> upsertTrigger(final String collectionLink, final Trigger trigger, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Trigger>>() {
            @Override
            public ResourceResponse<Trigger> invoke() throws Exception {
                return client.createTrigger(collectionLink, trigger, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Trigger>> replaceTrigger(final Trigger trigger, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Trigger>>() {
            @Override
            public ResourceResponse<Trigger> invoke() throws Exception {
                return client.replaceTrigger(trigger, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Trigger>> deleteTrigger(final String triggerLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Trigger>>() {
            @Override
            public ResourceResponse<Trigger> invoke() throws Exception {
                return client.deleteTrigger(triggerLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Trigger>> readTrigger(final String triggerLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Trigger>>() {
            @Override
            public ResourceResponse<Trigger> invoke() throws Exception {
                return client.readTrigger(triggerLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Trigger>> readTriggers(final String collectionLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Trigger>>() {
            @Override
            public FeedResponse<Trigger> invoke() throws Exception {
                return client.readTriggers(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Trigger>> queryTriggers(final String collectionLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Trigger>>() {
            @Override
            public FeedResponse<Trigger> invoke() throws Exception {
                return client.queryTriggers(collectionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Trigger>> queryTriggers(final String collectionLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Trigger>>() {
            @Override
            public FeedResponse<Trigger> invoke() throws Exception {
                return client.queryTriggers(collectionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> createUserDefinedFunction(final String collectionLink, final
            UserDefinedFunction udf, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<UserDefinedFunction>>() {
            @Override
            public ResourceResponse<UserDefinedFunction> invoke() throws Exception {
                return client.createUserDefinedFunction(collectionLink, udf, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> upsertUserDefinedFunction(final String collectionLink, final
            UserDefinedFunction udf, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<UserDefinedFunction>>() {
            @Override
            public ResourceResponse<UserDefinedFunction> invoke() throws Exception {
                return client.upsertUserDefinedFunction(collectionLink, udf, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> replaceUserDefinedFunction(final UserDefinedFunction udf, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<UserDefinedFunction>>() {
            @Override
            public ResourceResponse<UserDefinedFunction> invoke() throws Exception {
                return client.replaceUserDefinedFunction(udf, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> deleteUserDefinedFunction(final String udfLink, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<UserDefinedFunction>>() {
            @Override
            public ResourceResponse<UserDefinedFunction> invoke() throws Exception {
                return client.deleteUserDefinedFunction(udfLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<UserDefinedFunction>> readUserDefinedFunction(final String udfLink, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<UserDefinedFunction>>() {
            @Override
            public ResourceResponse<UserDefinedFunction> invoke() throws Exception {
                return client.readUserDefinedFunction(udfLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<UserDefinedFunction>> readUserDefinedFunctions(final String collectionLink, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<UserDefinedFunction>>() {
            @Override
            public FeedResponse<UserDefinedFunction> invoke() throws Exception {
                return client.readUserDefinedFunctions(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<UserDefinedFunction>> queryUserDefinedFunctions(final String collectionLink, final
            String query, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<UserDefinedFunction>>() {
            @Override
            public FeedResponse<UserDefinedFunction> invoke() throws Exception {
                return client.queryUserDefinedFunctions(collectionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<UserDefinedFunction>> queryUserDefinedFunctions(final String collectionLink, final
            SqlQuerySpec querySpec, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<UserDefinedFunction>>() {
            @Override
            public FeedResponse<UserDefinedFunction> invoke() throws Exception {
                return client.queryUserDefinedFunctions(collectionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> createAttachment(final String documentLink, final Attachment attachment, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.createAttachment(documentLink, attachment , options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> upsertAttachment(final String documentLink, final Attachment attachment, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.upsertAttachment(documentLink, attachment , options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> replaceAttachment(final Attachment attachment, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.replaceAttachment(attachment , options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> deleteAttachment(final String attachmentLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.deleteAttachment(attachmentLink , options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> readAttachment(final String attachmentLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.readAttachment(attachmentLink , options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Attachment>> readAttachments(final String documentLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Attachment>>() {
            @Override
            public FeedResponse<Attachment> invoke() throws Exception {
                return client.readAttachments(documentLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Attachment>> queryAttachments(final String documentLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Attachment>>() {
            @Override
            public FeedResponse<Attachment> invoke() throws Exception {
                return client.queryAttachments(documentLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Attachment>> queryAttachments(final String documentLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Attachment>>() {
            @Override
            public FeedResponse<Attachment> invoke() throws Exception {
                return client.queryAttachments(documentLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> createAttachment(final String documentLink, final InputStream mediaStream, final
            MediaOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.createAttachment(documentLink, mediaStream, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Attachment>> upsertAttachment(final String documentLink, final InputStream mediaStream, final
            MediaOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Attachment>>() {
            @Override
            public ResourceResponse<Attachment> invoke() throws Exception {
                return client.upsertAttachment(documentLink, mediaStream, options);
            }
        });
    }

    @Override
    public Observable<MediaResponse> readMedia(final String mediaLink) {
        return this.createDeferObservable(new ImplFunc<MediaResponse>() {
            @Override
            public MediaResponse invoke() throws Exception {
                return client.readMedia(mediaLink);
            }
        });
    }

    @Override
    public Observable<MediaResponse> updateMedia(final String mediaLink, final InputStream mediaStream, final MediaOptions options) {
        return this.createDeferObservable(new ImplFunc<MediaResponse>() {
            @Override
            public MediaResponse invoke() throws Exception {
                return client.updateMedia(mediaLink, mediaStream, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Conflict>> readConflict(final String conflictLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Conflict>>() {
            @Override
            public ResourceResponse<Conflict> invoke() throws Exception {
                return client.readConflict(conflictLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Conflict>> readConflicts(final String collectionLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Conflict>>() {
            @Override
            public FeedResponse<Conflict> invoke() throws Exception {
                return client.readConflicts(collectionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Conflict>> queryConflicts(final String collectionLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Conflict>>() {
            @Override
            public FeedResponse<Conflict> invoke() throws Exception {
                return client.queryConflicts(collectionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Conflict>> queryConflicts(final String collectionLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Conflict>>() {
            @Override
            public FeedResponse<Conflict> invoke() throws Exception {
                return client.queryConflicts(collectionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Conflict>> deleteConflict(final String conflictLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Conflict>>() {
            @Override
            public ResourceResponse<Conflict> invoke() throws Exception {
                return client.deleteConflict(conflictLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<User>> createUser(final String databaseLink, final User user, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<User>>() {
            @Override
            public ResourceResponse<User> invoke() throws Exception {
                return client.createUser(databaseLink, user, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<User>> upsertUser(final String databaseLink, final User user, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<User>>() {
            @Override
            public ResourceResponse<User> invoke() throws Exception {
                return client.upsertUser(databaseLink, user, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<User>> replaceUser(final User user, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<User>>() {
            @Override
            public ResourceResponse<User> invoke() throws Exception {
                return client.replaceUser(user, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<User>> deleteUser(final String userLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<User>>() {
            @Override
            public ResourceResponse<User> invoke() throws Exception {
                return client.deleteUser(userLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<User>> readUser(final String userLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<User>>() {
            @Override
            public ResourceResponse<User> invoke() throws Exception {
                return client.readUser(userLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<User>> readUsers(final String databaseLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<User>>() {
            @Override
            public FeedResponse<User> invoke() throws Exception {
                return client.readUsers(databaseLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<User>> queryUsers(final String databaseLink, final String query, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<User>>() {
            @Override
            public FeedResponse<User> invoke() throws Exception {
                return client.queryUsers(databaseLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<User>> queryUsers(final String databaseLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<User>>() {
            @Override
            public FeedResponse<User> invoke() throws Exception {
                return client.queryUsers(databaseLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Permission>> createPermission(final String userLink, final Permission permission, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Permission>>() {
            @Override
            public ResourceResponse<Permission> invoke() throws Exception {
                return client.createPermission(userLink, permission, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Permission>> upsertPermission(final String userLink, final Permission permission, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Permission>>() {
            @Override
            public ResourceResponse<Permission> invoke() throws Exception {
                return client.upsertPermission(userLink, permission, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Permission>> replacePermission(final Permission permission, final
            RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Permission>>() {
            @Override
            public ResourceResponse<Permission> invoke() throws Exception {
                return client.replacePermission(permission, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Permission>> deletePermission(final String permissionLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Permission>>() {
            @Override
            public ResourceResponse<Permission> invoke() throws Exception {
                return client.deletePermission(permissionLink, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Permission>> readPermission(final String permissionLink, final RequestOptions options) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Permission>>() {
            @Override
            public ResourceResponse<Permission> invoke() throws Exception {
                return client.readPermission(permissionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Permission>> readPermissions(final String permissionLink, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Permission>>() {
            @Override
            public FeedResponse<Permission> invoke() throws Exception {
                return client.readPermissions(permissionLink, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Permission>> queryPermissions(final String permissionLink, final String query, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Permission>>() {
            @Override
            public FeedResponse<Permission> invoke() throws Exception {
                return client.queryPermissions(permissionLink, query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Permission>> queryPermissions(final String permissionLink, final SqlQuerySpec querySpec, final
            FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Permission>>() {
            @Override
            public FeedResponse<Permission> invoke() throws Exception {
                return client.queryPermissions(permissionLink, querySpec, options);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Offer>> replaceOffer(final Offer offer) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Offer>>() {
            @Override
            public ResourceResponse<Offer> invoke() throws Exception {
                return client.replaceOffer(offer);
            }
        });
    }

    @Override
    public Observable<ResourceResponse<Offer>> readOffer(final String offerLink) {
        return this.createResourceResponseObservable(new ImplFunc<ResourceResponse<Offer>>() {
            @Override
            public ResourceResponse<Offer> invoke() throws Exception {
                return client.readOffer(offerLink);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Offer>> readOffers(final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Offer>>() {
            @Override
            public FeedResponse<Offer> invoke() throws Exception {
                return client.readOffers(options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Offer>> queryOffers(final String query, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Offer>>() {
            @Override
            public FeedResponse<Offer> invoke() throws Exception {
                return client.queryOffers(query, options);
            }
        });
    }

    @Override
    public Observable<FeedResponsePage<Offer>> queryOffers(final SqlQuerySpec querySpec, final FeedOptions options) {
        return this.createFeedResponsePageObservable(new ImplFunc<FeedResponse<Offer>>() {
            @Override
            public FeedResponse<Offer> invoke() throws Exception {
                return client.queryOffers(querySpec, options);
            }
        });
    }

    @Override
    public Observable<DatabaseAccount> getDatabaseAccount() {
        return this.createDeferObservable(new ImplFunc<DatabaseAccount>() {
            @Override
            public DatabaseAccount invoke() throws Exception {
                return client.getDatabaseAccount();
            }
        });
    }
    
    private void safeShutdownExecutorService(ExecutorService exS) {
        if (exS == null) {
            return;
        }
        
        try {
            exS.shutdown();
            exS.awaitTermination(15, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.warn("Failure in shutting down a executor service", e);
        }
    }

    @Override
    public void close() {
        safeShutdownExecutorService(this.executorService);
        client.close();
    }
}
