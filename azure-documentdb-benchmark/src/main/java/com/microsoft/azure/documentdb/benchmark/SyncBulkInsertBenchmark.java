package com.microsoft.azure.documentdb.benchmark;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.microsoft.azure.documentdb.Database;
import com.microsoft.azure.documentdb.DatabaseAccount;
import com.microsoft.azure.documentdb.Document;
import com.microsoft.azure.documentdb.DocumentClient;
import com.microsoft.azure.documentdb.DocumentClientException;
import com.microsoft.azure.documentdb.DocumentCollection;
import com.microsoft.azure.documentdb.ResourceResponse;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.servo.ServoEventsListenerFactory;

public class SyncBulkInsertBenchmark extends AbstractBulkInsertBenchmark {

    private DocumentClient documentClient;
    private ListeningExecutorService executor;

    SyncBulkInsertBenchmark(Configuration cfg, Database database, DocumentCollection collection,
            DatabaseAccount databaseAccount) throws DocumentClientException {
        super(cfg, database, collection);
    }

    @Override
    protected void onInit() throws Exception {
        RxNetty.useMetricListenersFactory(new ServoEventsListenerFactory());
        documentClient = new DocumentClient(cfg.getServiceEndpoint(), cfg.getMasterKey(),
                cfg.getConnectionPolicy(), cfg.getConsistencyLevel());
        
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(cfg.getConcurrency(), cfg.getConcurrency(),
                10, TimeUnit.MINUTES, new ArrayBlockingQueue<Runnable>(cfg.getConcurrency(), true),
                new ThreadPoolExecutor.CallerRunsPolicy());

        this.executor = MoreExecutors.listeningDecorator(threadPoolExecutor);
    }

    @Override
    protected void onShutdown() throws Exception {
        this.executor.shutdown();
        this.documentClient.close();
    }

    @Override
    protected void createDocument(Document docToInsert, CallBack<ResourceResponse<Document>> onSuccess,
            CallBack<Throwable> onFailure) {

        Callable<ResourceResponse<Document>> documentCreateCallable = new Callable<ResourceResponse<Document>>() {

            @Override
            public ResourceResponse<Document> call() throws Exception {
                return documentClient.createDocument(collection.getSelfLink(), docToInsert, null, true);
            }
        };
        ListenableFuture<ResourceResponse<Document>> listenableFuture = this.executor.submit(documentCreateCallable);

        Futures.addCallback(listenableFuture, new FutureCallback<ResourceResponse<Document>>() {

            @Override
            public void onFailure(Throwable t) {
                onFailure.invoke(t);
            }

            @Override
            public void onSuccess(ResourceResponse<Document> resourceResponse) {
                onSuccess.invoke(resourceResponse);
            }
        } );
    }

    public static SyncBulkInsertBenchmark fromConfiguration(Configuration cfg) throws DocumentClientException {
        try (DocumentClient client = new DocumentClient(cfg.getServiceEndpoint(), cfg.getMasterKey(),
                cfg.getConnectionPolicy(), cfg.getConsistencyLevel())) {

            Database database = DocDBUtils.getDatabase(client, cfg.getDatabaseId());
            DocumentCollection collection = DocDBUtils.getCollection(client, database.getSelfLink(),
                    cfg.getCollectionId());
            DatabaseAccount databaseAccount = client.getDatabaseAccount();

            return new SyncBulkInsertBenchmark(cfg, database, collection, databaseAccount);
        }
    }
}
